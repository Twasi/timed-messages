package at.saith.twasi.cmd;

import net.twasi.core.plugin.api.TwasiUserPlugin;
import net.twasi.core.plugin.api.customcommands.TwasiPluginCommand;

public class TimerCommand extends TwasiPluginCommand {

    public TimerCommand(TwasiUserPlugin twasiUserPlugin) {
        super(twasiUserPlugin);
    }

    @Override
    public String getCommandName() {
        return "timer";
    }
}
