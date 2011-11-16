package net.codjo.confluence;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
/**
 *
 */
public class Attachment extends ConfluenceObject {
    private static final String ID = "id";
    private static final String PAGE_ID = "pageId";
    private static final String TITLE = "title";
    private static final String FILE_NAME = "fileName";
    private static final String FILE_SIZE = "fileSize";
    private static final String CONTENT_TYPE = "contentType";
    private static final String CREATED = "created";
    private static final String CREATOR = "creator";
    private static final String URL = "url";
    private static final String COMMENT = "comment";


    public Attachment(Map<String, String> confluenceData) {
        super(confluenceData);
    }


    public String getId() {
        return getData(ID);
    }


    public String getPageId() {
        return getData(PAGE_ID);
    }


    public String getTitle() {
        return getData(TITLE);
    }


    public String getFileName() {
        return getData(FILE_NAME);
    }


    public String getFileSize() {
        return getData(FILE_SIZE);
    }


    public String getContentType() {
        return getData(CONTENT_TYPE);
    }


    public Date getCreated() throws ParseException {
        return getDataAsDateTime(CREATED);
    }


    public String getCreator() {
        return getData(CREATOR);
    }


    public String getUrl() {
        return getData(URL);
    }


    public String getComment() {
        return getData(COMMENT);
    }
}
