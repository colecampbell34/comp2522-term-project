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

/**
 * The Number Game implementation.
 *
 * @author colecampbell
 * @version 1.0
 */
public final class NumberGame extends AbstractGame
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
    private static final int HEIGHT           = 500;
    private static final int WIDTH            = 500;

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
        final VBox root;
        root = new VBox(10);
        root.setAlignment(Pos.CENTER);

        final GridPane gridPane;
        gridPane = new GridPane();
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

        statusLabel = new Label("Click Start to begin!");
        statusLabel.setAlignment(Pos.CENTER);

        final Button startButton;
        startButton = new Button("Start Game");
        startButton.setOnAction(event -> startGame());

        root.getChildren().addAll(statusLabel, gridPane, startButton);

        final Scene scene;
        scene = new Scene(root, HEIGHT, WIDTH);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Number Game");
        primaryStage.show();
    }

    /**
     * Starts the game.
     */
    @Override
    public void startGame()
    {
        resetGame();
        gameActive = true;
        this.setTotalPlacements(NOTHING);

        for (int i = 0; i < SQUARES_WIDE; i++)
        {
            for (int j = 0; j < SQUARES_TALL; j++)
            {
                buttons[i][j].setText("[ ]");
            }
        }
        this.setCurrentNumber(random.nextInt(UPPER_BOUND) + LOWER_BOUND);
        statusLabel.setText("Place the number: " + this.getCurrentNumber());
    }

    private void placeNumber(final int row, final int col)
    {
        if (!gameActive)
        {
            return;
        }

        if (this.getNumberAtArrayIndex(row * ARRAY_CONVERSION + col) == EMPTY_SPOT &&
            this.getCurrentNumber() != EMPTY_SPOT)
        {
            if (canPlaceNumber(row, col))
            {
                this.setNumberAtArrayIndex(row * ARRAY_CONVERSION + col, this.getCurrentNumber());
                buttons[row][col].setText(String.valueOf(this.getCurrentNumber()));
                this.incrementTotalPlacements();

                if (this.getTotalPlacements() == MAX_PLACEMENTS)
                {
                    endGame(true);
                }
                else
                {
                    this.setCurrentNumber(random.nextInt(UPPER_BOUND) + LOWER_BOUND);
                    statusLabel.setText("Place the number: " + this.getCurrentNumber());
                }
            }
            else
            {
                endGame(false);
            }
        }
    }

    private boolean canPlaceNumber(final int row, final int col)
    {
        int index = row * ARRAY_CONVERSION + col;
        if (index > FIRST_SPOT && this.getNumberAtArrayIndex(index - INDEXING) != EMPTY_SPOT
            && this.getCurrentNumber() < this.getNumberAtArrayIndex(index - INDEXING))
        {
            return false;
        }
        if (index < LAST_SPOT && this.getNumberAtArrayIndex(index + INDEXING) != EMPTY_SPOT
            && this.getCurrentNumber() > this.getNumberAtArrayIndex(index + INDEXING))
        {
            return false;
        }
        return true;
    }

    private void endGame(final boolean won)
    {
        gameActive = false;
        this.incrementGamesPlayed();
        if (won)
        {
            this.incrementGamesWon();
            statusLabel.setText("Congratulations! You won!");
        }
        else
        {
            statusLabel.setText("You lost! It's impossible to place the next number.");
        }
        updateScore();

        final Alert alert;
        alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText(won ? "You won! Would you like to play again?" :
                             "You lost! Would you like to try again?");

        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        final Optional<ButtonType> result;
        result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES)
        {
            startGame();
        }
        else
        {
            primaryStage.close();
        }
    }
}