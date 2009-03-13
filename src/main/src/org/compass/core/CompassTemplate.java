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

package org.compass.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.config.CompassSettings;
import org.compass.core.impl.ExistingCompassSession;
import org.compass.core.spi.InternalCompassSession;

/**
 * Helper class that simplifies the Compass access code using the template
 * design pattern.
 * <p>
 * The central method is "execute", supporting Compass code implementing the
 * CompassCallback interface. It provides Compass Session handling such that
 * neither the CompassCallback implementation nor the calling code needs to
 * explicitly care about retrieving/closing Compass Sessions, handling Session
 * lifecycle exceptions, or managing transactions. The template code is similar
 * to
 *
 * <pre>
 * CompassSession session = compass.openSession();
 * CompassTransaction tx = null;
 * try {
 * 	tx = session.beginTransaction();
 * 	Object result = compassCallback.doInCompass(session);
 * 	tx.commit();
 * 	return result;
 * } catch (RuntimeException e) {
 * 	if (tx != null) {
 * 		tx.rollback();
 * 	}
 * 	throw e;
 * } finally {
 * 	session.close();
 * }
 * </pre>
 *
 * <p>
 * The template must have a Compass reference set, either using the tempalte
 * constructor or using the set method.
 * <p>
 * CompassTemplate also provides the same operations available when working with
 * CompassSession, just that they are executed using the "execute" template
 * method, which means that they enjoy it's session lifecycle and transaction
 * support.
 *
 * @author kimchy
 */
public class CompassTemplate implements CompassOperations {

    private static Log log = LogFactory.getLog(CompassTemplate.class);

    private Compass compass;

    private CompassSettings globalSessionSettings = new CompassSettings();

    private boolean readOnly;

    /**
     * Creates a new CompassTemplate instance (remember to set Compass using the
     * setCompass method).
     */
    public CompassTemplate() {

    }

    /**
     * Creates a new CompassTemplate instance, already initialized with a
     * Compass instance.
     */
    public CompassTemplate(Compass compass) {
        this.compass = compass;
    }

    /**
     * Sets the compass instance that will be used by the template.
     */
    public void setCompass(Compass compass) {
        this.compass = compass;
    }

    /**
     * Sets the {@link org.compass.core.CompassSession} to be read only.
     *
     * @see CompassSession#setReadOnly() 
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Returns the compass instance used by the template.
     *
     * @return the compass instance
     */
    public Compass getCompass() {
        return compass;
    }

    /**
     * Executes the compass callback within a session and a transaction context.
     *
     * @param action The action to execute witin a compass transaction
     * @return An object as the result of the compass action
     * @throws CompassException
     */
    public <T> T execute(CompassCallback<T> action) throws CompassException {
        CompassSession session = compass.openSession();
        if (readOnly) {
            session.setReadOnly();
        }
        session.getSettings().addSettings(globalSessionSettings);
        CompassTransaction tx = null;
        try {
            tx = session.beginTransaction();
            // we wrap with existing session so if #close is called within the callback, it won't be closed
            T result = action.doInCompass(new ExistingCompassSession((InternalCompassSession) session));
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception e1) {
                    log.error("Failed to rollback transaction, ignoring", e1);
                }
            }
            throw e;
        } catch (Error err) {
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception e1) {
                    log.error("Failed to rollback transaction, ignoring", e1);
                }
            }
            throw err;
        } finally {
            session.close();
        }
    }

    /**
     * Executes the compass callback within a session and a <b>local</b> transaction context.
     * Applies the given transaction isolation level.
     *
     * @param action The action to execute witin a compass transaction
     * @return An object as the result of the compass action
     * @throws CompassException
     */
    public <T> T executeLocal(CompassCallback<T> action) throws CompassException {
        CompassSession session = compass.openSession();
        session.getSettings().addSettings(globalSessionSettings);
        CompassTransaction tx = null;
        try {
            tx = session.beginLocalTransaction();
            // we wrap with existing session so if #close is called within the callback, it won't be closed
            T result = action.doInCompass(new ExistingCompassSession((InternalCompassSession) session));
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception e1) {
                    log.error("Failed to rollback transaction, ignoring", e1);
                }
            }
            throw e;
        } catch (Error err) {
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception e1) {
                    log.error("Failed to rollback transaction, ignoring", e1);
                }
            }
            throw err;
        } finally {
            session.close();
        }
    }

    /**
     * A helper execute method for find operations.
     *
     * @param action the callback to execute.
     * @return The hits that match the query
     * @throws CompassException
     */
    public CompassHitsOperations executeFind(CompassCallback<CompassHitsOperations> action) throws CompassException {
        return execute(action);
    }

    // Compass Operations

    public CompassSettings getSettings() {
        throw new CompassException("getSettings should not be used with CompassTemplate. Either use getGlobalSettings or execute");
    }

    public void create(final Object obj) throws CompassException {
        execute(new CompassCallback<Object>() {
            public Object doInCompass(CompassSession session) throws CompassException {
                session.create(obj);
                return null;
            }
        });
    }

    public void create(final String alias, final Object obj) throws CompassException {
        execute(new CompassCallback<Object>() {
            public Object doInCompass(CompassSession session) throws CompassException {
                session.create(alias, obj);
                return null;
            }
        });
    }

    public void delete(final Object obj) throws CompassException {
        execute(new CompassCallback<Object>() {
            public Object doInCompass(CompassSession session) throws CompassException {
                session.delete(obj);
                return null;
            }
        });
    }

    public void delete(final Resource resource) throws CompassException {
        execute(new CompassCallback<Object>() {
            public Object doInCompass(CompassSession session) throws CompassException {
                session.delete(resource);
                return null;
            }
        });
    }

    public void delete(final Class clazz, final Object obj) throws CompassException {
        execute(new CompassCallback<Object>() {
            public Object doInCompass(CompassSession session) throws CompassException {
                session.delete(clazz, obj);
                return null;
            }
        });
    }

    public void delete(final String alias, final Object obj) throws CompassException {
        execute(new CompassCallback<Object>() {
            public Object doInCompass(CompassSession session) throws CompassException {
                session.delete(alias, obj);
                return null;
            }
        });
    }

    public void delete(final CompassQuery query) throws CompassException {
        execute(new CompassCallback<Object>() {
            public Object doInCompass(CompassSession session) throws CompassException {
                session.delete(query);
                return null;
            }
        });
    }

    public CompassHits find(final String query) throws CompassException {
        return execute(new CompassCallback<CompassHits>() {
            public CompassHits doInCompass(CompassSession session) throws CompassException {
                return session.find(query);
            }
        });
    }

    public CompassDetachedHits findWithDetach(final String query) throws CompassException {
        return execute(new CompassCallback<CompassDetachedHits>() {
            public CompassDetachedHits doInCompass(CompassSession session) throws CompassException {
                return session.find(query).detach();
            }
        });
    }

    public CompassDetachedHits findWithDetach(final String query, final int from, final int size)
            throws CompassException {
        return execute(new CompassCallback<CompassDetachedHits>() {
            public CompassDetachedHits doInCompass(CompassSession session) throws CompassException {
                return session.find(query).detach(from, size);
            }
        });
    }

    public <T> T get(final Class<T> clazz, final Object id) throws CompassException {
        return execute(new CompassCallback<T>() {
            public T doInCompass(CompassSession session) throws CompassException {
                return session.get(clazz, id);
            }
        });
    }

    public Object get(final String alias, final Object id) throws CompassException {
        return execute(new CompassCallback<Object>() {
            public Object doInCompass(CompassSession session) throws CompassException {
                return session.get(alias, id);
            }
        });
    }

    public Resource getResource(final Class clazz, final Object id) throws CompassException {
        return (Resource) execute(new CompassCallback<Object>() {
            public Object doInCompass(CompassSession session) throws CompassException {
                return session.getResource(clazz, id);
            }
        });
    }

    public Resource getResource(final String alias, final Object id) throws CompassException {
        return (Resource) execute(new CompassCallback<Object>() {
            public Object doInCompass(CompassSession session) throws CompassException {
                return session.getResource(alias, id);
            }
        });
    }

    public <T> T load(Class<T> clazz, Object... ids) throws CompassException {
        return load(clazz, (Object) ids);
    }

    public <T> T load(final Class<T> clazz, final Object id) throws CompassException {
        return execute(new CompassCallback<T>() {
            public T doInCompass(CompassSession session) throws CompassException {
                return session.load(clazz, id);
            }
        });
    }

    public Object load(String alias, Object... ids) throws CompassException {
        return load(alias, (Object) ids);
    }

    public Object load(final String alias, final Object id) throws CompassException {
        return execute(new CompassCallback<Object>() {
            public Object doInCompass(CompassSession session) throws CompassException {
                return session.load(alias, id);
            }
        });
    }

    public Resource loadResource(final Class clazz, final Object id) throws CompassException {
        return (Resource) execute(new CompassCallback<Object>() {
            public Object doInCompass(CompassSession session) throws CompassException {
                return session.loadResource(clazz, id);
            }
        });
    }

    public Resource loadResource(final String alias, final Object id) throws CompassException {
        return (Resource) execute(new CompassCallback<Object>() {
            public Object doInCompass(CompassSession session) throws CompassException {
                return session.loadResource(alias, id);
            }
        });
    }

    public void save(final Object obj) throws CompassException {
        execute(new CompassCallback<Object>() {
            public Object doInCompass(CompassSession session) throws CompassException {
                session.save(obj);
                return null;
            }
        });
    }

    public void save(final String alias, final Object obj) throws CompassException {
        execute(new CompassCallback<Object>() {
            public Object doInCompass(CompassSession session) throws CompassException {
                session.save(alias, obj);
                return null;
            }
        });
    }

    public void evict(final Object obj) {
        execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                session.evict(obj);
            }
        });
    }

    public void evict(final String alias, final Object id) {
        execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                session.evict(alias, id);
            }
        });
    }

    public void evict(final Resource resource) {
        execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                session.evict(resource);
            }
        });
    }

    public void evictAll() {
        execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                session.evictAll();
            }
        });
    }

    public Resource getResource(Class clazz, Object... ids) throws CompassException {
        return getResource(clazz, (Object) ids);
    }

    public Resource getResource(String alias, Object... ids) throws CompassException {
        return getResource(alias, (Object) ids);
    }

    public Resource loadResource(Class clazz, Object... ids) throws CompassException {
        return loadResource(clazz, (Object) ids);
    }

    public Resource loadResource(String alias, Object... ids) throws CompassException {
        return loadResource(alias, (Object) ids);
    }

    public void delete(String alias, Object... ids) throws CompassException {
        delete(alias, (Object) ids);
    }

    public void delete(Class clazz, Object... ids) throws CompassException {
        delete(clazz, (Object) ids);
    }

    public <T> T get(Class<T> clazz, Object... ids) throws CompassException {
        return get(clazz, (Object) ids);
    }

    public Object get(String alias, Object... ids) throws CompassException {
        return get(alias, (Object) ids);
    }
}
