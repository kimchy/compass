package org.compass.core.test.support.search;

import org.compass.core.Compass;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.support.search.CompassSearchCommand;
import org.compass.core.support.search.CompassSearchHelper;
    
/**
 * This file is named Abstract* in spite of the fact we can create its instance
 * but we don't want to process this call by JUnit during testing.
 */
public class AbstractSearchHelper extends CompassSearchHelper {
    
    private int expectedFrom;
    private int expectedSize;
    
    public AbstractSearchHelper(Compass compass) { super(compass); }
    
    public AbstractSearchHelper(Compass compass, Integer pageSize) {
	    super(compass, pageSize);
    }
    
    public int getExpectedSize() {
	    return expectedSize;
    }
    
    public int getExpectedFrom() {
	    return expectedFrom;
    }

    protected void doProcessBeforeDetach(
    	CompassSearchCommand searchCommand, CompassSession session,
        CompassHits hits, int from, int size) {
            
            expectedFrom = from;
            expectedSize = size;
    }
}
