package org.javaFX.controller;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.util.Duration;
import org.encryfoundation.tg.javaIntegration.BackMsg;
import org.javaFX.EncryWindow;
import org.javaFX.util.KeyboardHandler;
import org.javaFX.util.observers.BasicObserver;
import org.javaFX.util.observers.JWindowObserver;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class MainWindowBasicHandler extends DataHandler{

    private final Duration duration = Duration.millis(5) ;

    @FXML
    protected TextArea searchMessageTextArea;

    @FXML
    protected TextArea sendMessageTextArea;


    public MainWindowBasicHandler() {
        chatListObserve(this);

    }

    protected void terminateObserver(){
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
        setObserver(new JWindowObserver(controller));
        getObserver().setPeriod(duration);
        getObserver().start();
    }

    public abstract void updateEncryWindow(EncryWindow encryWindow);

    @FXML
    protected abstract void initializeTable();

    @FXML
    public void sendMessage() throws InterruptedException {
        String messageStr = sendMessageTextArea.getText().trim();
        if(!messageStr.isEmpty()) {
            BackMsg msg = new BackMsg.SendToChat(messageStr);
            getUserStateRef().get().outQueue.put(msg);
            sendMessageTextArea.setText("");
        }
    }

    @FXML
    protected abstract void clickItem() throws InterruptedException;

    @FXML
    public void sendMessageByKeyboard(){
        AtomicBoolean[] keysPressed = KeyboardHandler.handleShiftEnterPressed(sendMessageTextArea);
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (keysPressed[0].get() && !keysPressed[1].get()) {
                    try {
                        sendMessage();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(keysPressed[0].get() && keysPressed[1].get()){
                    sendMessageTextArea.appendText("\n");
                    keysPressed[0].set(false);
                    keysPressed[1].set(false);
                }
            }
        }.start();
    }

    @FXML
    protected abstract void searchMessageByKeyboard();

    @FXML
    protected abstract void findContentInDialog();

}
