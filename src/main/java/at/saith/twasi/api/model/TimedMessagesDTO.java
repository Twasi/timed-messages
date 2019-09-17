package at.saith.twasi.api.model;

import at.saith.twasi.TimedMessagesPlugin;
import at.saith.twasi.database.TimerEntity;
import at.saith.twasi.service.exception.*;
import net.twasi.core.database.models.User;
import net.twasi.core.graphql.TwasiGraphQLHandledException;
import net.twasi.core.interfaces.api.TwasiInterface;
import net.twasi.core.services.ServiceRegistry;
import net.twasi.core.services.providers.InstanceManagerService;

import java.util.List;

public class TimedMessagesDTO {
    private TwasiInterface twasiInterface;

    public TimedMessagesDTO(User user) {
        this.twasiInterface = ServiceRegistry.get(InstanceManagerService.class).getByUser(user);
    }

    public List<TimerEntity> listTimers() {
        return TimedMessagesPlugin.SERVICE.getTimersForUser(twasiInterface.getStreamer().getUser());
    }

    public TimerEntity registerTimer(String command, int interval, boolean enabled) {
        try {
            return TimedMessagesPlugin.SERVICE.registerTimer(twasiInterface, command, interval, enabled);
        } catch (Exception e) {
            throwGraphQLExcetion(e);
        }
        return null;
    }

    public TimerEntity removeTimer(String command) {
        try {
            return TimedMessagesPlugin.SERVICE.removeTimer(twasiInterface, command);
        } catch (Exception e) {
            throwGraphQLExcetion(e);
        }
        return null;
    }

    public TimerEntity enableTimer(String command, boolean enabled) {
        try {
            return TimedMessagesPlugin.SERVICE.enableTimer(twasiInterface, command, enabled);
        } catch (Exception e) {
            throwGraphQLExcetion(e);
        }
        return null;
    }

    public TimerEntity updateTimer(String command, String newCommand, int newInterval, boolean enabled) {
        try {
            return TimedMessagesPlugin.SERVICE.updateTimer(twasiInterface, command, newCommand, newInterval, enabled);
        } catch (Exception e) {
            throwGraphQLExcetion(e);
        }
        return null;
    }

    private static void throwGraphQLExcetion(Exception e) {
        if (e instanceof TooLowIntervalException) {
            throw new TwasiGraphQLHandledException(e.getMessage(), "timer.error.interval.toolow");
        } else if (e instanceof TimerAlreadyExistsException) {
            throw new TwasiGraphQLHandledException(e.getMessage(), "timer.error.alreadyexists");
        } else if (e instanceof CommandNotFoundException) {
            throw new TwasiGraphQLHandledException(e.getMessage(), "timer.error.command.notfound");
        } else if (e instanceof NotAllowedTimerException) {
            throw new TwasiGraphQLHandledException(e.getMessage(), "timer.error.command.notallowed");
        } else {
            //Shouldn't be executed
            e.printStackTrace();
            throw new TwasiGraphQLHandledException("", "timer.error.unexpected");
        }
    }
}
