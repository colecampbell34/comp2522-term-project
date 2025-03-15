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
public class TwistedWordle extends Application
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

    public static void main(final String[] args)
    {
        launch(args);
    }

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
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));

        gridLabels = new Label[MAX_ATTEMPTS][WORD_LENGTH];
        for (int row = 0; row < MAX_ATTEMPTS; row++)
        {
            for (int col = 0; col < WORD_LENGTH; col++)
            {
                Label label = new Label("");
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

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> handleGuess());

        HBox inputBox = new HBox(10, inputField, submitButton);
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
        VBox root = new VBox(20, gridPane, inputBox, messageLabel, timerLabel, scoreLabel, roundLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 600, 700); // Increased width to 600
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start the timer
        startTimer();
    }

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

    // Handle player's guess (Lecture 2: Exception Handling)
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
            currentPlayer.addScore(calculateScore(attemptsLeft));
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

    // Switch players (Lecture 3: Polymorphism, Substitution)
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

    // Reset the grid for the next player
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

    // Calculate score based on attempts left (Lecture 4: Abstract Methods, Interfaces)
    private int calculateScore(final int attemptsLeft)
    {
        return attemptsLeft * 10; // 10 points per remaining attempt
    }

    // Update the scoreboard
    private void updateScoreboard()
    {
        scoreLabel.setText("Scores: " + player1.getName() + ": " + player1.getScore() + " | " + player2.getName() + ": " + player2.getScore());
    }

    // Start the timer for the current player's turn
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

    // End the game and display the final scores
    private void endGame()
    {
        timer.stop();
        messageLabel.setText("Game over! Final scores: " + player1.getName() + ": " + player1.getScore() + " | " + player2.getName() + ": " + player2.getScore());
        inputField.setDisable(true);

        // Use Platform.runLater to show the popup after the animation/timer has completed
        Platform.runLater(this::showWinnerPopup);
    }

    // Show a popup box with the winner and a close button
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

    // Player class (Lecture 1: Classes, Objects, Methods)
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
