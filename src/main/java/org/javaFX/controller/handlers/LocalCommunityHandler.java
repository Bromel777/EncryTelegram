package org.javaFX.controller.handlers;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.javaFX.EncryWindow;
import org.javaFX.model.JChat;
import org.javaFX.model.JLocalCommunity;
import org.javaFX.model.JLocalCommunityMember;
import org.javaFX.model.JUser;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


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

    /*@FXML
    private TableColumn<JLocalCommunityMember, Boolean> checkBoxesColumn;*/

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

    private ObservableList<JUser> getObservableUserList(){
        ObservableList<JUser> observableUserList = FXCollections.observableArrayList();
        getUserStateRef().get().getUsers().values().forEach(
                user -> observableUserList.add(new JUser(user.firstName, user.id, user.phoneNumber) )
        );
        return observableUserList;
    }

    private void initChatsTable(){
        rowNumberColumn.setCellValueFactory(cellDate -> new SimpleIntegerProperty(cellDate.getValue().getThisNumber().get()).asObject() );
        chatsNameColumn.setCellValueFactory(cellData -> cellData.getValue().getTitle());
        chatsIDColumn.setCellValueFactory(cellData -> cellData.getValue().chatIdProperty().asObject());
        checkBoxesColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(new CheckBox()) );
        /*checkBoxesColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<JLocalCommunityMember, Boolean>, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<JLocalCommunityMember, Boolean> param) {
                JLocalCommunityMember member = param.getValue();
                SimpleBooleanProperty booleanProp = new SimpleBooleanProperty(member.isChosen());
                booleanProp.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
                                        Boolean newValue) {
                        member.setChosen(newValue);
                    }
                });
                return booleanProp;
            }
        });*/
        service.shutdown();
    }


    @FXML
    private void chooseMembers(){
        for (JLocalCommunityMember jMember: getObservableJCommunityMemberList() ) {
            if(checkBoxesColumn.getCellData(jMember).isSelected()){
                jMember.setChosen(true);
            }
        }
    }

    @FXML
    private void createButtonAction(){
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToMainWindowFXML);
        JLocalCommunity localCommunity = new JLocalCommunity();
        getObservableJCommunityMemberList().filtered(jLocalCommunityMember -> jLocalCommunityMember.isChosen()).
               forEach(jLocalCommunityMember -> localCommunity.addContactToCommunity(jLocalCommunityMember));
        localCommunity.getCommunityMembers().stream().forEach(mem -> System.out.println(mem));
        JLocalCommunityMember.resetRowNumber();
    }

    @FXML
    private void enableCheckBox() {
        System.out.println(chatsTable.getSelectionModel().getSelectedItem());
    }

}
