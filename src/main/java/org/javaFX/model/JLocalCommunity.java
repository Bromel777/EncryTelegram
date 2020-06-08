package org.javaFX.model;

import java.util.ArrayList;
import java.util.List;

public class JLocalCommunity {

    private List<JUser> communityMembers;

    public JLocalCommunity() {
        communityMembers = new ArrayList<>();
    }

    public JLocalCommunity(List<JUser> communityMembers) {
        this.communityMembers = communityMembers;
    }

    public List<JUser> getCommunityMembers() {
        return communityMembers;
    }

    public void setCommunityMembers(List<JUser> communityMembers) {
        this.communityMembers = communityMembers;
    }

    public void addContactToCommunity(JUser contact){
        if(!communityMembers.contains(contact)){
            communityMembers.add(contact);
        }
    }

    private void removeContactFromCommunity(JUser contact){
        if(!communityMembers.contains(contact)){
            communityMembers.remove(contact);
        }
    }
    private JUser removeContactFromCommunityById(int chatId){
        JUser contact = null;
        for(JUser user: communityMembers){
            if( user.getChatId() == chatId){
                contact = user;
                break;
            }
        }
        if (contact != null){
            removeContactFromCommunity(contact);
        }
        return contact;
    }
}
