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
        super(jMessage, 583, 491);
    }

    public VBoxDialogTextMessageCell(JMessage jMessage, int parentWidth, int parentHeight) {
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
        double height = 27 * multiplier;
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
        contentLabel.setPrefSize(width - 5 , height);
        setContentNode(contentLabel);
    }

    @Override
    protected void initMessageRectangle(JMessage jMessage) {
        Rectangle messageRectangle = new Rectangle();
        messageRectangle.setWidth(getCellWidth()- getCellHeight()/2);
        messageRectangle.setHeight(getCellHeight());
        messageRectangle.setLayoutY(0);
        double ownerIndent = getParentWidth()/3;
        double otherIndent = getCellHeight()/2;
        setFigureProperties(jMessage, messageRectangle, ownerIndent, otherIndent);
        setMessageRectangle(messageRectangle);
    }

    @Override
    protected void initOuterCircle(JMessage jMessage) {
        Circle outerCircle = new Circle();
        outerCircle.setRadius(getCellHeight()/2);
        outerCircle.setLayoutY(getCellHeight()/2);
        double ownerIndent = getParentWidth()/3;
        double otherIndent =  getCellWidth() ;
        setFigureProperties(jMessage, outerCircle, ownerIndent, otherIndent );
        setOuterCircle(outerCircle);
    }

    @Override
    protected void initInnerCircle(JMessage jMessage) {
        Circle innerCircle = new Circle();
        innerCircle.setRadius(getCellHeight()/2);
        innerCircle.setLayoutY(getCellHeight()/2);
        double ownerIndent = getParentWidth() - getCellHeight()/2;
        double otherIndent =  getCellHeight()/2  ;
        setFigureProperties(jMessage, innerCircle, ownerIndent, otherIndent);
        setInnerCircle(innerCircle);
    }

    @Override
    protected void initAngleRectangle(JMessage jMessage) {
        Rectangle miniRectangle = new Rectangle();
        miniRectangle.setWidth(getCellHeight()/2);
        miniRectangle.setHeight(getCellHeight()/2);
        miniRectangle.setLayoutY(getCellHeight()/2);
        double ownerIndent = getParentWidth() - getCellHeight()/2;
        double otherIndent =  0 ;
        setFigureProperties(jMessage, miniRectangle, ownerIndent, otherIndent );
        setAngleRectangle(miniRectangle);
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