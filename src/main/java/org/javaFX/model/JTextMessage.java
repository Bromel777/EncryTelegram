package org.javaFX.model;

public class JTextMessage {

    private boolean isMine;
    private String content;
    private String time;

    public JTextMessage(boolean isMine, String content, String time) {
        this.isMine = isMine;
        this.content = content;
        this.time = time;
    }

    public boolean isMine() {
        return isMine;
    }

    public String getContent() {
        return content;
    }

    public String getTime() {
        return time;
    }
}
