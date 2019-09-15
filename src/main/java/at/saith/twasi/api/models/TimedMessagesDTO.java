package at.saith.twasi.api.models;

import at.saith.twasi.TimedMessagesPlugin;
import at.saith.twasi.database.TimerEntity;
import at.saith.twasi.service.exception.TimerException;
import net.twasi.core.database.models.User;
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

    public TimerEntity registerTimer(String command, int interval) throws TimerException {
        return TimedMessagesPlugin.SERVICE.registerTimer(twasiInterface, command, interval);
    }

    public TimerEntity removeTimer(String command) throws TimerException {
        return TimedMessagesPlugin.SERVICE.removeTimer(twasiInterface, command);
    }

    public TimerEntity enableTimer(String command, boolean enabled) throws TimerException {
        return TimedMessagesPlugin.SERVICE.enableTimer(twasiInterface, command, enabled);
    }
}
