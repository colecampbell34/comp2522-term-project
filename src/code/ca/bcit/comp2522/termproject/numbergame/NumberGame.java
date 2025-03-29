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

                buttons[row][col].setOnAction(event -> handleGridButtonClick(finalRow, finalCol));
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
        resetUI(); // Set initial button text here
        gameActive = true;
        setTotalPlacements(NOTHING);
        generateNextNumber(); // Generate first number
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
                buttons[row][col].setDisable(false);
            }
        }
        statusLabel.setText("Game started! Place the first number.");
    }

    /**
     * Generates the next number to be placed and updates the status label.
     */
    private void generateNextNumber()
    {
        int nextNum = random.nextInt(UPPER_BOUND - LOWER_BOUND + OFFSET) + LOWER_BOUND;
        setCurrentNumber(nextNum);
        statusLabel.setText("Place the number: " + getCurrentNumber());
    }

    /**
     * Handles clicks on the grid buttons. Renamed from placeNumber/handleButtonClick
     */
    private void handleGridButtonClick(final int row, final int col)
    {
        if (!gameActive)
        {
            return;
        }

        int index = row * SQUARES_WIDE + col;

        if (index < NOTHING || index >= MAX_PLACEMENTS)
        {
            System.err.println("Error: Invalid index calculated: " + index + " for row=" + row + ", col=" + col);
            return;
        }

        if (gameBoard[index] != EMPTY_SPOT)
        {
            statusLabel.setText("Spot already taken!"); // Optional feedback
            return;
        }

        int numToPlace = getCurrentNumber();
        if (numToPlace == EMPTY_SPOT)
        {
            return;
        }


        if (canPlaceNumber(index, numToPlace))
        {
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
                generateNextNumber();
            }
        }
        else
        {
            // Cannot place the number legally - Player loses
            endGame(false);
        }
    }

    /**
     * Checks if a number can be legally placed at the given 1D index.
     * Numbers must be placed in increasing order across the 1D array.
     *
     * @param index  The 1D array index where placement is attempted.
     * @param number The number to place.
     * @return true if placement is legal, false otherwise.
     */
    // Simplified check based on 1D array ordering
    private boolean canPlaceNumber(final int index, final int number)
    {

        // Check number to the left (index - 1) if it exists and isn't empty
        if (index > NOTHING && gameBoard[index - OFFSET] != EMPTY_SPOT &&
            number < gameBoard[index - OFFSET])
        {
            statusLabel.setText("Lost! Cannot place " + number); // Loss message
            return false;
        }

        // Check number to the right (index + 1) if it exists and isn't empty
        if (index < MAX_PLACEMENTS - OFFSET && gameBoard[index + OFFSET] != EMPTY_SPOT &&
            number > gameBoard[index + OFFSET])
        {
            statusLabel.setText("Lost! Cannot place " + number); // Loss message
            return false;
        }

        // Placement is valid according to neighbours
        return true;
    }


    /**
     * Ends the current game round, updates score, and prompts for replay using original Alert.
     *
     * @param won true if the player won, false otherwise.
     */
    private void endGame(final boolean won)
    {
        gameActive = false;
        incrementGamesPlayed();

        String headerText; // Use header for main result

        if (won)
        {
            incrementGamesWon();
            headerText = "Congratulations! You won!";
            statusLabel.setText("You won!");
        }
        else
        {
            // If lost, canPlaceNumber or other logic should have set statusLabel
            headerText = statusLabel.getText(); // Use the specific loss reason if available
            if (headerText == null ||
                !headerText.startsWith("Lost!"))
            {
                headerText = "You lost! It's impossible to place the next number."; // Default loss
            }
        }
        updateScore();

        final Alert alert;
        alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(headerText);
        alert.setContentText(won ? "You won! Would you like to play again?" :
                             "You lost! Would you like to try again?");

        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        final Optional<ButtonType> result;
        result = alert.showAndWait();

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

    /**
     * Safely closes the game's Stage.
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
            System.err.println("Error: Game stage reference was null. Cannot close window.");
        }
    }
}
