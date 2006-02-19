package org.compass.annotations.test.analyzer;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.CompassHits;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassSettings;

/**
 * @author kimchy
 */
public class AnalyzerTests extends AbstractAnnotationsTestCase {

    private static final String TEXT = "the quick brown fox jumped over the lazy dogs";

    protected void addSettings(CompassSettings settings) {
        settings.setGroupSettings(LuceneEnvironment.Analyzer.PREFIX, "simple",
                new String[] { LuceneEnvironment.Analyzer.TYPE },
                new String[] { LuceneEnvironment.Analyzer.CoreTypes.SIMPLE });
        settings.setGroupSettings(LuceneEnvironment.Analyzer.PREFIX, LuceneEnvironment.Analyzer.SEARCH_GROUP,
                new String[] { LuceneEnvironment.Analyzer.TYPE },
                new String[] { LuceneEnvironment.Analyzer.CoreTypes.SIMPLE });
    }

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class);
    }

    public void testFieldAnalyzer() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = TEXT;
        a.analyzer = "simple";
        session.save(a);

        CompassHits hits = session.find("value:the");
        assertEquals(1, hits.getLength());

        a = new A();
        a.id = 1;
        a.value = TEXT;
        a.analyzer = null;
        try {
            session.save(a);
            fail();
        } catch (SearchEngineException e) {

        }

        tr.commit();
        session.close();
    }

}
