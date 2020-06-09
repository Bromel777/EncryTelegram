package org.javaFX.model;

import javafx.scene.control.TextArea;
import org.drinkless.tdlib.TdApi;
import org.encryfoundation.tg.javaIntegration.JavaInterMsg;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class JUserState {
    private List<TdApi.Chat> chatList = new ArrayList<>();
    private Map<Long, TdApi.Chat> chatIds = new HashMap<>();
    private Map<Long, JGroupChat> privateGroups = new HashMap<>();
    private Map<Integer, TdApi.User> users =  new HashMap<>();
    public AtomicReferenceArray<String> userInfo = new AtomicReferenceArray(3);
    private Map<Integer, TdApi.BasicGroup> basicGroups = new HashMap<>();
    private Map<Integer, TdApi.Supergroup> superGroups = new HashMap<>();
    private Map<Integer, TdApi.SecretChat> secretChats = new HashMap<>();
    public LinkedBlockingQueue<JavaInterMsg> msgsQueue = new LinkedBlockingQueue<JavaInterMsg>(100);
    public JDialog activeDialog;
    public TextArea activeDialogArea;
    private boolean isAuth = false;

    /*
        TODO: implement the client
        client: Client[F]
    */

    public JUserState() {
    }

    public void setActiveDialog(JDialog activeDialog) { this.activeDialog = activeDialog; }

    public void setActiveDialogArea(TextArea activeDialogArea) { this.activeDialogArea = activeDialogArea; }

    public List<TdApi.Chat> getChatList() {
        return chatList;
    }

    public void setChatList(List<TdApi.Chat> chatList) {
        this.chatList = chatList;
    }

    public void setPhoneNumber(String number) { this.userInfo.set(0, number); }

    public void setCode(String code) { this.userInfo.set(1, code); }

    public void setPass(String pass) { this.userInfo.set(2, pass); }

    public Map<Long, TdApi.Chat> getChatIds() {
        return chatIds;
    }

    public void setChatIds(Map<Long, TdApi.Chat> chatIds) {
        this.chatIds = chatIds;
    }

    public Map<Long, JGroupChat> getPrivateGroups() {
        return privateGroups;
    }

    public void setPrivateGroups(Map<Long, JGroupChat> privateGroups) {
        this.privateGroups = privateGroups;
    }

    public Map<Integer, TdApi.User> getUsers() {
        return users;
    }

    public void setUsers(Map<Integer, TdApi.User> users) {
        this.users = users;
    }

    public Map<Integer, TdApi.BasicGroup> getBasicGroups() {
        return basicGroups;
    }

    public void setBasicGroups(Map<Integer, TdApi.BasicGroup> basicGroups) {
        this.basicGroups = basicGroups;
    }

    public Map<Integer, TdApi.Supergroup> getSuperGroups() {
        return superGroups;
    }

    public void setSuperGroups(Map<Integer, TdApi.Supergroup> superGroups) {
        this.superGroups = superGroups;
    }

    public Map<Integer, TdApi.SecretChat> getSecretChats() {
        return secretChats;
    }

    public void setSecretChats(Map<Integer, TdApi.SecretChat> secretChats) {
        this.secretChats = secretChats;
    }

    public boolean isAuth() {
        return isAuth;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }


}
