package ca.bcit.comp2522.termproject.numbergame;

/**
 * An interface for a game.
 * Includes methods for
 * starting a game,
 * resetting a game,
 * and updating a score.
 *
 * @author colecampbell
 * @version 1.0
 */
public interface GameInterface
{
    /**
     *  Initializes and starts the game logic
     */
    void startGame();

    /**
     * Resets the game state to its initial conditions.
     */
    void resetGame();

    /**
     * Updates the player's score based on game events.
     */
    void updateScore();
}
