package at.saith.twasi.cmd;

import net.twasi.core.plugin.api.TwasiUserPlugin;
import net.twasi.core.plugin.api.customcommands.structuredcommands.StructuredPluginCommand;
import net.twasi.core.plugin.api.customcommands.structuredcommands.subcommands.SubCommandCollection;

public class TimerCommand extends StructuredPluginCommand {

    public TimerCommand(TwasiUserPlugin twasiUserPlugin) {
        super(twasiUserPlugin);
    }

    @Override
    public SubCommandCollection getSubCommands() {
        return SubCommandCollection.OFCLASSES(TimerAddCommand.class, TimerDeleteCommand.class, TimerListCommand.class);
    }

    @Override
    public String getCommandName() {
        return "timer";
    }
}
