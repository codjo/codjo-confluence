package net.codjo.confluence.plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import net.codjo.confluence.Attachment;
import net.codjo.confluence.BlogEntry;
import net.codjo.confluence.ConfluenceException;
import net.codjo.confluence.Label;
import net.codjo.confluence.Page;
import net.codjo.confluence.PageSummary;
import net.codjo.confluence.SearchCriteria;
import net.codjo.confluence.SearchResult;

public interface ConfluenceOperations {
    Page getPage(String spaceKey, String pageTitle) throws ConfluenceException;

    Page getPage(String pageId) throws ConfluenceException;

    List<Page> getPages(List<String> pageIds) throws ConfluenceException;

    Page createPage(String spaceKey, String parentId, String title, Map<String, String> metadata, String content) throws ConfluenceException;

    Page updatePage(Page page) throws ConfluenceException;

    void updatePageContent(Page page, Map<String, String> metadata, String content) throws ConfluenceException;

    void attachFile(File file, String pageId) throws IOException, ConfluenceException;

    void attachFile(File file, String pageId, String newName) throws IOException, ConfluenceException;

    boolean moveAttachment(String oldPageId, String oldName, String newPageId, String newName) throws ConfluenceException;

    List<Attachment> getAttachments(String pageId) throws ConfluenceException;

    File downloadAttachment(String pageId, String attachmentName, String directory) throws IOException, ConfluenceException;

    List<Page> getPagesByLabel(String spaceKey, String label) throws ConfluenceException;

    List<BlogEntry> getBlogEntriesByLabel(String spaceKey, String label) throws ConfluenceException;

    List<PageSummary> getChildren(String pageId) throws ConfluenceException;

    List<SearchResult> searchByCriteria(String spaceKey, SearchCriteria searchCriteria, int maxResults) throws ConfluenceException;

    List<SearchResult> searchByCriteria(String spaceKey, String type, SearchCriteria searchCriteria, int maxResults) throws ConfluenceException;

    List<Page> searchPagesByCriteria(String spaceKey, SearchCriteria searchCriteria, int maxResults) throws ConfluenceException;

    List<Label> getLabelsById(String pageId) throws ConfluenceException;

    void addLabel(String objectId, String label) throws ConfluenceException;

    void deletePage(String pageId) throws ConfluenceException;

    String renderPage(String spaceKey, String pageId, String content) throws ConfluenceException;

    void removeLabelByName(String objectId, String labelName) throws ConfluenceException;

    void removeLabelByName(String labelName) throws ConfluenceException;

    BlogEntry createBlogEntry(String spaceKey, String title, Map<String, String> metadata, String content) throws ConfluenceException;

    BlogEntry getBlogEntry(String spaceKey, String blogEntryTitle) throws ConfluenceException;

    void deleteBlogEntry(String blogId) throws ConfluenceException;
}
