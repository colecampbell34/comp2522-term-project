package ca.bcit.comp2522.termproject.wordgame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Description...
 *
 * @author colecampbell
 * @version 1.0
 */
public class WordGame
{
    private static final int NOTHING              = 0;
    private static final int GUESSES_PER_ROUND    = 10;
    private static final int RANDOM_SELECTOR      = 3;
    private static final int GIVE_CAPITAL         = 0;
    private static final int GIVE_COUNTRY         = 1;
    private static final int GIVE_FACT            = 2;
    private static final int FIRST_TRY            = 0;
    private static final int SECOND_TRY           = 1;
    private static final int THIRD_TRY            = 2;
    private static final int CHAR_OFFSET          = 2;
    private static final int FIRST_HALF           = 0;
    private static final int SECOND_HALF          = 1;
    private static final int FACTS_PER_COUNTRY    = 3;
    private static final int FIRST_TRY_MULTIPLIER = 2;

    private static Score score;

    public static void play() throws IOException
    {
        Scanner  fileScanner;
        char     fileName;
        String   name;
        String   capitalCityName;
        String[] splitter;
        Country  country;

        fileName     = 'a';
        score = new Score();

        // Read all files into the Hash Map
        while (fileName <= 'z')
        {
            if (fileName == 'w')
            {
                fileName += CHAR_OFFSET;
            }

            try
            {
                File file = new File("/Users/colecampbell/IntelliJProjects" +
                                     "/comp2522-term-project/src/resources/" + fileName + ".txt");

                fileScanner = new Scanner(file);

                while (fileScanner.hasNext())
                {
                    fileScanner.nextLine(); // Skip irrelevant line

                    splitter = fileScanner.nextLine().split(":");

                    name            = splitter[FIRST_HALF].trim();
                    capitalCityName = splitter[SECOND_HALF].trim();

                    String[] facts;
                    facts = new String[FACTS_PER_COUNTRY];
                    for (int i = 0; i < FACTS_PER_COUNTRY; i++)
                    {
                        facts[i] = fileScanner.nextLine();
                    }

                    country = new Country(name, capitalCityName, facts);
                    World.worldMap.put(name, country);
                }

            } catch (FileNotFoundException e)
            {
                System.err.println("Error opening file: " + fileName);
            } catch (Exception e)
            {
                System.err.println("Error processing file: " + fileName + " - " + e.getMessage());
            }

            fileName++;
        }

        playRound();
    }

    private static void playRound() throws IOException
    {
        final Scanner           input;
        final Random            random;
        final List<String>      keys;
        String                  userChoice;
        int                     guesses;
        int                     points;

        input     = new Scanner(System.in);
        random    = new Random();
        keys      = new ArrayList<>(World.worldMap.keySet()); // Get all keys from the map
        points    = NOTHING;


        System.out.println("\nNew round\n");

        for (int i = 0; i < GUESSES_PER_ROUND; i++)
        {
            // Select a random key from the list of keys
            String randomKey = keys.get(random.nextInt(keys.size()));

            // Select a random question type
            final int randomNum = random.nextInt(RANDOM_SELECTOR);

            guesses = switch (randomNum)
            {
                case GIVE_CAPITAL -> giveCapital(randomKey);
                case GIVE_COUNTRY -> giveCountry(randomKey);
                case GIVE_FACT -> giveFact(randomKey);
                default -> throw new IllegalArgumentException("This is impossible.");
            };

            System.out.println("Round complete\n");

            // Handle guesses
            if (guesses == FIRST_TRY)
            {
                score.addNumCorrectFirstAttempt();
            } else if (guesses == SECOND_TRY)
            {
                score.addNumCorrectSecondAttempt();
            } else
            {
                score.addNumIncorrectTwoAttempts();
            }
        }

        score.addNumGamesPlayed();
        points += (score.getNumCorrectFirstAttempt() * FIRST_TRY_MULTIPLIER +
                   score.getNumCorrectSecondAttempt());

        System.out.println("============Round Over============");
        System.out.println("\nYes to play again, no to go back to main menu");
        userChoice = input.next().toUpperCase();

        while (!userChoice.equals("YES") &&
               !userChoice.equals("NO"))
        {
            System.out.println("Invalid choice, please try again");
            System.out.println("\nYes to play again, no to go back to main menu");
            userChoice = input.next().toUpperCase();
        }

        if (userChoice.equals("YES"))
        {
            playRound();
        }

        printReport();

        if (points / (double) score.getNumGamesPlayed() > Score.getHighScore())
        {
            System.out.printf("CONGRATULATIONS! " +
                              "You are the new high score with an average of %.2f points per game\n" +
                              "The previous record was %.2f points per game on %s.\n",
                              points / (double) score.getNumGamesPlayed(),
                              Score.getHighScore(),
                              Score.getDateTimeOfHighScore());

            Score.setHighScore(points, score.getNumGamesPlayed());
            Score.setDateTimeOfHighScore(score.getDateTimePlayed());
                    }

        // Append score data to file
        try (final FileWriter writer = new FileWriter("score.txt", true))
        {
            writer.write("Date and Time: " + score.getDateTimePlayed() + "\n");
            writer.write("Games Played: " + score.getNumGamesPlayed() + "\n");
            writer.write("Correct First Attempts: " + score.getNumCorrectFirstAttempt() + "\n");
            writer.write("Correct Second Attempts: " + score.getNumCorrectSecondAttempt() + "\n");
            writer.write("Incorrect Attempts: " + score.getNumIncorrectTwoAttempts() + "\n");
            writer.write("Total Score: " + points + "\n\n");
            System.out.println("Score file updated successfully.");
        } catch (IOException e)
        {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private static int giveCapital(final String key)
    {
        final Scanner input   = new Scanner(System.in);
        int           guesses = NOTHING;
        String        guess;

        while (guesses < THIRD_TRY)
        {
            System.out.println("What country has the capital city of " +
                               World.worldMap.get(key).getCapitalCityName() + "?");

            guess = input.nextLine();

            if (guess.equalsIgnoreCase(World.worldMap.get(key).getName()))
            {
                System.out.println("CORRECT!");
                return guesses;
            }
            System.out.println("INCORRECT!");
            guesses++;
        }
        System.out.println("Sorry, you didn't get it.");
        System.out.println("The correct answer was " +
                           World.worldMap.get(key).getName());
        return guesses;
    }

    private static int giveCountry(final String key)
    {
        final Scanner input   = new Scanner(System.in);
        int           guesses = NOTHING;
        String        guess;

        while (guesses < THIRD_TRY)
        {
            System.out.println("What is the capital city of " +
                               World.worldMap.get(key).getName() + "?");

            guess = input.nextLine();

            if (guess.equalsIgnoreCase(World.worldMap.get(key).getCapitalCityName()))
            {
                System.out.println("CORRECT!");
                return guesses;
            }
            System.out.println("INCORRECT!");
            guesses++;
        }
        System.out.println("Sorry, you didn't get it.");
        System.out.println("The correct answer was " +
                           World.worldMap.get(key).getCapitalCityName());
        return guesses;
    }

    private static int giveFact(final String key)
    {
        final Random  random          = new Random();
        final Scanner input           = new Scanner(System.in);
        int           guesses         = NOTHING;
        String        guess;
        final int     randomFactIndex = random.nextInt(RANDOM_SELECTOR);

        while (guesses < THIRD_TRY)
        {
            System.out.println("What country has this fact: " +
                               World.worldMap.get(key).getFacts(randomFactIndex) + "?");

            guess = input.nextLine();

            if (guess.equalsIgnoreCase(World.worldMap.get(key).getName()))
            {
                System.out.println("CORRECT!");
                return guesses;
            }
            System.out.println("INCORRECT!");
            guesses++;
        }
        System.out.println("Sorry, you didn't get it.");
        System.out.println("The correct answer was " +
                           World.worldMap.get(key).getName());
        return guesses;
    }

    private static void printReport()
    {
        System.out.println("========================================");
        System.out.println(score.getNumGamesPlayed() + " word game(s) played");
        System.out.println(score.getNumCorrectFirstAttempt() + " correct answers on the first attempt");
        System.out.println(score.getNumCorrectSecondAttempt() + " correct answers on the second attempt");
        System.out.println(score.getNumIncorrectTwoAttempts() + " incorrect answers on two attempts each");
        System.out.println("========================================");
    }
}
