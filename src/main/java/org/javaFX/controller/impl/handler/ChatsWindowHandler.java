package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.encryfoundation.tg.javaIntegration.JavaInterMsg;
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

    private final String pathToBlueButton = "@../images/sendMessageBlue.png";
    private final String pathToGreyButton ="@../images/sendMessage.png";

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
        if( messagesListView != null && messagesListView.getItems().size() != 0){
            initializeDialogArea();
        }
        enableMenuBar();
    }

    @FXML
    private ObservableList<VBoxChatCell> getObservableJChatList(){
        ObservableList<VBoxChatCell> observableChatList = FXCollections.observableArrayList();
        final double chatCellWidth =
                (leftPane == null)
                        ? 300
                        : leftPane.getPrefWidth();
        getUserStateRef().get().getChatList().forEach(
                chat -> {
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
                    observableChatList.add(cell);
                }
        );
        return observableChatList;
    }

    @FXML
    protected void initializeTable() {
        chatsListView.setItems(getObservableJChatList());
    }

    @FXML
    private ObservableList<VBoxMessageCell>getObservableJMessageList(){
        ObservableList<VBoxMessageCell> observableMessageList = FXCollections.observableArrayList();
        getUserStateRef().get().messagesListView.getItems().forEach (
                message -> observableMessageList.add(message)
        );
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
        JavaInterMsg msg = new JavaInterMsg.SetActiveChat(
                chatsListView.getSelectionModel().getSelectedItem().chatIdProperty().get()
        );
        initializeDialogArea();
        try {
            getUserStateRef().get().msgsQueue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        showLeftPane();
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
                .filter(item -> item.getChatTitle().toLowerCase().contains(searchingStr.toLowerCase()) )
                .findAny()
                .ifPresent(item -> {
                    chatsListView.getSelectionModel().select(item);
                    chatsListView.scrollTo(item);
                });
    }

    private void flushDialogArea(){
        messagesListView = new ListView<>();
        getUserStateRef().get().setActiveDialog(messagesListView);
    }

    @FXML
    private void findContentInDialog(){
    }

}