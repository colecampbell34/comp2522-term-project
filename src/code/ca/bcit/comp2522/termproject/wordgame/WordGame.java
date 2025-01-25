package ca.bcit.comp2522.termproject.wordgame;

import java.io.File;
import java.io.FileNotFoundException;
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

    public static void play()
    {
        Scanner fileScanner;
        char fileName;
        String name;
        String capitalCityName;
        String[] splitter;
        Country country;

        fileName = 'a';

        // Read all files into the Hash Map
        while (fileName <= 'z')
        {
            if (fileName == 'w')
            {
                fileName += 2;
            }

            try
            {
                File file = new File("/Users/colecampbell/IntelliJProjects/comp2522-term-project/src/resources/" + fileName + ".txt");

                fileScanner = new Scanner(file);

                while (fileScanner.hasNext())
                {
                    fileScanner.nextLine(); // Skip irrelevant line

                    splitter = fileScanner.nextLine().split(":");

                    name = splitter[0].trim();
                    capitalCityName = splitter[1].trim();

                    String[] facts;
                    facts = new String[3];
                    for (int i = 0; i < 3; i++)
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

    private static void playRound()
    {
        final Random random = new Random();
        int guesses;
        List<String> keys = new ArrayList<>(World.worldMap.keySet()); // Get all keys from the map

        for (int i = 0; i < 10; i++)
        {
            if (keys.isEmpty()) {
                System.out.println("No countries available to play.");
                return;
            }

            // Select a random key from the list of keys
            String randomKey = keys.get(random.nextInt(keys.size()));

            // Select a random question type
            int randomNum = random.nextInt(3);

            guesses = switch (randomNum)
            {
                case 0 -> giveCapital(randomKey);
                case 1 -> giveCountry(randomKey);
                case 2 -> giveFact(randomKey);
                default -> 2;
            };

            System.out.println("Round complete\n");
            // Handle guesses and update score here if needed


        }

        // REPORT SCORE IN FORMAT, THEN ASK IF THEY WANT TO PLAY AGAIN

        // LATER WE NEED TO APPEND THE SCORE RESULTS TO A FILE CALLED SCORE.TXT
    }

    private static int giveCapital(final String key)
    {
        final Scanner input = new Scanner(System.in);
        int guesses = 0;
        String guess;

        while (guesses < 2)
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
        final Scanner input = new Scanner(System.in);
        int guesses = 0;
        String guess;

        while (guesses < 2)
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
        final Random random = new Random();
        final Scanner input = new Scanner(System.in);
        int guesses = 0;
        String guess;
        final int randomFactIndex = random.nextInt(3);

        while (guesses < 2)
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
}
