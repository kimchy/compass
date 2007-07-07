package org.compass.core.lucene.engine.indexdeletionpolicy;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.IndexDeletionPolicy;

/**
 * An index deletion policy that does not delete any commits, old or new.
 *
 * @author kimchy
 * @see org.compass.core.lucene.engine.indexdeletionpolicy.IndexDeletionPolicyFactory
 */
public class KeepAllDeletionPolicy implements IndexDeletionPolicy {

    public void onInit(List commits) throws IOException {

    }

    public void onCommit(List commits) throws IOException {

    }
}
