import ca.bcit.comp2522.termproject.twistedwordle.TwistedWordle;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TwistedWordleTest {

    @Test
    void testCalculateScore() {
        TwistedWordle game = new TwistedWordle();
        int score = game.calculateScore(3, 50); // 3 attempts left, 50 seconds left
        assertEquals(50 + (3 * 10) + 50, score); // Expected: base 50 + attempts bonus + time bonus
    }
}
