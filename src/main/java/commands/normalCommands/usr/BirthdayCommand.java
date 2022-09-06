package commands.normalCommands.usr;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import commands.core.Cubic;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import commands.normalCommands.bingo.BingoItem;
import commands.utils.Msg;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Deprecated
public class BirthdayCommand extends Command {
    private EventWaiter waiter;

    public BirthdayCommand(){
        this.name = "birthday";
        this.aliases = new String[]{"bd"};
        this.category = new Category("Profile");
        this.arguments = "";
        this.help = "Gives an option to get all the recorded birthdays of users in a server or a specific month. (Mod ONLY)";
        this.guildOnly = true;
        this.waiter = Cubic.getWaiter();
    }

    @Override
    protected void execute(CommandEvent event) {
        if(!event.getMember().hasPermission(Permission.BAN_MEMBERS)) return;

        event.getMessage().delete().queue();

        Message m = Msg.replyRet(event, "Looking up the birthdays..");
        new ButtonMenu.Builder()
                .setDescription("Pick one of the options:\n" +
                        ":one: All Birthdays\n" +
                        ":two: Birthday in a month")
                .setEventWaiter(waiter)
                .addChoices(BingoItem.getEmojis(2))
                .addChoice(EmojiManager.getForAlias("x").getUnicode())
                .setUsers(event.getAuthor())
                .setTimeout(20, TimeUnit.SECONDS)
                .setColor(event.getMember().getColor())
                .setAction(em -> {
                    String option = EmojiParser.parseToAliases(em.getEmoji());
                    switch (option.trim()){
                        case ":one:" : {
                            m.delete().queue();
                            Map<Integer, StringBuilder> map = new LinkedHashMap<>();
                            for(Qbee qbee : GameProfileCommand.qbees) {
                                if(qbee.getBirthday() != null) {
                                    if(map.containsKey(qbee.getBirthday().getMonth())) {
                                        Member member = event.getGuild().getMemberById(qbee.getUserID());
                                        if(member != null) map.get(qbee.getBirthday().getMonth())
                                                .append(Birthday.numToMonth(qbee.getBirthday().getMonth()))
                                                .append(" ")
                                                .append(String.format("%02d", qbee.getBirthday().getDay())).append(" - ").append(member.getAsMention()).append("\n");
                                    }
                                    else {
                                        Member member = event.getGuild().getMemberById(qbee.getUserID());
                                        if(member != null) {
                                            StringBuilder sb = new StringBuilder();
                                            sb.append(Birthday.numToMonth(qbee.getBirthday().getMonth()))
                                                    .append(" ")
                                                    .append(String.format("%02d", qbee.getBirthday().getDay())).append(" - ").append(member.getAsMention()).append("\n");
                                            map.put(qbee.getBirthday().getMonth(), sb);
                                        }
                                    }
                                }
                            }
                            StringBuilder sb = new StringBuilder();
                            for(int i = 1; i <= 12; i++){
                                if(i % 2 == 0){
                                    event.getTextChannel().sendMessage(sb.toString()).submitAfter(i, TimeUnit.SECONDS);
                                    sb = new StringBuilder();
                                }
                                if(!map.containsKey(i)){
                                    sb.append("**__").append(Birthday.numToMonth(i)).append(" ").append("Birthdays").append("__**").append("\n");
                                    sb.append("\n** **\n");
                                    continue;
                                }
                                sb.append("**__").append(Birthday.numToMonth(i)).append(" ").append("Birthdays").append("__**").append("\n");
                                sb.append(map.get(i).toString().trim()).append("\n** **\n");
                            }
                            event.getTextChannel().sendMessage(sb.toString()).queue();
                            break;
                        }
                        case ":two:" : {
                            m.delete().queue();
                            Message msg = Msg.replyRet(event, "Please enter month (full name like `January`) that you want to get the birthdays of.");
                            waiter.waitForEvent(GuildMessageReceivedEvent.class,
                                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                    e -> {
                                        String month = e.getMessage().getContentRaw().trim();
                                        e.getMessage().delete().queue();
                                        msg.delete().queue();
                                        if(Birthday.monthToNum(month) == -1) {
                                            Msg.bad(event, "Invalid month name. Please type only from `January` to `December`.");
                                            return;
                                        }
                                        Map<Integer, StringBuilder> map = new LinkedHashMap<>();
                                        for(Qbee qbee : GameProfileCommand.qbees) {
                                            if(qbee.getBirthday() != null) {
                                                if(map.containsKey(qbee.getBirthday().getMonth())) {
                                                    Member member;
                                                    try {
                                                        member = event.getGuild().retrieveMemberById(qbee.getUserID()).complete();
                                                    }catch(Exception ex){
                                                        member = null;
                                                    }
                                                    if(member != null) map.get(qbee.getBirthday().getMonth())
                                                            .append(Birthday.numToMonth(qbee.getBirthday().getMonth()))
                                                            .append(" ")
                                                            .append(String.format("%02d", qbee.getBirthday().getDay())).append(" - ").append(member.getAsMention()).append("\n");
                                                }
                                                else {
                                                    Member member;
                                                    try {
                                                        member = event.getGuild().retrieveMemberById(qbee.getUserID()).complete();
                                                    }catch(Exception ex){
                                                        member = null;
                                                    }
                                                    if(member != null) {
                                                        StringBuilder sb = new StringBuilder();
                                                        sb.append(Birthday.numToMonth(qbee.getBirthday().getMonth()))
                                                                .append(" ")
                                                                .append(String.format("%02d", qbee.getBirthday().getDay())).append(" - ").append(member.getAsMention()).append("\n");
                                                        map.put(qbee.getBirthday().getMonth(), sb);
                                                    }
                                                }
                                            }
                                        }
                                        if(!map.containsKey(Birthday.monthToNum(month))) {
                                            Msg.bad(event, "The provided month doesn't have any record of birthdays.");
                                            return;
                                        }

                                        StringBuilder sb = new StringBuilder();
                                        if(!map.containsKey(Birthday.monthToNum(month))){
                                            sb.append("**__").append(Birthday.numToMonth(Birthday.monthToNum(month))).append(" ").append("Birthdays").append("__**").append("\n");
                                            sb.append("\n** **\n");
                                        }
                                        else{
                                            sb.append("**__").append(Birthday.numToMonth(Birthday.monthToNum(month))).append(" ").append("Birthdays").append("__**").append("\n");
                                            sb.append(map.get(Birthday.monthToNum(month)).toString().trim()).append("\n** **\n");
                                        }
                                        event.getTextChannel().sendMessage(sb.toString()).queue();

                                    },
                                    30, TimeUnit.SECONDS, () -> Msg.replyTimed(event, "You took too long to respond, " + event.getAuthor().getAsMention() + "!", 10, TimeUnit.SECONDS));
                            break;
                        }
                        default : {
                            m.delete().queue();
                            Msg.reply(event.getTextChannel(), event.getAuthor().getAsMention()+" Cancelled looking up the birthdays.");
                            break;
                        }
                    }
                })
                .setFinalAction(me -> {
                    try {
                    }catch (Exception ignored) { }
                }).build().display(m);
//        event.getMessage().delete().queue();
//        Message msg = Msg.replyRet(event, "Please enter month (full name like `January`) that you want to get the birthdays of.");
//        waiter.waitForEvent(GuildMessageReceivedEvent.class,
//                e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
//                e -> {
//                    String month = e.getMessage().getContentRaw().trim();
//                    e.getMessage().delete().queue();
//                    msg.delete().queue();
//                    if(Birthday.monthToNum(month) == -1) {
//                        Msg.bad(event, "Invalid month name. Please type only from `January` to `December`.");
//                        return;
//                    }
//                    Map<Integer, StringBuilder> map = new LinkedHashMap<>();
//                    for(Qbee qbee : ProfileCommand.qbees) {
//                        if(qbee.getBirthday() != null) {
//                            if(map.containsKey(qbee.getBirthday().getMonth())) {
//                                Member member = event.getGuild().getMemberById(qbee.getUserID());
//                                if(member != null) map.get(qbee.getBirthday().getMonth())
//                                        .append(Birthday.numToMonth(qbee.getBirthday().getMonth()))
//                                        .append(" ")
//                                        .append(String.format("%02d", qbee.getBirthday().getDay())).append(" - ").append(member.getAsMention()).append("\n");
//                            }
//                            else {
//                                Member member = event.getGuild().getMemberById(qbee.getUserID());
//                                if(member != null) {
//                                    StringBuilder sb = new StringBuilder();
//                                    sb.append(Birthday.numToMonth(qbee.getBirthday().getMonth()))
//                                            .append(" ")
//                                            .append(String.format("%02d", qbee.getBirthday().getDay())).append(" - ").append(member.getAsMention()).append("\n");
//                                    map.put(qbee.getBirthday().getMonth(), sb);
//                                }
//                            }
//                        }
//                    }
//                    if(!map.containsKey(Birthday.monthToNum(month))) {
//                        Msg.bad(event, "The provided month doesn't have any record of birthdays.");
//                        return;
//                    }
//
//                    EmbedBuilder embed = new EmbedBuilder();
//                    embed.setTitle(Birthday.numToMonth(Birthday.monthToNum(month)) + " " + "Birthdays");
//                    embed.setDescription(map.get(Birthday.monthToNum(month)).toString().trim());
//                    embed.setColor(event.getMember().getColor());
//                    event.getTextChannel().sendMessage(embed.build()).queue();
//                },
//                30, TimeUnit.SECONDS, () -> Msg.replyTimed(event, "You took too long to respond, " + event.getAuthor().getAsMention() + "!", 10, TimeUnit.SECONDS));

    }


}
