package org.javaFX.model.nodes;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import org.javaFX.model.JMessage;

public class VBoxDialogTextMessageCell extends VBoxMessageCell{

    private Label contentLabel;

    public VBoxDialogTextMessageCell(JMessage jMessage) {
        super(jMessage, 583);
    }

    public VBoxDialogTextMessageCell(JMessage jMessage, int parentWidth) {
        super(jMessage, parentWidth);
    }

    @Override
    protected void initContentNode(JMessage jMessage) {
        contentLabel = new Label();
        String textContent = jMessage.getContent().toString()
                .substring(jMessage.getContent().toString().indexOf(":")+2);
        contentLabel.setText(textContent);
        contentLabel.setWrapText(true);
        int multiplier =
                textContent.length() % 40 == 0 ? textContent.length()/40: (textContent.length()/40) +1;
        double width = getParentWidth() - getParentWidth() / 3;
        double height = 27 * multiplier;
        if( jMessage.isMine() ){
            contentLabel.setLayoutX(getParentWidth() / 3 );
            contentLabel.setLayoutY(1);
            contentLabel.setTextFill(Color.WHITE);
        }
        else{
            contentLabel.setLayoutX(20);
            contentLabel.setLayoutY(1);
            contentLabel.setTextFill(Color.BLACK);
        }
        contentLabel.setPrefSize(width - 5 , height);
        setContentNode(contentLabel);
    }

    @Override
    protected void initMessageRectangle(JMessage jMessage) {
        Rectangle messageRectangle = new Rectangle();
        messageRectangle.setWidth(getCellWidth()-13);
        messageRectangle.setHeight(getCellHeight());
        messageRectangle.setLayoutY(0);
        double ownerIndent = getParentWidth()/3;
        double otherIndent = 13;
        setFigureProperties(jMessage, messageRectangle, ownerIndent, otherIndent);
        setMessageRectangle(messageRectangle);
    }

    @Override
    protected void initOuterTopCircle(JMessage jMessage) {
        setOuterTopCircle(initOuterCircle(jMessage, 13));
    }

    @Override
    protected void initOuterBotCircle(JMessage jMessage) {
        setOuterBotCircle(initOuterCircle(jMessage, getCellHeight()-13));
    }

    protected Circle initOuterCircle(JMessage jMessage, double layoutY){
        Circle outerCircle = new Circle();
        outerCircle.setRadius(13);
        outerCircle.setLayoutY(layoutY);
        double ownerIndent = getParentWidth()/3;
        double otherIndent =  getCellWidth() + 1;
        setFigureProperties(jMessage, outerCircle, ownerIndent, otherIndent );
        return outerCircle;
    }

    @Override
    protected void initInnerCircle(JMessage jMessage) {
        Circle innerCircle = new Circle();
        innerCircle.setRadius(13);
        innerCircle.setLayoutY(13);
        double ownerIndent = getParentWidth() - 13;
        double otherIndent =  13;
        setFigureProperties(jMessage, innerCircle, ownerIndent, otherIndent);
        setInnerCircle(innerCircle);
    }

    @Override
    protected void initAngleRectangle(JMessage jMessage) {
        Rectangle miniRectangle = new Rectangle();
        miniRectangle.setWidth(13);
        miniRectangle.setHeight(getCellHeight()-13);
        miniRectangle.setLayoutY(13);
        double ownerIndent = getParentWidth() - 13;
        double otherIndent = 0 ;
        setFigureProperties(jMessage, miniRectangle, ownerIndent, otherIndent );
        setAngleRectangle(miniRectangle);
    }

    @Override
    protected void initBorderRectangle(JMessage jMessage) {
        Rectangle borderRectangle = new Rectangle();
        borderRectangle.setWidth(14);
        borderRectangle.setHeight(getCellHeight()-26);
        borderRectangle.setLayoutY(13);
        double ownerIndent = getParentWidth()/3 -13;
        double otherIndent = getParentWidth() - getParentWidth()/3;
        setFigureProperties(jMessage, borderRectangle, ownerIndent, otherIndent);
        setBorderRectangle(borderRectangle);
    }

    protected void setFigureProperties(JMessage jMessage, Shape shape, double ownerIndent, double otherIndent){
        if(jMessage.isMine()){
            shape.setFill(Color.valueOf(getYouMessagesBackgroundColor()));
            shape.setLayoutX(ownerIndent);
        }
        else{
            shape.setFill(Color.valueOf(getOtherMessageBackgroundColor()));
            shape.setLayoutX(otherIndent);
        }
    }

    public Label getContentLabel() {
        return contentLabel;
    }

    public void setContentLabel(Label contentLabel) {
        this.contentLabel = contentLabel;
    }
}