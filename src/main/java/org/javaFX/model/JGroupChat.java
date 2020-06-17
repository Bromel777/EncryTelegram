package org.javaFX.model;

import org.drinkless.tdlib.TdApi;

import java.util.Objects;

public class JGroupChat {
    private TdApi.Chat chat;
    private String title;

    public JGroupChat(TdApi.Chat chat, String title) {
        this.chat = chat;
        this.title = title;
    }

    public TdApi.Chat getChat() {
        return chat;
    }

    public void setChat(TdApi.Chat chat) {
        this.chat = chat;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JGroupChat that = (JGroupChat) o;
        return chat.equals(that.chat) &&
                title.equals(that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chat, title);
    }

    @Override
    public String toString() {
        return "JGroupChat{" +
                "chat=" + chat +
                ", title='" + title + '\'' +
                '}';
    }
}
