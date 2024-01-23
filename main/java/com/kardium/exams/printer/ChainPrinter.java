package com.kardium.exams.printer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

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
    
    /**
    * Outputs a string to the printer and moves to the next line.
    *
    * @param line A string containing '0', '1' and ' ' characters.
    * Other characters are left blank.
    * The string is truncated to the line width.
    */
    @Override
    public void println(String line) {

        // Validating the input
        final char[] perfectInputChar = checkInput(line);

        int step = 0;
        int actuation = 0;
        final char[] letters = {'0','1'};

        // Counts the number of solenoid actuations needed
        final int maxActuation = countNumberOfActuation(perfectInputChar, letters);

        // Intial position for the aligned solenoid
        boolean firstSolenoidStatus = false;
        boolean secondSolenoidStatus = true;

        char firstStatusInt;
        char secondStatusInt;
        
        // Type in the letters
        while (actuation < maxActuation) {

            // Turn boolean to char for comparison
            firstStatusInt = firstSolenoidStatus ? '1' : '0';
            secondStatusInt = secondSolenoidStatus ? '1' : '0';
  
            // Check first aligned solenoid
            if (step < perfectInputChar.length && perfectInputChar[step] == firstStatusInt) {
                driver.fire(step); 
                actuation++;  
            } 

            // Check second aligned solenoid 
            if ( step+4 < perfectInputChar.length && perfectInputChar[step+4] == secondStatusInt) { 
                driver.fire(step+4); 
                actuation++; 
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
    }

    /**
    * Outputs a string to the printer and moves to the next line.
    *
    * @param line A string containing '0', '1' and ' ' characters.
    * Other characters are replaced with '1' and '0', on top of each others.
    * The string is truncated to the line width.
    */
    @Override
    public void dprintln(String line) {

        // Getting unsupported character locations
        int[] locations = debugCheckInput(line);
        char[] debugInputChar = checkInput(line);

        // Create first array with 1s in the unsupported character locations
        for (int index : locations) {
            debugInputChar[index] = '1';  
        }

        this.println(String.copyValueOf(debugInputChar));

        // Intialize the second array with ' '
        for (int i = 0; i < debugInputChar.length; i++){
            debugInputChar[i] = ' ';
        }

        // create second array with 0 in the unsupported character locations
        for (int index : locations) {
            debugInputChar[index] = '0';  
        }

        this.println(String.copyValueOf(debugInputChar));
    }

    /**
    * Outputs a string proportionally to the printer and moves to the next line.
    *
    * @param line A string containing '0', '1' and ' ' characters.
        * Successive ‘1’s are spaced 2 mm apart
        * Successive ‘0’s are spaced 3 mm apart
        * ‘0’s are spaced 3 mm from ‘1’s
        * Unsupported characters are left blank
    * The string is truncated to the line width.
    */
    @Override
    public void pprintln(String line) {
        
        // Chain representation Linkedlist
        LinkedList<String> chain10 = new LinkedList<String>();

        // Intializing the chain values
        Collections.addAll(chain10, " ", "0", " ",  " ", " ", "1",  " ", " ", " ",  "0", " ", " ",  " ", "1", " ",  " ", " ", "0",  " ", " ", " ",  "1", " ", " ");

        // Validating the input
        final char[] porInputChar = checkInput(line);

        // Printed characters representation Arraylist
        ArrayList<String> charArray = new ArrayList<>();
        
        // Intializing the characters array values
        charArray.add(" ");
        charArray.add(String.valueOf(porInputChar[0]));

        // Creating the character array with proportional spacing
        if (porInputChar.length > 1) {
            for (int i = 1; i < porInputChar.length; i++) {
                if (porInputChar[i-1] == '1' && porInputChar[i] == '1') {
                    charArray.add(" ");
                    charArray.add(String.valueOf(porInputChar[i]));
                } else {
                    charArray.add(" ");
                    charArray.add(" ");
                    charArray.add(String.valueOf(porInputChar[i]));
                }
            }
        }

        Iterator it = chain10.iterator();

        int actuation = 0;
        final char[] letters = {'0','1'};
        final int maxActuation = countNumberOfActuation(porInputChar, letters);
        int solenoidIndex;
        
        // Type in the letters
        while (actuation < maxActuation) {
            int index = 0;
            // Comparing the chain and solenoid position
            while (index < charArray.size()) {
                String nextChainValue = (String) it.next();
                if (nextChainValue != " " && nextChainValue.equals(charArray.get(index))) {
                    solenoidIndex = (index / 3);
                    driver.fire(solenoidIndex);
                    charArray.set(index, "X");
                    actuation++;
                }
                index++;
            }

            // Moving the chain one step forward
            chain10.add(chain10.poll());
            driver.step();

            // Reseting the Iterartor
            it = chain10.iterator();
        }
    }



     /**
    * Outputs an int array with the indexes of the unsupported characters.
    * Unsupported characters is anything other than '0', '1', ' '
    *
    * @param input A string containing any user input.
    */
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
        // turn the array list Integers to the Int array primitive type
        return unsupCharLocations.stream().mapToInt(i->i).toArray();
    }

     /**
    * Outputs a char array with the valid char without any of the unsupported characters.
    * Unsupported characters is anything other than '0', '1', ' ', which are replaced with ' '.
    * 
    * @param input string containing any user input. Input must not be empty.
    */
    private char[] checkInput(String input) { 

         // Checks if the input is empty
         if (input.isEmpty()) {
            throw new IllegalArgumentException("Input must not be empty");
        }

        // Truncating Input to 8 Chars 
        if (input.length() > 8) {
            input = input.substring(0, 8);
        }

        // Validating Input
        char[] inputChars = input.toCharArray();
        
        // Replacing all the unsupported characters
        for (int i = 0; i < inputChars.length; i++) {
            char letter = inputChars[i];
            if (!(letter == '0' || letter == '1' || letter == ' ')) {
                inputChars[i]= ' ';
            }
        }
        return inputChars;  
    }

     /**
    * Outputs an int representing the required solenoid actuation to type the line.
    *
    * @param input A char array containing the input letters.
    * @param letters A char array containing the letter that need counting
    */
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
        // errors
        printer.println("1");
        printer.println("0 0");
        printer.println("1 1");

        printer.println("11");
        //
        printer.println("0");
        
        printer.println("111");
        printer.println("1111");
        printer.dprintln("0s11a444fkjhsgew");

        printer.dprintln("013 4 110");
        // printer.pprintln("11111111");
        // printer.pprintln("");

    }

}
