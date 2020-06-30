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
    private final String otherMessageBackgroundColor = "#FFFFFF";
    private final String backGroundStyle = "-fx-background-color: #FBFBFB";

    private Rectangle messageRectangle;
    private Circle outerCircle;
    private Circle innerCircle;
    private Rectangle angleRectangle;
    private Node contentNode;
    private Text timeText;

    private double cellWidth;
    private double cellHeight;

    public VBoxMessageCell(JMessage jMessage, int parentWidth, int parentHeight) {
        super(jMessage, parentWidth, parentHeight);
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
        initOuterCircle(jMessage);
        initInnerCircle(jMessage);
        initAngleRectangle(jMessage);
        initOuterCircle(jMessage);
        initContentNode(jMessage);
        initTimeText(jMessage);
    }

    @Override
    protected void setNodesToRootPane() {
        getRootPane().getChildren().add(contentNode);
        getRootPane().getChildren().add(timeText);
        getRootPane().getChildren().add(messageRectangle);
        getRootPane().getChildren().get(getRootPane().getChildren().size()-1).toBack();
        getRootPane().getChildren().add(outerCircle);
        getRootPane().getChildren().get(getRootPane().getChildren().size()-1).toBack();
        getRootPane().getChildren().add(innerCircle);
        getRootPane().getChildren().get(getRootPane().getChildren().size()-1).toBack();
        getRootPane().getChildren().add(angleRectangle);
        getRootPane().getChildren().get(getRootPane().getChildren().size()-1).toBack();
        this.getChildren().add(getRootPane());
    }

    protected abstract void initContentNode(JMessage jMessage);

    protected abstract void initOuterCircle(JMessage jMessage);
    protected abstract void initInnerCircle(JMessage jMessage);
    protected abstract void initAngleRectangle(JMessage jMessage);

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
            pane.setLayoutX(cellWidth);
        }
        else {
            pane.setLayoutX(1);
        }
        pane.setPrefSize(cellWidth, cellHeight);
        pane.setMinSize(cellWidth, cellHeight);
        pane.setStyle(backGroundStyle);
        setRootPane(pane);
    }

    protected abstract void initMessageRectangle(JMessage jMessage);

    private void initTimeText(JMessage jMessage) {
        timeText = new Text();
        timeText.setFont(new Font(12));
        Timestamp ts= new Timestamp ( Long.parseLong(jMessage.getTime())*1000 );
        Date date=new Date(ts.getTime());
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

    public Circle getOuterCircle() {
        return outerCircle;
    }

    public Circle getInnerCircle() {
        return innerCircle;
    }

    public void setOuterCircle(Circle outerCircle) {
        this.outerCircle = outerCircle;
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
}