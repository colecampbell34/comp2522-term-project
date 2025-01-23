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
    }

    private final void playRound()
    {
        Random random = new Random();

        int randomNum;

         for (int i = 0; i < 10; i++)
         {
             randomNum = random.nextInt(3);

             switch (randomNum)
             {
                 case 0 -> giveCapital();
                 case 1 -> giveCountry();
                 case 2 -> giveFact();
             }


         }
    }

    private static void giveCapital()
    {
        int guesses;
        boolean correct;

        guesses = 2;
        correct = false;

        while (guesses > 0 ||
               !correct)
        {

        }
    }

    private static void giveCountry()
    {
        int guesses;
        boolean correct;

        guesses = 2;
        correct = false;

        while (guesses > 0 ||
               !correct)
        {

        }
    }

    private static void giveFact()
    {
        int guesses;
        boolean correct;

        guesses = 2;
        correct = false;

        while (guesses > 0 ||
               !correct)
        {

        }
    }
}