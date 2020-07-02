package org.javaFX.model.nodes;

import javafx.beans.property.LongProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.javaFX.model.JChat;
import org.javaFX.util.RandomChooser;
import org.javaFX.util.TimeParser;

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

    public VBoxChatCell(JChat jChat, double parentWidth) {
        super(jChat, parentWidth);
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
    protected void setNodesToRootPane(JChat sourceElement){
        getRootPane().getChildren().add(chatTitleLabel);
        getRootPane().getChildren().add(lastMessageLabel);
        getRootPane().getChildren().add(timeLabel);
        AnchorPane.setRightAnchor(timeLabel,10.0);
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
        AnchorPane pane = new AnchorPane();
        pane.setPrefSize(getParentWidth(),62);
        setRootPane(pane);
    }

    private void initChatTitleLabel(JChat jChat){
        chatTitleLabel = new Label();
        chatTitleLabel.setText(jChat.getTitle().get());
        int chatTitleIndent = 115;
        chatTitleLabel.setPrefSize(getParentWidth() - chatTitleIndent,31);
        chatTitleLabel.setLayoutX(70);
        chatTitleLabel.setLayoutY(0);
        chatTitleLabel.setWrapText(false);
    }

    private void initlLastMessageLabel(JChat jChat){
        lastMessageLabel = new Label();
        String lastMessageStr;
        if(jChat.getLastMessage().getValue().indexOf("\n") != -1){
            lastMessageStr = jChat.getLastMessage().getValue().substring(0,jChat.getLastMessage().getValue().indexOf("\n"));
        }
        else{
            lastMessageStr = jChat.getLastMessage().getValue();
        }
        lastMessageLabel.setText(lastMessageStr);
        int lastMessageIndent = 115;
        lastMessageLabel.setPrefSize(getParentWidth() - lastMessageIndent,31);
        lastMessageLabel.setLayoutX(70);
        lastMessageLabel.setLayoutY(31);
        lastMessageLabel.setWrapText(false);
    }

    private void initTimeLabel(JChat jChat){
        timeLabel = new Label();
        timeLabel.setText(TimeParser.parseDataString (jChat.getLastMessageTime().getValue().toString()));
        timeLabel.setPrefSize(44,31);

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
        String result;
        if(strings.length > 1){
            result = ""+strings[0].charAt(0)+ strings[1].charAt(0);
        }
        else {
            result = " "+strings[0].charAt(0);
        }
        abbreviationText.setText(result.toUpperCase());
        abbreviationText.setFont(Font.font("Times New Roman", FontWeight.BOLD ,16) );
        abbreviationText.setFill(Color.WHITE);
    }

    private void initSmallCircle(){
        smallCircle = new Circle();
        int smallCircleIndent = 20;
        smallCircle.setLayoutX(getParentWidth()-smallCircleIndent);
        smallCircle.setLayoutY(47);
        smallCircle.setRadius(16);
        smallCircle.setFill(Color.GRAY);
    }

    private void initSmallCircleText(String unreadMessagedNumberStr){
        unreadMsgsNumberText = new Text();
        unreadMsgsNumberText.setFont(new Font(14));
        int smallCircleTextIndent = 24;
        unreadMsgsNumberText.setLayoutX(getParentWidth()-smallCircleTextIndent);
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