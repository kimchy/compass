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

package org.compass.core.converter.basic;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * A Simple Utility class for parsing "math" like strings relating to Dates.
 *
 * <p>
 * The basic syntax support addition, subtraction and rounding at various
 * levels of granularity (or "units").  Commands can be chained together
 * and are parsed from left to right.  '+' and '-' denote addition and
 * subtraction, while '/' denotes "round".  Round requires only a unit, while
 * addition/subtraction require an integer value and a unit.
 * Command strings must not include white space, but the "No-Op" command
 * (empty string) is allowed....
 * </p>
 *
 * <pre>
 *   /HOUR
 *      ... Round to the start of the current hour
 *   /DAY
 *      ... Round to the start of the current day
 *   +2YEARS
 *      ... Exactly two years in the future from now
 *   -1DAY
 *      ... Exactly 1 day prior to now
 *   /DAY+6MONTHS+3DAYS
 *      ... 6 months and 3 days in the future from the start of
 *          the current day
 *   +6MONTHS+3DAYS/DAY
 *      ... 6 months and 3 days in the future from now, rounded
 *          down to nearest day
 * </pre>
 *
 * <p>
 * All commands are relative to a "now" which is fixed in an instance of
 * DateMathParser such that
 * <code>p.parseMath("+0MILLISECOND").equals(p.parseMath("+0MILLISECOND"))</code>
 * no matter how many wall clock milliseconds elapse between the two
 * distinct calls to parse (Assuming no other thread calls
 * "<code>setNow</code>" in the interim)
 * </p>
 *
 * <p>
 * Multiple aliases exist for the various units of time (ie:
 * <code>MINUTE</code> and <code>MINUTES</code>; <code>MILLI</code>,
 * <code>MILLIS</code>, <code>MILLISECOND</code>, and
 * <code>MILLISECONDS</code>.)  The complete list can be found by
 * inspecting the keySet of <code>CALENDAR_UNITS</code>.
 * </p>
 *
 * @author taken from Solr
 */
public class DateMathParser {

    /**
     * A mapping from (uppercased) String labels idenyifying time units,
     * to the corresponding Calendar constant used to set/add/roll that unit
     * of measurement.
     *
     * <p>
     * A single logical unit of time might be represented by multiple labels
     * for convenience (ie: <code>DATE==DAY</code>,
     * <code>MILLI==MILLISECOND</code>)
     * </p>
     *
     * @see java.util.Calendar
     */
    public static final Map CALENDAR_UNITS = makeUnitsMap();

    /**
     * @see #CALENDAR_UNITS
     */
    private static Map makeUnitsMap() {

        // NOTE: consciously choosing not to support WEEK at this time,
        // because of complexity in rounding down to the nearest week
        // arround a month/year boundry.
        // (Not to mention: it's not clear what people would *expect*)

        Map units = new HashMap(13);
        units.put("YEAR", new Integer(Calendar.YEAR));
        units.put("YEARS", new Integer(Calendar.YEAR));
        units.put("MONTH", new Integer(Calendar.MONTH));
        units.put("MONTHS", new Integer(Calendar.MONTH));
        units.put("DAY", new Integer(Calendar.DATE));
        units.put("DAYS", new Integer(Calendar.DATE));
        units.put("DATE", new Integer(Calendar.DATE));
        units.put("HOUR", new Integer(Calendar.HOUR_OF_DAY));
        units.put("HOURS", new Integer(Calendar.HOUR_OF_DAY));
        units.put("MINUTE", new Integer(Calendar.MINUTE));
        units.put("MINUTES", new Integer(Calendar.MINUTE));
        units.put("SECOND", new Integer(Calendar.SECOND));
        units.put("SECONDS", new Integer(Calendar.SECOND));
        units.put("MILLI", new Integer(Calendar.MILLISECOND));
        units.put("MILLIS", new Integer(Calendar.MILLISECOND));
        units.put("MILLISECOND", new Integer(Calendar.MILLISECOND));
        units.put("MILLISECONDS", new Integer(Calendar.MILLISECOND));

        return units;
    }

    /**
     * Modifies the specified Calendar by "adding" the specified value of units
     *
     * @throws IllegalArgumentException if unit isn't recognized.
     * @see #CALENDAR_UNITS
     */
    public static void add(Calendar c, int val, String unit) {
        Integer uu = (Integer) CALENDAR_UNITS.get(unit.toUpperCase());
        if (null == uu) {
            throw new IllegalArgumentException("Adding Unit not recognized: "
                    + unit);
        }
        c.add(uu.intValue(), val);
    }

    /**
     * Modifies the specified Calendar by "rounding" down to the specified unit
     *
     * @throws IllegalArgumentException if unit isn't recognized.
     * @see #CALENDAR_UNITS
     */
    public static void round(Calendar c, String unit) {
        Integer uu = (Integer) CALENDAR_UNITS.get(unit.toUpperCase());
        if (null == uu) {
            throw new IllegalArgumentException("Rounding Unit not recognized: "
                    + unit);
        }
        int u = uu.intValue();

        switch (u) {

            case Calendar.YEAR:
                c.clear(Calendar.MONTH);
                /* fall through */
            case Calendar.MONTH:
                c.clear(Calendar.DAY_OF_MONTH);
                c.clear(Calendar.DAY_OF_WEEK);
                c.clear(Calendar.DAY_OF_WEEK_IN_MONTH);
                c.clear(Calendar.DAY_OF_YEAR);
                c.clear(Calendar.WEEK_OF_MONTH);
                c.clear(Calendar.WEEK_OF_YEAR);
                /* fall through */
            case Calendar.DATE:
                c.clear(Calendar.HOUR_OF_DAY);
                c.clear(Calendar.HOUR);
                c.clear(Calendar.AM_PM);
                /* fall through */
            case Calendar.HOUR_OF_DAY:
                c.clear(Calendar.MINUTE);
                /* fall through */
            case Calendar.MINUTE:
                c.clear(Calendar.SECOND);
                /* fall through */
            case Calendar.SECOND:
                c.clear(Calendar.MILLISECOND);
                break;
            default:
                throw new IllegalStateException
                        ("No logic for rounding value (" + u + ") " + unit);
        }

    }


    private TimeZone zone;
    private Locale loc;
    private Date now;

    /**
     * @param tz The TimeZone used for rounding (to determine when hours/days begin)
     * @param l  The Locale used for rounding (to determine when weeks begin)
     * @see Calendar#getInstance(TimeZone,Locale)
     */
    public DateMathParser(TimeZone tz, Locale l) {
        zone = tz;
        loc = l;
        setNow(new Date());
    }

    /**
     * Redefines this instance's concept of "now"
     */
    public void setNow(Date n) {
        now = n;
    }

    /**
     * Returns a cloned of this instance's concept of "now"
     */
    public Date getNow() {
        return (Date) now.clone();
    }

    /**
     * Parses a string of commands relative "now" are returns the resulting Date.
     *
     * @throws java.text.ParseException positions in ParseExceptions are token positions, not character positions.
     */
    public Date parseMath(String math) throws ParseException {

        Calendar cal = Calendar.getInstance(zone, loc);
        cal.setTime(getNow());

        /* check for No-Op */
        if (0 == math.length()) {
            return cal.getTime();
        }

        String[] ops = splitter.split(math);
        int pos = 0;
        while (pos < ops.length) {

            if (1 != ops[pos].length()) {
                throw new ParseException
                        ("Multi character command found: \"" + ops[pos] + "\"", pos);
            }
            char command = ops[pos++].charAt(0);

            switch (command) {
                case '/':
                    if (ops.length < pos + 1) {
                        throw new ParseException
                                ("Need a unit after command: \"" + command + "\"", pos);
                    }
                    try {
                        round(cal, ops[pos++]);
                    } catch (IllegalArgumentException e) {
                        throw new ParseException
                                ("Unit not recognized: \"" + ops[pos - 1] + "\"", pos - 1);
                    }
                    break;
                case '+': /* fall through */
                case '-':
                    if (ops.length < pos + 2) {
                        throw new ParseException
                                ("Need a value and unit for command: \"" + command + "\"", pos);
                    }
                    int val;
                    try {
                        val = Integer.valueOf(ops[pos++]).intValue();
                    } catch (NumberFormatException e) {
                        throw new ParseException
                                ("Not a Number: \"" + ops[pos - 1] + "\"", pos - 1);
                    }
                    if ('-' == command) {
                        val = 0 - val;
                    }
                    try {
                        String unit = ops[pos++];
                        add(cal, val, unit);
                    } catch (IllegalArgumentException e) {
                        throw new ParseException
                                ("Unit not recognized: \"" + ops[pos - 1] + "\"", pos - 1);
                    }
                    break;
                default:
                    throw new ParseException
                            ("Unrecognized command: \"" + command + "\"", pos - 1);
            }
        }

        return cal.getTime();
    }

    private static Pattern splitter = Pattern.compile("\\b|(?<=\\d)(?=\\D)");
}
