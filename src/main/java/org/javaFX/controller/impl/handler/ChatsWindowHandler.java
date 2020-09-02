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
import org.javaFX.model.nodes.VBoxDialogTextMessageCell;
import org.javaFX.model.nodes.VBoxMessageCell;
import org.javaFX.util.KeyboardHandler;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatsWindowHandler extends MainWindowBasicHandler {

    private final String pathToBlueButton = "images/sendMessageBlue.png";
    private final String pathToGreyButton ="images/sendMessage.png";

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

    @FXML
    private ImageView crossImg;

    //todo: remove after updating chat by front msg
    private int chatsLimit = 20;
    private long activeChatId;

    private ObservableList<VBoxMessageCell> chatHistoryBackup;

    public ChatsWindowHandler(){}

    @FXML
    private void showCrossImg(){
        crossImg.setVisible(true);
    }

    @FXML
    private void hideCrossImg(){
        crossImg.setVisible(false);
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
        updateDialogArea();
    }

    private void updateDialogArea(){
        FrontMsg frontMsg = getUserStateRef().get().inQueue.poll();
        if (frontMsg != null ) {
            if (frontMsg.code() == FrontMsg.Codes$.MODULE$.newMsgsInChat()) {
                FrontMsg.NewMsgsInChat msg = (FrontMsg.NewMsgsInChat) frontMsg;
                ObservableList<VBoxMessageCell> observableChatList = FXCollections.observableArrayList();
                msg.msgs().forEach(msgCell -> observableChatList.add(msgCell));
                msg.msgs().forEach(msgCell -> chatHistoryBackup.add(msgCell));
                messagesListView.scrollTo(1);
                messagesListView.setItems(observableChatList);
            } else if (frontMsg.code() == FrontMsg.Codes$.MODULE$.historyMsgs()) {
                FrontMsg.HistoryMsgs msg = (FrontMsg.HistoryMsgs) frontMsg;
                if (msg.chatId() == activeChatId && messagesListView.getItems().size() > 0) {
                    ObservableList<VBoxMessageCell> observableChatList = FXCollections.observableArrayList();
                    msg.msgs().forEach(msgCell -> observableChatList.add(msgCell));
                    msg.msgs().forEach(msgCell -> chatHistoryBackup.add(msgCell));
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
        String previousMsgAuthor = "";
        for(VBoxMessageCell messageCell: getUserStateRef().get().messagesListView.getItems() ){
            String currentMsgAuthor = messageCell.getElement().getAuthor();
            messageCell.getElement().setPreviousSameAuthor(currentMsgAuthor.equals(previousMsgAuthor));
            if( currentMsgAuthor.equals(previousMsgAuthor) ){
                ((VBoxDialogTextMessageCell) messageCell).recreateMessageCell();
            }
            previousMsgAuthor = currentMsgAuthor;
            observableMessageList.add(messageCell);
        }

        messagesListView.setItems(observableMessageList);
        return observableMessageList;
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
        String chatTitleStr = chatsListView.getSelectionModel().getSelectedItem().getChatTitle();
        if(chatTitleStr.length() > 30 ){
            chatTitleStr = chatTitleStr.substring(0, 27)+"...";
        }
        chatNameLabel.setText( chatTitleStr );
        searchMessageTextField.setText("");
    }

    public void hideLeftPane(){
        rightTopAnchorPane.setVisible(false);
        messagesListView.setVisible(false);
        rightBottomAnchorPane.setVisible(false);
        selectChatLabel.setVisible(true);
    }

    @FXML
    protected void clickItem() {
        chatHistoryBackup = FXCollections.observableArrayList();
        getUserStateRef().get().setActiveDialog(messagesListView);
        if( messagesListView.getItems().size() == 0 ){
            showStartMessagingArea();
        }
        VBoxChatCell activeChat = chatsListView.getSelectionModel().getSelectedItem();
        refreshColors(activeChat);
        activeChatId = activeChat.chatIdProperty().get();
        BackMsg msg = new BackMsg.SetActiveChat(activeChatId);
        initializeDialogArea();
        sendMessageTextArea.setText("");
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
        messagesListView.setItems( getMessagesByStr(searchingStr) );
    }

    private ObservableList<VBoxMessageCell> getMessagesByStr(String searchingStr){
        ObservableList<VBoxMessageCell> observableMessageList = FXCollections.observableArrayList();
        chatHistoryBackup.filtered(message->message.getContentText().toLowerCase().contains(searchingStr.toLowerCase()))
                .forEach (
                message ->
                        observableMessageList.add(message)
        );
        messagesListView.setItems(observableMessageList);
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

    @FXML
    private void cleanTextField(){
        searchMessageTextField.setText("");
        messagesListView.setItems( getMessagesByStr("") );
    }
}