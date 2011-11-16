package net.codjo.confluence;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Page extends ConfluenceObject {
    private static final String ID = "id";
    private static final String SPACE = "space";
    private static final String PARENT_ID = "parentId";
    private static final String TITLE = "title";
    private static final String URL = "url";
    private static final String PERMISSIONS = "permissions";
    private static final String VERSION = "version";
    private static final String CONTENT = "content";
    private static final String CREATED = "created";
    private static final String CREATOR = "creator";
    private static final String MODIFIED = "modified";
    private static final String MODIFIER = "modifier";
    private static final String HOME_PAGE = "homePage";
    private static final String LOCKS = "locks";
    private static final String CONTENT_STATUS = "contentStatus";
    private static final String CURRENT = "current";

    public Page() {
        super(new HashMap<String, String>());
    }


    public Page(String spaceKey, String title) {
        this();
        setSpaceKey(spaceKey);
        setTitle(title);
    }


    Page(Map<String, String> pageEntry) {
        super(pageEntry);
    }


    public String getId() {
        return getData(ID);
    }


    public String getTitle() {
        return getData(TITLE);
    }


    public void setTitle(String title) {
        setData(TITLE, title);
    }


    public String getUrl() {
        return getData(URL);
    }


    public String getPermissions() {
        return getData(PERMISSIONS);
    }


    public String getSpaceKey() {
        return getData(SPACE);
    }


    public void setSpaceKey(String spaceKey) {
        setData(SPACE, spaceKey);
    }


    public String getParentPageId() {
        return getData(PARENT_ID);
    }


    public void setParentPageId(String pageId) {
        setData(PARENT_ID, pageId);
    }


    public String getContent() {
        return getData(CONTENT);
    }


    public void setContent(String content) {
        setData(CONTENT, content);
    }


    public Integer getVersion() {
        return getDataAsInteger(VERSION);
    }


    public Date getCreated() throws ParseException {
        return getDataAsDateTime(CREATED);
    }


    public String getCreator() {
        return getData(CREATOR);
    }


    public Date getModified() throws ParseException {
        return getDataAsDateTime(MODIFIED);
    }


    public String getModifier() {
        return getData(MODIFIER);
    }


    public Boolean isHomePage() {
        return getDataAsBoolean(HOME_PAGE);
    }


    public Integer getLocks() {
        return getDataAsInteger(LOCKS);
    }


    public String getContentStatus() {
        return getData(CONTENT_STATUS);
    }


    public Boolean isCurrent() {
        return getDataAsBoolean(CURRENT);
    }


    @Override
    public String toString() {
        return getTitle();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Page page = (Page) object;
        return !(this.getId() != null ? !this.getId().equals(page.getId()) : page.getId() != null);
    }


    @Override
    public int hashCode() {
        String id = getId();
        return id == null ? -1 : Integer.valueOf(id);
    }
}
