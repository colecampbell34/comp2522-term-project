package ca.bcit.comp2522.termproject.twistedwordle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

/**
 * Handles the initial console-based setup for the Twisted Wordle game.
 * Prompts users for player names and word selections for the opponent.
 * Relies on WordLoader for word validation and updates static fields in TwistedWordle.
 * <p>
 * This class provides the following functionality:
 * - Orchestrating the console setup sequence.
 * - Getting player names from the console.
 * - Getting word selections from the console for each player.
 * - Validating player names and chosen word lists.
 *
 * @author colecampbell
 * @version 1.0
 */
public final class GameSetup
{

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private GameSetup()
    {}

    /**
     * Performs initial game setup through the console interface.
     * This method handles loading the word list, getting player names,
     * and allowing each player to choose words for their opponent.
     * It relies on static fields in TwistedWordle to store this setup data.
     *
     * @return true if the setup completes successfully, false if an error occurs.
     */
    public static boolean setupGameFromConsole()
    {
        final Scanner consoleScanner;
        consoleScanner = new Scanner(System.in);

        boolean success;
        success = false;

        try
        {
            // Load words using WordLoader and store in TwistedWordle's static set
            TwistedWordle.setStaticWordSet(WordLoader.loadAndProcessWords(TwistedWordle.WORD_FILE_PATH));

            System.out.println("Loaded " +
                               TwistedWordle.getStaticWordSet().size() +
                               " valid words.");

            validateStaticWordSet(TwistedWordle.getStaticWordSet());

            System.out.print("Enter Player 1 Name: ");

            TwistedWordle.setStaticPlayer1Name(consoleScanner.nextLine().trim());
            validatePlayerName(TwistedWordle.getStaticPlayer1Name());

            System.out.print("Enter Player 2 Name: ");

            TwistedWordle.setStaticPlayer2Name(consoleScanner.nextLine().trim());
            validatePlayerName(TwistedWordle.getStaticPlayer2Name());

            // Player 1 chooses words for Player 2
            System.out.println("\n--- " +
                               TwistedWordle.getStaticPlayer1Name() +
                               ", choose " +
                               TwistedWordle.TOTAL_ROUNDS +
                               " words for " +
                               TwistedWordle.getStaticPlayer2Name() +
                               " ---");

            List<String> staticWordSetToPass;

            staticWordSetToPass = getWordsFromConsole(TwistedWordle.getStaticPlayer1Name(),
                                                                   consoleScanner);
            TwistedWordle.setStaticWordsForPlayer2(staticWordSetToPass);
            validateWordsForPlayer(TwistedWordle.getStaticWordsForPlayer2());

            // Player 2 chooses words for Player 1
            System.out.println("\n--- " +
                               TwistedWordle.getStaticPlayer2Name() +
                               ", choose " +
                               TwistedWordle.TOTAL_ROUNDS +
                               " words for " +
                               TwistedWordle.getStaticPlayer1Name() +
                               " ---");

            staticWordSetToPass = getWordsFromConsole(TwistedWordle.getStaticPlayer2Name(),
                                                      consoleScanner);
            TwistedWordle.setStaticWordsForPlayer1(staticWordSetToPass);
            validateWordsForPlayer(TwistedWordle.getStaticWordsForPlayer1());

            success = true;

        } catch (final Exception e)
        {
            e.printStackTrace();
        }

        return success;
    }

    /*
     * Validates that the static word set is not null and not empty.
     * Throws an IllegalArgumentException if the set is invalid.
     */
    private static void validateStaticWordSet(final Set<String> wordSet)
    {
        if (wordSet == null ||
            wordSet.isEmpty())
        {
            throw new IllegalArgumentException("Word set cannot be null or empty");
        }
    }

    /*
     * Validates that the player name is neither null nor blank.
     * Throws an IllegalArgumentException if the name is invalid.
     */
    private static void validatePlayerName(final String name)
    {
        if (name == null ||
            name.isBlank())
        {
            throw new IllegalArgumentException("Player name should not be null or blank");
        }
    }

    /*
     * Validates that the list of words chosen for a player is not null
     * and contains the correct number of words (equal to TwistedWordle.TOTAL_ROUNDS).
     * Throws an IllegalStateException if the list is invalid.
     */
    private static void validateWordsForPlayer(final List<String> words)
    {
        // Access TOTAL_ROUNDS from TwistedWordle
        if (words == null ||
            words.size() != TwistedWordle.TOTAL_ROUNDS)
        {
            throw new IllegalStateException("Word list for player is invalid (null or wrong size)");
        }
    }

    /*
     * Prompts the specified player via the console to enter a fixed number (TwistedWordle.TOTAL_ROUNDS)
     * of valid words for their opponent. Validates each entered word for length
     * and existence in the loaded staticWordSet using WordLoader methods.
     */
    private static List<String> getWordsFromConsole(final String playerName,
                                                    final Scanner scanner)
    {
        validatePlayerName(playerName);
        Objects.requireNonNull(scanner, "Scanner cannot be null for console input");
        validateStaticWordSet(TwistedWordle.getStaticWordSet());

        final List<String> chosenWords;
        chosenWords = new ArrayList<>();

        System.out.println("Words must be " +
                           TwistedWordle.WORD_LENGTH +
                           " letters long and present in the loaded word list.");

        // Loop until the required number of valid words are entered
        for (int i = 0; i < TwistedWordle.TOTAL_ROUNDS; i++)
        {
            String  enteredWord;
            boolean validWord;

            do
            {
                System.out.printf("  Enter word %d of %d: ",
                                  i + TwistedWordle.OFFSET, TwistedWordle.TOTAL_ROUNDS);

                enteredWord = scanner.nextLine().trim().toUpperCase();

                validWord = WordLoader.validateWordLength(enteredWord) &&
                            WordLoader.validateWordInWordList(enteredWord);

                // Provide feedback if the word is invalid
                if (!validWord)
                {
                    System.out.println("    Please try again.");
                }

            } while (!validWord);

            chosenWords.add(enteredWord);
        }

        System.out.println("  " +
                           playerName +
                           " finished choosing words.");

        return chosenWords;
    }
}
