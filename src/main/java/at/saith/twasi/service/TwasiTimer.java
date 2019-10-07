package at.saith.twasi.service;

import at.saith.twasi.TimedMessagesPlugin;
import net.twasi.core.database.models.TwitchAccount;
import net.twasi.core.database.models.permissions.PermissionGroups;
import net.twasi.core.interfaces.api.TwasiInterface;
import net.twasi.core.logger.TwasiLogger;
import net.twasi.core.models.Message.MessageType;
import net.twasi.core.models.Message.TwasiMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class TwasiTimer extends Timer {

    private TwasiInterface twasiInterface;
    private String command;
    private int interval;
    private boolean enabled;

    public TwasiTimer(TwasiInterface twasiInterface, String command, int interval, boolean enabled, int initialDelayInSeconds) {
        super(true);
        this.twasiInterface = twasiInterface;
        this.command = command;
        this.enabled = enabled;
        this.interval = interval;
        if (enabled) {
            start(initialDelayInSeconds);
        }
    }

    public void start(int initialDelayInSeconds) {
        int delay = (interval + initialDelayInSeconds);
        TwasiLogger.log.info("Timer for the User " + twasiInterface.getStreamer().getUser().getTwitchAccount().getDisplayName() + " for the Command " + command + " in an interval of " + interval + "s and a delay of " + delay + "s started.");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (!enabled) cancel();
                if (!TimedMessagesPlugin.SERVICE.commandExists(twasiInterface, command)) {
                    disable();
                }
                dispatchCommand();
            }
        };

        scheduleAtFixedRate(task, delay * 1000, interval * 1000);
    }

    public void disable() {
        this.enabled = false;
        cancel();
    }


    private void dispatchCommand() {
        TwitchAccount twitchAccount = twasiInterface.getStreamer().getUser().getTwitchAccount();
        twasiInterface.getDispatcher().dispatch(new TwasiMessage(
                command,
                MessageType.PRIVMSG,
                new TwitchAccount(
                        twitchAccount.getUserName(),
                        twitchAccount.getDisplayName(),
                        twitchAccount.getToken(),
                        twitchAccount.getTwitchId(),
                        new ArrayList<>(Collections.singletonList(PermissionGroups.BROADCASTER))
                ),
                twasiInterface
        ));
    }

    public boolean equals(Object object) {
        if (object == null) return false;
        if (this == object) return true;
        if (object instanceof TwasiTimer) {
            return command.equals(((TwasiTimer) object).command);
        }
        return false;
    }

    public String getCommand() {
        return command;
    }
}
