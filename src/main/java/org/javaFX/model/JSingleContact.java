package org.javaFX.model;

import org.drinkless.tdlib.TdApi;

public class JSingleContact extends JTableEntity{

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Long userId;
    private boolean isChosen;
    private TdApi.File circlePhoto;

    public JSingleContact(String firstNameStr, String lastNameStr, long userChatId){
        this.firstName = firstNameStr;
        this.lastName = lastNameStr;
        this.userId = userChatId;
    }
    public JSingleContact(String firstNameStr, String lastNameStr, String phoneNumber,long userChatId){
        this.firstName = firstNameStr;
        this.lastName = lastNameStr;
        this.phoneNumber = phoneNumber;
        this.userId = userChatId;
    }

    public JSingleContact(String firstName, String lastName, String phoneNumber, Long chatId, TdApi.File circlePhoto) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.userId = chatId;
        this.isChosen = false;
        this.circlePhoto = circlePhoto;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isChosen() {
        return isChosen;
    }

    public void setChosen(boolean chosen) {
        isChosen = chosen;
    }

    public String getFullName(){
        return getFirstName() + " " + getLastName();
    }

    @Override
    public String toString() {
        return "JLocalCommunityMember{" +
                "rowNumber=" + getRowNumber() +
                ", firstName=" + firstName +
                ", lastName=" + lastName +
                ", phoneNumber=" + phoneNumber +
                ", userId=" + userId +
                ", isChosen=" + isChosen +
                '}';
    }
}
