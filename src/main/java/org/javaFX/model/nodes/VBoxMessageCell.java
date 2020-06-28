package org.javaFX.model.nodes;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.javaFX.model.JMessage;

public abstract class VBoxMessageCell extends VBoxCell<JMessage> {
    //private ImageView backGroundImage;
    private Rectangle messageRectangle;
    private Node contentNode;
    private Text timeText;

    public VBoxMessageCell(JMessage jMessage){
        super(jMessage);
    }

    @Override
    protected void initNodes(JMessage jMessage){
        initAnchorPane(jMessage);
        initContentNode(jMessage);
        initMessageRectangle();
        initTimeText(jMessage);
    }

    @Override
    protected void setNodesToRootPane(){
        getRootPane().getChildren().add(messageRectangle);
        getRootPane().getChildren().add(contentNode);
        getRootPane().getChildren().add(timeText);
        this.getChildren().add( getRootPane() );
    }

    protected abstract void initContentNode(JMessage jMessage);

    private void initAnchorPane(JMessage jMessage){
        setRootPane(new AnchorPane() );
        if(jMessage.isMine()){
            getRootPane().setLayoutX(100);
        }
        getRootPane().setPrefSize(490,62);
    }

    protected abstract void initMessageRectangle();

    private void initTimeText(JMessage jMessage){
        timeText = new Text();
        timeText.setText(jMessage.getTime());
        timeText.setLayoutX(450);
        timeText.setLayoutY(42);
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
