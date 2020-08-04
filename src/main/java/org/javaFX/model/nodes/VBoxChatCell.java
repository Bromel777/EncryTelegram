package org.javaFX.model.nodes;

import javafx.beans.property.LongProperty;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.AnchorPane;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
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
    private Separator separator;
    private LongProperty chatId;

    private final String chatTitleColorStr = "#334856";
    private final String innerContentCircleColorStr = "#6e8ca0";
    private final String backGroundStyle = "-fx-background-color:#FFFFFF;";
    private final String selectedBackGroundStyle = "-fx-background-color:#D4D3D4;";

    private double smallCircleRightIndent = 9.0;
    private double smallCircleTextRightIndent = 21.0;

    public VBoxChatCell(JChat jChat, double parentWidth) {
        super(jChat, parentWidth);
        chatId = jChat.chatIdProperty();
    }

    @Override
    protected void initNodes(JChat jChat){
        int labelIndent = 155;
        initRootPane(jChat);
        initChatTitleLabel(jChat, labelIndent);
        initlLastMessageLabel(jChat, labelIndent);
        initTimeLabel(jChat);
        circleColor = RandomChooser.getRandomColor();
        initBigCircle(jChat);
        initBigCircleText(jChat);
        initSeparator();
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
        AnchorPane.setLeftAnchor(timeLabel, chatTitleLabel.getWidth() + 70);
        if(smallCircle != null){
            getRootPane().getChildren().add(smallCircle);
            AnchorPane.setLeftAnchor(smallCircle, 70 + lastMessageLabel.getWidth() -  smallCircleRightIndent);
            getRootPane().getChildren().add(unreadMsgsNumberText);
            AnchorPane.setLeftAnchor(unreadMsgsNumberText,70 + lastMessageLabel.getWidth()-  smallCircleTextRightIndent);
        }
        getRootPane().getChildren().add(bigCircle);
        getRootPane().getChildren().add(abbreviationText);
        getRootPane().getChildren().add(separator);
        AnchorPane.setBottomAnchor(separator,0.0);
        this.getChildren().add(getRootPane());
    }

    @Override
    protected void initRootPane(JChat source){
        AnchorPane pane = new AnchorPane();
        pane.setMinSize(getParentWidth(),62);
        pane.setStyle(backGroundStyle);
        setRootPane(pane);
    }

    public void resetPaneColor(){
        AnchorPane pane = getRootPane();
        pane.setStyle(backGroundStyle);
        setRootPane(pane);
    }

    public void updatePaneColor(){
        AnchorPane pane = getRootPane();
        pane.setStyle(selectedBackGroundStyle);
        lastMessageLabel.setTextFill(Paint.valueOf(innerContentCircleColorStr));
        timeLabel.setTextFill(Paint.valueOf(innerContentCircleColorStr));
        setRootPane(pane);
    }

    private void initChatTitleLabel(JChat jChat, int indent ){
        chatTitleLabel = new Label();
        chatTitleLabel.setText(jChat.getTitle().get());
        chatTitleLabel.setPrefSize(getParentWidth() - indent,31);
        chatTitleLabel.setLayoutX(70);
        chatTitleLabel.setLayoutY(0);
        chatTitleLabel.setFont(Font.font("System",FontWeight.BOLD,18));
        chatTitleLabel.setTextFill(Paint.valueOf(chatTitleColorStr));
        chatTitleLabel.setWrapText(true);
    }

    private void initlLastMessageLabel(JChat jChat, int indent ){
        lastMessageLabel = new Label();
        String lastMessageStr = jChat.getLastMessage().getValue().replaceAll("\\s"," ").trim();
        lastMessageLabel.setText(lastMessageStr);
        lastMessageLabel.setWrapText(true);
        lastMessageLabel.setLayoutX(70);
        lastMessageLabel.setLayoutY(31);
        lastMessageLabel.setTextFill(Paint.valueOf(innerContentCircleColorStr));
        lastMessageLabel.setPrefSize(getParentWidth() - indent ,31);
    }

    public void updateChatLabels(double length){
        int indent = 155;
        chatTitleLabel.setPrefSize(length - indent,31);
        lastMessageLabel.setPrefSize(length - indent,31);
        AnchorPane.setLeftAnchor(timeLabel, chatTitleLabel.getWidth() + 70);
        updateMessageCircle();
    }

    private void initSeparator(){
        separator = new Separator();
        separator.setLayoutX(10);
        separator.setPrefWidth(600);
        separator.setOpacity(0.25);
    }

    private void initTimeLabel(JChat jChat){
        timeLabel = new Label();
        timeLabel.setText(TimeParser.parseDataString (jChat.getLastMessageTime().getValue().toString()));
        timeLabel.setPrefSize(44,31);
        timeLabel.setTextFill(Paint.valueOf(innerContentCircleColorStr));
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
        smallCircle.setLayoutY(42);
        smallCircle.setRadius(16);
        smallCircle.setFill(Color.valueOf(innerContentCircleColorStr));
    }

    private void initSmallCircleText(String unreadMessagedNumberStr){
        unreadMsgsNumberText = new Text();
        unreadMsgsNumberText.setFont(new Font(10));
        int smallCircleTextIndent = 24;
        unreadMsgsNumberText.setLayoutX(getParentWidth()-smallCircleTextIndent);
        unreadMsgsNumberText.setLayoutY(47);
        unreadMsgsNumberText.setText(unreadMessagedNumberStr);
        unreadMsgsNumberText.setTextAlignment(TextAlignment.CENTER);
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

    public String getLastMessage(){
        return lastMessageLabel.getText();
    }

    public void updateLastMessage(String newText, Long newTime, Integer unreadCount) {
        lastMessageLabel.setText(newText);
        timeLabel.setText(TimeParser.parseDataString ( newTime.toString() ) );
        if (unreadMsgsNumberText != null && smallCircle != null)
        {
            if (unreadCount > 0) {
                if (!smallCircle.isVisible()) {
                    unreadMsgsNumberText.setVisible(true);
                    smallCircle.setVisible(true);
                    updateMessageCircle();
                }
                unreadMsgsNumberText.setText(unreadCount.toString());
            }
            else {
                unreadMsgsNumberText.setVisible(false);
                smallCircle.setVisible(false);
            }
        } else {
            initSmallCircle();
            initSmallCircleText(unreadCount.toString());
        }
    }

    private void updateMessageCircle(){
        getRootPane().getChildren().remove(smallCircle);
        getRootPane().getChildren().remove(unreadMsgsNumberText);
        getRootPane().getChildren().add(smallCircle);
        AnchorPane.setLeftAnchor(smallCircle, 82 + lastMessageLabel.getWidth()  -  smallCircleRightIndent);
        getRootPane().getChildren().add(unreadMsgsNumberText);
        AnchorPane.setLeftAnchor(unreadMsgsNumberText,107 + lastMessageLabel.getWidth()  -  smallCircleTextRightIndent);
    }

}