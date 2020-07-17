package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
                messagesListView.setItems(observableChatList);
            } else {
                System.out.println("Unknown msg");
            }
        }
    }

    @FXML
    private ObservableList<VBoxChatCell> getObservableJChatList(){
        ObservableList<VBoxChatCell> observableChatList = FXCollections.observableArrayList();
        final double chatCellWidth =
                (leftPane == null || leftPane.getWidth() == 0)
                        ? 320
                        : leftPane.getWidth();
        getUserStateRef().get().getChatList().forEach(
                chat -> {
                    initChatCell(observableChatList, chat, chatCellWidth);
                }
        );
        return observableChatList;
    }

    private void initChatCell(ObservableList<VBoxChatCell> observableChatList, TdApi.Chat chat, double chatCellWidth){
        Optional<VBoxChatCell> prevCell =
                chatsListView.getItems().stream()
                        .filter(elem -> elem.getChatId() == chat.id)
                        .findAny();
        VBoxChatCell cell = prevCell.orElse(new VBoxChatCell(
                new JChat(
                        chat.title,
                        MessagesUtils.processMessage(chat.lastMessage),
                        chat.id,
                        MessagesUtils.getLastMessageTime(chat.lastMessage),
                        new AtomicInteger(chat.unreadCount)
                ), chatCellWidth
        ));
        cell.updateLastMessage(
                MessagesUtils.processMessage(chat.lastMessage),
                MessagesUtils.getLastMessageTime(chat.lastMessage),
                chat.unreadCount
        );
        cell.updateChatLabels(chatCellWidth);
        observableChatList.add(cell);
    }

    @FXML
    protected void initializeTable() {
        chatsListView.setItems(getObservableJChatList());
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
        BackMsg msg = new BackMsg.SetActiveChat(
                chatsListView.getSelectionModel().getSelectedItem().chatIdProperty().get()
        );
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
                    findContentInChatsTable();
                }
            }
        }.start();
    }

    private void findContentInChatsTable(){
        final String searchingStr = searchThroughChatsTextField.getText().trim();
        chatsListView.getItems().stream()
                .filter(item -> item.getChatTitle().toLowerCase().contains(searchingStr.toLowerCase()) ||
                        item.getLastMessage().toLowerCase().contains(searchingStr.toLowerCase()))
                .findAny()
                .ifPresent(item -> {
                    chatsListView.getSelectionModel().select(item);
                    chatsListView.scrollTo(item);
                });
    }

    @FXML
    private void findContentInDialog(){
        final String searchingStr = searchMessageTextField.getText().trim();
        messagesListView.getItems().stream()
                .filter(item -> item.getContentText().toLowerCase().contains(searchingStr.toLowerCase()) )
                .findAny()
                .ifPresent(item -> {
                    messagesListView.getSelectionModel().select(item);
                    messagesListView.scrollTo(item);
                });
    }

    @FXML
    private void searchMessageByKeyboard(){
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



}