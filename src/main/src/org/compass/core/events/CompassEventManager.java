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

package org.compass.core.events;

import java.lang.reflect.Array;
import java.util.Map;

import org.compass.core.Compass;
import org.compass.core.CompassException;
import org.compass.core.CompassQuery;
import org.compass.core.Resource;
import org.compass.core.config.CompassAware;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassMappingAware;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.spi.AliasedObject;

/**
 * The event manager responsible for delegation of a specific event to one or more event listeners.
 *
 * @author kimchy
 */
public class CompassEventManager implements CompassConfigurable,
        PreCreateEventListener, PreDeleteEventListener, PreSaveEventListener,
        PostCreateEventListener, PostDeleteEventListener, PostSaveEventListener,
        PreCreateResourceEventListener, PreSaveResourceEventListener, PreDeleteResourceEventListener,
        PostCreateResourceEventListener, PostSaveResourceEventListener, PostDeleteResourceEventListener,
        PreDeleteQueryEventListener, PostDeleteQueryEventListener {

    private CompassMapping mapping;

    private Compass compass;

    private PreCreateEventListener[] preCreateEventListeners;

    private PreDeleteEventListener[] preDeleteEventListeners;

    private PreSaveEventListener[] preSaveEventListeners;

    private PostCreateEventListener[] postCreateEventListeners;

    private PostSaveEventListener[] postSaveEventListeners;

    private PostDeleteEventListener[] postDeleteEventListeners;

    private PreCreateResourceEventListener[] preCreateResourceEventListeners;

    private PreSaveResourceEventListener[] preSaveResourceEventListeners;

    private PreDeleteResourceEventListener[] preDeleteResourceEventListeners;

    private PostCreateResourceEventListener[] postCreateResourceEventListeners;

    private PostSaveResourceEventListener[] postSaveResourceEventListeners;

    private PostDeleteResourceEventListener[] postDeleteResourceEventListeners;

    private PreDeleteQueryEventListener[] preDeleteQueryEventListeners;

    private PostDeleteQueryEventListener[] postDeleteQueryEventListeners;

    public CompassEventManager(Compass compass, CompassMapping mapping) {
        this.compass = compass;
        this.mapping = mapping;
    }

    public void configure(CompassSettings settings) throws CompassException {
        preCreateEventListeners = configureListener(settings, CompassEnvironment.Event.PREFIX_PRE_CREATE, PreCreateEventListener.class);
        preDeleteEventListeners = configureListener(settings, CompassEnvironment.Event.PREFIX_PRE_DELETE, PreDeleteEventListener.class);
        preSaveEventListeners = configureListener(settings, CompassEnvironment.Event.PREFIX_PRE_SAVE, PreSaveEventListener.class);

        preCreateResourceEventListeners = configureListener(settings, CompassEnvironment.Event.PREFIX_PRE_CREATE_RESOURCE, PreCreateResourceEventListener.class);
        preDeleteResourceEventListeners = configureListener(settings, CompassEnvironment.Event.PREFIX_PRE_DELETE_RESOURCE, PreDeleteResourceEventListener.class);
        preSaveResourceEventListeners = configureListener(settings, CompassEnvironment.Event.PREFIX_PRE_SAVE_RESOURCE, PreSaveResourceEventListener.class);

        preDeleteQueryEventListeners = configureListener(settings, CompassEnvironment.Event.PREFIX_PRE_DELETE_QUERY, PreDeleteQueryEventListener.class);

        postCreateEventListeners = configureListener(settings, CompassEnvironment.Event.PREFIX_POST_CREATE, PostCreateEventListener.class);
        postDeleteEventListeners = configureListener(settings, CompassEnvironment.Event.PREFIX_POST_DELETE, PostDeleteEventListener.class);
        postSaveEventListeners = configureListener(settings, CompassEnvironment.Event.PREFIX_POST_SAVE, PostSaveEventListener.class);

        postCreateResourceEventListeners = configureListener(settings, CompassEnvironment.Event.PREFIX_POST_CREATE_RESOURCE, PostCreateResourceEventListener.class);
        postDeleteResourceEventListeners = configureListener(settings, CompassEnvironment.Event.PREFIX_POST_DELETE_RESOURCE, PostDeleteResourceEventListener.class);
        postSaveResourceEventListeners = configureListener(settings, CompassEnvironment.Event.PREFIX_POST_SAVE_RESOURCE, PostSaveResourceEventListener.class);

        postDeleteQueryEventListeners = configureListener(settings, CompassEnvironment.Event.PREFIX_POST_DELETE_QUERY, PostDeleteQueryEventListener.class);
    }

    private <T> T[] configureListener(CompassSettings settings, String settingPrefix, Class<T> type) {
        Map<String, CompassSettings> listenerSettings = settings.getSettingGroups(settingPrefix);
        if (listenerSettings.size() == 0) {
            return null;
        }
        T[] listeners = (T[]) Array.newInstance(type, listenerSettings.size());
        int count = 0;
        for (Map.Entry<String, CompassSettings> entry : listenerSettings.entrySet()) {
            T listener = (T) entry.getValue().getSettingAsInstance(CompassEnvironment.Event.TYPE);
            if (listener == null) {
                throw new ConfigurationException("type is required when configuring the [" + entry.getKey() + "] event");
            }
            if (listener instanceof CompassMappingAware) {
                ((CompassMappingAware) listener).setCompassMapping(mapping);
            }
            if (listener instanceof CompassAware) {
                ((CompassAware) listener).setCompass(compass);
            }
            listeners[count++] = listener;
        }
        return listeners;
    }

    public FilterOperation onPreCreate(String alias, Object obj) {
        if (preCreateEventListeners == null) {
            return FilterOperation.NO;
        }
        alias = findAlias(alias, obj);
        for (PreCreateEventListener listener : preCreateEventListeners) {
            if (listener.onPreCreate(alias, obj) == FilterOperation.YES) {
                return FilterOperation.YES;
            }
        }
        return FilterOperation.NO;
    }

    public FilterOperation onPreCreate(Resource resource) {
        if (preCreateResourceEventListeners == null) {
            return FilterOperation.NO;
        }
        for (PreCreateResourceEventListener listener : preCreateResourceEventListeners) {
            if (listener.onPreCreate(resource) == FilterOperation.YES) {
                return FilterOperation.YES;
            }
        }
        return FilterOperation.NO;
    }

    public FilterOperation onPreDelete(String alias, Object obj) {
        if (preDeleteEventListeners == null) {
            return FilterOperation.NO;
        }
        alias = findAlias(alias, obj);
        for (PreDeleteEventListener listener : preDeleteEventListeners) {
            if (listener.onPreDelete(alias, obj) == FilterOperation.YES) {
                return FilterOperation.YES;
            }
        }
        return FilterOperation.NO;
    }

    public FilterOperation onPreDelete(Class clazz, Object obj) {
        String alias = findAlias(clazz, obj);
        if (preDeleteEventListeners == null) {
            return FilterOperation.NO;
        }
        for (PreDeleteEventListener listener : preDeleteEventListeners) {
            if (listener.onPreDelete(alias, obj) == FilterOperation.YES) {
                return FilterOperation.YES;
            }
        }
        return FilterOperation.NO;
    }

    public FilterOperation onPreDelete(Resource resource) {
        if (preDeleteResourceEventListeners == null) {
            return FilterOperation.NO;
        }
        for (PreDeleteResourceEventListener listener : preDeleteResourceEventListeners) {
            if (listener.onPreDelete(resource) == FilterOperation.YES) {
                return FilterOperation.YES;
            }
        }
        return FilterOperation.NO;
    }

    public FilterOperation onPreDelete(CompassQuery query) {
        if (preDeleteQueryEventListeners == null) {
            return FilterOperation.NO;
        }
        for (PreDeleteQueryEventListener listener : preDeleteQueryEventListeners) {
            if (listener.onPreDelete(query) == FilterOperation.YES) {
                return FilterOperation.YES;
            }
        }
        return FilterOperation.NO;
    }

    public FilterOperation onPreSave(String alias, Object obj) {
        if (preSaveEventListeners == null) {
            return FilterOperation.NO;
        }
        alias = findAlias(alias, obj);
        for (PreSaveEventListener listener : preSaveEventListeners) {
            if (listener.onPreSave(alias, obj) == FilterOperation.YES) {
                return FilterOperation.YES;
            }
        }
        return FilterOperation.NO;
    }

    public FilterOperation onPreSave(Resource resource) {
        if (preSaveResourceEventListeners == null) {
            return FilterOperation.NO;
        }
        for (PreSaveResourceEventListener listener : preSaveResourceEventListeners) {
            if (listener.onPreSave(resource) == FilterOperation.YES) {
                return FilterOperation.YES;
            }
        }
        return FilterOperation.NO;
    }

    public void onPostCreate(String alias, Object obj) {
        if (postCreateEventListeners == null) {
            return;
        }
        alias = findAlias(alias, obj);
        for (PostCreateEventListener listener : postCreateEventListeners) {
            listener.onPostCreate(alias, obj);
        }
    }

    public void onPostCreate(Resource resource) {
        if (postCreateResourceEventListeners == null) {
            return;
        }
        for (PostCreateResourceEventListener listener : postCreateResourceEventListeners) {
            listener.onPostCreate(resource);
        }
    }

    public void onPostDelete(String alias, Object obj) {
        if (postDeleteEventListeners == null) {
            return;
        }
        alias = findAlias(alias, obj);
        for (PostDeleteEventListener listener : postDeleteEventListeners) {
            listener.onPostDelete(alias, obj);
        }
    }

    public void onPostDelete(Class clazz, Object obj) {
        String alias = findAlias(clazz, obj);
        if (postDeleteEventListeners == null) {
            return;
        }
        for (PostDeleteEventListener listener : postDeleteEventListeners) {
            listener.onPostDelete(alias, obj);
        }
    }

    public void onPostDelete(Resource resource) {
        if (postDeleteResourceEventListeners == null) {
            return;
        }
        for (PostDeleteResourceEventListener listener : postDeleteResourceEventListeners) {
            listener.onPostDelete(resource);
        }
    }

    public void onPostDelete(CompassQuery query) {
        if (postDeleteQueryEventListeners == null) {
            return;
        }
        for (PostDeleteQueryEventListener listener : postDeleteQueryEventListeners) {
            listener.onPostDelete(query);
        }
    }

    public void onPostSave(String alias, Object obj) {
        if (postSaveEventListeners == null) {
            return;
        }
        alias = findAlias(alias, obj);
        for (PostSaveEventListener listener : postSaveEventListeners) {
            listener.onPostSave(alias, obj);
        }
    }

    public void onPostSave(Resource resource) {
        if (postSaveResourceEventListeners == null) {
            return;
        }
        for (PostSaveResourceEventListener listener : postSaveResourceEventListeners) {
            listener.onPostSave(resource);
        }
    }

    private String findAlias(String alias, Object obj) {
        if (alias != null) {
            return alias;
        }
        if (obj instanceof AliasedObject) {
            return ((AliasedObject) obj).getAlias();
        }
        ResourceMapping resourceMapping = mapping.getMappingByClass(obj.getClass());
        if (resourceMapping == null) {
            throw new CompassException("Can't derive alias from [" + obj + "]");
        }
        return resourceMapping.getAlias();
    }

    private String findAlias(Class clazz, Object obj) {
        if (obj instanceof AliasedObject) {
            return ((AliasedObject) obj).getAlias();
        }
        ResourceMapping resourceMapping = mapping.getMappingByClass(clazz);
        if (resourceMapping == null) {
            throw new CompassException("Can't derive alias from [" + obj + "]");
        }
        return resourceMapping.getAlias();
    }
}
