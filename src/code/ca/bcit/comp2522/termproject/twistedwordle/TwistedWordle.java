package ca.bcit.comp2522.termproject.twistedwordle;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TwistedWordle extends Application implements Scorable {
    private static final int MAX_ATTEMPTS = 6;
    private static final int WORD_LENGTH = 5;
    private static final int TURN_TIME = 90;
    private static final int TOTAL_ROUNDS = 1;
    private static final int DELAY = 5;
    private static final int NOTHING = 0;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static Stage currentStage;
    private static Runnable onCloseCallback;

    private String targetWord;
    private int attemptsLeft;
    private Label[][] gridLabels;
    private TextField inputField;
    private Label messageLabel;
    private Label timerLabel;
    private Label scoreLabel;
    private Label roundLabel;
    private Player currentPlayer;
    private Player player1;
    private Player player2;
    private List<String> wordBatch;
    private long startTime;
    private AnimationTimer timer;
    private int currentRound;

    public static void launchGame(Runnable callback) {
        onCloseCallback = callback;
        Platform.runLater(() -> {
            try {
                if (currentStage != null) {
                    currentStage.close();
                }
                currentStage = new Stage();
                new TwistedWordle().start(currentStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) {
        primaryStage.setOnHidden(e -> {
            if (onCloseCallback != null) {
                Platform.runLater(onCloseCallback);
            }
        });

        primaryStage.setTitle("Wordle Game");

        // Get player names first
        final Scanner input = new Scanner(System.in);
        System.out.print("Enter Player 1 Name: ");
        player1 = new Player(input.nextLine());
        System.out.print("\nEnter Player 2 Name: ");
        player2 = new Player(input.nextLine());
        currentPlayer = player1;

        // Load words and initialize game after loading completes
        loadWordsAsync("src/resources/words.txt", () -> {
            Platform.runLater(() -> {
                if (wordBatch == null || wordBatch.isEmpty()) {
                    System.err.println("Failed to load words!");
                    primaryStage.close();
                    return;
                }
                initializeGame(primaryStage);
            });
        });
    }

    private void initializeGame(Stage primaryStage) {
        targetWord = wordBatch.get(new Random().nextInt(wordBatch.size())).toUpperCase();
        attemptsLeft = MAX_ATTEMPTS;
        currentRound = 1;

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

        inputField = new TextField();
        inputField.setFont(Font.font(20));
        inputField.setMaxWidth(200);
        inputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleGuess();
            }
        });

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> handleGuess());

        HBox inputBox = new HBox(10, inputField, submitButton);
        inputBox.setAlignment(Pos.CENTER);

        messageLabel = new Label(currentPlayer.getName() + ", guess the 5-letter word!");
        messageLabel.setFont(Font.font(20));

        timerLabel = new Label("Time left: " + TURN_TIME);
        timerLabel.setFont(Font.font(20));

        scoreLabel = new Label("Scores: " + player1.getName() + ": " + player1.getScore() + " | " + player2.getName() + ": " + player2.getScore());
        scoreLabel.setFont(Font.font(20));

        roundLabel = new Label("Round: " + currentRound + " of " + TOTAL_ROUNDS);
        roundLabel.setFont(Font.font(20));

        VBox root = new VBox(20, gridPane, inputBox, messageLabel, timerLabel, scoreLabel, roundLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 600, 700);
        primaryStage.setScene(scene);
        primaryStage.show();

        showInstructionsPopup(primaryStage);
    }

    private void showInstructionsPopup(Stage primaryStage) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.initOwner(primaryStage);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.setTitle("How to Play");

        Label rulesLabel = new Label(
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

        Button startButton = new Button("Start");
        startButton.setOnAction(e -> {
            popupStage.close();
            startTimer();
        });

        VBox popupLayout = new VBox(20, rulesLabel, startButton);
        popupLayout.setAlignment(Pos.CENTER);
        popupLayout.setPadding(new Insets(20));

        Scene popupScene = new Scene(popupLayout, 400, 300);
        popupStage.setScene(popupScene);
        popupStage.showAndWait();
    }

    public void loadWordsAsync(String filename, Runnable onComplete) {
        Task<List<String>> wordLoadTask = new Task<>() {
            @Override
            protected List<String> call() throws IOException {
                return Files.readAllLines(Paths.get(filename))
                            .stream()
                            .filter(word -> word.length() == WORD_LENGTH)
                            .collect(Collectors.toList());
            }
        };

        wordLoadTask.setOnSucceeded(event -> {
            wordBatch = wordLoadTask.getValue();
            onComplete.run();
        });

        wordLoadTask.setOnFailed(event -> {
            System.err.println("Error loading words!");
            onComplete.run();
        });

        executorService.execute(wordLoadTask);
    }

    private void handleGuess() {
        String guess = inputField.getText().toUpperCase();
        inputField.clear();

        if (guess.length() != WORD_LENGTH) {
            messageLabel.setText("Please enter a 5-letter word.");
            return;
        }

        int currentAttempt = MAX_ATTEMPTS - attemptsLeft;
        boolean[] targetMatched = new boolean[WORD_LENGTH];
        boolean[] guessMatched = new boolean[WORD_LENGTH];

        // First pass: Mark green letters
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (guess.charAt(i) == targetWord.charAt(i)) {
                gridLabels[currentAttempt][i].setText(String.valueOf(guess.charAt(i)));
                gridLabels[currentAttempt][i].setStyle("-fx-background-color: green; -fx-text-fill: white;");
                targetMatched[i] = true;
                guessMatched[i] = true;
            }
        }

        // Second pass: Mark yellow letters
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (!guessMatched[i]) {
                char guessedChar = guess.charAt(i);
                for (int j = 0; j < WORD_LENGTH; j++) {
                    if (!targetMatched[j] && guessedChar == targetWord.charAt(j)) {
                        gridLabels[currentAttempt][i].setText(String.valueOf(guessedChar));
                        gridLabels[currentAttempt][i].setStyle("-fx-background-color: yellow; -fx-text-fill: black;");
                        targetMatched[j] = true;
                        guessMatched[i] = true;
                        break;
                    }
                }
                // Mark gray for unmatched letters
                if (!guessMatched[i]) {
                    gridLabels[currentAttempt][i].setText(String.valueOf(guessedChar));
                    gridLabels[currentAttempt][i].setStyle("-fx-background-color: gray; -fx-text-fill: white;");
                }
            }
        }

        attemptsLeft--;

        if (guess.equals(targetWord)) {
            long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
            int timeLeft = TURN_TIME - (int) elapsedTime;
            int score = calculateScore(attemptsLeft, timeLeft);
            currentPlayer.addScore(score);

            messageLabel.setText("Congratulations, " + currentPlayer.getName() + "! You've guessed the word!");
            inputField.setDisable(true);
            updateScoreboard();
            timer.stop();

            PauseTransition delay = new PauseTransition(Duration.seconds(DELAY));
            delay.setOnFinished(e -> switchPlayer());
            delay.play();
        } else if (attemptsLeft == NOTHING) {
            messageLabel.setText("Game over! The word was: " + targetWord);
            inputField.setDisable(true);
            timer.stop();

            PauseTransition delay = new PauseTransition(Duration.seconds(DELAY));
            delay.setOnFinished(e -> switchPlayer());
            delay.play();
        } else {
            messageLabel.setText(currentPlayer.getName() + ", attempts left: " + attemptsLeft);
        }
    }

    public void switchPlayer() {
        if (currentPlayer == player1) {
            currentPlayer = player2;
        } else {
            currentPlayer = player1;
            currentRound++;
            if (currentRound > TOTAL_ROUNDS) {
                endGame();
                return;
            }
            roundLabel.setText("Round: " + currentRound + " of " + TOTAL_ROUNDS);
        }
        targetWord = wordBatch.get(new Random().nextInt(wordBatch.size())).toUpperCase();
        attemptsLeft = MAX_ATTEMPTS;
        resetGrid();
        messageLabel.setText(currentPlayer.getName() + ", guess the 5-letter word!");
        inputField.setDisable(false);
        startTimer();
    }

    private void resetGrid() {
        for (int row = 0; row < MAX_ATTEMPTS; row++) {
            for (int col = 0; col < WORD_LENGTH; col++) {
                gridLabels[row][col].setText("");
                gridLabels[row][col].setStyle("-fx-border-color: black; -fx-border-width: 2;");
            }
        }
    }

    @Override
    public int calculateScore(int attemptsLeft, int timeLeft) {
        return 50 + (attemptsLeft * 10) + timeLeft;
    }

    private void updateScoreboard() {
        scoreLabel.setText("Scores: " + player1.getName() + ": " + player1.getScore() + " | " + player2.getName() + ": " + player2.getScore());
    }

    private void startTimer() {
        startTime = System.currentTimeMillis();
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
                int timeLeft = TURN_TIME - (int) elapsedTime;
                timerLabel.setText("Time left: " + timeLeft);

                if (timeLeft <= 0) {
                    timer.stop();
                    messageLabel.setText("Time's up! The word was: " + targetWord);
                    inputField.setDisable(true);
                    PauseTransition delay = new PauseTransition(Duration.seconds(5));
                    delay.setOnFinished(e -> switchPlayer());
                    delay.play();
                }
            }
        };
        timer.start();
    }

    private void endGame() {
        timer.stop();
        messageLabel.setText("Game over! Final scores: " + player1.getName() + ": " + player1.getScore() + " | " + player2.getName() + ": " + player2.getScore());
        inputField.setDisable(true);
        Platform.runLater(this::showWinnerPopup);
    }

    private void showWinnerPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.setTitle("Game Over");

        String winnerMessage;
        if (player1.getScore() > player2.getScore()) {
            winnerMessage = player1.getName() + " wins!";
        } else if (player2.getScore() > player1.getScore()) {
            winnerMessage = player2.getName() + " wins!";
        } else {
            winnerMessage = "It's a tie!";
        }

        Label winnerLabel = new Label(winnerMessage);
        winnerLabel.setFont(Font.font(20));

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> {
            popupStage.close();
            ((Stage) messageLabel.getScene().getWindow()).close();
        });

        VBox popupLayout = new VBox(20, winnerLabel, closeButton);
        popupLayout.setAlignment(Pos.CENTER);
        popupLayout.setPadding(new Insets(20));

        Scene popupScene = new Scene(popupLayout, 300, 150);
        popupStage.setScene(popupScene);
        popupStage.showAndWait();
    }

    // Getters and setters
    public Player getCurrentPlayer() { return currentPlayer; }
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public void setCurrentPlayer(Player player) { this.currentPlayer = player; }
    public void setPlayers(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }
    public List<String> getWordBatch() { return wordBatch; }
    public String getTargetWord() { return targetWord; }
}