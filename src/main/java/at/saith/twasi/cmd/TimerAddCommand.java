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

public class TimerAddCommand extends TwasiSubCommand {

    public TimerAddCommand(TwasiCustomCommandEvent event, ISubCommands parent) {
        super(event, parent);
    }

    @Override
    protected boolean handle(TwasiStructuredCommandEvent event) {
        TranslationRenderer renderer = event.getRenderer();
        List<String> args = event.getArgs();
        if (event.hasArgs() && args.size() == 2) {
            String command = args.get(0);
            int interval;
            try {
                interval = Integer.parseInt(args.get(1)) * 60;//Multiply by 60 to get Minutes
            } catch (Exception e) {
                event.reply(renderer.render("timer.error.interval.notanumber"));
                return false;
            }
            renderer.bind("command", command);
            renderer.bind("interval", "" + interval);
            TimerService service = ServiceRegistry.get(TimerService.class);
            try {
                service.registerTimer(event.getTwasiInterface(), command, interval);
                event.reply(renderer.render("timer.add.success"));
                return true;
            } catch (TooLowIntervalException e) {
                event.reply(renderer.render("timer.error.interval.toolow"));
            } catch (TimerAlreadyExistsException e) {
                event.reply(renderer.render("timer.error.alreadyexists"));
            } catch (CommandNotFoundException e) {
                event.reply(renderer.render("timer.error.command.notfound"));
            } catch (NotAllowedTimerException e) {
                event.reply(renderer.render("timer.error.command.notallowed"));
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
        return "timer.add";
    }

    @Override
    public String getCommandName() {
        return "add";
    }
}
