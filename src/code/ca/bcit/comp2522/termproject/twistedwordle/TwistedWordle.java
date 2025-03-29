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
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Custom multiplayer wordle game. Words chosen via console before GUI starts.
 * Word loading is asynchronous, using a testable helper method for core logic.
 *
 * @author colecampbell
 * @version 1.0
 */
public final class TwistedWordle
        extends Application
        implements Scorable
{

    private static final int    MAX_ATTEMPTS             = 6;
    private static final int    WORD_LENGTH              = 5;
    private static final int    TURN_TIME                = 90;
    private static final int    TOTAL_ROUNDS             = 3;
    private static final int    DELAY                    = 3;
    private static final int    NOTHING                  = 0;
    private static final int    FIRST_ROUND              = 1;
    private static final int    TIME_FORMAT              = 1000;
    private static final int    BASE_CORRECT_SCORE       = 50;
    private static final int    ATTEMPTS_LEFT_MULTIPLIER = 10;
    private static final int    ATTEMPTS_OFFSET          = 1;
    private static final String WORD_FILE_PATH           = "src/resources/words.txt";

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
     * Performs console setup: loads words async (using helper), gets names/words.
     *
     * @return true if successful, false otherwise.
     */
    public static boolean setupGameFromConsole()
    {
        final ExecutorService wordLoadExecutor;
        wordLoadExecutor = Executors.newSingleThreadExecutor();

        final Scanner consoleScanner;
        consoleScanner = new Scanner(System.in);

        boolean success = false;

        try
        {
            final Callable<Set<String>> loadTask = () ->
            {
                return loadAndProcessWords(WORD_FILE_PATH); // Call the helper
            };

            final Future<Set<String>> futureWords;
            futureWords = wordLoadExecutor.submit(loadTask);

            try
            {
                staticWordSet = futureWords.get(); // Wait and get result
            } catch (final ExecutionException e)
            {
                final Throwable cause = e.getCause() != null ? e.getCause() : e;
                System.err.println("Error during asynchronous word loading: " + cause.getMessage());
                return false;
            } catch (final InterruptedException e)
            {
                System.err.println("Setup interrupted while loading words.");
                Thread.currentThread().interrupt();
                return false;
            }

            System.out.println("Loaded " + staticWordSet.size() + " valid words.");

            if (staticWordSet == null || staticWordSet.isEmpty())
            {
                System.err.println("Word file processing failed or yielded no valid words.");
                return false;
            }

            System.out.print("Enter Player 1 Name: ");
            staticPlayer1Name = consoleScanner.nextLine().trim();

            if (staticPlayer1Name.isEmpty())
            {
                staticPlayer1Name = "Player 1";
            }

            System.out.print("Enter Player 2 Name: ");
            staticPlayer2Name = consoleScanner.nextLine().trim();

            if (staticPlayer2Name.isEmpty())
            {
                staticPlayer2Name = "Player 2";
            }

            System.out.println("\n--- " + staticPlayer1Name +
                               ", choose " + TOTAL_ROUNDS +
                               " words for " + staticPlayer2Name + " ---");
            staticWordsForPlayer2 = getWordsFromConsole(staticPlayer1Name, consoleScanner);

            if (staticWordsForPlayer2 == null)
            {
                return false;
            }

            System.out.println("\n--- " + staticPlayer2Name +
                               ", choose " + TOTAL_ROUNDS +
                               " words for " + staticPlayer1Name + " ---");
            staticWordsForPlayer1 = getWordsFromConsole(staticPlayer2Name, consoleScanner);

            if (staticWordsForPlayer1 == null)
            {
                return false;
            }

            success = true;

        } catch (final Exception e)
        {
            System.err.println("An unexpected error occurred during console setup: " + e.getMessage());
            e.printStackTrace();

        } finally
        {
            if (wordLoadExecutor != null)
            {
                wordLoadExecutor.shutdown();
            }
        }
        return success;
    }

    /**
     * Reads, checks, filters, and collects words from a file.
     * This method contains the core logic testable by unit tests.
     *
     * @param filename Path to the word file.
     * @return A Set of valid, uppercase, 5-letter words.
     * @throws IOException If file access or reading fails.
     */
    public static Set<String> loadAndProcessWords(final String filename) throws IOException
    {
        final Path filePath;
        filePath = Paths.get(filename);

        if (!Files.exists(filePath))
        {
            throw new IOException("Word file not found: " + filePath.toAbsolutePath());
        }

        return Files.lines(filePath)
                    .map(String::trim)
                    .filter(word -> word.length() == WORD_LENGTH) // Filter by length
                    .map(String::toUpperCase) // Convert to uppercase
                    .collect(Collectors.toSet()); // Collect into a Set
    }


    /**
     * Main entry point (for standalone execution).
     */
    public static void main(final String[] args)
    {
        System.out.println("Starting Twisted Wordle setup...");
        boolean setupOk = setupGameFromConsole();

        if (setupOk)
        {
            launch(args);
        }
        else
        {
            System.err.println("Twisted Wordle setup failed. Exiting.");
        }
    }

    /*
     * Reads the words from the console that the user wants their opponent to guess.
     */
    private static List<String> getWordsFromConsole(final String playerName,
                                                    final Scanner scanner)
    {
        final List<String> chosenWords;
        chosenWords = new ArrayList<>();

        for (int i = 0; i < TOTAL_ROUNDS; i++)
        {
            String  enteredWord;
            boolean valid = false;

            do
            {
                System.out.printf("  Enter word %d of %d (must be %d letters and in the list): ",
                                  i + 1, TOTAL_ROUNDS, WORD_LENGTH);

                if (!scanner.hasNextLine())
                {
                    System.err.println("\nERROR: No more input available from console.");
                    return null;
                }

                enteredWord = scanner.nextLine().trim().toUpperCase();

                if (enteredWord.length() != WORD_LENGTH)
                {
                    System.out.println("    ERROR: Word must be exactly " + WORD_LENGTH + " letters long.");
                }
                else if (staticWordSet == null || staticWordSet.isEmpty())
                {
                    System.err.println("    ERROR: Word list is not available for validation.");
                    return null;
                }
                else if (!staticWordSet.contains(enteredWord))
                {
                    System.out.println("    ERROR: '" + enteredWord + "' is not in the allowed word list.");
                }
                else
                {
                    valid = true;
                }
            } while (!valid);

            chosenWords.add(enteredWord);
        }

        System.out.println("  " + playerName + " finished choosing words.");
        return chosenWords;
    }

    /**
     * Launches the UI for the game
     *
     * @param callback the callback
     */
    public static void launchGame(final Runnable callback)
    {
        if (staticPlayer1Name == null ||
            staticWordsForPlayer1 == null ||
            staticWordsForPlayer2 == null ||
            staticWordSet == null)
        {
            System.err.println("Cannot launch game: Pre-game setup data missing or incomplete.");

            if (Platform.isFxApplicationThread())
            {
                Platform.runLater(() ->
                                  {
                                  });
            }

            if (callback != null)
            {
                Platform.runLater(callback);
            }
            return;
        }

        onCloseCallback = callback;

        Platform.runLater(() ->
                          {
                              try
                              {
                                  if (currentStage != null) currentStage.close();
                                  currentStage = new Stage();
                                  new TwistedWordle().start(currentStage);
                              } catch (final Exception e)
                              {
                                  e.printStackTrace();
                                  if (onCloseCallback != null) onCloseCallback.run();
                              }
                          });
    }

    /**
     * Overrides the JavaFX start method.
     *
     * @param stage the stage for the UI
     */
    @Override
    public void start(final Stage stage)
    {
        this.primaryStage = stage;
        primaryStage.setOnHidden(e ->
                                 {
                                     if (timer != null) timer.stop();
                                     if (onCloseCallback != null) Platform.runLater(onCloseCallback);
                                 });

        primaryStage.setTitle("Twisted Wordle");

        try
        {
            player1       = new Player(staticPlayer1Name);
            player2       = new Player(staticPlayer2Name);
            currentPlayer = player1;

            if (staticWordsForPlayer1 == null ||
                staticWordsForPlayer2 == null ||
                staticWordsForPlayer1.size() != TOTAL_ROUNDS ||
                staticWordsForPlayer2.size() != TOTAL_ROUNDS)
            {
                throw new IllegalStateException("Word lists not properly initialized before GUI start.");
            }

            initializeGameUI(stage);
            startTurn(currentPlayer);

        } catch (final Exception e)
        {
            e.printStackTrace();
            if (primaryStage != null) Platform.runLater(primaryStage::close);
        }
    }

    /**
     * Sets up the main game UI elements.
     *
     * @param stage the stage for the UI
     */
    private void initializeGameUI(final Stage stage)
    {
        attemptsLeft = MAX_ATTEMPTS;
        currentRound = FIRST_ROUND;

        final GridPane gridPane;
        gridPane = new GridPane();

        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));
        gridLabels = new Label[MAX_ATTEMPTS][WORD_LENGTH];

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

        inputField = new TextField();
        inputField.setFont(Font.font(20));
        inputField.setMaxWidth(200);
        inputField.setDisable(true);
        inputField.setOnKeyPressed(e ->
                                   {
                                       if (e.getCode() == KeyCode.ENTER) handleGuess();
                                   });

        final Button submitButton;
        submitButton = new Button("Submit");

        submitButton.setDisable(true);
        submitButton.setOnAction(e -> handleGuess());
        submitButton.disableProperty().bind(inputField.disabledProperty());

        final HBox inputBox;
        inputBox = new HBox(10, inputField, submitButton);

        inputBox.setAlignment(Pos.CENTER);
        messageLabel = new Label("Initializing...");
        messageLabel.setFont(Font.font(20));
        timerLabel = new Label("Time left: --");
        timerLabel.setFont(Font.font(20));
        scoreLabel = new Label("Scores: " + player1.getName() + ": 0 | " + player2.getName() + ": 0");
        scoreLabel.setFont(Font.font(20));
        roundLabel = new Label("Round: " + currentRound + " of " + TOTAL_ROUNDS);
        roundLabel.setFont(Font.font(20));

        final VBox root;
        root = new VBox(20,
                        gridPane,
                        inputBox,
                        messageLabel,
                        timerLabel,
                        scoreLabel,
                        roundLabel);

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        final Scene scene;
        scene = new Scene(root, 600, 700);

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Handles the submission and evaluation of a guess.
     */
    private void handleGuess()
    {
        final String guess;
        guess = inputField.getText().toUpperCase();
        inputField.clear();

        if (guess.length() != WORD_LENGTH)
        {
            messageLabel.setText("Please enter a 5-letter word.");
            return;
        }

        final int currentAttempt;
        currentAttempt = MAX_ATTEMPTS - attemptsLeft;

        final boolean[] targetMatched;
        targetMatched = new boolean[WORD_LENGTH];

        final boolean[] guessMatched;
        guessMatched = new boolean[WORD_LENGTH];

        // first pass for green letters
        for (int i = 0; i < WORD_LENGTH; i++)
        {
            gridLabels[currentAttempt][i].setText(String.valueOf(guess.charAt(i)));

            if (guess.charAt(i) == targetWord.charAt(i))
            {
                gridLabels[currentAttempt][i].setStyle("-fx-background-color: #6aaa64; " +
                                                       "-fx-text-fill: white; " +
                                                       "-fx-border-color: #444; " +
                                                       "-fx-border-width: 1;");
                targetMatched[i] = true;
                guessMatched[i]  = true;
            }
            else
            {
                gridLabels[currentAttempt][i].setStyle("-fx-background-color: #787c7e; " +
                                                       "-fx-text-fill: white; " +
                                                       "-fx-border-color: #444; " +
                                                       "-fx-border-width: 1;");
            }
        }

        // second pass for yellow letters
        for (int i = 0; i < WORD_LENGTH; i++)
        {
            if (!guessMatched[i])
            {
                char guessedChar = guess.charAt(i);

                for (int j = 0; j < WORD_LENGTH; j++)
                {
                    if (!targetMatched[j] && guessedChar == targetWord.charAt(j))
                    {
                        gridLabels[currentAttempt][i].setStyle("-fx-background-color: #c9b458; " +
                                                               "-fx-text-fill: white; " +
                                                               "-fx-border-color: #444;" +
                                                               " -fx-border-width: 1;");
                        targetMatched[j] = true;
                        guessMatched[i]  = true;
                        break;
                    }
                }
            }
        }

        attemptsLeft--;

        if (guess.equals(targetWord))
        {
            if (timer != null)
            {
                timer.stop();
            }

            long elapsedTime = (System.currentTimeMillis() - startTime) / TIME_FORMAT;
            int  timeLeft    = Math.max(NOTHING, TURN_TIME - (int) elapsedTime);
            int  score       = calculateScore(attemptsLeft + ATTEMPTS_OFFSET, timeLeft);
            currentPlayer.addScore(score);
            messageLabel.setText("Correct! " + currentPlayer.getName() + " guessed the word!");
            inputField.setDisable(true);
            updateScoreboard();

            final PauseTransition delay;
            delay = new PauseTransition(Duration.seconds(DELAY));

            delay.setOnFinished(e -> Platform.runLater(this::prepareNextTurn));
            delay.play();
        }
        else if (attemptsLeft == NOTHING)
        {
            if (timer != null)
            {
                timer.stop();
            }

            messageLabel.setText("Out of attempts! The word was: " + targetWord);
            inputField.setDisable(true);

            final PauseTransition delay;
            delay = new PauseTransition(Duration.seconds(DELAY));

            delay.setOnFinished(e -> Platform.runLater(this::prepareNextTurn));
            delay.play();
        }
        else
        {
            messageLabel.setText(currentPlayer.getName() + ", attempts left: " + attemptsLeft);
        }
    }

    /**
     * Prepares for the next turn
     */
    public void prepareNextTurn()
    {
        if (timer != null)
        {
            timer.stop();
            timer = null;
        }

        timerLabel.setText("Time left: --");

        final Player nextPlayer;

        if (currentPlayer == player1)
        {
            nextPlayer = player2;
        }
        else
        {
            nextPlayer = player1;
            currentRound++;

            if (currentRound > TOTAL_ROUNDS)
            {
                endGame();
                return;
            }

            roundLabel.setText("Round: " + currentRound + " of " + TOTAL_ROUNDS);
        }
        startTurn(nextPlayer);
    }

    /**
     * Starts the actual guessing turn for the player.
     *
     * @param player the player who is guessing
     */
    private void startTurn(final Player player)
    {
        currentPlayer = player;
        attemptsLeft  = MAX_ATTEMPTS;
        int roundIndex = currentRound - FIRST_ROUND;

        final List<String> wordListForThisPlayer;

        if (currentPlayer == player1)
        {
            wordListForThisPlayer = staticWordsForPlayer1;
        }
        else
        {
            wordListForThisPlayer = staticWordsForPlayer2;
        }

        if (wordListForThisPlayer == null ||
            roundIndex < NOTHING ||
            roundIndex >= wordListForThisPlayer.size())
        {
            endGame();
            return;
        }

        targetWord = wordListForThisPlayer.get(roundIndex);
        resetGrid();
        messageLabel.setText(currentPlayer.getName() + ", guess the 5-letter word! (" + MAX_ATTEMPTS + " attempts)");
        inputField.setDisable(false);
        Platform.runLater(() -> inputField.requestFocus());
        updateScoreboard();
        roundLabel.setText("Round: " + currentRound + " of " + TOTAL_ROUNDS);
        startTimer();
    }

    /**
     * Resets the guess grid UI.
     */
    private void resetGrid()
    {
        for (int row = 0; row < MAX_ATTEMPTS; row++)
        {
            for (int col = 0; col < WORD_LENGTH; col++)
            {
                gridLabels[row][col].setText("");
                gridLabels[row][col].setStyle("-fx-border-color: black; " +
                                              "-fx-border-width: 2; " +
                                              "-fx-background-color: transparent;");
            }
        }
    }

    /**
     * Calculates score based on attempts and time.
     *
     * @param attemptsLeftBeforeGuess attempts the player had unused
     * @param timeLeft                time left
     */
    @Override
    public int calculateScore(final int attemptsLeftBeforeGuess,
                              final int timeLeft)
    {
        int attemptsScore;

        if (attemptsLeftBeforeGuess > NOTHING)
        {
            attemptsScore = attemptsLeftBeforeGuess * ATTEMPTS_LEFT_MULTIPLIER;
        }
        else
        {
            attemptsScore = NOTHING;
        }
        return BASE_CORRECT_SCORE + attemptsScore + Math.max(NOTHING, timeLeft);
    }

    /**
     * Updates the score display label.
     */
    private void updateScoreboard()
    {
        scoreLabel.setText("Scores: " +
                           player1.getName() + ": " +
                           player1.getScore() + " | " +
                           player2.getName() + ": " +
                           player2.getScore());
    }

    /**
     * Starts the turn timer animation.
     */
    private void startTimer()
    {
        if (timer != null)
        {
            timer.stop();
        }

        startTime = System.currentTimeMillis();

        timer = new AnimationTimer()
        {
            /**
             * Anonymous inner class to handle the start of the timer.
             * @param now the current time in long form
             */
            @Override
            public void handle(final long now)
            {
                long elapsedTime;
                elapsedTime = (System.currentTimeMillis() - startTime) / TIME_FORMAT;

                int timeLeft;
                timeLeft = TURN_TIME - (int) elapsedTime;

                if (timeLeft <= NOTHING)
                {
                    timerLabel.setText("Time left: 0");
                    this.stop();
                    timer = null;
                    messageLabel.setText("Time's up, " + currentPlayer.getName() + "! The word was: " + targetWord);
                    inputField.setDisable(true);

                    final PauseTransition delay;
                    delay = new PauseTransition(Duration.seconds(DELAY));

                    delay.setOnFinished(e ->
                                                Platform.runLater(() -> TwistedWordle.this.prepareNextTurn()));
                    delay.play();
                }
                else
                {
                    timerLabel.setText("Time left: " + timeLeft);
                }
            }
        };
        timer.start();
    }

    /**
     * Ends the game, stops timers, shows final scores.
     */
    private void endGame()
    {
        if (timer != null)
        {
            timer.stop();
            timer = null;
        }

        messageLabel.setText("Game over! Final scores:");
        inputField.setDisable(true);
        updateScoreboard();
        Platform.runLater(this::showWinnerPopup);
    }

    /**
     * Shows the final winner/tie popup.
     */
    private void showWinnerPopup()
    {
        final Stage popupStage;
        popupStage = new Stage();

        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initOwner(primaryStage);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.setTitle("Game Over");

        final String winnerMessage;

        if (player1.getScore() > player2.getScore())
        {
            winnerMessage = player1.getName() + " wins!";
        }
        else if (player2.getScore() > player1.getScore())
        {
            winnerMessage = player2.getName() + " wins!";
        }
        else
        {
            winnerMessage = "It's a tie!";
        }

        final Label finalScoreLabel;
        finalScoreLabel = new Label(player1.getName() + ": " +
                                    player1.getScore() + "\n" +
                                    player2.getName() + ": " +
                                    player2.getScore());

        finalScoreLabel.setFont(Font.font(16));

        final Label winnerLabel;
        winnerLabel = new Label(winnerMessage);

        winnerLabel.setFont(Font.font(20));
        winnerLabel.setStyle("-fx-font-weight: bold;");

        final Button closeButton;
        closeButton = new Button("Close Game");

        closeButton.setOnAction(e ->
                                {
                                    popupStage.close();
                                    if (primaryStage != null) primaryStage.close();
                                    if (onCloseCallback != null) Platform.runLater(onCloseCallback);
                                });

        final VBox popupLayout;
        popupLayout = new VBox(15,
                               finalScoreLabel,
                               winnerLabel,
                               closeButton);

        popupLayout.setAlignment(Pos.CENTER);
        popupLayout.setPadding(new Insets(25));

        final Scene popupScene;
        popupScene = new Scene(popupLayout, 350, 200);

        popupStage.setScene(popupScene);
        popupStage.showAndWait();
    }
}
