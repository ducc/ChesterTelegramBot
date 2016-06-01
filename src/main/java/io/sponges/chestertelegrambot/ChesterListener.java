package io.sponges.chestertelegrambot;

import org.json.JSONObject;
import pro.zackpollard.telegrambot.api.chat.Chat;
import pro.zackpollard.telegrambot.api.chat.GroupChat;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.message.CommandMessageReceivedEvent;
import pro.zackpollard.telegrambot.api.event.chat.message.TextMessageReceivedEvent;
import pro.zackpollard.telegrambot.api.user.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ChesterListener implements Listener {

    private List<String> respondToAllMessages = new ArrayList<>();

    private final Chester chester;

    public ChesterListener(Chester chester) {
        this.chester = chester;
    }

    @Override
    public void onTextMessageReceived(TextMessageReceivedEvent event) {
        String message = event.getContent().getContent();
        Chat chat = event.getChat();
        if (message.startsWith(Chester.BOT_USERNAME)) {
            message = message.replace(Chester.BOT_USERNAME, "");
        } else if (chat instanceof GroupChat && !respondToAllMessages.contains(chat.getId())) { //SuperGroupChat extends GroupChat
            return;
        }

        String response = this.askChester(message);
        if (response == null) {
            chat.sendMessage("Something went wrong :(");
            return;
        }
        chat.sendMessage(response);
    }

    @Override
    public void onCommandMessageReceived(CommandMessageReceivedEvent event) {
        Chat chat = event.getChat();
        String chatId = chat.getId();
        String command = event.getCommand().toLowerCase();
        switch (command) {
            case "enable": {
                if (respondToAllMessages.contains(chatId)) {
                    chat.sendMessage("Already enabled.");
                    break;
                }
                respondToAllMessages.add(chatId);
                chat.sendMessage("Responding to all messages has been enabled!");
                break;
            }
            case "disable": {
                if (!respondToAllMessages.contains(chatId)) {
                    chat.sendMessage("Already disabled.");
                    break;
                }
                respondToAllMessages.remove(chatId);
                chat.sendMessage("Responding to all messages has been disabled!");
                break;
            }
            case "start": {
                chat.sendMessage("Talk to me!");
                break;
            }
            case "stopbot": {
                User user = event.getMessage().getSender();
                if (user.getId() != Chester.OWNER_ID) {
                    chat.sendMessage("No perms sozm");
                    break;
                }
                chat.sendMessage("okbyebye");
                chester.setRunning(false);
            }
        }
    }

    private String askChester(String query) {
        query = URLEncoder.encode(query);
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(Chester.REQUEST_URL + query).openConnection();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 ChesterTelegramBot/1.0 (https://github.com/Sponges/ChesterTelegramBot)");
        String response;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            response = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        JSONObject object = new JSONObject(response);
        if (!object.isNull("error")) {
            if (object.getBoolean("error")) {
                return null;
            }
        }
        return object.getString("response");
    }
}
