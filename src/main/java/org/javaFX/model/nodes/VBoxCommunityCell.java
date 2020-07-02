package org.javaFX.model.nodes;

import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.javaFX.model.JLocalCommunity;
import org.javaFX.util.JavaFXTableBuilder;

public class VBoxCommunityCell extends VBoxCell<JLocalCommunity> {

    private Label communityIDLabel;
    private Label communityNameLabel;
    private Label numberOfMembersLabel;
    private ImageView createButtonImg;
    private ImageView deleteButtonImg;
    private Separator separatorLine;
    private final String pathToCreateImage = "file:src/main/resources/images/create.png";
    private final String pathToDeleteImage = "file:src/main/resources/images/delete.png";

    private final JLocalCommunity currentCommunity;

    public VBoxCommunityCell(JLocalCommunity sourceElement) {
        super(sourceElement);
        currentCommunity = sourceElement;
    }

    @Override
    protected void initNodes(JLocalCommunity sourceElement) {
        initRootPane(sourceElement);
        initCommunityIDLabel(sourceElement);
        initCommunityNameLabel(sourceElement);
        initNumberOfMembersLabel(sourceElement);
        initCreateButton();
        initDeleteButton();
        initSeparatorLine();
    }


    private void initCommunityIDLabel(JLocalCommunity sourceElement) {
        communityIDLabel = new Label();
        communityIDLabel.setText(sourceElement.getCommunityID()+"");
        communityIDLabel.setLayoutX(0);
        communityIDLabel.setLayoutY(20);
    }

    private void initCommunityNameLabel(JLocalCommunity sourceElement) {
        communityNameLabel = new Label();
        communityNameLabel.setText(sourceElement.getCommunityName());
        communityNameLabel.setLayoutX(200);
        communityNameLabel.setLayoutY(20);
    }

    private void initNumberOfMembersLabel(JLocalCommunity sourceElement) {
        numberOfMembersLabel = new Label();
        numberOfMembersLabel.setText(sourceElement.getCommunitySize().get()+"");
        numberOfMembersLabel.setLayoutX(400);
        numberOfMembersLabel.setLayoutY(20);
    }

    private void initSeparatorLine(){
        separatorLine = JavaFXTableBuilder.buildSeparatorLine( getRootPane() );
    }

    private void initCreateButton(){
        createButtonImg = new ImageView(new Image(pathToCreateImage) );
        createButtonImg.setLayoutX(600);
        createButtonImg.setLayoutY(3);
    }

    private void initDeleteButton(){
        deleteButtonImg = new ImageView(new Image(pathToDeleteImage) );
        deleteButtonImg.setFitWidth(60);
        deleteButtonImg.setFitHeight(60);
        deleteButtonImg.setLayoutX(900);
        deleteButtonImg.setLayoutY(5);
    }

    @Override
    protected void setNodesToRootPane(JLocalCommunity jLocalCommunity) {
        getRootPane().getChildren().add(communityIDLabel);
        getRootPane().getChildren().add(communityNameLabel);
        getRootPane().getChildren().add(numberOfMembersLabel);
        getRootPane().getChildren().add(separatorLine);
        AnchorPane.setBottomAnchor(separatorLine,0.0);
        getRootPane().getChildren().add(createButtonImg);
        getRootPane().getChildren().add(deleteButtonImg);
        this.getChildren().add(getRootPane());
    }

    @Override
    protected void initRootPane(JLocalCommunity sourceElement){
        setRootPane(new AnchorPane() );
        getRootPane().setPrefSize(800,60);
    }

    public JLocalCommunity getCurrentCommunity() {
        return currentCommunity;
    }

    public void setSeparatorLineSize(double newWidth) {
        this.separatorLine.setPrefWidth(newWidth);
    }

}
