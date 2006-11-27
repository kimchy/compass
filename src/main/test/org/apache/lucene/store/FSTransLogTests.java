package org.apache.lucene.store;

import java.io.IOException;

import junit.framework.TestCase;
import org.apache.lucene.index.FSTransLog;
import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.LuceneEnvironment;

/**
 * @author kimchy
 */
public class FSTransLogTests extends TestCase {

    private byte[] test = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
    
    public void testOverflowBufferOnRead() throws Exception {
        CompassSettings settings = new CompassSettings()
                .setIntSetting(LuceneEnvironment.Transaction.TransLog.READ_BUFFER_SIZE, 4);
        FSTransLog transLog = new FSTransLog();
        transLog.configure(settings);

        // first write the tests
        for (int i = 0; i < 10; i++) {
            writeData(transLog, "value" + i);
        }

        // flush the changes
        transLog.onDocumentAdded();

        for (int i = 0; i < 10; i++) {
            verifyDate(transLog, "value" + i);
        }
    }

    private void writeData(FSTransLog transLog, String fileName) throws IOException {
        IndexOutput indexOutput = transLog.getDirectory().createOutput(fileName);
        indexOutput.writeInt(-1);
        indexOutput.writeLong(10);
        indexOutput.writeInt(0);
        indexOutput.writeInt(0);
        indexOutput.writeBytes(test, 8);
        indexOutput.writeBytes(test, 5);

        indexOutput.seek(28);
        indexOutput.writeByte((byte) 8);
        indexOutput.seek(30);
        indexOutput.writeBytes(new byte[]{1, 2}, 2);

        indexOutput.close();
    }
    
    private void verifyDate(FSTransLog transLog, String fileName) throws IOException {
        assertTrue(transLog.getDirectory().fileExists(fileName));
        assertEquals(33, transLog.getDirectory().fileLength(fileName));

        IndexInput indexInput = transLog.getDirectory().openInput(fileName);
        assertEquals(-1, indexInput.readInt());
        assertEquals(10, indexInput.readLong());
        assertEquals(0, indexInput.readInt());
        assertEquals(0, indexInput.readInt());
        indexInput.readBytes(test, 0, 8);
        assertEquals((byte) 1, test[0]);
        assertEquals((byte) 8, test[7]);
        indexInput.readBytes(test, 0, 5);
        assertEquals((byte) 8, test[0]);
        assertEquals((byte) 5, test[4]);

        indexInput.seek(28);
        assertEquals((byte) 8, indexInput.readByte());
        indexInput.seek(30);
        assertEquals((byte) 1, indexInput.readByte());

        indexInput.close();
    }
}
