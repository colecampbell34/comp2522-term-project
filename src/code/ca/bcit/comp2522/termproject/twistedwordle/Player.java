package ca.bcit.comp2522.termproject.twistedwordle;

/**
 * Represents a Player.
 *
 * @author colecampbell
 * @version 1.0
 */
public class Player
{
    private final String name;
    private       int    score;

    public Player(final String name)
    {
        this.name  = name;
        this.score = 0;
    }

    public String getName()
    {
        return name;
    }

    public int getScore()
    {
        return score;
    }

    public void addScore(final int points)
    {
        score += points;
    }
}
