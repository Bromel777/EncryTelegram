package org.javaFX.util;

import org.javaFX.model.JLocalCommunity;

import java.util.HashMap;
import java.util.Map;

public class InfoContainer {
    private static Map<JLocalCommunity, Integer> communityMap = new HashMap<>();

    public static void addCommunity(JLocalCommunity jLocalCommunity){
        communityMap.put(jLocalCommunity, jLocalCommunity.getCommunityMembers().size());
    }

    public static int getSizeByName(String communityName){
        for(JLocalCommunity community: communityMap.keySet()){
            if(community.getCommunityName().equals(communityName)){
                return communityMap.get(community);
            }
        }
        return 0;
    }

}
