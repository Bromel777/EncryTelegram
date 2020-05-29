package org.javaFX.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class JChat {
    private StringProperty title;
    private StringProperty lastMessage;

    public JChat(StringProperty title, StringProperty lastMessage) {
        this.title = title;
        this.lastMessage = lastMessage;
    }

    public JChat(String titleStr) {
        this.title = new SimpleStringProperty(titleStr);
        this.lastMessage = new SimpleStringProperty("test");
    }

    public JChat(StringProperty title) {
        this.title = title;
        this.lastMessage = new SimpleStringProperty("test");
    }

    public JChat(String titleStr, String lastMessageStr) {
        this(new SimpleStringProperty(titleStr), new SimpleStringProperty(lastMessageStr));
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

    @Override
    public String toString() {
        return "title = " + title.toString() + ", lastMessage = " + lastMessage.toString() ;
    }
}
