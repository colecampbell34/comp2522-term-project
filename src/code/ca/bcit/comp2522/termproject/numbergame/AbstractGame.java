package ca.bcit.comp2522.termproject.numbergame;

import javafx.scene.control.Alert;

import java.util.Arrays;

/**
 * Abstract class for a board game.
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
     * Constructs an abstract game object.
     */
    public AbstractGame()
    {
        numbers = new int[MAX_PLACEMENTS]; // Create the array
        resetGame(); // Initialize the game state (fills array, sets currentNumber)
        gamesPlayed = NOTHING;
        gamesWon    = NOTHING;
    }

    /**
     * Resets the game board state.
     */
    @Override
    public void resetGame()
    {
        Arrays.fill(numbers, EMPTY_SPOT); // Reset the numbers array
        currentNumber = EMPTY_SPOT; // No current number until game starts
    }

    /**
     * Updates and displays the user's score/stats.
     */
    @Override
    public void updateScore()
    {
        final double averagePlacements;
        averagePlacements = (gamesPlayed == NOTHING) ? NOTHING :
                            (double) totalPlacements / gamesPlayed;

        final String scoreMessage;
        scoreMessage = String.format(
                "Stats for this session instance:\nGames Played: %d\nGames Won: %d\nPlacements in last game: %d",
                gamesPlayed,
                gamesWon,
                totalPlacements);
        showScoreDialog(scoreMessage);
    }

    private void showScoreDialog(final String message)
    {
        final Alert alert;
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Score Report");
        alert.setHeaderText(null); // No header
        alert.setContentText(message);
        alert.showAndWait(); // Blocks until user clicks OK
    }

    /**
     * Sets the total number of placements.
     *
     * @param totalPlacements the total placements to set
     */
    protected void setTotalPlacements(final int totalPlacements)
    {
        this.totalPlacements = totalPlacements;
    }

    /**
     * Sets the current number.
     *
     * @param currentNumber the current number to set
     */
    protected void setCurrentNumber(final int currentNumber)
    {
        this.currentNumber = currentNumber;
    }

    /**
     * Gets the current number.
     *
     * @return the current number
     */
    protected int getCurrentNumber()
    {
        return this.currentNumber;
    }

    /**
     * Retrieves the number stored at a specific index in the array.
     *
     * @param index the index to retrieve the number from
     * @return the number at the specified index
     * @throws ArrayIndexOutOfBoundsException if the index is out of bounds
     */
    protected int getNumberAtArrayIndex(final int index)
    {
        return numbers[index];
    }

    /**
     * Sets a number at a specific index in the array.
     *
     * @param index  the index where the number should be set
     * @param number the number to store at the specified index
     * @throws ArrayIndexOutOfBoundsException if the index is out of bounds
     */
    protected void setNumberAtArrayIndex(final int index, final int number)
    {
        numbers[index] = number;
    }

    /**
     * Increments the total placements by one.
     */
    protected void incrementTotalPlacements()
    {
        totalPlacements++;
    }

    /**
     * Gets the total number of placements.
     *
     * @return the total placements
     */
    protected int getTotalPlacements()
    {
        return totalPlacements;
    }

    /**
     * Increments the number of games played by one.
     */
    protected void incrementGamesPlayed()
    {
        gamesPlayed++;
    }

    /**
     * Increments the number of games won by one.
     */
    protected void incrementGamesWon()
    {
        gamesWon++;
    }

}
