package org.javaFX.model;

import java.util.ArrayList;
import java.util.List;

public class JLocalCommunity {

    private List<JLocalCommunityMember> communityMembers;

    public JLocalCommunity() {
        communityMembers = new ArrayList<>();
    }

    public JLocalCommunity(List<JLocalCommunityMember> communityMembers) {
        this.communityMembers = communityMembers;
    }

    public List<JLocalCommunityMember> getCommunityMembers() {
        return communityMembers;
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
            if( member.getChatId() == chatId){
                contact = member;
                break;
            }
        }
        if (contact != null){
            removeContactFromCommunity(contact);
        }
        return contact;
    }
}
