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

package org.compass.core.lucene.engine.transaction.support;

import org.compass.core.lucene.engine.transaction.support.job.TransactionJob;
import org.compass.core.spi.ResourceKey;
import org.compass.core.util.CollectionUtils;

/**
 * Hasing of resources with helper methods to compute the hash.
 *
 * @author kimchy
 */
public enum ResourceHashing {
    UID,
    SUBINDEX;

    public int hash(ResourceKey resourceKey) {
        switch (this) {
            case UID:
                return CollectionUtils.absHash(resourceKey.buildUID());
            case SUBINDEX:
                return CollectionUtils.absHash(resourceKey.getSubIndex());
        }
        throw new IllegalStateException("Intenral error in Compass");
    }

    public int hash(TransactionJob job) {
        if (job.getResourceUID() == null) {
            // in such cases, we always do it by sub index
            return CollectionUtils.absHash(job.getSubIndex());
        }
        switch (this) {
            case UID:
                return CollectionUtils.absHash(job.getResourceUID());
            case SUBINDEX:
                return CollectionUtils.absHash(job.getSubIndex());
        }
        throw new IllegalStateException("Intenral error in Compass");
    }

    public static ResourceHashing fromName(String name) {
        if ("uid".equalsIgnoreCase(name)) {
            return UID;
        } else if ("subindex".equalsIgnoreCase(name)) {
            return SUBINDEX;
        }
        throw new IllegalArgumentException("No resource hashing found for [" + name + "]");
    }
}
