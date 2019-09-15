package at.saith.twasi.api;

import at.saith.twasi.api.models.TimedMessagesDTO;
import net.twasi.core.database.models.User;
import net.twasi.core.graphql.TwasiCustomResolver;


public class TimedMessagesResolver extends TwasiCustomResolver {

    public TimedMessagesDTO getTimedmessages(String token) {
        User user = getUser(token);
        if (user == null)
            return null;
        if (!user.getInstalledPlugins().contains("TimedMessages"))
            return null;
        return new TimedMessagesDTO(user);
    }
}
