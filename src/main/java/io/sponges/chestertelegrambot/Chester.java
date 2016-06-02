package io.sponges.chestertelegrambot;

import pro.zackpollard.telegrambot.api.TelegramBot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Chester {

    public static final String BOT_USERNAME = "@chesterchatbot";
    public static final String REQUEST_URL = "http://chester.chat/api?q=";
    public static final long OWNER_ID = 118746988;

    private final TelegramBot bot;
    private boolean running = true;

    private Chester() {
        this.bot = TelegramBot.login(getToken(new File("token.txt")));
        if (this.bot == null) System.exit(-1);
        this.bot.getEventsManager().register(new ChesterListener(this));
        this.bot.startUpdates(false);
        while (running) {
            try {
                Thread.sleep(50); // decrease cpu usage
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new Chester();
    }

    private String getToken(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
