package org.javaFX.util;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.concurrent.atomic.AtomicBoolean;

public class KeyboardHandler {
    public static AtomicBoolean[] handleShiftEnterPressed(Node element ){
        AtomicBoolean[] keysPressed = new AtomicBoolean[2];
        keysPressed[0] = new AtomicBoolean(false);
        keysPressed[1] = new AtomicBoolean(false);
        element.setOnKeyPressed(keyEvent -> {
            switch (keyEvent.getCode()) {
                case ENTER:
                    keysPressed[0].set(true);
                    break;
                case SHIFT:
                    keysPressed[1].set(true);
                    break;
            }
        });

        element.setOnKeyReleased(keyEvent -> {
            switch (keyEvent.getCode()) {
                case ENTER:
                    keysPressed[0].set(false);
                    break;
                case SHIFT:
                    keysPressed[1].set(false);
                    break;
            }
        });
        return keysPressed;
    }


    public static AtomicBoolean handleEnterPressed(Node element ){
        AtomicBoolean keyPressed = new AtomicBoolean(false);
        element.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                keyPressed.set(true);
            }
        });

        element.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                keyPressed.set(false);
            }
        });
        return keyPressed;
    }

    public static EventHandler<KeyEvent> maxLengthHandler(final Integer i) {
        return arg0 -> {
            TextField tx = (TextField) arg0.getSource();
            if (tx.getText().length() >= i) {
                arg0.consume();
            }
        };
    }

}