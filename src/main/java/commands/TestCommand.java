package commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Arrays;
import java.util.Objects;

public class TestCommand extends SlashCommand {
    public TestCommand(){
        this.name = "test";
        this.guildOnly = true;
        this.arguments = "<text>";
        this.ownerCommand = true;

        OptionData args = new OptionData(OptionType.STRING, "text", "The text you want to show.", false);
//        args.addChoice("Choice 1", "Choice 1");
//        args.addChoice("Choice 2", "Choice 2");

        this.options = Arrays.asList(
                args
//                new OptionData(OptionType.USER, "user", "Mention user to tag.", false)
        );
    }
//
//    @Override
//    protected void execute(CommandEvent event) {
//
//    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String text = Objects.requireNonNull(event.getOption("text")).getAsString();

        event.getInteraction().reply(text).setEphemeral(true).queue();
    }
}
