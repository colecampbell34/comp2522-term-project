package ca.bcit.comp2522.termproject.numbergame;

import javafx.scene.control.Alert;

import java.util.Arrays;

/**
 * Abstract class for a board game.
 */
public abstract class AbstractGame implements GameInterface
{
    private static final int MAX_PLACEMENTS = 20;
    private static final int NOTHING = 0;
    private static final int EMPTY_SPOT = -1;

    protected int[] numbers;
    protected int   currentNumber;
    protected int   gamesPlayed;
    protected int   gamesWon;
    protected int   totalPlacements;

    /**
     * Constructs an abstract game object.
     */
    public AbstractGame()
    {
        numbers = new int[MAX_PLACEMENTS];
        resetGame(); // Initialize the game state
        gamesPlayed     = NOTHING;
        gamesWon        = NOTHING;
        totalPlacements = NOTHING;
    }

    /**
     * Resets the game board
     */
    @Override
    public void resetGame()
    {
        Arrays.fill(numbers, EMPTY_SPOT); // Reset the numbers array
        currentNumber = EMPTY_SPOT;
    }

    /**
     * Updates the users score.
     */
    @Override
    public void updateScore()
    {
        double averagePlacements = (gamesPlayed == NOTHING) ? NOTHING :
                                   (double) totalPlacements / gamesPlayed;
        final String scoreMessage;

        if (gamesWon == NOTHING)
        {
            scoreMessage = String.format(
                    "You lost %d out of %d games, with %d successful placements, an average of %.2f per game.",
                    gamesPlayed, gamesPlayed, totalPlacements, averagePlacements
                                        );
        }
        else if (gamesPlayed == gamesWon)
        {
            scoreMessage = String.format(
                    "You won %d out of %d games, with %d successful placements, an average of %.2f per game.",
                    gamesWon, gamesPlayed, totalPlacements, averagePlacements
                                        );
        }
        else
        {
            scoreMessage = String.format(
                    "You won %d out of %d games and lost %d out of %d games, with %d successful placements, an average of %.2f per game.",
                    gamesWon, gamesPlayed, gamesPlayed - gamesWon, gamesPlayed, totalPlacements, averagePlacements
                                        );
        }
        System.out.println(scoreMessage); // Print to console for debugging
        showScoreDialog(scoreMessage);
    }

    private void showScoreDialog(final String message)
    {
        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Score Report");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
