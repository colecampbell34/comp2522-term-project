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
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom multiplayer wordle game. Words chosen via console before GUI starts.
 * Words must be part of the text file in resources to be considered.
 * Players have 6 guesses each round, scores are calculated based on
 * time, remaining guesses and correct/incorrect.
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
        final Scanner consoleScanner;
        consoleScanner = new Scanner(System.in);

        boolean success;
        success = false;

        try {
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

        } catch (final Exception e) {
            e.printStackTrace();
        }

        return success;
    }

    /*
     * Validates a word set.
     */
    private static void validateStaticWordSet(final Set<String> staticWordSet)
    {
        if (staticWordSet == null ||
            staticWordSet.isEmpty()) {
            throw new IllegalArgumentException("Word set cannot be null");
        }
    }

    /*
     * Validates a player name.
     */
    private static void validatePlayerName(final String name)
    {
        if (name == null ||
            name.isBlank())
        {
            throw new IllegalArgumentException("Name should not be null or blank");
        }
    }

    /*
     * Validates a word list.
     */
    private static void validateWordsForPlayer(final List<String> words)
    {
        if (words == null ||
            words.size() != TOTAL_ROUNDS)
        {
            throw new IllegalStateException("Word list for player is invalid");
        }
    }

    /**
     * Reads, checks, filters, and collects words from a file.
     * This method contains the core logic testable by unit tests.
     *
     * @param filename Path to the word file.
     * @return A Set of valid, uppercase, 5-letter words.
     */
    public static Set<String> loadAndProcessWords(final String filename)
    throws IOException
    {
        validateFileName(filename);

        final Path filePath;
        filePath = Paths.get(filename);

        validateFileExistence(filePath);

        // return only the valid 5-letter words
        return Files.readAllLines(filePath)
                    .stream()
                    .map(String::trim)
                    .filter(word -> word.length() == WORD_LENGTH)
                    .map(String::toUpperCase)
                    .collect(Collectors.toSet());
    }

    /*
     * Validates a file name.
     */
    private static void validateFileName(final String fileName)
    {
        if (fileName == null ||
            fileName.isBlank())
        {
            throw new IllegalArgumentException("Invalid file name");
        }
    }

    /*
     * Validates that the file exists.
     */
    private static void validateFileExistence(final Path filePath)
    {
        if (!Files.exists(filePath))
        {
            throw new IllegalArgumentException("Word file not found");
        }
    }

    /**
     * Main entry point.
     *
     * @param args unused.
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
        validatePlayerName(playerName);
        validateScannerObject(scanner);

        final List<String> chosenWords;
        chosenWords = new ArrayList<>();

        // get 3 valid words from the user
        for (int i = 0; i < TOTAL_ROUNDS; i++)
        {
            String  enteredWord;
            boolean validWord;

            do
            {
                System.out.printf("  Enter word %d of %d (must be %d letters and in the list): ",
                                  i + OFFSET, TOTAL_ROUNDS, WORD_LENGTH);

                enteredWord = scanner.nextLine().trim().toUpperCase();

                validateStaticWordSet(staticWordSet);

                validWord = validateWordLength(enteredWord) &&
                            validateWordInWordList(enteredWord);

            } while (!validWord);

            chosenWords.add(enteredWord);
        }

        System.out.println("  " +
                           playerName +
                           " finished choosing words.");

        return chosenWords;
    }

    /*
     * Validates a scanner object.
     */
    private static void validateScannerObject(final Scanner scanner)
    {
        if (scanner == null)
        {
            throw new IllegalArgumentException("Scanner cannot be null");
        }
    }

    /*
     * Validates that the word length matches the game.
     */
    private static boolean validateWordLength(final String word)
    {
        if (word.length() != WORD_LENGTH)
        {
            System.out.println("    ERROR: Word must be exactly " +
                               WORD_LENGTH + " letters long.");
            return false;
        }

        return true;
    }

    /*
     * Validates that the word is in the list of allowed words.
     */
    private static boolean validateWordInWordList(final String word)
    {
        if (!staticWordSet.contains(word))
        {
            System.out.println("    ERROR: '" +
                               word +
                               "' is not in the allowed word list.");
            return false;
        }

        return true;
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
                Platform.runLater(() -> {});
            }

            validateCallback(callback);
            Platform.runLater(callback);

            return;
        }

        onCloseCallback = callback;

        Platform.runLater(() ->
                          {
                              try
                              {
                                  validateStage(currentStage);
                                  currentStage.close();

                                  currentStage = new Stage();
                                  new TwistedWordle().start(currentStage);

                              } catch (final Exception e)
                              {
                                  e.printStackTrace();

                                  validateCallback(onCloseCallback);
                                  onCloseCallback.run();
                              }
                          });
    }

    /*
     * Validates a callback object.
     */
    private static void validateCallback(final Runnable callback)
    {
        if (callback == null)
        {
            throw new IllegalArgumentException("Callback cannot be null");
        }
    }

    /*
     * Validates a stage object.
     */
    private static void validateStage(final Stage stage)
    {
        if (stage == null)
        {
            throw new IllegalArgumentException("Stage cannot be null");
        }
    }

    /**
     * Overrides the JavaFX start method.
     *
     * @param stage the stage for the UI
     */
    @Override
    public void start(final Stage stage)
    {
        validateStage(stage);

        this.primaryStage = stage;
        primaryStage.setOnHidden(e ->
                                 {
                                     validateTimer(timer);
                                     timer.stop();

                                     validateCallback(onCloseCallback);
                                     Platform.runLater(onCloseCallback);
                                 });

        primaryStage.setTitle("Twisted Wordle");

        // try to set up everything and start the game
        try
        {
            player1 = PlayerFactory.createPlayer(staticPlayer1Name);
            player2 = PlayerFactory.createPlayer(staticPlayer2Name);

            currentPlayer = player1;

            validateWordsForPlayer(staticWordsForPlayer1);
            validateWordsForPlayer(staticWordsForPlayer2);

            initializeGameUI(stage);
            startTurn(currentPlayer);

        } catch (final Exception e)
        {
            e.printStackTrace();

            validateStage(primaryStage);
            Platform.runLater(primaryStage::close);
        }
    }

    /*
     * Validates a label object.
     */
    private static void validateTimer(final AnimationTimer timer)
    {
        if (timer == null)
        {
            throw new IllegalArgumentException("Timer cannot be null");
        }
    }

    /**
     * Sets up the main game UI elements.
     *
     * @param stage the stage for the UI
     */
    private void initializeGameUI(final Stage stage)
    {
        validateStage(stage);

        attemptsLeft = MAX_ATTEMPTS;
        currentRound = FIRST_ROUND;

        final GridPane gridPane;
        gridPane = new GridPane();

        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));
        gridLabels = new Label[MAX_ATTEMPTS][WORD_LENGTH];

        // set all the squares to blank
        for (int row = 0; row < MAX_ATTEMPTS; row++)
        {
            for (int col = 0; col < WORD_LENGTH; col++)
            {
                final Label label;
                label = new Label("");

                label.setFont(Font.font(20));
                label.setMinSize(40, 40);
                label.setAlignment(Pos.CENTER);
                label.setStyle("-fx-border-color: black; " +
                               "-fx-border-width: 2;");
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
                                       if (e.getCode() == KeyCode.ENTER)
                                       {
                                           handleGuess();
                                       }
                                   });

        // the button for guessing
        final Button submitButton;
        submitButton = new Button("Guess");

        submitButton.setDisable(true);
        submitButton.setOnAction(e -> handleGuess());
        submitButton.disableProperty().bind(inputField.disabledProperty());

        // initialize the info for the game
        final HBox inputBox;
        inputBox = new HBox(10, inputField, submitButton);

        inputBox.setAlignment(Pos.CENTER);
        messageLabel = new Label("Initializing...");
        messageLabel.setFont(Font.font(20));
        timerLabel = new Label("Time left: --");
        timerLabel.setFont(Font.font(20));
        scoreLabel = new Label("Scores: " +
                               player1.getName() +
                               ": 0 | " +
                               player2.getName() +
                               ": 0");
        scoreLabel.setFont(Font.font(20));
        roundLabel = new Label("Round: " +
                               currentRound +
                               " of " +
                               TOTAL_ROUNDS);
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
        final boolean[] targetMatched;
        final boolean[] guessMatched;

        currentAttempt = MAX_ATTEMPTS - attemptsLeft;
        targetMatched = new boolean[WORD_LENGTH];
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
            validateTimer(timer);
            timer.stop();

            final long elapsedTime;
            final int  timeLeft;
            final int  score;

            elapsedTime = (System.currentTimeMillis() - startTime) / TIME_FORMAT;
            timeLeft = Math.max(NOTHING, TURN_TIME - (int) elapsedTime);
            score = calculateScore(attemptsLeft + OFFSET, timeLeft);

            currentPlayer.addScore(score);

            messageLabel.setText("Correct! " +
                                 currentPlayer.getName() +
                                 " guessed the word!");

            inputField.setDisable(true);
            updateScoreboard();

            final PauseTransition delay;
            delay = new PauseTransition(Duration.seconds(DELAY));

            delay.setOnFinished(e -> Platform.runLater(this::prepareNextTurn));
            delay.play();
        }
        else if (attemptsLeft == NOTHING)
        {
            validateTimer(timer);
            timer.stop();

            messageLabel.setText("Out of attempts! The word was: " +
                                 targetWord);
            inputField.setDisable(true);

            final PauseTransition delay;
            delay = new PauseTransition(Duration.seconds(DELAY));

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
     * Prepares for the next turn
     */
    public void prepareNextTurn()
    {
        validateTimer(timer);
        timer.stop();
        timer = null;

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

            roundLabel.setText("Round: " + currentRound +
                               " of " + TOTAL_ROUNDS);
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
        validatePlayer(player);

        currentPlayer = player;
        attemptsLeft  = MAX_ATTEMPTS;

        int roundIndex;
        roundIndex = currentRound - FIRST_ROUND;

        final List<String> wordListForThisPlayer;

        // make sure we are assigning the correct
        // word list for each player
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

        // display the guess prompt
        messageLabel.setText(currentPlayer.getName() +
                             ", guess the 5-letter word! (" +
                             MAX_ATTEMPTS + " attempts)");

        inputField.setDisable(false);

        Platform.runLater(() -> inputField.requestFocus());
        updateScoreboard();

        roundLabel.setText("Round: " + currentRound +
                           " of " + TOTAL_ROUNDS);
        startTimer();
    }

    /*
     * Validates a player object.
     */
    private static void validatePlayer(final Player player)
    {
        if (player == null)
        {
            throw new IllegalStateException("Player cannot be null");
        }
    }

    /**
     * Resets the guess grid UI.
     */
    private void resetGrid()
    {
        // set all squares to blank
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
        validateNumberForCalculation(attemptsLeftBeforeGuess);
        validateNumberForCalculation(timeLeft);

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

    /*
     * Validates a number for score calculation
     */
    private static void validateNumberForCalculation(final int num)
    {
        if (num < NOTHING)
        {
            throw new IllegalArgumentException("Number for calculation cannot be negative");
        }
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
        validateTimer(timer);
        timer.stop();

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
                final long elapsedTime;
                final int timeLeft;

                elapsedTime = (System.currentTimeMillis() - startTime) / TIME_FORMAT;
                timeLeft = TURN_TIME - (int) elapsedTime;

                // time is up
                if (timeLeft <= NOTHING)
                {
                    timerLabel.setText("Time left: 0");

                    // stop the timer
                    this.stop();
                    timer = null;

                    messageLabel.setText("Time's up, " + currentPlayer.getName() +
                                         "! The word was: " + targetWord);
                    inputField.setDisable(true);

                    final PauseTransition delay;
                    delay = new PauseTransition(Duration.seconds(DELAY));

                    delay.setOnFinished(e -> Platform.runLater(
                            TwistedWordle.this::prepareNextTurn));
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
        validateTimer(timer);
        timer.stop();
        timer = null;

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
            winnerMessage = player1.getName() +
                            " wins!";
        }
        else if (player2.getScore() > player1.getScore())
        {
            winnerMessage = player2.getName() +
                            " wins!";
        }
        else
        {
            winnerMessage = "It's a tie!";
        }

        // the final label for the score
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

        // close the game window
        closeButton.setOnAction(e ->
                                {
                                    popupStage.close();

                                    validateStage(popupStage);
                                    primaryStage.close();

                                    validateCallback(onCloseCallback);
                                    Platform.runLater(onCloseCallback);
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
