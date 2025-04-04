package ca.bcit.comp2522.termproject.twistedwordle;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
    private static final Path TEST_FILE_PATH = Paths.get("testwords_load_success.txt");

    private TwistedWordle scoreCalculator;

    @BeforeEach
    void setUp()
    {
        scoreCalculator = new TwistedWordle();
    }

    @Test
    void testCalculateScorePasses()
    {
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
        final List<String> wordsToWrite;
        wordsToWrite = List.of(
                "apple", " TABLE ", "CHAIR",
                "space", "LOWER", "TOOLONG", "SHRT",
                " DIG1T ", " tests", "VALID", "again", "WORDS");

        Files.write(TEST_FILE_PATH, wordsToWrite);

        final Set<String> loadedWords = TwistedWordle.loadAndProcessWords(TEST_FILE_PATH.toString());

        assertNotNull(loadedWords, "Loaded words should not be null");

        final Set<String> expectedWords = Set.of(
                "APPLE", "TABLE", "CHAIR", "SPACE", "LOWER",
                "DIG1T", "TESTS", "VALID", "AGAIN", "WORDS");

        assertEquals(expectedWords, loadedWords, "Should match expected transformed words");
        assertEquals(10, loadedWords.size(), "Should have exactly 10 valid words");
        assertTrue(loadedWords.contains("DIG1T"), "Should contain mixed case/digit word");
        assertFalse(loadedWords.contains("TOOLONG"), "Should exclude too long words");
        assertFalse(loadedWords.contains("SHRT"), "Should exclude too short words");
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

    @AfterEach
    void tearDown() throws Exception
    {
        scoreCalculator = null;

        if (Files.exists(TEST_FILE_PATH))
        {
            Files.delete(TEST_FILE_PATH);
        }
    }
}
