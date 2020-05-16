package javaFX.model;

import org.drinkless.tdlib.TdApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JUserState {
    private List<TdApi.Chat> chatList = new ArrayList<>();
    private Map<Long, TdApi.Chat> chatIds = new HashMap<>();
    private Map<Long, JGroupChat> privateGroups = new HashMap<>();
    private Map<Integer, TdApi.User> users =  new HashMap<>();

    private Map<Integer, TdApi.BasicGroup> basicGroups =  new HashMap<>();
    private Map<Integer, TdApi.Supergroup> superGroups =  new HashMap<>();
    private Map<Integer, TdApi.SecretChat> secretChats =  new HashMap<>();
    private boolean  isAuth = false;

    /*
        TODO: implement the client
        client: Client[F]
    */

    public JUserState() {
    }

    public List<TdApi.Chat> getChatList() {
        return chatList;
    }

    public void setChatList(List<TdApi.Chat> chatList) {
        this.chatList = chatList;
    }

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
