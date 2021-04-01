/*
 * File: Project1.java
 * Author: Ben Sutter
 * Date: November 11, 2020
 * Purpose: Try to parse a GUI from a file with various methods that ahere to certain grammar rules.
 */
package project1;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.*;

public class Project1 {

    //Very descriptive names for GUI variables that are "created" in parse methods.
    private JButton button;
    private ButtonGroup radioButtonGroup;
    private JRadioButton radioButton;
    private JTextField text;
    private JLabel label;
    private JPanel panel;
    private JFrame frame;
    //Keeps track of how many panels/button groups were started and finished.
    private int startedComponents = 0;
    private int finishedComponents = 0;


    /* These variables were inspired by Barrett Otte: 
    https://github.com/barrettotte/Recursive-Descent-GUI-Parser/blob/master/GUIParser.java
    Before, I was feeding strings as a parameter to everything and it was quite a mess.
    By adding each line from the file to an arraylist and using currentComponent 
    to track what line is being processed, things became much easier.
    isFrame also made determining where to add each widget a breeze.
    I also used a similar print style to show the user what has been parsed. */
    private boolean isFrame;
    ArrayList<String> components = new ArrayList<>();
    private int currentComponent = 0;

    public Project1() {
        //Opens the file, adds each line (stripped ofwhite spaces) to components
        openFile();
        //Rune through components, parsing each line if possible
        parseGUI();
    }//End GUI

    private void openFile() {
        try {
            //Create a JFileChooser so the user can select which file to parse.
            JFileChooser jfc = new JFileChooser("This doesn't matter");
            jfc.setCurrentDirectory(new File("."));
            int userApproval = jfc.showOpenDialog(null);//Initializes variable for approval later on
            if (userApproval == JFileChooser.APPROVE_OPTION) {//Changes userApproval to start program if a file is chosen
                File theChosenOne = jfc.getSelectedFile();
                BufferedReader reader = new BufferedReader(new FileReader(theChosenOne));//Creates new reader to read the selected file
                Scanner dataReader = new Scanner(reader);
                //Gets rid of all white spaces (unless they are within quotes), method found from: https://stackoverflow.com/a/15633284
                String regex = "\\s+(?=((\\\\[\\\\\"]|[^\\\\\"])*\"(\\\\[\\\\\"]|[^\\\\\"])*\")*(\\\\[\\\\\"]|[^\\\\\"])*$)";
                //Loops while there are lines to be read
                while (dataReader.hasNext()) {
                    //Grab each line from the file, strip it of white space (except in quotes) then add the line to components
                    String incomingLine = dataReader.nextLine().replaceAll(regex, "");
                    components.add(incomingLine);
                }
            }
            if (components.isEmpty()) {
                System.out.println("!!!File was not chosen or was blank");//Prints the exception message
            }
        } catch (FileNotFoundException e) {
            System.out.println("Problem with file \n" + e);//Prints the exception message
        }
    }

    private void parseGUI() {
        //If the file is not empty (user closed out of file chooser), then try to parse components
        if (!components.isEmpty()) {
            //If the last line is not "End.", then don't bother trying to parse anything else.
            if (components.get(components.size() - 1).equals("End.")) {
                //If window parses, then see if widgets parse.
                if (parseWindow()) {
                    //Widgets parse everything except for the window and "End." line
                    if (parseWidgets()) {
                        //Checks to make sure there aren't more components started then were finished (or vice versa)
                        if (startedComponents == finishedComponents) {
                            //If window and widgets parsed and the current (last) line is "End." then it parsed successfully.
                            if (components.get(currentComponent).equals("End.")) {
                                frame.setLocationRelativeTo(null);
                                frame.setVisible(true);
                                System.out.println("-----GUI has been succesfully parsed-----");
                                return;
                            }
                        }
                    }
                }
            }
        }
        System.out.println("!!!Failed to parse GUI");
    }

    //Used in parseWindow (dimensions), parseWidget (textfield size), and parseLayout (grid dimensions)
    private boolean isNumber(String number) {
        // All numerical characters to compare incoming string to
        String validNumbers = "012345789";
        //Split incoming string into string array of indivual characters
        String[] splitCharacters = number.split("");
        //Run through array to check each character
        for (int i = 0; i < splitCharacters.length; i++) {
            if (splitCharacters[i].isBlank()) {
                //If current character is blank, it is a not a number
                return false;
            } else if (!validNumbers.contains(splitCharacters[i])) {
                //If current character is not a number, then the whole string is not a number
                return false;
            }
        }
        return true;
    }

    //Checks the first line of the file to make sure it meets the grammar for a valid window.
    private boolean parseWindow() {
        //Grabs the current component (0 since its the first) and tries to parse it
        String win = components.get(currentComponent);
        //Saves a few characters
        int length = win.length();
        //If the string is big enough, and the first 8 characters are equal to 'Window"' then proceed.
        if (length > 8 && win.substring(0, 7).equals("Window\"")) {
            int indexTracker = 0; //Keeps track of where proceeding the for loops left off
            String title = ""; //Hold's a null value
            int width = 0; //Width of window
            int height = 0; //Height of window
            //Turns the string into an array of its characters
            String[] splitExpression = win.split("");
            //Starting at 8 cause that's the character after the quotation marks
            for (int i = 7; i < length; i++) {
                //If the current character is '"' then that means title is over.
                if (splitExpression[i].equals("\"")) {
                    //If title is over then set index to i for next loop and break
                    indexTracker = i;
                    System.out.println("---Parsed window title");
                    break;
                } else {
                    //If it is still being parsed, add current character to title
                    title += splitExpression[i];
                }
            }

            //The next thing to parse is the size, "(" should be two characters after the last '"' from the title
            if (splitExpression[indexTracker + 1].equals("(")) {
                //Keeps track of wether or not the number should go to width or height
                int parsedNumbers = 0;
                //"Null" value to add width and height to.
                String number = "";
                //Start the loop 3 characters after the last '"' (should be the first character in the parentheses.
                for (int i = indexTracker + 2; i < splitExpression.length; i++) {
                    //If the current character is not a comma, a blank space, or a right parenthesis, attempt to parse
                    if (!splitExpression[i].equals(",") && !splitExpression[i].isBlank() && !splitExpression[i].equals(")")) {
                        //If the current character is not a number, then return false and notify user
                        if (!isNumber(splitExpression[i])) {
                            System.out.println("!!!Failed to parse window width/height (invalid characters)");//Prints the exception message
                            break;
                        }
                        //If false is not returned, then add current character to number
                        number += splitExpression[i];

                        //If number isn't blank, then try to parse width/height
                    } else if (!number.isBlank()) {
                        //If there is a comma and no numbers have been parsed, add number to width and reset value.
                        if (splitExpression[i].equals(",") && parsedNumbers == 0) {
                            width = Integer.parseInt(number);
                            //Reset value of number
                            number = "";
                            //Increase count so the next number gets added to height
                            parsedNumbers++;
                        } else if (parsedNumbers == 1 && splitExpression[i].equals(")")) {
                            //If the current character is ")" and width has been parsed, parse height
                            height = Integer.parseInt(number);
                            //Set value of index for next loop and i to length to breakout early
                            indexTracker = i;
                            i = splitExpression.length;
                            System.out.println("---Parsed window size");
                        }
                    } else {
                        //If the character does not equal "(" then it is in an invalid format
                        System.out.println("!!!Failed to parse window size");//Prints the exception message
                        break;
                    }
                    //If none of these variables are null, then they have all been parsed to create window
                    if (indexTracker != 0 && !title.equals("") && width != 0 && height != 0) {
                        //Set boolean to true to track where things should be added.
                        isFrame = true;
                        //Creates a new frame with title, wdith, height, and to exit on close
                        frame = new JFrame(title);
                        frame.setSize(width, height);
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        System.out.println("---Window parsed successfully");
                        //If the character after the indextracker (which should have ended with ")" is L (as in layout), then proceed
                        if (splitExpression[indexTracker + 1].equals("L")) {
                            //Create substring of "LayoutLAYOUTHERE)
                            String layout = win.substring(indexTracker + 1, length);
                            //If the substrings first 6 characters are Layout, then send the characters after to parseLayout()
                            if (layout.substring(0, 6).equals("Layout")) {
                                //See if parseLayout can parse the characters after Layout
                                if (parseLayout(layout.substring(6, layout.length()))) {
                                    return true;
                                }
                            }
                        }
                    }//End if to parse layout
                }//End dimension parse if
            }//End title parse if
        } //End = Window if
        System.out.println("!!!Failed to parse window");
        return false;
    } //End parseWindow

    //Is sent string from parseWindow and parseWidget to analyze and parse
    private boolean parseLayout(String lay) {
        int length = lay.length();
        //If the layout sent = "Flow:" add the layout to frame or panel depending on isFrame
        if (lay.equals("Flow:")) {
            if (isFrame) {
                System.out.println("---Parsed flow layout for frame");
                frame.setLayout(new FlowLayout());
            } else {
                System.out.println("---Parsed flow layout for panel");
                panel.setLayout(new FlowLayout());
            }
            //On sucessful fowparse, increment currentComponent and return true to break out.
            currentComponent++;
            return true;

            //If the layout is "Grid(" and the last two characters are"):" then attempt to parse dimensions
        } else if (length > 5 && lay.substring(0, 5).equals("Grid(") && lay.substring(length - 2, length).equals("):")) {
            //Create a substring starting with the first character after "(" and ending with ")"
            String[] splitExpression = lay.substring(5, length - 1).split("");
            //String holds value of number for parsing, and int keeps track of parsed numbers
            String number = "";
            int parsedNumbers = 0;
            //These values will equal to number depending on the circumstances.
            int rows = 0;
            int cols = 0;
            int hgap = 0;
            int vgap = 0;
            for (int i = 0; i < splitExpression.length; i++) {
                //If the current characer is anything other than a comma, blank space, or ")" then add it to number.
                if (!splitExpression[i].equals(",") && !splitExpression[i].isBlank() && !splitExpression[i].equals(")")) {
                    //If it isn't a number, then break and notify user.
                    if (!isNumber(splitExpression[i])) {
                        System.out.println("!!!Failed to parse window width/height (invalid characters)");//Prints the exception message
                        break;
                    }
                    //If current character is a number, then add it to number
                    number += splitExpression[i];

                    //If the number is not blank (reset value or no first number), a comma, or ")" try to parse
                } else if (!number.isBlank() && (splitExpression[i].equals(",") || splitExpression[i].equals(")"))) {
                    //If no numbers have been parsed, then parse number to row
                    if (parsedNumbers == 0) {
                        rows = Integer.parseInt(number);
                    } else if (parsedNumbers == 1) {
                        //If row has been parsed, add number to column. If current char is ")" then stop parsing.
                        cols = Integer.parseInt(number);
                        if (splitExpression[i].equals(")")) {
                            break;
                        }
                    } else if (parsedNumbers == 2) {
                        //If row and column have been parsed, add number to horizontal gap
                        hgap = Integer.parseInt(number);
                    } else if (parsedNumbers == 3 && splitExpression[i].equals(")")) {
                        //If all others were parsed and the current character is ")" then parse last number and break
                        vgap = Integer.parseInt(number);
                        break;
                    }
                    //Increase parsed numbers and reset value of number after each successful parse.
                    parsedNumbers++;
                    number = "";
                }
            }
            //Add layout to frame if isFrame is true
            if (isFrame) {
                //If only rows and columns have been changed, create a Gridlayout(rows, cols)
                if (rows > 0 && cols > 0 && hgap == 0 && vgap == 0) {
                    frame.setLayout(new GridLayout(rows, cols));
                    System.out.println("---Parsed grid layout for frame. (" + rows + ", " + cols + ") No gaps.");

                } else if (rows > 0 && cols > 0 && hgap > 0 && vgap > 0) {
                    //If all values were changed, create a Gridlayout(rows, cols, vgap, hgap)
                    frame.setLayout(new GridLayout(rows, cols, hgap, vgap));
                    System.out.println("---Parsed grid layout for frame. "
                            + "(" + rows + ", " + cols + ", " + vgap + ", " + hgap + ")");

                } else {
                    System.out.println("!!!Failed to parse grid layout for panel");
                    return false;
                }
            } else {
                //If only rows and columns have been changed, create a Gridlayout(rows, cols)
                if (rows > 0 && cols > 0 && hgap == 0 && vgap == 0) {
                    panel.setLayout(new GridLayout(rows, cols));
                    System.out.println("---Parsed grid layout for panel. "
                            + "(" + rows + ", " + cols + ") No gaps.");

                } else if (rows > 0 && cols > 0 && hgap > 0 && vgap > 0) {
                    //If all values were changed, create a Gridlayout(rows, cols, vgap, hgap)
                    panel.setLayout(new GridLayout(rows, cols, hgap, vgap));
                    System.out.println("---Parsed grid layout for panel. "
                            + "(" + rows + ", " + cols + ", " + vgap + ", " + hgap + ")");
                } else {
                    System.out.println("!!!Failed to parse grid layout for panel");
                    return false;
                }
            }
            //If parse was sucessful, return true and increase components
            currentComponent++;
            return true;
        }
        System.out.println("!!Failed to parse layout");//Prints the exception message
        return false;
    }//End parseLayout

    private boolean parseWidget() {
        //Grabs the current component and tries to parse it
        String widg = components.get(currentComponent);
        // Size of string, Easier than typing widg.length() out everytime
        int length = widg.length();
        //If the first 7 characters of the current string are 'Button"' and the last 2 are '";', parse button
        if (length > 8 && widg.substring(0, 7).equals("Button\"") && widg.substring(length - 2, length).equals("\";")) {
            //The name starts at 7 (character after '"' and stops at the last two (";)
            String buttonName = widg.substring(7, length - 2);
            //Create a new button object with name
            button = new JButton(buttonName);
            //Add it to frame or button
            if (isFrame) {
                frame.add(button);
            } else {
                panel.add(button);
            }
            //Increase currentComponent and notify user of success
            currentComponent++;
            System.out.println("---Parsed button \"" + buttonName + "\"");
            return true;

            //If the first 6 characters of the current string are 'Label"' and the last two are '";', parse label
        } else if (length > 7 && widg.substring(0, 6).equals("Label\"") && widg.substring(length - 2, length).equals("\";")) {
            //The name starts at 6 (character after '"' and stops at the last two (";)
            String labelName = widg.substring(6, length - 2);
            //Create a new label object with name
            label = new JLabel(labelName);
            //Add it to frame or button
            if (isFrame) {
                frame.add(label);
            } else {
                panel.add(label);
            }
            //Increase currentComponent and notify user of success
            currentComponent++;
            System.out.println("---Parsed label \"" + labelName + "\"");
            return true;

            //If the first 11 characters of the current string are "PanelLayout" create panel and parse layout
        } else if (length > 14 && widg.substring(0, 11).equals("PanelLayout")) {
            //Keeps track of how many panels have been started
            startedComponents++;
            //Add it to frame or button
            if (isFrame) {
                frame.add(panel = new JPanel());
                //Once a panel has been added to the frame, everything else will now go to the panel.
                isFrame = false;
            } else {
                panel.add(panel = new JPanel());
            }

            //Send the string after "PanelLayout" to parseLayout() to get parsed
            if (parseLayout(widg.substring(11, length))) {
                System.out.println("---Parsed panel");

                //currentComponent is increase in parseLayout() so no need to increment it here
                return true;
            } else {
                System.out.println("!!!Failed to parse panel");
                return false;
            }

            //If the first 9  characters of the current string are "Textfield" and the last 1 is ';', parse texfield
        } else if (length > 11 && widg.substring(0, 9).equals("Textfield") && widg.substring(length - 1, length).equals(";")) {
            //The size starts at 9 (character after 'Textfield' and stops after ";", if it is  a number than proceed
            if (isNumber(widg.substring(9, length - 1))) {
                //If it is a number, then parse it
                int textFieldSize = Integer.parseInt(widg.substring(9, length - 1));
                //Create a new textfield from parsed int
                text = new JTextField(textFieldSize);
                //Add it to frame or button
                if (isFrame) {
                    frame.add(text);
                } else {
                    panel.add(text);
                }
                //Increase currentComponent and notify user of success
                currentComponent++;
                System.out.println("---Parsed textfield. Size: " + textFieldSize);
                return true;
            } else {
                System.out.println("!!!Textfield parse failed, size was not a number");
            }

            //If the string is Group, parse radio button group
        } else if (widg.equals("Group")) {
            //Keeps track of how many buton groups have been started
            startedComponents++;
            radioButtonGroup = new ButtonGroup();
            //Increase currentComponent so parseRadioButtons starts at next component
            currentComponent++;
            if (parseRadioButtons()) {
                //If all radio buttons parsed then notify user and return true.
                System.out.println("---Parsed radio button(s)");
                return true;
            }

            //If the string is End;, try to parse widget one more time in case of nested panels
        } else if (widg.equals("End;")) {
            //If current line =  "End;" then that means the current panel is finished
            finishedComponents++;
            //Increase current component so parseWidget checks the next component
            currentComponent++;
            if (parseWidget()) {
                //There are more widgets to parse, so return true and parse again.
                return true;
            } else {
                //If parseWidgets() is false, then all widgets have been parsed so return false at end
                System.out.println("---Parsed widgets");
            }
        }
        return false;
    }//End parseWidget

    /* Method borrowed from Barrett Otte (lines 353-361): 
    https://github.com/barrettotte/Recursive-Descent-GUI-Parser/blob/master/GUIParser.java 
    While parseWidget returns true (there are widgets to be parsed) keep parsing.
    It returns false when all widgets were parsed, or one can't be parsed*/
    private boolean parseWidgets() {
        if (parseWidget()) {
            if (parseWidgets()) {
                return true;
            }
            return true;
        }
        return false;
    }

    //Retrieves currentComponent and attempts to parse it as a radio button. Redirected here from parseWidgets parse group option.
    private boolean parseRadioButton() {
        String radButton = components.get(currentComponent);
        int sz = radButton.length();
        //If the first 6 characters are 'Radio"' and the last 2 are '";' then parse button
        if (sz > 7 && radButton.substring(0, 6).equals("Radio\"") && radButton.substring(sz - 2, sz).equals("\";")) {
            //String for button name is in between quotation marks
            String buttonName = radButton.substring(6, sz - 2);
            //Create a new button with button name
            radioButton = new JRadioButton(buttonName);
            //Add button to button group
            radioButtonGroup.add(radioButton);
            //Add to frame or panel
            if (isFrame) {
                frame.add(radioButton);
            } else {
                panel.add(radioButton);
            }
            //Increase components for next method
            currentComponent++;
            System.out.println("---Parsed radio button \"" + buttonName + "\"");
            return true;
        }
        return false;
    }

    //Same as parseWidgets(), returns false when all widgets were parsed, or one can't be parsed.
    private boolean parseRadioButtons() {
        if (parseRadioButton()) {
            if (parseRadioButtons()) {
                return true;
            }
            return true;
        }
        return false;
    }

    //Creates a new project object that opens a file and parses.
    public static void main(String[] args) {
        Project1 project = new Project1();
    }
}
