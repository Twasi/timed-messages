package at.saith.twasi;

import at.saith.twasi.cmd.TimerCommand;
import net.twasi.core.events.TwasiEventHandler;
import net.twasi.core.plugin.api.TwasiUserPlugin;
import net.twasi.core.plugin.api.events.TwasiDisableEvent;
import net.twasi.core.plugin.api.events.TwasiEnableEvent;
import net.twasi.core.plugin.api.events.TwasiInstallEvent;
import net.twasi.core.services.ServiceRegistry;
import net.twasiplugin.dependency.streamtracker.StreamTrackerService;
import net.twasiplugin.dependency.streamtracker.events.StreamStopEvent;
import net.twasiplugin.dependency.streamtracker.events.StreamTrackEvent;


public class TimedMessagesUserPlugin extends TwasiUserPlugin {
    public TimedMessagesUserPlugin() {
        registerCommand(TimerCommand.class);
    }

    @Override
    public void onInstall(TwasiInstallEvent e) {
        e.getAdminGroup().addKey("twasi.timer.*");
        e.getModeratorsGroup().addKey("twasi.timer.list");
        e.getModeratorsGroup().addKey("twasi.timer.enable");
        e.getModeratorsGroup().addKey("twasi.timer.disable");
    }

    @Override
    public void onUninstall(TwasiInstallEvent e) {
        e.getAdminGroup().removeKey("twasi.timer.*");
        e.getModeratorsGroup().removeKey("twasi.timer.list");
        e.getModeratorsGroup().removeKey("twasi.timer.enable");
        e.getModeratorsGroup().removeKey("twasi.timer.disable");
    }

    @Override
    public void onEnable(TwasiEnableEvent e) {
        StreamTrackerService sts = ServiceRegistry.get(StreamTrackerService.class);
        sts.registerStreamTrackEvent(getTwasiInterface().getStreamer().getUser(), new StreamTrackerService.TwasiStreamTrackEventHandler(){

            @Override
            public void on(StreamTrackEvent streamTrackEvent) {
                if(!TimedMessagesPlugin.SERVICE.hasTimersEnabled(streamTrackEvent.getUser())){
                    TimedMessagesPlugin.SERVICE.startTimers(TimedMessagesUserPlugin.this);
                }
            }
        });
        sts.registerStreamStopEvent(getTwasiInterface().getStreamer().getUser(), new TwasiEventHandler<StreamStopEvent>() {
            @Override
            public void on(StreamStopEvent streamStopEvent) {
                TimedMessagesPlugin.SERVICE.stopTimers(TimedMessagesUserPlugin.this);
            }
        });
    }

    @Override
    public void onDisable(TwasiDisableEvent e) {
        TimedMessagesPlugin.SERVICE.stopTimers(this);
    }
}
