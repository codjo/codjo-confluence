package net.codjo.confluence;
import net.codjo.confluence.SearchCriteria.Match;
import java.util.List;
import java.util.ArrayList;
/**
 *
 */
public class Criteria {
    private Match match;

    private String key;

    private List<String> values = new ArrayList<String>(2);


    public String getKey() {
        return key;
    }


    public void setKey(String key) {
        this.key = key;
    }


    public Match getMatch() {
        return match;
    }


    public void setMatch(Match match) {
        this.match = match;
    }


    public List<String> getValues() {
        return values;
    }


    public void setValues(List<String> values) {
        this.values = values;
    }
}
