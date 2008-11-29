/*
 * Copyright 2004-2008 the original author or authors.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.compass.core.engine.SearchEngineFactory;

/**
 * A holds for a list of jobs (usually, represent a transaction which holds several dirty operations).
 *
 * @author kimchy
 */
public class TransactionJobs implements Serializable {

    private List<TransactionJob> jobs = new ArrayList<TransactionJob>();

    private Set<String> subIndexes = new HashSet<String>();

    /**
     * Adds a transactional job, representing a single create/update/delete operation.
     */
    public void add(TransactionJob job) {
        jobs.add(job);
        subIndexes.add(job.getSubIndex());
    }

    /**
     * Returns all the jobs.
     */
    public List<TransactionJob> getJobs() {
        return this.jobs;
    }

    /**
     * Returns all the sub indexes this transactional jobs were performed against.
     */
    public Set<String> getSubIndexes() {
        return this.subIndexes;
    }

    public void attach(SearchEngineFactory searchEngineFactory) {
        for (TransactionJob job : jobs) {
            job.attach(searchEngineFactory);
        }
    }

    /**
     * Takes all the jobs within this transaction and breaks it into one or more
     * {@link org.compass.core.lucene.engine.transaction.support.TransactionJobs} per
     * sub index.
     */
    public Map<String, TransactionJobs> buildJobsPerSubIndex() {
        Map<String, TransactionJobs> jobsPerSubIndex = new HashMap<String, TransactionJobs>();
        for (TransactionJob job : jobs) {
            TransactionJobs jobs = jobsPerSubIndex.get(job.getSubIndex());
            if (jobs == null) {
                jobs = new TransactionJobs();
                jobsPerSubIndex.put(job.getSubIndex(), jobs);
            }
            jobs.add(job);
        }
        return jobsPerSubIndex;
    }
}
