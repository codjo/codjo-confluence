package net.codjo.confluence;
import java.util.Map;

public class SearchResult extends ConfluenceObject {
    private static final String TITLE = "title";
    private static final String URL = "url";
    private static final String EXCERPT = "excerpt";
    private static final String TYPE = "type";
    private static final String ID = "id";


    protected SearchResult(Map<String, String> confluenceData) {
        super(confluenceData);
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


    public String getExcerpt() {
        return getData(EXCERPT);
    }


    public String getType() {
        return getData(TYPE);
    }
}
