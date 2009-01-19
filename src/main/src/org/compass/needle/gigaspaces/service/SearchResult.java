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

/**
 * A serach result for an object hit.
 *
 * @author kimchy
 */
public class SearchResult implements Externalizable {

    private float score;

    private Object data;

    public SearchResult() {
    }

    public SearchResult(float score, Object data) {
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
     * Returns the object associated with this hit.
     */
    public Object getData() {
        return data;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeFloat(score);
        out.writeObject(data);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        score = in.readFloat();
        data = in.readObject();
    }
}
