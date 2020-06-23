package org.javaFX.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class JLocalCommunity extends JTableEntity{

    private String communityName;
    private long communityID;
    private List<JLocalCommunityMember> communityMembers;
    private int communitySize;

    private void generatePseudoRandomCommunityID(){
        this.communityID = (long)(Long.MAX_VALUE*Math.random());
    }

    public JLocalCommunity() {
        communityMembers = new ArrayList<>();
        generatePseudoRandomCommunityID();
    }

    public JLocalCommunity(String communityName) {
        this();
        this.communityName = communityName;
    }


    public JLocalCommunity(String communityName, int localCommunitySize) {
        this();
        this.communityName = communityName;
        this.communitySize = localCommunitySize;
    }


    public JLocalCommunity(List<JLocalCommunityMember> communityMembers) {
        this();
        this.communityMembers = communityMembers;
    }

    public List<JLocalCommunityMember> getCommunityMembers() {
        return communityMembers;
    }

    public StringProperty getStringPropertyCommunityName(){
        return new SimpleStringProperty(getCommunityName());
    }

    public void setCommunityMembers(List<JLocalCommunityMember> communityMembers) {
        this.communityMembers = communityMembers;
    }

    public void addContactToCommunity(JLocalCommunityMember contact){
        if(!communityMembers.contains(contact)){
            communityMembers.add(contact);
        }
    }

    private void removeContactFromCommunity(JLocalCommunityMember contact){
        if(!communityMembers.contains(contact)){
            communityMembers.remove(contact);
        }
    }

    private JLocalCommunityMember removeContactFromCommunityById(int chatId){
        JLocalCommunityMember contact = null;
        for(JLocalCommunityMember member: communityMembers){
            if( member.getUserId() == chatId){
                contact = member;
                break;
            }
        }
        if (contact != null){
            removeContactFromCommunity(contact);
        }
        return contact;
    }

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    public long getCommunityID() {
        return communityID;
    }

    public AtomicInteger getCommunitySize(){
        return new AtomicInteger(communityMembers.size() == 0 ?communitySize : communityMembers.size());
    }

}
