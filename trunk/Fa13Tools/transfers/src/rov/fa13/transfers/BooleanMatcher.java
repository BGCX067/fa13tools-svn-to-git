package rov.fa13.transfers;

import ca.odell.glazedlists.Filterator;
import ca.odell.glazedlists.matchers.Matcher;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Oleg Rachaev
 */
public class BooleanMatcher implements Matcher {

    private Filterator filterator;

    public BooleanMatcher(Filterator filterator) {
        this.filterator = filterator;
    }

    @Override
    public boolean matches(Object item) {
        List filteredValues = new ArrayList();
        filterator.getFilterValues(filteredValues, item);

        for (Object object : filteredValues) {

            if (!(object instanceof Boolean)) {
                return false;
            } else {
                Boolean b = (Boolean) object;
                if (!b.booleanValue()) {
                    return false;
                }
            }
        }
        return true;
    }
}