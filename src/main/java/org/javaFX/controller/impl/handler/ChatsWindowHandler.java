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
import org.encryfoundation.tg.javaIntegration.JavaInterMsg;
import org.encryfoundation.tg.utils.MessagesUtils;
import org.javaFX.EncryWindow;
import org.javaFX.controller.MainWindowBasicHandler;
import org.javaFX.model.JChat;
import org.javaFX.model.nodes.VBoxChatCell;
import org.javaFX.model.nodes.VBoxMessageCell;
import org.javaFX.util.KeyboardHandler;

import java.util.concurrent.atomic.AtomicBoolean;

public class ChatsWindowHandler extends MainWindowBasicHandler {

    private final String pathToBlueButton = "file:src/main/resources/images/sendMessageBlue.png";
    private final String pathToGreyButton ="file:src/main/resources/images/sendMessage.png";

    @FXML
    private ListView<VBoxChatCell> chatsListView;

    @FXML
    private ListView<VBoxMessageCell> messagesListView;

    @FXML
    private Label selectChatLabel;

    @FXML
    private Label chatNameLabel;

    @FXML
    private AnchorPane leftTopAnchorPane;

    @FXML
    private AnchorPane leftMiddleAnchorPane;

    @FXML
    private AnchorPane leftBottomAnchorPane;

    @FXML
    private TextField searchThroughChatsTextField;

    @FXML
    private ImageView sendMessageImage;

    public ChatsWindowHandler(){
    }

    @FXML
    private void onMouseEntered(){
        sendMessageImage = new ImageView(new Image(pathToBlueButton));
    }

    @FXML
    private void onMouseExited(){
        sendMessageImage = new ImageView(new Image(pathToGreyButton));
    }

    @Override
    public void updateEncryWindow(EncryWindow encryWindow) {
        super.setEncryWindow(encryWindow);
        initializeTable();
        initializeDialogArea();
        enableMenuBar();
    }

    @FXML
    private ObservableList<VBoxChatCell> getObservableJChatList(){
        ObservableList<VBoxChatCell> observableChatList = FXCollections.observableArrayList();
        getUserStateRef().get().getChatList().forEach(
                chat -> {
                    observableChatList.add(new VBoxChatCell(
                            new JChat(
                                    chat.title,
                                    MessagesUtils.processMessage(chat.lastMessage),
                                    chat.id,
                                    MessagesUtils.getLastMessageTime(chat.lastMessage)
                            )
                    ));
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

    private void changeLeftPaneVisibility(){
        leftTopAnchorPane.setVisible(true);
        leftMiddleAnchorPane.setVisible(true);
        leftBottomAnchorPane.setVisible(true);
        selectChatLabel.setVisible(false);
        chatNameLabel.setText(chatsListView.getSelectionModel().getSelectedItem().getChatTitle());
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
        changeLeftPaneVisibility();
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