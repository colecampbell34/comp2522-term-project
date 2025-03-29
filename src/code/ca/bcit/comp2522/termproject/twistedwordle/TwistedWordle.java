package ca.bcit.comp2522.termproject.twistedwordle;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Custom multiplayer wordle game.
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
    private static final int DELAY                    = 3; // Seconds for transition pauses
    private static final int NOTHING                  = 0;
    private static final int FIRST_ROUND              = 1;
    private static final int TIME_FORMAT              = 1000; // Milliseconds per second
    private static final int BASE_CORRECT_SCORE       = 50;
    private static final int ATTEMPTS_LEFT_MULTIPLIER = 10;

    private static Stage    currentStage;
    private static Runnable onCloseCallback;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private       String          targetWord;
    private       int             attemptsLeft;
    private       Label[][]       gridLabels;
    private       TextField       inputField; // Main game guess input
    private       Label           messageLabel;
    private       Label           timerLabel;
    private       Label           scoreLabel;
    private       Label           roundLabel;
    private       Player          currentPlayer;
    private       Player          player1;
    private       Player          player2;
    private       String          wordForPlayer1; // Word chosen for P1 to guess
    private       String          wordForPlayer2; // Word chosen for P2 to guess
    private       List<String>    wordList;     // If needed for reference
    private       Set<String>     wordSet;      // For fast validation
    private       long            startTime;
    private       AnimationTimer  timer;
    private       int             currentRound;
    private       Stage           primaryStage; // Reference to the main window
    private       Alert           wordSelectionDialog;
    private       TextField       wordSelectionTextField;
    private       Label           wordSelectionErrorLabel;

    /**
     * Launches the UI for the game.
     */
    public static void launchGame(final Runnable callback)
    {
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
                              }
                          });
    }

    /**
     * The entry point for the JVM.
     */
    public static void main(final String[] args)
    {
        launch(args);
    }

    /**
     * Overrides the javaFX start method.
     */
    @Override
    public void start(final Stage stage)
    {
        this.primaryStage = stage;
        primaryStage.setOnHidden(e ->
                                 {
                                     executorService.shutdownNow(); // Force shutdown on close
                                     if (timer != null) timer.stop(); // Stop animation timer
                                     if (onCloseCallback != null) Platform.runLater(onCloseCallback);
                                 });
        primaryStage.setTitle("Twisted Wordle");

        player1       = new Player(promptForPlayerName("Enter Player 1 Name"));
        player2       = new Player(promptForPlayerName("Enter Player 2 Name"));
        currentPlayer = player1;

        loadWordsAsync("src/resources/words.txt", () ->
        { // Adjust path if needed
            Platform.runLater(() ->
                              {
                                  if (wordSet == null || wordSet.isEmpty())
                                  {
                                      showErrorDialog("Error", "Failed to load words! Cannot start game.");
                                      primaryStage.close();
                                      return;
                                  }
                                  initializeGameUI(primaryStage);
                              });
        });
    }

    /**
     * Prompts for player name using a dialog.
     */
    private String promptForPlayerName(final String promptText)
    {
        // ... (promptForPlayerName logic remains the same)
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(primaryStage); // Set owner
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.setTitle("Player Name");
        Label          label     = new Label(promptText);
        TextField      textField = new TextField("Player" + (player1 == null ? "1" : "2"));
        Button         okButton  = new Button("OK");
        final String[] name      = {textField.getText()};
        okButton.setOnAction(e ->
                             {
                                 String enteredName = textField.getText().trim();
                                 if (!enteredName.isEmpty())
                                 {
                                     name[0] = enteredName;
                                     dialogStage.close();
                                 }
                                 else label.setText(promptText + " (Cannot be empty!)");
                             });
        textField.setOnKeyPressed(e ->
                                  {
                                      if (e.getCode() == KeyCode.ENTER) okButton.fire();
                                  });
        VBox vbox = new VBox(10, label, textField, okButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(15));
        Scene scene = new Scene(vbox);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
        return name[0];
    }

    /**
     * Sets up the main game UI elements.
     */
    private void initializeGameUI(final Stage stage)
    {
        // ... (initializeGameUI logic remains the same)
        attemptsLeft = MAX_ATTEMPTS;
        currentRound = FIRST_ROUND;
        final GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));
        gridLabels = new Label[MAX_ATTEMPTS][WORD_LENGTH];
        for (int row = 0; row < MAX_ATTEMPTS; row++)
        {
            for (int col = 0; col < WORD_LENGTH; col++)
            {
                final Label label = new Label("");
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
        final Button submitButton = new Button("Submit");
        submitButton.setDisable(true);
        submitButton.setOnAction(e -> handleGuess());
        submitButton.disableProperty().bind(inputField.disabledProperty());
        final HBox inputBox = new HBox(10, inputField, submitButton);
        inputBox.setAlignment(Pos.CENTER);
        messageLabel = new Label("Loading game...");
        messageLabel.setFont(Font.font(20));
        timerLabel = new Label("Time left: --");
        timerLabel.setFont(Font.font(20));
        scoreLabel = new Label("Scores: " + player1.getName() + ": 0 | " + player2.getName() + ": 0");
        scoreLabel.setFont(Font.font(20));
        roundLabel = new Label("Round: " + currentRound + " of " + TOTAL_ROUNDS);
        roundLabel.setFont(Font.font(20));
        final VBox root = new VBox(20, gridPane, inputBox, messageLabel, timerLabel, scoreLabel, roundLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        final Scene scene = new Scene(root, 600, 700);
        stage.setScene(scene);
        stage.show();
        showInstructionsPopup(stage, this::prepareNextTurn);
    }

    /**
     * Shows the initial instruction popup.
     */
    private void showInstructionsPopup(final Stage ownerStage, final Runnable onStartAction)
    {
        // ... (showInstructionsPopup logic remains the same)
        final Stage popupStage = new Stage();
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.initOwner(ownerStage);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.setTitle("How to Play");
        final Label rulesLabel = new Label( /* ... rules text ... */
                "Welcome to Twisted Wordle!\n\n" +
                "1. Before your turn, your opponent will choose a 5-letter word for you.\n" +
                "2. You have " + MAX_ATTEMPTS + " attempts to guess the word.\n" +
                "3. Green letters are correct and in the right position.\n" +
                "4. Yellow letters are correct but in the wrong position.\n" +
                "5. Gray letters are not in the word.\n" +
                "6. The game has " + TOTAL_ROUNDS + " rounds. The player with the highest score wins!\n\n" +
                "Press 'Start' to begin!"
        );
        rulesLabel.setFont(Font.font(16));
        rulesLabel.setWrapText(true);
        rulesLabel.setPadding(new Insets(10));
        final Button startButton = new Button("Start");
        startButton.setOnAction(e ->
                                {
                                    popupStage.close();
                                    if (onStartAction != null) onStartAction.run();
                                });
        final VBox popupLayout = new VBox(20, rulesLabel, startButton);
        popupLayout.setAlignment(Pos.CENTER);
        popupLayout.setPadding(new Insets(20));
        final Scene popupScene = new Scene(popupLayout, 450, 350);
        popupStage.setScene(popupScene);
        popupStage.showAndWait();
    }

    /**
     * Loads words asynchronously.
     */
    public void loadWordsAsync(final String filename, final Runnable onComplete)
    {
        // ... (loadWordsAsync logic remains the same)
        final Task<List<String>> wordLoadTask = new Task<>()
        {
            @Override
            protected List<String> call() throws IOException
            {
                return Files.lines(Paths.get(filename))
                            .map(String::trim)
                            .filter(word -> word.length() == WORD_LENGTH)
                            .map(String::toUpperCase)
                            .collect(Collectors.toList());
            }
        };
        wordLoadTask.setOnSucceeded(event ->
                                    {
                                        wordList = wordLoadTask.getValue();
                                        wordSet  = new HashSet<>(wordList);
                                        System.out.println("Loaded " + wordSet.size() + " words.");
                                        onComplete.run();
                                    });
        wordLoadTask.setOnFailed(event ->
                                 {
                                     System.err.println("Error loading words!");
                                     wordLoadTask.getException().printStackTrace();
                                     wordList = List.of();
                                     wordSet  = Set.of();
                                     onComplete.run();
                                 });
        executorService.execute(wordLoadTask);
    }

    private void promptForWordSelection(final Player choosingPlayer,
                                        final Player guessingPlayer,
                                        final Runnable onWordChosen)
    {

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Choose Word for " + guessingPlayer.getName());
        dialog.setHeaderText(choosingPlayer.getName() + ", enter a 5-letter word from the list:");

        // --- Create Custom Content ---
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        Label     promptLabel = new Label("Word:");
        TextField wordInput   = new TextField();
        wordInput.setPromptText("Enter valid 5-letter word...");
        wordInput.setStyle("-fx-text-fill: black; -fx-text-inner-color: black;");

        grid.add(promptLabel, 0, 0);
        grid.add(wordInput, 1, 0);
        GridPane.setHgrow(wordInput, Priority.ALWAYS);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 0.9em;");
        errorLabel.setWrapText(true);
        grid.add(errorLabel, 0, 1, 2, 1);
        grid.setMinWidth(350);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(grid);
        // --- End Custom Content ---

        // --- FOCUS FIX ADDED HERE ---
        dialog.setOnShown(event ->
                          {
                              Platform.runLater(() ->
                                                {
                                                    wordInput.requestFocus();
                                                    System.out.println("Focus requested AFTER dialog shown");
                                                });
                          });

        // --- Validation Logic ---
        final Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.addEventFilter(
                ActionEvent.ACTION,
                event ->
                {
                    String  chosenWord   = wordInput.getText().trim().toUpperCase();
                    boolean isValid      = true;
                    String  errorMessage = "";

                    if (chosenWord.length() != WORD_LENGTH)
                    {
                        errorMessage = "Word must be exactly " + WORD_LENGTH + " letters long.";
                        isValid      = false;
                    }
                    else if (!wordSet.contains(chosenWord))
                    {
                        errorMessage = "'" + chosenWord + "' is not in the valid word list.";
                        isValid      = false;
                    }

                    if (!isValid)
                    {
                        errorLabel.setText(errorMessage);
                        event.consume();
                        wordInput.requestFocus();
                        if (chosenWord.length() == WORD_LENGTH) wordInput.selectAll();
                    }
                }
                               );

        if (inputField != null) inputField.setDisable(true);
        if (messageLabel != null) messageLabel.setText(choosingPlayer.getName() +
                                                       " is choosing a word for " + guessingPlayer.getName() + "...");

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK)
        {
            String validWord = wordInput.getText().trim().toUpperCase();
            if (guessingPlayer == player1)
            {
                wordForPlayer1 = validWord;
            }
            else
            {
                wordForPlayer2 = validWord;
            }
            onWordChosen.run();
        }
        else
        {
            if (!executorService.isShutdown())
            {
                Platform.runLater(() ->
                                  {
                                      showErrorAlert("Word Selection Cancelled",
                                                     choosingPlayer.getName() + " did not select a word. Ending game.");
                                      endGame();
                                  });
            }
        }
    }

    // --- Keep the Alert-based showErrorAlert ---

    /**
     * Helper method for showing simple error/warning Alerts.
     */
    private void showErrorAlert(String title, String message)
    {
        if (!Platform.isFxApplicationThread())
        {
            Platform.runLater(() -> showErrorAlert(title, message));
            return;
        }
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(primaryStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    /**
     * Handles the submission and evaluation of a guess.
     */
    private void handleGuess()
    {
        final String guess = inputField.getText().toUpperCase();
        inputField.clear();
        if (guess.length() != WORD_LENGTH)
        {
            messageLabel.setText("Please enter a 5-letter word.");
            return;
        }

        final int       currentAttempt = MAX_ATTEMPTS - attemptsLeft;
        final boolean[] targetMatched  = new boolean[WORD_LENGTH];
        final boolean[] guessMatched   = new boolean[WORD_LENGTH];

        // --- Coloring logic (same as before) ---
        // First pass: Mark green
        for (int i = 0; i < WORD_LENGTH; i++)
        {
            gridLabels[currentAttempt][i].setText(String.valueOf(guess.charAt(i)));
            if (guess.charAt(i) == targetWord.charAt(i))
            {
                gridLabels[currentAttempt][i].setStyle("-fx-background-color: #6aaa64; -fx-text-fill: white; -fx-border-color: #444; -fx-border-width: 1;");
                targetMatched[i] = true;
                guessMatched[i]  = true;
            }
            else
            {
                gridLabels[currentAttempt][i].setStyle("-fx-background-color: #787c7e; -fx-text-fill: white; -fx-border-color: #444; -fx-border-width: 1;");
            }
        }
        // Second pass: Mark yellow
        for (int i = 0; i < WORD_LENGTH; i++)
        {
            if (!guessMatched[i])
            {
                char guessedChar = guess.charAt(i);
                for (int j = 0; j < WORD_LENGTH; j++)
                {
                    if (!targetMatched[j] && guessedChar == targetWord.charAt(j))
                    {
                        gridLabels[currentAttempt][i].setStyle("-fx-background-color: #c9b458; -fx-text-fill: white; -fx-border-color: #444; -fx-border-width: 1;");
                        targetMatched[j] = true;
                        guessMatched[i]  = true;
                        break;
                    }
                }
            }
        }
        // --- End Coloring ---

        attemptsLeft--;

        if (guess.equals(targetWord))
        {
            if (timer != null) timer.stop();
            long elapsedTime = (System.currentTimeMillis() - startTime) / TIME_FORMAT;
            int  timeLeft    = Math.max(0, TURN_TIME - (int) elapsedTime);
            int  score       = calculateScore(attemptsLeft + 1, timeLeft);
            currentPlayer.addScore(score);
            messageLabel.setText("Correct! " + currentPlayer.getName() + " guessed the word!");
            inputField.setDisable(true);
            updateScoreboard();
            PauseTransition delay = new PauseTransition(Duration.seconds(DELAY));
            // --- FIX for IllegalStateException ---
            delay.setOnFinished(e -> Platform.runLater(this::prepareNextTurn));
            // --- End Fix ---
            delay.play();
        }
        else if (attemptsLeft == NOTHING)
        {
            if (timer != null) timer.stop();
            messageLabel.setText("Out of attempts! The word was: " + targetWord);
            inputField.setDisable(true);
            PauseTransition delay = new PauseTransition(Duration.seconds(DELAY));
            // --- FIX for IllegalStateException ---
            delay.setOnFinished(e -> Platform.runLater(this::prepareNextTurn));
            // --- End Fix ---
            delay.play();
        }
        else
        {
            messageLabel.setText(currentPlayer.getName() + ", attempts left: " + attemptsLeft);
        }
    }

    /**
     * Prepares for the next turn by switching players, handling rounds/endgame,
     * and then calling the word selection prompt.
     */
    public void prepareNextTurn()
    {
        if (timer != null)
        {
            timer.stop();
            timer = null;
        }
        timerLabel.setText("Time left: --");

        Player nextPlayer;
        Player wordChooser;
        if (currentPlayer == player1)
        {
            nextPlayer  = player2;
            wordChooser = player1;
        }
        else
        {
            nextPlayer  = player1;
            wordChooser = player2;
            if (currentPlayer != null) currentRound++;
            if (currentRound > TOTAL_ROUNDS)
            {
                endGame();
                return;
            }
            roundLabel.setText("Round: " + currentRound + " of " + TOTAL_ROUNDS);
        }

        if (nextPlayer == player1) wordForPlayer1 = null;
        else wordForPlayer2 = null;

        // Prompt for word selection. The prompt itself now calls showAndWait safely.
        // The callback () -> startTurn(nextPlayer) ensures startTurn runs *after* selection.
        promptForWordSelection(wordChooser, nextPlayer, () -> startTurn(nextPlayer));
    }


    /**
     * Starts the actual guessing turn for the player after word selection.
     *
     * @param player The player whose turn is starting.
     */
    private void startTurn(final Player player)
    {
        currentPlayer = player;
        if (currentPlayer == player1) targetWord = wordForPlayer1;
        else targetWord = wordForPlayer2;

        if (targetWord == null)
        {
            // Use Platform.runLater for safety when showing dialog immediately after potential prior UI events
            Platform.runLater(() ->
                              {
                                  showErrorDialog("Critical Error", "No word was set for " + currentPlayer.getName() + "! Ending game.");
                                  endGame();
                              });
            return;
        }

        System.out.println("Starting turn for " + currentPlayer.getName() + ". Target word: " + targetWord); // Debug

        attemptsLeft = MAX_ATTEMPTS;
        resetGrid();
        messageLabel.setText(currentPlayer.getName() + ", guess the 5-letter word! (" + MAX_ATTEMPTS + " attempts)");
        inputField.setDisable(false);
        Platform.runLater(() -> inputField.requestFocus()); // Focus main input field
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
                gridLabels[row][col].setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-background-color: transparent;");
            }
        }
    }

    /**
     * Calculates score based on attempts and time.
     */
    @Override
    public int calculateScore(final int attemptsLeftBeforeGuess, final int timeLeft)
    {
        int attemptsScore = (attemptsLeftBeforeGuess > 0) ? (attemptsLeftBeforeGuess * ATTEMPTS_LEFT_MULTIPLIER) : 0;
        return BASE_CORRECT_SCORE + attemptsScore + Math.max(0, timeLeft);
    }

    /**
     * Updates the score display label.
     */
    private void updateScoreboard()
    {
        scoreLabel.setText("Scores: " +
                           player1.getName() + ": " + player1.getScore() + " | " +
                           player2.getName() + ": " + player2.getScore());
    }

    /**
     * Starts the turn timer animation.
     */
    private void startTimer()
    {
        if (timer != null) timer.stop();
        startTime = System.currentTimeMillis();
        timer     = new AnimationTimer()
        {
            @Override
            public void handle(final long now)
            {
                long elapsedTime = (System.currentTimeMillis() - startTime) / TIME_FORMAT;
                int  timeLeft    = TURN_TIME - (int) elapsedTime;
                if (timeLeft <= NOTHING)
                {
                    timerLabel.setText("Time left: 0");
                    this.stop();
                    timer = null;
                    messageLabel.setText("Time's up, " + currentPlayer.getName() + "! The word was: " + targetWord);
                    inputField.setDisable(true);
                    PauseTransition delay = new PauseTransition(Duration.seconds(DELAY));
                    // --- FIX for IllegalStateException ---
                    delay.setOnFinished(e -> Platform.runLater(TwistedWordle.this::prepareNextTurn));
                    // --- End Fix ---
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
        // Don't shut down executor here if called during word selection cancellation,
        // let the main window close handle it. Only shut down if game naturally ends.
        // if (currentRound > TOTAL_ROUNDS) { // Only shutdown if game ended normally
        //     executorService.shutdown();
        // }
        timerLabel.setText("Time left: --");
        messageLabel.setText("Game over! Final scores:");
        inputField.setDisable(true);
        updateScoreboard();
        Platform.runLater(this::showWinnerPopup); // Show popup safely
    }

    /**
     * Shows the final winner/tie popup.
     */
    private void showWinnerPopup()
    {
        // ... (showWinnerPopup logic remains the same)
        final Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initOwner(primaryStage);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.setTitle("Game Over");
        final String winnerMessage;
        if (player1.getScore() > player2.getScore()) winnerMessage = player1.getName() + " wins!";
        else if (player2.getScore() > player1.getScore()) winnerMessage = player2.getName() + " wins!";
        else winnerMessage = "It's a tie!";
        final Label finalScoreLabel = new Label(player1.getName() + ": " + player1.getScore() + "\n" + player2.getName() + ": " + player2.getScore());
        finalScoreLabel.setFont(Font.font(16));
        final Label winnerLabel = new Label(winnerMessage);
        winnerLabel.setFont(Font.font(20));
        winnerLabel.setStyle("-fx-font-weight: bold;");
        final Button closeButton = new Button("Close Game");
        closeButton.setOnAction(e ->
                                {
                                    popupStage.close();
                                    if (primaryStage != null) primaryStage.close();
                                    if (onCloseCallback != null) Platform.runLater(onCloseCallback);
                                });
        final VBox popupLayout = new VBox(15, finalScoreLabel, winnerLabel, closeButton);
        popupLayout.setAlignment(Pos.CENTER);
        popupLayout.setPadding(new Insets(25));
        final Scene popupScene = new Scene(popupLayout, 350, 200);
        popupStage.setScene(popupScene);
        popupStage.showAndWait(); // This is safe here as it's called via runLater from endGame
    }

    /**
     * Helper method for showing simple error dialogs (using custom stage).
     */
    private void showErrorDialog(String title, String message)
    {
        // Ensure runs on FX thread (already wrapped in runLater where needed, but good practice)
        if (!Platform.isFxApplicationThread())
        {
            Platform.runLater(() -> showErrorDialog(title, message));
            return;
        }
        Stage errorStage = new Stage();
        errorStage.initModality(Modality.APPLICATION_MODAL);
        errorStage.initOwner(primaryStage);
        errorStage.initStyle(StageStyle.UTILITY);
        errorStage.setTitle(title);
        Label label = new Label(message);
        label.setPadding(new Insets(15));
        label.setWrapText(true);
        Button okButton = new Button("OK");
        okButton.setOnAction(e -> errorStage.close());
        VBox vbox = new VBox(10, label, okButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(10));
        Scene scene = new Scene(vbox, 300, 150);
        errorStage.setScene(scene);
        errorStage.showAndWait(); // Safe if not called during animation/layout
    }
}
