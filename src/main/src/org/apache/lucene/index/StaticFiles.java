package org.apache.lucene.index;

import java.util.HashSet;
import java.util.Set;

import org.compass.core.lucene.engine.manager.DefaultLuceneSearchEngineIndexManager;
import org.compass.core.lucene.engine.spellcheck.DefaultLuceneSpellCheckManager;

/**
 * @author kimchy
 */
public class StaticFiles {

    private static final Set<String> staticFiles;

    static {
        staticFiles = new HashSet<String>();
        staticFiles.add("segments.gen");
        staticFiles.add(DefaultLuceneSearchEngineIndexManager.CLEAR_CACHE_NAME);
        staticFiles.add(DefaultLuceneSpellCheckManager.SPELL_CHECK_VERSION_FILENAME);
    }

    public static boolean isStaticFile(String name) {
        return staticFiles.contains(name);
    }
}
