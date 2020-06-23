package org.javaFX.model;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class JTableEntity {

    private static AtomicInteger totalNumber = new AtomicInteger(1);
    private AtomicInteger rowNumber;

    public JTableEntity() {
        this.rowNumber = new AtomicInteger( totalNumber.getAndIncrement() );
    }

    public static void resetRowNumber(){
        totalNumber.set(1);
    }

    public AtomicInteger getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(AtomicInteger rowNumber) {
        this.rowNumber = rowNumber;
    }
}
