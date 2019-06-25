package at.saith.twasi.cmd;

import net.twasi.core.plugin.api.customcommands.TwasiCustomCommandEvent;
import net.twasi.core.plugin.api.customcommands.structuredcommands.subcommands.ISubCommands;
import net.twasi.core.plugin.api.customcommands.structuredcommands.subcommands.TwasiSubCommand;

public class TimerDeleteCommand extends TwasiSubCommand {
    public TimerDeleteCommand(TwasiCustomCommandEvent event, ISubCommands parent) {
        super(event, parent);
    }

    @Override
    public String getCommandName() {
        return "delete";
    }
}
