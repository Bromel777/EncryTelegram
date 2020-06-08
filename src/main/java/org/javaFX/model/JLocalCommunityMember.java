package org.javaFX.model;

import javafx.beans.property.*;

import java.util.concurrent.atomic.AtomicInteger;

public class JLocalCommunityMember {

    private static AtomicInteger totalNumber = new AtomicInteger(1);

    private AtomicInteger tableNumber;
    private StringProperty title;
    private StringProperty lastMessage;
    private LongProperty chatId;

    public JLocalCommunityMember(StringProperty title, StringProperty lastMessage, LongProperty chatId) {
        this.title = title;
        this.lastMessage = lastMessage;
        this.chatId = chatId;
        tableNumber = new AtomicInteger( totalNumber.getAndIncrement() );
    }

    public JLocalCommunityMember(String titleStr, String lastMessageStr, Long chatId) {
        this(new SimpleStringProperty(titleStr), new SimpleStringProperty(lastMessageStr), new SimpleLongProperty(chatId));
    }

    public StringProperty getTitle() {
        return title;
    }

    public void setTitle(StringProperty title) {
        this.title = title;
    }

    public StringProperty getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(StringProperty lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LongProperty chatIdProperty() {
        return chatId;
    }

    public AtomicInteger getThisNumber() {
        return tableNumber;
    }

    public long getChatId() {
        return chatId.get();
    }

    @Override
    public String toString() {
        return "title = " + title.toString() + ", lastMessage = " + lastMessage.toString() + ", chatId = " + chatId.toString() ;
    }
}
