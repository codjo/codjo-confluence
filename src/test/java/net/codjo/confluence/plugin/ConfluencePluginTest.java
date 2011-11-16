package net.codjo.confluence.plugin;

import net.codjo.confluence.Attachment;
import net.codjo.confluence.ConfluenceException;
import net.codjo.confluence.ConfluenceServer;
import net.codjo.confluence.ConfluenceTimeoutException;
import net.codjo.confluence.DefaultSearchCriteria;
import net.codjo.confluence.Label;
import net.codjo.confluence.Page;
import net.codjo.confluence.PageSummary;
import net.codjo.confluence.SearchCriteria;
import net.codjo.confluence.SearchResult;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.mockito.Mockito;

public class ConfluencePluginTest extends TestCase {

    private static final String SPACE_KEY = "sandbox";
    private static final String NEW_PAGE_TITLE = "Titre";
    private static final String CHILD_PAGE_TITLE = "Child";

    private ConfluenceOperations confluenceOperations;
    private ConfluencePlugin plugin;


    public void test_deletePage() throws Exception {
        Page page = confluenceOperations.createPage(SPACE_KEY, null, NEW_PAGE_TITLE, null, "Hi there");
        String pageId = page.getId();
        confluenceOperations.deletePage(pageId);
        assertNull(confluenceOperations.getPage(SPACE_KEY, NEW_PAGE_TITLE));
    }


    public void test_operations_getPageByTitle() throws Exception {
        Page homePage = confluenceOperations.getPage(SPACE_KEY, "Home");
        assertNotNull(homePage);
    }


    public void test_operations_getPageById() throws Exception {
        Page homePage = confluenceOperations.getPage(SPACE_KEY, "Home");
        Page stillHomePage = confluenceOperations.getPage(homePage.getId());
        assertEquals(stillHomePage.getId(), homePage.getId());
        assertEquals(stillHomePage.getTitle(), homePage.getTitle());
        assertEquals(stillHomePage.getContent(), homePage.getContent());
    }


    public void test_operations_getChildren() throws Exception {
        Page fatherPage = confluenceOperations.createPage(SPACE_KEY, null, NEW_PAGE_TITLE, null, "Hi there");
        String fatherPageId = fatherPage.getId();
        Page child1 = confluenceOperations
              .createPage(SPACE_KEY, fatherPageId, CHILD_PAGE_TITLE, null, "child1");
        List<PageSummary> result = confluenceOperations.getChildren(fatherPageId);
        assertEquals(1, result.size());

        assertEquals(child1.getParentPageId(), fatherPageId);
        assertEquals(child1.getId(), result.get(0).getId());
        assertEquals(child1.getTitle(), result.get(0).getTitle());
    }


    public void test_operations_createPage() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("cle1", "val1");
        map.put("cle2", "val2");

        confluenceOperations.createPage(SPACE_KEY, null, NEW_PAGE_TITLE, map, "Hi there");

        Page searchedPage = confluenceOperations.getPage(SPACE_KEY, NEW_PAGE_TITLE);
        assertNotNull(searchedPage);
        assertTrue(searchedPage.getContent().contains("Hi there"));
    }


    public void test_operations_attachFile() throws Exception {
        Page page = confluenceOperations.createPage(SPACE_KEY, null, NEW_PAGE_TITLE, null, "Hi there");
        confluenceOperations
              .attachFile(new File(getClass().getResource("/fichier.txt").getFile()), page.getId());
        List<Attachment> attachments = confluenceOperations.getAttachments(page.getId());
        assertEquals(1, attachments.size());
        Attachment attachment = attachments.get(0);
        assertEquals("fichier.txt", attachment.getFileName());
        assertEquals("8", attachment.getFileSize());
    }


    public void test_operations_attachFile_timeout() throws Exception {
        Page page = confluenceOperations.createPage(SPACE_KEY, null, NEW_PAGE_TITLE, null, "Hi there");
        plugin.getConfiguration().setTimeout(1);
        try {
            confluenceOperations
                  .attachFile(new File(getClass().getResource("/fichier.txt").getFile()), page.getId());
            fail();
        }
        catch (ConfluenceTimeoutException e) {
            assertEquals("Délai d'attente dépassé.", e.getLocalizedMessage());
        }
        plugin.getConfiguration().setTimeout(0);
        List<Attachment> attachments = confluenceOperations.getAttachments(page.getId());
        assertEquals(0, attachments.size());
    }


    public void test_operations_updatePage_newPage() throws Exception {
        Page page = new Page();
        page.setSpaceKey(SPACE_KEY);
        page.setTitle(NEW_PAGE_TITLE);
        page.setContent("Content");

        Page newPage = confluenceOperations.updatePage(page);
        assertNotNull(newPage);
        assertNotNull(newPage.getId());
    }


    public void test_operations_updatePage_existingPage() throws Exception {
        Page page = new Page();
        page.setSpaceKey(SPACE_KEY);
        page.setTitle(NEW_PAGE_TITLE);
        page.setContent("Content");
        page = confluenceOperations.updatePage(page);

        page.setContent("New content");
        Page newPage = confluenceOperations.updatePage(page);

        assertNotNull(newPage);
        assertEquals(page.getId(), newPage.getId());
        assertEquals("New content", newPage.getContent());
    }


    public void test_operations_searchByCriteria() throws Exception {
        ConfluenceServer server = Mockito.mock(ConfluenceServer.class);
        ConfluencePlugin confluencePlugin = Mockito.mock(ConfluencePlugin.class);
        Mockito.when(confluencePlugin.getServer()).thenReturn(server);

        ConfluencePlugin.ConfluenceOperationsImpl operations
              = confluencePlugin.new ConfluenceOperationsImpl();

        SearchCriteria criteria = new DefaultSearchCriteria();
        List<SearchResult> expectedResult = new ArrayList<SearchResult>();
        Mockito.when(server.search(SPACE_KEY, criteria.toLuceneString(), 50)).thenReturn(expectedResult);

        List<SearchResult> result = operations.searchByCriteria(SPACE_KEY, criteria, 50);

        Mockito.verify(server).login();
        Mockito.verify(server).logout();

        assertEquals(expectedResult, result);
    }


    public void test_getPages() throws Exception {
        List<String> pageIds = Arrays.asList("page1", "page2", "page3");

        Page page1 = new Page();
        Page page2 = new Page();

        ConfluenceServer server = Mockito.mock(ConfluenceServer.class);
        ConfluencePlugin confluencePlugin = Mockito.mock(ConfluencePlugin.class);
        Mockito.when(confluencePlugin.getServer()).thenReturn(server);

        ConfluencePlugin.ConfluenceOperationsImpl operations
              = confluencePlugin.new ConfluenceOperationsImpl();

        Mockito.when(server.getPage("page1")).thenReturn(page1);
        Mockito.when(server.getPage("page2")).thenReturn(page2);
        Mockito.when(server.getPage("page3")).thenThrow(new ConfluenceException("Page does not exist!"));

        List<Page> actualPages = operations.getPages(pageIds);

        assertEquals(3, actualPages.size());
        assertEquals(page1, actualPages.get(0));
        assertEquals(page2, actualPages.get(1));
        assertNull(actualPages.get(2));
    }


    public void test_removeLabelByNameForPage() throws ConfluenceException {
        Page page = confluenceOperations.createPage(SPACE_KEY, null, NEW_PAGE_TITLE, null, "Hi there");
        confluenceOperations.addLabel(page.getId(), "label1");
        confluenceOperations.addLabel(page.getId(), "label2");
        confluenceOperations.removeLabelByName(page.getId(), "label1");
        List<Label> labels = confluenceOperations.getLabelsById(page.getId());
        assertEquals(1, labels.size());
        assertEquals("label2", labels.get(0).getName());
    }


    public void test_removeLabelByName() throws ConfluenceException {
        Page page1 = confluenceOperations.createPage(SPACE_KEY, null, NEW_PAGE_TITLE, null, "Hi there");
        confluenceOperations.addLabel(page1.getId(), "usetheforceluke");
        confluenceOperations.addLabel(page1.getId(), "label2");
        Page page2 = confluenceOperations.createPage(SPACE_KEY, null, CHILD_PAGE_TITLE, null, "Hi there");
        confluenceOperations.addLabel(page2.getId(), "usetheforceluke");
        confluenceOperations.removeLabelByName("usetheforceluke");
        List<Label> labels1 = confluenceOperations.getLabelsById(page1.getId());
        assertEquals(1, labels1.size());
        assertEquals("label2", labels1.get(0).getName());
        List<Label> labels2 = confluenceOperations.getLabelsById(page2.getId());
        assertTrue(labels2.isEmpty());
    }


    public void testRunWithRetry() throws ConfluenceException {
        ConfluenceServer server = Mockito.mock(ConfluenceServer.class);

        final List<String> resultList = new ArrayList<String>();

        plugin.new RunWithRetry<String>(server) {
            boolean hasPlanted = false;


            @Override
            public List<String> prepare() throws ConfluenceException {
                return Arrays.asList("a", "b", "c");
            }


            @Override
            public void iterate(String param) throws ConfluenceException {
                if ("b".equals(param) && !hasPlanted) {
                    hasPlanted = true;
                    throw new ConfluenceException("Call login() to open a new session");
                }
                resultList.add(param);
            }
        }.run();

        Mockito.verify(server, Mockito.times(2)).login();
        Mockito.verify(server, Mockito.times(1)).logout();

        assertEquals(3, resultList.size());
        assertEquals("a", resultList.get(0));
        assertEquals("b", resultList.get(1));
        assertEquals("c", resultList.get(2));
    }


    @Override
    protected void setUp() throws Exception {
        plugin = new ConfluencePlugin();
        plugin.getConfiguration().setServerUrl("http://wd-confluence/confluence");
        plugin.getConfiguration().setUser("user_dev");
        plugin.getConfiguration().setPassword("user_dev");
        plugin.start(null);
        confluenceOperations = plugin.getOperations();
    }


    @Override
    protected void tearDown() throws Exception {
        purgeNewPage();
    }


    private void purgeNewPage() throws ConfluenceException {
        Page page = confluenceOperations.getPage(SPACE_KEY, NEW_PAGE_TITLE);
        if (page != null) {
            confluenceOperations.deletePage(page.getId());
        }

        Page child = confluenceOperations.getPage(SPACE_KEY, CHILD_PAGE_TITLE);
        if (child != null) {
            confluenceOperations.deletePage(child.getId());
        }
    }
}
