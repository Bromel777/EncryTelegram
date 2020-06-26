package org.javaFX.controller.impl.handler;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import org.encryfoundation.tg.javaIntegration.JavaInterMsg;
import org.javaFX.EncryWindow;
import org.javaFX.controller.MainWindowBasicHandler;
import org.javaFX.model.JChat;
import org.javaFX.model.VBoxCell;

public class _ChatsWindowHandler extends MainWindowBasicHandler {

    @FXML
    private ListView<VBoxCell> chatsListView;

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



    public _ChatsWindowHandler(){
    }

    @Override
    public void updateEncryWindow(EncryWindow encryWindow) {
        super.setEncryWindow(encryWindow);
        initializeTable();
        enableMenuBar();
    }

    @FXML
    private ObservableList<VBoxCell> getObservableJChatList(){
        ObservableList<VBoxCell> observableChatList = FXCollections.observableArrayList();
        getUserStateRef().get().getChatList().forEach(
                chat -> observableChatList.add(new VBoxCell( new JChat(chat.title, "stub", chat.id)  ))
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
        /*callButton.setVisible(true);
        callButton.setDisable(false);*/
    }

    @FXML
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
    }

    @FXML
    private void findContentInDialog(){
        //TODO find message in dialog area
    }
}
