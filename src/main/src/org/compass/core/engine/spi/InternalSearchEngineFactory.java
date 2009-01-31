package org.compass.core.engine.spi;

import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.transaction.context.TransactionContext;

/**
 * @author kimchy
 */
public interface InternalSearchEngineFactory extends SearchEngineFactory {

    /**
     * Internal method. Should be called before perfoming any operations with the
     * search engine factory.
     */
    void setTransactionContext(TransactionContext transactionContext);

    void start();
    
    void stop();


    boolean isDebug();
}
