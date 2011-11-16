package net.codjo.confluence;

import java.util.HashMap;
import java.util.Map;

/**
 * http://confluence.atlassian.com/display/DOC/Remote+API+Specification#RemoteAPISpecification-PageSummary
 */
public class Label extends ConfluenceObject {
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String OWNER = "owner";
    private static final String NAMESPACE = "namespace";


    public Label(String id, String name) {
        super(new HashMap<String, String>());
        setData(ID, id);
        setData(NAME, name);
    }


    public Label(Map<String, String> labelEntry) {
        super(labelEntry);
    }


    public String getId() {
        return getData(ID);
    }


    public String getName() {
        return getData(NAME);
    }


    public String getOwner() {
        return getData(OWNER);
    }


    public String getNamespace() {
        return getData(NAMESPACE);
    }


    @Override
    public String toString() {
        return getName();
    }
}
