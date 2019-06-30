package at.saith.twasi.database;

import jdk.nashorn.internal.ir.annotations.Reference;
import net.twasi.core.database.models.BaseEntity;
import net.twasi.core.database.models.User;
import org.mongodb.morphia.annotations.Entity;

@Entity(value = "timers", noClassnameStored = true)
public class TimerEntity extends BaseEntity {
    @Reference
    private User user;

    private String name;
    private String msg;
    private int interval;//in Seconds
    private boolean enabled;

    public TimerEntity(){}

    public TimerEntity(User user, String name,String msg, int interval, boolean enabled) {
        this.user = user;
        this.name = name;
        this.msg = msg;
        this.interval = interval;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public User getUser() {
        return user;
    }

    public String getMsg() {
        return msg;
    }

    public int getInterval() {
        return interval;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
