package ca.bcit.comp2522.termproject;

import ca.bcit.comp2522.termproject.twistedwordle.TwistedWordle;
import ca.bcit.comp2522.termproject.wordgame.WordGame;
import java.io.IOException;
import java.util.Scanner;

/**
 * This is the entry point of the program.
 *
 * @author colecampbell
 * @version 1.0
 */
public class Main
{
    private static final int FIRST_CHAR = 0;

    public static void main(final String[] args) throws IOException
    {
        Scanner input = new Scanner(System.in);

        char userChoice;

        System.out.println("Welcome to Cole's comp2522 term project!");
        System.out.println("----------------------------------------");

        do
        {
            printMenu();

            userChoice = input.next().charAt(FIRST_CHAR);
            userChoice = Character.toUpperCase(userChoice);

            switch (userChoice)
            {
                case 'W' -> playWordGame();
                case 'N' -> playNumberGame();
                case 'T' -> playTwistedWordleGame();
                case 'Q' -> System.out.println("Thank you for playing! Goodbye!");
                default -> System.out.println("Invalid choice. Please try again.");
            }
        } while (userChoice != 'Q');

        input.close();
    }

    /*
     * Prints the menu options.
     */
    private static void printMenu()
    {
        System.out.println("\nPlease choose which game you would like to play:");
        System.out.println("Press W to play the Word game.");
        System.out.println("Press N to play the Number game.");
        System.out.println("Press T to play the Twisted Wordle game.");
        System.out.println("Press Q to quit.");
    }

    /*
     * Simulates playing the Word game.
     */
    private static void playWordGame() throws IOException
    {
        System.out.println("Starting the Word game...");
        WordGame.play();
    }

    /*
     * Simulates playing the Number game.
     */
    private static void playNumberGame()
    {
        System.out.println("Starting the Number game...\n(Placeholder functionality)");
        // Add logic for the Number game here.
    }

    /*
     * Simulates playing the Twisted Wordle game.
     */
    private static void playTwistedWordleGame()
    {
        System.out.println("Starting the Multiplayer Wordle game...");
        TwistedWordle.main(null);
    }
}
