package org.javaFX.model.nodes;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.javaFX.EncryWindow;
import org.javaFX.model.JMessage;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;


public abstract class VBoxMessageCell extends VBoxCell<JMessage> {

    private final String youMessagesBackgroundColor = "#4988C1";
    //private final String otherMessageBackgroundColor = "#FFFFFF";
    private final String otherMessageBackgroundColor = "#a4f3c5";
    private final String backGroundStyle = "-fx-background-color: #fbfbfb";

    private Rectangle messageRectangle;
    private Circle outerTopCircle;
    private Circle outerBotCircle;
    private Circle innerCircle;
    private Rectangle angleRectangle;
    private Rectangle borderRectangle;
    private Node contentNode;
    private Text timeText;

    private double cellWidth;
    private double cellHeight;

    public VBoxMessageCell(JMessage jMessage, int parentWidth) {
        super(jMessage, parentWidth);
    }

    @Override
    protected void setIsYourMessage(JMessage jMessage) {
        if(jMessage.getContent().toString().length() <12){
            return;
        }
        String messagePhoneNumber = jMessage.getContent().toString().substring(0, 12);
        if (messagePhoneNumber.equals(EncryWindow.getUserPhoneNumber())) {
            jMessage.setMine(true);
        }
    }

    @Override
    protected void initNodes(JMessage jMessage) {
        initRootPane(jMessage);
        initMessageRectangle(jMessage);
        initOuterBotCircle(jMessage);
        initOuterTopCircle(jMessage);
        initInnerCircle(jMessage);
        initAngleRectangle(jMessage);
        initBorderRectangle(jMessage);
        initContentNode(jMessage);
        initTimeText(jMessage);
    }

    @Override
    protected void setNodesToRootPane() {
        getRootPane().getChildren().add(contentNode);
        getRootPane().getChildren().add(timeText);
        addElementToBack(messageRectangle);
        addElementToBack(outerTopCircle);
        addElementToBack(outerBotCircle);
        addElementToBack(innerCircle);
        addElementToBack(angleRectangle);
        addElementToBack(borderRectangle);
        this.getChildren().add(getRootPane());
    }

    private void addElementToBack(Node node){
        getRootPane().getChildren().add(node);
        getRootPane().getChildren().get(getRootPane().getChildren().size()-1).toBack();
    }

    protected abstract void initContentNode(JMessage jMessage);

    protected abstract void initOuterBotCircle(JMessage jMessage);
    protected abstract void initOuterTopCircle(JMessage jMessage);
    protected abstract void initInnerCircle(JMessage jMessage);
    protected abstract void initAngleRectangle(JMessage jMessage);
    protected abstract void initBorderRectangle(JMessage jMessage);
    protected abstract void initMessageRectangle(JMessage jMessage);

    @Override
    protected void initRootPane(JMessage jMessage) {
        AnchorPane pane = new AnchorPane();

        String textContent = jMessage.getContent().toString()
                .substring(jMessage.getContent().toString().indexOf(":")+2);
        int multiplier =
                textContent.length()%40 == 0 ? textContent.length()/40: (textContent.length()/40) +1;
        cellWidth = getParentWidth() - getParentWidth() / 3;
        cellHeight = 27 * multiplier;
        if (jMessage.isMine()) {
            pane.setLayoutX(getParentWidth() / 3);
        }
        else {
            pane.setLayoutX(1);
        }
        pane.setPrefSize(cellWidth, cellHeight);
        pane.setMinSize(cellWidth, cellHeight);
        pane.setStyle(backGroundStyle);
        setRootPane(pane);
    }


    private void initTimeText(JMessage jMessage) {
        timeText = new Text();
        timeText.setFont(new Font(12));
        Timestamp ts = new Timestamp ( Long.parseLong(jMessage.getTime())*1000 );
        Date date = new Date(ts.getTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM HH:mm");;
        timeText.setText(dateFormat.format(date));
        if(jMessage.isMine()){
            timeText.setLayoutX(getParentWidth() - 70);
            timeText.setFill(Color.WHITE);
        }
        else{
            timeText.setLayoutX(cellWidth - 70);
            timeText.setFill(Color.BLACK);
        }
        timeText.setLayoutY(cellHeight -3);
    }

    public Rectangle getMessageRectangle() {
        return messageRectangle;
    }

    public Circle getOuterTopCircle() {
        return outerTopCircle;
    }

    public Circle getInnerCircle() {
        return innerCircle;
    }

    public void setOuterTopCircle(Circle outerTopCircle) {
        this.outerTopCircle = outerTopCircle;
    }

    public void setOuterBotCircle(Circle outerBotCircle) {
        this.outerBotCircle = outerBotCircle;
    }

    public void setInnerCircle(Circle innerCircle) {
        this.innerCircle = innerCircle;
    }

    public Rectangle getAngleRectangle() {
        return angleRectangle;
    }

    public void setAngleRectangle(Rectangle angleRectangle) {
        this.angleRectangle = angleRectangle;
    }

    public void setMessageRectangle(Rectangle messageRectangle) {
        this.messageRectangle = messageRectangle;
    }

    public Node getContentNode() {
        return contentNode;
    }

    public void setContentNode(Node contentNode) {
        this.contentNode = contentNode;
    }

    public Text getTimeText() {
        return timeText;
    }

    public void setTimeText(Text timeText) {
        this.timeText = timeText;
    }

    public double getCellWidth() {
        return cellWidth;
    }

    public double getCellHeight() {
        return cellHeight;
    }

    public String getYouMessagesBackgroundColor() {
        return youMessagesBackgroundColor;
    }

    public String getOtherMessageBackgroundColor() {
        return otherMessageBackgroundColor;
    }

    public Rectangle getBorderRectangle() {
        return borderRectangle;
    }

    public void setBorderRectangle(Rectangle borderRectangle) {
        this.borderRectangle = borderRectangle;
    }
}