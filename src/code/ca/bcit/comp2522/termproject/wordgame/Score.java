package ca.bcit.comp2522.termproject.wordgame;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Represents a game score with details such as attempts and timestamp.
 *
 * @author colecampbell
 * @version 1.0
 */
public class Score
{
    private static final DateTimeFormatter formatter;
    private static final int               FIRST_TRY_MULTIPLIER     = 2;
    private static final int               ACTUAL_DATE_STRING_INDEX = 15;
    private static final int               DATA_HALF                = 1;

    static
    {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    private final LocalDateTime dateTime;
    private final int           gamesPlayed;
    private final int           correctFirstAttempts;
    private final int           correctSecondAttempts;
    private final int           incorrectAttempts;
    private final int           score;

    /**
     * Constructs a Score object.
     *
     * @param dateTime              the date and time of the score
     * @param gamesPlayed           the number of games played
     * @param correctFirstAttempts  the number of correct first attempts
     * @param correctSecondAttempts the number of correct second attempts
     * @param incorrectAttempts     the number of incorrect attempts
     */
    public Score(final LocalDateTime dateTime,
                 final int gamesPlayed,
                 final int correctFirstAttempts,
                 final int correctSecondAttempts,
                 final int incorrectAttempts)
    {
        this.dateTime              = dateTime;
        this.gamesPlayed           = gamesPlayed;
        this.correctFirstAttempts  = correctFirstAttempts;
        this.correctSecondAttempts = correctSecondAttempts;
        this.incorrectAttempts     = incorrectAttempts;
        this.score                 = (correctFirstAttempts * FIRST_TRY_MULTIPLIER) +
                                     correctSecondAttempts;
    }

    /**
     * Accessor for the total score.
     *
     * @return the total score
     */
    public int getScore()
    {
        return score;
    }

    /**
     * Accessor for the average score.
     *
     * @return the average score
     */
    public double getAvgScore()
    {
        return score / (double) gamesPlayed;
    }

    /**
     * Accessor for the date and time.
     *
     * @return the date and time of the game
     */
    public String getDate()
    {
        return dateTime.format(formatter);
    }

    /**
     * Overrides the toString method.
     *
     * @return all the instance variables
     */
    @Override
    public String toString()
    {
        return String.format(
                """
                Date and Time: %s
                Games Played: %d
                Correct First Attempts: %d
                Correct Second Attempts: %d
                Incorrect Attempts: %d
                Score: %d points
                """,
                dateTime.format(formatter),
                gamesPlayed,
                correctFirstAttempts,
                correctSecondAttempts,
                incorrectAttempts,
                score);
    }

    /**
     * Appends a score entry to a file.
     *
     * @param score    the score to append
     * @param filePath the file to store scores
     * @throws IOException if an I/O error occurs
     */
    public static void appendScoreToFile(final Score score,
                                         final String filePath) throws IOException
    {
        try (final FileWriter writer = new FileWriter(filePath, true))
        {
            writer.write(score.toString() + "\n");
        }
    }

    /**
     * Reads scores from a file.
     *
     * @param filePath the file to read scores from
     * @return a list of Score objects
     * @throws IOException if an I/O error occurs
     */
    public static List<Score> readScoresFromFile(final String filePath) throws IOException
    {
        final List<Score> scores;
        final File        file;

        scores = new ArrayList<>();
        file   = new File(filePath);

        if (!file.exists())
        {
            System.out.println("File does not exist");
            return scores;
        }

        try (final Scanner scanner = new Scanner(file))
        {
            while (scanner.hasNextLine())
            {
                final String line;
                line = scanner.nextLine();

                // make sure we read in the proper order
                if (line.startsWith("Date and Time: "))
                {
                    final LocalDateTime dateTime;
                    final int           gamesPlayed;
                    final int           correctFirstAttempts;
                    final int           correctSecondAttempts;
                    final int           incorrectAttempts;

                    dateTime              = LocalDateTime.parse(line.substring(ACTUAL_DATE_STRING_INDEX), formatter);
                    gamesPlayed           = Integer.parseInt(scanner.nextLine().split(": ")[DATA_HALF]);
                    correctFirstAttempts  = Integer.parseInt(scanner.nextLine().split(": ")[DATA_HALF]);
                    correctSecondAttempts = Integer.parseInt(scanner.nextLine().split(": ")[DATA_HALF]);
                    incorrectAttempts     = Integer.parseInt(scanner.nextLine().split(": ")[DATA_HALF]);


                    scanner.nextLine();
                    scores.add(new Score(dateTime,
                                         gamesPlayed,
                                         correctFirstAttempts,
                                         correctSecondAttempts,
                                         incorrectAttempts));
                }
            }
        }

        return scores;
    }
}
