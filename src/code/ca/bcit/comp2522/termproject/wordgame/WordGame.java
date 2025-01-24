package ca.bcit.comp2522.termproject.wordgame;

import java.io.File;
import java.io.FileNotFoundException;
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
        Scanner  fileScanner;
        char     fileName;
        String   name;
        String   capitalCityName;
        String[] facts;
        String[] splitter;
        Country  country;

        fileName = 'a';
        facts = new String[3];
        splitter = new String[2];

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

                    name            = splitter[0].trim();
                    capitalCityName = splitter[1].trim();

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
        final Random random;
        int guesses;

        random = new Random();
        int randomNum;

         for (int i = 0; i < 10; i++)
         {
             randomNum = random.nextInt(3);

             // PASS RANDOM COUNTRY KEY STRING TO METHODS BELOW

             guesses = switch (randomNum)
             {
                 case 0 -> giveCapital("United States");
                 case 1 -> giveCountry("United States");
                 case 2 -> giveFact("United States");
                 default -> 2;
             };
             System.out.println("Round complete");
             // round complete, deal with int guesses and add it to the score thing

         }
    }

    private static int giveCapital(final String key)
    {
        final Scanner input;
        int guesses;
        String guess;

        input = new Scanner(System.in);
        guesses = 0;

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
        return guesses;
    }

    private static int giveCountry(final String key)
    {
        final Scanner input;
        int guesses;
        String guess;

        input = new Scanner(System.in);
        guesses = 0;

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
        return guesses;
    }

    private static int giveFact(final String key)
    {
        final Random random;
        final Scanner input;
        int guesses;
        String guess;
        final int randomFactIndex;

        random = new Random();
        input = new Scanner(System.in);
        guesses = 0;
        randomFactIndex = random.nextInt(3);

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
        return guesses;
    }
}