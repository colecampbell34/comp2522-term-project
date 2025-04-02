package ca.bcit.comp2522.termproject.twistedwordle;

/**
 * Represents a Player.
 *
 * @author colecampbell
 * @version 1.0
 */
public final class Player
{
    private static final int NOTHING = 0;

    private final String name;
    private       int    score;

    /**
     * Constructs a player.
     *
     * @param name the name of the player
     */
    public Player(final String name)
    {
        validateName(name);

        this.name  = name;
        this.score = NOTHING;
    }

    /*
     * Validates the player name.
     */
    private static void validateName(final String name)
    {
        if (name == null ||
            name.isBlank())
        {
            throw new IllegalArgumentException("Invalid name");
        }
    }

    /**
     * Gets the name of the player.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the score for the player.
     *
     * @return the score
     */
    public int getScore()
    {
        return score;
    }

    /**
     * Adds to the players score
     *
     * @param points the points to add
     */
    public void addScore(final int points)
    {
        validatePoints(points);

        score += points;
    }

    /*
     * Validates the points to add.
     */
    private static void validatePoints(final int points)
    {
        if (points < NOTHING)
        {
            throw new IllegalArgumentException("Points cannot be negative");
        }
    }
}
