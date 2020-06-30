package org.javaFX.model.nodes;

import javafx.beans.property.LongProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.encryfoundation.tg.utils.MessagesUtils;
import org.javaFX.model.JChat;
import org.javaFX.util.RandomChooser;

public class VBoxChatCell extends VBoxCell<JChat>{
    private Label chatTitleLabel;
    private Label lastMessageLabel;
    private Label timeLabel;
    private Circle smallCircle;
    private Text unreadMsgsNumberText;
    private Circle bigCircle;
    private Text abbreviationText;
    private Color circleColor;
    private LongProperty chatId;

    public VBoxChatCell(JChat jChat) {
        super(jChat);
        chatId = jChat.chatIdProperty();
    }

    @Override
    protected void initNodes(JChat jChat){
        initRootPane(jChat);
        initChatTitleLabel(jChat);
        initlLastMessageLabel(jChat);
        initTimeLabel(jChat);
        circleColor = RandomChooser.getRandomColor();
        initBigCircle(jChat);
        initBigCircleText(jChat);
        if(jChat.getUnreadMessagesNumber().get() > 0 ){
            initSmallCircle();
            initSmallCircleText((String.valueOf(jChat.getUnreadMessagesNumber().get())));
        }
    }

    @Override
    protected void setNodesToRootPane(){
        getRootPane().getChildren().add(chatTitleLabel);
        getRootPane().getChildren().add(lastMessageLabel);
        getRootPane().getChildren().add(timeLabel);
        if(smallCircle != null){
            getRootPane().getChildren().add(smallCircle);
            getRootPane().getChildren().add(unreadMsgsNumberText);
        }
        getRootPane().getChildren().add(bigCircle);
        getRootPane().getChildren().add(abbreviationText);
        this.getChildren().add(getRootPane());
    }

    @Override
    protected void initRootPane(JChat source){
        setRootPane(new AnchorPane() );
        getRootPane().setPrefSize(260,62);
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
        lastMessageLabel.setText(jChat.getLastMessage().getValue());
        lastMessageLabel.setPrefSize(145,31);
        lastMessageLabel.setLayoutX(70);
        lastMessageLabel.setLayoutY(31);
    }

    private void initTimeLabel(JChat jChat){
        timeLabel = new Label();
        timeLabel.setText(jChat.getLastMessageTime().getValue());
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
        String [] strings = jChat.getTitle().get().split(" ");
        String result = " ";
        if(strings.length > 1){
            result = ""+strings[0].charAt(0)+ strings[1].charAt(0);
        }
        else {
            result += ""+strings[0].charAt(0);
        }
        abbreviationText.setText(result.toUpperCase());
        abbreviationText.setFont(Font.font("Times New Roman", FontWeight.BOLD ,16) );
        abbreviationText.setFill(Color.WHITE);
    }

    private void initSmallCircle(){
        smallCircle = new Circle();
        smallCircle.setLayoutX(240);
        smallCircle.setLayoutY(47);
        smallCircle.setRadius(16);
        smallCircle.setFill(Color.GRAY);
    }

    private void initSmallCircleText(String unreadMessagedNumberStr){
        unreadMsgsNumberText = new Text();
        unreadMsgsNumberText.setFont(new Font(14));
        unreadMsgsNumberText.setLayoutX(236);
        unreadMsgsNumberText.setLayoutY(52);
        unreadMsgsNumberText.setText(unreadMessagedNumberStr);
        unreadMsgsNumberText.setFill(Color.WHITE);
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