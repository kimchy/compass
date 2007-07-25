package org.compass.gps.device.jpa.support;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.EntityManagerFactory;

import org.compass.core.util.ClassUtils;
import org.compass.gps.device.jpa.JpaGpsDeviceException;
import org.compass.gps.device.jpa.NativeJpaExtractor;

/**
 * @author kimchy
 */
public abstract class NativeJpaHelper {

    public static interface NativeJpaCallback<T> {

        T onHibernate();

        T onTopLinkEssentials();

        T onOpenJPA();

        T onUnknown();
    }

    public static <T> T detectNativeJpa(EntityManagerFactory emf, NativeJpaCallback<T> callback) throws JpaGpsDeviceException {
        EntityManagerFactory nativeEmf = extractNativeJpa(emf);

        Set interfaces = ClassUtils.getAllInterfacesAsSet(nativeEmf);
        Set<String> interfacesAsStrings = new HashSet<String>();
        for (Object anInterface : interfaces) {
            interfacesAsStrings.add(((Class) anInterface).getName());
        }
        interfacesAsStrings.add(nativeEmf.getClass().getName());

        T retVal;
        if (interfacesAsStrings.contains("org.hibernate.ejb.HibernateEntityManagerFactory")) {
            retVal = callback.onHibernate();
        } else
        if (interfacesAsStrings.contains("oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerFactoryImpl")) {
            retVal = callback.onTopLinkEssentials();
        } else if (interfacesAsStrings.contains("org.apache.openjpa.persistence.OpenJPAEntityManagerFactory")) {
            retVal = callback.onOpenJPA();
        } else {
            retVal = callback.onUnknown();
        }
        return retVal;
    }

    public static EntityManagerFactory extractNativeJpa(EntityManagerFactory emf) {
        Set interfaces = ClassUtils.getAllInterfacesAsSet(emf);
        Set<String> interfacesAsStrings = new HashSet<String>();
        for (Object anInterface : interfaces) {
            interfacesAsStrings.add(((Class) anInterface).getName());
        }
        interfacesAsStrings.add(emf.getClass().getName());

        NativeJpaExtractor extractor = null;
        if (interfacesAsStrings.contains("org.springframework.orm.jpa.EntityManagerFactoryInfo")) {
            try {
                extractor = (NativeJpaExtractor)
                        ClassUtils.forName("org.compass.spring.device.jpa.SpringNativeJpaExtractor").newInstance();
            } catch (Exception e) {
                throw new JpaGpsDeviceException("Failed to load/create spring native extractor", e);
            }
        } else if (interfacesAsStrings.contains("org.jboss.ejb3.entity.InjectedEntityManagerFactory")) {
            try {
                extractor = (NativeJpaExtractor)
                        ClassUtils.forName("org.compass.jboss.device.jpa.JBossNativeHibernateJpaExtractor").newInstance();
            } catch (Exception e) {
                throw new JpaGpsDeviceException("Failed to load/create JBoss native extractor", e);
            }
        }
        // possible else if ...

        EntityManagerFactory nativeEmf = emf;
        if (extractor != null) {
            nativeEmf = extractor.extractNative(emf);
            // recursivly call in order to find
            nativeEmf = extractNativeJpa(nativeEmf);
        }
        return nativeEmf;
    }
}
