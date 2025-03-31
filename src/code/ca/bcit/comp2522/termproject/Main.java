package ca.bcit.comp2522.termproject;

import ca.bcit.comp2522.termproject.numbergame.NumberGameMain;
import ca.bcit.comp2522.termproject.twistedwordle.TwistedWordle;
import ca.bcit.comp2522.termproject.wordgame.WordGame;
import javafx.application.Platform;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * The main class for Cole Campbell's 2522 term project.
 * Handles the main menu and launching different games.
 * Ensures JavaFX platform is managed correctly for reusable stages.
 *
 * @author colecampbell
 * @version 1.0
 */
public class Main
{
    private static final Scanner input               = new Scanner(System.in);
    private static final char    WORD_GAME           = 'W';
    private static final char    NUMBER_GAME         = 'N';
    private static final char    TWISTED_WORDLE_GAME = 'T';
    private static final char    QUIT                = 'Q';
    private static final int     FIRST_INDEX         = 0;

    /**
     * The entry point for the JVM.
     *
     * @param args unused
     */
    public static void main(final String[] args)
    {
        initializeJavaFX();

        System.out.println("Welcome to Cole's comp2522 term project!");
        System.out.println("----------------------------------------");

        boolean keepRunning;
        keepRunning = true;

        while (keepRunning)
        {
            printMenu();

            final String line;
            line = input.nextLine().trim();

            if (line.isEmpty())
            {
                // if the user didn't input anything, re-prompt
                continue;
            }

            final char choice;
            choice = line.toUpperCase().charAt(FIRST_INDEX); // Get first char

            switch (choice)
            {
                case WORD_GAME:
                    playWordGame();
                    break;
                case NUMBER_GAME:
                    playNumberGame();
                    break;
                case TWISTED_WORDLE_GAME:
                    playTwistedWordleGame();
                    break;
                case QUIT:
                    System.out.println("Thank you for playing! Shutting down...");
                    // Cleanly shut down the JavaFX platform
                    Platform.exit();
                    // Close the scanner resource
                    input.close();
                    // Exit the application
                    keepRunning = false; // Exit loop
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice '" +
                                       choice +
                                       "', please try again.");
                    break;
            }

            if (keepRunning)
            {
                System.out.println("\nPress Enter to return to the main menu...");
                input.nextLine(); // Consume the enter press
            }
        }
    }

    /* Initializes the UI for all the games. */
    private static void initializeJavaFX()
    {
        try
        {
            // Startup the JavaFX platform. The lambda runs after initialization.
            Platform.startup(() ->
                             {
                                 System.out.println("JavaFX Platform initialized successfully.");
                             });

            // IMPORTANT: Prevent JavaFX from exiting when the last window is closed.
            // This allows us to open new game windows later.
            Platform.setImplicitExit(false);

        } catch (final IllegalStateException e)
        {
            // This usually means JavaFX is already running (e.g., launched elsewhere).
            System.out.println("JavaFX Platform already running.");
            // Ensure implicit exit is still false if it was already running
            if (Platform.isImplicitExit())
            {
                Platform.setImplicitExit(false);
            }
        } catch (final Exception e)
        {
            System.err.println("Failed to initialize JavaFX Platform!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /* Prints the game selection menu. */
    private static void printMenu()
    {
        System.out.println("\n--- Main Menu ---");
        System.out.println("Please choose which game you would like to play:");
        System.out.println("Word Game (W)");
        System.out.println("Number Game (N)");
        System.out.println("Twisted Wordle Game (T)");
        System.out.println("Quit (Q)");
        System.out.print("Your choice: ");
    }

    /* Launches the word game. */
    private static void playWordGame()
    {
        try
        {
            System.out.println("\nStarting the Word game...");
            WordGame.play();
            System.out.println("Word game finished.");
        } catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    /* Launches the number game. */
    private static void playNumberGame()
    {
        System.out.println("\nLaunching Number Game...");

        // pause the main thread until the number game is done
        final CountDownLatch gameCloseLatch;
        gameCloseLatch = new CountDownLatch(1);

        try
        {
            NumberGameMain.launchGame(gameCloseLatch::countDown);

            gameCloseLatch.await();

        } catch (final InterruptedException e)
        {
            Thread.currentThread().interrupt();
        } catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    /* Launches the wordle game. */
    private static void playTwistedWordleGame()
    {
        System.out.println("\nSetting up Twisted Wordle Game...");

        boolean setupSuccessful;
        setupSuccessful = TwistedWordle.setupGameFromConsole();

        if (setupSuccessful)
        {
            // pause the main thread until the wordle game is done
            final CountDownLatch gameCloseLatch;
            gameCloseLatch = new CountDownLatch(1);

            try
            {
                TwistedWordle.launchGame(gameCloseLatch::countDown);
                gameCloseLatch.await();

            } catch (final InterruptedException e)
            {
                Thread.currentThread().interrupt();
            } catch (final Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            System.err.println("Failed to set up Twisted Wordle.");
        }
        System.out.println("Returning to main menu...");
    }
}
