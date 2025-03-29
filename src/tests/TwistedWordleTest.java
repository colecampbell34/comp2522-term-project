import ca.bcit.comp2522.termproject.twistedwordle.TwistedWordle; // Adjust package if needed
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Method; // Import reflection
import java.nio.file.Files;
import java.nio.file.Path;
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
    void testCalculateScore()
    {
        final TwistedWordle scoreCalculator;
        scoreCalculator = new TwistedWordle();

        // Formula: 50 + (attemptsLeftBeforeGuess * 10) + max(0, timeLeft)
        assertEquals(100, scoreCalculator.calculateScore(3, 20), "Score: 3 attempts left, 20s");
        assertEquals(55, scoreCalculator.calculateScore(0, 5), "Score: 0 attempts left, 5s");
        assertEquals(100, scoreCalculator.calculateScore(5, 0), "Score: 5 attempts left, 0s");
        assertEquals(60, scoreCalculator.calculateScore(1, -10), "Score: 1 attempt left, negative time");
        assertEquals(140, scoreCalculator.calculateScore(6, 30), "Score: 6 attempts left, 30s");
    }

    @Test
    void testLoadAndProcessWordsSuccess(@TempDir final Path tempDir) throws Exception
    {
        // Setup: Create a temporary word file
        final Path wordFile;
        wordFile = tempDir.resolve("testwords_load_success.txt");

        final List<String> wordsToWrite = List.of(
                "apple", // Lowercase
                " TABLE ", // Needs trim
                "CHAIR", // Valid
                "space", // Lowercase
                "LOWER", // Valid
                "TOOLONG", // Invalid length
                "SHRT", // Invalid length
                " DIG1T ", // Needs trim, Length 5 (digits allowed by filter)
                " tests", // Needs trim, Length 5
                "VALID", // Valid
                "again", // Lowercase
                "WORDS"); // Valid
        Files.write(wordFile, wordsToWrite);
        System.out.println("Test word file created at: " + wordFile.toAbsolutePath());

        // Action: Call the static helper method using reflection (since it's private)
        final Method method;
        method = TwistedWordle.class.getDeclaredMethod("loadAndProcessWords", String.class);
        method.setAccessible(true); // Make the private method accessible
        @SuppressWarnings("unchecked")

        final Set<String> loadedWords = (Set<String>) method.invoke(null, wordFile.toString());

        // Verification: Check the returned set
        assertNotNull(loadedWords, "Loaded word set should not be null.");

        // Verify content based on filtering logic (length 5, trimmed, uppercase)
        final Set<String> expectedWords = Set.of(
                "APPLE", "TABLE", "CHAIR", "SPACE", "LOWER",
                "DIG1T", "TESTS", "VALID", "AGAIN", "WORDS");
        final int expectedCount = 10;

        assertEquals(expectedCount, loadedWords.size(),
                     "Should contain exactly " + expectedCount + " valid words.");
        assertEquals(expectedWords, loadedWords, "The loaded set should match the expected set.");

        // Optional: Individual checks if needed
        assertTrue(loadedWords.contains("APPLE"));
        assertTrue(loadedWords.contains("TABLE"));
        assertFalse(loadedWords.contains("TOOLONG"));
        assertFalse(loadedWords.contains("SHRT"));
    }


    /**
     * Tests the static loadAndProcessWords helper method when the file does not exist.
     * Verifies that an IOException is thrown.
     *
     * @param tempDir A temporary directory for test files.
     */
    @Test
    void testLoadAndProcessWordsFileNotExist(@TempDir final Path tempDir) throws Exception
    {
        // Setup: Path to a non-existent file
        final Path nonExistentFile;
        nonExistentFile = tempDir.resolve("non_existent_words.txt");
        System.out.println("Testing load failure with non-existent file: " + nonExistentFile.toAbsolutePath());

        // Action & Verification: Expect IOException when calling the helper method
        final Method method;
        method = TwistedWordle.class.getDeclaredMethod("loadAndProcessWords", String.class);
        method.setAccessible(true);

        // Use assertThrows to check for the specific exception from the *invocation*
        final Exception exception;
        exception = assertThrows(Exception.class, () ->
        {
            method.invoke(null, nonExistentFile.toString());
        }, "Calling loadAndProcessWords with non-existent file should throw");

        // Check that the cause of the InvocationTargetException is the expected IOException
        final Throwable cause;
        cause = exception.getCause();
        assertNotNull(cause, "Invocation should have a cause");
        assertInstanceOf(IOException.class, cause, "Cause should be IOException");
        assertTrue(cause.getMessage().contains("Word file not found"), "IOException message should indicate file not found.");

        System.out.println("Correctly caught expected exception: " + cause.getMessage());
    }
}
