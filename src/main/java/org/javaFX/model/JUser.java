package org.javaFX.model;

import java.util.Objects;

public class JUser {

    private int ID;
    private String firstName;
    private String lastName;
    private String phoneNumber;

    public JUser(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public JUser(int ID, String firstName, String lastName, String phoneNumber) {
        this.ID = ID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() { return phoneNumber; }

    public int getID() {
        return ID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JUser jUser = (JUser) o;
        return ID == jUser.ID &&
                Objects.equals(firstName, jUser.firstName) &&
                Objects.equals(lastName, jUser.lastName) &&
                Objects.equals(phoneNumber, jUser.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, firstName, lastName, phoneNumber);
    }

    @Override
    public String toString() {
        return "JUser{" +
                "chatId=" + ID +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
