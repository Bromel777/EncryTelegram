package org.javaFX.model;

public class JMessage<T> {

    private boolean isMine;
    private T content;
    private String time;
    private String author;

    public JMessage(boolean isMine, T content, String time) {
        this.isMine = isMine;
        this.content = content;
        this.time = time;
    }

    public boolean isMine() {
        return isMine;
    }

    public T getContent() {
        return content;
    }

    public String getTime() {
        return time;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
