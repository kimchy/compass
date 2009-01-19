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

package org.compass.core.lucene.engine.merge.policy;

import org.apache.lucene.index.MergePolicy;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.util.ClassUtils;

/**
 * A {@link org.apache.lucene.index.MergePolicy} factory creating based on the type of the provider
 * the actual implementation of {@link org.compass.core.lucene.engine.merge.policy.MergePolicyProvider}
 * and then using it to create the merge factory.
 *
 * @author kimchy
 */
public class MergePolicyFactory {

    public static MergePolicy createMergePolicy(CompassSettings settings) throws ConfigurationException {
        String type = settings.getSetting(LuceneEnvironment.MergePolicy.TYPE, LuceneEnvironment.MergePolicy.LogByteSize.NAME);
        MergePolicyProvider provider;
        if (type.equals(LuceneEnvironment.MergePolicy.LogByteSize.NAME)) {
            provider = new LogByteSizeMergePolicyProvider();
        } else if (type.equals(LuceneEnvironment.MergePolicy.LogDoc.NAME)) {
            provider = new LogDocMergePolicyProvider();
        } else {
            try {
                provider = (MergePolicyProvider) ClassUtils.forName(type, settings.getClassLoader()).newInstance();
            } catch (Exception e) {
                throw new ConfigurationException("Failed to load/create merge policy provider [" + type + "]", e);
            }
        }
        return provider.create(settings);
    }
}
