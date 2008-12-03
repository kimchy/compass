package org.compass.core.config.loader;

import java.io.File;

import org.compass.core.Compass;
import org.compass.core.config.CompassConfiguration;

/**
 * Compass main allowin to run a Compass instance.
 *
 * <p>Accepts optional configuration location paramter (the last one), and one or more
 * key=value pairs to be replaced in teh configuration file.
 *
 * @author kimchy
 */
public class CompassMain {

    public static void main(String[] args) throws Exception {
        String configPath = null;
        if (args.length > 0) {
            for (String arg : args) {
                if (arg.indexOf('=') != -1) {
                    System.setProperty(arg.substring(0, arg.indexOf('=')), arg.substring(arg.indexOf('=') + 1));
                }
            }
            if (args[args.length - 1].indexOf('=') == -1) {
                configPath = args[args.length - 1];
            }
        }
        CompassConfiguration conf = new CompassConfiguration();
        if (configPath != null) {
            conf.configure(new File(configPath));
        }
        final Compass compass = conf.buildCompass();
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    compass.close();
                } finally {
                    mainThread.interrupt();
                }
            }
        });
        while (!mainThread.isInterrupted()) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                // do nothing, simply exit
            }
        }
    }
}
