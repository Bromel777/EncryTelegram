package org.javaFX.util;

import javafx.scene.control.Separator;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.Shadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

public class JavaFXTableBuilder {

    public static Separator buildSeparatorLine( AnchorPane rootPane){
        Separator separatorLine = new Separator();
        separatorLine.setPrefWidth(rootPane.getPrefWidth());
        Shadow effect = new Shadow();
        effect.setBlurType(BlurType.ONE_PASS_BOX);
        effect.setColor(Color.GRAY);
        effect.setHeight(0.0);
        effect.setRadius(0.0);
        effect.setWidth(0.0);
        separatorLine.setEffect(effect);
        return separatorLine;
    }
}
