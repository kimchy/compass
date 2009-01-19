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

package org.compass.needle.gigaspaces.service;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.compass.core.Resource;

/**
 * A serach result for a {@link org.compass.core.Resource} hit.
 *
 * @author kimchy
 */
public class SearchResourceResult implements Externalizable {

    private float score;

    private Resource data;

    public SearchResourceResult() {
    }

    public SearchResourceResult(float score, Resource data) {
        this.score = score;
        this.data = data;
    }

    /**
     * Returns the score associated with this hit.
     */
    public float getScore() {
        return score;
    }

    /**
     * Returns the resource associated with this hit.
     */
    public Resource getResource() {
        return data;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeFloat(score);
        out.writeObject(data);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        score = in.readFloat();
        data = (Resource) in.readObject();
    }
}