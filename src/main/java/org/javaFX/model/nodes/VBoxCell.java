package org.javaFX.model.nodes;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public abstract class VBoxCell<T> extends VBox {

    private double smallCircleRightIndent = 9.0;
    private double smallCircleTextRightIndent = smallCircleRightIndent + 12;

    private double parentWidth;

    private AnchorPane rootPane;

    private T element;

    public VBoxCell(T sourceElement){
        this.element = sourceElement;
        setIsYourMessage(sourceElement);
        initNodes(sourceElement);
        setNodesToRootPane(sourceElement);
    }

    public VBoxCell(T sourceElement, double parentWidth){
        this.element = sourceElement;
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

    public T getElement() {return element;}

    public void setRootPane(AnchorPane rootPane) {
        this.rootPane = rootPane;
    }

    public double getParentWidth() {
        return parentWidth;
    }

    public double getSmallCircleTextRightIndent() {
        return smallCircleTextRightIndent;
    }

    public double getSmallCircleRightIndent() {
        return smallCircleRightIndent;
    }
}
