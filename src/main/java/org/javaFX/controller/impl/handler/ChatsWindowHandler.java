package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.encryfoundation.tg.javaIntegration.JavaInterMsg;
import org.javaFX.EncryWindow;
import org.javaFX.controller.MainWindowBasicHandler;
import org.javaFX.model.JChat;
import org.javaFX.model.nodes.VBoxChatCell;
import org.javaFX.model.nodes.VBoxDialogMessageCell;
import org.javaFX.model.nodes.VBoxMessageCell;
import org.javaFX.util.KeyboardHandler;

import java.util.concurrent.atomic.AtomicBoolean;

public class ChatsWindowHandler extends MainWindowBasicHandler {

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

    public ChatsWindowHandler(){
    }

    @Override
    public void updateEncryWindow(EncryWindow encryWindow) {
        super.setEncryWindow(encryWindow);
        initializeTable();
        enableMenuBar();
    }

    @FXML
    private ObservableList<VBoxChatCell> getObservableJChatList(){
        ObservableList<VBoxChatCell> observableChatList = FXCollections.observableArrayList();
        getUserStateRef().get().getChatList().forEach(
                chat -> observableChatList.add(new VBoxChatCell( new JChat(chat.title, "stub", chat.id)  ))
        );
        return observableChatList;
    }

    @FXML
    protected void initializeTable() {
        chatsListView.setItems(getObservableJChatList());
    }

    private void changeLeftPaneVisibility(){
        leftTopAnchorPane.setVisible(true);
        leftMiddleAnchorPane.setVisible(true);
        leftBottomAnchorPane.setVisible(true);
        selectChatLabel.setVisible(false);
        chatNameLabel.setText(chatsListView.getSelectionModel().getSelectedItem().getChatTitle());
    }

    /*@FXML
    protected void clickItem() {
        getUserStateRef().get().setActiveDialog(jDialog);
        getUserStateRef().get().setActiveDialogArea(dialogTextArea);
        JavaInterMsg msg = new JavaInterMsg.SetActiveChat(
                chatsListView.getSelectionModel().getSelectedItem().chatIdProperty().get()
        );
        changeLeftPaneVisibility();
        try {
            getUserStateRef().get().msgsQueue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }*/

    @FXML
    protected void clickItem() {
        this.messagesListView = getUserStateRef().get().messagesListView;

        /*getUserStateRef().get().setActiveDialog(jDialog);
        getUserStateRef().get().setActiveDialogArea(dialogTextArea);*/
        JavaInterMsg msg = new JavaInterMsg.SetActiveChat(
                chatsListView.getSelectionModel().getSelectedItem().chatIdProperty().get()
        );
        changeLeftPaneVisibility();
        try {
            getUserStateRef().get().msgsQueue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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


    @FXML
    private void findContentInDialog(){

    }
}
