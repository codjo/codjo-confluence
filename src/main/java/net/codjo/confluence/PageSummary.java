package net.codjo.confluence;
import java.util.HashMap;
import java.util.Map;
/**
 * http://confluence.atlassian.com/display/DOC/Remote+API+Specification#RemoteAPISpecification-PageSummary
 */
public class PageSummary extends ConfluenceObject {
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String URL = "url";
    private static final String PERMISSIONS = "permissions";
    private static final String SPACE = "space";
    private static final String PARENT_ID = "parentId";


    public PageSummary(String id, String title) {
        super(new HashMap<String, String>());
        setData(ID, id);
        setData(TITLE, title);
    }


    public PageSummary(Map<String, String> pageEntry) {
        super(pageEntry);
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


    public String getPermissions() {
        return getData(PERMISSIONS);
    }


    public String getSpaceKey() {
        return getData(SPACE);
    }


    public String getParentPageId() {
        return getData(PARENT_ID);
    }


    @Override
    public String toString() {
        return getTitle();
    }
}
