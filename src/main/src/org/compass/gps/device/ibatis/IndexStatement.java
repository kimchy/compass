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

package org.compass.gps.device.ibatis;

/**
 * An iBatis index statement including the statement id and a possible parameters.
 *
 * @author kimchy
 */
public class IndexStatement {

    private String statementId;

    private Object param;

    public IndexStatement(String statementId) {
        this.statementId = statementId;
    }

    public IndexStatement(String statementId, Object param) {
        this.statementId = statementId;
        this.param = param;
    }

    public String getStatementId() {
        return statementId;
    }

    public Object getParam() {
        return param;
    }
}
