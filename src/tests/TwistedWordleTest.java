import ca.bcit.comp2522.termproject.twistedwordle.TwistedWordle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for the TwistedWordle game logic.
 * Focuses on testable components: score calculation and the word loading helper method.
 *
 * @author colecampbell
 * @version 1.0
 */
public final class TwistedWordleTest
{
    @Test
    void testCalculateScorePasses()
    {
        final TwistedWordle scoreCalculator;
        scoreCalculator = new TwistedWordle();

        // Formula: 50 + (attemptsLeftBeforeGuess * 10) + max(0, timeLeft)
        assertEquals(100,
                     scoreCalculator.calculateScore(3,
                                                    20));

        assertEquals(55,
                     scoreCalculator.calculateScore(0,
                                                    5));

        assertEquals(100,
                     scoreCalculator.calculateScore(5,
                                                    0));
    }

    @Test
    void testCalculateScoreFails()
    {
        final TwistedWordle scoreCalculator;
        scoreCalculator = new TwistedWordle();

        assertThrows(IllegalArgumentException.class,
                     () -> scoreCalculator.calculateScore(5,
                                                          -10));

        assertThrows(IllegalArgumentException.class,
                     () -> scoreCalculator.calculateScore(-5,
                                                          10));

        assertThrows(IllegalArgumentException.class,
                     () -> scoreCalculator.calculateScore(-5,
                                                          -10));
    }

    @Test
    void testLoadAndProcessWordsSuccess() throws Exception
    {
        final Path wordFile;
        wordFile = Paths.get("testwords_load_success.txt");

        final List<String> wordsToWrite;
        wordsToWrite = List.of("apple", " TABLE ", "CHAIR",
                               "space", "LOWER", "TOOLONG", "SHRT",
                               " DIG1T ", " tests", "VALID", "again", "WORDS");

        Files.write(wordFile, wordsToWrite);

        final Set<String> loadedWords;
        loadedWords = TwistedWordle.loadAndProcessWords(wordFile.toString());

        // Testing that the loadAndProcessWords method returned properly
        assertNotNull(loadedWords, "Loaded words should not be null");

        final Set<String> expectedWords = Set.of(
                "APPLE", "TABLE", "CHAIR", "SPACE", "LOWER",
                "DIG1T", "TESTS", "VALID", "AGAIN", "WORDS");

        assertEquals(expectedWords, loadedWords,
                     "Should match expected transformed words");

        assertEquals(10, loadedWords.size(),
                     "Should have exactly 10 valid words");

        assertTrue(loadedWords.contains("DIG1T"),
                   "Should contain mixed case/digit word");

        assertFalse(loadedWords.contains("TOOLONG"),
                    "Should exclude too long words");

        assertFalse(loadedWords.contains("SHRT"),
                    "Should exclude too short words");
    }

    @Test
    void testLoadAndProcessWordsFileNonExist()
    {
        assertThrows(IllegalArgumentException.class,
                     () -> TwistedWordle.loadAndProcessWords(null));

        assertThrows(IllegalArgumentException.class,
                     () -> TwistedWordle.loadAndProcessWords("   "));

        assertThrows(IllegalArgumentException.class,
                     () -> TwistedWordle.loadAndProcessWords("non_existent_file.txt"));
    }
}
