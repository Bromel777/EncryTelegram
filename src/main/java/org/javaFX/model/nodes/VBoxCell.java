package org.javaFX.model.nodes;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public abstract class VBoxCell<T> extends VBox {

/*
    private int messageCellWidth;
    private int messageCellHeight;*/

    private int parentWidth;
    private int parentHeight;
    private AnchorPane rootPane;

    public VBoxCell(T sourceElement){
        setIsYourMessage(sourceElement);
        initNodes(sourceElement);
        setNodesToRootPane();
    }

    public VBoxCell(T sourceElement, int parentWidth, int parentHeight){
        this.parentWidth = parentWidth;
        this.parentHeight = parentHeight;
        setIsYourMessage(sourceElement);
        initNodes(sourceElement);
        setNodesToRootPane();
    }

    protected void setIsYourMessage(T sourceElement){};

    protected abstract void initNodes(T sourceElement);

    protected abstract void setNodesToRootPane();

    protected abstract void initRootPane(T sourceElement);

    public AnchorPane getRootPane() {
        return rootPane;
    }

    public void setRootPane(AnchorPane rootPane) {
        this.rootPane = rootPane;
    }

    public int getParentWidth() {
        return parentWidth;
    }

    public void setParentWidth(int parentWidth) {
        this.parentWidth = parentWidth;
    }

    public int getParentHeight() {
        return parentHeight;
    }

    public void setParentHeight(int parentHeight) {
        this.parentHeight = parentHeight;
    }

}
