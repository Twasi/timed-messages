package at.saith.twasi.service;

import at.saith.twasi.TimedMessagesPlugin;
import at.saith.twasi.database.TimerEntity;
import at.saith.twasi.database.TimerRepository;
import at.saith.twasi.service.exception.*;
import net.twasi.core.database.models.User;
import net.twasi.core.interfaces.api.TwasiInterface;
import net.twasi.core.logger.TwasiLogger;
import net.twasi.core.models.Streamer;
import net.twasi.core.plugin.api.customcommands.TwasiCustomCommand;
import net.twasi.core.services.IService;
import net.twasi.core.services.ServiceRegistry;
import net.twasi.core.services.providers.DataService;
import net.twasiplugin.commands.CommandRepository;
import net.twasiplugin.commands.CustomCommand;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

        stopTimers(twasiInterface);

        List<TwasiTimer> timers = new ArrayList<>();
        registeredTimers.put(user.getId(), timers);

        for (TimerEntity timer : getTimersForUser(user)) {
            timers.add(new TwasiTimer(twasiInterface, timer.getCommand(), timer.getInterval(), timer.isEnabled()));
        }
    }

    public void stopTimers(TwasiInterface twasiInterface) {
        User user = twasiInterface.getStreamer().getUser();
        TwasiLogger.log.debug("Starting timers for user " + user.getTwitchAccount().getDisplayName());

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

    public TimerEntity getTimerEntityForUserAndCommand(User user, String command) throws TimerException {
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

    public TimerEntity registerTimer(TwasiInterface twasiInterface, String command, int interval) throws TimerException {
        Streamer streamer = twasiInterface.getStreamer();
        User user = streamer.getUser();

        TwasiLogger.log.debug("Trying to register new timer for user " + user.getTwitchAccount().getDisplayName() + " for the command " + command);

        if (interval < 1)
            throw new TooLowIntervalException("The Interval " + interval + " is too low.");

        TimerEntity timer = repository.getTimerForUserAndCommand(user, command);
        if (timer != null) {
            throw new TimerAlreadyExistsException("A Timer for the command " + command + " already exists.");
        }
        boolean exists = commandExists(twasiInterface, command);

        if (!exists) throw new CommandNotFoundException("The Command " + command + "doesn't exist.");

        TwasiCustomCommand cmd = twasiInterface.getCustomCommands().stream().filter(c -> c.getCommandName().equalsIgnoreCase(command)).findFirst().get();
        if (!cmd.allowsTimer())
            throw new NotAllowedTimerException("The command " + cmd.getCommandName() + " doesn't allow timers!");

        timer = new TimerEntity(user, command, interval, true);
        repository.add(timer);

        if (hasTimersEnabled(user)) {
            TwasiLogger.log.debug("Timer for the command " + command + " was registered.");
            registeredTimers.get(user.getId()).add(new TwasiTimer(twasiInterface, command, interval, true));
        }
        return timer;
    }

    public TimerEntity removeTimer(TwasiInterface twasiInterface, String command) throws TimerException {
        User user = twasiInterface.getStreamer().getUser();

        TimerEntity entity = getTimerEntityForUserAndCommand(user, command);
        if (hasTimersEnabled(user)) {
            List<TwasiTimer> timers = getRunningTimersForUser(user);
            for (TwasiTimer timer : timers) {
                if (timer.getCommand().equalsIgnoreCase(command)) {
                    timer.disable();
                    timers.remove(timer);
                    break;
                }
            }
        }
        repository.remove(entity);
        return entity;
    }

    public void enableTimer(TwasiInterface twasiInterface, String command, boolean enabled) throws TimerException {
        User user = twasiInterface.getStreamer().getUser();

        TimerEntity entity = getTimerEntityForUserAndCommand(user, command);
        entity.setEnabled(enabled);

        repository.commit(entity);

        if (hasTimersEnabled(user)) {
            if (enabled) {
                TwasiTimer timer = new TwasiTimer(twasiInterface, command, entity.getInterval(), true);
                List<TwasiTimer> timers = this.registeredTimers.get(user.getId());
                timers.add(timer);
                return;
            }
            List<TwasiTimer> timers = getRunningTimersForUser(user);
            for (TwasiTimer timer : timers) {
                if (timer.getCommand().equalsIgnoreCase(command)) {
                    getRunningTimersForUser(user).remove(timer);
                    timer.disable();
                    return;
                }
            }
        }
    }

    boolean commandExists(TwasiInterface twasiInterface, String command) {
        return
                twasiInterface
                        .getCustomCommands()
                        .stream().anyMatch(cmd -> cmd.getCommandName().equalsIgnoreCase(command))
                        || commandRepository
                        .getAllCommands(twasiInterface.getStreamer().getUser())
                        .stream().anyMatch(cmd -> cmd.getName().equalsIgnoreCase(command));
    }
}
