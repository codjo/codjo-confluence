package net.codjo.confluence;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
/**
 *
 */
public abstract class ConfluenceObject {
    static final String CONFLUENCE_DATE_PATTERN = "EEE MMM dd HH:mm:ss zzz yyyy";
    private static final DateFormat dateTimeFormat = createDateFormat();

    private final Map<String, String> confluenceData;


    protected ConfluenceObject(Map<String, String> confluenceData) {
        this.confluenceData = confluenceData;
    }


    protected String getData(String key) {
        return confluenceData.get(key);
    }


    protected Integer getDataAsInteger(String key) {
        return Integer.valueOf(confluenceData.get(key));
    }


    protected Date getDataAsDateTime(String key) {
        try {
            return dateTimeFormat.parse(getData(key));
        }
        catch (Exception e) {
            throw new InternalError("Format de date invalide " + getData(key));
        }
    }


    protected Boolean getDataAsBoolean(String key) {
        return Boolean.valueOf(getData(key));
    }


    protected void setData(String key, String value) {
        confluenceData.put(key, value);
    }


    public Map<String, String> getConfluenceStructure() {
        return confluenceData;
    }


    static SimpleDateFormat createDateFormat() {
        return new SimpleDateFormat(CONFLUENCE_DATE_PATTERN, Locale.US);
    }
}
