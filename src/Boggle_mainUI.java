import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Boggle_mainUI
{
    public static void main(String[] args) throws IOException {

        Scanner sc= new Scanner(System.in); //For reading the user's command from the console
        String userCommand="";

        //Display the commands that're available to users
        String getDictionaryCommand = "dt";
        String getPuzzleCommand = "puzzle";
        String solveCommand = "solve";
        String printCommand = "print";
        String quitCommand = "quit";

        System.out.println("Commands available:");
        System.out.println("  " + getDictionaryCommand);
        System.out.println("  " + getPuzzleCommand);
        System.out.println("  " + solveCommand);
        System.out.println("  " + printCommand);
        System.out.println("  " + quitCommand);
        Boggle game = new Boggle();

        do
        {
            /* Accept the User's command */
            System.out.println("Enter a command to Boggle!");
            userCommand=sc.nextLine(); //Read the user's command from the console

            /* Accept a list of words and add it to the game */
            if (userCommand.equalsIgnoreCase(getDictionaryCommand)) //If user wants to input a list of words to the game
            {
                FileReader input = new FileReader("C:\\Users\\welcome\\Desktop\\Dal Coursework\\CSCI 3901\\Assignments\\Assignment 4\\benny\\src\\Dictionary.txt");
                boolean result;
                try {
                    BufferedReader stream = new BufferedReader(input);
                    result = game.getDictionary(stream);
                    if (result)
                        System.out.println("Dictionary has been added to Boggle!");
                    else
                        System.out.println("Failed to add the Dictionary to Boggle");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            /* Accept a puzzle of letters and add it to the game */
            else if (userCommand.equalsIgnoreCase(getPuzzleCommand))
            {

                boolean result;
                try {
                    FileReader inputPuzzle = new FileReader("C:\\Users\\welcome\\Desktop\\Dal Coursework\\CSCI 3901\\Assignments\\Assignment 4\\benny\\src\\Puzzle.txt");
                    BufferedReader stream = new BufferedReader(inputPuzzle);
                    result = game.getPuzzle(stream);
                    if (result)
                        System.out.println("A new Puzzle has been added to Boggle!");
                    else
                        System.out.println("Failed to add the new Puzzle to Boggle!");
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

        }

            /* Solve the puzzle */
            else if (userCommand.equalsIgnoreCase(solveCommand))
            {
                System.out.println("Solving the puzzle!");
                List<String> solution = game.solve(); //Obtain the list of words that can be found in the puzzle
                for(String word:solution)
                {
                    System.out.println(word); //Print the word and the path traversed to obtain the word
                }
            }

            /* Print the Puzzle */
            else if (userCommand.equalsIgnoreCase(printCommand))
            {
                System.out.println("**********PUZZLE**********");
                System.out.println(game.print());
                System.out.println("**********PUZZLE**********");

            }

            else if (userCommand.equalsIgnoreCase("quit"))
            {
                System.out.println("Quiting the game");
            }

            else
            {
                System.out.println("Bad Command: " + userCommand);
            }

        }while(!userCommand.equalsIgnoreCase("quit"));

    }
}
