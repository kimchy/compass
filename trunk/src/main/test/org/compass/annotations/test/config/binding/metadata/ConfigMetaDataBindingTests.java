package org.compass.annotations.test.config.binding.metadata;

import java.io.IOException;
import java.io.InputStream;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.Compass;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.binding.metadata.AsmMetaDataReader;
import org.compass.core.config.binding.metadata.JavassistMetaDataReader;
import org.compass.core.spi.InternalCompass;
import org.compass.core.util.ClassUtils;

/**
 * @author kimchy
 */
public class ConfigMetaDataBindingTests extends AbstractAnnotationsTestCase {

    public void testDefaultAddingInputStream() throws Exception {
        CompassConfiguration conf = buildConf();
        verifyInputStreamLoaded(conf);
    }

    public void testAsmAddingInputStream() throws Exception {
        CompassConfiguration conf = buildConf();
        conf.getSettings().setObjectSetting(CompassEnvironment.Scanner.READER, new AsmMetaDataReader());
        verifyInputStreamLoaded(conf);
    }

    public void testJavassistAddingInputStream() throws Exception {
        CompassConfiguration conf = buildConf();
        conf.getSettings().setObjectSetting(CompassEnvironment.Scanner.READER, new JavassistMetaDataReader());
        verifyInputStreamLoaded(conf);
    }

    private void verifyInputStreamLoaded(CompassConfiguration conf) throws IOException {
        InputStream is = A.class.getClassLoader().getResourceAsStream(ClassUtils.convertClassNameToResourcePath(A.class.getName()) + ".class");
        conf.addInputStream(is, ClassUtils.convertClassNameToResourcePath(A.class.getName()) + ".class");
        is.close();

        Compass compass = conf.buildCompass();
        assertNotNull(((InternalCompass) compass).getMapping().getRootMappingByClass(A.class));
        compass.close();
    }
}
