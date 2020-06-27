package org.javaFX.model.nodes;

import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.javaFX.model.JTextMessage;

public class VBoxMessageCell extends VBoxCell<JTextMessage> {
    //private ImageView backGroundImage;
    private Rectangle messageRectangle;
    private Label textLabel;
    private Text timeText;

    public VBoxMessageCell(JTextMessage jTextMessage){
        super(jTextMessage);
    }

    @Override
    protected void initNodes(JTextMessage jTextMessage){
        initAnchorPane(jTextMessage);
        initTextLabel(jTextMessage);
        initMessageRectangle();
        initTimeText(jTextMessage);
    }

    @Override
    protected void setNodesToRootPane(){
        getRootPane().getChildren().add(messageRectangle);
        getRootPane().getChildren().add(textLabel);
        getRootPane().getChildren().add(timeText);
        this.getChildren().add( getRootPane() );
    }

    private void initTextLabel(JTextMessage jTextMessage){
        textLabel = new Label();
        textLabel.setText(jTextMessage.getContent());
        textLabel.setWrapText(true);
        textLabel.setLayoutX(15);
        textLabel.setLayoutY(31);
    }

    private void initAnchorPane(JTextMessage jTextMessage){
        setRootPane(new AnchorPane() );
        if(jTextMessage.isMine()){
            getRootPane().setLayoutX(100);
        }
        getRootPane().setPrefSize(490,62);
    }

    private void initMessageRectangle(){
        messageRectangle = new Rectangle();
        getRootPane().setPrefSize(textLabel.getWidth(),textLabel.getHeight());
        textLabel.setLayoutX(0);
        textLabel.setLayoutY(0);
    }

    private void initTimeText(JTextMessage jTextMessage){
        timeText = new Text();
        timeText.setText("18:23");
        timeText.setLayoutX(450);
        timeText.setLayoutY(42);
    }

}
