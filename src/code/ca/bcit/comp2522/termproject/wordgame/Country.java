package ca.bcit.comp2522.termproject.wordgame;

/**
 * Represents a single country with variables for
 * name, capital city, and facts.
 *
 * @author colecampbell
 * @version 1.0
 */
public final class Country
{
    private static final int NOTHING = 0;

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
        validateName(name);
        validateCapitalCityName(capitalCityName);
        validateFacts(facts);

        this.name            = name;
        this.capitalCityName = capitalCityName;
        this.facts           = facts;
    }

    /*
     * Validates the country name for null or blank.
     */
    private void validateName(final String name)
    {
        if (name == null ||
            name.isBlank())
        {
            throw new IllegalArgumentException("Bad name");
        }
    }

    /*
     * Validates the capital city name for null or blank.
     */
    private void validateCapitalCityName(final String capitalCityName)
    {
        if (capitalCityName == null ||
            capitalCityName.isBlank())
        {
            throw new IllegalArgumentException(("Bad capital city name"));
        }
    }

    /*
     * Validates the facts array for empty.
     */
    private void validateFacts(final String[] facts)
    {
        if (facts.length == NOTHING)
        {
            throw new IllegalArgumentException("Bad facts array");
        }
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
        validateIndex(index);

        return facts[index];
    }

    /*
     * Validates that the index is within range.
     */
    private void validateIndex(final int index)
    {
        if (index < 0 ||
            index >= facts.length)
        {
            throw new IndexOutOfBoundsException("Invalid fact index: " +
                                                index);
        }
    }
}
