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

package org.compass.core.config;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Runtime settings for Compass applies on the Session level. Reading settings
 * is first perfomed against the runtime settings set, and then the global settings.
 * Writing settings will only apply on the runtime settings and not affect the
 * global settings.
 *
 * @author kimchy
 */
public class RuntimeCompassSettings extends CompassSettings {

    private CompassSettings globalSettings;

    private CompassSettings runtimeSettings;

    public RuntimeCompassSettings(CompassSettings globalSettings) {
        this.globalSettings = globalSettings;
        this.runtimeSettings = new CompassSettings();
    }

    public void addSettings(Properties settings) {
        runtimeSettings.addSettings(settings);
    }

    public void addSettings(Map<String, Object> settings) {
        runtimeSettings.addSettings(settings);
    }

    public void addSettings(CompassSettings settings) {
        runtimeSettings.addSettings(settings);
    }

    public CompassSettings copy() {
        RuntimeCompassSettings copy = new RuntimeCompassSettings(globalSettings);
        copy.runtimeSettings = runtimeSettings.copy();
        return copy;
    }

    public String getSetting(String setting) {
        String value = runtimeSettings.getSetting(setting);
        if (value != null) {
            return value;
        }
        return globalSettings.getSetting(setting);
    }

    public Object getSettingAsObject(String setting) {
        Object value = runtimeSettings.getSettingAsObject(setting);
        if (value != null) {
            return value;
        }
        return globalSettings.getSettingAsObject(setting);
    }

    public String getSetting(String setting, String defaultValue) {
        String value = runtimeSettings.getSetting(setting);
        if (value != null) {
            return value;
        }
        return globalSettings.getSetting(setting, defaultValue);
    }

    public Map<String, CompassSettings> getSettingGroups(String settingPrefix) {
        Map<String, CompassSettings> group = runtimeSettings.getSettingGroups(settingPrefix);
        if (group.size() != 0) {
            return group;
        }
        return globalSettings.getSettingGroups(settingPrefix);
    }

    public float getSettingAsFloat(String setting, float defaultValue) {
        String value = runtimeSettings.getSetting(setting);
        if (value != null) {
            return runtimeSettings.getSettingAsFloat(setting, defaultValue);
        }
        return globalSettings.getSettingAsFloat(setting, defaultValue);
    }

    public int getSettingAsInt(String setting, int defaultValue) {
        String value = runtimeSettings.getSetting(setting);
        if (value != null) {
            return runtimeSettings.getSettingAsInt(setting, defaultValue);
        }
        return globalSettings.getSettingAsInt(setting, defaultValue);
    }

    public long getSettingAsLong(String setting, long defaultValue) {
        String value = runtimeSettings.getSetting(setting);
        if (value != null) {
            return runtimeSettings.getSettingAsLong(setting, defaultValue);
        }
        return globalSettings.getSettingAsLong(setting, defaultValue);
    }

    public boolean getSettingAsBoolean(String setting, boolean defaultValue) {
        String value = runtimeSettings.getSetting(setting);
        if (value != null) {
            return runtimeSettings.getSettingAsBoolean(setting, defaultValue);
        }
        return globalSettings.getSettingAsBoolean(setting, defaultValue);
    }

    public long getSettingAsTimeInSeconds(String setting, long defaultValue) {
        String value = runtimeSettings.getSetting(setting);
        if (value != null) {
            return runtimeSettings.getSettingAsTimeInSeconds(setting, defaultValue);
        }
        return globalSettings.getSettingAsTimeInSeconds(setting, defaultValue);
    }

    public long getSettingAsTimeInMillis(String setting, long defaultValue) {
        String value = runtimeSettings.getSetting(setting);
        if (value != null) {
            return runtimeSettings.getSettingAsTimeInMillis(setting, defaultValue);
        }
        return globalSettings.getSettingAsTimeInMillis(setting, defaultValue);
    }

    public long getSettingAsBytes(String setting, long defaultValue) {
        String value = runtimeSettings.getSetting(setting);
        if (value != null) {
            return runtimeSettings.getSettingAsBytes(setting, defaultValue);
        }
        return globalSettings.getSettingAsBytes(setting, defaultValue);
    }

    public double getSettingAsDouble(String setting, double defaultValue) {
        String value = runtimeSettings.getSetting(setting);
        if (value != null) {
            return runtimeSettings.getSettingAsDouble(setting, defaultValue);
        }
        return globalSettings.getSettingAsDouble(setting, defaultValue);
    }

    public Class getSettingAsClass(String setting, Class clazz) throws ClassNotFoundException {
        String value = runtimeSettings.getSetting(setting);
        if (value != null) {
            return runtimeSettings.getSettingAsClass(setting, clazz);
        }
        return globalSettings.getSettingAsClass(setting, clazz);
    }

    public Class getSettingAsClass(String setting, Class clazz, ClassLoader classLoader) throws ClassNotFoundException {
        String value = runtimeSettings.getSetting(setting);
        if (value != null) {
            return runtimeSettings.getSettingAsClass(setting, clazz, classLoader);
        }
        return globalSettings.getSettingAsClass(setting, clazz, classLoader);
    }

    public Object getSettingAsInstance(String setting, String defaultClass) {
        Object value = runtimeSettings.getSettingAsObject(setting);
        if (value != null) {
            return runtimeSettings.getSettingAsInstance(setting, defaultClass);
        }
        return globalSettings.getSettingAsInstance(setting, defaultClass);
    }

    public CompassSettings setSetting(String setting, String value) {
        runtimeSettings.setSetting(setting, value);
        return this;
    }

    public CompassSettings setObjectSetting(String setting, Object value) {
        runtimeSettings.setObjectSetting(setting, value);
        return this;
    }

    public CompassSettings setBooleanSetting(String setting, boolean value) {
        runtimeSettings.setBooleanSetting(setting, value);
        return this;
    }

    public CompassSettings setFloatSetting(String setting, float value) {
        runtimeSettings.setFloatSetting(setting, value);
        return this;
    }

    public CompassSettings setIntSetting(String setting, int value) {
        runtimeSettings.setIntSetting(setting, value);
        return this;
    }

    public CompassSettings setLongSetting(String setting, long value) {
        runtimeSettings.setLongSetting(setting, value);
        return this;
    }

    public CompassSettings setTimeSetting(String setting, long value, TimeUnit timeUnit) {
        runtimeSettings.setTimeSetting(setting, value, timeUnit);
        return this;
    }

    public CompassSettings setDoubleSetting(String setting, double value) {
        runtimeSettings.setDoubleSetting(setting, value);
        return this;
    }

    public CompassSettings setClassSetting(String setting, Class clazz) {
        runtimeSettings.setClassSetting(setting, clazz);
        return this;
    }

    public CompassSettings setGroupSettings(String settingPrefix, String groupName, String[] settings, Object[] values) {
        runtimeSettings.setGroupSettings(settingPrefix, groupName, settings, values);
        return this;
    }

    public Object getRegistry(Object key) {
        return globalSettings.getRegistry(key);
    }

    public void setRegistry(Object key, Object value) {
        globalSettings.setRegistry(key, value);
    }

    public Object removeRegistry(Object key) {
        return globalSettings.removeRegistry(key);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Global [").append(globalSettings).append("]");
        sb.append("Runtime [").append(runtimeSettings).append("]");
        return sb.toString();
    }
}
