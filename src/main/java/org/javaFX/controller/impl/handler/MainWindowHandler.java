package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

import org.encryfoundation.tg.javaIntegration.JavaInterMsg;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;
import org.javaFX.model.JChat;
import org.javaFX.model.JDialog;
import org.javaFX.util.KeyboardHandler;
import org.javaFX.util.observers.JTableObserver;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainWindowHandler extends DataHandler {

    @FXML
    private TableView<JChat> chatsTable;

    @FXML
    private TableColumn<JChat, String> chatsColumn;

    @FXML
    private TableColumn<JChat, String> lastMsgColumn;

    @FXML
    private TextArea searchMessageArea;

    @FXML
    private TextArea sendMessageArea;

    @FXML
    private TextArea dialogArea;

    @FXML
    private Button callButton;


    private JDialog jDialog;

    public MainWindowHandler() {
        chatListObserve(this);
        createDialog();
    }

    private void enableMenuBar(){
        if(getRootLayout().getTop().isDisable()) {
            getRootLayout().getTop().setDisable(false);
        }
    }

    private void chatListObserve(DataHandler controller){
        setObserver(new JTableObserver(controller));
        getObserver().setPeriod(Duration.seconds(3));
        getObserver().start();
    }


    @Override
    public void updateEncryWindow(EncryWindow encryWindow) {
        super.setEncryWindow(encryWindow);
        chatsTable.setItems(getObservableChatList());
        initializeTable();
        enableMenuBar();
    }

    @FXML
    private ObservableList<JChat> getObservableChatList(){
        ObservableList<JChat> observableChatList = FXCollections.observableArrayList();
        getUserStateRef().get().getChatList().forEach(
                chat -> observableChatList.add(new JChat(chat.title, "stub", chat.id) )
        );
        return observableChatList;
    }

    @FXML
    private void initializeTable() {
        chatsColumn.setCellValueFactory(cellData -> cellData.getValue().getTitle());
        lastMsgColumn.setCellValueFactory(cellData -> cellData.getValue().getLastMessage());
    }

    @FXML
    private void sendMessage() throws InterruptedException {
        String messageStr = sendMessageArea.getText().trim();
        if(!messageStr.isEmpty()) {
            JavaInterMsg msg = new JavaInterMsg.SendToChat(messageStr);
            getUserStateRef().get().msgsQueue.put(msg);
            sendMessageArea.setText("");
        }
    }

    @FXML
    private void clickItem() throws InterruptedException {
        getUserStateRef().get().setActiveDialog(jDialog);
        getUserStateRef().get().setActiveDialogArea(dialogArea);
        JavaInterMsg msg = new JavaInterMsg.SetActiveChat(
                chatsTable.getSelectionModel().getSelectedItem().chatIdProperty().get()
        );
        getUserStateRef().get().msgsQueue.put(msg);
        callButton.setVisible(true);
        callButton.setDisable(false);
    }

    @FXML
    private void sendMessageByKeyboard(){
        AtomicBoolean[] keysPressed = KeyboardHandler.handleShiftEnterPressed(sendMessageArea);
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (keysPressed[0].get() && keysPressed[1].get()) {
                    try {
                        sendMessage();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    @FXML
    private void findMessageByKeyboard(){
        AtomicBoolean keysPressed = KeyboardHandler.handleEnterPressed(searchMessageArea);
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if ( keysPressed.get() ) {
                    findMessage();
                }
            }
        }.start();
    }

    private void findMessage(){
        final String searchingStr = searchMessageArea.getText().trim();
        searchMessageArea.setText(searchingStr);
        if(!searchingStr.isEmpty()) {
            StringBuffer localDialogHistory = jDialog.getContent();
            String [] messagesArray = localDialogHistory.toString().split("\n");
            StringBuffer results = new StringBuffer();
            Arrays.stream(messagesArray)
                    .filter(str -> str.toLowerCase().contains(searchingStr.toLowerCase()))
                    .forEach(str -> results.append(str).append("\n"));
            dialogArea.setText(results.toString());
        }
    }


    private void createDialog(){
        jDialog = new JDialog("title stub");
    }

    @FXML
    private void callContactAction(){
        //TODO implement
    }


    @FXML
    private void showMenu(){

    }

    @FXML
    private void showChats(){

    }

    @FXML
    private void showPrivateChats(){

    }

    @FXML
    private void showOptions(){

    }

    @FXML
    private void makeCall(){

    }


}