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
import java.util.Optional;
import java.util.Random;

/**
 * The Number Game implementation. Sets up and manages the game UI and logic.
 *
 * @author colecampbell
 * @version 1.1 // Version updated to reflect changes
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
     * Constructs a NumberGame object and sets up its UI on the provided Stage.
     *
     * @param stage The JavaFX Stage on which this game will be displayed.
     */
    public NumberGame(final Stage stage)
    {
        if (stage == null)
        {
            throw new IllegalArgumentException("Stage cannot be null");
        }

        this.gameStage = stage;
        resetLogicOnly();
        setupUI(); // Build the UI components
        this.gameStage.show();
    }

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

        for (int row = 0; row < SQUARES_TALL; row++)
        {
            for (int col = 0; col < SQUARES_WIDE; col++)
            {
                buttons[row][col] = new Button();
                buttons[row][col].setPrefSize(60, 60);

                final int finalRow = row;
                final int finalCol = col;

                buttons[row][col].setOnAction(e -> handleGridButtonClick(finalRow, finalCol));
                gridPane.add(buttons[row][col], col, row);
            }
        }

        statusLabel = new Label("Click Start to begin!");
        statusLabel.setAlignment(Pos.CENTER);

        final Button startButton;
        startButton = new Button("Start Game");
        startButton.setOnAction(event -> startGame());

        root.getChildren().addAll(statusLabel, gridPane, startButton);

        final Scene scene;
        scene = new Scene(root, WIDTH, HEIGHT);
        gameStage.setScene(scene);
    }

    /**
     * Starts or restarts the game logic and UI.
     */
    @Override
    public void startGame()
    {
        System.out.println("Starting new Number Game round.");
        resetLogicOnly();
        resetUI(); // Set initial button text and enable buttons
        setTotalPlacements(NOTHING);
        gameActive = true; // Set active before generating number

        generateNextNumber(); // Generate first number

        // checking if the first number can be placed just in case
        if (!canPlaceCurrentNumberAnywhere())
        {
            statusLabel.setText("Lost! Impossible to place first number " + getCurrentNumber());
            disableAllButtons(); // Prevent clicks
            endGame(false);
            // gameActive is set to false within endGame
        }
        // If we get here, the game starts normally, statusLabel set by generateNextNumber
    }

    /**
     * Resets the game board logic array.
     */
    private void resetLogicOnly()
    {
        Arrays.fill(gameBoard, EMPTY_SPOT);
        setCurrentNumber(EMPTY_SPOT);
    }

    /**
     * Resets the UI elements to their initial state.
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

    /**
     * Generates the next number to be placed and updates the status label.
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
        statusLabel.setText("Place the number: " + getCurrentNumber());
    }

    /**
     * Handles clicks on the grid buttons.
     */
    private void handleGridButtonClick(final int row,
                                       final int col)
    {
        if (!gameActive)
        {
            return;
        }

        final int index;
        index = row * SQUARES_WIDE + col;

        if (index < NOTHING ||
            index >= MAX_PLACEMENTS)
        {
            System.err.println("Error: Invalid index calculated: " + index +
                               " for row=" + row +
                               ", col=" + col);
            return;
        }

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
                endGame(true); // Player won
            }
            else
            {
                // Generate the next number
                generateNextNumber();

                if (gameActive && !canPlaceCurrentNumberAnywhere())
                {
                    statusLabel.setText("Lost! No valid spot for " + getCurrentNumber());
                    disableAllButtons();
                    endGame(false); // Player loses because no valid moves exist
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
            endGame(false); // Player loses for making an incorrect placement
        }
    }


    /**
     * Checks if a number can be legally placed at the given 1D index based on
     * all other placed numbers. Numbers must maintain strictly increasing order
     * across the flattened board (left-to-right, top-to-bottom).
     *
     * @param index  The 1D array index where placement is attempted.
     * @param number The number to place.
     * @return true if placement is legal, false otherwise.
     */
    private boolean canPlaceNumber(final int index,
                                   final int number)
    {
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

        // If we passed both checks, the placement is valid relative to all other numbers
        return true;
    }

    /**
     * Checks if the current number (returned by getCurrentNumber()) can be legally
     * placed in ANY remaining empty spot on the board.
     *
     * @return true if there is at least one valid spot, false otherwise.
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


    /**
     * Ends the current game round, updates score, disables buttons, and prompts for replay.
     *
     * @param won true if the player won, false otherwise.
     */
    private void endGame(final boolean won)
    {
        if (!gameActive)
        {
            return; // Avoid multiple calls if already ended
        }

        gameActive = false; // Mark game as inactive first

        incrementGamesPlayed();

        String headerText; // Use header for main result

        if (won)
        {
            incrementGamesWon();
            headerText = "Congratulations! You won!";
            statusLabel.setText("You won!"); // Final status update
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

        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        final Optional<ButtonType> result;
        result = alert.showAndWait();

        updateScore();

        if (result.isPresent() &&
            result.get() == ButtonType.YES)
        {
            startGame(); // Restart the game
        }
        else
        {
            closeGameWindow(); // Close the application window
        }
    }

    /**
     * Disables all grid buttons, typically used when the game ends unexpectedly.
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


    /**
     * Safely closes the games Stage.
     */
    private void closeGameWindow()
    {
        System.out.println("Closing the Number Game window.");

        if (gameStage != null)
        {
            gameStage.close();
        }
        else
        {
            System.err.println("Error: Cannot close window.");
        }
    }
}
