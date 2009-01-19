/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.core.config.builder;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.ConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author kimchy
 */
public abstract class AbstractXmlConfigurationBuilder extends AbstractInputStreamConfigurationBuilder {

    private class SimpleSaxErrorHandler implements ErrorHandler {

        private final Log log;

        /**
         * Create a new SimpleSaxErrorHandler for the given
         * Commons Logging logger instance.
         */
        public SimpleSaxErrorHandler(Log log) {
            this.log = log;
        }

        public void warning(SAXParseException ex) throws SAXException {
            log.warn("Ignored XML validation warning [" + ex.getMessage() + "]", ex);
        }

        public void error(SAXParseException ex) throws SAXException {
            throw ex;
        }

        public void fatalError(SAXParseException ex) throws SAXException {
            throw ex;
        }

    }


    protected void doConfigure(InputStream is, String resourceName, CompassConfiguration config) throws ConfigurationException {
        InputSource inputSource = new InputSource(is);
        try {
            DocumentBuilderFactory factory = createDocumentBuilderFactory();
            DocumentBuilder builder = createDocumentBuilder(factory);
            Document doc = builder.parse(inputSource);
            doProcess(doc, config);
        } catch (ParserConfigurationException ex) {
            throw new ConfigurationException(
                    "Parser configuration exception parsing XML from [" + resourceName + "]", ex);
        }
        catch (SAXParseException ex) {
            throw new ConfigurationException(
                    "Line [" + ex.getLineNumber() + "] in XML document from [" + resourceName + "] is invalid", ex);
        }
        catch (SAXException ex) {
            throw new ConfigurationException("XML document from [" + resourceName + "] is invalid", ex);
        }
        catch (IOException ex) {
            throw new ConfigurationException("IOException parsing XML document from [" + resourceName + "]", ex);
        }

    }

    protected abstract void doProcess(Document doc, CompassConfiguration config) throws ConfigurationException;

    protected DocumentBuilderFactory createDocumentBuilderFactory()
            throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        return factory;
    }

    protected DocumentBuilder createDocumentBuilder(DocumentBuilderFactory factory)
            throws ParserConfigurationException {
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        docBuilder.setErrorHandler(doGetErrorHandler());
        docBuilder.setEntityResolver(doGetEntityResolver());
        return docBuilder;
    }

    protected ErrorHandler doGetErrorHandler() {
        return new SimpleSaxErrorHandler(log);
    }

    protected abstract EntityResolver doGetEntityResolver();
}
