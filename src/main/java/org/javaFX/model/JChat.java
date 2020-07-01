package org.javaFX.model;

import javafx.beans.property.*;
import org.drinkless.tdlib.TdApi;


import java.util.concurrent.atomic.AtomicInteger;

public class JChat {

    private StringProperty title;
    private StringProperty lastMessage;
    private LongProperty lastMessageTime;
    private final LongProperty chatId;
    private AtomicInteger unreadMessagesNumber;
    private TdApi.File smallPicture;


    public JChat(StringProperty title, StringProperty lastMessage, LongProperty chatId, LongProperty lastMessageTime) {
        this.title = title;
        this.lastMessage = lastMessage;
        this.chatId = chatId;
        this.lastMessageTime = lastMessageTime;
        unreadMessagesNumber = new AtomicInteger(0);
    }

    public JChat(String titleStr, String lastMessageStr, Long chatId, Long lastMessageTime) {
        this(new SimpleStringProperty(titleStr),
             new SimpleStringProperty(lastMessageStr),
             new SimpleLongProperty(chatId),
             new SimpleLongProperty(lastMessageTime));
        unreadMessagesNumber = new AtomicInteger(0);
    }

    public JChat(StringProperty title, StringProperty lastMessage, LongProperty chatId, LongProperty lastMessageTime, AtomicInteger unreadMessagesNumber ) {
        this.title = title;
        this.lastMessage = lastMessage;
        this.chatId = chatId;
        this.lastMessageTime = lastMessageTime;
        this.unreadMessagesNumber = unreadMessagesNumber;
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

    public LongProperty getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LongProperty lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public LongProperty chatIdProperty() {
        return chatId;
    }

    public AtomicInteger getUnreadMessagesNumber() {
        return unreadMessagesNumber;
    }

    public TdApi.File getSmallPicture() {
        return smallPicture;
    }

    public void setSmallPicture(TdApi.File smallPicture) {
        this.smallPicture = smallPicture;
    }

    @Override
    public String toString() {
        return "title = " + title.toString() + ", lastMessage = " + lastMessage.toString() + ", chatId = " + chatId.toString() ;
    }
}
