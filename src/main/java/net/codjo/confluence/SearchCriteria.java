package net.codjo.confluence;
import java.util.List;
/**
 *
 */
public interface SearchCriteria {

    static enum Match {
        exact,
        range,
        contains
    }

    SearchCriteria withCriteria(String key, String value, Match match);

    SearchCriteria withRangeCriteria(String key, String from, String to);

    SearchCriteria removeCriteria(String key);

    String toLuceneString();

    boolean isEmpty();

    List<Criteria> getCriteriaList();
}
