package org.javaFX.model.nodes;

import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import org.javaFX.model.JLocalCommunity;

public class VBoxCommunityCell extends VBoxCell<JLocalCommunity> {

    private Label communityIDLabel;
    private Label communityNameLabel;
    private Label numberOfMembersLabel;
    private Line separatorLine;

    private final JLocalCommunity currentCommunity;

    public VBoxCommunityCell(JLocalCommunity sourceElement) {
        super(sourceElement);
        currentCommunity = sourceElement;
    }

    @Override
    protected void initNodes(JLocalCommunity sourceElement) {
        initAnchorPane();
        initCommunityIDLabel(sourceElement);
        initCommunityNameLabel(sourceElement);
        initNumberOfMembersLabel(sourceElement);
        initSeparatorLine();
    }


    private void initAnchorPane(){
        setRootPane(new AnchorPane() );
        getRootPane().setPrefSize(800,60);
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
        communityNameLabel.setLayoutX(250);
        communityNameLabel.setLayoutY(20);
    }

    private void initNumberOfMembersLabel(JLocalCommunity sourceElement) {
        numberOfMembersLabel = new Label();
        numberOfMembersLabel.setText(sourceElement.getCommunitySize().get()+"");
        numberOfMembersLabel.setLayoutX(756);
        numberOfMembersLabel.setLayoutY(20);
    }

    private void initSeparatorLine(){
        separatorLine = new Line();
        separatorLine.setLayoutX(0);
        separatorLine.setLayoutY(62);
        separatorLine.setEndX(800);
        separatorLine.setStrokeWidth(3);
        separatorLine.setStroke(Color.GRAY);
    }

    @Override
    protected void setNodesToRootPane() {
        getRootPane().getChildren().add(communityIDLabel);
        getRootPane().getChildren().add(communityNameLabel);
        getRootPane().getChildren().add(numberOfMembersLabel);
        getRootPane().getChildren().add(separatorLine);
        this.getChildren().add(getRootPane());
    }

    public JLocalCommunity getCurrentCommunity() {
        return currentCommunity;
    }
}
