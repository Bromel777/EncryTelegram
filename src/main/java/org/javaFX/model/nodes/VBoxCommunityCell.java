package org.javaFX.model.nodes;

import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.javaFX.model.JLocalCommunity;
import org.javaFX.util.JavaFXTableBuilder;

public class VBoxCommunityCell extends VBoxCell<JLocalCommunity> {

    private Label communityIDLabel;
    private Label communityNameLabel;
    private Label numberOfMembersLabel;
    private Separator separatorLine;

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
        initSeparatorLine();
    }

    private void initCommunityIDLabel(JLocalCommunity sourceElement) {
        communityIDLabel = new Label();
        communityIDLabel.setText(sourceElement.getCommunityID()+"");
        communityIDLabel.setLayoutX(0);
        communityIDLabel.setLayoutY(20);
        communityIDLabel.setFont(Font.font("Roboto", FontPosture.REGULAR,18 ));
    }

    private void initCommunityNameLabel(JLocalCommunity sourceElement) {
        communityNameLabel = new Label();
        communityNameLabel.setText(sourceElement.getCommunityName());
        communityNameLabel.setLayoutX(240);
        communityNameLabel.setLayoutY(20);
        communityNameLabel.setFont(Font.font("Roboto", FontPosture.REGULAR,18 ));
    }

    private void initNumberOfMembersLabel(JLocalCommunity sourceElement) {
        numberOfMembersLabel = new Label();
        numberOfMembersLabel.setText(sourceElement.getCommunitySize().get()+"");
        numberOfMembersLabel.setLayoutY(20);
        numberOfMembersLabel.setFont(Font.font("Roboto", FontPosture.REGULAR,18 ));
    }

    private void initSeparatorLine(){
        separatorLine = JavaFXTableBuilder.buildSeparatorLine( getRootPane() );
    }

    @Override
    protected void setNodesToRootPane(JLocalCommunity jLocalCommunity) {
        getRootPane().getChildren().add(communityIDLabel);
        getRootPane().getChildren().add(communityNameLabel);
        getRootPane().getChildren().add(numberOfMembersLabel);
        AnchorPane.setRightAnchor(numberOfMembersLabel,45.0);
        getRootPane().getChildren().add(separatorLine);
        AnchorPane.setBottomAnchor(separatorLine,0.0);
        this.getChildren().add(getRootPane());
    }

    @Override
    protected void initRootPane(JLocalCommunity sourceElement){
        setRootPane(new AnchorPane() );
        getRootPane().setPrefHeight(60);
    }

    public JLocalCommunity getCurrentCommunity() {
        return currentCommunity;
    }

    public void setSeparatorLineSize(double newWidth) {
        this.separatorLine.setPrefWidth(newWidth);
    }

}
