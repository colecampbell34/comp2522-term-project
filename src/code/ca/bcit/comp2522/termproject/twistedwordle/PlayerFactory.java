package ca.bcit.comp2522.termproject.twistedwordle;

/**
 * A factory to construct a Player.
 *
 * @author colecampbell
 * @version 1.0
 */
public class PlayerFactory
{
    public static Player createPlayer(final String name)
    {
        return new Player(name);
    }
}
