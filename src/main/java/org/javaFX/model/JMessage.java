package org.javaFX.model;

public class JMessage<T> {

    private boolean isMine;
    private T content;
    private String time;

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
}
