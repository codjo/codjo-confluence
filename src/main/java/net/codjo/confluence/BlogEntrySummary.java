package net.codjo.confluence;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 * http://confluence.atlassian.com/display/DOC/Remote+API+Specification#RemoteAPISpecification-PageSummary
 */
public class BlogEntrySummary extends ConfluenceObject {
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String URL = "url";
    private static final String LOCKS = "locks";
    private static final String SPACE = "space";
    private static final String PUBLISH_DATE = "publishDate";

    public BlogEntrySummary(Map<String, Object> pageEntry) {
        super(new HashMap<String, String>());

        for (String key : pageEntry.keySet()) {
            final Object value = pageEntry.get(key);
            if (value instanceof Date) {
                setData(key, ConfluenceObject.createDateFormat().format(value));
            }
            else {
                setData(key, (String)value);
            }
        }
    }


    public String getId() {
        return getData(ID);
    }


    public String getTitle() {
        return getData(TITLE);
    }


    public String getUrl() {
        return getData(URL);
    }


    public String getLocks() {
        return getData(LOCKS);
    }


    public String getSpaceKey() {
        return getData(SPACE);
    }


    public String getPublishDate() {
        return getData(PUBLISH_DATE);
    }


    @Override
    public String toString() {
        return getTitle();
    }
}
