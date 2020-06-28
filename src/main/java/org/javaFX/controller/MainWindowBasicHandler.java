package org.javaFX.controller;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.util.Duration;
import org.encryfoundation.tg.javaIntegration.JavaInterMsg;
import org.javaFX.EncryWindow;
import org.javaFX.model.JDialog;
import org.javaFX.util.KeyboardHandler;
import org.javaFX.util.observers.BasicObserver;
import org.javaFX.util.observers.JTableObserver;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class MainWindowBasicHandler extends DataHandler{

    private final int delayDuration = 3;

    @FXML
    protected TextArea searchMessageTextArea;

    @FXML
    protected TextArea sendMessageTextArea;

    @FXML
    protected TextArea dialogTextArea;

    @FXML
    protected Button callButton;

    protected JDialog jDialog;

    public MainWindowBasicHandler() {
        chatListObserve(this);
        createDialog();
        /*dialogTextArea.setBackground(new Background(new BackgroundImage(new Image("file:src/main/resources/images/back.jpg"),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT)));*/
    }

    private void terminateObserver(){
        BasicObserver observer = getObserver();
        if( observer != null){
            observer.cancel();
        }
    }

    public void enableMenuBar(){
        if(getRootLayout().getTop().isDisable()) {
            getRootLayout().getTop().setDisable(false);
            getRootLayout().getTop().setVisible(true);
        }
    }

    public void chatListObserve(DataHandler controller){
        setObserver(new JTableObserver(controller));
        getObserver().setPeriod(Duration.seconds(delayDuration));
        getObserver().start();
    }

    public abstract void updateEncryWindow(EncryWindow encryWindow);

    @FXML
    protected abstract void initializeTable();

    @FXML
    public void sendMessage() throws InterruptedException {
        String messageStr = sendMessageTextArea.getText().trim();
        if(!messageStr.isEmpty()) {
            JavaInterMsg msg = new JavaInterMsg.SendToChat(messageStr);
            getUserStateRef().get().msgsQueue.put(msg);
            sendMessageTextArea.setText("");
        }
    }

    @FXML
    protected abstract void clickItem();

    @FXML
    public void sendMessageByKeyboard(){
        AtomicBoolean[] keysPressed = KeyboardHandler.handleShiftEnterPressed(sendMessageTextArea);
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
    public void findMessageByKeyboard(){
        AtomicBoolean keysPressed = KeyboardHandler.handleEnterPressed(searchMessageTextArea);
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if ( keysPressed.get() ) {
                    findMessage();
                }
            }
        }.start();
    }

    public void findMessage(){
        final String searchingStr = searchMessageTextArea.getText().trim();
        if(!searchingStr.isEmpty()) {
            StringBuffer localDialogHistory = jDialog.getContent();
            String [] messagesArray = localDialogHistory.toString().split("\n");
            StringBuffer results = new StringBuffer();
            Arrays.stream(messagesArray)
                    .filter(str -> str.toLowerCase().contains(searchingStr.toLowerCase()))
                    .forEach(str -> results.append(str).append("\n"));
            dialogTextArea.setText(results.toString());
        }
    }

    private void createDialog(){
        jDialog = new JDialog();
    }

}
