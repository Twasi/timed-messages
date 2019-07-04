package at.saith.twasi.cmd;

import at.saith.twasi.service.TimerService;
import at.saith.twasi.service.exception.TimerException;
import at.saith.twasi.service.exception.TimerNotFoundException;
import net.twasi.core.plugin.api.customcommands.TwasiCustomCommandEvent;
import net.twasi.core.plugin.api.customcommands.structuredcommands.TwasiStructuredCommandEvent;
import net.twasi.core.plugin.api.customcommands.structuredcommands.subcommands.ISubCommands;
import net.twasi.core.plugin.api.customcommands.structuredcommands.subcommands.TwasiSubCommand;
import net.twasi.core.services.ServiceRegistry;
import net.twasi.core.translations.renderer.TranslationRenderer;

public class TimerEnableCommand extends TwasiSubCommand {
    public TimerEnableCommand(TwasiCustomCommandEvent event, ISubCommands parent) {
        super(event, parent);
    }

    @Override
    protected boolean handle(TwasiStructuredCommandEvent event) {
        return handleEnableCommand(event);
    }

    public static boolean handleEnableCommand(TwasiStructuredCommandEvent event) {
        TranslationRenderer renderer = event.getRenderer();
        TimerService service = ServiceRegistry.get(TimerService.class);
        if (event.hasArgs() && event.getArgs().size() == 1) {
            String command = event.getArgs().get(0);
            boolean enabled = event.getBaseCommand().equalsIgnoreCase("enable");
            try {
                String enabledString = enabled ? renderer.render("timer.enabled") : renderer.render("timer.disabled");
                service.enableTimer(event.getTwasiInterface(), command, enabled);
                renderer.bind("command",command);
                renderer.bind("enabled", enabledString);
                event.reply(renderer.render("timer.enable.success"));
                return true;
            } catch (TimerNotFoundException e) {
                event.reply(renderer.render("timer.error.notfound"));
            } catch (Exception e) {
                event.reply(renderer.render("timer.error.unexpected"));
                e.printStackTrace();
            }
            return false;
        } else {
            event.reply(renderer.render("timer.enable.syntax"));
        }
        return false;
    }

    @Override
    public String requirePermissionKey() {
        return "timer.enable";
    }

    @Override
    public String getCommandName() {
        return "enable";
    }

}
