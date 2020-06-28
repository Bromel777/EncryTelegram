package org.javaFX.model.nodes;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.javaFX.EncryWindow;
import org.javaFX.model.JMessage;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;


public abstract class VBoxMessageCell extends VBoxCell<JMessage> {

    //private ImageView backGroundImage;
    private Rectangle messageRectangle;
    private Node contentNode;
    private Text timeText;

    private final int INDENT = 250;

    public VBoxMessageCell(JMessage jMessage, int parentWidth, int parentHeight) {
        super(jMessage, parentWidth, parentHeight);
    }

    @Override
    protected void setIsYourMessage(JMessage jMessage) {
        String messagePhoneNumber = jMessage.getContent().toString().substring(0, 12);
        if (messagePhoneNumber.equals(EncryWindow.getUserPhoneNumber())) {
            jMessage.setMine(true);
        }
    }

    @Override
    protected void initNodes(JMessage jMessage) {
        initRootPane(jMessage);
        initContentNode(jMessage);
        initMessageRectangle(jMessage);
        initTimeText(jMessage);
    }

    @Override
    protected void setNodesToRootPane() {
        getRootPane().getChildren().add(messageRectangle);
        getRootPane().getChildren().add(contentNode);
        getRootPane().getChildren().add(timeText);
        this.getChildren().add(getRootPane());
    }

    protected abstract void initContentNode(JMessage jMessage);

    @Override
    protected void initRootPane(JMessage jMessage) {
        setRootPane(new AnchorPane());
        if (jMessage.isMine()) {
            getRootPane().setLayoutX(INDENT);
        }
        getRootPane().setPrefSize(getParentWidth()-INDENT, 62);
    }

    protected abstract void initMessageRectangle(JMessage jMessage);

    private void initTimeText(JMessage jMessage) {
        timeText = new Text();
        timeText.setFont(new Font(12));
        Timestamp ts= new Timestamp ( Long.parseLong(jMessage.getTime())*1000 );
        Date date=new Date(ts.getTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");;
        timeText.setText(dateFormat.format(date));
        timeText.setLayoutX(450);
        timeText.setLayoutY(62);
    }

    public Rectangle getMessageRectangle() {
        return messageRectangle;
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

}