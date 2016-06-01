package io.sponges.chestertelegrambot;

import org.json.JSONObject;
import pro.zackpollard.telegrambot.api.chat.Chat;
import pro.zackpollard.telegrambot.api.chat.ChatType;
import pro.zackpollard.telegrambot.api.chat.GroupChat;
import pro.zackpollard.telegrambot.api.chat.SuperGroupChat;
import pro.zackpollard.telegrambot.api.chat.message.content.ContentType;
import pro.zackpollard.telegrambot.api.chat.message.content.TextContent;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.message.CommandMessageReceivedEvent;
import pro.zackpollard.telegrambot.api.event.chat.message.TextMessageReceivedEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class ChesterListener implements Listener {
    private Map<Chat, Boolean> respondToAllMessages = new HashMap<>();

    @Override
    public void onTextMessageReceived(TextMessageReceivedEvent event) {
        String message = event.getContent().getContent();
        Chat chat = event.getChat();
        if (message.startsWith(Chester.BOT_USERNAME)) {
            message = message.replace(Chester.BOT_USERNAME, "");
        } else if (chat instanceof GroupChat && !respondToAllMessages.get(chat)) { //SuperGroupChat extends GroupChat
            return;
        }

        String response = this.askChester(message);
        if (response == null) {
            event.getChat().sendMessage("Something went wrong :(");
        }
        event.getChat().sendMessage(response);
    }

    //HOW NOT TO COMMAND HANDLER
    @Override
    public void onCommandMessageReceived(CommandMessageReceivedEvent event) {
        if (event.getCommand().equalsIgnoreCase("enable")) {
            respondToAllMessages.put(event.getChat(), true);
            event.getChat().sendMessage("Responding to all messages has been enabled!");
        } else if (event.getCommand().equalsIgnoreCase("disable")) {
            respondToAllMessages.put(event.getChat(), false);
            event.getChat().sendMessage("Responding to all messages has been disabled!");
        } else if (event.getCommand().equalsIgnoreCase("start")) {
            event.getChat().sendMessage("Talk to me!");
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
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 SpongyBot");
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
