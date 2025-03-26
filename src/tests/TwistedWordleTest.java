import ca.bcit.comp2522.termproject.twistedwordle.TwistedWordle;
import ca.bcit.comp2522.termproject.twistedwordle.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;

class TwistedWordleTest
{
    private TwistedWordle game;

    @BeforeEach
    void setUp()
    {
        game = new TwistedWordle();
    }

    @Test
    void testCalculateScore()
    {
        assertEquals(80, game.calculateScore(3, 20)); // 50 + (3 * 10) + 20 = 80
        assertEquals(60, game.calculateScore(1, 0));  // 50 + (1 * 10) + 0 = 60
        assertEquals(110, game.calculateScore(5, 10)); // 50 + (5 * 10) + 10 = 110
    }

    @Test
    void testSwitchPlayer()
    {
        game.setPlayers(new Player("Alice"), new Player("Bob"));
        game.setCurrentPlayer(game.getPlayer1());

        game.switchPlayer();
        assertEquals("Bob", game.getCurrentPlayer().getName());

        game.switchPlayer();
        assertEquals("Alice", game.getCurrentPlayer().getName());
    }

    @Test
    void testLoadWordsAsync() throws InterruptedException
    {
        game.loadWordsAsync("src/resources/words.txt");

        // Wait for async operation to complete
        Thread.sleep(2000); // Allow time for word loading

        assertNotNull(game.getWordBatch());
        assertFalse(game.getWordBatch().isEmpty());
        assertNotNull(game.getTargetWord());
    }
}
