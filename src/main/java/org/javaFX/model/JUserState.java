package org.javaFX.model;

import javafx.scene.control.ListView;
import org.drinkless.tdlib.TdApi;
import org.encryfoundation.tg.javaIntegration.FrontMsg;
import org.encryfoundation.tg.javaIntegration.BackMsg;
import org.javaFX.model.nodes.VBoxMessageCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class JUserState {
    private List<TdApi.Chat> chatList = new ArrayList<>();
    private Map<Long, TdApi.Chat> chatsMap = new HashMap<>();
    private Map<Long, JGroupChat> privateGroupsMap = new HashMap<>();
    private Map<Long, TdApi.User> usersMap =  new HashMap<>();
    public AtomicReferenceArray<String> userInfo = new AtomicReferenceArray(3);
    private Map<Long, TdApi.BasicGroup> basicGroups = new HashMap<>();
    private Map<Long, TdApi.Supergroup> superGroups = new HashMap<>();
    private Map<Long, TdApi.SecretChat> secretChats = new HashMap<>();
    public List<JLocalCommunity> communities = new ArrayList<JLocalCommunity>();
    //processed by back
    public LinkedBlockingQueue<BackMsg> outQueue = new LinkedBlockingQueue<BackMsg>(100);
    //processed by front
    public LinkedBlockingQueue<FrontMsg> inQueue = new LinkedBlockingQueue<FrontMsg>(100);
    private boolean isAuth = false;
    public ListView<VBoxMessageCell> messagesListView;

    public JUserState() {
    }

    public void setActiveDialog(ListView<VBoxMessageCell> messagesListView) { this.messagesListView = messagesListView; }

    public List<TdApi.Chat> getChatList() {
        return chatList;
    }

    public void setChatList(List<TdApi.Chat> chatList) {
        this.chatList = chatList;
    }

    public void removeChatById(long chatId) {
        this.chatList.removeIf(chat -> chat.id == chatId);
    }

    public void setPhoneNumber(String number) { this.userInfo.set(0, number); }

    public String getPhoneNumber() { return this.userInfo.get(0); }

    public String getPreparedPhoneNumber(){
        String phoneNumber = this.userInfo.get(0);
        StringBuilder sb = new StringBuilder("+");
        switch (phoneNumber.substring(0,1)){
            case "7":
                sb.append("7 ");
                sb.append(phoneNumber.substring(1,4)+" ");
                sb.append(phoneNumber.substring(4,7)+" ");
                sb.append(phoneNumber.substring(7,9)+" ");
                sb.append(phoneNumber.substring(9,11)+" ");
                break;
            case "3":
                sb.append("375 ");
                sb.append(phoneNumber.substring(3,5)+" ");
                sb.append(phoneNumber.substring(5,8)+" ");
                sb.append(phoneNumber.substring(8,10)+" ");
                sb.append(phoneNumber.substring(10,12)+" ");
                break;
        }
        return sb.toString();
    }

    public void setCode(String code) { this.userInfo.set(1, code); }

    public void setPass(String pass) { this.userInfo.set(2, pass); }

    public Map<Long, TdApi.Chat> getChatsMap() {
        return chatsMap;
    }

    public void setChatsMap(Map<Long, TdApi.Chat> chatsMap) {
        this.chatsMap = chatsMap;
    }

    public Map<Long, JGroupChat> getPrivateGroupsMap() {
        return privateGroupsMap;
    }

    public void setPrivateGroupsMap(Map<Long, JGroupChat> privateGroupsMap) {
        this.privateGroupsMap = privateGroupsMap;
    }

    public Map<Long, TdApi.User> getUsersMap() {
        return usersMap;
    }

    public void setUsersMap(Map<Long, TdApi.User> usersMap) {
        this.usersMap = usersMap;
    }

    public Map<Long, TdApi.BasicGroup> getBasicGroups() {
        return basicGroups;
    }

    public void setBasicGroups(Map<Long, TdApi.BasicGroup> basicGroups) {
        this.basicGroups = basicGroups;
    }

    public Map<Long, TdApi.Supergroup> getSuperGroups() {
        return superGroups;
    }

    public void setSuperGroups(Map<Long, TdApi.Supergroup> superGroups) {
        this.superGroups = superGroups;
    }

    public Map<Long, TdApi.SecretChat> getSecretChats() {
        return secretChats;
    }

    public void setSecretChats(Map<Long, TdApi.SecretChat> secretChats) {
        this.secretChats = secretChats;
    }

    public boolean isAuth() {
        return isAuth;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }


}
