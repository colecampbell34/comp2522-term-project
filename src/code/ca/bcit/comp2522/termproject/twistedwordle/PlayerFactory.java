package ca.bcit.comp2522.termproject.twistedwordle;

/**
 * A factory to construct a Player.
 *
 * @author colecampbell
 * @version 1.0
 */
public final class PlayerFactory
{
    /**
     * Constructs a player within the factory.
     * @param name
     * @return
     */
    public static Player createPlayer(final String name)
    {
        return new Player(name);
    }
}
