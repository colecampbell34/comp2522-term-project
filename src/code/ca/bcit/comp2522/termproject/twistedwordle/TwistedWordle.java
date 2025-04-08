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

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents the main application class for the Twisted Wordle game.
 * This class manages the JavaFX GUI, controls the game flow between players,
 * tracks rounds, times turns, and calculates scores. Setup is handled by GameSetup,
 * and word loading by WordLoader.
 * <p>
 * This class provides the following functionality:
 * - JavaFX Application lifecycle management and GUI launch.
 * - JavaFX GUI for displaying the game grid, input fields, messages, timer, and scores.
 * - Turn-based gameplay control for two players across multiple rounds.
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
    /**
     * Maximum number of guesses allowed per turn.
     */
    public static final int    MAX_ATTEMPTS             = 6;
    /**
     * Required length for words used in the game.
     */
    public static final int    WORD_LENGTH              = 5;
    /**
     * Duration of each player's turn in seconds.
     */
    public static final int    TURN_TIME                = 90;
    /**
     * Total number of rounds in a game.
     */
    public static final int    TOTAL_ROUNDS             = 3;
    /**
     * Delay in seconds before proceeding after a guess or turn end.
     */
    public static final int    DELAY_SECONDS            = 3;
    /**
     * Represents zero or nothing, used for comparisons and initial values.
     */
    public static final int    NOTHING                  = 0;
    /**
     * The index (1-based) of the first round.
     */
    public static final int    FIRST_ROUND              = 1;
    /**
     * Conversion factor from milliseconds to seconds for timer calculations.
     */
    public static final int    TIME_FORMAT              = 1000;
    /**
     * Base points awarded for a correct guess.
     */
    public static final int    BASE_CORRECT_SCORE       = 50;
    /**
     * Points awarded for each attempt remaining after a correct guess.
     */
    public static final int    ATTEMPTS_LEFT_MULTIPLIER = 10;
    /**
     * Offset used for display purposes (e.g., showing round 1 instead of 0).
     */
    public static final int    OFFSET                   = 1;
    /**
     * Path to the file containing the list of valid words.
     */
    public static final String WORD_FILE_PATH           = "src/resources/words.txt";

    /* Error message for null stage arguments. */
    private static final String STAGE_MESSAGE = "Stage cannot be null";


    /* Stores Player 1's name entered during console setup. */
    private static String       staticPlayer1Name;
    /* Stores Player 2's name entered during console setup. */
    private static String       staticPlayer2Name;
    /* Stores the words Player 1 must guess (chosen by Player 2). */
    private static List<String> staticWordsForPlayer1;
    /* Stores the words Player 2 must guess (chosen by Player 1). */
    private static List<String> staticWordsForPlayer2;
    /* The set of all valid words loaded from the file. */
    private static Set<String>  staticWordSet;

    /* Reference to the currently active game stage. */
    private static Stage    currentStage;
    /* Callback to execute when the game stage is closed. */
    private static Runnable onCloseCallback;

    /* The word the current player is trying to guess. */
    private String         targetWord;
    /* Number of guesses remaining for the current player in the current turn. */
    private int            attemptsLeft;
    /* 2D array of Labels representing the Wordle grid cells. */
    private Label[][]      gridLabels;
    /* Text field for player input (guesses). */
    private TextField      inputField;
    /* Label for displaying game messages (e.g., prompts, errors, results). */
    private Label          messageLabel;
    /* Label for displaying the remaining time in the current turn. */
    private Label          timerLabel;
    /* Label for displaying the current scores of both players. */
    private Label          scoreLabel;
    /* Label for displaying the current round number. */
    private Label          roundLabel;
    /* The player whose turn it currently is. */
    private Player         currentPlayer;
    /* Player 1 object. */
    private Player         player1;
    /* Player 2 object. */
    private Player         player2;
    /* Timestamp marking the start of the current turn timer. */
    private long           startTime;
    /* AnimationTimer used to update the turn timer display. */
    private AnimationTimer timer;
    /* The current round number (1-based). */
    private int            currentRound;
    /* The primary stage for the JavaFX application. */
    private Stage          primaryStage;

    /*
     * Validates that the list of words chosen for a player is not null
     * and contains the correct number of words (equal to TOTAL_ROUNDS).
     * Throws an IllegalStateException if the list is invalid.
     * (Kept here as it's used during game start validation)
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
     * The main entry point for the application.
     * It triggers the console setup process via GameSetup and, if successful,
     * launches the JavaFX application GUI. If setup fails, it prints an error message.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(final String[] args)
    {
        System.out.println("Starting Twisted Wordle setup...");

        // Call setup method from GameSetup class
        boolean setupOk;
        setupOk = GameSetup.setupGameFromConsole();

        if (setupOk)
        {
            // Launch the JavaFX application thread (must be called from the Application class)
            launch(args);
        }
        else
        {
            System.err.println("Twisted Wordle setup failed. Exiting.");
            // Optionally System.exit(1) if immediate termination is desired
        }
    }


    /**
     * Launches the JavaFX GUI for the game on the JavaFX Application Thread.
     * Ensures that the necessary static setup data (player names, words, word set) is available.
     * If data is missing, it logs an error and executes the callback.
     * Manages the game's Stage lifecycle, ensuring only one instance runs
     * and handling window closing events via the provided callback.
     *
     * @param callback A Runnable to be executed when the game stage is eventually closed
     *                 or if launch fails early. Must not be null.
     */
    public static void launchGame(final Runnable callback)
    {
        Objects.requireNonNull(callback, "Callback cannot be null");

        // Pre-check: Ensure all necessary static data from console setup is present.
        if (staticPlayer1Name == null ||
            staticPlayer1Name.isBlank() ||
            staticPlayer2Name == null ||
            staticPlayer2Name.isBlank() ||
            staticWordsForPlayer1 == null ||
            staticWordsForPlayer1.size() != TOTAL_ROUNDS ||
            staticWordsForPlayer2 == null ||
            staticWordsForPlayer2.size() != TOTAL_ROUNDS ||
            staticWordSet == null || staticWordSet.isEmpty())
        {
            System.err.println("Cannot launch game: Pre-game setup data missing or incomplete.");
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
                                                               // Ensure this is the stage we think it is
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
        primaryStage.toFront(); // Make sure it's the active window
        primaryStage.setTitle("Twisted Wordle");

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

        } catch (final Exception e)
        {
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
                label = new Label("");

                label.setFont(Font.font(20));
                label.setMinSize(40, 40);
                label.setAlignment(Pos.CENTER);
                label.setStyle("-fx-border-color: black; -fx-border-width: 2;");
                gridLabels[row][col] = label;
                gridPane.add(label, col, row);
            }
        }

        // Create the input field for guesses
        inputField = new TextField();
        inputField.setFont(Font.font(20));
        inputField.setMaxWidth(200); // Limit width
        inputField.setPromptText("Enter guess...");
        inputField.setDisable(true); // Initially disabled until a turn starts

        // Add event handler for pressing Enter key
        inputField.setOnKeyPressed(e ->
                                   {
                                       if (e.getCode() == KeyCode.ENTER)
                                       {
                                           handleGuess();
                                       }
                                   });

        // Create the submit button for guesses
        final Button submitButton;
        submitButton = new Button("Guess");
        submitButton.setDisable(true);
        submitButton.setOnAction(e -> handleGuess());
        submitButton.disableProperty().bind(inputField.disabledProperty());

        // Arrange input field and button horizontally
        final HBox inputBox;
        inputBox = new HBox(10, inputField, submitButton); // 10px spacing
        inputBox.setAlignment(Pos.CENTER);

        // Create labels for displaying game information
        messageLabel = new Label("Initializing...");
        messageLabel.setFont(Font.font(20));
        timerLabel = new Label("Time left: --");
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
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        // Create the scene with the root layout and set dimensions
        final Scene scene;
        scene = new Scene(root, 600, 700);

        // Set the scene on the stage
        stage.setScene(scene);
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
        guess = inputField.getText().trim().toUpperCase();

        inputField.clear();

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
            inputField.setDisable(true);
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

            currentLabel.setText(String.valueOf(guessedChar));

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

        attemptsLeft--;

        // 6. Check Win/Loss Conditions for the Turn
        if (guess.equals(targetWord))
        {
            if (timer != null)
            {
                timer.stop();
            }

            // Calculate score based on remaining attempts and time
            final long elapsedTime;
            final int  timeLeft;
            final int  score;

            elapsedTime = (System.currentTimeMillis() - startTime) / TIME_FORMAT; // Time in seconds
            timeLeft    = Math.max(NOTHING, TURN_TIME - (int) elapsedTime); // Ensure non-negative time
            // Score includes points for attempts left *before* this successful guess
            score = calculateScore(attemptsLeft + OFFSET, timeLeft);

            currentPlayer.addScore(score);

            messageLabel.setText("Correct! " +
                                 currentPlayer.getName() +
                                 " guessed the word!");

            inputField.setDisable(true); // Disable input until next turn
            updateScoreboard(); // Display updated scores

            // Pause briefly before moving to the next turn/round
            final PauseTransition delay;
            delay = new PauseTransition(Duration.seconds(DELAY_SECONDS));
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
            delay = new PauseTransition(Duration.seconds(DELAY_SECONDS));
            delay.setOnFinished(e -> Platform.runLater(this::prepareNextTurn));
            delay.play();
        }
        else
        {
            messageLabel.setText(currentPlayer.getName() +
                                 ", attempts left: " +
                                 attemptsLeft);
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
        roundIndex = currentRound - FIRST_ROUND;

        final List<String> wordListForThisPlayer;

        // Select the word list chosen by the *opponent*
        // Access static variables directly
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
            messageLabel.setText("Error: Could not load word for this round.");
            inputField.setDisable(true);
            endGame(); // Terminate the game due to the error
            return;
        }

        // Set the target word for this turn
        targetWord = wordListForThisPlayer.get(roundIndex);
        // Defensive check: Ensure the word itself is valid (though should be pre-validated)
        if (targetWord == null ||
            targetWord.length() != WORD_LENGTH)
        {  
            messageLabel.setText("Error: Invalid word loaded for this round.");
            inputField.setDisable(true);
            endGame();
            return;
        }

        // Prepare the UI for the new turn
        resetGrid();

        // Display turn prompt
        messageLabel.setText(currentPlayer.getName() +
                             ", guess the " +
                             WORD_LENGTH +
                             "-letter word! (" +
                             MAX_ATTEMPTS +
                             " attempts)");

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
            // corresponds to attemptsLeftBeforeGuess = 1 here.
            attemptsScore = attemptsLeftBeforeGuess * ATTEMPTS_LEFT_MULTIPLIER;  
        }
        else
        {
            attemptsScore = NOTHING;  
        }

        // Total score combines base points, attempt bonus, and time bonus.
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
        if (player1 != null &&
            player2 != null &&
            scoreLabel != null)
        {
            scoreLabel.setText("Scores: " +
                               player1.getName() + ": " + player1.getScore() +
                               " | " +
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
            public void handle(final long now)
            {
                final long elapsedTime;
                final int  timeLeft;

                // Calculate elapsed time since startTime
                elapsedTime = (System.currentTimeMillis() - startTime) / TIME_FORMAT;
                // Calculate remaining time, ensuring it doesn't go below zero
                timeLeft = Math.max(NOTHING, TURN_TIME - (int) elapsedTime);

                // Check if the player's time is up
                if (timeLeft <= NOTHING)
                {  
                    timerLabel.setText("Time left: 0");

                    this.stop();
                    timer = null;

                    messageLabel.setText("Time's up, " + currentPlayer.getName() +
                                         "! The word was: " + targetWord); // Reveal word
                    inputField.setDisable(true);

                    // Pause briefly before moving to the next turn
                    final PauseTransition delay;
                    delay = new PauseTransition(Duration.seconds(DELAY_SECONDS));  
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
        if (timer != null)
        {
            timer.stop();
            timer = null;
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
        if (player1 == null ||
            player2 == null)
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
        final String scoreText;

        if (player1 != null &&
            player2 != null)
        {
            scoreText = player1.getName() + ": " + player1.getScore() + "\n" +
                        player2.getName() + ": " + player2.getScore();
        } else
        {
            scoreText = "Scores unavailable";
        }

        finalScoreLabel = new Label(scoreText);
        finalScoreLabel.setFont(Font.font(16));

        final Label winnerLabel;
        winnerLabel = new Label(winnerMessage);
        winnerLabel.setFont(Font.font(20)); // Larger font for winner message
        winnerLabel.setStyle("-fx-font-weight: bold;"); // Make winner message bold

        final Button closeButton;
        closeButton = new Button("Close Game");

        // Define action for the close button
        closeButton.setOnAction(e ->
                                {
                                    popupStage.close(); // Close the popup window

                                    // Close the main game stage if it's still open
                                    if (primaryStage != null && primaryStage.isShowing())
                                    {
                                        primaryStage.close();
                                    }
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

    /**
     * Accessor for the static field player1Name.
     *
     * @return the player name
     */
    public static String getStaticPlayer1Name()
    {
        return staticPlayer1Name;
    }

    /**
     * Accessor for the static field player2Name.
     *
     * @return the player name
     */
    public static String getStaticPlayer2Name()
    {
        return staticPlayer2Name;
    }

    /**
     * Accessor for the static field wordsForPlayer1.
     *
     * @return the List of words for Player 1
     */
    public static List<String> getStaticWordsForPlayer1()
    {
        return staticWordsForPlayer1;
    }

    /**
     * Accessor for the static field wordsForPlayer2.
     *
     * @return the List of words for Player 2
     */
    public static List<String> getStaticWordsForPlayer2()
    {
        return staticWordsForPlayer2;
    }

    /**
     * Accessor for the static field wordSet.
     *
     * @return the Set of accepted words
     */
    public static Set<String> getStaticWordSet()
    {
        return staticWordSet;
    }


    /**
     * Setter for the static field player1Name.
     *
     * @param staticPlayer1Name the name of the player
     */
    public static void setStaticPlayer1Name(final String staticPlayer1Name)
    {
        TwistedWordle.staticPlayer1Name = staticPlayer1Name;
    }

    /**
     * Setter for the static field player2Name.
     *
     * @param staticPlayer2Name the name of the player
     */
    public static void setStaticPlayer2Name(final String staticPlayer2Name)
    {
        TwistedWordle.staticPlayer2Name = staticPlayer2Name;
    }

    /**
     * Setter for the static field wordsForPlayer1.
     *
     * @param staticWordsForPlayer1 the List of words
     */
    public static void setStaticWordsForPlayer1(final List<String> staticWordsForPlayer1)
    {
        TwistedWordle.staticWordsForPlayer1 = staticWordsForPlayer1;
    }

    /**
     * Setter for the static field wordsForPlayer2.
     *
     * @param staticWordsForPlayer2 the List of words
     */
    public static void setStaticWordsForPlayer2(final List<String> staticWordsForPlayer2)
    {
        TwistedWordle.staticWordsForPlayer2 = staticWordsForPlayer2;
    }

    /**
     * Setter for the static field wordSet.
     *
     * @param staticWordSet the Set of words
     */
    public static void setStaticWordSet(Set<String> staticWordSet)
    {
        TwistedWordle.staticWordSet = staticWordSet;
    }
}
