package at.saith.twasi;

import at.saith.twasi.service.TimerService;
import net.twasi.core.plugin.TwasiPlugin;
import net.twasi.core.plugin.api.TwasiUserPlugin;
import net.twasi.core.services.ServiceRegistry;
import net.twasi.core.services.providers.config.ConfigService;


public class TimedMessagesPlugin extends TwasiPlugin {

    public static final String COMMAND_PREFIX = ServiceRegistry.get(ConfigService.class).getCatalog().bot.prefix;
    public static TimerService SERVICE;

    @Override
    public void onActivate() {
        ServiceRegistry.register(new TimerService());
        SERVICE = ServiceRegistry.get(TimerService.class);
    }


    @Override
    public Class<? extends TwasiUserPlugin> getUserPluginClass() {
        return TimedMessagesUserPlugin.class;
    }
}
