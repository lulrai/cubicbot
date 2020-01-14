/*package cubicCastles;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.apache.commons.validator.routines.EmailValidator;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;
import utils.ImageURLUtil;
import utils.Msg;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class SumbitReportCommand extends Command {
    public SumbitReportCommand() {
        this.name = "report";
        this.aliases = new String[]{"support", "rprt"};
        this.category = new Category("Cubic Castles");
        this.ownerCommand = false;
        this.cooldown = 3;
    }

    private static String[] submitForm(String optionValue, String email, String fullName, String phoneNum, String inGameName, String realmName, String issueSummary, String issueDetails, File file, File[] files) {
        String[] reply = new String[2];
        try {
            java.util.logging.Logger.getLogger("com.gargoylesoftware.htm‌​lunit").setLevel(Level.OFF);
            java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
            java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

            final WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.setCssErrorHandler(new ErrorHandler() {
                @Override
                public void error(CSSParseException arg0) throws CSSException {
                    return;
                }

                @Override
                public void fatalError(CSSParseException arg0) throws CSSException {
                    System.out.println(arg0.getMessage());
                }

                @Override
                public void warning(CSSParseException arg0) throws CSSException {
                    return;
                }
            });
            webClient.setJavaScriptErrorListener(new JavaScriptErrorListener() {
                @Override
                public void timeoutError(HtmlPage arg0, long arg1, long arg2) {
                    return;
                }

                @Override
                public void scriptException(HtmlPage arg0, ScriptException arg1) {
                    return;
                }

                @Override
                public void malformedScriptURL(HtmlPage arg0, String arg1, MalformedURLException arg2) {
                    return;
                }

                @Override
                public void loadScriptError(HtmlPage arg0, URL arg1, Exception arg2) {
                    return;
                }
            });
            webClient.setHTMLParserListener(new HTMLParserListener() {
                @Override
                public void error(String message, URL url, String html, int line, int column, String key) {
                    return;
                }

                @Override
                public void warning(String message, URL url, String html, int line, int column, String key) {
                    return;
                }
            });

            // Get the first page
            final HtmlPage page1 = webClient.getPage("http://help.cubiccastles.com/support/open.php");


            // Get the form that we are dealing with and within that form,
            // find the submit button and the field that we want to change.
            final HtmlForm form = page1.getHtmlElementById("ticketForm");
            final HtmlSubmitInput button = form.getInputByValue("Create Ticket");

            //Select Help Topic
            HtmlSelect selectHelp = (HtmlSelect) page1.getElementById("topicId");
            HtmlOption option = selectHelp.getOptionByValue(optionValue);
            selectHelp.setSelectedAttribute(option, true);

            DomNodeList<DomElement> allInputs = page1.getElementsByTagName("input");

            //System.out.println(page1.asXml());

            //Email Address
            HtmlTextInput emailField = (HtmlTextInput) allInputs.get(2);
            emailField.setValueAttribute(email);

            //Full Name
            HtmlTextInput fullNameField = (HtmlTextInput) allInputs.get(3);
            fullNameField.setValueAttribute(fullName);

            //Phone Number
            HtmlTextInput phoneNumField = (HtmlTextInput) allInputs.get(4);
            phoneNumField.setValueAttribute(phoneNum);

            //In-game User Name
            HtmlTextInput inGameNameField = (HtmlTextInput) allInputs.get(6);
            inGameNameField.setValueAttribute(inGameName);

            //Realm Name
            HtmlTextInput realmNameField = (HtmlTextInput) allInputs.get(7);
            realmNameField.setValueAttribute(realmName);

            //Issue Summary
            HtmlTextInput issueSummaryField = (HtmlTextInput) allInputs.get(8);
            issueSummaryField.setValueAttribute(issueSummary);

            //Files
            if (file != null && file.exists()) {
                HtmlFileInput filesField = (HtmlFileInput) allInputs.get(9);
                filesField.setFiles(file);
                file.delete();
            } else if (files[0] != null && files[0].exists()) {
                HtmlFileInput filesField = (HtmlFileInput) allInputs.get(9);
                filesField.setFiles(files);
                for (int i = 0; i < files.length; i++) {
                    files[i].delete();
                }
            }

            //Issue Description
            HtmlTextArea issueDescField = form.getTextAreaByName("message");
            issueDescField.setText(issueDetails);

            //System.out.println(page1.asText());

            HtmlPage page2 = button.click();
            webClient.waitForBackgroundJavaScript(2000);

            DomNodeList<DomElement> divs = page2.getElementsByTagName("div");
            reply[1] = divs.get(3).asText();

            //System.out.println(page2.asText());
            //System.out.println(page2.asXml());

            reply[0] = "Cubic Castles Support Ticket successfully created and sent.";

            webClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reply;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.getMessage().delete().queue();
            Msg.badTimed(event, "USAGE:\n\n"
                    + "Separator: \"|\"\n\n"
                    + "report arg1 | arg2 | arg3 | arg4 | arg5 | arg6 | arg7 | arg8 | arg9\n\n"
                    + "HelpType (arg1): 		\n*Feedback, General Inquiry, Report A Problem, Access Issue (one of these)*\n\n"
                    + "Email (arg2): 			\n*A valid email address*\n\n"
                    + "FullName (arg3): 		\n*Your full name, if you are concered about privacy, put your In-Game Name*\n\n"
                    + "PhoneNum (arg4): 		\n*Put your phone number or write in \"none\"*\n\n"
                    + "InGameName (arg5): 		\n*Your In-Game Name for Cubic Castles*\n\n"
                    + "RealmName (arg6): 		\n*The name of the realm in Cubic Castles*\n\n"
                    + "IssueSummary (arg7): 	\n*Quick little summary of the issue being discussed*\n\n"
                    + "IssueDetails (arg8): 	\n*Details about the issue being discussed*\n\n"
                    + "ImageLinks (arg9): 		\n*OPTIONAL, links of images to be uploaded to the ticket*\n\n"
                    + "Note: If you only have one file or image, please attach it and use the command as a command. If you have more than one files, only images can be used for links.", 30, TimeUnit.SECONDS);
            return;
        }
        String allArgs = event.getMessage().getContentRaw().split(" ", 2)[1];
        String[] eachArg = allArgs.split("\\|");
        if (eachArg.length < 8) {
            Msg.normalTimedMessage(event, "Not enough arguments.", 5, TimeUnit.SECONDS);
            event.getMessage().delete().queue();
            return;
        }

        String optionValue = "";
        String email = "";
        String fullName = "";
        String phoneNum = "";
        String inGameName = "";
        String realmName = "";
        String issueSummary = "";
        String issueDetails = "";

        event.getMessage().delete().queue();

        //For option
        switch (eachArg[0].trim().toLowerCase()) {
            case "feedback": {
                optionValue = "2";
                break;
            }
            case "general inquiry": {
                optionValue = "1";
                break;
            }
            case "report a problem": {
                optionValue = "10";
                break;
            }
            case "access issue": {
                optionValue = "11";
                break;
            }
            default: {
                Msg.badTimed(event, "Not a valid type of support ticket (arg1). Please choose one of the following:\n"
                        + "Feedback\n"
                        + "General Inquiry\n"
                        + "Report a Problem\n"
                        + "Access Issue", 10, TimeUnit.SECONDS);
                return;
            }
        }

        //For email
        if (eachArg[1].trim().length() > 64 || eachArg[1].trim().length() < 1) {
            Msg.badTimed(event, "Email (arg2) too long or too short, please make it more than 0 characters and less than 64 characters.", 10, TimeUnit.SECONDS);
            return;
        } else {
            if (EmailValidator.getInstance().isValid(eachArg[1].trim())) {
                email = eachArg[1].trim();
            } else {
                Msg.badTimed(event, "Invalid email (arg2), please use a valid email.", 10, TimeUnit.SECONDS);
                return;
            }
        }

        //For fullName
        if (eachArg[2].trim().length() > 64 || eachArg[2].trim().length() < 1) {
            Msg.badTimed(event, "Full Name (arg3) too long or too short, please make it more than 0 characters and less than 64 characters.", 10, TimeUnit.SECONDS);
            return;
        } else {
            fullName = eachArg[2].trim();
        }

        //Phone
        if (eachArg[3].trim().matches("^[0-9]{10}$")) {
            phoneNum = eachArg[3].trim();
        } else if (eachArg[3].trim().equalsIgnoreCase("none")) {
            phoneNum = "";
        } else {
            Msg.badTimed(event, "Invalid phone number (arg4), please enter a valid phone number or put in \"none\".", 10, TimeUnit.SECONDS);
            return;
        }

        //In-Game Name
        if (eachArg[4].trim().length() > 30 || eachArg[4].trim().length() < 1) {
            Msg.badTimed(event, "In-Game name (arg5) is too long or too short, please make it more than 0 characters and less than 30 characters.", 10, TimeUnit.SECONDS);
            return;
        } else {
            inGameName = eachArg[4].trim();
        }

        //Realm Name
        if (eachArg[5].trim().length() > 30) {
            Msg.badTimed(event, "Realm name (arg6) is too long, please make it less than 30 characters.", 10, TimeUnit.SECONDS);
            return;
        } else {
            realmName = eachArg[5].trim();
        }

        //Issue Summary
        if (eachArg[6].trim().length() > 50) {
            Msg.badTimed(event, "Issue summary (arg7) is too long, please make it less than 50 characters.", 10, TimeUnit.SECONDS);
            return;
        } else {
            issueSummary = eachArg[6].trim();
        }

        //Issue Description
        issueDetails = eachArg[7].trim();

        //For files
        File attached = null;
        File[] links = null;
        if (!event.getMessage().getAttachments().isEmpty()) {
            attached = new File(event.getMessage().getAttachments().get(0).getFileName());
            if (event.getMessage().getAttachments().get(0).download(attached)) {
                if (attached.length() > 1000000) {
                    Msg.badTimed(event, "Files (attached file) larger than 1 MB is not supported.", 10, TimeUnit.SECONDS);
                    attached.delete();
                    return;
                }
            } else {
                Msg.badTimed(event, "Failed to fetch the attachment.", 10, TimeUnit.SECONDS);
                return;
            }
        } else if (eachArg.length > 8) {
            List<String> allLink = Arrays.asList(eachArg[8].trim().split(" "));
            links = new File[allLink.size()];
            for (int i = 0; i < allLink.size(); i++) {
                if (ImageURLUtil.getContentType(allLink.get(i).trim()).equalsIgnoreCase("image")) {
                    links[i] = ImageURLUtil.getFileFromURL(allLink.get(i));
                    if (links[i].length() > 1000000) {
                        Msg.badTimed(event, "Files (link files/arg9) larger than 1 MB is not supported.", 10, TimeUnit.SECONDS);
                        for (int j = 0; j <= i; j++) {
                            links[j].delete();
                        }
                        return;
                    }
                } else {
                    Msg.badTimed(event, "Only image links (arg9) supported. Please make sure your URL ends with an image extension like .jpg, .png, etc.\nUse `.imgur` command with an attachment to get an imgur link for that attachment image.", 10, TimeUnit.SECONDS);
                    return;
                }
            }
        }

		System.out.println("Details:\n"
				+ "Chosen Option: " + optionValue + "\n"
				+ "Emails:        " + email + "\n"
				+ "Full Name:     " + fullName + "\n"
				+ "Phone Number:  " + phoneNum + "\n"
				+ "In-Game Name:  " + inGameName + "\n"
				+ "Realm Name:    " + realmName + "\n"
				+ "Issue Summary: " + issueSummary + "\n"
				+ "Issue Details: " + issueDetails + "\n");

        String[] reply = submitForm(optionValue, email, fullName, phoneNum, inGameName, realmName, issueSummary, issueDetails, attached, links);

        Msg.replyTimed(event, reply[0], 5, TimeUnit.SECONDS);

        event.getAuthor().openPrivateChannel().queue(v -> {
            v.sendMessage(reply[1]).queue();
        });
    }
}*/
