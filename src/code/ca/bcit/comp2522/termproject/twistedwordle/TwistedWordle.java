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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * A multiplayer wordle game using JavaFX,
 *
 * @author colecampbell
 * @version 1.0
 */
public class TwistedWordle extends Application implements Scorable
{

    private static final int MAX_ATTEMPTS = 6;
    private static final int WORD_LENGTH  = 5;
    private static final int TURN_TIME    = 90;
    private static final int TOTAL_ROUNDS = 1;
    private static final int DELAY        = 5;
    private static final int NOTHING      = 0;

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
    private List<String>   wordBatch;
    private long           startTime;
    private AnimationTimer timer;
    private int            currentRound;

    /**
     * The entry point for this program.
     * @param args unused
     */
    public static void main(final String[] args)
    {
        launch(args);
    }

    /**
     * Overrides the javaFX start method.
     * @param primaryStage the stage
     */
    @Override
    public void start(final Stage primaryStage)
    {
        primaryStage.setTitle("Wordle Game");

        // Load words from file (Lecture 7: File NIO.2)
        wordBatch = loadWordsFromFile("src/resources/words.txt");
        if (wordBatch.isEmpty())
        {
            System.out.println("No words found in the file. Exiting.");
            return;
        }

        // Get player names from the console
        final Scanner input;
        input = new Scanner(System.in);
        System.out.print("Enter Player 1 Name: ");
        player1 = new Player(input.nextLine());
        System.out.print("\nEnter Player 2 Name: ");
        player2       = new Player(input.nextLine());
        currentPlayer = player1;

        // Initialize game
        targetWord   = wordBatch.get(new Random().nextInt(wordBatch.size())).toUpperCase();
        attemptsLeft = MAX_ATTEMPTS;
        currentRound = 1;

        // Create the grid for displaying guesses (Lecture 10: Graphical User Interfaces)
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

        // Input field and submit button (Lecture 10: Graphical User Interfaces)
        inputField = new TextField();
        inputField.setFont(Font.font(20));
        inputField.setMaxWidth(200);

        // Allow submitting guesses using the Enter key
        inputField.setOnKeyPressed(e ->
                                   {
                                       if (e.getCode() == KeyCode.ENTER)
                                       {
                                           handleGuess();
                                       }
                                   });

        final Button submitButton;
        submitButton = new Button("Submit");
        submitButton.setOnAction(e -> handleGuess());

        final HBox inputBox;
        inputBox = new HBox(10, inputField, submitButton);
        inputBox.setAlignment(Pos.CENTER);

        // Message label
        messageLabel = new Label(currentPlayer.getName() + ", guess the 5-letter word!");
        messageLabel.setFont(Font.font(20));

        // Timer label
        timerLabel = new Label("Time left: " + TURN_TIME);
        timerLabel.setFont(Font.font(20));

        // Scoreboard
        scoreLabel = new Label("Scores: " + player1.getName() + ": " + player1.getScore() + " | " + player2.getName() + ": " + player2.getScore());
        scoreLabel.setFont(Font.font(20));

        // Round label
        roundLabel = new Label("Round: " + currentRound + " of " + TOTAL_ROUNDS);
        roundLabel.setFont(Font.font(20));

        // Main layout
        final VBox root;
        root = new VBox(20, gridPane, inputBox, messageLabel, timerLabel, scoreLabel, roundLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        final Scene scene;
        scene = new Scene(root, 600, 700); // Increased width to 600
        primaryStage.setScene(scene);
        primaryStage.show();

        // Show the instructions popup after the main window is displayed
        showInstructionsPopup(primaryStage);
    }

    /* A popup explaining the instructions for the game. */
    private void showInstructionsPopup(final Stage primaryStage)
    {
        final Stage popupStage;
        popupStage = new Stage();
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.initOwner(primaryStage); // Set the main window as the owner
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.setTitle("How to Play");

        final Label rulesLabel = new Label(
                "Welcome to Twisted Wordle!\n\n" +
                "1. Each player takes turns guessing a 5-letter word.\n" +
                "2. You have " + MAX_ATTEMPTS + " attempts to guess the word.\n" +
                "3. Green letters are correct and in the right position.\n" +
                "4. Yellow letters are correct but in the wrong position.\n" +
                "5. Gray letters are not in the word.\n" +
                "6. The game has " + TOTAL_ROUNDS + " rounds. The player with the highest score wins!\n\n" +
                "Press 'Start' to begin!"
        );
        rulesLabel.setFont(Font.font(16));
        rulesLabel.setWrapText(true);

        final Button startButton = new Button("Start");
        startButton.setOnAction(e ->
                                {
                                    popupStage.close();
                                    startTimer(); // Start the timer after the popup is closed
                                });

        final VBox popupLayout;
        popupLayout = new VBox(20, rulesLabel, startButton);
        popupLayout.setAlignment(Pos.CENTER);
        popupLayout.setPadding(new Insets(20));

        final Scene popupScene;
        popupScene = new Scene(popupLayout, 400, 300);
        popupStage.setScene(popupScene);
        popupStage.showAndWait();
    }

    /* Loads the 5 letter words from words.txt */
    private List<String> loadWordsFromFile(final String filename)
    {
        try
        {
            // (Lecture 8: Streams and Paths)
            return Files.readAllLines(Paths.get(filename))
                        .stream()
                        .filter(word -> word.length() == WORD_LENGTH)
                        .collect(Collectors.toList());
        } catch (final IOException e)
        {
            System.out.println("Error reading file: " + filename);
            return new ArrayList<>();
        }
    }

    /* Calculates and displays guess result. */
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

        // Track which letters in the target word have already been matched
        final boolean[] targetMatched;
        targetMatched = new boolean[WORD_LENGTH];
        // Track which letters in the guess have already been marked as green or yellow
        final boolean[] guessMatched;
        guessMatched = new boolean[WORD_LENGTH];

        // First pass: Mark green letters (correct letter in correct position)
        for (int i = 0; i < WORD_LENGTH; i++)
        {
            if (guess.charAt(i) == targetWord.charAt(i))
            {
                gridLabels[currentAttempt][i].setText(String.valueOf(guess.charAt(i)));
                gridLabels[currentAttempt][i].setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-border-color: black; -fx-border-width: 2;");
                targetMatched[i] = true;
                guessMatched[i]  = true;
            }
        }

        // Second pass: Mark yellow letters (correct letter in wrong position)
        for (int i = 0; i < WORD_LENGTH; i++)
        {
            if (!guessMatched[i])
            {
                final char guessedChar;
                guessedChar = guess.charAt(i);
                for (int j = 0; j < WORD_LENGTH; j++)
                {
                    if (!targetMatched[j] && guessedChar == targetWord.charAt(j))
                    {
                        gridLabels[currentAttempt][i].setText(String.valueOf(guessedChar));
                        gridLabels[currentAttempt][i].setStyle("-fx-background-color: yellow; -fx-text-fill: black; -fx-border-color: black; -fx-border-width: 2;");
                        targetMatched[j] = true;
                        guessMatched[i]  = true;
                        break;
                    }
                }
                // If the letter wasn't matched, mark it as gray
                if (!guessMatched[i])
                {
                    gridLabels[currentAttempt][i].setText(String.valueOf(guessedChar));
                    gridLabels[currentAttempt][i].setStyle("-fx-background-color: gray; -fx-text-fill: white; -fx-border-color: black; -fx-border-width: 2;");
                }
            }
        }

        attemptsLeft--;

        if (guess.equals(targetWord))
        {
            // Calculate time left
            final long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
            final int timeLeft = TURN_TIME - (int) elapsedTime;

            // Calculate score based on attempts left and time left
            final int score = calculateScore(attemptsLeft, timeLeft);
            currentPlayer.addScore(score);

            messageLabel.setText("Congratulations, " + currentPlayer.getName() + "! You've guessed the word!");
            inputField.setDisable(true);
            updateScoreboard();
            // Stop the timer
            timer.stop();
            // Add a delay before switching players
            final PauseTransition delay;
            delay = new PauseTransition(Duration.seconds(DELAY)); // 5-second delay
            delay.setOnFinished(e -> switchPlayer());
            delay.play();
        }
        else if (attemptsLeft == NOTHING)
        {
            messageLabel.setText("Game over! The word was: " + targetWord);
            inputField.setDisable(true);
            // Stop the timer
            timer.stop();
            // Add a delay before switching players
            final PauseTransition delay;
            delay = new PauseTransition(Duration.seconds(DELAY)); // 5-second delay
            delay.setOnFinished(e -> switchPlayer());
            delay.play();
        }
        else
        {
            messageLabel.setText(currentPlayer.getName() + ", attempts left: " + attemptsLeft);
        }
    }

    /* Switches to the other players turn. */
    private void switchPlayer()
    {
        if (currentPlayer == player1)
        {
            currentPlayer = player2;
        }
        else
        {
            currentPlayer = player1;
            currentRound++;
            if (currentRound > TOTAL_ROUNDS)
            {
                endGame();
                return;
            }
            roundLabel.setText("Round: " + currentRound + " of " + TOTAL_ROUNDS);
        }
        targetWord   = wordBatch.get(new Random().nextInt(wordBatch.size())).toUpperCase();
        attemptsLeft = MAX_ATTEMPTS;
        resetGrid();
        messageLabel.setText(currentPlayer.getName() + ", guess the 5-letter word!");
        inputField.setDisable(false);
        startTimer();
    }

    /* Resets the wordle grid for the next player. */
    private void resetGrid()
    {
        for (int row = 0; row < MAX_ATTEMPTS; row++)
        {
            for (int col = 0; col < WORD_LENGTH; col++)
            {
                gridLabels[row][col].setText("");
                gridLabels[row][col].setStyle("-fx-border-color: black; -fx-border-width: 2;");
            }
        }
    }

    /* Calculate score based on attempts left and time left. */
    @Override
    public int calculateScore(final int attemptsLeft, final int timeLeft)
    {
        // Base score for guessing the word
        final int baseScore = 50;

        // Bonus for remaining attempts (10 points per attempt)
        final int attemptsBonus = attemptsLeft * 10;

        // Total score
        return baseScore + attemptsBonus + timeLeft;
    }

    /* Update the scoreboard. */
    private void updateScoreboard()
    {
        scoreLabel.setText("Scores: " + player1.getName() + ": " + player1.getScore() + " | " + player2.getName() + ": " + player2.getScore());
    }

    /* Start the timer for the current player's turn. */
    private void startTimer()
    {
        startTime = System.currentTimeMillis();
        timer     = new AnimationTimer()
        {
            @Override
            public void handle(long now)
            {
                long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
                int  timeLeft    = TURN_TIME - (int) elapsedTime;
                timerLabel.setText("Time left: " + timeLeft);

                if (timeLeft <= 0)
                {
                    timer.stop();
                    messageLabel.setText("Time's up! The word was: " + targetWord);
                    inputField.setDisable(true);
                    // Add a delay before switching players
                    PauseTransition delay = new PauseTransition(Duration.seconds(5)); // 5-second delay
                    delay.setOnFinished(e -> switchPlayer());
                    delay.play();
                }
            }
        };
        timer.start();
    }

    /* End the game and display the final scores. */
    private void endGame()
    {
        timer.stop();
        messageLabel.setText("Game over! Final scores: " + player1.getName() + ": " + player1.getScore() + " | " + player2.getName() + ": " + player2.getScore());
        inputField.setDisable(true);

        // Use Platform.runLater to show the popup after the animation/timer has completed
        Platform.runLater(this::showWinnerPopup);
    }

    /* Show a popup box with the winner and a close button. */
    private void showWinnerPopup()
    {
        final Stage popupStage;
        popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
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

        final Label winnerLabel;
        winnerLabel = new Label(winnerMessage);
        winnerLabel.setFont(Font.font(20));

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e ->
                                {
                                    popupStage.close();
                                    ((Stage) messageLabel.getScene().getWindow()).close(); // Close the main window
                                });

        final VBox popupLayout;
        popupLayout = new VBox(20, winnerLabel, closeButton);
        popupLayout.setAlignment(Pos.CENTER);
        popupLayout.setPadding(new Insets(20));

        final Scene popupScene;
        popupScene = new Scene(popupLayout, 300, 150);
        popupStage.setScene(popupScene);
        popupStage.showAndWait();
    }

    /* Nested inner class for a player. */
    private static class Player
    {
        private final String name;
        private       int    score;

        public Player(final String name)
        {
            this.name  = name;
            this.score = 0;
        }

        public String getName()
        {
            return name;
        }

        public int getScore()
        {
            return score;
        }

        public void addScore(final int points)
        {
            score += points;
        }
    }
}
