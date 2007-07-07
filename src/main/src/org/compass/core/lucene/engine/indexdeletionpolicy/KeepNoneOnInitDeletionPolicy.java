package org.compass.core.lucene.engine.indexdeletionpolicy;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.index.IndexCommitPoint;
import org.apache.lucene.index.IndexDeletionPolicy;

/**
 * An index deletion policy that deletes all commits when initialized.
 *
 * @author kimchy
 * @see org.compass.core.lucene.engine.indexdeletionpolicy.IndexDeletionPolicyFactory
 */
public class KeepNoneOnInitDeletionPolicy implements IndexDeletionPolicy {

    public void onInit(List commits) throws IOException {
        Iterator it = commits.iterator();
        while (it.hasNext()) {
            ((IndexCommitPoint) it.next()).delete();
        }
    }

    public void onCommit(List commits) throws IOException {
        int size = commits.size();
        // Delete all but last one:
        for (int i = 0; i < size - 1; i++) {
            ((IndexCommitPoint) commits.get(i)).delete();
        }
    }
}