package org.compass.core.config.binding.metadata;

import java.io.IOException;
import java.io.InputStream;

import org.compass.core.mapping.MappingException;
import org.objectweb.asm.ClassReader;

/**
 * @author kimchy
 */
public class AsmMetaDataReader implements MetaDataReader {

    public ClassMetaData getClassMetaData(InputStream is, String resourceName) throws MappingException {
        ClassReader classReader = null;
        try {
            classReader = new ClassReader(is);
        } catch (IOException e) {
            throw new MappingException("Failed to read [" + resourceName + "] class meta data using asm", e);
        }
        AsmClassMetaData asmClassMetaData = new AsmClassMetaData();
        classReader.accept(asmClassMetaData, ClassReader.SKIP_DEBUG);
        return asmClassMetaData;
    }
}
