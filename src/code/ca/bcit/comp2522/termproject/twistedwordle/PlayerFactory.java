package ca.bcit.comp2522.termproject.twistedwordle;

/**
 * A factory to construct a Player object.
 *
 * @author colecampbell
 * @version 1.0
 */
public final class PlayerFactory
{
    /**
     * Constructs a player within the factory.
     *
     * @param name the name of the player
     * @return the new player object
     */
    public static Player createPlayer(final String name)
    {
        validatePlayerName(name);

        return new Player(name);
    }

    /*
     * Validates the player name.
     */
    private static void validatePlayerName(final String name)
    {
        if (name == null ||
            name.isBlank())
        {
            throw new IllegalArgumentException("Invalid player name");
        }
    }
}
