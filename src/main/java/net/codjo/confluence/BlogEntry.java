package net.codjo.confluence;
import java.util.HashMap;
import java.util.Map;
/**
 *
 */
public class BlogEntry extends ConfluenceObject {
    private static final String ID = "id";
    private static final String SPACE = "space";
    private static final String TITLE = "title";
    private static final String URL = "url";
    private static final String VERSION = "version";
    private static final String CONTENT = "content";
    private static final String LOCKS = "locks";

    public BlogEntry(Map<String, String> confluenceData) {
        super(confluenceData);
    }


    public BlogEntry() {
        this(new HashMap<String, String>());
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


    public String getSpaceKey() {
        return getData(SPACE);
    }


    public void setSpaceKey(String spaceKey) {
        setData(SPACE, spaceKey);
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


    public Integer getLocks() {
        return getDataAsInteger(LOCKS);
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

        BlogEntry blogEntry = (BlogEntry)object;
        return !(this.getId() != null ? !this.getId().equals(blogEntry.getId()) : blogEntry.getId() != null);
    }


    @Override
    public int hashCode() {
        String id = getId();
        return id == null ? -1 : Integer.valueOf(id);
    }
}

