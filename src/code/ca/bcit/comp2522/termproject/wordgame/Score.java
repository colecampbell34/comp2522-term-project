package ca.bcit.comp2522.termproject.wordgame;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 *
 * @author colecampbell
 * @version 1.0
 */
public class Score
{
    private static final int NOTHING = 0;

    private final String dateTimePlayed;
    private int numGamesPlayed;
    private int numCorrectFirstAttempt;
    private int numCorrectSecondAttempt;
    private int numIncorrectTwoAttempts;
    private static double highScore;
    private static String dateTimeOfHighScore;

    static
    {
        highScore = NOTHING;
        dateTimeOfHighScore = null;
    }

    public Score()
    {
        final LocalDateTime     currentTime;
        final DateTimeFormatter formatter;
        final String            formattedDateTime;

        currentTime       = LocalDateTime.now();
        formatter         = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        formattedDateTime = currentTime.format(formatter);

        dateTimePlayed = formattedDateTime;
        numGamesPlayed = NOTHING;
        numCorrectFirstAttempt = NOTHING;
        numCorrectSecondAttempt = NOTHING;
        numIncorrectTwoAttempts = NOTHING;

    }

    public String getDateTimePlayed()
    {
        return dateTimePlayed;
    }

    public int getNumGamesPlayed()
    {
        return numGamesPlayed;
    }

    public void addNumGamesPlayed()
    {
        this.numGamesPlayed ++;
    }

    public int getNumCorrectFirstAttempt()
    {
        return numCorrectFirstAttempt;
    }

    public void addNumCorrectFirstAttempt()
    {
        this.numCorrectFirstAttempt ++;
    }

    public int getNumCorrectSecondAttempt()
    {
        return numCorrectSecondAttempt;
    }

    public void addNumCorrectSecondAttempt()
    {
        this.numCorrectSecondAttempt ++;
    }

    public int getNumIncorrectTwoAttempts()
    {
        return numIncorrectTwoAttempts;
    }

    public void addNumIncorrectTwoAttempts()
    {
        this.numIncorrectTwoAttempts ++;
    }

    public static double getHighScore()
    {
        return highScore;
    }

    public static void setHighScore(final int points,
                                    final int gamesPlayed)
    {
        Score.highScore = points / (double) gamesPlayed;
    }

    public static String getDateTimeOfHighScore()
    {
        return dateTimeOfHighScore;
    }

    public static void setDateTimeOfHighScore(final String dateTimeOfHighScore)
    {
        Score.dateTimeOfHighScore = dateTimeOfHighScore;
    }
}
