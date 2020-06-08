package org.javaFX.model;

import java.util.Objects;

// simple stub entity
public class JUser {

    private String name;
    private int chatId;

    public JUser(String name) {
        this.name = name;
    }

    public JUser(String name, int chatId) {
        this.name = name;
        this.chatId = chatId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JUser jUser = (JUser) o;
        return chatId == jUser.chatId &&
                Objects.equals(name, jUser.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, chatId);
    }

    @Override
    public String toString() {
        return "JUser{" +
                "name='" + name + '\'' +
                ", chatId=" + chatId +
                '}';
    }
}
