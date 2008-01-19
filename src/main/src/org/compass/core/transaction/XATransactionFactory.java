package org.compass.core.transaction;

import org.compass.core.CompassException;
import org.compass.core.CompassTransaction;
import org.compass.core.spi.InternalCompassSession;

/**
 * Factory for {@link XATransaction}s.
 *
 * @author kimchy
 * @see XATransaction
 */
public class XATransactionFactory extends AbstractJTATransactionFactory {

    public InternalCompassTransaction doBeginTransaction(InternalCompassSession session,
                                                         CompassTransaction.TransactionIsolation transactionIsolation) throws CompassException {
        XATransaction tx = new XATransaction(getUserTransaction(), this);
        tx.begin(session, getTransactionManager(), transactionIsolation);
        return tx;
    }

    protected InternalCompassTransaction doContinueTransaction(InternalCompassSession session) throws CompassException {
        XATransaction tx = new XATransaction(getUserTransaction(), this);
        tx.join();
        return tx;
    }

}
