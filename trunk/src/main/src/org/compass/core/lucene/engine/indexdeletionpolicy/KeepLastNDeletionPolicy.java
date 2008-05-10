package org.compass.core.lucene.engine.indexdeletionpolicy;

import java.util.List;

import org.apache.lucene.index.IndexCommitPoint;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.LuceneEnvironment;

/**
 * An index deletion policy that keepds the last "N" commits. Configured using
 * {@link org.compass.core.lucene.LuceneEnvironment.IndexDeletionPolicy.KeepLastN} settings with
 * the number of last commits to save. Defaults to <code>10</code>.
 *
 * @author kimchy
 * @see org.compass.core.lucene.engine.indexdeletionpolicy.IndexDeletionPolicyFactory
 */
public class KeepLastNDeletionPolicy implements IndexDeletionPolicy, CompassConfigurable {

    private int numToKeep;

    public void configure(CompassSettings settings) throws CompassException {
        numToKeep = settings.getSettingAsInt(LuceneEnvironment.IndexDeletionPolicy.KeepLastN.NUM_TO_KEEP, 10);
    }

    public void onInit(List commits) {
        onCommit(commits);
    }

    public void onCommit(List commits) {
        int size = commits.size();
        for (int i = 0; i < size - numToKeep; i++) {
            ((IndexCommitPoint) commits.get(i)).delete();
        }
    }

    public String toString() {
        return super.toString() + " numToKeep [" + numToKeep + "]";
    }
}
