package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.encryfoundation.tg.javaIntegration.BackMsg;
import org.encryfoundation.tg.javaIntegration.FrontMsg;
import org.drinkless.tdlib.TdApi;
import org.encryfoundation.tg.utils.MessagesUtils;
import org.javaFX.EncryWindow;
import org.javaFX.controller.MainWindowBasicHandler;
import org.javaFX.model.JChat;
import org.javaFX.model.nodes.VBoxChatCell;
import org.javaFX.model.nodes.VBoxMessageCell;
import org.javaFX.util.KeyboardHandler;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatsWindowHandler extends MainWindowBasicHandler {

    private final String pathToBlueButton = "file:images/sendMessageBlue.png";
    private final String pathToGreyButton ="file:images/sendMessage.png";

    @FXML
    private ListView<VBoxChatCell> chatsListView;

    @FXML
    private ListView<VBoxMessageCell> messagesListView;

    @FXML
    private Label selectChatLabel;

    @FXML
    private Label chatNameLabel;

    @FXML
    private AnchorPane leftPane;

    @FXML
    private AnchorPane rightTopAnchorPane;

    @FXML
    private AnchorPane rightMiddleAnchorPane;

    @FXML
    private AnchorPane rightBottomAnchorPane;

    @FXML
    private TextField searchThroughChatsTextField;

    @FXML
    private ImageView sendMessageImage;

    @FXML
    private TextField searchMessageTextField;

    @FXML
    private Label notFoundChatLabel;

    @FXML
    private ImageView searchImg;


    //todo: remove after updating chat by front msg
    private int chatsLimit = 20;
    private long activeChatId;

    public ChatsWindowHandler(){
    }

    @FXML
    private void onMouseEntered(){
        sendMessageImage.setImage(new Image(pathToBlueButton));
    }

    @FXML
    private void onMouseExited(){
        sendMessageImage.setImage(new Image(pathToGreyButton));
    }

    @Override
    public void updateEncryWindow(EncryWindow encryWindow) {
        super.setEncryWindow(encryWindow);
        initializeTable();
        if( messagesListView != null && messagesListView.getItems().size() != 0 ) {
            initializeDialogArea();
        }
        enableMenuBar();
        FrontMsg a = getUserStateRef().get().inQueue.poll();
        if (a != null) {
            if (a.code() == FrontMsg.Codes$.MODULE$.newMsgsInChat()) {
                FrontMsg.NewMsgsInChat msg = (FrontMsg.NewMsgsInChat) a;
                ObservableList<VBoxMessageCell> observableChatList = FXCollections.observableArrayList();
                msg.msgs().forEach(msgCell -> observableChatList.add(msgCell));
                messagesListView.scrollTo(1);
                messagesListView.setItems(observableChatList);
            } else if (a.code() == FrontMsg.Codes$.MODULE$.historyMsgs()) {
                FrontMsg.HistoryMsgs msg = (FrontMsg.HistoryMsgs) a;
                if (msg.chatId() == activeChatId) {
                    ObservableList<VBoxMessageCell> observableChatList = FXCollections.observableArrayList();
                    msg.msgs().forEach(msgCell -> observableChatList.add(msgCell));
                    VBoxMessageCell prevLastCell = messagesListView.getItems().get(0);
                    messagesListView.getItems().forEach(cell -> observableChatList.add(cell));
                    messagesListView.setItems(observableChatList);
                    messagesListView.scrollTo(prevLastCell);
                }
            } else {
                System.out.println("Unknown msg");
            }

        }
    }

    @FXML
    private ObservableList<VBoxChatCell> getObservableJChatList(){
        final String searchingStr = searchThroughChatsTextField.getText().trim();
        ObservableList<VBoxChatCell> observableChatList = initTableBySubstr(searchingStr);
        return observableChatList;
    }

    @FXML
    private void scrollChats(){
        ScrollBar bar = (ScrollBar) chatsListView.lookup(".scroll-bar");
        if (bar.getValue() == bar.getMax()) {
            BackMsg msg = new BackMsg.LoadNextChatsChunk(chatsListView.getItems().size());
            try {
                getUserStateRef().get().outQueue.put(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void scrollMsgs(){
        ScrollBar bar = (ScrollBar) messagesListView.lookup(".scroll-bar");
        if (bar.getValue() == bar.getMin()) {
            BackMsg msg = new BackMsg.LoadNextMsgsChunk(messagesListView.getItems().size());
            try {
                getUserStateRef().get().outQueue.put(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void initChatCell(ObservableList<VBoxChatCell> observableChatList, TdApi.Chat chat, double chatCellWidth){
        Optional<VBoxChatCell> prevCell =
                chatsListView.getItems().stream()
                        .filter(elem -> elem.getChatId() == chat.id)
                        .findAny();
        VBoxChatCell cell = prevCell.orElse(new VBoxChatCell(
                new JChat(
                        chat.title,
                        MessagesUtils.tdMsg2String(chat.lastMessage),
                        chat.id,
                        MessagesUtils.getLastMessageTime(chat.lastMessage),
                        new AtomicInteger(chat.unreadCount)
                ), chatCellWidth
        ));
        cell.updateLastMessage(
                MessagesUtils.tdMsg2String(chat.lastMessage),
                MessagesUtils.getLastMessageTime(chat.lastMessage),
                chat.unreadCount
        );
        cell.updateChatLabels(chatCellWidth);
        observableChatList.add(cell);
    }

    @FXML
    protected void initializeTable() {
        ObservableList<VBoxChatCell> newChats = getObservableJChatList();
        boolean scrollFlag = newChats.size() > chatsLimit;
        VBoxChatCell scrollPoint;
        //todo: refactor
        if (scrollFlag && chatsListView.getItems().size() != 0) {
            scrollPoint = chatsListView.getItems().get(chatsLimit - 1);
            chatsListView.setItems(newChats);
            chatsListView.scrollTo(scrollPoint);
            chatsLimit = newChats.size();
        } else {
            chatsListView.setItems(newChats);
        }
    }

    @FXML
    private ObservableList<VBoxMessageCell> getObservableJMessageList(){
        ObservableList<VBoxMessageCell> observableMessageList = FXCollections.observableArrayList();
        getUserStateRef().get().messagesListView.getItems().forEach (
                message ->
                        observableMessageList.add(message)
        );
        forceListRefreshOn();
        return observableMessageList;

        /*
        new way of initialization
        final String searchingStr = searchMessageTextField.getText().trim();
        ObservableList<VBoxMessageCell> observableMessageList = findMessagesByStr(searchingStr);
        return observableMessageList;*/
    }

    @FXML
    private void initializeDialogArea(){
        messagesListView.setItems(getObservableJMessageList());
    }

    private void showLeftPane(){
        rightTopAnchorPane.setVisible(true);
        rightMiddleAnchorPane.setVisible(true);
        messagesListView.setVisible(true);
        rightBottomAnchorPane.setVisible(true);
        selectChatLabel.setVisible(false);
        chatNameLabel.setText(chatsListView.getSelectionModel().getSelectedItem().getChatTitle());
    }

    public void hideLeftPane(){
        rightTopAnchorPane.setVisible(false);
        messagesListView.setVisible(false);
        rightBottomAnchorPane.setVisible(false);
        selectChatLabel.setVisible(true);
    }

    @FXML
    protected void clickItem() {
        getUserStateRef().get().setActiveDialog(messagesListView);
        if( messagesListView.getItems().size() == 0 ){
            showStartMessagingArea();
        }
        VBoxChatCell activeChat = chatsListView.getSelectionModel().getSelectedItem();
        refreshColors(activeChat);
        activeChatId = activeChat.chatIdProperty().get();
        BackMsg msg = new BackMsg.SetActiveChat(activeChatId);
        initializeDialogArea();
        ObservableList<VBoxMessageCell> observableMessageList = FXCollections.observableArrayList();
        messagesListView.setItems(observableMessageList);
        try {
            getUserStateRef().get().outQueue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        showLeftPane();
    }

    private void refreshColors(VBoxChatCell activeChat){
        for(VBoxChatCell cell: chatsListView.getItems()){
            cell.resetPaneColor();
        }
        activeChat.updatePaneColor();
    }

    private void forceListRefreshOn() {
        ObservableList<VBoxMessageCell> items = messagesListView.getItems();
        messagesListView.getItems().clear();
        messagesListView.setItems(items);
    }

    @FXML
    private void searchContactByKeyboard(){
        AtomicBoolean keysPressed = KeyboardHandler.handleEnterPressed(searchThroughChatsTextField);
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if ( keysPressed.get() ) {
                    findContact();
                }
            }
        }.start();
    }


    private void findContact(){
        final String searchingStr = searchThroughChatsTextField.getText().trim();
        chatsListView.setItems(initTableBySubstr(searchingStr));
    }

    private ObservableList<VBoxChatCell> initTableBySubstr(String searchingStr){
        ObservableList<VBoxChatCell> observableList = FXCollections.observableArrayList();
        final double chatCellWidth =
                (leftPane == null || leftPane.getWidth() == 0)
                        ? 320
                        : leftPane.getWidth();
        getUserStateRef().get().getChatList().stream()
                .filter(item -> item.title.toLowerCase().contains(searchingStr.toLowerCase()) ||
                        MessagesUtils.tdMsg2String(item.lastMessage).toLowerCase().contains(searchingStr.toLowerCase()))
                                .forEach(
                chat -> {
                    initChatCell(observableList, chat, chatCellWidth);
                }
        );
        notFoundChatLabel.setVisible((observableList.size() == 0 ));
        return observableList;
    }

    @FXML
    public void findContentInDialog(){
        final String searchingStr = searchMessageTextField.getText().trim();
     //   findMessagesByStr(searchingStr);
    }

    private ObservableList<VBoxMessageCell> findMessagesByStr(String searchingStr){
        ObservableList<org.javaFX.model.nodes.VBoxMessageCell> observableMessageList = FXCollections.observableArrayList();
        getUserStateRef().get().messagesListView.getItems()
                .filtered(message->message.getContentText().contains(searchingStr))
                .forEach (
                message ->
                        observableMessageList.add(message)
        );
        forceListRefreshOn();
        return observableMessageList;
    }

    @FXML
    public void searchMessageByKeyboard(){
        AtomicBoolean keysPressed = KeyboardHandler.handleEnterPressed(searchMessageTextField);
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if ( keysPressed.get() ) {
                    findContentInDialog();
                }
            }
        }.start();
    }

    private void showStartMessagingArea(){
        selectChatLabel.setText("There is no messages in this dialogue");
    }

    @FXML
    private void handleSearchImg(){
        searchImg.setVisible(false);
        searchThroughChatsTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue) {
                if(searchThroughChatsTextField.getText().length() == 0){
                    searchImg.setVisible(true);
                }
            }
        });
    }

}