package utils;

import com.jagrosh.jdautilities.command.CommandEvent;

public enum ErrorHandling {
    BOT_ERROR,
    USER_PERMISSION_ERROR,
    BOT_PERMISSION_ERROR,
    EMPTY_MENTION_ERROR;

    public void error(CommandEvent event) {
        switch (this) {
            case BOT_ERROR: {
                Msg.bad(event, "A bot may not perform this command and/or the provided user is a bot.");
                return;
            }
            case USER_PERMISSION_ERROR: {
                Msg.bad(event, "You do not have permission to perform this command.");
                return;
            }
            case BOT_PERMISSION_ERROR: {
                Msg.bad(event, "I do not have permission to perform this command.");
                return;
            }
            case EMPTY_MENTION_ERROR: {
                Msg.bad(event, "Please mention who you would like to perform this command on.");
                return;
            }
        }
    }
}
