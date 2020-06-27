package org.javaFX.model.nodes;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public abstract class VBoxCell<T> extends VBox {
    private AnchorPane rootPane;

    public VBoxCell(T sourceElement){
        initNodes(sourceElement);
        setNodesToRootPane();
    }

    protected abstract void initNodes(T sourceElement);

    protected abstract void setNodesToRootPane();

    public AnchorPane getRootPane() {
        return rootPane;
    }

    public void setRootPane(AnchorPane rootPane) {
        this.rootPane = rootPane;
    }
}
