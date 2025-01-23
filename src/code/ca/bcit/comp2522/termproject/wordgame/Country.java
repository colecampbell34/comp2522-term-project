package ca.bcit.comp2522.termproject.wordgame;

/**
 * @author colecampbell
 * @version 1.0
 */
public class Country
{
    private final String name;
    private final String capitalCityName;
    private final String[] facts;

    public Country(final String name,
                   final String capitalCityName,
                   final String[] facts)
    {
        this.name = name;
        this.capitalCityName = capitalCityName;
        this.facts = facts;
    }

    public String getName()
    {
        return name;
    }

    public String getCapitalCityName()
    {
        return capitalCityName;
    }

    public String[] getFacts()
    {
        return facts;
    }
}
