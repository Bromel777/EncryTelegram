package org.javaFX.model.nodes;

import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.AnchorPane;
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
        separatorLine = JavaFXTableBuilder.buildSeparatorLine( getRootPane() );
    }

    @Override
    protected void setNodesToRootPane(JLocalCommunity jLocalCommunity) {
        getRootPane().getChildren().add(communityIDLabel);
        getRootPane().getChildren().add(communityNameLabel);
        getRootPane().getChildren().add(numberOfMembersLabel);
        getRootPane().getChildren().add(separatorLine);
        AnchorPane.setBottomAnchor(separatorLine,0.0);
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
