package org.javaFX.model;

import javafx.beans.property.*;

import java.util.concurrent.atomic.AtomicInteger;

public class JLocalCommunityMember {

    private static AtomicInteger totalNumber = new AtomicInteger(1);

    private AtomicInteger tableNumber;


    private StringProperty firstName;
    private StringProperty lastName;
    private StringProperty phoneNumber;
    private LongProperty userId;
    private BooleanProperty isChosen;

    public JLocalCommunityMember(StringProperty firstName, StringProperty lastName, StringProperty phoneNumber, LongProperty chatId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.userId = chatId;
        this.tableNumber = new AtomicInteger( totalNumber.getAndIncrement() );
        this.isChosen = new SimpleBooleanProperty(false);
    }

    public JLocalCommunityMember(String firstNameStr, String lastNameStr, String lastMessageStr, Long chatId) {
        this(new SimpleStringProperty(firstNameStr), new SimpleStringProperty(lastNameStr), new SimpleStringProperty(lastMessageStr), new SimpleLongProperty(chatId));
    }

    public StringProperty getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(StringProperty phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LongProperty userIdProperty() {
        return userId;
    }

    public AtomicInteger getThisNumber() {
        return tableNumber;
    }

    public long getUserId() {
        return userId.get();
    }

    public BooleanProperty isChosen() {
        return isChosen;
    }

    public void setChosen(BooleanProperty chosen) {
        isChosen = chosen;
    }

    public boolean isChosenBoolean() {
        return isChosen.get();
    }

    public void setBooleanChosen(boolean chosen) {
        isChosen.setValue(chosen);
    }

    public static void resetRowNumber(){
        totalNumber.set(1);
    }

    public String getFirstName() {
        return firstName.get();
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public String getLastName() {
        return lastName.get();
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public StringProperty phoneNumberProperty() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber.set(phoneNumber);
    }

    public StringProperty getFullName(){
        return new SimpleStringProperty(getFirstName() + " " + getLastName());
    }

    @Override
    public String toString() {
        return "JLocalCommunityMember{" +
                "tableNumber=" + tableNumber +
                ", firstName=" + firstName +
                ", lastName=" + lastName +
                ", phoneNumber=" + phoneNumber +
                ", userId=" + userId +
                ", isChosen=" + isChosen +
                '}';
    }
}
