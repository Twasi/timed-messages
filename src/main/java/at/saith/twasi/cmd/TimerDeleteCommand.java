package at.saith.twasi.cmd;

import at.saith.twasi.service.TimerService;
import at.saith.twasi.service.exception.*;
import net.twasi.core.plugin.api.customcommands.TwasiCustomCommandEvent;
import net.twasi.core.plugin.api.customcommands.structuredcommands.TwasiStructuredCommandEvent;
import net.twasi.core.plugin.api.customcommands.structuredcommands.subcommands.ISubCommands;
import net.twasi.core.plugin.api.customcommands.structuredcommands.subcommands.TwasiSubCommand;
import net.twasi.core.services.ServiceRegistry;
import net.twasi.core.translations.renderer.TranslationRenderer;

import java.util.List;

public class TimerDeleteCommand extends TwasiSubCommand {

    public TimerDeleteCommand(TwasiCustomCommandEvent event, ISubCommands parent) {
        super(event, parent);
    }

    @Override
    protected boolean handle(TwasiStructuredCommandEvent event) {

        TranslationRenderer renderer = event.getRenderer();
        List<String> args = event.getArgs();
        if (event.hasArgs() && args.size() == 1) {
            String command = args.get(0);
            renderer.bind("command", command);
            TimerService service = ServiceRegistry.get(TimerService.class);
            try {
                service.removeTimer(event.getTwasiInterface(), command);
                event.reply(renderer.render("timer.delete.success"));
                return true;
            } catch (TimerNotFoundException e) {
                event.reply(renderer.render("timer.error.notfound"));
            } catch (Exception e) {
                //Shouldn't be executed
                event.reply(renderer.render("timer.error.unexpected"));
                e.printStackTrace();
            }

            return false;
        } else {
            super.handle(event);
            return false;
        }
    }

    @Override
    public String requirePermissionKey() {
        return "timer.delete";
    }

    @Override
    public String getCommandName() {
        return "delete";
    }
}
