package net.codjo.confluence.plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.confluence.Attachment;
import net.codjo.confluence.BlogEntry;
import net.codjo.confluence.ConfluenceException;
import net.codjo.confluence.ConfluenceServer;
import net.codjo.confluence.ConfluenceSession;
import net.codjo.confluence.ConfluenceUploader;
import net.codjo.confluence.Label;
import net.codjo.confluence.Page;
import net.codjo.confluence.PageSummary;
import net.codjo.confluence.SearchCriteria;
import net.codjo.confluence.SearchResult;
import net.codjo.plugin.common.ApplicationPlugin;

public class ConfluencePlugin implements ApplicationPlugin {

    private ConfluenceOperations operations = new ConfluenceOperationsImpl();
    private ConfluenceServer server;
    private ConfluenceSession session;

    private ConfluencePluginConfiguration configuration = new ConfluencePluginConfiguration();


    public void initContainer(ContainerConfiguration containerConfiguration) throws Exception {
    }


    public void start(AgentContainer agentContainer) throws Exception {
        session = new ConfluenceSession(configuration.getServerUrl(), configuration.getUser(),
                                        configuration.getPassword());
        server = new ConfluenceServer(session);
    }


    public void stop() throws Exception {

    }


    public ConfluenceOperations getOperations() {
        return operations;
    }


    public ConfluencePluginConfiguration getConfiguration() {
        return configuration;
    }


    ConfluenceServer getServer() {
        server.setTimeout(configuration.getTimeout());
        server.setRetryCount(configuration.getRetryCount());
        server.setRetryDelay(configuration.getRetryDelay());
        return server;
    }


    public class ConfluenceOperationsImpl implements ConfluenceOperations {

        public Page getPage(String spaceKey, String pageTitle) throws ConfluenceException {
            getServer().login();
            Page page = null;
            try {
                page = getServer().getPage(spaceKey, pageTitle);
            }
            catch (ConfluenceException exception) {
                if (!exception.getLocalizedMessage().contains("does not exist")) {
                    throw exception;
                }
            }
            getServer().logout();
            return page;
        }


        public Page getPage(String pageId) throws ConfluenceException {
            return getPages(Arrays.asList(pageId)).get(0);
        }


        public List<Page> getPages(final List<String> pageIds) throws ConfluenceException {
            final List<Page> result = new ArrayList<Page>(pageIds.size());
            new RunWithRetry<String>(getServer()) {

                @Override
                public List<String> prepare() throws ConfluenceException {
                    return pageIds;
                }


                @Override
                public void iterate(String pageId) throws ConfluenceException {
                    try {
                        Page page = getServer().getPage(pageId);
                        result.add(page);
                    }
                    catch (ConfluenceException exception) {
                        if (!exception.getLocalizedMessage().contains("does not exist")) {
                            throw exception;
                        }
                        result.add(null);
                    }
                }
            }.run();
            return result;
        }


        public List<Page> getPagesByLabel(final String spaceKey, final String label)
              throws ConfluenceException {
            final List<Page> list = new ArrayList<Page>();
            new RunWithRetry<SearchResult>(getServer()) {

                @Override
                public List<SearchResult> prepare() throws ConfluenceException {
                    return server.searchByLabelName(label);
                }


                @Override
                public void iterate(SearchResult searchResult) throws ConfluenceException {
                    if ("page".equals(searchResult.getType())) {
                        Page page = getServer().getPage(searchResult.getId());
                        if (page.getSpaceKey().equals(spaceKey)) {
                            list.add(page);
                        }
                    }
                }
            }.run();
            return list;
        }


        public List<BlogEntry> getBlogEntriesByLabel(final String spaceKey, final String label)
              throws ConfluenceException {
            final List<BlogEntry> list = new ArrayList<BlogEntry>();
            new RunWithRetry<SearchResult>(getServer()) {

                @Override
                public List<SearchResult> prepare() throws ConfluenceException {
                    return server.searchByLabelName(label);
                }


                @Override
                public void iterate(SearchResult searchResult) throws ConfluenceException {
                    if ("blogpost".equals(searchResult.getType())) {
                        BlogEntry blogEntry = getServer().getBlogEntry(searchResult.getId());
                        if (blogEntry.getSpaceKey().equals(spaceKey)) {
                            list.add(blogEntry);
                        }
                    }
                }
            }.run();
            return list;
        }


        public List<PageSummary> getChildren(String pageId) throws ConfluenceException {
            getServer().login();
            List<PageSummary> pageSummaries = getServer().getChildren(pageId);
            getServer().logout();
            return pageSummaries;
        }


        public List<SearchResult> searchByCriteria(String spaceKey, SearchCriteria criteria, int maxResults)
              throws ConfluenceException {
            return searchByCriteria(spaceKey, "page", criteria, maxResults);
        }


        public List<SearchResult> searchByCriteria(String spaceKey,
                                                   String type,
                                                   SearchCriteria searchCriteria,
                                                   int maxResults) throws ConfluenceException {
            getServer().login();
            List<SearchResult> searchResults = getServer()
                  .search(spaceKey, type, searchCriteria.toLuceneString(), maxResults);
            getServer().logout();
            return searchResults;
        }


        public List<Page> searchPagesByCriteria(final String spaceKey,
                                                final SearchCriteria criteria,
                                                final int maxResults) throws ConfluenceException {
            final List<Page> pages = new ArrayList<Page>();
            new RunWithRetry<SearchResult>(getServer()) {

                @Override
                public List<SearchResult> prepare() throws ConfluenceException {
                    return server.search(spaceKey, criteria.toLuceneString(), maxResults);
                }


                @Override
                public void iterate(SearchResult searchResult) throws ConfluenceException {
                    Page page = server.getPage(searchResult.getId());
                    pages.add(page);
                }
            }.run();
            return pages;
        }


        public List<Label> getLabelsById(String pageId) throws ConfluenceException {
            getServer().login();
            List<Label> labels = getServer().getLabelsById(pageId);
            getServer().logout();
            return labels;
        }


        public void addLabel(String objectId, String label) throws ConfluenceException {
            getServer().login();
            getServer().addLabel(label, objectId);
            getServer().logout();
        }


        public void deletePage(String pageId) throws ConfluenceException {
            getServer().login();
            Page page = getServer().getPage(pageId);
            if (page != null) {
                getServer().removePage(page);
            }
            getServer().logout();
        }


        public String renderPage(String spaceKey, String pageId, String content) throws ConfluenceException {
            getServer().login();
            String renderedContent = getServer().renderPage(spaceKey, pageId, content);
            getServer().logout();
            return renderedContent;
        }


        public void removeLabelByName(String objectId, String labelName) throws ConfluenceException {
            getServer().login();
            getServer().removeLabelByName(labelName, objectId);
            getServer().logout();
        }


        public void removeLabelByName(String labelName) throws ConfluenceException {
            getServer().login();
            getServer().removeLabelByName(labelName);
            getServer().logout();
        }


        public Page createPage(String spaceKey,
                               String parentId,
                               String title, Map<String, String> metadata,
                               String content) throws ConfluenceException {
            getServer().login();
            Page newPage = new Page();
            newPage.setSpaceKey(spaceKey);
            newPage.setTitle(title);
            if (parentId != null) {
                newPage.setParentPageId(parentId);
            }
            newPage.setContent(buildPageContent(metadata, content));
            Page page = getServer().storePage(newPage);
            getServer().logout();
            return page;
        }


        public Page updatePage(Page page) throws ConfluenceException {
            getServer().login();
            Page newPage = getServer().storePage(page);
            getServer().logout();
            return newPage;
        }


        public void updatePageContent(Page page, Map<String, String> metadata, String content)
              throws ConfluenceException {
            getServer().login();
            page.setContent(buildPageContent(metadata, content));
            getServer().storePage(page);
            getServer().logout();
        }


        public void attachFile(File file, String pageId) throws IOException, ConfluenceException {
            attachFile(file, pageId, null);
        }


        public void attachFile(File file, String pageId, String newName)
              throws IOException, ConfluenceException {
            ConfluenceUploader confluenceUploader = new ConfluenceUploader(session);
            confluenceUploader.setTimeout(configuration.getTimeout());
            confluenceUploader.setRetryCount(configuration.getRetryCount());
            confluenceUploader.setRetryDelay(configuration.getRetryDelay());
            try {
                confluenceUploader.login();
                confluenceUploader.upload(file, pageId, newName);
            }
            finally {
                confluenceUploader.logout();
            }
        }


        public boolean moveAttachment(String oldPageId, String oldName, String newPageId, String newName)
              throws ConfluenceException {
            getServer().login();
            boolean result = getServer().moveAttachment(oldPageId, oldName, newPageId, newName);
            getServer().logout();
            return result;
        }


        public List<Attachment> getAttachments(String pageId) throws ConfluenceException {
            getServer().login();
            List<Attachment> attachments = getServer().getAttachments(pageId);
            getServer().logout();
            return attachments;
        }


        public File downloadAttachment(String pageId, String attachmentName, String directory)
              throws IOException, ConfluenceException {
            getServer().login();
            File file = getServer().downloadAttachment(pageId, attachmentName, directory);
            getServer().logout();
            return file;
        }


        public BlogEntry createBlogEntry(String spaceKey, String title, Map<String, String> metadata, String content)
              throws ConfluenceException {
            getServer().login();
            BlogEntry newBlogEntry = new BlogEntry();
            newBlogEntry.setSpaceKey(spaceKey);
            newBlogEntry.setTitle(title);
            newBlogEntry.setContent(buildPageContent(metadata, content));
            BlogEntry page = getServer().storeBlogEntry(newBlogEntry);
            getServer().logout();
            return page;
        }


        public BlogEntry getBlogEntry(String spaceKey, String blogEntryTitle) throws ConfluenceException {
            getServer().login();
            BlogEntry blogEntry = null;
            try {
                blogEntry = getServer().getBlogEntry(spaceKey, blogEntryTitle);
            }
            catch (ConfluenceException exception) {
                if (!exception.getLocalizedMessage().contains("does not exist")) {
                    throw exception;
                }
            }
            getServer().logout();
            return blogEntry;
        }


        public void deleteBlogEntry(String blogId) throws ConfluenceException {
            getServer().login();
            BlogEntry blogEntry = getServer().getBlogEntry(blogId);
            if (blogEntry != null) {
                getServer().removeBlogEntry(blogEntry);
            }
            getServer().logout();
        }


        private String buildPageContent(Map<String, String> metadata, String content) {
            if (metadata != null) {
                if (content != null) {
                    return buildMetadata(metadata) + "\n" + content;
                }

                return buildMetadata(metadata);
            }

            if (content != null) {
                return content;
            }

            return "";
        }


        private String buildMetadata(Map<String, String> metadata) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{metadata-list}\n");
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                stringBuilder
                      .append("||")
                      .append(entry.getKey())
                      .append("|")
                      .append(entry.getValue())
                      .append("|\n");
            }
            stringBuilder.append("{metadata-list}");
            return stringBuilder.toString();
        }
    }

    public abstract class RunWithRetry<T> {
        protected ConfluenceServer server;


        protected RunWithRetry(ConfluenceServer server) {
            this.server = server;
        }


        public void run() throws ConfluenceException {
            server.login();
            List<T> params = prepare();
            for (T param : params) {
                try {
                    iterate(param);
                }
                catch (ConfluenceException ex) {
                    if (ex.getLocalizedMessage().contains("Call login() to open a new session")) {
                        server.login();
                        iterate(param);
                    }
                    else {
                        throw ex;
                    }
                }
            }
            server.logout();
        }


        public abstract List<T> prepare() throws ConfluenceException;


        public abstract void iterate(T param) throws ConfluenceException;
    }

    public class ConfluencePluginConfiguration {
        private String serverUrl;
        private String user;
        private String password;
        private int timeout;
        private int retryCount = 2;
        private long retryDelay = 1000;


        public String getUser() {
            return user;
        }


        public void setUser(String user) {
            this.user = user;
        }


        public String getPassword() {
            return password;
        }


        public void setPassword(String password) {
            this.password = password;
        }


        public String getServerUrl() {
            return serverUrl;
        }


        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }


        public int getTimeout() {
            return timeout;
        }


        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }


        public int getRetryCount() {
            return retryCount;
        }


        public void setRetryCount(int retryCount) {
            this.retryCount = retryCount;
        }


        public long getRetryDelay() {
            return retryDelay;
        }


        public void setRetryDelay(long retryDelay) {
            this.retryDelay = retryDelay;
        }
    }
}
