package ca.bcit.comp2522.termproject.twistedwordle;
//
//import javafx.application.Application;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.control.TextField;
//import javafx.scene.layout.GridPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import javafx.scene.text.Font;
//import javafx.stage.Stage;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//import java.util.Scanner;
//
//public class TwistedWordle extends Application {
//
//    private static final int MAX_ATTEMPTS = 6;
//    private static final int WORD_LENGTH = 5;
//
//    private String targetWord;
//    private int attemptsLeft;
//    private Label[][] gridLabels;
//    private TextField inputField;
//    private Label messageLabel;
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//
//    @Override
//    public void start(Stage primaryStage) {
//        primaryStage.setTitle("Wordle Game");
//
//        // Load words from file
//        List<String> words = loadWordsFromFile("src/resources/words.txt");
//        if (words.isEmpty()) {
//            System.out.println("No words found in the file. Exiting.");
//            return;
//        }
//
//        // Initialize game
//        targetWord = words.get(new Random().nextInt(words.size())).toUpperCase();
//        attemptsLeft = MAX_ATTEMPTS;
//
//        // Create the grid for displaying guesses
//        GridPane gridPane = new GridPane();
//        gridPane.setAlignment(Pos.CENTER);
//        gridPane.setHgap(10);
//        gridPane.setVgap(10);
//        gridPane.setPadding(new Insets(20));
//
//        gridLabels = new Label[MAX_ATTEMPTS][WORD_LENGTH];
//        for (int row = 0; row < MAX_ATTEMPTS; row++) {
//            for (int col = 0; col < WORD_LENGTH; col++) {
//                Label label = new Label("");
//                label.setFont(Font.font(20));
//                label.setMinSize(40, 40);
//                label.setAlignment(Pos.CENTER);
//                label.setStyle("-fx-border-color: black; -fx-border-width: 2;");
//                gridLabels[row][col] = label;
//                gridPane.add(label, col, row);
//            }
//        }
//
//        // Input field and submit button
//        inputField = new TextField();
//        inputField.setFont(Font.font(20));
//        inputField.setMaxWidth(200);
//
//        Button submitButton = new Button("Submit");
//        submitButton.setOnAction(e -> handleGuess());
//
//        HBox inputBox = new HBox(10, inputField, submitButton);
//        inputBox.setAlignment(Pos.CENTER);
//
//        // Message label
//        messageLabel = new Label("Guess the 5-letter word!");
//        messageLabel.setFont(Font.font(20));
//
//        // Main layout
//        VBox root = new VBox(20, gridPane, inputBox, messageLabel);
//        root.setAlignment(Pos.CENTER);
//        root.setPadding(new Insets(20));
//
//        Scene scene = new Scene(root, 400, 500);
//        primaryStage.setScene(scene);
//        primaryStage.show();
//    }
//
//    private List<String> loadWordsFromFile(String filename) {
//        List<String> words = new ArrayList<>();
//        try (Scanner scanner = new Scanner(new File(filename))) {
//            while (scanner.hasNextLine()) {
//                String word = scanner.nextLine().trim();
//                if (word.length() == WORD_LENGTH) { // Only add 5-letter words
//                    words.add(word.toUpperCase());
//                }
//            }
//        } catch (FileNotFoundException e) {
//            System.out.println("File not found: " + filename);
//        }
//        return words;
//    }
//
//    private void handleGuess() {
//        String guess = inputField.getText().toUpperCase();
//        inputField.clear();
//
//        if (guess.length() != WORD_LENGTH) {
//            messageLabel.setText("Please enter a 5-letter word.");
//            return;
//        }
//
//        int currentAttempt = MAX_ATTEMPTS - attemptsLeft;
//
//        // Track which letters in the target word have already been matched
//        boolean[] targetMatched = new boolean[WORD_LENGTH];
//        // Track which letters in the guess have already been marked as green or yellow
//        boolean[] guessMatched = new boolean[WORD_LENGTH];
//
//        // First pass: Mark green letters (correct letter in correct position)
//        for (int i = 0; i < WORD_LENGTH; i++) {
//            if (guess.charAt(i) == targetWord.charAt(i)) {
//                gridLabels[currentAttempt][i].setText(String.valueOf(guess.charAt(i)));
//                gridLabels[currentAttempt][i].setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-border-color: black; -fx-border-width: 2;");
//                targetMatched[i] = true; // Mark this position in the target word as matched
//                guessMatched[i] = true;  // Mark this position in the guess as matched
//            }
//        }
//
//        // Second pass: Mark yellow letters (correct letter in wrong position)
//        for (int i = 0; i < WORD_LENGTH; i++) {
//            if (!guessMatched[i]) { // Only process letters in the guess that haven't been marked yet
//                char guessedChar = guess.charAt(i);
//                for (int j = 0; j < WORD_LENGTH; j++) {
//                    if (!targetMatched[j] && guessedChar == targetWord.charAt(j)) {
//                        gridLabels[currentAttempt][i].setText(String.valueOf(guessedChar));
//                        gridLabels[currentAttempt][i].setStyle("-fx-background-color: yellow; -fx-text-fill: black; -fx-border-color: black; -fx-border-width: 2;");
//                        targetMatched[j] = true; // Mark this position in the target word as matched
//                        guessMatched[i] = true;  // Mark this position in the guess as matched
//                        break; // Move to the next letter in the guess
//                    }
//                }
//                // If the letter wasn't matched, mark it as gray
//                if (!guessMatched[i]) {
//                    gridLabels[currentAttempt][i].setText(String.valueOf(guessedChar));
//                    gridLabels[currentAttempt][i].setStyle("-fx-background-color: gray; -fx-text-fill: white; -fx-border-color: black; -fx-border-width: 2;");
//                }
//            }
//        }
//
//        attemptsLeft--;
//
//        if (guess.equals(targetWord)) {
//            messageLabel.setText("Congratulations! You've guessed the word!");
//            inputField.setDisable(true);
//        } else if (attemptsLeft == 0) {
//            messageLabel.setText("Game over! The word was: " + targetWord);
//            inputField.setDisable(true);
//        } else {
//            messageLabel.setText("Attempts left: " + attemptsLeft);
//        }
//    }
//}








//import javafx.application.Application;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.control.TextField;
//import javafx.scene.layout.GridPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import javafx.scene.text.Font;
//import javafx.stage.Stage;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//import java.util.stream.Collectors;
//
//public class TwistedWordle extends Application
//{
//
//    private static final int MAX_ATTEMPTS = 6;
//    private static final int WORD_LENGTH  = 5;
//
//    private String       targetWord;
//    private int          attemptsLeft;
//    private Label[][]    gridLabels;
//    private TextField    inputField;
//    private Label        messageLabel;
//    private Player       currentPlayer;
//    private Player       player1;
//    private Player       player2;
//    private List<String> wordBatch;
//
//    // TODO STILL NEED TO INCORPORATE LAST 3 LECTURES
//    public static void main(String[] args)
//    {
//        launch(args);
//    }
//
//    @Override
//    public void start(Stage primaryStage)
//    {
//        primaryStage.setTitle("Wordle Game");
//
//        // Load words from file (Lecture 7: File NIO.2)
//        wordBatch = loadWordsFromFile("src/resources/words.txt");
//        if (wordBatch.isEmpty())
//        {
//            System.out.println("No words found in the file. Exiting.");
//            return;
//        }
//
//        // Initialize players (Lecture 1: Classes, Objects, Constructors)
//        player1       = new Player("Player 1");
//        player2       = new Player("Player 2");
//        currentPlayer = player1;
//
//        // Initialize game
//        targetWord   = wordBatch.get(new Random().nextInt(wordBatch.size())).toUpperCase();
//        attemptsLeft = MAX_ATTEMPTS;
//
//        // Create the grid for displaying guesses (Lecture 10: Graphical User Interfaces)
//        GridPane gridPane = new GridPane();
//        gridPane.setAlignment(Pos.CENTER);
//        gridPane.setHgap(10);
//        gridPane.setVgap(10);
//        gridPane.setPadding(new Insets(20));
//
//        gridLabels = new Label[MAX_ATTEMPTS][WORD_LENGTH];
//        for (int row = 0; row < MAX_ATTEMPTS; row++)
//        {
//            for (int col = 0; col < WORD_LENGTH; col++)
//            {
//                Label label = new Label("");
//                label.setFont(Font.font(20));
//                label.setMinSize(40, 40);
//                label.setAlignment(Pos.CENTER);
//                label.setStyle("-fx-border-color: black; -fx-border-width: 2;");
//                gridLabels[row][col] = label;
//                gridPane.add(label, col, row);
//            }
//        }
//
//        // Input field and submit button (Lecture 10: Graphical User Interfaces)
//        inputField = new TextField();
//        inputField.setFont(Font.font(20));
//        inputField.setMaxWidth(200);
//
//        Button submitButton = new Button("Submit");
//        submitButton.setOnAction(e -> handleGuess());
//
//        HBox inputBox = new HBox(10, inputField, submitButton);
//        inputBox.setAlignment(Pos.CENTER);
//
//        // Message label
//        messageLabel = new Label(currentPlayer.getName() + ", guess the 5-letter word!");
//        messageLabel.setFont(Font.font(20));
//
//        // Main layout
//        VBox root = new VBox(20, gridPane, inputBox, messageLabel);
//        root.setAlignment(Pos.CENTER);
//        root.setPadding(new Insets(20));
//
//        Scene scene = new Scene(root, 400, 500);
//        primaryStage.setScene(scene);
//        primaryStage.show();
//    }
//
//    // Load words from file (Lecture 7: File NIO.2, Try-With-Resources)
//    // (Lecture 5: Collections)
//    private List<String> loadWordsFromFile(String filename)
//    {
//        try
//        {
//            // (Lecture 6: Lambda expressions)
//            // (Lecture 8: Streams and filters)
//            return Files.readAllLines(Paths.get(filename))
//                        .stream()
//                        .filter(word -> word.length() == WORD_LENGTH)
//                        .collect(Collectors.toList());
//        } catch (IOException e)
//        {
//            System.out.println("Error reading file: " + filename);
//            return new ArrayList<>();
//        }
//    }
//
//    // Handle player's guess (Lecture 2: Exception Handling)
//    private void handleGuess()
//    {
//        String guess = inputField.getText().toUpperCase();
//        inputField.clear();
//
//        if (guess.length() != WORD_LENGTH)
//        {
//            messageLabel.setText("Please enter a 5-letter word.");
//            return;
//        }
//
//        int currentAttempt = MAX_ATTEMPTS - attemptsLeft;
//
//        // Track which letters in the target word have already been matched
//        boolean[] targetMatched = new boolean[WORD_LENGTH];
//        // Track which letters in the guess have already been marked as green or yellow
//        boolean[] guessMatched = new boolean[WORD_LENGTH];
//
//        // First pass: Mark green letters (correct letter in correct position)
//        for (int i = 0; i < WORD_LENGTH; i++)
//        {
//            if (guess.charAt(i) == targetWord.charAt(i))
//            {
//                gridLabels[currentAttempt][i].setText(String.valueOf(guess.charAt(i)));
//                gridLabels[currentAttempt][i].setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-border-color: black; -fx-border-width: 2;");
//                targetMatched[i] = true;
//                guessMatched[i]  = true;
//            }
//        }
//
//        // Second pass: Mark yellow letters (correct letter in wrong position)
//        for (int i = 0; i < WORD_LENGTH; i++)
//        {
//            if (!guessMatched[i])
//            {
//                char guessedChar = guess.charAt(i);
//                for (int j = 0; j < WORD_LENGTH; j++)
//                {
//                    if (!targetMatched[j] && guessedChar == targetWord.charAt(j))
//                    {
//                        gridLabels[currentAttempt][i].setText(String.valueOf(guessedChar));
//                        gridLabels[currentAttempt][i].setStyle("-fx-background-color: yellow; -fx-text-fill: black; -fx-border-color: black; -fx-border-width: 2;");
//                        targetMatched[j] = true;
//                        guessMatched[i]  = true;
//                        break;
//                    }
//                }
//                // If the letter wasn't matched, mark it as gray
//                if (!guessMatched[i])
//                {
//                    gridLabels[currentAttempt][i].setText(String.valueOf(guessedChar));
//                    gridLabels[currentAttempt][i].setStyle("-fx-background-color: gray; -fx-text-fill: white; -fx-border-color: black; -fx-border-width: 2;");
//                }
//            }
//        }
//
//        attemptsLeft--;
//
//        if (guess.equals(targetWord))
//        {
//            currentPlayer.addScore(calculateScore(attemptsLeft));
//            messageLabel.setText("Congratulations, " + currentPlayer.getName() + "! You've guessed the word!");
//            inputField.setDisable(true);
//            switchPlayer();
//        }
//        else if (attemptsLeft == 0)
//        {
//            messageLabel.setText("Game over! The word was: " + targetWord);
//            inputField.setDisable(true);
//            switchPlayer();
//        }
//        else
//        {
//            messageLabel.setText(currentPlayer.getName() + ", attempts left: " + attemptsLeft);
//        }
//    }
//
//    // Switch players (Lecture 3: Polymorphism, Substitution)
//    private void switchPlayer()
//    {
//        if (currentPlayer == player1)
//        {
//            currentPlayer = player2;
//        }
//        else
//        {
//            currentPlayer = player1;
//        }
//        targetWord   = wordBatch.get(new Random().nextInt(wordBatch.size())).toUpperCase();
//        attemptsLeft = MAX_ATTEMPTS;
//        resetGrid();
//        messageLabel.setText(currentPlayer.getName() + ", guess the 5-letter word!");
//        inputField.setDisable(false);
//    }
//
//    // Reset the grid for the next player
//    private void resetGrid()
//    {
//        for (int row = 0; row < MAX_ATTEMPTS; row++)
//        {
//            for (int col = 0; col < WORD_LENGTH; col++)
//            {
//                gridLabels[row][col].setText("");
//                gridLabels[row][col].setStyle("-fx-border-color: black; -fx-border-width: 2;");
//            }
//        }
//    }
//
//    // Calculate score based on attempts left (Lecture 4: Abstract Methods, Interfaces)
//    private int calculateScore(int attemptsLeft)
//    {
//        return attemptsLeft * 10; // 10 points per remaining attempt
//    }
//
//    // Player class (Lecture 1: Classes, Objects, Methods)
//    private static class Player
//    {
//        private final String name;
//        private       int    score;
//
//        public Player(String name)
//        {
//            this.name  = name;
//            this.score = 0;
//        }
//
//        public String getName()
//        {
//            return name;
//        }
//
//        public int getScore()
//        {
//            return score;
//        }
//
//        public void addScore(int points)
//        {
//            score += points;
//        }
//    }
//}









//import javafx.animation.AnimationTimer;
//import javafx.application.Application;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.control.TextField;
//import javafx.scene.layout.GridPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import javafx.scene.text.Font;
//import javafx.stage.Stage;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//import java.util.stream.Collectors;
//
//public class TwistedWordle extends Application {
//
//    private static final int MAX_ATTEMPTS = 6;
//    private static final int WORD_LENGTH = 5;
//    private static final int TURN_TIME = 90; // 60 seconds per turn
//
//    private String targetWord;
//    private int attemptsLeft;
//    private Label[][] gridLabels;
//    private TextField inputField;
//    private Label messageLabel;
//    private Label timerLabel;
//    private Label scoreLabel;
//    private Player currentPlayer;
//    private Player player1;
//    private Player player2;
//    private List<String> wordBatch;
//    private long startTime;
//    private AnimationTimer timer;
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//
//    @Override
//    public void start(Stage primaryStage) {
//        primaryStage.setTitle("Wordle Game");
//
//        // Load words from file (Lecture 7: File NIO.2)
//        wordBatch = loadWordsFromFile("src/resources/words.txt");
//        if (wordBatch.isEmpty()) {
//            System.out.println("No words found in the file. Exiting.");
//            return;
//        }
//
//        // Initialize players (Lecture 1: Classes, Objects, Constructors)
//        player1 = new Player("Player 1");
//        player2 = new Player("Player 2");
//        currentPlayer = player1;
//
//        // Initialize game
//        targetWord = wordBatch.get(new Random().nextInt(wordBatch.size())).toUpperCase();
//        attemptsLeft = MAX_ATTEMPTS;
//
//        // Create the grid for displaying guesses (Lecture 10: Graphical User Interfaces)
//        GridPane gridPane = new GridPane();
//        gridPane.setAlignment(Pos.CENTER);
//        gridPane.setHgap(10);
//        gridPane.setVgap(10);
//        gridPane.setPadding(new Insets(20));
//
//        gridLabels = new Label[MAX_ATTEMPTS][WORD_LENGTH];
//        for (int row = 0; row < MAX_ATTEMPTS; row++) {
//            for (int col = 0; col < WORD_LENGTH; col++) {
//                Label label = new Label("");
//                label.setFont(Font.font(20));
//                label.setMinSize(40, 40);
//                label.setAlignment(Pos.CENTER);
//                label.setStyle("-fx-border-color: black; -fx-border-width: 2;");
//                gridLabels[row][col] = label;
//                gridPane.add(label, col, row);
//            }
//        }
//
//        // Input field and submit button (Lecture 10: Graphical User Interfaces)
//        inputField = new TextField();
//        inputField.setFont(Font.font(20));
//        inputField.setMaxWidth(200);
//
//        Button submitButton = new Button("Submit");
//        submitButton.setOnAction(e -> handleGuess());
//
//        HBox inputBox = new HBox(10, inputField, submitButton);
//        inputBox.setAlignment(Pos.CENTER);
//
//        // Message label
//        messageLabel = new Label(currentPlayer.getName() + ", guess the 5-letter word!");
//        messageLabel.setFont(Font.font(20));
//
//        // Timer label
//        timerLabel = new Label("Time left: " + TURN_TIME);
//        timerLabel.setFont(Font.font(20));
//
//        // Scoreboard
//        scoreLabel = new Label("Scores: " + player1.getName() + " - " + player1.getScore() + " | " + player2.getName() + " - " + player2.getScore());
//        scoreLabel.setFont(Font.font(20));
//
//        // Main layout
//        VBox root = new VBox(20, gridPane, inputBox, messageLabel, timerLabel, scoreLabel);
//        root.setAlignment(Pos.CENTER);
//        root.setPadding(new Insets(20));
//
//        Scene scene = new Scene(root, 400, 600);
//        primaryStage.setScene(scene);
//        primaryStage.show();
//
//        // Start the timer
//        startTimer();
//    }
//
//    // Load words from file (Lecture 7: File NIO.2, Try-With-Resources)
//    private List<String> loadWordsFromFile(String filename) {
//        try {
//            return Files.readAllLines(Paths.get(filename))
//                        .stream()
//                        .filter(word -> word.length() == WORD_LENGTH)
//                        .collect(Collectors.toList());
//        } catch (IOException e) {
//            System.out.println("Error reading file: " + filename);
//            return new ArrayList<>();
//        }
//    }
//
//    // Handle player's guess (Lecture 2: Exception Handling)
//    private void handleGuess() {
//        String guess = inputField.getText().toUpperCase();
//        inputField.clear();
//
//        if (guess.length() != WORD_LENGTH) {
//            messageLabel.setText("Please enter a 5-letter word.");
//            return;
//        }
//
//        int currentAttempt = MAX_ATTEMPTS - attemptsLeft;
//
//        // Track which letters in the target word have already been matched
//        boolean[] targetMatched = new boolean[WORD_LENGTH];
//        // Track which letters in the guess have already been marked as green or yellow
//        boolean[] guessMatched = new boolean[WORD_LENGTH];
//
//        // First pass: Mark green letters (correct letter in correct position)
//        for (int i = 0; i < WORD_LENGTH; i++) {
//            if (guess.charAt(i) == targetWord.charAt(i)) {
//                gridLabels[currentAttempt][i].setText(String.valueOf(guess.charAt(i)));
//                gridLabels[currentAttempt][i].setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-border-color: black; -fx-border-width: 2;");
//                targetMatched[i] = true;
//                guessMatched[i] = true;
//            }
//        }
//
//        // Second pass: Mark yellow letters (correct letter in wrong position)
//        for (int i = 0; i < WORD_LENGTH; i++) {
//            if (!guessMatched[i]) {
//                char guessedChar = guess.charAt(i);
//                for (int j = 0; j < WORD_LENGTH; j++) {
//                    if (!targetMatched[j] && guessedChar == targetWord.charAt(j)) {
//                        gridLabels[currentAttempt][i].setText(String.valueOf(guessedChar));
//                        gridLabels[currentAttempt][i].setStyle("-fx-background-color: yellow; -fx-text-fill: black; -fx-border-color: black; -fx-border-width: 2;");
//                        targetMatched[j] = true;
//                        guessMatched[i] = true;
//                        break;
//                    }
//                }
//                // If the letter wasn't matched, mark it as gray
//                if (!guessMatched[i]) {
//                    gridLabels[currentAttempt][i].setText(String.valueOf(guessedChar));
//                    gridLabels[currentAttempt][i].setStyle("-fx-background-color: gray; -fx-text-fill: white; -fx-border-color: black; -fx-border-width: 2;");
//                }
//            }
//        }
//
//        attemptsLeft--;
//
//        if (guess.equals(targetWord)) {
//            currentPlayer.addScore(calculateScore(attemptsLeft));
//            messageLabel.setText("Congratulations, " + currentPlayer.getName() + "! You've guessed the word!");
//            inputField.setDisable(true);
//            updateScoreboard();
//            switchPlayer();
//        } else if (attemptsLeft == 0) {
//            messageLabel.setText("Game over! The word was: " + targetWord);
//            inputField.setDisable(true);
//            switchPlayer();
//        } else {
//            messageLabel.setText(currentPlayer.getName() + ", attempts left: " + attemptsLeft);
//        }
//    }
//
//    // Switch players (Lecture 3: Polymorphism, Substitution)
//    private void switchPlayer() {
//        if (currentPlayer == player1) {
//            currentPlayer = player2;
//        } else {
//            currentPlayer = player1;
//        }
//        targetWord = wordBatch.get(new Random().nextInt(wordBatch.size())).toUpperCase();
//        attemptsLeft = MAX_ATTEMPTS;
//        resetGrid();
//        messageLabel.setText(currentPlayer.getName() + ", guess the 5-letter word!");
//        inputField.setDisable(false);
//        startTimer();
//    }
//
//    // Reset the grid for the next player
//    private void resetGrid() {
//        for (int row = 0; row < MAX_ATTEMPTS; row++) {
//            for (int col = 0; col < WORD_LENGTH; col++) {
//                gridLabels[row][col].setText("");
//                gridLabels[row][col].setStyle("-fx-border-color: black; -fx-border-width: 2;");
//            }
//        }
//    }
//
//    // Calculate score based on attempts left (Lecture 4: Abstract Methods, Interfaces)
//    private int calculateScore(int attemptsLeft) {
//        return attemptsLeft * 10; // 10 points per remaining attempt
//    }
//
//    // Update the scoreboard
//    private void updateScoreboard() {
//        scoreLabel.setText("Scores: " + player1.getName() + ": " + player1.getScore() + " | " + player2.getName() + ": " + player2.getScore());
//    }
//
//    // Start the timer for the current player's turn
//    private void startTimer() {
//        startTime = System.currentTimeMillis();
//        timer = new AnimationTimer() {
//            @Override
//            public void handle(long now) {
//                long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
//                int timeLeft = TURN_TIME - (int) elapsedTime;
//                timerLabel.setText("Time left: " + timeLeft);
//
//                if (timeLeft <= 0) {
//                    timer.stop();
//                    messageLabel.setText("Time's up! The word was: " + targetWord);
//                    inputField.setDisable(true);
//                    switchPlayer();
//                }
//            }
//        };
//        timer.start();
//    }
//
//    // Player class (Lecture 1: Classes, Objects, Methods)
//    private static class Player {
//        private final String name;
//        private       int    score;
//
//        public Player(String name) {
//            this.name = name;
//            this.score = 0;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public int getScore() {
//            return score;
//        }
//
//        public void addScore(int points) {
//            score += points;
//        }
//    }
//}












import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
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
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TwistedWordle extends Application {

    private static final int MAX_ATTEMPTS = 6;
    private static final int WORD_LENGTH = 5;
    private static final int TURN_TIME = 90; // 60 seconds per turn
    private static final int TOTAL_ROUNDS = 3; // Total rounds to play

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

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Wordle Game");

        // Load words from file (Lecture 7: File NIO.2)
        wordBatch = loadWordsFromFile("src/resources/words.txt");
        if (wordBatch.isEmpty()) {
            System.out.println("No words found in the file. Exiting.");
            return;
        }

        // Initialize players (Lecture 1: Classes, Objects, Constructors)
        player1 = new Player("Player 1");
        player2 = new Player("Player 2");
        currentPlayer = player1;

        // Initialize game
        targetWord = wordBatch.get(new Random().nextInt(wordBatch.size())).toUpperCase();
        attemptsLeft = MAX_ATTEMPTS;
        currentRound = 1;

        // Create the grid for displaying guesses (Lecture 10: Graphical User Interfaces)
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

        Scene scene = new Scene(root, 400, 650);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start the timer
        startTimer();
    }

    // Load words from file (Lecture 7: File NIO.2, Try-With-Resources)
    private List<String> loadWordsFromFile(String filename) {
        try {
            return Files.readAllLines(Paths.get(filename))
                        .stream()
                        .filter(word -> word.length() == WORD_LENGTH)
                        .collect(Collectors.toList());
        } catch (IOException e) {
            System.out.println("Error reading file: " + filename);
            return new ArrayList<>();
        }
    }

    // Handle player's guess (Lecture 2: Exception Handling)
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
                targetMatched[i] = true;
                guessMatched[i] = true;
            }
        }

        // Second pass: Mark yellow letters (correct letter in wrong position)
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (!guessMatched[i]) {
                char guessedChar = guess.charAt(i);
                for (int j = 0; j < WORD_LENGTH; j++) {
                    if (!targetMatched[j] && guessedChar == targetWord.charAt(j)) {
                        gridLabels[currentAttempt][i].setText(String.valueOf(guessedChar));
                        gridLabels[currentAttempt][i].setStyle("-fx-background-color: yellow; -fx-text-fill: black; -fx-border-color: black; -fx-border-width: 2;");
                        targetMatched[j] = true;
                        guessMatched[i] = true;
                        break;
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
            currentPlayer.addScore(calculateScore(attemptsLeft));
            messageLabel.setText("Congratulations, " + currentPlayer.getName() + "! You've guessed the word!");
            inputField.setDisable(true);
            updateScoreboard();
            // Add a delay before switching players
            PauseTransition delay = new PauseTransition(Duration.seconds(5)); // 3-second delay
            delay.setOnFinished(e -> switchPlayer());
            delay.play();
        } else if (attemptsLeft == 0) {
            messageLabel.setText("Game over! The word was: " + targetWord);
            inputField.setDisable(true);
            // Add a delay before switching players
            PauseTransition delay = new PauseTransition(Duration.seconds(5)); // 3-second delay
            delay.setOnFinished(e -> switchPlayer());
            delay.play();
        } else {
            messageLabel.setText(currentPlayer.getName() + ", attempts left: " + attemptsLeft);
        }
    }

    // Switch players (Lecture 3: Polymorphism, Substitution)
    private void switchPlayer() {
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

    // Reset the grid for the next player
    private void resetGrid() {
        for (int row = 0; row < MAX_ATTEMPTS; row++) {
            for (int col = 0; col < WORD_LENGTH; col++) {
                gridLabels[row][col].setText("");
                gridLabels[row][col].setStyle("-fx-border-color: black; -fx-border-width: 2;");
            }
        }
    }

    // Calculate score based on attempts left (Lecture 4: Abstract Methods, Interfaces)
    private int calculateScore(int attemptsLeft) {
        return attemptsLeft * 10; // 10 points per remaining attempt
    }

    // Update the scoreboard
    private void updateScoreboard() {
        scoreLabel.setText("Scores: " + player1.getName() + ": " + player1.getScore() + " | " + player2.getName() + ": " + player2.getScore());
    }

    // Start the timer for the current player's turn
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
                    // Add a delay before switching players
                    PauseTransition delay = new PauseTransition(Duration.seconds(3)); // 3-second delay
                    delay.setOnFinished(e -> switchPlayer());
                    delay.play();
                }
            }
        };
        timer.start();
    }

    // End the game and display the final scores
    private void endGame() {
        timer.stop();
        messageLabel.setText("Game over! Final scores: " + player1.getName() + ": " + player1.getScore() + " | " + player2.getName() + ": " + player2.getScore());
        inputField.setDisable(true);
    }

    // Player class (Lecture 1: Classes, Objects, Methods)
    private static class Player {
        private final String name;
        private       int    score;

        public Player(String name) {
            this.name = name;
            this.score = 0;
        }

        public String getName() {
            return name;
        }

        public int getScore() {
            return score;
        }

        public void addScore(int points) {
            score += points;
        }
    }
}
