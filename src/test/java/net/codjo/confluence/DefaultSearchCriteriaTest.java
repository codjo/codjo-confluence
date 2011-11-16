package net.codjo.confluence;
import junit.framework.TestCase;
import net.codjo.confluence.SearchCriteria.Match;
/**
 *
 */
public class DefaultSearchCriteriaTest extends TestCase {
    private DefaultSearchCriteria criteria;


    public void test_withCriteria() throws Exception {
        assertTrue(criteria.isEmpty());

        assertSame(criteria, criteria.withCriteria("date", "2007-02-01", Match.exact));
        assertFalse(criteria.isEmpty());

        criteria.removeCriteria("date");
        assertTrue(criteria.isEmpty());
    }


    public void test_toLuceneQuery() throws Exception {
        criteria
              .withCriteria("auteur", "toto", Match.exact)
              .withRangeCriteria("date", "20080101", "20080115");

        assertEquals("(\"auteur:toto\") AND (date:[20080101 TO 20080115])", criteria.toLuceneString());

        criteria
              .removeCriteria("auteur")
              .removeCriteria("date")
              .withCriteria("auteur", "toto", Match.contains)
              .withRangeCriteria("date", "20080101", "20080115");

        assertEquals("(\"auteur:toto\") AND (date:[20080101 TO 20080115])", criteria.toLuceneString());
    }

    public void test_repeatedCriteria() throws Exception {
        criteria
              .withCriteria("auteur", "toto", Match.exact)
              .withCriteria("auteur", "titi", Match.exact);

        assertEquals("(\"auteur:titi\")", criteria.toLuceneString());
    }


    @Override
    protected void setUp() throws Exception {
        criteria = new DefaultSearchCriteria();
    }
}
