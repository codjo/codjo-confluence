package net.codjo.confluence;
import junit.framework.TestCase;
/**
 *
 */
public class ConfluenceObjectTest extends TestCase {

    public void testParseDate() throws Exception {
        assertNotNull(ConfluenceObject.createDateFormat().parse("Mon Feb 19 17:00:03 CET 2007"));
    }


    public void testParseBoolean() throws Exception {
        assertTrue(Boolean.valueOf("true"));
        assertFalse(Boolean.valueOf("false"));
    }
}
