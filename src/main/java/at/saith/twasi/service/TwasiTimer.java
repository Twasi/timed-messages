package at.saith.twasi.service;

import at.saith.twasi.TimedMessagesPlugin;
import net.twasi.core.database.models.TwitchAccount;
import net.twasi.core.database.models.permissions.PermissionGroups;
import net.twasi.core.interfaces.api.TwasiInterface;
import net.twasi.core.logger.TwasiLogger;
import net.twasi.core.models.Message.MessageType;
import net.twasi.core.models.Message.TwasiMessage;

import java.util.*;

public class TwasiTimer extends Timer {

    private TwasiInterface twasiInterface;
    private String command;
    private int interval;
    private boolean enabled;


    private TimerTask task;
    private long nextExecution;

    private static HashSet<TwasiTimer> timers = new HashSet<>();

    public TwasiTimer(TwasiInterface twasiInterface, String command, int interval, boolean enabled) {
        super(true);
        this.twasiInterface = twasiInterface;
        this.command = command;
        this.enabled = enabled;
        this.interval = interval;
        if (timers.contains(this)) {
            this.task = new TimerTask() {
                @Override
                public void run() {
                    cancel();
                }
            };
        } else {
            timers.add(this);

            this.task = new TwasiTimerTask();
        }
    }

    public void start() {
        start(0);
    }

    public void start(int initialDelayInSeconds) {
        if (enabled) {
            int delay = (interval + initialDelayInSeconds);

            nextExecution = System.currentTimeMillis() + delay * 1000;
            TwasiLogger.log.debug(this + " starts in " + delay + "s.");
            try {

                scheduleAtFixedRate(task, delay * 1000, interval * 1000);
            } catch (IllegalStateException e) {
                TwasiLogger.log.error("Task for Timer " + this + " might be already scheduled or canceled.");
            }
        }
    }

    public void disable() {
        this.enabled = false;
        task.cancel();
    }

    private void dispatchCommand() {
        long current = System.currentTimeMillis();
        TwasiLogger.log.debug("Executing Timer " + this);
        if (nextExecution - current <= 0) {
            TwasiLogger.log.debug("Expected Execution Time is " + -(nextExecution - current) + "ms ahead of Real Execution Time");
        } else {
            TwasiLogger.log.debug("Expected Execution Time is " + (nextExecution - current) + "ms behind Real Execution Time");
        }
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

    public int hashCode() {
        return Objects.hash(command, twasiInterface.getStreamer().getUser().getTwitchAccount().getDisplayName());
    }

    public boolean equals(Object object) {
        if (object == null) return false;
        if (this == object) return true;
        if (object instanceof TwasiTimer) {
            if (object.hashCode() == hashCode()) return true;
            return command.equals(((TwasiTimer) object).command) && twasiInterface.equals(((TwasiTimer) object).twasiInterface);
        }
        return false;
    }

    @Override
    public String toString() {
        return "{Command=" + command + ", User=" + twasiInterface.getStreamer().getUser().getTwitchAccount().getDisplayName() + ", Interval=" + interval + ", Enabled=" + enabled + "}";
    }

    public String getCommand() {
        return command;
    }


    private class TwasiTimerTask extends TimerTask {
        @Override
        public boolean cancel() {
            TwasiLogger.log.debug("Cancelling Timer " + TwasiTimer.this);
            timers.remove(TwasiTimer.this);
            return super.cancel();
        }

        @Override
        public void run() {

            if (!enabled) cancel();
            if (!TimedMessagesPlugin.SERVICE.commandExists(twasiInterface, command)) {
                disable();
            }
            dispatchCommand();
            nextExecution = System.currentTimeMillis() + interval * 1000;
        }
    }
}
