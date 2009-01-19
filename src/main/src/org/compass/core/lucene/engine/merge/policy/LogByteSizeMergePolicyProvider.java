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

import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.index.MergePolicy;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;

/**
 * A merge factory provider creating a {@link org.apache.lucene.index.LogByteSizeMergePolicy}. Can be configured
 * using {@link org.compass.core.lucene.LuceneEnvironment.MergePolicy.LogByteSize}.
 *
 * @author kimchy
 */
public class LogByteSizeMergePolicyProvider implements MergePolicyProvider {

    public MergePolicy create(CompassSettings settings) throws SearchEngineException {
        LogByteSizeMergePolicy mergePolicy = new LogByteSizeMergePolicy();
        mergePolicy.setMaxMergeMB(settings.getSettingAsDouble(LuceneEnvironment.MergePolicy.LogByteSize.MAX_MERGE_MB, LogByteSizeMergePolicy.DEFAULT_MAX_MERGE_MB));
        mergePolicy.setMinMergeMB(settings.getSettingAsDouble(LuceneEnvironment.MergePolicy.LogByteSize.MIN_MERGE_MB, LogByteSizeMergePolicy.DEFAULT_MIN_MERGE_MB));
        return mergePolicy;
    }
}
