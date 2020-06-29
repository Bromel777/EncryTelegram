package org.javaFX.model.nodes;

import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.javaFX.model.JMessage;

public class VBoxDialogMessageCell extends VBoxMessageCell{

    private Label contentLabel;

    public VBoxDialogMessageCell(JMessage jMessage) {
        super(jMessage, 583, 491);
    }

    public VBoxDialogMessageCell(JMessage jMessage, int parentWidth, int parentHeight) {
        super(jMessage, parentWidth, parentHeight);
    }

    @Override
    protected void initContentNode(JMessage jMessage) {
        contentLabel = new Label();
        String textContent = jMessage.getContent().toString()
                .substring(jMessage.getContent().toString().indexOf(":")+2);
        contentLabel.setText(textContent);
        contentLabel.setWrapText(true);
        int multiplier =
                textContent.length()%40 == 0 ? textContent.length()/40: (textContent.length()/40) +1;
        double width = getParentWidth() - getParentWidth() / 3;
        double height = 27*multiplier;
        if( jMessage.isMine() ){
            contentLabel.setLayoutX(getParentWidth() / 3 );
            contentLabel.setLayoutY(1);
            contentLabel.setTextFill(Color.WHITE);
        }
        else{
            contentLabel.setLayoutX(1);
            contentLabel.setLayoutY(1);
            contentLabel.setTextFill(Color.BLACK);
        }
        contentLabel.setPrefSize(width, height);
        setContentNode(contentLabel);
    }

    @Override
    protected void initMessageRectangle(JMessage jMessage) {
        Rectangle messageRectangle = new Rectangle();
        messageRectangle.setWidth(getCellWidth()- getCellHeight()/2);
        messageRectangle.setHeight(getCellHeight());
        messageRectangle.setLayoutY(0);
        if(jMessage.isMine()){
            messageRectangle.setFill(Color.valueOf("#4988C1"));
            messageRectangle.setLayoutX(getParentWidth()/3);
        }
        else{
            messageRectangle.setFill(Color.valueOf("#ffffff"));
            messageRectangle.setLayoutX(getCellHeight()/2);
        }
        setMessageRectangle(messageRectangle);
    }

    @Override
    protected void initOuterCircle(JMessage jMessage) {
        Circle outerCircle = new Circle();
        outerCircle.setRadius(getCellHeight()/2);
        outerCircle.setLayoutY(getCellHeight()/2);
        if(jMessage.isMine()){
            outerCircle.setFill(Color.valueOf("#4988C1"));
            outerCircle.setLayoutX(getParentWidth()/3);
        }
        else{
            outerCircle.setFill(Color.valueOf("#ffffff"));
            outerCircle.setLayoutX(getCellWidth());
        }
        setOuterCircle(outerCircle);
    }

    @Override
    protected void initInnerCircle(JMessage jMessage) {
        Circle innerCircle = new Circle();
        innerCircle.setRadius(getCellHeight()/2);
        innerCircle.setLayoutY(getCellHeight()/2);
        if(jMessage.isMine()){
            innerCircle.setFill(Color.valueOf("#4988C1"));
            innerCircle.setLayoutX(getParentWidth()-getCellHeight()/2);
        }
        else{
            innerCircle.setFill(Color.valueOf("#ffffff"));
            innerCircle.setLayoutX(getCellHeight()/2);
        }
        setInnerCircle(innerCircle);
    }

    @Override
    protected void initAngleRectangle(JMessage jMessage) {
        Rectangle miniRectangle = new Rectangle();
        miniRectangle.setWidth(getCellHeight()/2);
        miniRectangle.setHeight(getCellHeight()/2);
        miniRectangle.setLayoutY(getCellHeight()/2);
        if(jMessage.isMine()){
            miniRectangle.setFill(Color.valueOf("#4988C1"));
            miniRectangle.setLayoutX(getParentWidth() - getCellHeight()/2);
        }
        else{
            miniRectangle.setFill(Color.valueOf("#ffffff"));
            miniRectangle.setLayoutX(0);
        }
        setAngleRectangle(miniRectangle);
    }
}