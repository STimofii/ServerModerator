package com.bulka.java.net.tg.bots.servermoder;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;

public abstract class Command {
    private String name = "";
    public abstract void execute(Update update, String arguments);

    public Command() {
    }

    public Command(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
