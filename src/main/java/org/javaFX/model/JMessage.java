package org.javaFX.model;

public class JMessage<T> {

    private boolean isMine;
    private T content;
    private String time;
    private String author;
    private boolean isPreviousSameAuthor;

    public JMessage(boolean isMine, T content, String time) {
        this.isMine = isMine;
        this.content = content;
        this.time = time;
        if(isMine){
            author = "You:";
        }
        else{
            author = "Your interlocutor:";
        }
    }

    //TODO please, use this constructor to create JMessage objects in your code if you want to see author properly
    // when we know the author of last message we can type the text in a more beautiful way
    public JMessage(boolean isMine, T content, String time, String author, boolean isPreviousSameAuthor) {
        this.isMine = isMine;
        this.content = content;
        this.time = time;
        this.author = author;
        this.isPreviousSameAuthor = isPreviousSameAuthor;
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

    public boolean isPreviousSameAuthor() {
        return isPreviousSameAuthor;
    }

    public void setPreviousSameAuthor(boolean previousSameAuthor) {
        isPreviousSameAuthor = previousSameAuthor;
    }
}
