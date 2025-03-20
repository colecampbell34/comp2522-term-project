package ca.bcit.comp2522.termproject.numbergame;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.Random;

public class NumberGame extends AbstractGame
{
    private static final int SQUARES_WIDE     = 4;
    private static final int SQUARES_TALL     = 5;
    private static final int NOTHING          = 0;
    private static final int UPPER_BOUND      = 1000;
    private static final int LOWER_BOUND      = 1;
    private static final int MAX_PLACEMENTS   = 20;
    private static final int FIRST_SPOT       = 1;
    private static final int LAST_SPOT        = 19;
    private static final int ARRAY_CONVERSION = 5;
    private static final int EMPTY_SPOT       = -1;
    private static final int INDEXING         = 1;


    private final Button[][] buttons = new Button[SQUARES_WIDE][SQUARES_TALL];
    private       Label      statusLabel;
    private final Random     random  = new Random();
    private final Stage      primaryStage;
    private       boolean    gameActive;

    /**
     * Constructs a NumberGame object.
     *
     * @param primaryStage the stage
     */
    public NumberGame(final Stage primaryStage)
    {
        this.primaryStage = primaryStage;
        setupUI();
    }

    private void setupUI()
    {
        final VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);

        // Create the grid of buttons
        final GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        for (int i = 0; i < SQUARES_WIDE; i++)
        {
            for (int j = 0; j < SQUARES_TALL; j++)
            {
                buttons[i][j] = new Button();
                buttons[i][j].setPrefSize(60, 60);
                int finalI = i;
                int finalJ = j;
                buttons[i][j].setOnAction(event -> placeNumber(finalI, finalJ));
                gridPane.add(buttons[i][j], j, i);
            }
        }

        // Status label
        statusLabel = new Label("Click Start to begin!");
        statusLabel.setAlignment(Pos.CENTER);

        // Start button
        final Button startButton = new Button("Start Game");
        startButton.setOnAction(event -> startGame());

        // Add components to the root
        root.getChildren().addAll(statusLabel, gridPane, startButton);

        // Set up the scene
        final Scene scene = new Scene(root, 500, 500);
        primaryStage.setTitle("Number Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Starts the game.
     */
    @Override
    public void startGame()
    {
        resetGame();
        gameActive      = true;
        totalPlacements = NOTHING; // Reset the total placements counter
        for (int i = 0; i < SQUARES_WIDE; i++)
        {
            for (int j = 0; j < SQUARES_TALL; j++)
            {
                buttons[i][j].setText("[ ]"); // Clear button text
            }
        }
        currentNumber = random.nextInt(UPPER_BOUND) + LOWER_BOUND;
        statusLabel.setText("Place the number: " + currentNumber);
    }

    private void placeNumber(final int row, final int col)
    {
        if (!gameActive) return; // Do nothing if the game is over

        if (numbers[row * ARRAY_CONVERSION + col] == EMPTY_SPOT && currentNumber != EMPTY_SPOT)
        {
            // Check if the number can be placed in ascending order
            if (canPlaceNumber(row, col))
            {
                numbers[row * ARRAY_CONVERSION + col] = currentNumber;
                buttons[row][col].setText(String.valueOf(currentNumber));
                totalPlacements++;

                if (totalPlacements == MAX_PLACEMENTS)
                {
                    endGame(true); // All numbers placed in order
                }
                else
                {
                    currentNumber = random.nextInt(UPPER_BOUND) + LOWER_BOUND;
                    statusLabel.setText("Place the number: " + currentNumber);
                }
            }
            else
            {
                endGame(false); // Cannot place the number in ascending order
            }
        }
    }

    private boolean canPlaceNumber(final int row, final int col)
    {
        int index = row * ARRAY_CONVERSION + col;
        // Check the previous and next numbers to ensure ascending order
        if (index > FIRST_SPOT && numbers[index - INDEXING] != EMPTY_SPOT && currentNumber < numbers[index - INDEXING])
        {
            return false; // Cannot place a smaller number after a larger one
        }
        if (index < LAST_SPOT && numbers[index + INDEXING] != EMPTY_SPOT && currentNumber > numbers[index + INDEXING])
        {
            return false; // Cannot place a larger number before a smaller one
        }
        return true;
    }

    private void endGame(final boolean won)
    {
        gameActive = false; // Stop the game
        gamesPlayed++;
        if (won)
        {
            gamesWon++;
            statusLabel.setText("Congratulations! You won!");
        }
        else
        {
            statusLabel.setText("You lost! It's impossible to place the next number.");
        }
        updateScore();

        // Ask the user if they want to try again or quit
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText(won ? "You won! Would you like to play again?" :
                             "You lost! Would you like to try again?");

        final ButtonType tryAgainButton = new ButtonType("Try Again");
        final ButtonType quitButton     = new ButtonType("Quit");
        alert.getButtonTypes().setAll(tryAgainButton, quitButton);

        final Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == tryAgainButton)
        {
            startGame(); // Restart the game
        }
        else
        {
            primaryStage.close(); // Close the game window
        }
    }
}
