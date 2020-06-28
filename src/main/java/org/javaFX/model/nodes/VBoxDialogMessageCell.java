package org.javaFX.model.nodes;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.javaFX.model.JMessage;

public class VBoxDialogMessageCell extends VBoxMessageCell{

    public VBoxDialogMessageCell(JMessage jMessage) {
        super(jMessage, 590, 505);
    }

    public VBoxDialogMessageCell(JMessage jMessage, int parentWidth, int parentHeight) {
        super(jMessage, parentWidth, parentHeight);
    }

    @Override
    protected void initContentNode(JMessage jMessage) {
        Label contentLabel = new Label();
        String textContent = jMessage.getContent().toString()
                .substring(jMessage.getContent().toString().indexOf(":")+2);
        contentLabel.setText(textContent);
        contentLabel.setWrapText(true);
        contentLabel.setLayoutX(15);
        contentLabel.setLayoutY(15);
        contentLabel.setPrefSize(getRootPane().getPrefWidth()- 5 , getRootPane().getPrefHeight() - 5 );
        setContentNode(contentLabel);
    }

    @Override
    protected void initMessageRectangle(JMessage jMessage) {
        Rectangle messageRectangle = new Rectangle();
        Label contentNode = (Label) getContentNode();
        messageRectangle.setWidth(contentNode.getWidth());
        messageRectangle.setWidth(contentNode.getHeight());
        messageRectangle.setLayoutX(0);
        messageRectangle.setLayoutY(0);
        if(jMessage.isMine()){
            messageRectangle.setFill(Color.valueOf("#00B6FF"));
        }
        else{
            messageRectangle.setFill(Color.valueOf("#cbd1d3"));
        }
        setMessageRectangle(messageRectangle);
        messageRectangle.setVisible(true);
    }
}
