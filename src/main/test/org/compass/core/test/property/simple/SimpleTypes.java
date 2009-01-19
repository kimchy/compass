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

package org.compass.core.test.property.simple;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author kimchy
 */
public class SimpleTypes {

    private Long id;
    
    private Locale oLocale;

    private Character oChar;

    private String oString;

    private Boolean oBoolean;

    private Byte oByte;

    private Short oShort;

    private Integer oInteger;

    private Long oLong;

    private Float oFloat;

    private Double oDouble;

    private BigDecimal oBigDecimal;

    private BigInteger oBigInteger;

    private Date oDate;

    private Calendar oCalendar;

    private char sChar;

    private boolean sBoolean;

    private byte sByte;

    private short sShort;

    private int sInt;

    private long sLong;

    private double sDouble;

    private float sFloat;

    private StringBuffer oStringBuffer;

    private URL oURL;

    private File oFile;

    /**
     * @return Returns the oBigDecimal.
     */
    public BigDecimal getOBigDecimal() {
        return oBigDecimal;
    }

    /**
     * @param bigDecimal
     *            The oBigDecimal to set.
     */
    public void setOBigDecimal(BigDecimal bigDecimal) {
        oBigDecimal = bigDecimal;
    }

    /**
     * @return Returns the oBigInteger.
     */
    public BigInteger getOBigInteger() {
        return oBigInteger;
    }

    /**
     * @param bigInteger
     *            The oBigInteger to set.
     */
    public void setOBigInteger(BigInteger bigInteger) {
        oBigInteger = bigInteger;
    }

    /**
     * @return Returns the oBoolean.
     */
    public Boolean getOBoolean() {
        return oBoolean;
    }

    /**
     * @param boolean1
     *            The oBoolean to set.
     */
    public void setOBoolean(Boolean boolean1) {
        oBoolean = boolean1;
    }

    /**
     * @return Returns the oByte.
     */
    public Byte getOByte() {
        return oByte;
    }

    /**
     * @param byte1
     *            The oByte to set.
     */
    public void setOByte(Byte byte1) {
        oByte = byte1;
    }

    /**
     * @return Returns the oChar.
     */
    public Character getOChar() {
        return oChar;
    }

    /**
     * @param char1
     *            The oChar to set.
     */
    public void setOChar(Character char1) {
        oChar = char1;
    }

    /**
     * @return Returns the oDate.
     */
    public Date getODate() {
        return oDate;
    }

    /**
     * @param date
     *            The oDate to set.
     */
    public void setODate(Date date) {
        oDate = date;
    }

    /**
     * @return Returns the oDouble.
     */
    public Double getODouble() {
        return oDouble;
    }

    /**
     * @param double1
     *            The oDouble to set.
     */
    public void setODouble(Double double1) {
        oDouble = double1;
    }

    /**
     * @return Returns the oFile.
     */
    public File getOFile() {
        return oFile;
    }

    /**
     * @param file
     *            The oFile to set.
     */
    public void setOFile(File file) {
        oFile = file;
    }

    /**
     * @return Returns the oInteger.
     */
    public Integer getOInteger() {
        return oInteger;
    }

    /**
     * @param integer
     *            The oInteger to set.
     */
    public void setOInteger(Integer integer) {
        oInteger = integer;
    }

    /**
     * @return Returns the oLong.
     */
    public Long getOLong() {
        return oLong;
    }

    /**
     * @param long1
     *            The oLong to set.
     */
    public void setOLong(Long long1) {
        oLong = long1;
    }

    /**
     * @return Returns the oShort.
     */
    public Short getOShort() {
        return oShort;
    }

    /**
     * @param short1
     *            The oShort to set.
     */
    public void setOShort(Short short1) {
        oShort = short1;
    }

    /**
     * @return Returns the oString.
     */
    public String getOString() {
        return oString;
    }

    /**
     * @param string
     *            The oString to set.
     */
    public void setOString(String string) {
        oString = string;
    }

    /**
     * @return Returns the oStringBuffer.
     */
    public StringBuffer getOStringBuffer() {
        return oStringBuffer;
    }

    /**
     * @param stringBuffer
     *            The oStringBuffer to set.
     */
    public void setOStringBuffer(StringBuffer stringBuffer) {
        oStringBuffer = stringBuffer;
    }

    /**
     * @return Returns the oURL.
     */
    public URL getOURL() {
        return oURL;
    }

    /**
     * @param ourl
     *            The oURL to set.
     */
    public void setOURL(URL ourl) {
        oURL = ourl;
    }

    /**
     * @return Returns the sBoolean.
     */
    public boolean isSBoolean() {
        return sBoolean;
    }

    /**
     * @param boolean1
     *            The sBoolean to set.
     */
    public void setSBoolean(boolean boolean1) {
        sBoolean = boolean1;
    }

    /**
     * @return Returns the sByte.
     */
    public byte getSByte() {
        return sByte;
    }

    /**
     * @param byte1
     *            The sByte to set.
     */
    public void setSByte(byte byte1) {
        sByte = byte1;
    }

    /**
     * @return Returns the sChar.
     */
    public char getSChar() {
        return sChar;
    }

    /**
     * @param char1
     *            The sChar to set.
     */
    public void setSChar(char char1) {
        sChar = char1;
    }

    /**
     * @return Returns the sDouble.
     */
    public double getSDouble() {
        return sDouble;
    }

    /**
     * @param double1
     *            The sDouble to set.
     */
    public void setSDouble(double double1) {
        sDouble = double1;
    }

    /**
     * @return Returns the sInt.
     */
    public int getSInt() {
        return sInt;
    }

    /**
     * @param int1
     *            The sInt to set.
     */
    public void setSInt(int int1) {
        sInt = int1;
    }

    /**
     * @return Returns the sLong.
     */
    public long getSLong() {
        return sLong;
    }

    /**
     * @param long1
     *            The sLong to set.
     */
    public void setSLong(long long1) {
        sLong = long1;
    }

    /**
     * @return Returns the sShort.
     */
    public short getSShort() {
        return sShort;
    }

    /**
     * @param short1
     *            The sShort to set.
     */
    public void setSShort(short short1) {
        sShort = short1;
    }

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            The id to set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return Returns the sFloat.
     */
    public float getSFloat() {
        return sFloat;
    }

    /**
     * @param float1
     *            The sFloat to set.
     */
    public void setSFloat(float float1) {
        sFloat = float1;
    }

    /**
     * @return Returns the oFloat.
     */
    public Float getOFloat() {
        return oFloat;
    }

    /**
     * @param float1
     *            The oFloat to set.
     */
    public void setOFloat(Float float1) {
        oFloat = float1;
    }

    public Calendar getOCalendar() {
        return oCalendar;
    }

    public void setOCalendar(Calendar calendar) {
        oCalendar = calendar;
    }

    public Locale getOLocale() {
        return oLocale;
    }

    public void setOLocale(Locale locale) {
        oLocale = locale;
    }

}
