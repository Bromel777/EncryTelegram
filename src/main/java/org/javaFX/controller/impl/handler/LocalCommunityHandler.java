package org.javaFX.controller.impl.handler;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.drinkless.tdlib.TdApi;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;
import org.javaFX.controller.impl.dialog.EnterCommunityNameDialogController;
import org.javaFX.model.JLocalCommunity;
import org.javaFX.model.JLocalCommunityMember;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class LocalCommunityHandler extends DataHandler {

    @FXML
    private TableView<JLocalCommunityMember> chatsTable;

    @FXML
    private TableColumn<JLocalCommunityMember, Integer> rowNumberColumn;

    @FXML
    private TableColumn<JLocalCommunityMember, String> chatsNameColumn;

    @FXML
    private TableColumn<JLocalCommunityMember, String> phoneNumberColumn;

    @FXML
    private TableColumn<JLocalCommunityMember, Boolean> checkBoxesColumn;

    private ScheduledExecutorService service;

    private void runDelayedInitialization(){
        service = Executors.newSingleThreadScheduledExecutor();
        service.schedule(() -> updateEncryWindow(getEncryWindow()), 1, TimeUnit.SECONDS);
    }

    public LocalCommunityHandler() {
        runDelayedInitialization();
    }

    @Override
    public void updateEncryWindow(EncryWindow encryWindow) {
        super.setEncryWindow(encryWindow);
        chatsTable.setItems(getObservableUserList());
        initChatsTable();
    }


    private ObservableList<JLocalCommunityMember> getObservableUserList(){
        ObservableList<JLocalCommunityMember> observableChatList = FXCollections.observableArrayList();
        for(Integer jUserId: getUserStateRef().get().getUsers().keySet()){
            TdApi.User user = getUserStateRef().get().getUsers().get(jUserId);
            if(!user.lastName.isEmpty())
                observableChatList.add(new JLocalCommunityMember(user.firstName, user.lastName, user.phoneNumber, (long)user.id));
        }
        return observableChatList;
    }

    private void initChatsTable(){
        rowNumberColumn.setCellValueFactory(cellDate -> new SimpleIntegerProperty(cellDate.getValue().getRowNumber().get()).asObject() );
        chatsNameColumn.setCellValueFactory(cellData -> cellData.getValue().getFullName());
        phoneNumberColumn.setCellValueFactory(cellData -> cellData.getValue().getPhoneNumber());
        checkBoxesColumn.setCellValueFactory(cellData ->  cellData.getValue().isChosen());
        service.shutdown();
    }

    @FXML
    private void createButtonAction(){
        JLocalCommunity localCommunity = new JLocalCommunity();
        chatsTable.getItems().filtered(JLocalCommunityMember::isChosenBoolean).
               forEach(localCommunity::addContactToCommunity);
        JLocalCommunityMember.resetRowNumber();
        launchEnterNameDialog(localCommunity);
    }

    @FXML
    private void changeCheckBoxValue() {
        JLocalCommunityMember communityMember = chatsTable.getSelectionModel().getSelectedItem();
        if(communityMember.getUserId() > 0L ){
            boolean isChosen = communityMember.isChosenBoolean();
            communityMember.setBooleanChosen(!isChosen);
        }
    }

    private void launchEnterNameDialog(JLocalCommunity localCommunity){
        FXMLLoader loader = new FXMLLoader();
        Stage dialogStage = createDialogByPathToFXML(loader, EncryWindow.pathToLocalCommunityNameDialogFXML);
        EnterCommunityNameDialogController controller = loader.getController();
        controller.setEncryWindow(getEncryWindow());
        controller.setDialogStage(dialogStage);
        controller.setLocalCommunity(localCommunity);
        controller.setState(getUserStateRef());
        dialogStage.show();
    }

}