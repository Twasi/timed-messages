package at.saith.twasi.database;

import net.twasi.core.database.lib.Repository;
import net.twasi.core.database.models.User;

import java.util.List;
import java.util.regex.Pattern;

public class TimerRepository extends Repository<TimerEntity> {

    public TimerEntity getTimerForUserAndCommand(User user, String command){
        Pattern pattern = Pattern.compile("^"+command+"$", Pattern.CASE_INSENSITIVE);
        return store.createQuery(TimerEntity.class).field("user").equal(user).field("command").equal(pattern).get();
    }
    public List<TimerEntity> getUserTimers(User user){
        return store.createQuery(TimerEntity.class).field("user").equal(user).asList();
    }
    public void deleteTimer(TimerEntity timerEntity){
        store.delete(timerEntity);
    }
}
