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

package org.compass.core.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.ResourceFactory;
import org.compass.core.cache.first.NullFirstLevelCache;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.spi.InternalCompass;
import org.compass.core.util.FileHandlerMonitor;

/**
 * @author kimchy
 */
public abstract class AbstractTestCase extends ExtendedTestCase {

    private static InternalCompass compass;

    private static FileHandlerMonitor fileHandlerMonitor;

    protected String[] getMappings() {
        return new String[0];
    }

    protected void beforeTestCase() throws Exception {
        compass = (InternalCompass) buildCompass();
        fileHandlerMonitor = FileHandlerMonitor.getFileHandlerMonitor(compass);
        fileHandlerMonitor.verifyNoHandlers();
    }

    protected void setUp() throws Exception {
        compass.getSearchEngineIndexManager().clearCache();
        try {
            compass.getSearchEngineIndexManager().deleteIndex();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (compass.getSpellCheckManager() != null) {
            try {
                compass.getSpellCheckManager().deleteIndex();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        compass.getSearchEngineIndexManager().verifyIndex();
    }

    protected void tearDown() throws Exception {
        compass.getSearchEngineIndexManager().clearCache();
        try {
            compass.getSearchEngineIndexManager().deleteIndex();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (compass.getSpellCheckManager() != null) {
            try {
                compass.getSpellCheckManager().deleteIndex();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    protected void afterTestCase() throws Exception {
        compass.close();
        fileHandlerMonitor.verifyNoHandlers();
    }

    protected Compass buildCompass() throws IOException {
        CompassConfiguration conf = buildConf();
        return conf.buildCompass();
    }

    protected CompassConfiguration buildConf() throws IOException {
        CompassConfiguration conf = createConfiguration()
                .configure("/org/compass/core/test/compass.cfg.xml");
        File testPropsFile = new File("compass.test.properties");
        if (testPropsFile.exists()) {
            Properties testProps = new Properties();
            testProps.load(new FileInputStream(testPropsFile));
            conf.getSettings().addSettings(testProps);
        }
        for (String mapping : getMappings()) {
            conf.addResource(getPackagePrefix() + mapping, AbstractTestCase.class.getClassLoader());
        }
        conf.getSettings().setSetting(CompassEnvironment.Cache.FirstLevel.TYPE, NullFirstLevelCache.class.getName());
        conf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
        addSettings(conf.getSettings());
        addExtraConf(conf);
        return conf;
    }

    protected String getPackagePrefix() {
        return "org/compass/core/test/";
    }

    protected CompassConfiguration createConfiguration() {
        return new CompassConfiguration();
    }

    protected void addExtraConf(CompassConfiguration conf) {
        // do nothing
    }

    protected void addSettings(CompassSettings settings) {

    }

    protected CompassSession openSession() {
        return compass.openSession();
    }

    public InternalCompass getCompass() {
        return compass;
    }

    public ResourceFactory getResourceFactory() {
        return getCompass().getResourceFactory();
    }

    public FileHandlerMonitor getFileHandlerMonitor() {
        return fileHandlerMonitor;
    }
}
