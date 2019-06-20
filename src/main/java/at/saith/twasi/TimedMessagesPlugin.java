package at.saith.twasi;

import at.saith.twasi.service.TimerService;
import net.twasi.core.plugin.TwasiPlugin;
import net.twasi.core.plugin.api.TwasiUserPlugin;
import net.twasi.core.services.ServiceRegistry;



public class TimedMessagesPlugin extends TwasiPlugin {
    private static TimerService service;
    @Override
    public void onActivate() {
        service = new TimerService();
        ServiceRegistry.register(service);
    }

    @Override
    public Class<? extends TwasiUserPlugin> getUserPluginClass() {
        return TimedMessagesUserPlugin.class;
    }
    public static TimerService getService(){
        return service;
    }
}
