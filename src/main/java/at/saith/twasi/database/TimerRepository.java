package at.saith.twasi.database;

import net.twasi.core.database.lib.Repository;
import net.twasi.core.database.models.User;

import java.util.List;

public class TimerRepository extends Repository<TimerEntity> {

    public TimerEntity getTimerForUserAndCommand(User user, String command) {
        return store.createQuery(TimerEntity.class).field("user").equal(user).field("command").equalIgnoreCase(command).get();
    }

    public List<TimerEntity> getUserTimers(User user) {
        return store.createQuery(TimerEntity.class).field("user").equal(user).asList();
    }
}
