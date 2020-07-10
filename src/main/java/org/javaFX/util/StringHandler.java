package org.javaFX.util;

public class StringHandler {

    public static int countCharactersInStr(String str, char c){
        int number = 0;
        for(int i = 0 ; i < str.length() ; ++i){
            if(str.charAt(i) == c){
                number++;
            }
        }
        return number;
    }
}
