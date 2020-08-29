package org.javaFX.model.nodes;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.drinkless.tdlib.TdApi;
import org.javaFX.model.JMessage;

public class VBoxCommonChatTextMessageCell extends VBoxDialogTextMessageCell{

    private ImageView smallPhoto;
    private int indent = 30;
    private final String pathToImgStub = "file:src/main/resources/images/lightBlueCircle.png";

    public VBoxCommonChatTextMessageCell(JMessage<String> jMessage) {
        super(jMessage);
    }

    public VBoxCommonChatTextMessageCell(JMessage<String> jMessage, int parentWidth) {
        super(jMessage, parentWidth);
    }

    //TODO It would be perfect if you set somehow small image to one of these constructors.
    // Then I'll get an opportunity to show them on pages.
    // When we know that number of members in dialogue is more than 2 we should create these instance


    public VBoxCommonChatTextMessageCell(JMessage<String> jMessage, TdApi.File smallPhoto) {
        super(jMessage);
    }

    public VBoxCommonChatTextMessageCell(JMessage<String> jMessage, int parentWidth, TdApi.File smallPhoto) {
        super(jMessage, parentWidth);
    }

    @Override
    protected void initNodes(JMessage jMessage) {
        super.initNodes(jMessage);
        initSmallPhoto();
    }

    @Override
    protected void setNodesToRootPane(JMessage jMessage) {
        getRootPane().getChildren().add(smallPhoto);
        super.setNodesToRootPane(jMessage);
    }

    @Override
    protected void setLayoutsContentLabel(JMessage jMessage) {
        if( jMessage.isMine() ){
            getContentLabel().setLayoutX(indent + 16  + getParentWidth() / 3 );
            getContentLabel().setLayoutY(1);
            getContentLabel().setTextFill(Color.WHITE);
        }
        else{
            getContentLabel().setLayoutX(indent + 5);
            getContentLabel().setLayoutY(1);
            getContentLabel().setTextFill(Color.BLACK);
        }
    }

    @Override
    protected void setLayoutsBigRectangle(JMessage jMessage, Rectangle messageRectangle) {
        double ownerIndent = indent+ getParentWidth()/3;
        double otherIndent = indent ;
        setFigureProperties(jMessage, messageRectangle, ownerIndent, otherIndent);
    }

    @Override
    protected void setLayoutsAngleRectangle(JMessage jMessage, Rectangle miniRectangle) {
        double ownerIndent = indent + getParentWidth() - 13;
        double otherIndent = indent ;
        setFigureProperties(jMessage, miniRectangle, ownerIndent, otherIndent );
    }

    private void initSmallPhoto(){
        smallPhoto = new ImageView(new Image(pathToImgStub) );
        smallPhoto.setSmooth(true);
        smallPhoto.setFitHeight(26);
        smallPhoto.setFitWidth(26);
    }
}