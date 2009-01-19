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

package org.compass.needle.gigaspaces.datasource;

import java.util.List;
import java.util.Properties;

import com.gigaspaces.datasource.BulkDataPersister;
import com.gigaspaces.datasource.BulkItem;
import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataPersister;
import com.gigaspaces.datasource.DataSourceException;
import org.compass.core.Compass;
import org.compass.core.CompassCallbackWithoutResult;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.CompassTemplate;
import org.compass.core.config.CompassConfigurationFactory;
import org.compass.core.spi.InternalCompass;

/**
 * A Compass data source to be used with GigaSpaces mirror.
 *
 * <p>GigaSpaces mirror allows to mirror changes done to the Space into an external data source
 * in a reliable async manner. The Compass data source allows to mirror changes done to the
 * Space into the search engine using Compass.
 *
 * <p>This extenal data source can be injected with a {@link Compass} instance, or it can be
 * configured using the extenal data source proeprties configuration to create a Compass instance.
 * The proeprty <code>compass-config-file</code> will point to Compass configuration file.
 *
 * @author kimchy
 */
public class CompassDataSource implements DataPersister<Object>, BulkDataPersister {

    public static final String COMPASS_CFG_PROPERTY = "compass-config-file";

    private Compass compass;

    private CompassTemplate compassTemplate;

    public void setCompass(Compass compass) {
        this.compass = compass;
    }

    public void init(Properties properties) throws DataSourceException {
        if (compass == null) {
            String compassConfig = properties.getProperty(COMPASS_CFG_PROPERTY);
            compass = CompassConfigurationFactory.newConfiguration().configure(compassConfig).buildCompass();
        }
        compassTemplate = new CompassTemplate(compass);
    }

    public void shutdown() throws DataSourceException {
        if (compass != null) {
            compass.close();
            compass = null;
        }
    }

    /**
     * Write given new object to the data store
     */
    public void write(Object object) throws DataSourceException {
        if (hasMapping(object)) {
            compassTemplate.save(object);
        }
    }

    /**
     * Update the given object in the data store
     */
    public void update(Object object) throws DataSourceException {
        if (hasMapping(object)) {
            compassTemplate.save(object);
        }
    }

    /**
     * Remove the given object from the data store
     */
    public void remove(Object object) throws DataSourceException {
        if (hasMapping(object)) {
            compassTemplate.delete(object);
        }
    }

    /**
     * Write given new objects to the data store.
     *
     * <p>If the implementation uses transactions, all the objects must be written
     * in one transaction.
     */
    public void writeBatch(final List<Object> objects) throws DataSourceException {
        compassTemplate.execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                for (Object object : objects) {
                    if (hasMapping(object)) {
                        session.save(object);
                    }
                }
            }
        });
    }

    /**
     * Update given objects in the data store.
     *
     * <p>If the implementation uses transactions, all the objects must be updated
     * in one transaction.
     *
     * <p>* This operation is not currently supported by the space.
     */
    public void updateBatch(final List<Object> objects) throws DataSourceException {
        compassTemplate.execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                for (Object object : objects) {
                    if (hasMapping(object)) {
                        session.save(object);
                    }
                }
            }
        });
    }

    /**
     * Remove given objects from the data store.
     *
     * <p>If the implementation uses transactions, All the objects must be removed
     * in one transaction.
     *
     * <p>This operation is not currently supported by the space.<br>
     */
    public void removeBatch(final List<Object> objects) throws DataSourceException {
        compassTemplate.execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                for (Object object : objects) {
                    if (hasMapping(object)) {
                        session.delete(object);
                    }
                }
            }
        });
    }

    /**
     * Execute given bulk of operations. Each {@link BulkItem} contains one of
     * the following operation -<br>
     *
     * <p>
     * WRITE - given object should be inserted to the data store,<br>
     * UPDATE - given object should be updated in the data store,<br>
     * REMOVE - given object should be deleted from the data store<br>
     * <br>
     * If the implementation uses transactions,<br>
     * all the bulk operations must be executed in one transaction.<br>
     */
    public void executeBulk(final List<BulkItem> bulkItems) throws DataSourceException {
        compassTemplate.execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                for (BulkItem bulkItem : bulkItems) {
                    switch (bulkItem.getOperation()) {
                        case BulkItem.REMOVE:
                            if (hasMapping(bulkItem.getItem())) {
                                session.delete(bulkItem.getItem());
                            }
                            break;
                        case BulkItem.WRITE:
                            if (hasMapping(bulkItem.getItem())) {
                                session.save(bulkItem.getItem());
                            }
                            break;
                        case BulkItem.UPDATE:
                            if (hasMapping(bulkItem.getItem())) {
                                session.save(bulkItem.getItem());
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    /**
     * Read one object that matches the given template. <br>
     * Used by the space for read templates with UID.<br>
     */
    public Object read(Object template) throws DataSourceException {
        throw new UnsupportedOperationException();
    }

    /**
     * Create an iterator over all objects that match the given template.<br>
     * Note: null value can be passed - in case of a null template or at initial
     * space load
     */
    public DataIterator<Object> iterator(Object template) throws DataSourceException {
        throw new UnsupportedOperationException();
    }

    /**
     * Count number of object in the data source that match given template.<br>
     * Note: null value can be passed - in case of a null template.
     */
    public int count(Object template) throws DataSourceException {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates and returns an iterator over all the entries that should be
     * loaded into space.
     *
     * @return a {@link DataIterator} or null if no data should be loaded into
     *         space
     * @throws DataSourceException
     */
    public DataIterator<Object> initialLoad() throws DataSourceException {
        throw new UnsupportedOperationException();
    }

    private boolean hasMapping(Object object) {
        return ((InternalCompass) compass).getMapping().getRootMappingByClass(object.getClass()) != null;
    }
}
