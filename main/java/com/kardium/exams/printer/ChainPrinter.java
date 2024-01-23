package com.kardium.exams.printer;

import java.util.ArrayList;
import java.util.Arrays;

/*
 * Copyright Kardium Inc. 2023.
 */

/**
 * Fill out this class to implement the {@link Printer} interface.
 */

public class ChainPrinter implements Printer {
    private final ChainPrinterDriver driver;

    public ChainPrinter(ChainPrinterDriver driver) {
        this.driver = driver;
    }
    
    @Override
    public void println(String line) {

        // Checks if the input is empty
        if (line.isEmpty()) {
            // Assumed that he need to give an imput and empty inputs are illegal
            throw new IllegalArgumentException("Input must not be empty");
        }

        // Validating the input
        final char[] perfectInputChar = checkInput(line);

    
        int step = 0;
        int actuation = 0;
        final char[] letters = {'0','1'};
        final int maxActuation = countNumberOfActuation(perfectInputChar, letters);
        boolean firstSolenoidStatus = false;
        boolean secondSolenoidStatus = true;
        char firstStatusInt;
        char secondStatusInt;
        
        // Type in the letters
        while (actuation < maxActuation) {
            // Turn boolean to char
            firstStatusInt = firstSolenoidStatus ? '1' : '0';
            secondStatusInt = secondSolenoidStatus ? '1' : '0';
  
            // Check first aligned solenoid 
            if (perfectInputChar[step] == firstStatusInt) {
                driver.fire(step); 
                actuation++; 
            } 

            // Check second aligned solenoid 
            if ((step+4 < perfectInputChar.length)) { 
                if ( perfectInputChar[step+4] == secondStatusInt) { 
                    driver.fire(step+4); 
                    actuation++; 
                }
            }
            
            // Reset logic every 4 steps
            if ((step+1) % 4 == 0) {
                step = 0;
                firstSolenoidStatus = !firstSolenoidStatus;
                secondSolenoidStatus = !secondSolenoidStatus;
            } else {
                step++;
            }
            
            // Toggle logic every step
            firstSolenoidStatus = !firstSolenoidStatus;
            secondSolenoidStatus = !secondSolenoidStatus;
            driver.step();
        }

        driver.linefeed();

        // for testing, remove later
        String perfectInput = String.copyValueOf(perfectInputChar);
        System.out.println( '"'  + perfectInput + '"');
        System.out.println(perfectInput.length());
    }

    @Override
    public void dprintln(String line) {

        // Getting unsupported character locations
        int[] locations = debugCheckInput(line);
        char[] debugInputChar = checkInput(line);
        
        System.out.println(Arrays.toString(locations));

        // create first array with 1
        for (int index : locations) {
            debugInputChar[index] = '1';  
        }

        System.out.println('"'+String.copyValueOf(debugInputChar)+'"');

        // this.println(String.copyValueOf(perfectInputChar));

        // create second array with 0
        for (int i = 0; i < debugInputChar.length; i++){
            debugInputChar[i] = ' ';
        }

        for (int index : locations) {
            debugInputChar[index] = '0';  
        }

        System.out.println('"'+String.copyValueOf(debugInputChar)+'"');
        // this.println(String.copyValueOf(debugInputChar));
    }

    @Override
    public void pprintln(String line) {
        // REPLACE THIS WITH YOUR CODE
        println(line);
    }




    private int[] debugCheckInput(String input) {

        if (input.length() > 8) {
            input = input.substring(0, 8);
        }

        ArrayList<Integer> unsupCharLocations = new ArrayList<>();
        // Validating Input
        char[] inputChars = input.toCharArray();

        for (int i = 0; i < inputChars.length; i++) {
            char letter = inputChars[i];
            if (!(letter == '0' || letter == '1' || letter == ' ')) {
                unsupCharLocations.add(i);
            }
        }
        return unsupCharLocations.stream().mapToInt(i->i).toArray();
    }

    private char[] checkInput(String input) { 
        // Truncating Input to 8 Chars 
        if (input.length() > 8) {
            input = input.substring(0, 8);
        }
        // Validating Input
        char[] inputChars = input.toCharArray();
        
        for (int i = 0; i < inputChars.length; i++) {
            char letter = inputChars[i];
            if (!(letter == '0' || letter == '1' || letter == ' ')) {
                inputChars[i]= ' ';
            }
        }
        return inputChars;  
    }


    // Count needed accuations 
    private int countNumberOfActuation(char[] input, char[] letters) {
        int letterActuation = 0;

        for (char letter : letters){
            for (int i = 0; i < input.length; i++) {
                if (input[i] == letter) { letterActuation++; }
            }
        }
        return letterActuation;
    }
    

    public static void main(String[] args) {
        Printer printer = new ChainPrinter(new LoggingChainPrinterDriver());
        printer.println("01100101");
        // printer.dprintln("0s11a444fkjhsgew");

        // printer.dprintln("013 4 110");
    }

}