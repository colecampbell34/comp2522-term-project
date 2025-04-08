package ca.bcit.comp2522.termproject.twistedwordle;

/**
 * An interface for games that are scorable.
 *
 * @author colecampbell
 * @version 1.0
 */
public interface Scorable
{
    /*
     * Calculates the score
     */
    int calculateScore(int attemptsLeft, int timeLeft);
}
