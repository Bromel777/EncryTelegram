package org.javaFX.model.nodes;

import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import org.javaFX.model.JMessage;

public class VBoxDialogMessageCell extends VBoxMessageCell{

    public VBoxDialogMessageCell(JMessage jMessage) {
        super(jMessage);
    }

    @Override
    protected void initContentNode(JMessage jMessage) {
        Label contentLabel = new Label();
        contentLabel.setText(jMessage.getContent().toString());
        contentLabel.setWrapText(true);
        contentLabel.setLayoutX(15);
        contentLabel.setLayoutY(31);
        setContentNode(contentLabel);
    }

    @Override
    protected void initMessageRectangle() {
        Rectangle messageRectangle = new Rectangle();
        Label contentNode = (Label) getContentNode();
        messageRectangle.setWidth(contentNode.getWidth());
        messageRectangle.setWidth(contentNode.getHeight());
        messageRectangle.setLayoutX(0);
        messageRectangle.setLayoutY(0);
        setMessageRectangle(messageRectangle);
    }
}
