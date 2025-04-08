package ca.bcit.comp2522.termproject.numbergame;

import javafx.scene.control.Alert;

import java.util.Arrays;

/**
 * An abstract base class representing a generalized number-based board game.
 *
 * This class provides shared functionality for games that involve placing numbers into a fixed-size board.
 * It manages core game state including:
 * - The board layout (represented by an integer array)
 * - The currently selected number
 * - Gameplay statistics such as total games played, games won, and total placements
 *
 * It includes utility methods for resetting the game board, updating score statistics,
 * and maintaining game state across multiple rounds.
 * Subclasses are expected to implement their specific gameplay mechanics while leveraging this base structure.
 *
 * Designed for use with a JavaFX user interface.
 *
 * @author colecampbell
 * @version 1.0
 */
public abstract class AbstractGame implements GameInterface
{
    private static final int MAX_PLACEMENTS = 20;
    private static final int NOTHING        = 0;
    private static final int EMPTY_SPOT     = -1;

    private final int[] numbers;
    private       int   currentNumber;
    private       int   gamesPlayed;
    private       int   gamesWon;
    private       int   totalPlacements; // successful placements in the current game

    /**
     * Constructs an abstract game object, initializing the core game state.
     * - Initializes the numbers array
     * - Resets the game board using the helper method
     * - Resets the games played and games won stats
     */
    public AbstractGame()
    {
        numbers = new int[MAX_PLACEMENTS];
        resetGame();
        gamesPlayed = NOTHING;
        gamesWon    = NOTHING;
    }

    /**
     * Resets the internal state of the game board to its initial configuration.
     * Specifically:
     * - Fills the entire numbers array with the EMPTY_SPOT value, effectively clearing all placements.
     * - Resets currentNumber to EMPTY_SPOT to indicate that no number is selected for placement.
     *
     * Note: This method does not reset game statistics such as gamesPlayed, gamesWon, or totalPlacements.
     */
    @Override
    public void resetGame()
    {
        Arrays.fill(numbers, EMPTY_SPOT); // Reset the numbers array
        currentNumber = EMPTY_SPOT; // No current number until game starts
    }

    /**
     * Calculates and displays the user's current game statistics using a pop-up alert.
     * Statistics shown include:
     * - Total number of games played
     * - Total number of games won
     * - Number of placements in the last game
     * - Average number of placements across all games played
     *
     * The average is calculated as totalPlacements divided by gamesPlayed.
     * If no games have been played, the average is shown as 0.
     * The statistics are formatted into a string and passed to the UI via an alert dialog.
     */
    @Override
    public void updateScore()
    {
        final double averagePlacements;

        if (gamesPlayed == NOTHING)
        {
            averagePlacements = NOTHING;
        }
        else
        {
            averagePlacements = (double) totalPlacements / gamesPlayed;
        }


        final String scoreMessage;
        scoreMessage = String.format(
                "Stats for the last round:" +
                "\nGames Played: %d" +
                "\nGames Won: %d" +
                "\nPlacements in last game: %d" +
                "\nAverage placements: %.2f",
                gamesPlayed,
                gamesWon,
                totalPlacements,
                averagePlacements);

        showScoreDialog(scoreMessage);
    }

    /*
     * Displays the score for the user.
     */
    private void showScoreDialog(final String message)
    {
        final Alert alert;
        alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Score Report");
        alert.setHeaderText(null); // No header
        alert.setContentText(message);
        alert.showAndWait(); // Blocks until user clicks OK
    }

    /*
     * Sets the total number of placements.
     */
    void setTotalPlacements(final int totalPlacements)
    {
        this.totalPlacements = totalPlacements;
    }

    /*
     * Sets the current number.
     */
    void setCurrentNumber(final int currentNumber)
    {
        this.currentNumber = currentNumber;
    }

    /*
     * Gets the current number.
     */
    int getCurrentNumber()
    {
        return this.currentNumber;
    }

    /*
     * Increments the total placements by one.
     */
    void incrementTotalPlacements()
    {
        totalPlacements++;
    }

    /*
     * Gets the total number of placements.
     */
    int getTotalPlacements()
    {
        return totalPlacements;
    }

    /*
     * Increments the number of games played by one.
     */
    void incrementGamesPlayed()
    {
        gamesPlayed++;
    }

    /*
     * Increments the number of games won by one.
     */
    void incrementGamesWon()
    {
        gamesWon++;
    }
}
