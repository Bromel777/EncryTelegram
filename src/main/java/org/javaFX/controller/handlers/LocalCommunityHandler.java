package org.javaFX.controller.handlers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Duration;
import org.javaFX.EncryWindow;
import org.javaFX.model.JChat;
import org.javaFX.model.JLocalCommunity;
import org.javaFX.model.JLocalCommunityMember;
import org.javaFX.util.observers.BasicObserver;
import org.javaFX.util.observers.JTableObserver;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class LocalCommunityHandler extends DataHandler{

    @FXML
    private TableView<JLocalCommunityMember> chatsTable;

    @FXML
    private TableColumn<JLocalCommunityMember, Integer> rowNumberColumn;

    @FXML
    private TableColumn<JLocalCommunityMember, String> chatsNameColumn;

    @FXML
    private TableColumn<JLocalCommunityMember, Long> chatsIDColumn;

    @FXML
    private TableColumn<JLocalCommunityMember, CheckBox> checkBoxesColumn;

    private ScheduledExecutorService service;

    public LocalCommunityHandler() {
        service = Executors.newSingleThreadScheduledExecutor();
        service.schedule(new Runnable() {
            @Override
            public void run() {
                updateEncryWindow(getEncryWindow());
            }
        }, 1, TimeUnit.SECONDS);
    }

    @Override
    public void updateEncryWindow(EncryWindow encryWindow) {
        super.setEncryWindow(encryWindow);
        chatsTable.setItems(getObservableJCommunityMemberList());
        initChatsTable();
    }

    private ObservableList<JLocalCommunityMember> getObservableJCommunityMemberList(){
        ObservableList<JLocalCommunityMember> result = FXCollections.observableArrayList();
        getObservableChatList().forEach(
                chat -> result.add(new JLocalCommunityMember(chat.getTitle(), chat.getLastMessage(), chat.chatIdProperty()) )
        );
        return result;
    }

    private ObservableList<JChat> getObservableChatList(){
        ObservableList<JChat> observableChatList = FXCollections.observableArrayList();
        getUserStateRef().get().getChatList().forEach(
                chat -> observableChatList.add(new JChat(chat.title, "test", chat.id) )
        );
        return observableChatList;
    }

    private void initChatsTable(){
        rowNumberColumn.setCellValueFactory(cellDate -> new SimpleIntegerProperty(cellDate.getValue().getThisNumber().get()).asObject() );
        chatsNameColumn.setCellValueFactory(cellData -> cellData.getValue().getTitle());
        chatsIDColumn.setCellValueFactory(cellData -> cellData.getValue().chatIdProperty().asObject());
        checkBoxesColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(new CheckBox()) );
        service.shutdown();
    }

    @FXML
    private void createButtonAction(){
        System.out.println("create local community");
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToMainWindowFXML);
    }

    @FXML
    private void enableCheckBox() {
        System.out.println(chatsTable.getSelectionModel().getSelectedItem());
    }

}
