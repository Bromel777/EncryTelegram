package org.javaFX.model.nodes;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import org.javaFX.model.JMessage;

import java.util.Objects;

public class VBoxDialogTextMessageCell extends VBoxMessageCell{

    private Label contentLabel;

    public VBoxDialogTextMessageCell(JMessage<String> jMessage) {
        super(jMessage, 583);
    }

    public VBoxDialogTextMessageCell(JMessage<String> jMessage, int parentWidth) {
        super(jMessage, parentWidth);
    }

    @Override
    protected void initContentNode(JMessage jMessage) {
        contentLabel = createContentLabel(jMessage);
        setLayoutsContentLabel(jMessage);
        setContentNode(contentLabel);
    }

    private Label createContentLabel(JMessage jMessage){
        Label contentLabel = new Label();
        String textContent = jMessage.getContent().toString().trim();
        contentLabel.setText(jMessage.getAuthor()+"\n"+textContent);
        contentLabel.setWrapText(true);
        contentLabel.setPrefSize(getCellWidth() - 5 , getCellHeight());
        return contentLabel;
    }

    protected void setLayoutsContentLabel(JMessage jMessage){
        if( jMessage.isMine() ){
            contentLabel.setLayoutX(16 + getParentWidth() / 3 );
            contentLabel.setLayoutY(1);
            contentLabel.setTextFill(Color.WHITE);
        }
        else{
            contentLabel.setLayoutX(5);
            contentLabel.setLayoutY(1);
            contentLabel.setTextFill(Color.BLACK);
        }
    }

    @Override
    protected void initMessageRectangle(JMessage jMessage) {
        Rectangle messageRectangle = createBigRectangle();
        setLayoutsBigRectangle(jMessage, messageRectangle);
        setMessageRectangle(messageRectangle);
    }

    private Rectangle createBigRectangle(){
        Rectangle messageRectangle = new Rectangle();
        messageRectangle.setArcHeight(26);
        messageRectangle.setArcWidth(26);
        messageRectangle.setWidth(getCellWidth()+5);
        messageRectangle.setHeight(getCellHeight());
        messageRectangle.setLayoutY(0);
        return messageRectangle;
    }

    protected void setLayoutsBigRectangle(JMessage jMessage , Rectangle messageRectangle){
        double ownerIndent = getParentWidth()/3;
        double otherIndent = 0;
        setFigureProperties(jMessage, messageRectangle, ownerIndent, otherIndent);
    }

    @Override
    protected void initAngleRectangle(JMessage jMessage) {
        Rectangle miniRectangle = initAngleRectangle();
        setLayoutsAngleRectangle(jMessage, miniRectangle);
        setAngleRectangle(miniRectangle);
    }

    private Rectangle initAngleRectangle (){
        Rectangle miniRectangle = new Rectangle();
        miniRectangle.setWidth(18);
        miniRectangle.setHeight(getCellHeight()-13);
        miniRectangle.setLayoutY(13);
        return miniRectangle;
    }

    protected void setLayoutsAngleRectangle(JMessage jMessage, Rectangle miniRectangle){
        double ownerIndent = getParentWidth() - 13;
        double otherIndent = 0 ;
        setFigureProperties(jMessage, miniRectangle, ownerIndent, otherIndent );
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VBoxDialogTextMessageCell that = (VBoxDialogTextMessageCell) o;
        return contentLabel.getText().equals(that.contentLabel.getText())
                && that.getTimeText().getText().equals(getTimeText().getText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentLabel);
    }
}