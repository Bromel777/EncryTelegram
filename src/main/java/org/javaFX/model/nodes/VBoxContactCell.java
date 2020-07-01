package org.javaFX.model.nodes;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import org.javaFX.model.JSingleContact;

public class VBoxContactCell extends VBoxCell<JSingleContact> {

    private final String pathToCheckboxNeutralImage = "file:src/main/resources/images/checkboxNeutral.png";
    private final String pathToCheckboxSelectedImage = "file:src/main/resources/images/checkboxSelected.png";
    private Label contactNameLabel;
    private Label phoneNumberLabel;
    private ImageView checkBoxImg;
    private Line separatorLine;

    private final JSingleContact currentContact;


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
        contactNameLabel.setText(communityMember.getFullName().get());
        contactNameLabel.setLayoutX(0);
        contactNameLabel.setLayoutY(20);
    }

    private void initPhoneNumberLabel(JSingleContact communityMember){
        phoneNumberLabel = new Label();
        phoneNumberLabel.setText(getPreparedTelNumber(communityMember));
        phoneNumberLabel.setLayoutX(300);
        phoneNumberLabel.setLayoutY(20);
    }

    private void initCheckBoxImg(){
        checkBoxImg = new ImageView(new Image(pathToCheckboxNeutralImage) );
        /*checkBoxImg.setLayoutX(756);
        checkBoxImg.setLayoutY(7);*/
    }

    private void initSeparatorLine(){
        separatorLine = new Line();
        separatorLine.setLayoutX(0);
        separatorLine.setLayoutY(62);
        separatorLine.setEndX(800);
        separatorLine.setStrokeWidth(3);
        separatorLine.setStroke(Color.GRAY);
    }

    @Override
    protected void setNodesToRootPane() {
        getRootPane().getChildren().add(contactNameLabel);
        getRootPane().getChildren().add(phoneNumberLabel);
        getRootPane().getChildren().add(checkBoxImg);
        AnchorPane.setRightAnchor(checkBoxImg,20.0);
        getRootPane().getChildren().add(separatorLine);
        AnchorPane.setBottomAnchor(separatorLine,0.0);
        this.getChildren().add(getRootPane());
    }

    @Override
    protected void initRootPane(JSingleContact sourceElement) {
        setRootPane(new AnchorPane() );
        getRootPane().setPrefSize(800,60);

    }

    public JSingleContact getCurrentContact() {
        return currentContact;
    }

    private String getPreparedTelNumber(JSingleContact communityMember){
        StringBuilder sb = new StringBuilder("+");
        String tellNumber = communityMember.getPhoneNumber().get();
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
        if(currentContact.isChosenBoolean()){
            checkBoxImg = new ImageView(new Image(pathToCheckboxSelectedImage) );
        }
        else{
            checkBoxImg = new ImageView(new Image(pathToCheckboxNeutralImage) );
        }
        checkBoxImg.setLayoutX(756);
        checkBoxImg.setLayoutY(7);
        getRootPane().getChildren().add(checkBoxImg);
    }
}
