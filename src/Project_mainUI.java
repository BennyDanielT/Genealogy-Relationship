import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;

public class Project_mainUI
{
    public static void main(String[] args) throws IOException {
        Genealogy tree = new Genealogy();

        Scanner sc= new Scanner(System.in); //For reading the user's command from the console
        String userCommand="";

        //Display the commands that're available to users
        String addPerson = "person";
        String addFile = "file";
        String addPersonAttributes = "details";
        String addFileAttributes = "properties";
        String quitCommand = "quit";

        System.out.println("Commands available:");
        System.out.println("  " + addPerson);
        System.out.println("  " + addFile);
        System.out.println("  " + addPersonAttributes);
        System.out.println("  " + addFileAttributes);
        System.out.println("  " + quitCommand);
        Boggle game = new Boggle();

        do
        {
            /* Accept the User's command */
            System.out.println("Enter a command!");
            userCommand=sc.nextLine(); //Read the user's command from the console

            /* Accept a list of words and add it to the game */
            if (userCommand.equalsIgnoreCase(addPerson)) //If user wants to input a list of words to the game
            {
                System.out.println("Enter the name of the person to be added to the tree");
                String name = sc.nextLine();
                tree.addPerson(name);
            }

            /* Accept a puzzle of letters and add it to the game */
            else if (userCommand.equalsIgnoreCase(addFile))
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
            else if (userCommand.equalsIgnoreCase(addPersonAttributes))
            {
                System.out.println("Solving the puzzle!");
                List<String> solution = game.solve(); //Obtain the list of words that can be found in the puzzle
                for(String word:solution)
                {
                    System.out.println(word); //Print the word and the path traversed to obtain the word
                }
            }

            /* Print the Puzzle */
            else if (userCommand.equalsIgnoreCase(addFileAttributes))
            {
                System.out.println("**********Family Tree**********");
                System.out.println(game.print());
                System.out.println("**********Family Tree**********");

            }

            else if (userCommand.equalsIgnoreCase("quit"))
            {
                System.out.println("Bye Now...");
            }

            else
            {
                System.out.println("Bad Command: " + userCommand);
            }

        }while(!userCommand.equalsIgnoreCase("quit"));

    }
}
