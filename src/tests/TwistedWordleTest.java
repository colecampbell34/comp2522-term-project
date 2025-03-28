import ca.bcit.comp2522.termproject.twistedwordle.TwistedWordle;
import ca.bcit.comp2522.termproject.twistedwordle.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

import javafx.embed.swing.JFXPanel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for the TwistedWordle game logic (version extending Application).
 * Adheres to specific coding style. testSwitchPlayer removed due to private field access.
 *
 * @author colecampbell
 * @version 1.0
 */
public final class TwistedWordleTest
{

    private TwistedWordle game;

    // Initializes the JavaFX Toolkit required for Platform.runLater etc.
    // Needs to run once before any JavaFX related test.
    static
    {
        try
        {
            final JFXPanel panel = new JFXPanel(); // Add final
            System.out.println("JavaFX Toolkit Initialized for Testing.");
        } catch (final Exception e)
        {
            System.err.println("Failed to initialize JavaFX Toolkit for testing: " + e.getMessage());
            fail("JavaFX Toolkit could not be initialized.", e);
        }
    }

    /**
     * Sets up the objects before each test.
     */
    @BeforeEach
    void setUp()
    {
        game = new TwistedWordle();
    }

    /**
     * Tests the score calculation logic based on attempts left and time.
     */
    @Test
    void testCalculateScore()
    {
        // Formula from source: 50 + (attemptsLeft * 10) + timeLeft
        final int score1 = game.calculateScore(3, 20); // 100
        assertEquals(100, score1, "Score with 3 attempts left, 20s");

        final int score2 = game.calculateScore(0, 5); // 55
        assertEquals(55, score2, "Score with 0 attempts left, 5s");

        final int score3 = game.calculateScore(5, 0); // 100
        assertEquals(100, score3, "Score with 5 attempts left, 0s");

        final int score4 = game.calculateScore(1, -10); // 50
        assertEquals(50, score4, "Score with negative time");
    }

    /**
     * Tests the asynchronous loading of words from a file, verifying successful
     * loading and correct filtering based only on word length.
     *
     * @param tempDir A temporary directory provided by JUnit for test files.
     * @throws InterruptedException if the waiting thread is interrupted.
     * @throws IOException          if file I/O fails during test setup.
     */
    @Test
    void testLoadWordsAsyncSuccess(@TempDir final Path tempDir) throws InterruptedException,
                                                                       IOException
    {
        // Setup: Create a temporary word file
        final Path wordFile;
        wordFile = tempDir.resolve("testwords.txt");
        final List<String> wordsToWrite;
        wordsToWrite = List.of(
                "APPLE", "TABLE", "CHAIR", "SPACE", "LOWER", "TOOLONG", "SHRT", "DIG1T", "tests", "  BLA  ");
        Files.write(wordFile, wordsToWrite);
        System.out.println("Test word file created at: " + wordFile.toAbsolutePath());

        final CountDownLatch latch;
        latch = new CountDownLatch(1);

        // Action: Call loadWordsAsync with the Runnable callback
        game.loadWordsAsync(wordFile.toString(), latch::countDown);

        // Verification: Wait for the async operation
        final boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertTrue(completed, "Asynchronous word loading timed out.");

        // Check results after onComplete has run
        final List<String> loadedWords;
        loadedWords = game.getWordBatch();
        assertNotNull(loadedWords, "Word batch should not be null after successful load.");
        assertFalse(loadedWords.isEmpty(), "Word batch should not be empty after successful load.");

        // Verify content based on simple length == 5 filter from the source code
        final int expectedWordCount = 7; // APPLE, TABLE, CHAIR, SPACE, LOWER, DIG1T, tests
        assertEquals(expectedWordCount, loadedWords.size(), "Should contain exactly " + expectedWordCount + " words of length 5.");
        assertTrue(loadedWords.contains("APPLE"), "Should contain APPLE");
        assertTrue(loadedWords.contains("TABLE"), "Should contain TABLE");
        assertTrue(loadedWords.contains("CHAIR"), "Should contain CHAIR");
        assertTrue(loadedWords.contains("SPACE"), "Should contain SPACE");
        assertTrue(loadedWords.contains("LOWER"), "Should contain LOWER");
        assertTrue(loadedWords.contains("DIG1T"), "Should contain DIG1T (length 5)");
        assertTrue(loadedWords.contains("tests"), "Should contain tests");

        assertFalse(loadedWords.contains("TOOLONG"), "Should filter out TOOLONG.");
        assertFalse(loadedWords.contains("SHRT"), "Should filter out SHRT.");
        assertFalse(loadedWords.contains("  BLA  "), "Should filter out '  BLA  '.");
    }

    /**
     * Tests the asynchronous loading of words when the specified file does not exist.
     * Verifies that the completion callback is still called but the word list remains null.
     *
     * @param tempDir A temporary directory provided by JUnit for test files.
     * @throws InterruptedException if the waiting thread is interrupted.
     */
    @Test
    void testLoadWordsAsyncFailure(@TempDir final Path tempDir) throws InterruptedException
    {
        // Setup: Use a non-existent file path
        final Path nonExistentFile;
        nonExistentFile = tempDir.resolve("non_existent_words.txt");

        final CountDownLatch latch;
        latch = new CountDownLatch(1);

        // Action: Call loadWordsAsync with the non-existent file
        game.loadWordsAsync(nonExistentFile.toString(), latch::countDown);

        // Verification: Wait for the onComplete callback
        final boolean completed;
        completed = latch.await(5, TimeUnit.SECONDS);
        assertTrue(completed, "Asynchronous word loading (failure case) timed out.");

        // Check results after onComplete has run
        final List<String> loadedWords;
        loadedWords = game.getWordBatch();
        assertNull(loadedWords, "Word batch should be null after a failed load.");
    }
}
