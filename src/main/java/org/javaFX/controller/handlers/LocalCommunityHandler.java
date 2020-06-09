package org.javaFX.controller.handlers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.javaFX.EncryWindow;
import org.javaFX.model.JChat;
import org.javaFX.model.JLocalCommunity;
import org.javaFX.model.JLocalCommunityMember;

import java.io.IOException;
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
        checkBoxesColumn.setCellValueFactory(cellData ->  cellData.getValue().isChosen());
        service.shutdown();
    }

    @FXML
    private void createButtonAction(){
        JLocalCommunity localCommunity = new JLocalCommunity();
        chatsTable.getItems().filtered(JLocalCommunityMember::isChosenBoolean).
               forEach(localCommunity::addContactToCommunity);
        localCommunity.getCommunityMembers().forEach(System.out::println);
        JLocalCommunityMember.resetRowNumber();
        launchEnterNameDialog(localCommunity);
    }

    @FXML
    private void changeCheckBoxValue() {
        JLocalCommunityMember communityMember = chatsTable.getSelectionModel().getSelectedItem();
        if(communityMember.getChatId() > 0L ){
            boolean isChosen = communityMember.isChosenBoolean();
            communityMember.setBooleanChosen(!isChosen);
        }
    }

    private void launchEnterNameDialog(JLocalCommunity localCommunity){
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(EncryWindow.class.getResource(EncryWindow.pathToLocalCommunityNameDialogFXML));
        Stage dialogStage = new Stage();
        try {
            AnchorPane startOverview = loader.load();
            Scene scene = new Scene(startOverview);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            EnterCommunityNameDialogController controller = loader.getController();
            controller.setEncryWindow(getEncryWindow());
            controller.setDialogStage(dialogStage);
            controller.setLocalCommunity(localCommunity);
            dialogStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
