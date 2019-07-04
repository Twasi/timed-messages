package at.saith.twasi.cmd;

import net.twasi.core.plugin.api.TwasiUserPlugin;
import net.twasi.core.plugin.api.customcommands.TwasiCustomCommandEvent;
import net.twasi.core.plugin.api.customcommands.structuredcommands.StructuredPluginCommand;
import net.twasi.core.plugin.api.customcommands.structuredcommands.subcommands.SubCommandCollection;
import net.twasi.core.translations.renderer.TranslationRenderer;

public class TimerCommand extends StructuredPluginCommand {

    public TimerCommand(TwasiUserPlugin twasiUserPlugin) {
        super(twasiUserPlugin);
    }

    @Override
    public SubCommandCollection getSubCommands() {
        return SubCommandCollection.
                OFCLASSES(
                        TimerAddCommand.class,
                        TimerDeleteCommand.class,
                        TimerListCommand.class,
                        TimerEnableCommand.class,
                        TimerDisableCommand.class
                );
    }

    @Override
    protected boolean handle(TwasiCustomCommandEvent event) {
        TranslationRenderer renderer = event.getRenderer();
        event.reply(renderer.render("timer.syntax"));
        return true;
    }

    @Override
    public String getCommandName() {
        return "timer";
    }
}
