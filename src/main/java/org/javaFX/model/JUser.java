package org.javaFX.model;

import java.util.Objects;

// simple stub entity
public class JUser {

    private String name;
    private int chatId;
    private String phoneNumber;

    public JUser(String name) {
        this.name = name;
    }

    public JUser(String name, int chatId, String phoneNumber) {
        this.name = name;
        this.chatId = chatId;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() { return phoneNumber; }

    public void setPhone(String phone) {
        this.phoneNumber = phone;
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
