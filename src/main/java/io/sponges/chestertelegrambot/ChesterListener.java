package io.sponges.chestertelegrambot;

import org.json.JSONObject;
import pro.zackpollard.telegrambot.api.chat.ChatType;
import pro.zackpollard.telegrambot.api.chat.message.content.ContentType;
import pro.zackpollard.telegrambot.api.chat.message.content.TextContent;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.message.TextMessageReceivedEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ChesterListener implements Listener {

    @Override
    public void onTextMessageReceived(TextMessageReceivedEvent event) {
        if (event.getContent().getType() != ContentType.TEXT) return;
        TextContent content = event.getContent();
        String message = content.getContent();
        ChatType type = event.getChat().getType();
        if (type == ChatType.GROUP || type == ChatType.SUPERGROUP) {
            if (!message.startsWith(Chester.BOT_USERNAME)) return;
            message = message.substring(message.indexOf("@chester") + 16);
        }
        System.out.println("got msg " + message);
        String response = askChester(message);
        if (response == null) {
            event.getChat().sendMessage("Something went wrong :'(");
        }
        System.out.println("got response " + response);
        event.getChat().sendMessage(response);
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
