package ca.bcit.comp2522.termproject.wordgame;

/**
 * Represents a single country with variables for
 * name, capital city, and facts.
 *
 * @author colecampbell
 * @version 1.0
 */
public class Country
{
    private final String   name;
    private final String   capitalCityName;
    private final String[] facts;

    /**
     * Constructs a Country object.
     *
     * @param name            the name of the country
     * @param capitalCityName the name of the capital city
     * @param facts           the three random facts from the country
     */
    public Country(final String name,
                   final String capitalCityName,
                   final String[] facts)
    {
        this.name            = name;
        this.capitalCityName = capitalCityName;
        this.facts           = facts;
    }

    /**
     * Accessor for the country name.
     *
     * @return the country name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Accessor for the countries capital.
     *
     * @return the capital city name
     */
    public String getCapitalCityName()
    {
        return capitalCityName;
    }

    /**
     * Accessor for one random fact.
     *
     * @param index the random index for the fact
     * @return the fact
     */
    public String getFacts(final int index)
    {
        return facts[index];
    }
}
