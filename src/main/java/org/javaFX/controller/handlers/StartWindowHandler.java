package org.javaFX.controller.handlers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.javaFX.controller.DataHandler;

import java.io.File;
import java.net.MalformedURLException;

public class StartWindowHandler extends DataHandler {

    @FXML
    private ImageView loadingGif;

    @FXML
    private Button signInButton;

    public StartWindowHandler() {
    }

    public void setLoadingGif(ImageView loadingGif) {
        this.loadingGif = loadingGif;
    }

    @FXML
    public void handleCancelAction(){
        getStage().close();
    }

    @FXML
    public void singInAction(){
        System.out.println("the button is disabled");
    }
}
