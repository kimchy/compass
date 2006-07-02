package org.compass.core.converter.xsem;

import org.compass.core.config.ConfigurationException;

/**
 * Some actual implementations of {@link XmlContentConverter} might not suppoer a certain
 * wrapper strategy (probably some will have problem with a singleton wrapper). This gives
 * the actual implementation a chance to vote on which wrapper implementation it supports.
 *
 * @author kimchy
 */
public interface SupportsXmlContentWrapper {

    public static class NotSupportedXmlContentWrapperException extends ConfigurationException {

        public NotSupportedXmlContentWrapperException(XmlContentConverter converter, String wrapper) {
            super("Xml content converter [" + converter.getClass().getName() + "] does not support wrapper mode [" + wrapper + "]");
        }
    }

    /**
     * Returns <code>true</code> if the give wrapper strategy is supported, <code>false</code> otherwise.
     */
    boolean supports(String wrapper);
}
