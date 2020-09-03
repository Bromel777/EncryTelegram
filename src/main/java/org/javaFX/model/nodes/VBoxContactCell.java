package org.javaFX.model.nodes;

import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.javaFX.model.JSingleContact;
import org.javaFX.util.JavaFXTableBuilder;

public class VBoxContactCell extends VBoxCell<JSingleContact> {

    private final String pathToCheckboxNeutralImage = "images/checkboxNeutralSmall.png";
    private final String pathToCheckboxSelectedImage = "images/checkboxSelectedSmall.png";
    private Label contactNameLabel;
    private Label phoneNumberLabel;
    private ImageView checkBoxImg;
    private Separator separatorLine;

    private final JSingleContact currentContact;

    private final String backGroundStyle = "-fx-background-color:#FFFFFF;";
    private final String innerContentColorStr = "#000000";

    public VBoxContactCell(JSingleContact communityMember){
        super(communityMember);
        currentContact = communityMember;
    }

    @Override
    protected void initNodes(JSingleContact sourceElement) {
        initRootPane(sourceElement);
        initContactNameLabel(sourceElement);
        initPhoneNumberLabel(sourceElement);
        initCheckBoxImg();
        initSeparatorLine();
    }


    private void initContactNameLabel(JSingleContact communityMember){
        contactNameLabel = new Label();
        String preparedFullName = cutNameIfNecessary(communityMember.getFullName());
        contactNameLabel.setText("    " + preparedFullName);
        contactNameLabel.setLayoutX(0);
        contactNameLabel.setLayoutY(20);
        contactNameLabel.setFont(Font.font("Roboto", FontPosture.REGULAR,18 ));
    }

    private String cutNameIfNecessary(String communityMemberName){
        String[] nameWords = communityMemberName.split(" ");
        StringBuilder sb = new StringBuilder();
        for( String partOFName: nameWords){
            if(partOFName.length() > 15){
                partOFName = partOFName.substring(0,5)+".";
            }
            sb.append(partOFName);
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    private void initPhoneNumberLabel(JSingleContact communityMember){
        phoneNumberLabel = new Label();
        phoneNumberLabel.setText(getPreparedTelNumber(communityMember));
        phoneNumberLabel.setLayoutX(300);
        phoneNumberLabel.setLayoutY(20);
        phoneNumberLabel.setFont(Font.font("Roboto", FontPosture.REGULAR,18 ));
    }

    private void initCheckBoxImg(){
        checkBoxImg = new ImageView(new Image(pathToCheckboxNeutralImage));
        checkBoxImg.setLayoutY(15);
    }

    private void initSeparatorLine(){
        separatorLine = JavaFXTableBuilder.buildSeparatorLine( getRootPane());
    }

    @Override
    protected void setNodesToRootPane(JSingleContact jMessage) {
        getRootPane().getChildren().add(contactNameLabel);
        getRootPane().getChildren().add(phoneNumberLabel);
        getRootPane().getChildren().add(checkBoxImg);
        AnchorPane.setRightAnchor(checkBoxImg,45.0);
        getRootPane().getChildren().add(separatorLine);
        AnchorPane.setBottomAnchor(separatorLine,0.0);
        this.getChildren().add(getRootPane());
    }

    @Override
    protected void initRootPane(JSingleContact sourceElement) {
        setRootPane(new AnchorPane() );
        getRootPane().setPrefHeight(60);
        getRootPane().setStyle(backGroundStyle);
    }

    public void resetPaneColor(){
        AnchorPane pane = getRootPane();
        pane.setStyle(backGroundStyle);
        setRootPane(pane);
    }

    public void updatePaneColor(){
        AnchorPane pane = getRootPane();
        pane.setStyle(backGroundStyle);
        contactNameLabel.setTextFill(Paint.valueOf(innerContentColorStr));
        phoneNumberLabel.setTextFill(Paint.valueOf(innerContentColorStr));
        setRootPane(pane);
    }


    public JSingleContact getCurrentContact() {
        return currentContact;
    }

    private String getPreparedTelNumber(JSingleContact communityMember){
        StringBuilder sb = new StringBuilder("+");
        String tellNumber = communityMember.getPhoneNumber();
        if(tellNumber.startsWith("7")){
            sb.append(tellNumber.charAt(0)).append(" ")
            .append(tellNumber, 1, 4).append(" ")
            .append(tellNumber, 4, 7).append(" ")
            .append(tellNumber, 7, 9).append(" ")
            .append(tellNumber, 9, 11);
        }
        else if(tellNumber.startsWith("375")){
            sb.append(tellNumber, 0, 3).append(" ").
            append(tellNumber, 3, 5).append(" ")
            .append(tellNumber, 5, 8).append(" ")
            .append(tellNumber, 8, 10).append(" ")
            .append(tellNumber, 10, 12);
        }
        else {
            return tellNumber;
        }
        return sb.toString();
    }

    public void changeCheckboxStatus(){
        getRootPane().getChildren().remove(checkBoxImg);
        if(currentContact.isChosen()){
            checkBoxImg = new ImageView(new Image(pathToCheckboxSelectedImage));
        }
        else{
            checkBoxImg = new ImageView(new Image(pathToCheckboxNeutralImage));
        }
        checkBoxImg.setLayoutY(15);
        AnchorPane.setRightAnchor(checkBoxImg,45.0);
        getRootPane().getChildren().add(checkBoxImg);
    }

    public void setSeparatorLineSize(double newSize){
        separatorLine.setPrefWidth(newSize);
    }

}
