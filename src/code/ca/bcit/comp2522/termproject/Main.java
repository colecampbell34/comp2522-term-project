package ca.bcit.comp2522.termproject;

import ca.bcit.comp2522.termproject.numbergame.NumberGameMain;
import ca.bcit.comp2522.termproject.twistedwordle.TwistedWordle;
import ca.bcit.comp2522.termproject.wordgame.WordGame;
import javafx.application.Platform;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The main class for Cole Campbell's 2522 term project.
 *
 * @author colecampbell
 * @version 1.0
 */
public class Main
{
    private static final int           FIRST_CHAR  = 0;
    private static final AtomicBoolean gameRunning = new AtomicBoolean(false);
    private static final Scanner       input       = new Scanner(System.in);

    public static void main(String[] args)
    {
        Platform.startup(() ->
                         {
                         });
        Platform.setImplicitExit(false);

        System.out.println("Welcome to Cole's comp2522 term project!");
        System.out.println("----------------------------------------");

        new Thread(() ->
                   {
                       while (true)
                       {
                           if (!gameRunning.get())
                           {
                               printMenu();
                               char userChoice = input.next().charAt(FIRST_CHAR);
                               userChoice = Character.toUpperCase(userChoice);

                               switch (userChoice)
                               {
                                   case 'W' -> playWordGame();
                                   case 'N' -> playNumberGame();
                                   case 'T' -> playTwistedWordleGame();
                                   case 'Q' ->
                                   {
                                       System.out.println("Thank you for playing! Goodbye!");
                                       System.exit(0);
                                   }
                                   default -> System.out.println("Invalid choice. Please try again.");
                               }
                           }
                           else
                           {
                               try
                               {
                                   Thread.sleep(100);
                               } catch (InterruptedException e)
                               {
                                   Thread.currentThread().interrupt();
                               }
                           }
                       }
                   }).start();
    }

    private static void printMenu()
    {
        System.out.println("\nPlease choose which game you would like to play:");
        System.out.println("Press W to play the Word game.");
        System.out.println("Press N to play the Number game.");
        System.out.println("Press T to play the Twisted Wordle game.");
        System.out.println("Press Q to quit.");
    }

    private static void playWordGame()
    {
        try
        {
            System.out.println("Starting the Word game...");
            WordGame.play();
        } catch (IOException e)
        {
            System.err.println("Error starting Word game: " + e.getMessage());
        }
    }

    private static void playNumberGame()
    {
        System.out.println("Starting the Number game...");
        gameRunning.set(true);
        NumberGameMain.launchGame(() -> gameRunning.set(false));
    }

    private static void playTwistedWordleGame()
    {
        System.out.println("Starting the Multiplayer Wordle game...");
        gameRunning.set(true);
        TwistedWordle.launchGame(() -> gameRunning.set(false));
    }
}