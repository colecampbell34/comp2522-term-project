package ca.bcit.comp2522.termproject.twistedwordle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles loading and validation of the word list for the Twisted Wordle game.
 * Reads words from a file, filters them based on length, and provides validation checks.
 * <p>
 * This class provides the following functionality:
 * - Loading words from a specified file path.
 * - Filtering words to ensure they meet the required length.
 * - Validating file names and existence.
 * - Validating if a word has the correct length.
 * - Validating if a word exists within the loaded word set.
 *
 * @author colecampbell
 * @version 1.0
 */
public final class WordLoader
{
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private WordLoader()
    {}

    /**
     * Reads words from the specified file, filters them based on length,
     * converts them to uppercase, and returns them as a Set.
     * This method performs the core logic for loading and validating words from the source file.
     *
     * @param filename The path to the file containing words. Must be non-null and non-blank.
     * @return A Set containing valid, uppercase words of the required length (TwistedWordle.WORD_LENGTH).
     * @throws IOException              if an I/O error occurs reading from the file.
     * @throws IllegalArgumentException if the filename is invalid or the file does not exist.
     */
    public static Set<String> loadAndProcessWords(final String filename)
    throws IOException
    {
        validateFileName(filename);

        final Path filePath;
        filePath = Paths.get(filename);

        validateFileExistence(filePath);

        // Read all lines, trim whitespace, filter by length, convert to uppercase, collect into a Set.
        // Access WORD_LENGTH from TwistedWordle
        return Files.readAllLines(filePath)
                    .stream()
                    .map(String::trim)
                    .filter(word -> word.length() == TwistedWordle.WORD_LENGTH)
                    .map(String::toUpperCase)
                    .collect(Collectors.toSet());
    }

    /*
     * Validates that the provided file name string is neither null nor blank.
     * Throws an IllegalArgumentException if the file name is invalid.
     */
    private static void validateFileName(final String fileName)
    {
        if (fileName == null ||
            fileName.isBlank())
        {
            throw new IllegalArgumentException("Invalid file name provided (null or blank)");
        }
    }

    /*
     * Validates that the file specified by the Path object exists.
     * Throws an IllegalArgumentException if the file does not exist.
     */
    private static void validateFileExistence(final Path filePath)
    {
        Objects.requireNonNull(filePath, "File path cannot be null"); // Added null check
        if (!Files.exists(filePath))
        {
            // Provide a more informative error message including the path
            throw new IllegalArgumentException("Word file not found at path: " + filePath.toAbsolutePath());
        }
    }

    /*
     * Validates if the entered word has the correct length (TwistedWordle.WORD_LENGTH).
     * Prints an error message to the console if the length is incorrect.
     * (Used during console setup)
     */
    static boolean validateWordLength(final String word)
    {
        // Access WORD_LENGTH from TwistedWordle
        if (word == null ||
            word.length() != TwistedWordle.WORD_LENGTH)
        {
            System.out.println("    ERROR: Word must be exactly " +
                               TwistedWordle.WORD_LENGTH +
                               " letters long.");
            return false;
        }
        return true;
    }

    /*
     * Validates if the entered word exists in the static set of allowed words (TwistedWordle.staticWordSet).
     * Prints an error message to the console if the word is not found.
     * Assumes TwistedWordle.staticWordSet has been previously validated and is not null.
     * (Used during console setup)
     */
    static boolean validateWordInWordList(final String word)
    {
        // Check for null defensively, although upstream validation might cover this.
        // Access staticWordSet from TwistedWordle
        if (word == null ||
            !TwistedWordle.getStaticWordSet().contains(word))
        {
            System.out.println("    ERROR: '" +
                               word +
                               "' is not in the allowed word list.");
            return false;
        }
        return true;
    }
}
