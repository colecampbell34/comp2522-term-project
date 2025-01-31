package ca.bcit.comp2522.termproject.wordgame;

/**
 *
 *
 * @author colecampbell
 * @version 1.0
 */
public class Score
{
    private String dateTimePlayed;
    private int numGamesPlayed;
    private int numCorrectFirstAttempt;
    private int numCorrectSecondAttempt;
    private int numIncorrectTwoAttempts;

    // CHANGE WORD GAME CLASS SO IT UPDATES THE SCORE OBJECT INSTEAD


    public String getDateTimePlayed()
    {
        return dateTimePlayed;
    }

    public void setDateTimePlayed(final String dateTimePlayed)
    {
        this.dateTimePlayed = dateTimePlayed;
    }

    public int getNumGamesPlayed()
    {
        return numGamesPlayed;
    }

    public void setNumGamesPlayed(final int numGamesPlayed)
    {
        this.numGamesPlayed = numGamesPlayed;
    }

    public int getNumCorrectFirstAttempt()
    {
        return numCorrectFirstAttempt;
    }

    public void setNumCorrectFirstAttempt(final int numCorrectFirstAttempt)
    {
        this.numCorrectFirstAttempt = numCorrectFirstAttempt;
    }

    public int getNumCorrectSecondAttempt()
    {
        return numCorrectSecondAttempt;
    }

    public void setNumCorrectSecondAttempt(final int numCorrectSecondAttempt)
    {
        this.numCorrectSecondAttempt = numCorrectSecondAttempt;
    }

    public int getNumIncorrectTwoAttempts()
    {
        return numIncorrectTwoAttempts;
    }

    public void setNumIncorrectTwoAttempts(final int numIncorrectTwoAttempts)
    {
        this.numIncorrectTwoAttempts = numIncorrectTwoAttempts;
    }
}
