package org.compass.core.lucene.engine.indexdeletionpolicy;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.index.IndexCommitPoint;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.store.Directory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.LuceneEnvironment;

/**
 * An index deletion policy that keeps all commits that have not expired. Expiration time (in seconds)
 * can be set using {@link org.compass.core.lucene.LuceneEnvironment.IndexDeletionPolicy.ExpirationTime}
 * settings.
 *
 * @author kimchy
 * @see org.compass.core.lucene.engine.indexdeletionpolicy.IndexDeletionPolicyFactory
 */
public class ExpirationTimeDeletionPolicy implements IndexDeletionPolicy, DirectoryConfigurable, CompassConfigurable {

    private double expirationTimeSeconds;

    private Directory dir;

    public void setDirectory(Directory directory) {
        this.dir = directory;
    }

    /**
     * Configures the expiration time of the deletion policy. Can be controlled using
     * {@link LuceneEnvironment.IndexDeletionPolicy.ExpirationTime#EXPIRATION_TIME_IN_SECONDS}.
     *
     * <p>Defaults to the cache invalidation time * 3.
     */
    public void configure(CompassSettings settings) throws CompassException {
        double defaultValue = ((double) settings.getSettingAsLong(LuceneEnvironment.SearchEngineIndex.CACHE_INTERVAL_INVALIDATION, LuceneEnvironment.SearchEngineIndex.DEFAULT_CACHE_INTERVAL_INVALIDATION)) / 1000;
        this.expirationTimeSeconds = settings.getSettingAsDouble(LuceneEnvironment.IndexDeletionPolicy.ExpirationTime.EXPIRATION_TIME_IN_SECONDS,
                defaultValue * 3);
    }

    public void onInit(List commits) throws IOException {
        onCommit(commits);
    }

    public void onCommit(List commits) throws IOException {
        IndexCommitPoint lastCommit = (IndexCommitPoint) commits.get(commits.size() - 1);

        // Any commit older than expireTime should be deleted:
        double expireTime = dir.fileModified(lastCommit.getSegmentsFileName()) / 1000.0 - expirationTimeSeconds;

        Iterator it = commits.iterator();

        while (it.hasNext()) {
            IndexCommitPoint commit = (IndexCommitPoint) it.next();
            double modTime = dir.fileModified(commit.getSegmentsFileName()) / 1000.0;
            if (commit != lastCommit && modTime < expireTime) {
                commit.delete();
            }
        }
    }

    public String toString() {
        return super.toString() + " expirationTimeInSeconds [" + expirationTimeSeconds + "]";
    }
}
