package at.saith.twasi.database;


import net.twasi.core.database.models.BaseEntity;
import net.twasi.core.database.models.User;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "timers", noClassnameStored = true)
public class TimerEntity extends BaseEntity {

    @Reference
    private User user;

    private String command;
    private int interval;//in Seconds
    private boolean enabled;

    public TimerEntity() {
    }

    public TimerEntity(User user, String command, int interval, boolean enabled) {
        this.user = user;
        this.command = command;
        this.interval = interval;
        this.enabled = enabled;
    }


    public User getUser() {
        return user;
    }

    public String getCommand() {
        return command;
    }

    public int getInterval() {
        return interval;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setInterval(int interval){
        this.interval = interval;
    }
}
