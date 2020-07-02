package org.javaFX.model.nodes;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public abstract class VBoxCell<T> extends VBox {

    private double parentWidth;

    private AnchorPane rootPane;

    public VBoxCell(T sourceElement){
        setIsYourMessage(sourceElement);
        initNodes(sourceElement);
        setNodesToRootPane(sourceElement);
    }

    public VBoxCell(T sourceElement, double parentWidth){
        this.parentWidth = parentWidth;
        setIsYourMessage(sourceElement);
        initNodes(sourceElement);
        setNodesToRootPane(sourceElement);
    }

    protected void setIsYourMessage(T sourceElement){}

    protected abstract void initNodes(T sourceElement);

    protected abstract void setNodesToRootPane(T sourceElement);

    protected abstract void initRootPane(T sourceElement);

    public AnchorPane getRootPane() {
        return rootPane;
    }

    public void setRootPane(AnchorPane rootPane) {
        this.rootPane = rootPane;
    }

    public double getParentWidth() {
        return parentWidth;
    }

}
