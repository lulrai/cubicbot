package cubicCastles.user;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import normalCommands.bingo.BingoItem;
import utils.Msg;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class ProfileEditCommand extends Command {
    private EventWaiter waiter;
    public ProfileEditCommand(EventWaiter waiter) {
        this.name = "editprofile";
        this.aliases = new String[]{"ep"};
        this.category = new Category("Normal");
        this.ownerCommand = false;
        this.cooldown = 10;
        this.cooldownScope = CooldownScope.USER;
        this.waiter = waiter;
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getMessage().delete().queue();
        Message displayMessage = event.getTextChannel().sendMessage("Editing " + event.getAuthor().getAsMention() + "'s profile..").complete();
        ButtonMenu.Builder bm = new ButtonMenu.Builder()
                .setDescription("Pick one of the following options:\n" +
                        ":one: Add/Edit in-game name\n" +
                        ":two: Add/Edit about me\n" +
                        ":three: Add/Edit birthday\n" +
                        ":four: Add/Edit favorite item\n" +
                        ":five: Add/Edit level\n" +
                        ":six: Add/Edit clan\n" +
                        ":seven: Add/Edit game join date\n" +
                        ":eight: Add/Remove realm\n" +
                        ":nine: Add/Remove overworld\n" +
                        ":frame_photo: Add/Edit custom profile pic\n" +
                        ":link: Add/Edit forum profile link")
                .setEventWaiter(waiter)
                .addChoices(BingoItem.getEmojis(9))
                .addChoice(EmojiManager.getForAlias("picture_frame").getUnicode())
                .addChoice(EmojiManager.getForAlias("link").getUnicode())
                .addChoice(EmojiManager.getForAlias("x").getUnicode())
                .setUsers(event.getAuthor())
                .setTimeout(30, TimeUnit.SECONDS)
                .setColor(event.getMember().getColor())
                .setAction(v -> {
                    int index = -1;
                    for(int i = 0; i < ProfileCommand.qbees.size(); i++){
                        if(ProfileCommand.qbees.get(i).getUserID().equals(event.getAuthor().getId())){
                            index = i;
                            break;
                        }
                    }
                    if(index == -1){
                        Qbee qbee = new Qbee(event.getAuthor().getId(), event.getAuthor().getEffectiveAvatarUrl());
                        index = Collections.binarySearch(ProfileCommand.qbees, qbee, new Qbee.QbeeSortingComparator());
                        index = ~index;
                        ProfileCommand.qbees.add(index, qbee);
                    }

                    String choice = EmojiParser.parseToAliases(v.getEmoji());
                    switch(choice.trim()){
                        case ":picture_frame:" : {
                            displayMessage.delete().queue();
                            Message m = Msg.replyRet(event, "Please type the link for your profile picture that you want or upload an image file.");
                            waiter.waitForEvent(GuildMessageReceivedEvent.class,
                                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                    e -> {
                                        String profilePic = e.getMessage().getContentRaw().trim();
                                        m.delete().queue();
                                        if(!e.getMessage().getAttachments().isEmpty() && e.getMessage().getAttachments().get(0).isImage()){
                                            profilePic = e.getMessage().getAttachments().get(0).getUrl();
                                            //e.getMessage().delete().queue();
                                        }
                                        else {
                                            e.getMessage().delete().queue();
                                            try{
                                                new URL(profilePic);
                                            } catch (MalformedURLException ex) {
                                                Msg.bad(event, "Invalid URL or no image file attached. Profile editing cancelled.");
                                                return;
                                            }
                                        }
                                        Qbee qbee = ProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                        qbee.setProfilePic(profilePic);
                                        ProfileReadWrite.updateUser(qbee);
                                    },
                                    1, TimeUnit.MINUTES, () -> Msg.replyTimed(event, "You took too long to respond, " + event.getAuthor().getAsMention() + "! Profile editing has been cancelled.", 10, TimeUnit.SECONDS));
                            break;
                        }
                        case ":link:" : {
                            displayMessage.delete().queue();
                            Message m = Msg.replyRet(event, "Please type the link for forum profile if you have one.\n**NOTE** Link isn't visible but it makes the title of your profile clickable/tapable.");
                            waiter.waitForEvent(GuildMessageReceivedEvent.class,
                                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                    e -> {
                                        String profileLink = e.getMessage().getContentRaw().trim();
                                        m.delete().queue();
                                        e.getMessage().delete().queue();
                                        try{
                                            new URL(profileLink);
                                        } catch (MalformedURLException ex) {
                                            Msg.bad(event, "Invalid link provided. Please provide a valid link. Profile editing cancelled.");
                                            return;
                                        }
                                        Qbee qbee = ProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                        qbee.setForumLink(profileLink);
                                        ProfileReadWrite.updateUser(qbee);
                                    },
                                    1, TimeUnit.MINUTES, () -> Msg.replyTimed(event, "You took too long to respond, " + event.getAuthor().getAsMention() + "! Profile editing has been cancelled.", 10, TimeUnit.SECONDS));
                            break;
                        }
                        case ":one:" : {
                            displayMessage.delete().queue();
                            Message m = Msg.replyRet(event, "Please type what you want your in-game name to be.");
                            waiter.waitForEvent(GuildMessageReceivedEvent.class,
                                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                    e -> {
                                        String name = e.getMessage().getContentRaw().trim();
                                        e.getMessage().delete().queue();
                                        m.delete().queue();
                                        if(name.length() > 60) {
                                            Msg.bad(event, "Name too long, please make it shorter. Profile editing cancelled.");
                                            return;
                                        }
                                        Qbee qbee = ProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                        qbee.setGameName(name);
                                        ProfileReadWrite.updateUser(qbee);
                                    },
                                    30, TimeUnit.SECONDS, () -> Msg.replyTimed(event, "You took too long to respond, " + event.getAuthor().getAsMention() + "! Profile editing has been cancelled.", 10, TimeUnit.SECONDS));
                            break;
                        }
                        case ":two:" : {
                            displayMessage.delete().queue();
                            Message m = Msg.replyRet(event, "Please type what you want your \"About me\" information to be. (Less than 120 characters)");
                            waiter.waitForEvent(GuildMessageReceivedEvent.class,
                                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                    e -> {
                                        String about = e.getMessage().getContentRaw().trim();
                                        e.getMessage().delete().queue();
                                        m.delete().queue();
                                        if(about.length() > 120) {
                                            Msg.bad(event, "Too long of an about me/description. Profile editing cancelled.");
                                            return;
                                        }
                                        Qbee qbee = ProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                        qbee.setAbout(about);
                                        ProfileReadWrite.updateUser(qbee);
                                    },
                                    2, TimeUnit.MINUTES, () -> Msg.replyTimed(event, "You took too long to respond, " + event.getAuthor().getAsMention() + "! Profile editing has been cancelled.", 10, TimeUnit.SECONDS));
                            break;
                        }
                        case ":three:" : {
                            displayMessage.delete().queue();
                            Message m = Msg.replyRet(event, "Please type your birthday, just month and day. DO NOT TYPE YEAR. (Ex: 07/10)");
                            waiter.waitForEvent(GuildMessageReceivedEvent.class,
                                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                    e -> {
                                        String date = e.getMessage().getContentRaw().trim();
                                        e.getMessage().delete().queue();
                                        m.delete().queue();
                                        Birthday b = Birthday.parseBirthday(date);
                                        if(b == null) {
                                            Msg.bad(event, "Invalid format, use this format:" +
                                                    "MM/dd\n" +
                                                    "Ex: 07/10\n" +
                                                    "Profile editing cancelled.");
                                            return;
                                        }
                                        Qbee qbee = ProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                        qbee.setBirthday(b);
                                        ProfileReadWrite.updateUser(qbee);
                                    },
                                    30, TimeUnit.SECONDS, () -> Msg.replyTimed(event, "You took too long to respond, " + event.getAuthor().getAsMention() + "! Profile editing has been cancelled.", 10, TimeUnit.SECONDS));
                            break;
                        }
                        case ":four:" : {
                            displayMessage.delete().queue();
                            Message m = Msg.replyRet(event, "Please enter what your favorite item is.");
                            waiter.waitForEvent(GuildMessageReceivedEvent.class,
                                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                    e -> {
                                        String item = e.getMessage().getContentRaw().trim();
                                        e.getMessage().delete().queue();
                                        m.delete().queue();
                                        if(item.length() > 60) {
                                            Msg.bad(event, "Too long of an item name, please make it less than 60 characters. Profile editing cancelled.");
                                            return;
                                        }
                                        Qbee qbee = ProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                        qbee.setFavoriteItem(item);
                                        ProfileReadWrite.updateUser(qbee);
                                    },
                                    30, TimeUnit.SECONDS, () -> Msg.replyTimed(event, "You took too long to respond, " + event.getAuthor().getAsMention() + "! Profile editing has been cancelled.", 10, TimeUnit.SECONDS));
                            break;
                        }
                        case ":five:" : {
                            displayMessage.delete().queue();
                            Message m = Msg.replyRet(event, "Please enter your level, ONLY numbers.");
                            waiter.waitForEvent(GuildMessageReceivedEvent.class,
                                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                    e -> {
                                        int level = -1;
                                        m.delete().queue();
                                        try {
                                            level = Integer.parseInt(e.getMessage().getContentRaw().trim());
                                        }
                                        catch (NumberFormatException n) {
                                            Msg.bad(event, "Not a number, please only enter a number. Profile editing cancelled.");
                                            return;
                                        }
                                        e.getMessage().delete().queue();
                                        if(level > 60 || level < 0) {
                                            Msg.bad(event, "Invalid level number. Profile editing cancelled.");
                                            return;
                                        }
                                        Qbee qbee = ProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                        qbee.setLevel(level);
                                        ProfileReadWrite.updateUser(qbee);
                                    },
                                    30, TimeUnit.SECONDS, () -> Msg.replyTimed(event, "You took too long to respond, " + event.getAuthor().getAsMention() + "! Profile editing has been cancelled.", 10, TimeUnit.SECONDS));
                            break;
                        }
                        case ":six:" : {
                            displayMessage.delete().queue();
                            Message m = Msg.replyRet(event, "Please enter your clan name.");
                            waiter.waitForEvent(GuildMessageReceivedEvent.class,
                                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                    e -> {
                                        String clan = e.getMessage().getContentRaw().trim();
                                        e.getMessage().delete().queue();
                                        m.delete().queue();
                                        if(clan.length() > 60) {
                                            Msg.bad(event, "Too long of a clan name, please make it less than 60 characters. Profile editing cancelled.");
                                            return;
                                        }
                                        Qbee qbee = ProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                        qbee.setClan(clan);
                                        ProfileReadWrite.updateUser(qbee);
                                    },
                                    30, TimeUnit.SECONDS, () -> Msg.replyTimed(event, "You took too long to respond, " + event.getAuthor().getAsMention() + "! Profile editing has been cancelled.", 10, TimeUnit.SECONDS));
                            break;
                        }
                        case ":seven:" : {
                            displayMessage.delete().queue();
                            Message m = Msg.replyRet(event, "Enter the date when you joined/started playing Cubic Castles. (Ex: 07/10/2016)");
                            waiter.waitForEvent(GuildMessageReceivedEvent.class,
                                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                    e -> {
                                        String date = e.getMessage().getContentRaw().trim();
                                        e.getMessage().delete().queue();
                                        m.delete().queue();
                                        if(!Birthday.isThisDateValid(date, "MM/dd/yyyy") && !Birthday.isThisDateValid(date, "MM-dd-yyyy")) {
                                            Msg.bad(event, "Invalid date format. Following formats are acceptable:\n" +
                                                    "`MM/dd/YYYY` or `MM-dd-YYYY`\n" +
                                                    "Profile editing cancelled.");
                                            return;
                                        }
                                        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
                                        LocalDate givenDate = LocalDate.parse(sdf.format(date.replaceAll("/", "-")), DateTimeFormatter.ofPattern("MM-dd-yyyy"));
                                        LocalDate oldestDate = LocalDate.of(2014, Month.MAY, 1);
                                        if(!givenDate.isAfter(oldestDate) || !givenDate.isBefore(LocalDate.now().plusDays(1))){
                                            Msg.bad(event, "Date cannot be before May 1st, 2014 or after TODAY." +
                                                    "Profile editing cancelled.");
                                            return;
                                        }
                                        Qbee qbee = ProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                        qbee.setJoinDate(givenDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                                        ProfileReadWrite.updateUser(qbee);
                                    },
                                    30, TimeUnit.SECONDS, () -> Msg.replyTimed(event, "You took too long to respond, " + event.getAuthor().getAsMention() + "! Profile editing has been cancelled.", 10, TimeUnit.SECONDS));
                            break;
                        }
                        case ":eight:" : {
                            displayMessage.clearReactions().queue();
                            new ButtonMenu.Builder()
                                    .setDescription("Pick one of the options:\n" +
                                            ":one: Add Realm\n" +
                                            ":two: Remove Realm")
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
                                                displayMessage.delete().queue();
                                                Message m = Msg.replyRet(event, "Please enter the name of the realm you want to add.");
                                                waiter.waitForEvent(GuildMessageReceivedEvent.class,
                                                        e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                                        e -> {
                                                            String realmName = e.getMessage().getContentRaw().trim();
                                                            e.getMessage().delete().queue();
                                                            m.delete().queue();
                                                            if(realmName.length() > 60) {
                                                                Msg.bad(event, "Too long of a realm name, please make it less than 60 characters. Profile editing cancelled.");
                                                                return;
                                                            }
                                                            Qbee qbee = ProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                                            qbee.addRealm(realmName);
                                                            ProfileReadWrite.updateUser(qbee);
                                                        },
                                                        30, TimeUnit.SECONDS, () -> Msg.replyTimed(event, "You took too long to respond, " + event.getAuthor().getAsMention() + "! Profile editing has been cancelled.", 10, TimeUnit.SECONDS));
                                                break;
                                            }
                                            case ":two:" : {
                                                displayMessage.delete().queue();
                                                Message m = Msg.replyRet(event, "Please enter the name of the realm you want to remove.");
                                                waiter.waitForEvent(GuildMessageReceivedEvent.class,
                                                        e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                                        e -> {
                                                            String realmName = e.getMessage().getContentRaw().trim();
                                                            e.getMessage().delete().queue();
                                                            m.delete().queue();
                                                            if(realmName.length() > 60) {
                                                                Msg.bad(event, "Too long of a realm name, please make it less than 60 characters. Profile editing cancelled.");
                                                                return;
                                                            }
                                                            Qbee qbee = ProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                                            boolean wasRemoved = qbee.removeRealm(realmName);
                                                            if(!wasRemoved) {
                                                                Msg.bad(event, "The realm provided wasn't removed. It wasn't spelled correctly or was not in the list. Profile editing cancelled.");
                                                                return;
                                                            }
                                                            ProfileReadWrite.updateUser(qbee);
                                                        },
                                                        30, TimeUnit.SECONDS, () -> Msg.replyTimed(event, "You took too long to respond, " + event.getAuthor().getAsMention() + "! Profile editing has been cancelled.", 10, TimeUnit.SECONDS));
                                                break;
                                            }
                                            default : {
                                                displayMessage.delete().queue();
                                                Msg.reply(event.getTextChannel(), event.getAuthor().getAsMention()+" Cancelled editing the profile.");
                                                break;
                                            }
                                        }
                                    })
                                    .setFinalAction(me -> {
                                        try {
                                        }catch (Exception ignored) { }
                                    }).build().display(displayMessage);
                            break;
                        }
                        case ":nine:" : {
                            displayMessage.clearReactions().queue();
                            new ButtonMenu.Builder()
                                    .setDescription("Pick one of the options:\n" +
                                            ":one: Add Overworld\n" +
                                            ":two: Remove Overworld")
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
                                                displayMessage.delete().queue();
                                                Message m = Msg.replyRet(event, "Please enter the name of the overworld you want to add.");
                                                waiter.waitForEvent(GuildMessageReceivedEvent.class,
                                                        e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                                        e -> {
                                                            String overworldName = e.getMessage().getContentRaw().trim();
                                                            e.getMessage().delete().queue();
                                                            m.delete().queue();
                                                            if(overworldName.length() > 60) {
                                                                Msg.bad(event, "Too long of a overworld name, please make it less than 60 characters. Profile editing cancelled.");
                                                                return;
                                                            }
                                                            Qbee qbee = ProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                                            qbee.addOverworld(overworldName);
                                                            ProfileReadWrite.updateUser(qbee);
                                                        },
                                                        30, TimeUnit.SECONDS, () -> Msg.replyTimed(event, "You took too long to respond, " + event.getAuthor().getAsMention() + "! Profile editing has been cancelled.", 10, TimeUnit.SECONDS));
                                                break;
                                            }
                                            case ":two:" : {
                                                displayMessage.delete().queue();
                                                Message m = Msg.replyRet(event, "Please enter the name of the overworld you want to remove.");
                                                waiter.waitForEvent(GuildMessageReceivedEvent.class,
                                                        e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                                        e -> {
                                                            String overworldName = e.getMessage().getContentRaw().trim();
                                                            e.getMessage().delete().queue();
                                                            m.delete().queue();
                                                            if(overworldName.length() > 60) {
                                                                Msg.bad(event, "Too long of a overworld name, please make it less than 60 characters. Profile editing cancelled.");
                                                                return;
                                                            }
                                                            Qbee qbee = ProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                                            boolean wasRemoved = qbee.removeRealm(overworldName);
                                                            if(!wasRemoved) {
                                                                Msg.bad(event, "The overworld provided wasn't removed. It wasn't spelled correctly or was not in the list. Profile editing cancelled.");
                                                                return;
                                                            }
                                                            ProfileReadWrite.updateUser(qbee);
                                                        },
                                                        30, TimeUnit.SECONDS, () -> Msg.replyTimed(event, "You took too long to respond, " + event.getAuthor().getAsMention() + "! Profile editing has been cancelled.", 10, TimeUnit.SECONDS));
                                                break;
                                            }
                                            default : {
                                                displayMessage.delete().queue();
                                                Msg.reply(event.getTextChannel(), event.getAuthor().getAsMention()+" Cancelled editing the profile.");
                                                break;
                                            }
                                        }
                                    })
                                    .setFinalAction(me -> {
                                        try {
                                            me.delete().queue();
                                        }catch (ErrorResponseException ignored) { }
                                        catch (Exception ignored) { }
                                    }).build().display(displayMessage);
                            break;
                        }
                        default : {
                            Msg.reply(event.getTextChannel(), event.getAuthor().getAsMention()+" Cancelled editing the profile.");
                            break;
                        }
                    }
                })
                .setFinalAction(me -> {
                });
        bm.build().display(displayMessage);
    }
}
