package at.saith.twasi.service;

import at.saith.twasi.TimedMessagesPlugin;
import at.saith.twasi.database.TimerEntity;
import at.saith.twasi.database.TimerRepository;
import at.saith.twasi.service.exception.*;
import net.twasi.core.database.models.User;
import net.twasi.core.interfaces.api.TwasiInterface;
import net.twasi.core.logger.TwasiLogger;
import net.twasi.core.plugin.api.customcommands.TwasiCustomCommand;
import net.twasi.core.services.IService;
import net.twasi.core.services.ServiceRegistry;
import net.twasi.core.services.providers.DataService;
import net.twasiplugin.commands.database.CommandRepository;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class TimerService implements IService {

    private final HashMap<ObjectId, List<TwasiTimer>> registeredTimers;
    private final TimerRepository repository;
    private final CommandRepository commandRepository;

    public TimerService() {
        registeredTimers = new HashMap<>();
        repository = ServiceRegistry.get(DataService.class).get(TimerRepository.class);
        commandRepository = ServiceRegistry.get(DataService.class).get(CommandRepository.class);
    }

    public void startTimers(TwasiInterface twasiInterface) {
        deleteUnusedTimers(twasiInterface);//Cleaning up the database
        User user = twasiInterface.getStreamer().getUser();
        TwasiLogger.log.debug("Starting timers for user " + user.getTwitchAccount().getDisplayName());

        stopTimers(user);//Make sure that no timer runs beforehand

        List<TwasiTimer> timers = new ArrayList<>();
        registeredTimers.put(user.getId(), timers);
        List<TimerEntity> timerEntities = getTimersForUser(user);
        int i = 0;

        for (TimerEntity timer : timerEntities) {
            startTimer(twasiInterface, timer.getCommand(), timer.getInterval(), timer.isEnabled(), i++ * 30);
        }
    }

    private void startTimer(TwasiInterface twasiInterface, String command, int interval, boolean enabled, int initialDelay) {

        User user = twasiInterface.getStreamer().getUser();
        if (hasTimersEnabled(user)) {
            List<TwasiTimer> timers = registeredTimers.get(user.getId());
            TwasiTimer timer = new TwasiTimer(twasiInterface, command, interval, enabled);
            if (timers.contains(timer)) {
                timer = timers.get(timers.indexOf(timer));
            }
            timer.start(initialDelay);
            timers.add(timer);
            TwasiLogger.log.debug("Started timer " + command + " for User " + user.getTwitchAccount().getDisplayName());
        } else {
            TwasiLogger.log.debug("Tried to start timer " + command + " but User " + user.getTwitchAccount().getDisplayName() + " hasn't timers enabled.");
        }
    }

    public void stopTimers(User user) {
        TwasiLogger.log.debug("Stopping timers for user " + user.getTwitchAccount().getDisplayName());

        List<TwasiTimer> runningTimers = getRunningTimersForUser(user);
        runningTimers.forEach(TwasiTimer::disable);

        registeredTimers.remove(user.getId());
    }

    //Deletes all Timers which commands have been deleted
    private void deleteUnusedTimers(TwasiInterface twasiInterface) {
        User user = twasiInterface.getStreamer().getUser();
        List<TimerEntity> timers = getTimersForUser(user);
        for (TimerEntity timer : timers) {
            if (!commandExists(twasiInterface, timer.getCommand())) {
                repository.remove(timer);//remove timer from Database
                timers.remove(timer);
            }
        }

    }

    public TwasiTimer getTimerForUserAndCommand(User user, String command) {
        List<TwasiTimer> timers = registeredTimers.get(user.getId());
        for (TwasiTimer timer : timers)
            if (timer.getCommand().equalsIgnoreCase(command)) return timer;
        return null;
    }

    public TimerEntity getTimerEntityForUserAndCommand(User user, String command) throws TimerNotFoundException {
        for (TimerEntity entity : getTimersForUser(user)) {
            if (entity.getCommand().equalsIgnoreCase(command)) return entity;
        }
        throw new TimerNotFoundException("The timer for the command " + command + " and the user" + user.getTwitchAccount().getDisplayName() + " doesn't exist.");
    }

    public List<TimerEntity> getTimersForUser(User user) {
        List<TimerEntity> timers = repository.getUserTimers(user);
        if (timers == null || timers.size() == 0)
            return new ArrayList<>(); // Return empty list if there are no timers yet
        return timers;
    }

    public List<TwasiTimer> getRunningTimersForUser(User user) {
        List<TwasiTimer> timers = this.registeredTimers.get(user.getId());

        return timers != null ? timers : new ArrayList<>();
    }

    public boolean hasTimersEnabled(User user) {
        return registeredTimers.get(user.getId()) != null;
    }

    public TimerEntity registerTimer(TwasiInterface twasiInterface, String command, int interval, boolean enabled) throws TimerException {
        User user = twasiInterface.getStreamer().getUser();

        TwasiLogger.log.debug("Trying to register new timer for user " + user.getTwitchAccount().getDisplayName() + " for the command " + command);

        if (interval < 1)
            throw new TooLowIntervalException("The Interval " + interval + " is too low.");


        if (timerExists(user, command)) {
            throw new TimerAlreadyExistsException("A Timer for the command " + command + " already exists.");
        }
        boolean exists = commandExists(twasiInterface, command);

        if (!exists) throw new CommandNotFoundException("The Command " + command + " doesn't exist.");

        final String commandName = command; //Have to make a new Variable because it might change later

        Optional<TwasiCustomCommand> cmd = twasiInterface.getCustomCommands().stream().filter(c -> c.getCommandName().equalsIgnoreCase(commandName)).findFirst();
        if (cmd.isPresent() && !cmd.get().allowsTimer())
            throw new NotAllowedTimerException("The command " + cmd.get().getCommandName() + " doesn't allow timers!");

        if (cmd.isPresent() && !command.startsWith(TimedMessagesPlugin.COMMAND_PREFIX)) {
            command = TimedMessagesPlugin.COMMAND_PREFIX + command;
        }

        TimerEntity timer = new TimerEntity(user, command, interval, enabled);
        repository.add(timer);
        TwasiLogger.log.debug("Timer for the command " + command + " was registered.");

        startTimer(twasiInterface, command, interval, enabled, 0);

        return timer;
    }

    public TimerEntity removeTimer(TwasiInterface twasiInterface, String command) throws TimerException {
        User user = twasiInterface.getStreamer().getUser();

        TimerEntity entity = getTimerEntityForUserAndCommand(user, command);
        if (hasTimersEnabled(user)) {
            disableTimer(user, command);
        }
        repository.remove(entity);
        return entity;
    }

    public TimerEntity updateTimer(TwasiInterface twasiInterface, String command, String newCommand, int newInterval, boolean enabled) throws TimerException {
        if (command.equals(newCommand)) {
            TimerEntity entity = enableTimer(twasiInterface, command, enabled);
            entity.setInterval(newInterval);
            repository.commit(entity);
            return entity;
        } else {
            TimerEntity entity = registerTimer(twasiInterface, newCommand, newInterval, enabled);
            removeTimer(twasiInterface, command);
            return entity;
        }
    }

    public TimerEntity enableTimer(TwasiInterface twasiInterface, String command, boolean enabled) throws TimerException {
        User user = twasiInterface.getStreamer().getUser();

        TimerEntity entity = getTimerEntityForUserAndCommand(user, command);
        entity.setEnabled(enabled);

        repository.commit(entity);


        if (enabled) {
            startTimer(twasiInterface, command, entity.getInterval(), true, 0);
        } else {
            disableTimer(user, command);
        }

        return entity;
    }

    private void disableTimer(User user, String command) {
        if (hasTimersEnabled(user)) {
            List<TwasiTimer> timers = getRunningTimersForUser(user);
            Optional<TwasiTimer> timer = timers.stream().filter(t -> t.getCommand().equalsIgnoreCase(command)).findFirst();
            if (!timer.isPresent()) return;

            timer.get().disable();
            timers.remove(timer.get());
        }
    }

    public boolean timerExists(User user, String command) {
        return repository.getTimerForUserAndCommand(user, command) != null;
    }

    boolean commandExists(TwasiInterface twasiInterface, String command) {
        return
                twasiInterface
                        .getCustomCommands()
                        .stream().anyMatch(cmd -> cmd.getCommandName().equalsIgnoreCase(command) || cmd.getFormattedCommandName().equalsIgnoreCase(command))
                        || commandRepository
                        .getAllCommands(twasiInterface.getStreamer().getUser())
                        .stream().anyMatch(cmd -> cmd.getName().equalsIgnoreCase(command));
    }


}
