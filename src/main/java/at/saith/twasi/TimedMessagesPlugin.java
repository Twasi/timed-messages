package at.saith.twasi;

import at.saith.twasi.service.TimerService;
import net.twasi.core.plugin.TwasiPlugin;
import net.twasi.core.plugin.api.TwasiUserPlugin;
import net.twasi.core.services.ServiceRegistry;


public class TimedMessagesPlugin extends TwasiPlugin {
    @Override
    public void onActivate() {
        ServiceRegistry.register(new TimerService());
    }

    @Override
    public Class<? extends TwasiUserPlugin> getUserPluginClass() {
        return TimedMessagesUserPlugin.class;
    }
}
