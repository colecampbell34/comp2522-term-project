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

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/**
 * A JavaFX-based mini-game where the player places 20 randomly generated numbers
 * onto a 5x4 grid. The numbers must be placed in strictly ascending order from left
 * to right, top to bottom. The game ends in a win if all numbers are placed legally,
 * or in a loss if an illegal placement is made or no valid placements remain.
 *
 * This class is responsible for managing both the UI and the core game logic, including:
 * - UI layout with JavaFX components
 * - Random number generation
 * - Validation of player moves
 * - Tracking of game state and score
 * - Displaying alerts for win/loss conditions
 *
 * This game extends AbstractGame and follows the game interface/structure of the overall project.
 *
 * @author colecampbell
 * @version 1.0
 */
public final class NumberGame extends AbstractGame
{
    private static final int SQUARES_WIDE   = 5;
    private static final int SQUARES_TALL   = 4;
    private static final int NOTHING        = 0;
    private static final int UPPER_BOUND    = 1000;
    private static final int LOWER_BOUND    = 1;
    private static final int MAX_PLACEMENTS = SQUARES_WIDE * SQUARES_TALL;
    private static final int EMPTY_SPOT     = -1;
    private static final int HEIGHT         = 500;
    private static final int WIDTH          = 500;
    private static final int OFFSET         = 1;

    private final Button[][] buttons   = new Button[SQUARES_TALL][SQUARES_WIDE];
    private       Label      statusLabel;
    private final Random     random    = new Random();
    private final Stage      gameStage;
    private       boolean    gameActive;
    private final int[]      gameBoard = new int[MAX_PLACEMENTS];

    /**
     * Constructs the NumberGame and initializes the user interface on the provided JavaFX Stage.
     * This sets up the game layout, binds button actions, and displays the initial screen to the user.
     *
     * @param stage the primary JavaFX Stage where the NumberGame UI will be shown
     * @throws NullPointerException if the provided stage is null
     */
    public NumberGame(final Stage stage)
    {
        Objects.requireNonNull(stage,
                               "Stage cannot be null");

        this.gameStage = stage;
        resetLogicOnly();
        setupUI(); // Build the UI components
        this.gameStage.show();
    }

    /*
     * Creates and arranges all JavaFX UI components for the game screen.
     * This includes:
     * - A 5x4 grid of buttons representing the game board
     * - A status label displaying instructions and messages
     * - "Start New Game" and "Quit" buttons
     * All UI actions are connected to corresponding game logic methods.
     */
    private void setupUI()
    {
        final VBox root;
        root = new VBox(10);
        root.setAlignment(Pos.CENTER);

        final GridPane gridPane;
        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        // set up actions for all buttons in the grid
        for (int row = 0; row < SQUARES_TALL; row++)
        {
            for (int col = 0; col < SQUARES_WIDE; col++)
            {
                buttons[row][col] = new Button();
                buttons[row][col].setPrefSize(60, 60);

                final int finalRow = row;
                final int finalCol = col;

                buttons[row][col].setOnAction(
                        e -> handleGridButtonClick(finalRow, finalCol));

                gridPane.add(buttons[row][col],
                             col,
                             row);
            }
        }

        statusLabel = new Label("Click Start to begin!");
        statusLabel.setAlignment(Pos.CENTER);

        final Button startButton;
        startButton = new Button("Start New Game");
        startButton.setOnAction(event -> startGame());

        final Button quitButton;
        quitButton = new Button("Quit");
        quitButton.setOnAction(event -> endGame(false));

        root.getChildren().addAll(statusLabel,
                                  gridPane,
                                  startButton,
                                  quitButton);

        final Scene scene;
        scene = new Scene(root, WIDTH, HEIGHT);
        gameStage.setScene(scene);
    }

    /**
     * Starts a new game session by resetting game logic, UI, and the first random number.
     * This method also checks whether the first number can be placed legally; if not, the game
     * ends immediately with a loss.
     */
    @Override
    public void startGame()
    {
        resetLogicOnly();
        resetUI();
        setTotalPlacements(NOTHING);
        gameActive = true;

        generateNextNumber();

        // checking if the first number can be placed just in case
        if (!canPlaceCurrentNumberAnywhere())
        {
            statusLabel.setText("Lost! Impossible to place first number " +
                                getCurrentNumber());
            disableAllButtons();
            endGame(false);
        }

        // If we get here, the game starts normally, statusLabel set by generateNextNumber
    }

    /*
     * Clears the internal game board state and resets the current number to EMPTY_SPOT.
     * This method does not update or modify the UI.
     */
    private void resetLogicOnly()
    {
        Arrays.fill(gameBoard, EMPTY_SPOT);
        setCurrentNumber(EMPTY_SPOT);
    }

    /*
     * Resets all buttons in the game grid to their default appearance ("[ ]" and enabled),
     * and updates the status label to indicate that the game has started.
     */
    private void resetUI()
    {
        for (int row = 0; row < SQUARES_TALL; row++)
        {
            for (int col = 0; col < SQUARES_WIDE; col++)
            {
                buttons[row][col].setText("[ ]"); // Set initial empty text
                buttons[row][col].setDisable(false); // Ensure buttons are enabled
            }
        }
        statusLabel.setText("Game started! Place the first number.");
    }

    /*
     * Generates a new random number within the predefined bounds (1 to 1000),
     * updates the internal current number, and refreshes the status label
     * to prompt the player to place it on the board.
     * If the game is not active, no number is generated.
     */
    private void generateNextNumber()
    {
        // Only generate if the game is active (avoid issues if called after game ends)
        if (!gameActive)
        {
            return;
        }

        final int nextNum;
        nextNum = random.nextInt(UPPER_BOUND - LOWER_BOUND + OFFSET) + LOWER_BOUND;

        setCurrentNumber(nextNum);

        // display the next number to the user
        statusLabel.setText("Place the number: " + getCurrentNumber());
    }

    /*
     * Responds to player clicks on grid buttons by attempting to place the current number
     * at the selected location. It validates placement legality based on ascending order rules.
     * If the placement is valid, the number is placed and a new number is generated.
     * If the move is illegal or if no more valid moves exist, the game ends.
     */
    private void handleGridButtonClick(final int row,
                                       final int col)
    {
        if (!gameActive)
        {
            return;
        }

        validateRow(row);
        validateCol(col);

        // convert the index for a 1-D array to check values
        final int index;
        index = row * SQUARES_WIDE + col;

        if (gameBoard[index] != EMPTY_SPOT)
        {
            statusLabel.setText("Spot already taken! Try again.");
            return;
        }

        final int numToPlace;
        numToPlace= getCurrentNumber();

        if (canPlaceNumber(index, numToPlace))
        {
            // valid placement
            gameBoard[index] = numToPlace;
            buttons[row][col].setText(String.valueOf(numToPlace));

            incrementTotalPlacements();

            // Check for win condition
            if (getTotalPlacements() == MAX_PLACEMENTS)
            {
                endGame(true);
            }
            else
            {
                generateNextNumber();

                if (gameActive && !canPlaceCurrentNumberAnywhere())
                {
                    // Player loses because no valid moves exist
                    statusLabel.setText("Lost! No valid spot for " + getCurrentNumber());
                    disableAllButtons();
                    endGame(false);
                }
            }
        }
        else
        {
            // The user clicked a spot where this number cannot
            // legally go based on overall order.
            statusLabel.setText("Lost! Placing " +
                                numToPlace +
                                " there is illegal.");
            disableAllButtons();
            endGame(false);
        }
    }

    /*
     * Ensures the given row index is within the bounds of the game grid.
     * Throws IllegalArgumentException if the row is invalid.
     */
    private static void validateRow(final int row)
    {
        if (row < NOTHING ||
            row > SQUARES_TALL)
        {
            throw new IllegalArgumentException("Row is out of bounds");
        }
    }

    /*
     * Ensures the given column index is within the bounds of the game grid.
     * Throws IllegalArgumentException if the column is invalid.
     */
    private static void validateCol(final int col)
    {
        if (col < NOTHING ||
            col > SQUARES_WIDE)
        {
            throw new IllegalArgumentException("Column is out of bounds");
        }
    }

    /*
     * Determines whether a given number can be legally placed at the specified
     * 1D board index. Placement rules require:
     * - The number must be greater than all numbers to the left (lower indices)
     * - The number must be less than all numbers to the right (higher indices)
     * Returns true if the placement is valid, false otherwise.
     */
    private boolean canPlaceNumber(final int index,
                                   final int number)
    {
        validateIndex(index);
        validateNumber(number);

        // Check all numbers placed at indices BEFORE the target index
        for (int i = 0; i < index; i++)
        {
            if (gameBoard[i] != EMPTY_SPOT &&
                number <= gameBoard[i])
            {
                // Found a number to the left that is >= the number we want to place
                return false;
            }
        }

        // Check all numbers placed at indices AFTER the target index
        for (int i = index + 1; i < MAX_PLACEMENTS; i++)
        {
            if (gameBoard[i] != EMPTY_SPOT &&
                number >= gameBoard[i])
            {
                // Found a number to the right that is <= the number we want to place
                return false;
            }
        }

        // If we passed both checks, the placement is valid
        return true;
    }

    /*
     * Ensures the index is a valid position in the internal 1D game board array.
     * Throws IllegalArgumentException if the index is out of range.
     */
    private static void validateIndex(final int index)
    {
        if (index < NOTHING ||
            index > MAX_PLACEMENTS)
        {
            throw new IllegalArgumentException("Index is out of bounds");
        }
    }

    /*
     * Ensures the number is within the allowed game bounds (1 to 1000).
     * Throws IllegalArgumentException if the number is invalid.
     */
    private static void validateNumber(final int number)
    {
        if (number < LOWER_BOUND ||
            number > UPPER_BOUND)
        {
            throw new IllegalArgumentException("Number is out of bounds");
        }
    }

    /*
     * Checks whether the current number can legally be placed in any
     * empty spot on the board. Used to determine if the game should continue.
     * Returns true if at least one valid placement exists, false otherwise.
     */
    private boolean canPlaceCurrentNumberAnywhere()
    {
        final int numToCheck;
        numToCheck= getCurrentNumber();

        for (int i = 0; i < MAX_PLACEMENTS; i++)
        {
            if (gameBoard[i] == EMPTY_SPOT)
            {
                // Found an empty spot. Check if numToCheck can legally go here.
                if (canPlaceNumber(i, numToCheck))
                {
                    // Yes, found at least one valid spot. Game can continue.
                    return true;
                }
            }
        }

        // Went through all spots, no empty spot allows placing numToCheck legally.
        return false;
    }

    /*
     * Finalizes the game based on whether the player won or lost.
     * - Updates the score and game state
     * - Disables the grid buttons
     * - Displays an alert dialog asking if the player wants to play again
     * If the player chooses not to replay, the game window is closed.
     */
    private void endGame(final boolean won)
    {
        if (!gameActive)
        {
            return;
        }

        gameActive = false; // Mark game as inactive first

        incrementGamesPlayed();

        String headerText;

        if (won)
        {
            incrementGamesWon();
            headerText = "Congratulations! You won!";
            statusLabel.setText("You won!");
        }
        else
        {
            headerText = statusLabel.getText();

            if (headerText == null ||
                headerText.isEmpty() ||
                headerText.startsWith("Place the number:"))
            {
                headerText = "You lost!";
                statusLabel.setText("You lost!");
            }
        }

        final Alert alert;
        alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Game Over");
        alert.setHeaderText(headerText);
        alert.setContentText(won ? "Excellent work! Would you like to play again?" :
                             "Better luck next time! Would you like to try again?");

        alert.getButtonTypes().setAll(ButtonType.YES,
                                      ButtonType.NO);

        final Optional<ButtonType> result;
        result = alert.showAndWait();

        updateScore();

        if (result.isPresent() &&
            result.get() == ButtonType.YES)
        {
            startGame();
        }
        else
        {
            closeGameWindow();
        }
    }

    /*
     * Disables all buttons on the game board to prevent further interaction
     * once the game ends.
     */
    private void disableAllButtons()
    {
        for (int r = 0; r < SQUARES_TALL; r++)
        {
            for (int c = 0; c < SQUARES_WIDE; c++)
            {
                if (buttons[r][c] != null)
                {
                    buttons[r][c].setDisable(true);
                }
            }
        }
    }

    /*
     * Safely closes the JavaFX window for the NumberGame.
     */
    private void closeGameWindow()
    {
        Objects.requireNonNull(gameStage,
                               "Stage cannot be null");
        gameStage.close();
    }
}
