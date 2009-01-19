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

package org.compass.core.test.formatter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @author kimchy
 */
public class A {

    private Long id;

    private int intVal;

    private double doubleVal;

    private short shortVal;

    private float floatVal;

    private long longVal;

    private Date dateVal;

    private BigInteger bigIntegerVal;

    private BigDecimal bigDecimalVal;

    private Time timeVal;

    private Timestamp timestampVal;

    public float getFloatVal() {
        return floatVal;
    }

    public void setFloatVal(float floatVal) {
        this.floatVal = floatVal;
    }

    public long getLongVal() {
        return longVal;
    }

    public void setLongVal(long longVal) {
        this.longVal = longVal;
    }

    public short getShortVal() {
        return shortVal;
    }

    public void setShortVal(short shortVal) {
        this.shortVal = shortVal;
    }

    public double getDoubleVal() {
        return doubleVal;
    }

    public void setDoubleVal(double doubleVal) {
        this.doubleVal = doubleVal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getIntVal() {
        return intVal;
    }

    public void setIntVal(int intVal) {
        this.intVal = intVal;
    }

    public Date getDateVal() {
        return dateVal;
    }

    public void setDateVal(Date dateVal) {
        this.dateVal = dateVal;
    }

    public BigInteger getBigIntegerVal() {
        return bigIntegerVal;
    }

    public void setBigIntegerVal(BigInteger bigIntegerVal) {
        this.bigIntegerVal = bigIntegerVal;
    }

    public BigDecimal getBigDecimalVal() {
        return bigDecimalVal;
    }

    public void setBigDecimalVal(BigDecimal bigDecimalVal) {
        this.bigDecimalVal = bigDecimalVal;
    }

    public Time getTimeVal() {
        return timeVal;
    }

    public void setTimeVal(Time timeVal) {
        this.timeVal = timeVal;
    }

    public Timestamp getTimestampVal() {
        return timestampVal;
    }

    public void setTimestampVal(Timestamp timestampVal) {
        this.timestampVal = timestampVal;
    }
}
