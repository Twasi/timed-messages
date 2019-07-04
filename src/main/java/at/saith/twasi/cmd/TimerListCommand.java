package at.saith.twasi.cmd;

import at.saith.twasi.database.TimerEntity;
import at.saith.twasi.service.TimerService;
import net.twasi.core.plugin.api.customcommands.TwasiCustomCommandEvent;
import net.twasi.core.plugin.api.customcommands.structuredcommands.TwasiStructuredCommandEvent;
import net.twasi.core.plugin.api.customcommands.structuredcommands.subcommands.ISubCommands;
import net.twasi.core.plugin.api.customcommands.structuredcommands.subcommands.TwasiSubCommand;
import net.twasi.core.services.ServiceRegistry;
import net.twasi.core.translations.renderer.TranslationRenderer;

import java.util.List;

public class TimerListCommand extends TwasiSubCommand {
    public TimerListCommand(TwasiCustomCommandEvent event, ISubCommands parent) {
        super(event, parent);
    }

    @Override
    protected boolean handle(TwasiStructuredCommandEvent event) {
        TranslationRenderer renderer = event.getRenderer();
        if (!event.hasArgs()) {
            TimerService service = ServiceRegistry.get(TimerService.class);
            List<TimerEntity> timers = service.getTimersForUser(event.getTwasiInterface().getStreamer().getUser());
            System.out.println(timers.size());
            StringBuilder timersBuilder = new StringBuilder();
            for (TimerEntity timer : timers) {
                String enabled = timer.isEnabled() ? renderer.render("timer.enabled") : renderer.render("timer.disabled");
                timersBuilder.append(timer.getCommand() + "(" + timer.getInterval() + ", " + enabled + "), ");
            }
            if (timersBuilder.length() < 2) {
                event.reply(renderer.render("timer.list.empty"));
            } else {
                renderer.bind("timers", timersBuilder.substring(0, timersBuilder.length() - 2));
                event.reply(renderer.render("timer.list.success"));
            }
            return true;
        } else {
            event.reply(renderer.render("timer.list.syntax"));
        }
        return false;
    }

    @Override
    public String requirePermissionKey() {
        return "timer.list";
    }

    @Override
    public String getCommandName() {
        return "list";
    }
}
