package ca.bcit.comp2522.termproject.numbergame;

/**
 * A game interface for the number game.
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
