package ca.bcit.comp2522.termproject;

import ca.bcit.comp2522.termproject.numbergame.NumberGameMain;
import ca.bcit.comp2522.termproject.twistedwordle.TwistedWordle;
import ca.bcit.comp2522.termproject.wordgame.WordGame;
import javafx.application.Platform;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * The main class that controls the entry point of the term project application.
 * It presents the user with a menu to choose from multiple games and launches the selected one.
 * Also handles JavaFX platform initialization to support UI elements used by the games.
 * This class ensures that the platform remains active throughout the program's lifecycle,
 * allowing users to launch different JavaFX-based games one after another without crashing.
 *
 * Games available:
 * - Word Game
 * - Number Game (JavaFX-based)
 * - Twisted Wordle (JavaFX-based with console-based setup)
 *
 * The class handles graceful shutdown and resource cleanup.
 *
 * @author colecampbell
 * @version 1.0
 */
public final class Main
{
    private static final Scanner input               = new Scanner(System.in);
    private static final char    WORD_GAME           = 'W';
    private static final char    NUMBER_GAME         = 'N';
    private static final char    TWISTED_WORDLE_GAME = 'T';
    private static final char    QUIT                = 'Q';
    private static final int     FIRST_INDEX         = 0;
    private static final int     EXIT_STATUS         = 1;
    private static final int     COUNTDOWN           = 1;

    /**
     * Entry point for the application. Initializes JavaFX and
     * displays a game selection menu to the user.
     * Based on the user's choice, it launches the corresponding game
     * and waits for the game session to end before returning to the menu.
     * Continues this cycle until the user chooses to quit, at which point
     * it gracefully shuts down JavaFX and closes system resources.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(final String[] args)
    {
        initializeJavaFX();

        System.out.println("Welcome to Cole's comp2522 term project!");
        System.out.println("----------------------------------------");

        boolean running;
        running = true;

        while (running)
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
            choice = line.toUpperCase().charAt(FIRST_INDEX);

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
                    running = false; // Exit loop
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice '" +
                                       choice +
                                       "', please try again.");
                    break;
            }

            if (running)
            {
                System.out.println("\nPress Enter to return to the main menu...");
                input.nextLine(); // Consume the enter press
            }
        }
    }

    /*
     * Initializes the JavaFX platform required for launching JavaFX-based games.
     * Ensures that the platform remains active even after a game window is closed,
     * allowing users to launch multiple games in one session without reinitialization.
     * Handles edge cases where JavaFX is already running to prevent crashes.
     */
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
            System.exit(EXIT_STATUS);
        }
    }

    /*
     * Displays the main menu to the user with options to play different games or quit the application.
     * Prompts the user for input corresponding to a specific game selection or quit command.
     */
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

    /*
     * Attempts to start the Word Game by invoking its play method.
     * Catches and prints any IOExceptions that occur during game setup or execution.
     */
    private static void playWordGame()
    {
        try
        {
            System.out.println("\nStarting the Word game...");
            WordGame.play();
        } catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    /*
     * Launches the Number Game, a JavaFX-based game.
     * Uses a CountDownLatch to pause the main thread until the game window is closed by the user.
     * Handles potential exceptions and interruptions during game execution.
     */
    private static void playNumberGame()
    {
        System.out.println("\nLaunching Number Game...");

        // pause the main thread until the number game is done
        final CountDownLatch gameCloseLatch;
        gameCloseLatch = new CountDownLatch(COUNTDOWN);

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

    /*
     * Launches the Twisted Wordle game after setting it up through console inputs.
     * If setup is successful, starts the JavaFX game and waits until the user closes the window.
     * Logs an error message if the game setup fails or if an exception occurs during launch.
     */
    private static void playTwistedWordleGame()
    {
        System.out.println("\nLaunching Twisted Wordle Game...");

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
            System.err.println("Failed to set up Twisted Wordle from the console.");
        }
    }
}
