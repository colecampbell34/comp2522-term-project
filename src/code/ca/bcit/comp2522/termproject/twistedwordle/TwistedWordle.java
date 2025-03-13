package ca.bcit.comp2522.termproject.twistedwordle;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Random;

public class TwistedWordle extends Application {

    private static final String[] WORDS = {"APPLE", "BEACH", "CRANE", "DANCE", "EAGLE"};
    private static final int MAX_ATTEMPTS = 6;
    private static final int WORD_LENGTH = 5;

    private String targetWord;
    private int attemptsLeft;
    private Label[][] gridLabels;
    private TextField inputField;
    private Label messageLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Wordle Game");

        // Initialize game
        targetWord = WORDS[new Random().nextInt(WORDS.length)];
        attemptsLeft = MAX_ATTEMPTS;

        // Create the grid for displaying guesses
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));

        gridLabels = new Label[MAX_ATTEMPTS][WORD_LENGTH];
        for (int row = 0; row < MAX_ATTEMPTS; row++) {
            for (int col = 0; col < WORD_LENGTH; col++) {
                Label label = new Label("");
                label.setFont(Font.font(20));
                label.setMinSize(40, 40);
                label.setAlignment(Pos.CENTER);
                label.setStyle("-fx-border-color: black; -fx-border-width: 2;");
                gridLabels[row][col] = label;
                gridPane.add(label, col, row);
            }
        }

        // Input field and submit button
        inputField = new TextField();
        inputField.setFont(Font.font(20));
        inputField.setMaxWidth(200);

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> handleGuess());

        HBox inputBox = new HBox(10, inputField, submitButton);
        inputBox.setAlignment(Pos.CENTER);

        // Message label
        messageLabel = new Label("Guess the 5-letter word!");
        messageLabel.setFont(Font.font(20));

        // Main layout
        VBox root = new VBox(20, gridPane, inputBox, messageLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleGuess() {
        String guess = inputField.getText().toUpperCase();
        inputField.clear();

        if (guess.length() != WORD_LENGTH) {
            messageLabel.setText("Please enter a 5-letter word.");
            return;
        }

        int currentAttempt = MAX_ATTEMPTS - attemptsLeft;

        // Track which letters in the target word have already been matched
        boolean[] targetMatched = new boolean[WORD_LENGTH];
        // Track which letters in the guess have already been marked as green or yellow
        boolean[] guessMatched = new boolean[WORD_LENGTH];

        // First pass: Mark green letters (correct letter in correct position)
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (guess.charAt(i) == targetWord.charAt(i)) {
                gridLabels[currentAttempt][i].setText(String.valueOf(guess.charAt(i)));
                gridLabels[currentAttempt][i].setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-border-color: black; -fx-border-width: 2;");
                targetMatched[i] = true; // Mark this position in the target word as matched
                guessMatched[i] = true;  // Mark this position in the guess as matched
            }
        }

        // Second pass: Mark yellow letters (correct letter in wrong position)
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (!guessMatched[i]) { // Only process letters in the guess that haven't been marked yet
                char guessedChar = guess.charAt(i);
                for (int j = 0; j < WORD_LENGTH; j++) {
                    if (!targetMatched[j] && guessedChar == targetWord.charAt(j)) {
                        gridLabels[currentAttempt][i].setText(String.valueOf(guessedChar));
                        gridLabels[currentAttempt][i].setStyle("-fx-background-color: yellow; -fx-text-fill: black; -fx-border-color: black; -fx-border-width: 2;");
                        targetMatched[j] = true; // Mark this position in the target word as matched
                        guessMatched[i] = true;  // Mark this position in the guess as matched
                        break; // Move to the next letter in the guess
                    }
                }
                // If the letter wasn't matched, mark it as gray
                if (!guessMatched[i]) {
                    gridLabels[currentAttempt][i].setText(String.valueOf(guessedChar));
                    gridLabels[currentAttempt][i].setStyle("-fx-background-color: gray; -fx-text-fill: white; -fx-border-color: black; -fx-border-width: 2;");
                }
            }
        }

        attemptsLeft--;

        if (guess.equals(targetWord)) {
            messageLabel.setText("Congratulations! You've guessed the word!");
            inputField.setDisable(true);
        } else if (attemptsLeft == 0) {
            messageLabel.setText("Game over! The word was: " + targetWord);
            inputField.setDisable(true);
        } else {
            messageLabel.setText("Attempts left: " + attemptsLeft);
        }
    }
}
