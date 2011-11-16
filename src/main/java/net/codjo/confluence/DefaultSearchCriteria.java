package net.codjo.confluence;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
/**
 *
 */
public class DefaultSearchCriteria extends Observable implements SearchCriteria {

    private List<Criteria> criteriaList = new ArrayList<Criteria>();


    public SearchCriteria withCriteria(String key, String value, Match match) {
        addCriteria(key, match, value);
        notifyObservers();
        return this;
    }


    public SearchCriteria withRangeCriteria(String key, String from, String to) {
        addCriteria(key, Match.range, from, to);
        notifyObservers();
        return this;
    }


    public SearchCriteria removeCriteria(String key) {
        for (Criteria criteria : criteriaList) {
            if (criteria.getKey().equals(key)) {
                criteriaList.remove(criteria);
                break;
            }
        }

        notifyObservers();
        return this;
    }


    @Override
    public void notifyObservers() {
        setChanged();
        super.notifyObservers();
    }


    public String toLuceneString() {
        StringBuilder builder = new StringBuilder("");

        for (Criteria criteria : criteriaList) {

            builder.append("(");

            switch (criteria.getMatch()) {
                case exact:
                case contains:
                    builder
                          .append("\"")
                          .append(criteria.getKey())
                          .append(":").append(criteria.getValues().get(0))
                          .append("\"");
                    break;
                case range:
                    builder
                          .append(criteria.getKey())
                          .append(":[")
                          .append(criteria.getValues().get(0))
                          .append(" TO ")
                          .append(criteria.getValues().get(1))
                          .append("]");
                    break;
            }

            builder
                  .append(")")
                  .append(" AND ");
        }
        String luceneString = "";

        if (criteriaList.size() > 0) {
            int index = builder.lastIndexOf(" AND ");
            luceneString = builder.delete(index, builder.length()).toString();
        }

        return luceneString;
    }


    private void addCriteria(String key, Match match, String... value) {
        removeCriteria(key);
        Criteria criteria = new Criteria();
        criteria.setKey(key);
        criteria.setMatch(match);
        criteria.setValues(Arrays.asList(value));
        criteriaList.add(criteria);
    }


    public boolean isEmpty() {
        return criteriaList.isEmpty();
    }

    public List<Criteria> getCriteriaList() {
        return criteriaList;
    }
}
