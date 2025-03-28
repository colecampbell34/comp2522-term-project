package ca.bcit.comp2522.termproject;

import ca.bcit.comp2522.termproject.numbergame.NumberGameMain;
import ca.bcit.comp2522.termproject.twistedwordle.TwistedWordle; // Use the launcher class
import ca.bcit.comp2522.termproject.wordgame.WordGame; // Assuming console game
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
 * @version 1.2 - Refined JavaFX launching and waiting
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
        // Initialize JavaFX toolkit ONCE at the very beginning.
        initializeJavaFX();

        System.out.println("Welcome to Cole's comp2522 term project!");
        System.out.println("----------------------------------------");

        boolean keepRunning = true;
        while (keepRunning)
        {
            printMenu();
            final String line = input.nextLine().trim();
            if (line.isEmpty())
            {
                continue;
            }
            char choice = line.toUpperCase().charAt(FIRST_INDEX); // Get first char

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
                    System.out.println("Invalid choice '" + choice + "', please try again.");
                    break;
            }
            // Add a small pause or prompt before showing the menu again after a game
            if (keepRunning && choice != QUIT)
            {
                System.out.println("\nPress Enter to return to the main menu...");
                input.nextLine(); // Consume the enter press
            }
        }
        // Optional: Explicit exit if Platform.exit() doesn't terminate JVM (e.g., non-daemon threads still running)
        // System.exit(0);
    }

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
            System.out.println("JavaFX implicit exit disabled.");

        } catch (final IllegalStateException e)
        {
            // This usually means JavaFX is already running (e.g., launched elsewhere).
            System.out.println("JavaFX Platform already running.");
            // Ensure implicit exit is still false if it was already running
            if (Platform.isImplicitExit())
            {
                Platform.setImplicitExit(false);
                System.out.println("Ensured JavaFX implicit exit is disabled.");
            }
        } catch (final Exception e)
        {
            System.err.println("CRITICAL: Failed to initialize JavaFX Platform!");
            e.printStackTrace();
            // Exit if JavaFX can't start, as GUI games won't work.
            System.exit(1);
        }
    }

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

    // Assuming WordGame is a console-based game or handles its own lifecycle
    private static void playWordGame()
    {
        try
        {
            System.out.println("\nStarting the Word game...");
            WordGame.play();
            System.out.println("Word game finished.");
        } catch (final IOException e)
        {
            System.err.println("Error running Word game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void playNumberGame()
    {
        System.out.println("\nLaunching Number Game...");

        final CountDownLatch gameCloseLatch;
        gameCloseLatch = new CountDownLatch(1);

        try
        {
            // Call the static launcher, passing the action to perform on close (counting down the latch)
            NumberGameMain.launchGame(gameCloseLatch::countDown);

            // Wait here until gameCloseLatch.countDown() is called from the JavaFX thread.
            gameCloseLatch.await();

        } catch (final InterruptedException e)
        {
            System.err.println("Main thread interrupted while waiting for Number Game.");
            Thread.currentThread().interrupt(); // Restore interrupt status
        } catch (final Exception e)
        {
            System.err.println("An error occurred while launching or waiting for the Number Game:");
            e.printStackTrace();
        }
    }

    private static void playTwistedWordleGame()
    {
        System.out.println("\nLaunching Twisted Wordle Game...");

        // Latch to make the main thread wait until the game window is closed.
        final CountDownLatch gameCloseLatch;
        gameCloseLatch = new CountDownLatch(1);

        try
        {
            // Call the static launcher, passing the action to perform on close
            TwistedWordle.launchGame(gameCloseLatch::countDown);

            // Wait here until gameCloseLatch.countDown() is called from the JavaFX thread.
            gameCloseLatch.await();

        } catch (final InterruptedException e)
        {
            System.err.println("Main thread interrupted while waiting for Twisted Wordle.");
            Thread.currentThread().interrupt(); // Restore interrupt status
        } catch (final Exception e)
        {
            System.err.println("An error occurred while launching or waiting for Twisted Wordle:");
            e.printStackTrace();
        }
    }
}
