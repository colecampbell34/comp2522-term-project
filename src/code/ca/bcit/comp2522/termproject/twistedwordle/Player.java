package ca.bcit.comp2522.termproject.twistedwordle;

/**
 * Represents a player in the game. A player has a name and a score. The score starts at NOTHING (0)
 * and can be incremented as the player progresses through the game.
 *
 * This class provides the following functionality:
 * - Validates player name during creation to ensure it is neither null nor blank.
 * - Tracks and retrieves the player's score.
 * - Allows for adding points to the player's score, with validation to prevent negative point additions.
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
     * Constructs a new Player with the provided name.
     * The player's score is initialized to NOTHING (0).
     *
     * This constructor validates the player name to ensure that it is neither null nor blank.
     * If the name is invalid, an IllegalArgumentException is thrown.
     *
     * @param name The name of the player. Must be a non-null, non-blank string.
     * @throws IllegalArgumentException if the name is null or blank
     */
    public Player(final String name)
    {
        validateName(name);

        this.name  = name;
        this.score = NOTHING;
    }

    /*
     * Validates that the player name is neither null nor blank.
     * Throws an IllegalArgumentException if the name is invalid.
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
     * @return the player's name as a string
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the current score of the player.
     *
     * @return the player's score as an integer
     */
    public int getScore()
    {
        return score;
    }

    /**
     * Adds the specified number of points to the player's score.
     *
     * This method validates the points to ensure they are non-negative before adding them to the score.
     * If the points are negative, an IllegalArgumentException is thrown.
     *
     * @param points the number of points to add to the player's score
     * @throws IllegalArgumentException if the points are negative
     */
    public void addScore(final int points)
    {
        validatePoints(points);

        score += points;
    }

    /*
     * Validates that the number of points to be added is not negative.
     * Throws an IllegalArgumentException if the points are negative.
     */
    private static void validatePoints(final int points)
    {
        if (points < NOTHING)
        {
            throw new IllegalArgumentException("Points cannot be negative");
        }
    }
}
