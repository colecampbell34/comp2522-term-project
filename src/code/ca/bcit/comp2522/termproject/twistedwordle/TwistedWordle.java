package ca.bcit.comp2522.termproject.twistedwordle;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the main application class for the Twisted Wordle game.
 * This class handles the game setup via the console, manages the JavaFX GUI,
 * controls the game flow between players, tracks rounds, times turns, and calculates scores.
 * <p>
 * This class provides the following functionality:
 * - Console-based setup for player names and word selection.
 * - Loading and validation of a word list from a file.
 * - JavaFX GUI for displaying the game grid, input fields, messages, timer, and scores.
 * - Turn-based gameplay for two players across multiple rounds.
 * - Guess evaluation with visual feedback (correct position, correct letter/wrong position, incorrect).
 * - Turn timer implementation.
 * - Score calculation based on remaining attempts and time.
 * - End-of-game summary and winner declaration.
 *
 * @author colecampbell
 * @version 1.0
 */
public final class TwistedWordle
        extends Application
        implements Scorable
{
    private static final int MAX_ATTEMPTS             = 6;
    private static final int WORD_LENGTH              = 5;
    private static final int TURN_TIME                = 90;
    private static final int TOTAL_ROUNDS             = 3;
    private static final int DELAY                    = 3;
    private static final int NOTHING                  = 0;
    private static final int FIRST_ROUND              = 1;
    private static final int TIME_FORMAT              = 1000;
    private static final int BASE_CORRECT_SCORE       = 50;
    private static final int ATTEMPTS_LEFT_MULTIPLIER = 10;
    private static final int OFFSET                   = 1;

    private static final String WORD_FILE_PATH = "src/resources/words.txt";
    private static final String STAGE_MESSAGE  = "Stage cannot be null";

    private static String       staticPlayer1Name;
    private static String       staticPlayer2Name;
    private static List<String> staticWordsForPlayer1;
    private static List<String> staticWordsForPlayer2;
    private static Set<String>  staticWordSet;
    private static Stage        currentStage;
    private static Runnable     onCloseCallback;

    private String         targetWord;
    private int            attemptsLeft;
    private Label[][]      gridLabels;
    private TextField      inputField;
    private Label          messageLabel;
    private Label          timerLabel;
    private Label          scoreLabel;
    private Label          roundLabel;
    private Player         currentPlayer;
    private Player         player1;
    private Player         player2;
    private long           startTime;
    private AnimationTimer timer;
    private int            currentRound;
    private Stage          primaryStage;

    /**
     * Performs initial game setup through the console interface.
     * This method handles loading the word list, getting player names,
     * and allowing each player to choose words for their opponent.
     * It relies on static fields to store this setup data before the GUI starts.
     *
     * @return true if the setup completes successfully, false if an error occurs.
     */
    public static boolean setupGameFromConsole()
    {
        final Scanner consoleScanner;
        consoleScanner = new Scanner(System.in);

        boolean success;
        success = false;

        try
        {
            staticWordSet = loadAndProcessWords(WORD_FILE_PATH);

            System.out.println("Loaded " +
                               staticWordSet.size() +
                               " valid words.");

            validateStaticWordSet(staticWordSet);

            System.out.print("Enter Player 1 Name: ");
            staticPlayer1Name = consoleScanner.nextLine().trim();

            validatePlayerName(staticPlayer1Name);

            System.out.print("Enter Player 2 Name: ");
            staticPlayer2Name = consoleScanner.nextLine().trim();

            validatePlayerName(staticPlayer2Name);

            // Player 1 chooses words
            System.out.println("\n--- " +
                               staticPlayer1Name +
                               ", choose " +
                               TOTAL_ROUNDS +
                               " words for " +
                               staticPlayer2Name +
                               " ---");
            staticWordsForPlayer2 = getWordsFromConsole(staticPlayer1Name,
                                                        consoleScanner);
            validateWordsForPlayer(staticWordsForPlayer2);

            // Player 2 chooses words
            System.out.println("\n--- " +
                               staticPlayer2Name +
                               ", choose " +
                               TOTAL_ROUNDS +
                               " words for " +
                               staticPlayer1Name +
                               " ---");
            staticWordsForPlayer1 = getWordsFromConsole(staticPlayer2Name,
                                                        consoleScanner);
            validateWordsForPlayer(staticWordsForPlayer1);

            success = true;

        } catch (final Exception e)
        {
            // Log the exception details for debugging
            System.err.println("An error occurred during console setup: " + e.getMessage());
            e.printStackTrace(); // Consider using a logging framework for better error handling
        }
        // No need to close System.in scanner

        return success;
    }

    /*
     * Validates that the static word set is not null and not empty.
     * Throws an IllegalArgumentException if the set is invalid.
     */
    private static void validateStaticWordSet(final Set<String> staticWordSet)
    {
        if (staticWordSet == null ||
            staticWordSet.isEmpty())
        {
            throw new IllegalArgumentException("Word set cannot be null or empty");
        }
    }

    /*
     * Validates that the player name is neither null nor blank.
     * Throws an IllegalArgumentException if the name is invalid.
     */
    private static void validatePlayerName(final String name)
    {
        if (name == null ||
            name.isBlank())
        {
            throw new IllegalArgumentException("Player name should not be null or blank");
        }
    }

    /*
     * Validates that the list of words chosen for a player is not null
     * and contains the correct number of words (equal to TOTAL_ROUNDS).
     * Throws an IllegalStateException if the list is invalid.
     */
    private static void validateWordsForPlayer(final List<String> words)
    {
        if (words == null ||
            words.size() != TOTAL_ROUNDS)
        {
            throw new IllegalStateException("Word list for player is invalid (null or wrong size)");
        }
    }

    /**
     * Reads words from the specified file, filters them based on length,
     * converts them to uppercase, and returns them as a Set.
     * This method performs the core logic for loading and validating words from the source file.
     *
     * @param filename The path to the file containing words. Must be non-null and non-blank.
     * @return A Set containing valid, uppercase words of the required length (WORD_LENGTH).
     * @throws IOException              if an I/O error occurs reading from the file.
     * @throws IllegalArgumentException if the filename is invalid or the file does not exist.
     */
    public static Set<String> loadAndProcessWords(final String filename)
    throws IOException
    {
        validateFileName(filename);

        final Path filePath;
        filePath = Paths.get(filename);

        validateFileExistence(filePath);

        // Read all lines, trim whitespace, filter by length, convert to uppercase, collect into a Set.
        return Files.readAllLines(filePath)
                    .stream()
                    .map(String::trim)
                    .filter(word -> word.length() == WORD_LENGTH)
                    .map(String::toUpperCase)
                    .collect(Collectors.toSet());
    }

    /*
     * Validates that the provided file name string is neither null nor blank.
     * Throws an IllegalArgumentException if the file name is invalid.
     */
    private static void validateFileName(final String fileName)
    {
        if (fileName == null ||
            fileName.isBlank())
        {
            throw new IllegalArgumentException("Invalid file name provided (null or blank)");
        }
    }

    /*
     * Validates that the file specified by the Path object exists.
     * Throws an IllegalArgumentException if the file does not exist.
     */
    private static void validateFileExistence(final Path filePath)
    {
        if (!Files.exists(filePath))
        {
            // Provide a more informative error message including the path
            throw new IllegalArgumentException("Word file not found at path: " + filePath.toAbsolutePath());
        }
    }

    /**
     * The main entry point for the application.
     * It triggers the console setup process and, if successful,
     * launches the JavaFX application GUI. If setup fails, it prints an error message.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(final String[] args)
    {
        System.out.println("Starting Twisted Wordle setup...");
        boolean setupOk = setupGameFromConsole();

        if (setupOk)
        {
            // Launch the JavaFX application thread
            launch(args);
        }
        else
        {
            System.err.println("Twisted Wordle setup failed. Exiting.");
            // Optionally System.exit(1) if immediate termination is desired
        }
    }

    /*
     * Prompts the specified player via the console to enter a fixed number (TOTAL_ROUNDS)
     * of valid words for their opponent. Validates each entered word for length
     * and existence in the loaded staticWordSet.
     */
    private static List<String> getWordsFromConsole(final String playerName,
                                                    final Scanner scanner)
    {
        validatePlayerName(playerName);
        Objects.requireNonNull(scanner, "Scanner cannot be null for console input");
        // Ensure word set is ready before proceeding
        validateStaticWordSet(staticWordSet);

        final List<String> chosenWords;
        chosenWords = new ArrayList<>();

        System.out.println("Words must be " + WORD_LENGTH + " letters long and present in the loaded word list.");

        // Loop until the required number of valid words are entered
        for (int i = 0; i < TOTAL_ROUNDS; i++)
        {
            String  enteredWord;
            boolean validWord;

            do
            {
                System.out.printf("  Enter word %d of %d: ",
                                  i + OFFSET, TOTAL_ROUNDS);

                enteredWord = scanner.nextLine().trim().toUpperCase();

                // Perform validation checks sequentially
                validWord = validateWordLength(enteredWord) &&
                            validateWordInWordList(enteredWord);

                // Provide feedback if the word is invalid
                if (!validWord)
                {
                    System.out.println("    Please try again.");
                }

            } while (!validWord);

            chosenWords.add(enteredWord);
        }

        System.out.println("  " +
                           playerName +
                           " finished choosing words.");

        return chosenWords;
    }

    /*
     * Validates if the entered word has the correct length (WORD_LENGTH).
     * Prints an error message to the console if the length is incorrect.
     */
    private static boolean validateWordLength(final String word)
    {
        // Check for null to avoid NullPointerException, although trim() handles it.
        if (word == null || word.length() != WORD_LENGTH)
        {
            System.out.println("    ERROR: Word must be exactly " +
                               WORD_LENGTH + " letters long.");
            return false;
        }
        return true;
    }

    /*
     * Validates if the entered word exists in the static set of allowed words (staticWordSet).
     * Prints an error message to the console if the word is not found.
     * Assumes staticWordSet has been previously validated and is not null.
     */
    private static boolean validateWordInWordList(final String word)
    {
        // Check for null defensively, although upstream validation might cover this.
        if (word == null || !staticWordSet.contains(word))
        {
            System.out.println("    ERROR: '" +
                               word + // Display the problematic word
                               "' is not in the allowed word list.");
            return false;
        }
        return true;
    }

    /**
     * Launches the JavaFX GUI for the game on the JavaFX Application Thread.
     * Ensures that the necessary static setup data (player names, words, word set) is available.
     * If data is missing, it logs an error and executes the callback.
     * Manages the game's Stage lifecycle, ensuring only one instance runs
     * and handling window closing events via the provided callback.
     *
     * @param callback A Runnable to be executed when the game stage is eventually closed
     *                or if launch fails early. Must not be null.
     */
    public static void launchGame(final Runnable callback)
    {
        Objects.requireNonNull(callback, "Callback cannot be null");

        // Pre-check: Ensure all necessary static data from console setup is present.
        if (staticPlayer1Name == null || staticPlayer1Name.isBlank() ||
            staticPlayer2Name == null || staticPlayer2Name.isBlank() ||
            staticWordsForPlayer1 == null || staticWordsForPlayer1.size() != TOTAL_ROUNDS ||
            staticWordsForPlayer2 == null || staticWordsForPlayer2.size() != TOTAL_ROUNDS ||
            staticWordSet == null || staticWordSet.isEmpty())
        {
            System.err.println("Cannot launch game: Pre-game setup data missing or incomplete.");
            // Execute the callback immediately on the FX thread if setup data is bad
            Platform.runLater(callback);
            return;
        }

        // Store the callback for later use when the stage closes.
        onCloseCallback = callback;

        // Execute GUI launch logic on the JavaFX Application Thread.
        Platform.runLater(() ->
                          {
                              try
                              {
                                  // If a stage is already showing, just bring it to the front.
                                  if (currentStage != null &&
                                      currentStage.isShowing())
                                  {
                                      currentStage.toFront();
                                      return; // Prevent creating a duplicate stage
                                  }

                                  // If a previous stage existed but wasn't showing, close it first.
                                  if (currentStage != null)
                                  {
                                      currentStage.close(); // Trigger its onHidden if set
                                      currentStage = null;
                                  }

                                  // Create and configure the new game stage.
                                  currentStage = new Stage();
                                  currentStage.setTitle("Twisted Wordle");

                                  // Set up the cleanup logic for when the stage is hidden/closed.
                                  currentStage.setOnHidden((final WindowEvent event) ->
                                                           {
                                                               // Ensure this is the stage we think it is before nulling
                                                               if (currentStage == event.getSource())
                                                               {
                                                                   currentStage = null; // Clear the static reference
                                                               }

                                                               // Execute the stored callback
                                                               if (onCloseCallback != null)
                                                               {
                                                                   onCloseCallback.run();
                                                               }
                                                               else
                                                               {
                                                                   System.err.println("onCloseCallback was null when stage closed.");
                                                               }
                                                           });

                                  // Instantiate the application and start the JavaFX lifecycle for it.
                                  // This will call the start(Stage) method below.
                                  new TwistedWordle().start(currentStage);

                                  // Ensure the newly created stage is visible and focused.
                                  currentStage.show(); // Show the stage first
                                  currentStage.toFront(); // Then bring it to front

                              } catch (final Exception e)
                              {
                                  // Catch potential exceptions during TwistedWordle().start() or stage setup.
                                  System.err.println("Error launching Twisted Wordle GUI: " + e.getMessage());
                                  e.printStackTrace(); // Log stack trace for debugging

                                  // Ensure the callback is executed even if GUI launch fails.
                                  if (onCloseCallback != null)
                                  {
                                      onCloseCallback.run();
                                  }
                                  // Attempt to close the potentially problematic stage if it was created
                                  if (currentStage != null)
                                  {
                                      currentStage.close();
                                  }
                              }
                          });
    }

    /**
     * The main entry point for the JavaFX application instance, called by
     * `launch()` or `Platform.runLater()`.
     * Initializes the primary stage, sets up player objects using the static data from console setup,
     * initializes the game UI components, and starts the first turn.
     * Handles potential exceptions during initialization and closes the stage if setup fails.
     *
     * @param stage The primary stage for this application instance, provided by the JavaFX framework.
     *              Must not be null.
     * @throws NullPointerException     if stage is null.
     * @throws IllegalStateException    if required static data (words) is invalid.
     * @throws IllegalArgumentException if player names from static data are invalid.
     */
    @Override
    public void start(final Stage stage)
    {
        Objects.requireNonNull(stage, STAGE_MESSAGE);

        this.primaryStage = stage;

        // Set up cleanup for this specific stage instance.
        primaryStage.setOnHidden(e ->
                                 {
                                     // Stop the timer if it's running when the window closes.
                                     if (timer != null)
                                     {
                                         timer.stop();
                                         timer = null; // Release reference
                                     }
                                     // Execute the global onCloseCallback if it's set.
                                     if (onCloseCallback != null)
                                     {
                                         // Ensure callback runs on FX thread if needed by callback's implementation.
                                         Platform.runLater(onCloseCallback);
                                     }
                                 });

        // Ensure the stage is visible and focused.
        // primaryStage.show(); // show() is called by launchGame now
        primaryStage.toFront(); // Make sure it's the active window
        primaryStage.setTitle("Twisted Wordle"); // Set title again just in case

        try
        {
            // Create Player objects using factory and names from static setup data.
            player1 = PlayerFactory.createPlayer(staticPlayer1Name);
            player2 = PlayerFactory.createPlayer(staticPlayer2Name);

            // Player 1 starts the first round.
            currentPlayer = player1;

            // Validate the word lists obtained from static setup data again.
            validateWordsForPlayer(staticWordsForPlayer1);
            validateWordsForPlayer(staticWordsForPlayer2);

            // Build the game's user interface.
            initializeGameUI(stage);

            // Start the first turn of the game.
            startTurn(currentPlayer);

        } catch (final IllegalArgumentException | IllegalStateException e)
        {
            // Catch specific setup errors (bad name, bad word list).
            System.err.println("Failed to initialize game state: " + e.getMessage());
            e.printStackTrace();
            // Close the stage immediately if critical setup fails.
            Platform.runLater(primaryStage::close);
        } catch (final Exception e)
        {
            // Catch any other unexpected errors during startup.
            System.err.println("An unexpected error occurred during game start: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(primaryStage::close);
        }
    }

    /*
     * Sets up the primary graphical user interface elements for the game.
     * Creates the grid, input field, buttons, and labels, arranges them in layouts,
     * and sets the scene on the provided stage.
     * Initializes game state variables like attemptsLeft and currentRound.
     */
    private void initializeGameUI(final Stage stage)
    {
        // Initialize game state for the very beginning
        attemptsLeft = MAX_ATTEMPTS;
        currentRound = FIRST_ROUND;

        // Create the main grid for displaying guesses
        final GridPane gridPane;
        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10); // Horizontal gap between cells
        gridPane.setVgap(10); // Vertical gap between cells
        gridPane.setPadding(new Insets(20)); // Padding around the grid
        gridLabels = new Label[MAX_ATTEMPTS][WORD_LENGTH];

        // Initialize each cell in the grid as an empty Label
        for (int row = 0; row < MAX_ATTEMPTS; row++)
        {
            for (int col = 0; col < WORD_LENGTH; col++)
            {
                final Label label;
                label = new Label(""); // Start empty

                label.setFont(Font.font(20)); // Set font size
                label.setMinSize(40, 40);     // Ensure square cells
                label.setAlignment(Pos.CENTER); // Center text within the label
                // Initial styling: black border
                label.setStyle("-fx-border-color: black; -fx-border-width: 2;");
                gridLabels[row][col] = label;  // Store reference
                gridPane.add(label, col, row); // Add to GridPane at column `col`, row `row`
            }
        }

        // Create the input field for guesses
        inputField = new TextField();
        inputField.setFont(Font.font(20));
        inputField.setMaxWidth(200); // Limit width
        inputField.setPromptText("Enter guess..."); // Placeholder text
        inputField.setDisable(true); // Initially disabled until a turn starts
        // Add event handler for pressing Enter key
        inputField.setOnKeyPressed(e ->
                                   {
                                       if (e.getCode() == KeyCode.ENTER)
                                       {
                                           handleGuess(); // Process guess on Enter
                                       }
                                   });

        // Create the submit button for guesses
        final Button submitButton;
        submitButton = new Button("Guess");
        submitButton.setDisable(true); // Initially disabled
        submitButton.setOnAction(e -> handleGuess()); // Process guess on button click
        // Bind the button's disable state to the input field's disable state
        submitButton.disableProperty().bind(inputField.disabledProperty());

        // Arrange input field and button horizontally
        final HBox inputBox;
        inputBox = new HBox(10, inputField, submitButton); // 10px spacing
        inputBox.setAlignment(Pos.CENTER);

        // Create labels for displaying game information
        messageLabel = new Label("Initializing..."); // Status messages
        messageLabel.setFont(Font.font(20));
        timerLabel = new Label("Time left: --"); // Turn timer display
        timerLabel.setFont(Font.font(20));
        // Initial score display (scores start at 0)
        scoreLabel = new Label("Scores: " +
                               player1.getName() +
                               ": 0 | " +
                               player2.getName() +
                               ": 0");
        scoreLabel.setFont(Font.font(20));
        // Initial round display
        roundLabel = new Label("Round: " +
                               currentRound +
                               " of " +
                               TOTAL_ROUNDS);
        roundLabel.setFont(Font.font(20));

        // Create the main vertical layout container
        final VBox root;
        root = new VBox(20, // 20px vertical spacing between elements
                        gridPane,
                        inputBox,
                        messageLabel,
                        timerLabel,
                        scoreLabel,
                        roundLabel);
        root.setAlignment(Pos.CENTER); // Center elements vertically
        root.setPadding(new Insets(20)); // Padding around the entire layout

        // Create the scene with the root layout and set dimensions
        final Scene scene;
        scene = new Scene(root, 600, 700); // Width, Height

        // Set the scene on the stage
        stage.setScene(scene);
        // Stage is shown by the caller (launchGame or Application.launch)
        // stage.show();
    }

    /*
     * Handles the logic when the player submits a guess (via Enter key or button).
     * Validates the guess length, compares it against the target word,
     * updates the grid UI with appropriate colors (green, yellow, gray),
     * decrements attemptsLeft, and checks for win/loss conditions for the current turn.
     * Triggers score calculation and prepares the next turn or ends the game if necessary.
     */
    private void handleGuess()
    {
        final String guess;
        guess = inputField.getText().trim().toUpperCase(); // Trim and convert to uppercase

        inputField.clear(); // Clear input field after retrieving guess

        // 1. Validate Guess Length
        if (guess.length() != WORD_LENGTH)
        {
            messageLabel.setText("Please enter a " + WORD_LENGTH + "-letter word.");
            return; // Stop processing if length is wrong
        }

        // 2. Prepare for Grid Update
        final int       currentAttempt; // Row index for the current guess
        final boolean[] targetMatched;  // Tracks if target letter positions are matched (green/yellow)
        final boolean[] guessMatched;   // Tracks if guess letter positions are matched (green/yellow)

        currentAttempt = MAX_ATTEMPTS - attemptsLeft; // Calculate row (0-indexed)
        // Check for valid row index, though attemptsLeft logic should prevent out-of-bounds
        if (currentAttempt < 0 || currentAttempt >= MAX_ATTEMPTS)
        {
            System.err.println("Error: Invalid attempt index calculated: " + currentAttempt);
            messageLabel.setText("Internal error. Please restart.");
            inputField.setDisable(true); // Prevent further input
            return;
        }

        targetMatched = new boolean[WORD_LENGTH];
        guessMatched  = new boolean[WORD_LENGTH];

        // 3. First Pass: Check for Correct Letters in Correct Positions (Green)
        for (int i = 0; i < WORD_LENGTH; i++)
        {
            final char  guessedChar  = guess.charAt(i);
            final char  targetChar   = targetWord.charAt(i);
            final Label currentLabel = gridLabels[currentAttempt][i];

            currentLabel.setText(String.valueOf(guessedChar)); // Display the guessed letter

            if (guessedChar == targetChar)
            {
                // Correct letter, correct position (Green)
                currentLabel.setStyle("-fx-background-color: #6aaa64; " + // Green background
                                      "-fx-text-fill: white; " +          // White text
                                      "-fx-border-color: #444; " +         // Darker border
                                      "-fx-border-width: 1;");
                targetMatched[i] = true; // Mark target position as matched
                guessMatched[i]  = true;  // Mark guess position as matched
            }
            else
            {
                // Initially assume incorrect (Gray) - may change to yellow in the second pass
                currentLabel.setStyle("-fx-background-color: #787c7e; " + // Gray background
                                      "-fx-text-fill: white; " +          // White text
                                      "-fx-border-color: #444; " +         // Darker border
                                      "-fx-border-width: 1;");
            }
        }

        // 4. Second Pass: Check for Correct Letters in Wrong Positions (Yellow)
        for (int i = 0; i < WORD_LENGTH; i++) // Iterate through guess letters
        {
            if (!guessMatched[i]) // Only check letters not already marked green
            {
                char  guessedChar  = guess.charAt(i);
                Label currentLabel = gridLabels[currentAttempt][i];

                // Check against unmatched target letters
                for (int j = 0; j < WORD_LENGTH; j++) // Iterate through target letters
                {
                    // If target letter at j is not already matched (green/yellow)
                    // and the current guessed letter matches it
                    if (!targetMatched[j] && guessedChar == targetWord.charAt(j))
                    {
                        // Correct letter, wrong position (Yellow)
                        currentLabel.setStyle("-fx-background-color: #c9b458; " + // Yellow background
                                              "-fx-text-fill: white; " +          // White text
                                              "-fx-border-color: #444;" +          // Darker border
                                              " -fx-border-width: 1;");
                        targetMatched[j] = true; // Mark this target letter position as used for yellow
                        guessMatched[i]  = true;  // Mark this guess letter as matched (yellow)
                        break; // Stop checking target letters for this guessed char once a yellow match is found
                    }
                }
                // If loop finishes and guessMatched[i] is still false, it remains gray
            }
        }

        // 5. Update Game State
        attemptsLeft--;

        // 6. Check Win/Loss Conditions for the Turn
        if (guess.equals(targetWord)) // Player guessed correctly
        {
            if (timer != null)
            {
                timer.stop(); // Stop the turn timer
            }

            // Calculate score based on remaining attempts and time
            final long elapsedTime;
            final int  timeLeft;
            final int  score;

            elapsedTime = (System.currentTimeMillis() - startTime) / TIME_FORMAT; // Time in seconds
            timeLeft    = Math.max(NOTHING, TURN_TIME - (int) elapsedTime); // Ensure non-negative time
            // Score includes points for attempts left *before* this successful guess
            score = calculateScore(attemptsLeft + OFFSET, timeLeft);

            currentPlayer.addScore(score); // Add score to the current player

            messageLabel.setText("Correct! " +
                                 currentPlayer.getName() +
                                 " guessed the word!");

            inputField.setDisable(true); // Disable input until next turn
            updateScoreboard(); // Display updated scores

            // Pause briefly before moving to the next turn/round
            final PauseTransition delay;
            delay = new PauseTransition(Duration.seconds(DELAY));
            delay.setOnFinished(e -> Platform.runLater(this::prepareNextTurn));
            delay.play();
        }
        else if (attemptsLeft == NOTHING) // Player ran out of attempts
        {
            if (timer != null)
            {
                timer.stop(); // Stop the turn timer
            }

            messageLabel.setText("Out of attempts! The word was: " +
                                 targetWord); // Reveal the word
            inputField.setDisable(true); // Disable input

            // Pause briefly before moving to the next turn/round
            final PauseTransition delay;
            delay = new PauseTransition(Duration.seconds(DELAY));
            delay.setOnFinished(e -> Platform.runLater(this::prepareNextTurn));
            delay.play();
        }
        else // Guess was incorrect, but attempts remain
        {
            messageLabel.setText(currentPlayer.getName() +
                                 ", attempts left: " +
                                 attemptsLeft); // Update attempt count message
            // Input field remains enabled for the next guess
        }
    }

    /**
     * Prepares the game state for the next turn or ends the game if all rounds are completed.
     * Stops the current timer, determines the next player, increments the round counter if necessary,
     * and either calls Player for the next player or endGame().
     * This method ensures a clean transition between player turns or rounds.
     */
    public void prepareNextTurn()
    {
        // Stop the timer for the completed turn, if it's running
        if (timer != null)
        {
            timer.stop();
            timer = null; // Clear the timer reference
        }
        // Reset timer label display
        timerLabel.setText("Time left: --");

        final Player nextPlayer;

        // Determine who plays next
        if (currentPlayer == player1)
        {
            nextPlayer = player2; // Switch to Player 2
        }
        else // Current player was Player 2
        {
            nextPlayer = player1; // Switch back to Player 1
            currentRound++; // Increment round number after Player 2's turn

            // Check if the game should end
            if (currentRound > TOTAL_ROUNDS)
            {
                endGame(); // All rounds completed
                return;    // Stop further turn processing
            }

            // Update round label display for the new round
            roundLabel.setText("Round: " + currentRound +
                               " of " + TOTAL_ROUNDS);
        }

        // Start the turn for the determined next player
        startTurn(nextPlayer);
    }

    /*
     * Initializes and starts a new turn for the specified player.
     * Sets the current player, resets attemptsLeft, determines the target word for the player
     * based on the current round, resets the UI grid, updates info labels (message, score, round),
     * enables the input field, and starts the turn timer.
     */
    private void startTurn(final Player player)
    {
        // Set the active player for this turn
        currentPlayer = player;
        attemptsLeft  = MAX_ATTEMPTS; // Reset guess attempts

        // Determine the correct target word for this player and round
        int roundIndex;
        roundIndex = currentRound - FIRST_ROUND; // 0-based index for list access

        final List<String> wordListForThisPlayer;

        // Select the word list chosen by the *opponent*
        if (currentPlayer == player1)
        {
            // Player 1 guesses words chosen by Player 2 (stored in staticWordsForPlayer1)
            wordListForThisPlayer = staticWordsForPlayer1;
        }
        else // currentPlayer is player2
        {
            // Player 2 guesses words chosen by Player 1 (stored in staticWordsForPlayer2)
            wordListForThisPlayer = staticWordsForPlayer2;
        }

        // Validate that the word list and round index are valid
        if (wordListForThisPlayer == null ||
            roundIndex < NOTHING ||
            roundIndex >= wordListForThisPlayer.size())
        {
            // This indicates a setup error or logic flaw
            System.err.println("Error starting turn: Invalid word list or round index. Player: "
                               + player.getName() + ", Round: " + currentRound + ", Index: " + roundIndex);
            messageLabel.setText("Error: Could not load word for this round.");
            inputField.setDisable(true);
            endGame(); // Terminate the game due to the error
            return;
        }

        // Set the target word for this turn
        targetWord = wordListForThisPlayer.get(roundIndex);
        // Defensive check: Ensure the word itself is valid (though should be pre-validated)
        if (targetWord == null || targetWord.length() != WORD_LENGTH)
        {
            System.err.println("Error starting turn: Invalid target word loaded: " + targetWord);
            messageLabel.setText("Error: Invalid word loaded for this round.");
            inputField.setDisable(true);
            endGame();
            return;
        }
        // System.out.println("Debug: " + currentPlayer.getName() + "'s target word for round " + currentRound + " is " + targetWord); // DEBUG ONLY


        // Prepare the UI for the new turn
        resetGrid(); // Clear the guess grid display

        // Display turn prompt
        messageLabel.setText(currentPlayer.getName() +
                             ", guess the " + WORD_LENGTH + "-letter word! (" +
                             MAX_ATTEMPTS + " attempts)");

        inputField.setDisable(false); // Enable the input field

        // Request focus on the input field so the player can type immediately
        Platform.runLater(() -> inputField.requestFocus());

        updateScoreboard(); // Ensure scoreboard is current

        // Update round label (might be redundant if called in prepareNextTurn, but safe)
        roundLabel.setText("Round: " + currentRound +
                           " of " + TOTAL_ROUNDS);

        startTimer(); // Start the timer for this turn
    }

    /*
     * Resets the visual appearance of the Wordle grid UI.
     * Clears the text and resets the background color and border style
     * for all label cells in the gridLabels array, preparing it for a new turn.
     */
    private void resetGrid()
    {
        // Iterate through each row and column of the grid
        for (int row = 0; row < MAX_ATTEMPTS; row++)
        {
            for (int col = 0; col < WORD_LENGTH; col++)
            {
                // Clear the text content of the label
                gridLabels[row][col].setText("");
                // Reset the style to the default appearance (transparent background, black border)
                gridLabels[row][col].setStyle("-fx-border-color: black; " +
                                              "-fx-border-width: 2; " +
                                              "-fx-background-color: transparent;");
            }
        }
    }

    /**
     * Calculates the score awarded for a correct guess based on the number
     * of attempts remaining *before* the guess and the time left on the turn timer.
     * The score is calculated as: Base Score + (Attempts Left * Multiplier) + Time Left.
     *
     * @param attemptsLeftBeforeGuess The number of attempts the player had remaining
     *                                *before* making the correct guess (must be non-negative).
     * @param timeLeft                The time in seconds remaining on the timer when the
     *                                guess was made (must be non-negative).
     * @return The calculated score as an integer.
     * @throws IllegalArgumentException if attemptsLeftBeforeGuess or timeLeft are negative.
     */
    @Override
    public int calculateScore(final int attemptsLeftBeforeGuess,
                              final int timeLeft)
    {
        // Validate inputs
        validateNumberForCalculation(attemptsLeftBeforeGuess);
        validateNumberForCalculation(timeLeft);

        int attemptsScore;

        // Calculate points based on remaining attempts (more attempts left = higher score)
        if (attemptsLeftBeforeGuess > NOTHING)
        {
            // Note: uses attempts *before* the guess, so a guess on the last try (attemptsLeft=0 after guess)
            // corresponds to attemptsLeftBeforeGuess = 1 here.
            attemptsScore = attemptsLeftBeforeGuess * ATTEMPTS_LEFT_MULTIPLIER;
        }
        else
        {
            // Should not happen if called on a correct guess, but defensively set to 0.
            // If called when attempts ran out, attemptsLeftBeforeGuess would be 0 here.
            attemptsScore = NOTHING;
        }

        // Total score combines base points, attempt bonus, and time bonus.
        // Math.max ensures time component isn't negative if timeLeft somehow becomes < 0.
        return BASE_CORRECT_SCORE + attemptsScore + Math.max(NOTHING, timeLeft);
    }

    /*
     * Validates that a number used in score calculation is not negative.
     * Throws an IllegalArgumentException if the number is negative.
     */
    private static void validateNumberForCalculation(final int num)
    {
        if (num < NOTHING)
        {
            throw new IllegalArgumentException("Number for score calculation cannot be negative: " + num);
        }
    }

    /*
     * Updates the text of the scoreLabel UI element to reflect the
     * current scores stored in the player1 and player2 objects.
     */
    private void updateScoreboard()
    {
        // Ensure player objects are not null before accessing properties
        if (player1 != null && player2 != null && scoreLabel != null)
        {
            scoreLabel.setText("Scores: " +
                               player1.getName() + ": " + player1.getScore() +
                               " | " + // Separator
                               player2.getName() + ": " + player2.getScore());
        }
        else
        {
            System.err.println("Error updating scoreboard: Player or label object is null.");
        }
    }

    /*
     * Initializes and starts the AnimationTimer for the current player's turn.
     * Records the start time and updates the timerLabel UI element each frame
     * with the remaining time. If time runs out, it stops the timer, updates
     * the message label, disables input, and triggers the transition to the next turn.
     */
    private void startTimer()
    {
        // Stop any existing timer before starting a new one
        if (timer != null)
        {
            timer.stop();
        }

        // Record the system time when the timer starts
        startTime = System.currentTimeMillis();

        // Create a new AnimationTimer instance
        timer = new AnimationTimer()
        {
            /**
             * Handles the timer tick event, called by the JavaFX framework on each frame.
             * Calculates elapsed time, updates the timer display, and checks if time has run out.
             * @param now The current timestamp in nanoseconds, provided by the framework.
             */
            @Override
            public void handle(final long now) // 'now' parameter is typically unused when using System.currentTimeMillis
            {
                final long elapsedTime; // Elapsed time in seconds
                final int  timeLeft;    // Remaining time in seconds

                // Calculate elapsed time since startTime
                elapsedTime = (System.currentTimeMillis() - startTime) / TIME_FORMAT; // Convert ms to s
                // Calculate remaining time, ensuring it doesn't go below zero
                timeLeft = Math.max(NOTHING, TURN_TIME - (int) elapsedTime);

                // Check if the player's time is up
                if (timeLeft <= NOTHING)
                {
                    timerLabel.setText("Time left: 0"); // Display 0 time left

                    this.stop(); // Stop this AnimationTimer
                    timer = null; // Clear the timer reference

                    // Update UI to indicate time's up
                    messageLabel.setText("Time's up, " + currentPlayer.getName() +
                                         "! The word was: " + targetWord); // Reveal word
                    inputField.setDisable(true); // Disable guessing

                    // Pause briefly before moving to the next turn
                    final PauseTransition delay;
                    delay = new PauseTransition(Duration.seconds(DELAY));
                    // Use Platform.runLater if prepareNextTurn modifies UI from a non-FX thread context,
                    // though AnimationTimer's handle() runs on the FX thread. Safer to keep it.
                    delay.setOnFinished(e -> Platform.runLater(
                            TwistedWordle.this::prepareNextTurn));
                    delay.play();
                }
                else // Time still remaining
                {
                    // Update the timer label display
                    timerLabel.setText("Time left: " + timeLeft);
                }
            }
        };

        // Start the timer loop
        timer.start();
    }

    /*
     * Handles the end of the game logic.
     * Stops the game timer if it's running, disables the input field,
     * updates the message label to indicate game over, ensures the final scores
     * are displayed, and triggers the display of the winner popup window.
     */
    private void endGame()
    {
        // Stop the turn timer if it's active
        if (timer != null)
        {
            timer.stop();
            timer = null; // Clear reference
        }

        // Update UI elements for game over state
        messageLabel.setText("Game over! Final scores:");
        inputField.setDisable(true); // Disable further input
        timerLabel.setText("Time left: --"); // Clear timer display

        // Ensure the final scores are displayed correctly
        updateScoreboard();

        // Show the winner/tie popup window on the JavaFX Application Thread
        Platform.runLater(this::showWinnerPopup);
    }

    /*
     * Creates and displays a modal popup window (dialog) showing the final scores
     * and declaring the winner or a tie based on the scores.
     * The popup includes a button to close the popup and the main game window.
     */
    private void showWinnerPopup()
    {
        // Create a new stage for the popup
        final Stage popupStage;
        popupStage = new Stage();

        // Configure popup properties
        popupStage.initModality(Modality.APPLICATION_MODAL); // Blocks interaction with the main game window
        popupStage.initOwner(primaryStage); // Set the main game window as the owner
        popupStage.initStyle(StageStyle.UTILITY); // Simple window style without minimize/maximize
        popupStage.setTitle("Game Over");

        // Determine the winner message
        final String winnerMessage;
        // Ensure players are not null before comparing scores
        if (player1 == null || player2 == null)
        {
            System.err.println("Cannot determine winner: Player objects are null.");
            winnerMessage = "Error determining winner.";
        }
        else if (player1.getScore() > player2.getScore())
        {
            winnerMessage = player1.getName() + " wins!";
        }
        else if (player2.getScore() > player1.getScore())
        {
            winnerMessage = player2.getName() + " wins!";
        }
        else // Scores are equal
        {
            winnerMessage = "It's a tie!";
        }

        // Create labels for displaying scores and the winner message
        final Label finalScoreLabel;
        // Handle potential null players for score display
        final String scoreText = (player1 != null && player2 != null)
                                 ? player1.getName() + ": " + player1.getScore() + "\n" +
                                   player2.getName() + ": " + player2.getScore()
                                 : "Scores unavailable.";
        finalScoreLabel = new Label(scoreText);
        finalScoreLabel.setFont(Font.font(16)); // Set font size for scores

        final Label winnerLabel;
        winnerLabel = new Label(winnerMessage);
        winnerLabel.setFont(Font.font(20)); // Larger font for winner message
        winnerLabel.setStyle("-fx-font-weight: bold;"); // Make winner message bold

        // Create a button to close the popup and the main game
        final Button closeButton;
        closeButton = new Button("Close Game");

        // Define action for the close button
        closeButton.setOnAction(e ->
                                {
                                    popupStage.close(); // Close the popup window

                                    // Close the main game stage if it's still open
                                    if (primaryStage != null && primaryStage.isShowing())
                                    {
                                        primaryStage.close(); // This will trigger the onHidden handler set in start()
                                    }
                                    // Explicitly run callback if primaryStage was already closed or null?
                                    // The onHidden handler should cover the primary scenario.
                                    // If primaryStage is null, the callback might need manual trigger here,
                                    // but that suggests an issue elsewhere.
//                                    else if (onCloseCallback != null) {
//                                         Platform.runLater(onCloseCallback); // Failsafe?
//                                    }
                                });

        // Arrange the labels and button vertically in the popup
        final VBox popupLayout;
        popupLayout = new VBox(15, // 15px vertical spacing
                               finalScoreLabel,
                               winnerLabel,
                               closeButton);
        popupLayout.setAlignment(Pos.CENTER); // Center elements horizontally
        popupLayout.setPadding(new Insets(25)); // Padding around the content

        // Create the scene for the popup window
        final Scene popupScene;
        popupScene = new Scene(popupLayout, 350, 200); // Width, Height

        // Set the scene on the popup stage and display it
        popupStage.setScene(popupScene);
        // Show the popup and wait for it to be closed before continuing
        popupStage.showAndWait();
    }
}
