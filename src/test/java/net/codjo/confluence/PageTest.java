package net.codjo.confluence;

import org.junit.Assert;
import org.junit.Test;

public class PageTest {
    @Test
    public void test_equalsFalse() throws Exception {
        Page page1 = new PageMock("1", "toto");
        Page page2 = new PageMock("2", "titi");

        Assert.assertFalse(page1.equals(page2));
        Assert.assertFalse(page2.equals(page1));
    }

    @Test
    public void test_equalsTrue() throws Exception {
        Page page1 = new PageMock("1", "toto");
        Page page2 = new PageMock("1", "titi");

        Assert.assertTrue(page1.equals(page2));
        Assert.assertTrue(page2.equals(page1));
    }

}
