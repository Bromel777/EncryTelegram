package org.javaFX.controller.handlers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import org.javaFX.controller.DataHandler;

public class StartWindowHandler extends DataHandler {

    @FXML
    private ImageView loadingGif;

    @FXML
    private Button signInButton;

    public void setLoadingGif(ImageView loadingGif) {
        this.loadingGif = loadingGif;
    }

    @FXML
    public void handleCancelAction(){
        getStage().close();
    }

    @FXML
    public void singInAction(){
    }
}
