package com.bulka.java.net.tg.bots.servermoder;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Bot extends TelegramLongPollingBot {
    private static final Logger logger = Logger.getLogger(Bot.class.getName());
    private String username = "";
    private long myID;
    private final HashMap<String, Command> commands = new HashMap<>();
    public String osName = System.getProperty("os.name");

    public Bot(DefaultBotOptions defaultBotOptions, String token, String username, long myID) {
        super(defaultBotOptions, token);
        this.username = username;
        this.myID = myID;

        commands.put("start", new Command("start") {
            @Override
            public void execute(Update update, String arguments) {
                Message message = update.getMessage();
                long chat_id = message.getChatId();
                long user_id = message.getFrom().getId();
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chat_id);
                sendMessage.setText("I`m working!");
                sendMessage(sendMessage);
            }
        });
        commands.put("reboot", new Command("reboot") {
            @Override
            public void execute(Update update, String arguments) {
                Message message = update.getMessage();
                long chat_id = message.getChatId();
                long user_id = message.getFrom().getId();
                if(user_id != myID)
                    return;
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chat_id);
                sendMessage.setText("Rebooting!");
                sendMessage(sendMessage);
                logger.log(Level.SEVERE, "!!!Got command - REBOOT!!!. From " + user_id);
                try {
                    reboot();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "!!!CAN`T REBOOT SYSTEM!!!", e);
                    sendMessage.setChatId(chat_id);
                    sendMessage.setText("Can`t reboot!\n" + e.getMessage());
                    sendMessage(sendMessage);
                }

            }
        });
    }

    public void postInit(){

    }

    @Override
    public void onUpdateReceived(Update update) {
        logger.finest("Got update");
        try {
            if (update.hasMessage()) {
                Message message = update.getMessage();
                if (message.hasText()) {
                    long user_id = message.getFrom().getId();
                    String text = message.getText();
                    if (user_id != myID || !message.getChat().isUserChat()) {
                        sendMessage(message.getChatId(), "Sorry, I don`t work");
                        return;
                    }
                    if (text.startsWith("/")) {
                        checkCommand(update);
                    }


                }
            }
        } catch (Exception e){
            logger.log(Level.SEVERE, "Error in handling update", e);
        }
    }

    public boolean checkCommand(Update update) {
        String input = update.getMessage().getText();
        if (input == null || input.isEmpty()) {
            return false;
        }

        input = input.trim();

        if (!input.startsWith("/")) {
            return false;
        }

        String commandPart;
        String botName = null;
        String arguments = null;

        String[] parts = input.substring(1).split(" ", 2);
        commandPart = parts[0];

        if (parts.length > 1) {
            arguments = parts[1].trim();
        }

        if (commandPart.contains("@")) {
            String[] commandParts = commandPart.split("@", 2);
            commandPart = commandParts[0];
            botName = commandParts[1];
        }

        Command command = commands.get(commandPart.toLowerCase());

        if (command == null) {
            return false;
        }

        if (botName != null && !botName.equalsIgnoreCase(username)) {
            return false;
        }

        command.execute(update, arguments);

        return true;
    }

    public int sendMessage(SendMessage sendMessage) {
        try {
            return execute(sendMessage).getMessageId();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in sending message in chat " + sendMessage.getChatId(), e);
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(long chatID, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatID);
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in sending message in chat " + chatID, e);
            throw new RuntimeException(e);
        }
    }

    public void editMessage(EditMessageText editMessageText) {
        try {
            execute(editMessageText);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in editing message in chat " + editMessageText.getChatId(), e);
            throw new RuntimeException(e);
        }
    }

    public void editMessage(long chatID, int messageID, String text, InlineKeyboardMarkup markup) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatID);
        editMessageText.setMessageId(messageID);
        editMessageText.setText(text);
        editMessageText.setReplyMarkup(markup);
        try {
            execute(editMessageText);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in editing message in chat " + chatID, e);
            throw new RuntimeException(e);
        }
    }

    public void deleteMessage(DeleteMessage deleteMessage) {
        try {
            execute(deleteMessage);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in deleting message in chat " + deleteMessage.getChatId(), e);
            throw new RuntimeException(e);
        }
    }

    public void deleteMessage(long chatID, int messageID) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatID);
        deleteMessage.setMessageId(messageID);
        try {
            execute(deleteMessage);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in deleting message in chat " + chatID, e);
            throw new RuntimeException(e);
        }
    }

    public void reboot() throws Exception {
        if (osName.toLowerCase().contains("windows")) {
            rebootWindows();
        } else if (osName.toLowerCase().contains("linux") || osName.toLowerCase().contains("unix") || osName.toLowerCase().contains("mac os x")) {
            rebootLinux();
        } else {
            logger.log(Level.SEVERE, "Can`t reboot, unknown system " + osName);
        }
    }

    public void rebootWindows() throws Exception {
        Process process = Runtime.getRuntime().exec("shutdown /r /t 0 /f");
    }
    public void rebootLinux() throws IOException {
        Process process = Runtime.getRuntime().exec("shutdown -r now");
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }
}
