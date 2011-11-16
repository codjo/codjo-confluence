package net.codjo.confluence;
import java.util.HashMap;
import java.util.Map;
/**
 *
 */
public class Space extends ConfluenceObject {
    private static final String KEY = "key";
    private static final String NAME = "name";


    Space(Map<String, String> spaceEntry) {
        super(spaceEntry);
    }


    public Space(String spaceKey) {
        super(new HashMap<String, String>());
        setData(KEY, spaceKey);
        setData(NAME, "n/a(" + spaceKey + ")");
    }


    public String getKey() {
        return getData(KEY);
    }


    public String getName() {
        return getData(NAME);
    }


    @Override
    public String toString() {
        return getName();
    }
}
