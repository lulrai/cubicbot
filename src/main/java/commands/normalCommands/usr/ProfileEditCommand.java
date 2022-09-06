package commands.normalCommands.usr;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import commands.core.Cubic;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import commands.normalCommands.bingo.BingoItem;
import commands.utils.Msg;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class ProfileEditCommand extends Command {
    private EventWaiter waiter;
    public ProfileEditCommand() {
        this.name = "editprofile";
        this.aliases = new String[]{"ep"};
        this.category = new Category("Profile");
        this.ownerCommand = false;
        this.cooldown = 10;
        this.cooldownScope = CooldownScope.USER;
        this.arguments = "";
        this.help = "Edit your profile to change, add, or remove certain aspects.";
        this.waiter = Cubic.getWaiter();
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if(event.isFromType(ChannelType.TEXT)) {
            event.getMessage().delete().queue();
        }
        Message displayMessage = event.getChannel().sendMessage("Editing " + event.getAuthor().getAsMention() + "'s profile..").complete();
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
                .setAction(v -> {
                    Color c = Color.CYAN;
                    if(event.isFromType(ChannelType.TEXT)){
                        c = event.getMember().getColor();
                    }

                    int index = -1;
                    for(int i = 0; i < GameProfileCommand.qbees.size(); i++){
                        if(GameProfileCommand.qbees.get(i).getUserID().equals(event.getAuthor().getId())){
                            index = i;
                            break;
                        }
                    }
                    if(index == -1){
                        Qbee qbee = new Qbee(event.getAuthor().getId(), event.getAuthor().getEffectiveAvatarUrl());
                        GameProfileCommand.qbees.add(qbee);
                    }

                    String choice = EmojiParser.parseToAliases(v.getEmoji());
                    switch(choice.trim()){
                        case ":picture_frame:" : {
                            displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), c, event.getAuthor().getAsMention()+" Please type the link for your profile picture that you want or upload an image file.", "Or type \"default\" to set it to your discord profile pic.")).queue();
                            waiter.waitForEvent(MessageReceivedEvent.class,
                                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                    e -> {
                                        String profilePic = e.getMessage().getContentRaw().trim();
                                        if(!e.getMessage().getAttachments().isEmpty() && e.getMessage().getAttachments().get(0).isImage()){
                                            profilePic = e.getMessage().getAttachments().get(0).getUrl();
                                        }
                                        else if(profilePic.equalsIgnoreCase("default")){
                                            profilePic = e.getAuthor().getEffectiveAvatarUrl();
                                        }
                                        else {
                                            try{
                                                new URL(profilePic);
                                            } catch (MalformedURLException ex) {
                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.RED,event.getAuthor().getAsMention()+" Invalid URL or no image file attached. Profile editing cancelled.", "")).queue();
                                                return;
                                            }
                                        }
                                        if(e.isFromType(ChannelType.TEXT)) e.getMessage().delete().queue();
                                        Qbee qbee = GameProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                        qbee.setProfilePic(profilePic);
                                        ProfileReadWrite.updateUser(qbee);
                                        displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.GREEN, event.getAuthor().getAsMention()+" Successfully updated the profile.", "")).queue();
                                    },
                                    1, TimeUnit.MINUTES, () -> displayMessage.delete().queue());
                            break;
                        }
                        case ":link:" : {
                            displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), c, event.getAuthor().getAsMention()+" Please type the link for forum profile if you have one.\n**NOTE** Link isn't visible but it makes the title of your profile clickable/tapable."
                            ,"Or type \"none\" to remove the current value")).queue();
                            waiter.waitForEvent(MessageReceivedEvent.class,
                                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                    e -> {
                                        String profileLink = e.getMessage().getContentRaw().trim();
                                        if(e.isFromType(ChannelType.TEXT)) e.getMessage().delete().queue();
                                        if(profileLink.equalsIgnoreCase("none")) profileLink = "";
                                        else {
                                            try {
                                                new URL(profileLink);
                                                if (!profileLink.startsWith("https://forums2.cubiccastles.com/") && !profileLink.startsWith("http://forums2.cubiccastles.com/"))
                                                    throw new MalformedURLException();
                                            } catch (MalformedURLException ex) {
                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.RED, event.getAuthor().getAsMention()+" Invalid link provided. Please provide a valid FORUM link. No other links are accepted. Profile editing cancelled.", "")).queue();
                                                return;
                                            }
                                        }
                                        Qbee qbee = GameProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                        qbee.setForumLink(profileLink);
                                        ProfileReadWrite.updateUser(qbee);
                                        displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.GREEN, event.getAuthor().getAsMention()+" Successfully updated the profile.", "")).queue();
                                    },
                                    1, TimeUnit.MINUTES, () -> displayMessage.delete().queue());
                            break;
                        }
                        case ":one:" : {
                            displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), c, event.getAuthor().getAsMention()+" Please type what you want your in-game name to be.", "Or type \"none\" to remove the current value")).queue();
                            waiter.waitForEvent(MessageReceivedEvent.class,
                                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                    e -> {
                                        String name = e.getMessage().getContentRaw().trim();
                                        if(e.isFromType(ChannelType.TEXT)) e.getMessage().delete().queue();
                                        if(name.equalsIgnoreCase("none")){
                                            name = "";
                                        }
                                        else {
                                            if (name.length() > 60) {
                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.RED,  "Name too long, please make it shorter. Profile editing cancelled.", "")).queue();
                                                return;
                                            }
                                        }
                                        Qbee qbee = GameProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                        qbee.setGameName(name);
                                        ProfileReadWrite.updateUser(qbee);
                                        displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.GREEN, event.getAuthor().getAsMention()+" Successfully updated the profile.", "")).queue();
                                    },
                                    30, TimeUnit.SECONDS, () -> displayMessage.delete().queue());
                            break;
                        }
                        case ":two:" : {
                            displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), c, event.getAuthor().getAsMention()+" Please type what you want your \"About me\" commands.information to be. (Less than 120 characters)", "Or type \"none\" to remove the current value")).queue();
                            waiter.waitForEvent(MessageReceivedEvent.class,
                                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                    e -> {
                                        String about = e.getMessage().getContentRaw().trim();
                                        if(e.isFromType(ChannelType.TEXT)) e.getMessage().delete().queue();
                                        if(about.equalsIgnoreCase("none")){
                                            about = "";
                                        }
                                        else {
                                            if (about.length() > 120) {
                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.RED,  "Too long of an about me/description. Profile editing cancelled.", "")).queue();
                                                return;
                                            }
                                        }
                                        Qbee qbee = GameProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                        qbee.setAbout(about);
                                        ProfileReadWrite.updateUser(qbee);
                                        displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.GREEN, event.getAuthor().getAsMention()+" Successfully updated the profile.", "")).queue();
                                    },
                                    2, TimeUnit.MINUTES, () -> displayMessage.delete().queue());
                            break;
                        }
                        case ":three:" : {
                            displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), c, event.getAuthor().getAsMention()+" Please type your birthday, just month and day. DO NOT TYPE YEAR. Preferred format is Month-Day (Ex: 07/23)\n" +
                                    "Format: `M/d` or `d/M` or `M-d` or `d-M`", "Or type \"none\" to remove the current value")).queue();
                            waiter.waitForEvent(MessageReceivedEvent.class,
                                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                    e -> {
                                        String date = e.getMessage().getContentRaw().trim();
                                        if(e.isFromType(ChannelType.TEXT)) e.getMessage().delete().queue();
                                        Birthday b;
                                        if(date.equalsIgnoreCase("none")){
                                            b = null;
                                        }
                                        else {
                                            b = Birthday.parseBirthday(date.replaceAll("/", "-") + "-2000");
                                            if (b == null) {
                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.RED,  "Invalid format, use this format:" +
                                                        "`M/d` or `d/M` or `M-d` or `d-M`\n" +
                                                        "Ex: 07/23\n" +
                                                        "Profile editing cancelled.", "")).queue();
                                                return;
                                            }
                                        }
                                        Qbee qbee = GameProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                        qbee.setBirthday(b);
                                        ProfileReadWrite.updateUser(qbee);
                                        GameProfileCommand.qbees.remove(qbee);
                                        int i = Collections.binarySearch(GameProfileCommand.qbees, qbee, new Qbee.QbeeSortingComparator());
                                        if(i < 0){
                                            i = ~i;
                                        }
                                        GameProfileCommand.qbees.add(i, qbee);
                                        displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.GREEN, event.getAuthor().getAsMention()+" Successfully updated the profile.", "")).queue();
                                    },
                                    30, TimeUnit.SECONDS, () -> displayMessage.delete().queue());
                            break;
                        }
                        case ":four:" : {
                            displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), c, event.getAuthor().getAsMention()+"Please enter what your favorite item is.", "Or type \"none\" to remove the current value")).queue();
                            waiter.waitForEvent(MessageReceivedEvent.class,
                                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                    e -> {
                                        String item = e.getMessage().getContentRaw().trim();
                                        if(e.isFromType(ChannelType.TEXT)) e.getMessage().delete().queue();
                                        if(item.equalsIgnoreCase("none")){
                                            item = "";
                                        }
                                        else {
                                            if (item.length() > 60) {
                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.RED,  "Too long of an item name, please make it less than 60 characters. Profile editing cancelled.", "")).queue();
                                                return;
                                            }
                                        }
                                        Qbee qbee = GameProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                        qbee.setFavoriteItem(item);
                                        ProfileReadWrite.updateUser(qbee);
                                        displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.GREEN, event.getAuthor().getAsMention()+" Successfully updated the profile.", "")).queue();
                                    },
                                    30, TimeUnit.SECONDS, () -> displayMessage.delete().queue());
                            break;
                        }
                        case ":five:" : {
                            displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), c, event.getAuthor().getAsMention()+"Please enter your level, ONLY numbers.", "Or type \"none\" to remove the current value")).queue();
                            waiter.waitForEvent(MessageReceivedEvent.class,
                                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                    e -> {
                                        int level;
                                        if(e.getMessage().getContentRaw().trim().equalsIgnoreCase("none")){
                                            level = -1;
                                            if(e.isFromType(ChannelType.TEXT)) e.getMessage().delete().queue();
                                        }
                                        else {
                                            try {
                                                level = Integer.parseInt(e.getMessage().getContentRaw().trim());
                                            } catch (NumberFormatException n) {
                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.RED,  "Not a number, please only enter a number. Profile editing cancelled.", "")).queue();
                                                return;
                                            }
                                            if(e.isFromType(ChannelType.TEXT)) e.getMessage().delete().queue();
                                            if (level > 60 || level < 0) {
                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.RED,  "Invalid level number. Profile editing cancelled.", "")).queue();
                                                return;
                                            }
                                        }
                                        Qbee qbee = GameProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                        qbee.setLevel(level);
                                        ProfileReadWrite.updateUser(qbee);
                                        displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.GREEN, event.getAuthor().getAsMention()+" Successfully updated the profile.", "")).queue();
                                    },
                                    30, TimeUnit.SECONDS, () -> displayMessage.delete().queue());
                            break;
                        }
                        case ":six:" : {
                            displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), c, event.getAuthor().getAsMention()+"Please enter your clan name.", "Or type \"none\" to remove the current value")).queue();
                            waiter.waitForEvent(MessageReceivedEvent.class,
                                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                    e -> {
                                        String clan = e.getMessage().getContentRaw().trim();
                                        if(e.isFromType(ChannelType.TEXT)) e.getMessage().delete().queue();
                                        if(clan.equalsIgnoreCase("none")){
                                            clan = "";
                                        }
                                        else {
                                            if (clan.length() > 60) {
                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.RED,  "Too long of a clan name, please make it less than 60 characters. Profile editing cancelled.", "")).queue();
                                                return;
                                            }
                                        }
                                        Qbee qbee = GameProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                        qbee.setClan(clan);
                                        ProfileReadWrite.updateUser(qbee);
                                        displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.GREEN, event.getAuthor().getAsMention()+" Successfully updated the profile.", "")).queue();
                                    },
                                    30, TimeUnit.SECONDS, () -> displayMessage.delete().queue());
                            break;
                        }
                        case ":seven:" : {
                            displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), c, event.getAuthor().getAsMention()+"Enter the date when you joined/started playing Cubic Castles. Preferred format is Month-Day-Year. (Ex: 07/23/2016)\n" +
                                    "Formats acceptable: `M/d/Y` or `M-d-Y` or `d-M-Y` or `d/M/Y`", "Or type \"none\" to remove the current value")).queue();
                            waiter.waitForEvent(MessageReceivedEvent.class,
                                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                    e -> {
                                        String date = e.getMessage().getContentRaw().trim();
                                        if(e.isFromType(ChannelType.TEXT)) e.getMessage().delete().queue();
                                        if(date.equalsIgnoreCase("none")){
                                            Qbee qbee = GameProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                            qbee.setJoinDate(null);
                                            ProfileReadWrite.updateUser(qbee);
                                        }
                                        else {
                                            LocalDate givenDate = Birthday.isThisDateValid(date, new String[]{"M/d/y", "M-d-y", "d/M/y", "d-M-y", "d.M.y", "M.d.y"});
                                            if (givenDate == null) {
                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.RED,  "Invalid date format. Following formats are acceptable:\n" +
                                                        "`M/d/Y` or `M-d-Y` or `d-M-Y` or `d/M/Y`\n" +
                                                        "Profile editing cancelled.", "")).queue();
                                                return;
                                            }
                                            LocalDate oldestDate = LocalDate.of(2014, Month.MAY, 1);
                                            if (!givenDate.isAfter(oldestDate) || !givenDate.isBefore(LocalDate.now().plusDays(1))) {
                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.RED,  event.getAuthor().getAsMention() + "Date cannot be before May 1st, 2014 or after TODAY.\n" +
                                                        "Profile editing cancelled.", "")).queue();
                                                return;
                                            }
                                            Qbee qbee = GameProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                            qbee.setJoinDate(givenDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                                            ProfileReadWrite.updateUser(qbee);
                                            displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.GREEN, event.getAuthor().getAsMention()+" Successfully updated the profile.", "")).queue();
                                        }
                                    },
                                    30, TimeUnit.SECONDS, () -> displayMessage.delete().queue());
                            break;
                        }
                        case ":eight:" : {
                            ButtonMenu.Builder be = new ButtonMenu.Builder()
                                    .setDescription("Pick one of the options:\n" +
                                            ":one: Add Realm\n" +
                                            ":two: Remove Realm")
                                    .setEventWaiter(waiter)
                                    .addChoices(BingoItem.getEmojis(2))
                                    .addChoice(EmojiManager.getForAlias("x").getUnicode())
                                    .setUsers(event.getAuthor())
                                    .setTimeout(20, TimeUnit.SECONDS)
                                    .setAction(em -> {
                                        Color col = Color.CYAN;
                                        if(event.isFromType(ChannelType.TEXT)){
                                            col = event.getMember().getColor();
                                        }
                                        String option = EmojiParser.parseToAliases(em.getEmoji());
                                        switch (option.trim()){
                                            case ":one:" : {
                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), col, event.getAuthor().getAsMention()+"Please enter the name of the realm you want to add.", "")).queue();
                                                waiter.waitForEvent(MessageReceivedEvent.class,
                                                        e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                                        e -> {
                                                            String realmName = e.getMessage().getContentRaw().trim();
                                                            if(e.isFromType(ChannelType.TEXT)) e.getMessage().delete().queue();
                                                            if(realmName.length() > 60) {
                                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.RED,  event.getAuthor().getAsMention()+"Too long of a realm name, please make it less than 60 characters. Profile editing cancelled.", "")).queue();
                                                                return;
                                                            }
                                                            Qbee qbee = GameProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                                            qbee.addRealm(realmName);
                                                            ProfileReadWrite.updateUser(qbee);
                                                            displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.GREEN, event.getAuthor().getAsMention()+" Successfully updated the profile.", "")).queue();
                                                        },
                                                        30, TimeUnit.SECONDS, () -> displayMessage.delete().queue());
                                                break;
                                            }
                                            case ":two:" : {
                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), col, event.getAuthor().getAsMention()+" Please enter the name of the realm you want to remove.", "")).queue();
                                                waiter.waitForEvent(MessageReceivedEvent.class,
                                                        e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                                        e -> {
                                                            String realmName = e.getMessage().getContentRaw().trim();
                                                            if(e.isFromType(ChannelType.TEXT)) e.getMessage().delete().queue();
                                                            if(realmName.length() > 60) {
                                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.RED,  event.getAuthor().getAsMention()+" Too long of a realm name, please make it less than 60 characters. Profile editing cancelled.", "")).queue();
                                                                return;
                                                            }
                                                            Qbee qbee = GameProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                                            boolean wasRemoved = qbee.removeRealm(realmName);
                                                            if(!wasRemoved) {
                                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.RED,  event.getAuthor().getAsMention()+" The realm provided wasn't removed. It wasn't spelled correctly or was not in the list. Profile editing cancelled.", "")).queue();
                                                                return;
                                                            }
                                                            ProfileReadWrite.updateUser(qbee);
                                                            displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.GREEN, event.getAuthor().getAsMention()+" Successfully updated the profile.", "")).queue();
                                                        },
                                                        30, TimeUnit.SECONDS, () -> displayMessage.delete().queue());
                                                break;
                                            }
                                            default : {
                                                Msg.reply(event.getChannel(), event.getAuthor().getAsMention()+" Cancelled editing the profile.");
                                                break;
                                            }
                                        }
                                    })
                                    .setFinalAction(me -> {
                                        try {
                                            if(displayMessage.isFromType(ChannelType.TEXT)) displayMessage.clearReactions().queue( ve -> {} , q-> {});
                                        }catch (Exception ignored) { }
                                    });
                            if(event.isFromType(ChannelType.TEXT)){
                                be.setColor(event.getMember().getColor());
                            }
                            else{
                                be.setColor(Color.WHITE);
                            }
                            be.build().display(displayMessage);
                            break;
                        }
                        case ":nine:" : {
                            ButtonMenu.Builder be = new ButtonMenu.Builder()
                                    .setDescription("Pick one of the options:\n" +
                                            ":one: Add Overworld\n" +
                                            ":two: Remove Overworld")
                                    .setEventWaiter(waiter)
                                    .addChoices(BingoItem.getEmojis(2))
                                    .addChoice(EmojiManager.getForAlias("x").getUnicode())
                                    .setUsers(event.getAuthor())
                                    .setTimeout(20, TimeUnit.SECONDS)
                                    .setAction(em -> {
                                        Color col = Color.CYAN;
                                        if(event.isFromType(ChannelType.TEXT)){
                                            col = event.getMember().getColor();
                                        }
                                        String option = EmojiParser.parseToAliases(em.getEmoji());
                                        switch (option.trim()){
                                            case ":one:" : {
                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), col, event.getAuthor().getAsMention()+"Please enter the name of the overworld you want to add.", "")).queue();
                                                waiter.waitForEvent(MessageReceivedEvent.class,
                                                        e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                                        e -> {
                                                            String overworldName = e.getMessage().getContentRaw().trim();
                                                            if(e.isFromType(ChannelType.TEXT)) e.getMessage().delete().queue();
                                                            if(overworldName.length() > 60) {
                                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.RED,  event.getAuthor().getAsMention()+"Too long of a overworld name, please make it less than 60 characters. Profile editing cancelled.", "")).queue();
                                                                return;
                                                            }
                                                            Qbee qbee = GameProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                                            qbee.addOverworld(overworldName);
                                                            ProfileReadWrite.updateUser(qbee);
                                                            displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.GREEN, event.getAuthor().getAsMention()+" Successfully updated the profile.", "")).queue();
                                                        },
                                                        30, TimeUnit.SECONDS, () -> displayMessage.delete().queue());
                                                break;
                                            }
                                            case ":two:" : {
                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), col, event.getAuthor().getAsMention()+"Please enter the name of the overworld you want to remove.", "")).queue();
                                                waiter.waitForEvent(MessageReceivedEvent.class,
                                                        e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                                                        e -> {
                                                            String overworldName = e.getMessage().getContentRaw().trim();
                                                            if(e.isFromType(ChannelType.TEXT)) e.getMessage().delete().queue();
                                                            if(overworldName.length() > 60) {
                                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.RED,  event.getAuthor().getAsMention()+"Too long of a overworld name, please make it less than 60 characters. Profile editing cancelled.", "")).queue();
                                                                return;
                                                            }
                                                            Qbee qbee = GameProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(event.getAuthor().getId())).findFirst().get();
                                                            boolean wasRemoved = qbee.removeOverworld(overworldName);
                                                            if(!wasRemoved) {
                                                                displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.RED,  event.getAuthor().getAsMention()+"The overworld provided wasn't removed. It wasn't spelled correctly or was not in the list. Profile editing cancelled.", "")).queue();
                                                                return;
                                                            }
                                                            ProfileReadWrite.updateUser(qbee);
                                                            displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.GREEN, event.getAuthor().getAsMention()+" Successfully updated the profile.", "")).queue();
                                                        },
                                                        30, TimeUnit.SECONDS, () -> displayMessage.delete().queue());
                                                break;
                                            }
                                            default : {
                                                Msg.reply(event.getChannel(), event.getAuthor().getAsMention()+" Cancelled editing the profile.");
                                                break;
                                            }
                                        }
                                    })
                                    .setFinalAction(me -> {
                                        try {
                                            if(displayMessage.isFromType(ChannelType.TEXT)) displayMessage.clearReactions().queue( ve -> {} , q-> {});
                                        } catch (Exception ignored) { }
                                    });
                            if(event.isFromType(ChannelType.TEXT)){
                                be.setColor(event.getMember().getColor());
                            }
                            else{
                                be.setColor(Color.WHITE);
                            }
                            be.build().display(displayMessage);
                            break;
                        }
                        default : {
                            Msg.reply(event.getChannel(), event.getAuthor().getAsMention()+" Cancelled editing the profile.");
                            break;
                        }
                    }
                })
                .setFinalAction(me -> {
                    if(displayMessage.isFromType(ChannelType.TEXT)) displayMessage.clearReactions().queue( v -> {} , q-> {});
                });
        if(event.isFromType(ChannelType.TEXT)){
            bm.setColor(event.getMember().getColor());
        }
        else{
            bm.setColor(Color.WHITE);
        }
        bm.build().display(displayMessage);
    }
}
