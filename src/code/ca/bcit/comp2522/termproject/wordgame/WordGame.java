package ca.bcit.comp2522.termproject.wordgame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Word Game that tests players on country capitals and facts.
 *
 * @author colecampbell
 * @version 1.0
 */
public final class WordGame
{
    private static final int NOTHING           = 0;
    private static final int GUESSES_PER_ROUND = 10;
    private static final int LAST_QUESTION     = 9;
    private static final int RANDOM_SELECTOR   = 3;
    private static final int GIVE_CAPITAL      = 0;
    private static final int GIVE_COUNTRY      = 1;
    private static final int GIVE_FACT         = 2;
    private static final int RANDOM_INDEX      = 3;
    private static final int CHAR_OFFSET       = 2;
    private static final int FIRST_HALF        = 0;
    private static final int SECOND_HALF       = 1;
    private static final int NUMBER_OF_FACTS   = 3;
    private static final int FIRST_TRY         = 0;
    private static final int SECOND_TRY        = 1;
    private static final int MAX_GUESSES       = 2;

    private static final String SCORE_FILE    = "score.txt";
    private static final String CORRECT       = "CORRECT!";
    private static final String INCORRECT     = "INCORRECT!";
    private static final String SORRY_MESSAGE = "Sorry, the correct answer was ";
    private static final char   FIRST_FILE    = 'a';
    private static final char   EMPTY_FILE    = 'w';
    private static final char   LAST_FILE     = 'z';

    private static int gamesPlayed;
    private static int correctFirstAttempts;
    private static int correctSecondAttempts;
    private static int incorrectAttempts;

    /**
     * Initializes variables for a new round and commences play.
     *
     * @throws IOException if a I/O error occurs
     */
    public static void play() throws IOException
    {
        gamesPlayed           = NOTHING;
        correctFirstAttempts  = NOTHING;
        correctSecondAttempts = NOTHING;
        incorrectAttempts     = NOTHING;

        loadWorldData();
        playRound();

        System.out.println("\n==========Returning To Main Menu==========");
    }

    /*
     * Reads from the country files to populate the World HashMap.
     */
    private static void loadWorldData()
    {
        char fileName = FIRST_FILE;

        while (fileName <= LAST_FILE)
        {
            if (fileName == EMPTY_FILE)
            {
                // skips over the 2 non-existent files
                fileName += CHAR_OFFSET;
            }

            try
            {
                final File    file;
                final Scanner fileScanner;

                file        = new File("src/resources/" +
                                       fileName +
                                       ".txt");
                fileScanner = new Scanner(file);

                while (fileScanner.hasNext())
                {
                    fileScanner.nextLine(); // Skip irrelevant line
                    String[] splitter = fileScanner.nextLine().split(":");

                    String   name            = splitter[FIRST_HALF].trim();
                    String   capitalCityName = splitter[SECOND_HALF].trim();
                    String[] facts           = new String[NUMBER_OF_FACTS];

                    for (int i = 0; i < NUMBER_OF_FACTS; i++)
                    {
                        facts[i] = fileScanner.nextLine();
                    }

                    World.worldMap.put(name, new Country(name, capitalCityName, facts));
                }
                fileScanner.close();
            } catch (final FileNotFoundException e)
            {
                System.err.println("Error opening file: " +
                                   fileName);
            } catch (final Exception e)
            {
                System.err.println("Error processing file: " +
                                   fileName + " - " +
                                   e.getMessage());
            }

            fileName++;
        }
    }

    /*
     * Plays one round of the game, posing the user with ten random questions.
     */
    private static void playRound() throws IOException
    {
        final Scanner      input;
        final Random       random;
        final List<String> keys;
        String             userChoice;

        input  = new Scanner(System.in);
        random = new Random();
        keys   = new ArrayList<>(World.worldMap.keySet());

        System.out.println("\n=====New Round=====\n");

        for (int i = 0; i < GUESSES_PER_ROUND; i++)
        {
            final String randomKey;
            final int    randomNum;
            final int    guesses;

            randomKey = keys.get(random.nextInt(keys.size()));
            randomNum = random.nextInt(RANDOM_SELECTOR);

            guesses = switch (randomNum)
            {
                case GIVE_CAPITAL -> giveCapital(randomKey);
                case GIVE_COUNTRY -> giveCountry(randomKey);
                case GIVE_FACT -> giveFact(randomKey);
                default -> throw new IllegalArgumentException("Unexpected value: " +
                                                              randomNum);
            };

            if (i < LAST_QUESTION)
            {
                System.out.println("\n===== NEXT QUESTION =====\n");
            }

            if (guesses == FIRST_TRY)
            {
                correctFirstAttempts++;
            }
            else if (guesses == SECOND_TRY)
            {
                correctSecondAttempts++;
            }
            else
            {
                incorrectAttempts++;
            }
        }

        gamesPlayed++;

        System.out.println("\n============ ROUND OVER ============");
        System.out.println("\nYes to play again, No to go back to the main menu");
        userChoice = input.next().toUpperCase();

        while (!userChoice.equals("YES") &&
               !userChoice.equals("NO"))
        {
            System.out.println("Invalid choice, please try again");
            userChoice = input.next().toUpperCase();
        }

        if (userChoice.equals("YES"))
        {
            playRound();
        }
        else
        {
            final Score roundScore;
            roundScore = new Score(LocalDateTime.now(),
                                   gamesPlayed,
                                   correctFirstAttempts,
                                   correctSecondAttempts,
                                   incorrectAttempts);

            printReport(roundScore);
            checkForHighScore(roundScore);
            Score.appendScoreToFile(roundScore, SCORE_FILE);
        }
    }

    /*
     * Gives the user a random capital city for them to guess the country.
     */
    private static int giveCapital(final String key)
    {
        validateKey(key);

        final Scanner input;
        int           guesses;

        input   = new Scanner(System.in);
        guesses = NOTHING;

        while (guesses < MAX_GUESSES)
        {
            System.out.println("What country has the capital city of " +
                               World.worldMap.get(key).getCapitalCityName() + "?");

            final String guess;
            guess = input.nextLine();

            if (guess.equalsIgnoreCase(World.worldMap.get(key).getName()))
            {
                System.out.println(CORRECT);
                return guesses;
            }

            System.out.println(INCORRECT);
            guesses++;
        }

        System.out.println(SORRY_MESSAGE +
                           World.worldMap.get(key).getName());

        return guesses;
    }

    /*
     * Gives the user a random country for them to guess the capital city.
     */
    private static int giveCountry(final String key)
    {
        validateKey(key);

        final Scanner input;
        int           guesses;

        input   = new Scanner(System.in);
        guesses = NOTHING;

        while (guesses < MAX_GUESSES)
        {
            System.out.println("What is the capital city of " +
                               World.worldMap.get(key).getName() + "?");

            final String guess;
            guess = input.nextLine();

            if (guess.equalsIgnoreCase(World.worldMap.get(key).getCapitalCityName()))
            {
                System.out.println(CORRECT);
                return guesses;
            }

            System.out.println(INCORRECT);
            guesses++;
        }

        System.out.println(SORRY_MESSAGE +
                           World.worldMap.get(key).getCapitalCityName());

        return guesses;
    }

    /*
     * Gives the user a random fact for them to guess the country.
     */
    private static int giveFact(final String key)
    {
        validateKey(key);

        final Random  random;
        final Scanner input;
        int           guesses;
        final int     randomFactIndex;

        random          = new Random();
        input           = new Scanner(System.in);
        guesses         = NOTHING;
        randomFactIndex = random.nextInt(RANDOM_INDEX);

        while (guesses < MAX_GUESSES)
        {
            System.out.println("What country has this fact: " +
                               World.worldMap.get(key).getFacts(randomFactIndex) + "?");

            final String guess;
            guess = input.nextLine();

            if (guess.equalsIgnoreCase(World.worldMap.get(key).getName()))
            {
                System.out.println(CORRECT);
                return guesses;
            }

            System.out.println(INCORRECT);
            guesses++;
        }

        System.out.println(SORRY_MESSAGE +
                           World.worldMap.get(key).getName());

        return guesses;
    }

    /*
     * Calls the toString method from the Score class to display the report of the round.
     */
    private static void printReport(final Score score)
    {
        System.out.println("======================================\n");
        System.out.println(score);
        System.out.println("======================================");
    }

    /*
     * Checks if the user has a new high score,
     * and prints the last high score if they do.
     */
    private static void checkForHighScore(final Score latestScore)
    throws IOException
    {
        final List<Score> scores;
        Score             highScore;

        scores    = Score.readScoresFromFile(SCORE_FILE);
        highScore = null;

        for (final Score score : scores)
        {
            if (highScore == null ||
                highScore.getAvgScore() <= score.getAvgScore())
            {
                highScore = score;
            }
        }

        if (highScore == null ||
            latestScore.getAvgScore() > highScore.getAvgScore())
        {
            System.out.printf("CONGRATULATIONS! You are the new high score " +
                              "with an average of %.2f ppg!\n",
                              latestScore.getAvgScore());

            if (highScore != null)
            {
                System.out.printf("The previous high score was %.2f ppg on %s",
                                  highScore.getAvgScore(), highScore.getDate());
            }
            else
            {
                System.out.println("There was no previous high score.");
            }
        }
    }

    /*
     * Validates a key for the world map.
     */
    private static void validateKey(final String key)
    {
        if (key == null ||
            key.isBlank())
        {
            throw new IllegalArgumentException("Key cannot be null or blank");
        }
    }
}
