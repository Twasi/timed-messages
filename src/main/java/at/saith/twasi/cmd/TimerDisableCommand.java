package at.saith.twasi.cmd;

import net.twasi.core.plugin.api.customcommands.TwasiCustomCommandEvent;
import net.twasi.core.plugin.api.customcommands.structuredcommands.subcommands.ISubCommands;
import net.twasi.core.plugin.api.customcommands.structuredcommands.subcommands.TwasiSubCommand;

public class TimerDisableCommand extends TwasiSubCommand {
    public TimerDisableCommand(TwasiCustomCommandEvent event, ISubCommands parent) {
        super(event, parent);
    }

    @Override
    public String requirePermissionKey() {
        return "timer.disable";
    }
    @Override
    public String getCommandName() {
        return "disable";
    }
}
