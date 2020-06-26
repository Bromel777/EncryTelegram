package org.javaFX.util;

import javafx.scene.paint.Color;

import java.util.Random;

public class RandomChooser {

    public static int getRandomIntNotMoreThan(int topBorder){
        return (int)(Math.random()*topBorder);
    }

    public static Color getRandomColor(){
        String[] mColors = {
                "#39add1", // light blue
                "#3079ab", // dark blue
                "#c25975", // mauve
                "#e15258", // red
                "#f9845b", // orange
                "#838cc7", // lavender
                "#7d669e", // purple
                "#53bbb4", // aqua
                "#51b46d", // green
                "#e0ab18", // mustard
                "#637a91", // dark gray
                "#f092b0", // pink
                "#b7c0c7"  // light gray
        };
        Random randomGenerator = new Random();
        int randomNumber = randomGenerator.nextInt(mColors.length);
        Color color = Color.valueOf ( mColors[randomNumber] );
        return color;
    }
}
