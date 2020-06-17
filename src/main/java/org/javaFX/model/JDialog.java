package org.javaFX.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JDialog {
    private List<JUser> userList;
    private String title;
    private StringBuffer content;

    public JDialog(String title) {
        this.title = title;
        userList = new ArrayList<>();
        content = new StringBuffer();
    }

    public JDialog(List<JUser> userList, String title, StringBuffer content) {
        this.userList = userList;
        this.title = title;
        this.content = content;
    }

    public List<JUser> getUserList() {
        return userList;
    }

    public void setUserList(List<JUser> userList) {
        this.userList = userList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public StringBuffer getContent() {
        return content;
    }

    public void setContent(StringBuffer content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JDialog jDialog = (JDialog) o;
        return Objects.equals(userList, jDialog.userList) &&
                Objects.equals(title, jDialog.title) &&
                Objects.equals(content, jDialog.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userList, title, content);
    }

    @Override
    public String toString() {
        return "JDialog " +
                "title=" + title + '\'' +
                ", content=" + content.toString();
    }
}
