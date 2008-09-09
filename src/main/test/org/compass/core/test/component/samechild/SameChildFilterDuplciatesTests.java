package org.compass.core.test.component.samechild;

import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;

/**
 * @author kimchy
 */
public class SameChildFilterDuplciatesTests extends SameChildTests {

    protected void addSettings(CompassSettings settings) {
        super.addSettings(settings);
        settings.setBooleanSetting(CompassEnvironment.Osem.FILTER_DUPLICATES, true);
    }
}
