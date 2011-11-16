package net.codjo.confluence;

import net.codjo.test.common.Directory;
import net.codjo.test.common.FileComparator;
import net.codjo.test.common.PathUtil;
import net.codjo.util.file.FileUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConfluenceServerTest extends TestCase {
    private ConfluenceSession session = new ConfluenceSession("http://wd-confluence/confluence", "user_dev",
                                                              "user_dev");
    private ConfluenceServer server = new ConfluenceServer(session);

    private static final String TEST_PAGE_TITLE = "ConfluenceServerTest";
    private static final String TEST_PAGE2_TITLE = "ConfluenceServerTestBis";
    private static final String FIRST_CHILD_TITLE = "first child test";
    private static final String SECOND_CHILD_TITLE = "second child test";
    private static final String SPACE_KEY = "sandbox";


    @Override
    protected void setUp() throws Exception {
        server.login();
    }


    @Override
    protected void tearDown() throws Exception {
        removePage(TEST_PAGE_TITLE);
        removePage(TEST_PAGE2_TITLE);
        removePage(FIRST_CHILD_TITLE);
        removePage(SECOND_CHILD_TITLE);
        server.logout();
    }


    public void test_getSpaces() throws Exception {
        List<Space> spaces = server.getSpaces();
        assertThat(spaces, containsSpaceId(SPACE_KEY));
    }


    public void test_getAllPageSummaries() throws Exception {
        createDummyPage();
        List<PageSummary> summaries = server.getAllPageSummaries(new Space(SPACE_KEY));
        assertThat(summaries, containsPageSummary(TEST_PAGE_TITLE));
    }


    public void test_storePage() throws Exception {
        Page newPage = createPage();
        newPage.setContent("*Page temporaire pour un [TestUnitaire] avec caractères accentués.*");
        Page page = server.storePage(newPage);

        assertNotNull(page.getId());
        assertThat(server.getAllPageSummaries(new Space(SPACE_KEY)), containsPageSummary(TEST_PAGE_TITLE));
        assertEquals(newPage.getContent(), server.getPage(page.getId()).getContent());
    }


    public void test_getChildren() throws Exception {
        Page newPage = createPage();
        newPage.setContent("*Page temporaire pour un [TestUnitaire] avec caractères accentués.*");
        newPage = server.storePage(newPage);
        Page firstChild = new Page(SPACE_KEY, FIRST_CHILD_TITLE);
        firstChild.setParentPageId(newPage.getId());
        server.storePage(firstChild);
        Page secondChild = new Page(SPACE_KEY, SECOND_CHILD_TITLE);
        secondChild.setParentPageId(newPage.getId());
        server.storePage(secondChild);

        List<PageSummary> pageSummaries = server
              .getChildren(server.getPage(SPACE_KEY, TEST_PAGE_TITLE).getId());

        assertEquals(2, pageSummaries.size());
        assertThat(pageSummaries, containsPageSummary(FIRST_CHILD_TITLE));
        assertThat(pageSummaries, containsPageSummary(SECOND_CHILD_TITLE));
    }


    public void test_getChildren_withTitle() throws Exception {
        Page newPage = createPage();
        newPage.setContent("*Page temporaire pour un [TestUnitaire] avec caractères accentués.*");
        newPage = server.storePage(newPage);
        Page firstChild = new Page(SPACE_KEY, FIRST_CHILD_TITLE);
        firstChild.setParentPageId(newPage.getId());
        server.storePage(firstChild);
        Page secondChild = new Page(SPACE_KEY, SECOND_CHILD_TITLE);
        secondChild.setParentPageId(newPage.getId());
        server.storePage(secondChild);

        List<PageSummary> pageSummaries = server.getChildren(SPACE_KEY, TEST_PAGE_TITLE);

        assertEquals(2, pageSummaries.size());
        assertThat(pageSummaries, containsPageSummary(FIRST_CHILD_TITLE));
        assertThat(pageSummaries, containsPageSummary(SECOND_CHILD_TITLE));
    }


    public void test_getLabelContentByName() throws Exception {
        Page newPage = createPage();
        newPage.setTitle(TEST_PAGE_TITLE);
        newPage = server.storePage(newPage);
        server.addLabel("testLabel", newPage.getId());

        List<SearchResult> labels = server.searchByLabelName("testLabel");

        assertEquals(1, labels.size());
        assertEquals(TEST_PAGE_TITLE, labels.get(0).getTitle());
    }


    public void test_getLabelContentByName_returnsNoResults() throws Exception {
        Page newPage = createPage();
        newPage.setTitle(TEST_PAGE_TITLE);
        newPage = server.storePage(newPage);
        server.addLabel("testLabel", newPage.getId());

        List<SearchResult> labels = server.searchByLabelName("a_wrong_label");

        assertEquals(0, labels.size());
    }


    public void test_getLabelsById() throws Exception {
        Page newPage = createPage();
        newPage.setTitle(TEST_PAGE_TITLE);
        newPage = server.storePage(newPage);
        server.addLabel("testlabel1", newPage.getId());
        server.addLabel("testlabel2", newPage.getId());

        List<Label> labels = server.getLabelsById(newPage.getId());
        assertEquals(2, labels.size());
        assertEquals("testlabel1", labels.get(0).getName());
        assertEquals("testlabel2", labels.get(1).getName());
    }


    /**
     * Test désactivé car l'indexation nécessaire à la recherche prend environ 30 secondes ce qui est trop
     * long pour un TU.
     */
    public void search() throws Exception {
        Page newPage = createPage();
        int random = (int)(Math.random() * 1000000);
        newPage.setContent(
              "*Page temporaire pour un [TestUnitaire" + random + "] avec caractères accentués.*\n"
              + "Metadas pour le test.\n"
              + "{metadata-list}|| cle_sans_espace | valeur_sans_espace |\n"
              + "|| cle avec espace | une autre valeur |\n"
              + "{metadata-list}");
        server.storePage(newPage);

        List<SearchResult> results = Collections.emptyList();
        for (int i = 0; i < 20 && results.isEmpty(); i++) {
            results = server.search(SPACE_KEY, "TestUnitaire" + random, 10);
            Thread.sleep(5000);
        }

        assertEquals(1, results.size());
        assertThat(results, containsSearchResult(TEST_PAGE_TITLE));

        results = server.search(SPACE_KEY, "cle_sans_espace:valeur_sans_espace", 10);
        assertEquals(1, results.size());
        assertThat(results, containsSearchResult(TEST_PAGE_TITLE));

        results = server.search(SPACE_KEY, "'cle_sans_espace:une autre valeur'", 10);
        assertEquals(0, results.size());

        results = server.search(SPACE_KEY,
                                "cle_sans_espace:valeur_sans_espace 'cle avec espace:une autre valeur'", 10);
        assertEquals(1, results.size());
        assertThat(results, containsSearchResult(TEST_PAGE_TITLE));
    }


    public void test_getAttachments() throws Exception {
        File tempFile = createDummyFile();
        Page newPage = createDummyPage(tempFile);

        List<Attachment> attachments = server.getAttachments(newPage.getId());
        assertEquals(1, attachments.size());

        InputStreamReader streamReader = null;
        try {
            streamReader = new InputStreamReader(new URL(attachments.get(0).getUrl()).openStream());
            assertTrue(new FileComparator("*").equals(new FileReader(tempFile), streamReader));
        }
        finally {
            if (streamReader != null) {
                streamReader.close();
            }
        }
    }


    private File createDummyFile() throws IOException {
        File tempFile = File.createTempFile("dummy", "txt");
        FileUtil.saveContent(tempFile, "Fichier pour test.");
        return tempFile;
    }


    public void test_moveAttachment() throws Exception {
        File tempFile = createDummyFile();
        Page newPage = createDummyPage(tempFile);

        Page otherPage = new Page(SPACE_KEY, FIRST_CHILD_TITLE);
        otherPage = server.storePage(otherPage);

        boolean result = server
              .moveAttachment(newPage.getId(), tempFile.getName(), otherPage.getId(), "TOTO.pdf");

        assertTrue(result);

        List<Attachment> oldAttachments = server.getAttachments(newPage.getId());
        assertEquals(0, oldAttachments.size());

        List<Attachment> newAttachments = server.getAttachments(otherPage.getId());
        assertEquals(1, newAttachments.size());

        assertEquals("TOTO.pdf", newAttachments.get(0).getFileName());
    }


    public void test_moveAttachment_samePage() throws Exception {
        File tempFile = createDummyFile();
        Page newPage = createDummyPage(tempFile);

        boolean result = server
              .moveAttachment(newPage.getId(), tempFile.getName(), newPage.getId(), "new dummy.pdf");

        assertTrue(result);

        List<Attachment> newAttachments = server.getAttachments(newPage.getId());
        assertEquals(1, newAttachments.size());

        assertEquals("new dummy.pdf", newAttachments.get(0).getFileName());
    }


    public void test_downloadAttachment() throws Exception {
        Directory resourceDir = PathUtil.findResourcesFileDirectory(getClass());
        Directory targetDir = PathUtil.findTargetDirectory(getClass());

        File tempFile = createDummyFile();
        File tempFile2 = new File(resourceDir, "generics-tutorial.pdf");

        Page newPage = createDummyPage(tempFile, tempFile2);

        File result = server.downloadAttachment(newPage.getId(),
                                                tempFile2.getName(),
                                                targetDir.getAbsolutePath());

        assertEquals(targetDir.getAbsolutePath() + File.separator + tempFile2.getName(),
                     result.getAbsolutePath());
        assertEquals(FileUtil.loadContent(tempFile2), FileUtil.loadContent(result));

        result.delete();
    }


    public void test_downloadAttachment_invalidDirectory() throws Exception {
        File tempFile = createDummyFile();
        Page newPage = createDummyPage(tempFile);

        File targetDir = new File(PathUtil.findTargetDirectory(getClass()), "répertoire qui n'existe pas.");

        try {
            server.downloadAttachment(newPage.getId(), tempFile.getName(), targetDir.getAbsolutePath());
            fail("Exception attendue car le répertoire cible est invalide.");
        }
        catch (FileNotFoundException e) {
            ;
        }
    }


    public void test_downloadAttachment_doesNotExist() throws Exception {
        File tempFile = createDummyFile();
        Page newPage = createDummyPage(tempFile);

        Directory targetDir = PathUtil.findTargetDirectory(getClass());

        String attachmentName = tempFile.getName() + "error";
        try {
            server.downloadAttachment(newPage.getId(), attachmentName, targetDir.getAbsolutePath());
            fail("Exception attendue car nom d'attachement incorrect.");
        }
        catch (ConfluenceException e) {
            assertTrue(e.getLocalizedMessage().contains("No attachment on content"));
        }
    }


    public void test_removeLabelByName_noLabelSet() throws ConfluenceException {
        Page newPage = createPage();
        newPage = server.storePage(newPage);

        try {
            server.removeLabelByName("DUMMY LABEL", newPage.getId());
        }
        catch (Exception e) {
            fail("La suppression d'un label inexistant ne doit pas générer d'erreur.");
        }
    }


    public void test_removeLabelByName() throws ConfluenceException {
        Page newPage = server.storePage(createPage());
        server.addLabel("snapshot", newPage.getId());
        server.addLabel("agf-ma-page", newPage.getId());

        List<Label> labels = server.getLabelsById(newPage.getId());
        assertEquals(2, labels.size());
        assertEquals("snapshot", labels.get(0).getName());
        assertEquals("agf-ma-page", labels.get(1).getName());

        Page newPage2 = server.storePage(new Page(SPACE_KEY, TEST_PAGE2_TITLE));
        server.addLabel("snapshot", newPage2.getId());

        server.removeLabelByName("snapshot");

        assertEquals(1, server.getLabelsById(newPage.getId()).size());
        assertEquals(0, server.getLabelsById(newPage2.getId()).size());
    }


    public void test_timeout() throws Exception {
        server.setTimeout(1);
        try {
            server.getSpaces();
            fail("Timeout attendu");
        }
        catch (ConfluenceTimeoutException e) {
            assertEquals("Délai d'attente dépassé.", e.getLocalizedMessage());
        }
        finally {
            server.setTimeout(0);
        }
    }


    public void test_retryCount_default() throws Exception {

        ConfluenceSession aSession = new ConfluenceSession("http://wrong-url", "user_dev", "user_dev");

        ConfluenceServer aServer = new ConfluenceServer(aSession);
        aServer.setRetryDelay(100);

        XmlRpcClient rpcClient = mock(XmlRpcClient.class);
        aServer.setXmlRpcClient(rpcClient);

        when(rpcClient.execute(anyString(), (List)anyObject()))
              .thenThrow(new XmlRpcException("Wrong server mate !"));

        try {
            aServer.login();
            fail("Exception attendue");
        }
        catch (ConfluenceException exception) {
            assertTrue(exception.getLocalizedMessage().contains("Wrong server mate !"));
        }

        verify(rpcClient, times(3)).execute(anyString(), (List)anyObject());
    }


    public void test_retryCount() throws Exception {

        ConfluenceSession aSession = new ConfluenceSession("http://wrong-url", "user_dev", "user_dev");

        ConfluenceServer aServer = new ConfluenceServer(aSession);
        aServer.setRetryCount(4);
        aServer.setRetryDelay(100);

        XmlRpcClient rpcClient = mock(XmlRpcClient.class);
        aServer.setXmlRpcClient(rpcClient);

        when(rpcClient.execute(anyString(), (List)anyObject()))
              .thenThrow(new XmlRpcException("Wrong server mate !"));

        try {
            aServer.login();
            fail("Exception attendue");
        }
        catch (ConfluenceException exception) {
            assertTrue(exception.getLocalizedMessage().contains("Wrong server mate !"));
        }

        verify(rpcClient, times(5)).execute(anyString(), (List)anyObject());
    }


    public void test_retryCount_retryNotNeeded() throws Exception {

        ConfluenceSession aSession = new ConfluenceSession("http://wrong-url", "user_dev", "user_dev");

        ConfluenceServer aServer = new ConfluenceServer(aSession);
        aServer.setRetryCount(1001);

        XmlRpcClient rpcClient = mock(XmlRpcClient.class);
        aServer.setXmlRpcClient(rpcClient);

        when(rpcClient.execute(anyString(), (List)anyObject()))
              .thenThrow(new XmlRpcException("Page does not exist"));

        try {
            aServer.login();
            fail("Exception attendue");
        }
        catch (ConfluenceException exception) {
            assertTrue(exception.getLocalizedMessage().contains("Page does not exist"));
        }

        verify(rpcClient, times(1)).execute(anyString(), (List)anyObject());
    }


    private Page createDummyPage(File... files) throws ConfluenceException, IOException {
        Page newPage = createPage();
        newPage = server.storePage(newPage);
        for (File file : files) {
            attachFileToPage(newPage, file);
        }
        return newPage;
    }


    private void attachFileToPage(Page newPage, File file) throws IOException, ConfluenceException {
        ConfluenceUploader confluenceUploader = null;
        try {
            confluenceUploader = new ConfluenceUploader(session);
            confluenceUploader.login();
            confluenceUploader.upload(file, newPage.getId());
        }
        finally {
            if (confluenceUploader != null) {
                confluenceUploader.logout();
            }
        }
    }


    private void removePage(String testPageTitle) {
        try {
            Page page = server.getPage(SPACE_KEY, testPageTitle);
            if (page != null) {
                server.removePage(page);
            }
        }
        catch (Throwable exception) {
            ;
        }
    }


    private Page createPage() {
        Page newPage = new Page();
        newPage.setSpaceKey(SPACE_KEY);
        newPage.setTitle(TEST_PAGE_TITLE);
        return newPage;
    }


    private <T> void assertThat(List<T> list, AssertComparator<T> comparator) {
        for (T object : list) {
            if (comparator.found(object)) {
                return;
            }
        }
        fail();
    }


    private AssertComparator<Space> containsSpaceId(final String spaceKey) {
        return new AssertComparator<Space>() {
            public boolean found(Space space) {
                return spaceKey.equals(space.getKey());
            }
        };
    }


    private AssertComparator<PageSummary> containsPageSummary(final String title) {
        return new AssertComparator<PageSummary>() {
            public boolean found(PageSummary summary) {
                return title.equals(summary.getTitle());
            }
        };
    }


    private AssertComparator<SearchResult> containsSearchResult(final String title) {
        return new AssertComparator<SearchResult>() {
            public boolean found(SearchResult searchResult) {
                return title.equals(searchResult.getTitle());
            }
        };
    }


    private static interface AssertComparator<T> {
        public boolean found(T object);
    }
}
