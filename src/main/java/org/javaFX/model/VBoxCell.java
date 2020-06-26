package org.javaFX.model;

import javafx.beans.property.LongProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.javaFX.util.RandomChooser;

public class VBoxCell extends VBox {
    private AnchorPane rootPane;
    private Label chatTitleLabel;
    private Label lastMessageLabel;
    private Label timeLabel;
    private Circle smallCircle;
    private Text unreadMsgsNumberText;
    private Circle bigCircle;
    private Text abbreviationText;
    private Color circleColor;
    private LongProperty chatId;

    public VBoxCell(JChat jChat) {
        super();
        circleColor = RandomChooser.getRandomColor();
        initNodes(jChat);
        setNodesToRootPane();
        chatId = jChat.chatIdProperty();
    }

    private void initNodes(JChat jChat){
        initAnchorPane();
        initChatTitleLabel(jChat);
        initlLastMessageLabel(jChat);
        initTimeLabel(jChat);
        initBigCircle(jChat);
        initBigCircleText(jChat);
        initSmallCircle(jChat);
        initSmallCircleText(jChat);
    }

    private void setNodesToRootPane(){
        rootPane.getChildren().add(chatTitleLabel);
        rootPane.getChildren().add(lastMessageLabel);
        rootPane.getChildren().add(timeLabel);
        rootPane.getChildren().add(smallCircle);
        rootPane.getChildren().add(unreadMsgsNumberText);
        rootPane.getChildren().add(bigCircle);
        rootPane.getChildren().add(abbreviationText);
        this.getChildren().add(rootPane);
    }


    private void initAnchorPane(){
        rootPane = new AnchorPane();
        rootPane.setPrefSize(260,62);
    }

    private void initChatTitleLabel(JChat jChat){
        chatTitleLabel = new Label();
        chatTitleLabel.setText(jChat.getTitle().get());
        chatTitleLabel.setPrefSize(145,31);
        chatTitleLabel.setLayoutX(70);
        chatTitleLabel.setLayoutY(0);
    }

    private void initlLastMessageLabel(JChat jChat){
        lastMessageLabel = new Label();
        lastMessageLabel.setText("last message stub");
        lastMessageLabel.setPrefSize(145,31);
        lastMessageLabel.setLayoutX(70);
        lastMessageLabel.setLayoutY(31);
    }

    private void initTimeLabel(JChat jChat){
        timeLabel = new Label();
        timeLabel.setText("14:47");
        timeLabel.setPrefSize(44,31);
        timeLabel.setLayoutX(218);
        timeLabel.setLayoutY(0);
    }

    private void initBigCircle(JChat jChat){
        bigCircle = new Circle();
        bigCircle.setLayoutX(33);
        bigCircle.setLayoutY(32);
        bigCircle.setRadius(26);
        bigCircle.setFill(circleColor);
    }

    private void initBigCircleText(JChat jChat){
        abbreviationText = new Text();
        abbreviationText.setLayoutX(22);
        abbreviationText.setLayoutY(38);
        String [] strings = jChat.getTitle().get().toUpperCase().split(" ");
        String result = "";
        if(strings.length > 1){
            result = strings[0].charAt(0)+" "+ strings[1].charAt(0);
        }
        else {
            result += " "+strings[0].charAt(0);
        }
        abbreviationText.setText(result);
    }

    private void initSmallCircle(JChat jChat){
        smallCircle = new Circle();
        smallCircle.setLayoutX(240);
        smallCircle.setLayoutY(47);
        smallCircle.setRadius(15);
        smallCircle.setFill(circleColor);
    }

    private void initSmallCircleText(JChat jChat){
        unreadMsgsNumberText = new Text();
        unreadMsgsNumberText.setFont(new Font(14));
        unreadMsgsNumberText.setLayoutX(236);
        unreadMsgsNumberText.setLayoutY(50);
        unreadMsgsNumberText.setText("1");
    }

    public long getChatId() {
        return chatId.get();
    }

    public LongProperty chatIdProperty() {
        return chatId;
    }

    public String getChatTitle(){
        return chatTitleLabel.getText();
    }
}