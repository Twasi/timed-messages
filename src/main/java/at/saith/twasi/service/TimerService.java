package at.saith.twasi.service;

import at.saith.twasi.database.TimerEntity;
import at.saith.twasi.database.TimerRepository;
import at.saith.twasi.service.exception.TimerAlreadyExistsException;
import at.saith.twasi.service.exception.TimerException;
import at.saith.twasi.service.exception.TooLowIntervalException;
import net.twasi.core.database.models.User;
import net.twasi.core.interfaces.api.TwasiInterface;
import net.twasi.core.logger.TwasiLogger;
import net.twasi.core.models.Streamer;
import net.twasi.core.plugin.api.TwasiUserPlugin;
import net.twasi.core.services.IService;
import net.twasi.core.services.ServiceRegistry;
import net.twasi.core.services.providers.config.ConfigService;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;

public class TimerService implements IService {
    private HashMap<String, List<Timer>> registeredTimers;
    private TimerRepository repository;
    private static final String COMMAND_PREFIX = ServiceRegistry.get(ConfigService.class).getCatalog().bot.prefix;
    public TimerEntity registerTimer(TwasiUserPlugin userPlugin, String command, int interval) throws TimerException {

        TwasiInterface twasiInterface = userPlugin.getTwasiInterface();
        Streamer streamer = twasiInterface.getStreamer();
        User user = streamer.getUser();

        TwasiLogger.log.debug("Trying to register new timer for user " + user.getTwitchAccount().getDisplayName() + " for the command "+command);

        if(interval < 1)
            throw new TooLowIntervalException("The Interval "+interval+" is too low.");

        TimerEntity timer = repository.getTimerForUserAndCommand(user,command);
        if(timer != null){
            throw new TimerAlreadyExistsException("A Timer for the command "+command+" already exists.");
        }

        return timer;
    }

}
