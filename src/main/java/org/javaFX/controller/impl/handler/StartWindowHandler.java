package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;
import org.javaFX.util.observers.JWindowObserver;

import java.util.concurrent.atomic.AtomicInteger;

public class StartWindowHandler extends DataHandler {


    @FXML
    private Label progressLabel;

    @FXML
    private ProgressBar progressBar;

    private AtomicInteger loadingStatusInt;

    private double delayDuration = 100;

    private double totalTimeMillis = 3000;

    public StartWindowHandler() {
        super();
        loadingStatusInt = new AtomicInteger(0);
        rootPaneObserve(this);
    }

    @Override
    public void updateEncryWindow(EncryWindow encryWindow) {
        super.updateEncryWindow(encryWindow);
        double localProgress = loadingStatusInt.getAndAdd(51)/totalTimeMillis ;
        progressBar.setProgress(localProgress);
        progressLabel.setText("loading "+String.format("%.0f", localProgress*100)+"%");
    }

    private void rootPaneObserve(DataHandler controller){
        setObserver(new JWindowObserver(controller));
        getObserver().setPeriod(Duration.millis(delayDuration));
        getObserver().start();
    }
}