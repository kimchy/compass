package org.compass.gps.device.jpa.queryprovider;

import org.compass.gps.device.jpa.entities.EntityInformation;
import org.hibernate.Criteria;
import org.hibernate.ejb.HibernateEntityManager;

/**
 * @author kimchy
 */
public class HibernateJpaQueryProvider extends DefaultJpaQueryProvider {

    public HibernateJpaQueryProvider(Class<?> entityClass, String entityName) {
        super(entityClass, entityName);
    }

    public HibernateJpaQueryProvider(String selectQuery) {
        super(selectQuery);
    }

    /**
     * Returns a Hibernate criteria if no <code>selectQuery</code> has been set.
     */
    public Criteria createCriteria(HibernateEntityManager entityManager, EntityInformation entityInformation) {
        if (!isUsingDefaultSelectQuery()) {
            return null;
        }
        return entityManager.getSession().createCriteria(entityInformation.getEntityClass());
    }

}
