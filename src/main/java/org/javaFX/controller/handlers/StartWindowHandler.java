package org.javaFX.controller.handlers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class StartWindowHandler extends DataHandler {

    @FXML
    private ImageView loadingGif;

    @FXML
    private Button signInButton;

    public StartWindowHandler() {

    }

    public void setLoadingGif(ImageView loadingGif) {
        this.loadingGif = loadingGif;
        System.out.println("width: "+ loadingGif.getImage().getWidth()+"\theight: "+ loadingGif.getImage().getHeight());
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
