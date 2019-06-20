package at.saith.twasi;

import at.saith.twasi.cmd.TimerCommand;
import net.twasi.core.plugin.api.TwasiUserPlugin;
import net.twasi.core.plugin.api.events.TwasiInstallEvent;

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
}
