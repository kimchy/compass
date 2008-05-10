package org.compass.core.lucene.engine.store.wrapper;

import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;

/**
 * @author kimchy
 */
public class DirectoryWrapperAdapter extends Directory {

    protected Directory dir;

    public DirectoryWrapperAdapter(Directory dir) {
        this.dir = dir;
    }

    public String[] list() throws IOException {
        return dir.list();
    }

    public boolean fileExists(String name) throws IOException {
        return dir.fileExists(name);
    }

    public long fileModified(String name) throws IOException {
        return dir.fileModified(name);
    }

    public void touchFile(String name) throws IOException {
        dir.touchFile(name);
    }

    public void deleteFile(String name) throws IOException {
        dir.deleteFile(name);
    }

    public void renameFile(String from, String to) throws IOException {
        dir.renameFile(from, to);
    }

    public long fileLength(String name) throws IOException {
        return dir.fileLength(name);
    }

    public IndexOutput createOutput(String name) throws IOException {
        return dir.createOutput(name);
    }

    public IndexInput openInput(String name) throws IOException {
        return dir.openInput(name);
    }

    public Lock makeLock(String name) {
        return dir.makeLock(name);
    }

    public void close() throws IOException {
        dir.close();
    }
}
