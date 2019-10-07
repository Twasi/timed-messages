package at.saith.twasi.cmd;

import at.saith.twasi.service.TimerService;
import at.saith.twasi.service.exception.CommandNotFoundException;
import at.saith.twasi.service.exception.NotAllowedTimerException;
import at.saith.twasi.service.exception.TimerAlreadyExistsException;
import at.saith.twasi.service.exception.TooLowIntervalException;
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
        if (event.hasArgs() && args.size() >= 2 && args.size() < 4) {
            String command = args.get(0);
            long interval;
            try {
                interval = Long.parseLong(args.get(1)) * 60;//Multiply by 60 to get Minutes
                if (interval > 3600) {
                    event.reply(renderer.render("timer.error.interval.toohigh"));
                    return false;
                }
            } catch (Exception e) {
                event.reply(renderer.render("timer.error.interval.notanumber"));
                return false;
            }
            boolean enabled = true;
            if (args.size() == 3 && !args.get(2).equals(renderer.render("timer.enabled"))) {
                if (args.get(2).equals(renderer.render("timer.disabled"))) {
                    enabled = false;
                } else {
                    return super.handle(event);
                }
            }
            renderer.bind("command", command);
            renderer.bind("interval", "" + interval);
            TimerService service = ServiceRegistry.get(TimerService.class);
            try {
                service.registerTimer(event.getTwasiInterface(), command, (int) interval, enabled);
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
            return super.handle(event);
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
