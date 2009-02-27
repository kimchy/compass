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

package org.compass.core.lucene.engine.transaction.support.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * Takes all the jobs within this transaction and breaks it into one or more
     * {@link TransactionJobs} per
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (TransactionJob job : jobs) {
            sb.append(job).append(", ");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionJobs that = (TransactionJobs) o;

        if (subIndexes != null ? !subIndexes.equals(that.subIndexes) : that.subIndexes != null) return false;
        if (jobs != null ? !jobs.equals(that.jobs) : that.jobs != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = jobs != null ? jobs.hashCode() : 0;
        result = 31 * result + (subIndexes != null ? subIndexes.hashCode() : 0);
        return result;
    }
}
