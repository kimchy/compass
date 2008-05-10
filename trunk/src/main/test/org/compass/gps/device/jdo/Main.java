/*
 * Copyright 2004-2006 the original author or authors.
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

package org.compass.gps.device.jdo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.jdo.Extent;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * Controlling application for the JPOX Tutorial. Relies on the user defining a
 * file jpox.properties to be in the CLASSPATH and to include the JDO properties
 * for the JPOX PersistenceManager.
 * 
 */
public class Main {
    public static void main(String args[]) throws IOException {
        System.out.println("JPOX Tutorial");
        System.out.println("=============");

        PersistenceManager pm = createPersistenceManager();
        System.out.println("Created a PersistenceManager");

        // Persistence of a Product and a Book.
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            Product product = new Product("Sony Discman", "A standard discman from Sony", 49.99);
            Book book = new Book("Lord of the Rings by Tolkien", "The classic story", 49.99, "JRR Tolkien", "12345678",
                    "MyBooks Factory");
            pm.makePersistent(product);
            pm.makePersistent(book);

            tx.commit();
            System.out.println("Product and Book have been persisted");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
        System.out.println("");

        // Basic Extent
        pm = createPersistenceManager();
        tx = pm.currentTransaction();
        try {
            tx.begin();
            Extent e = pm.getExtent(Product.class, true);
            Iterator iter = e.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                System.out.println("Extent returned object : " + obj);
            }
            tx.commit();
        } catch (Exception e) {
            System.out.println("Exception thrown during retrieval of Extent : " + e.getMessage());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }

        // Perform some query operations
        pm = createPersistenceManager();
        tx = pm.currentTransaction();
        try {
            tx.begin();
            System.out.println("Executing Query");
            Extent e = pm.getExtent(Product.class, true);
            Query q = pm.newQuery(e, "price < 150.00");
            q.setOrdering("price ascending");
            Collection c = (Collection) q.execute();
            Iterator iter = c.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                System.out.println(">  " + obj);

                // Give an example of an update
                if (obj instanceof Book) {
                    Book b = (Book) obj;
                    b.setDescription("This book has been reduced in price!");
                }
            }

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
        System.out.println("");
        System.out.println("End of Tutorial");
    }

    /**
     * Utility method to create a PersistenceManager
     * 
     * @return The PersistenceManager
     */
    private static PersistenceManager createPersistenceManager() throws IOException {
        Properties properties = new Properties();

        InputStream is = Main.class.getClassLoader().getResourceAsStream("jpox.properties");
        if (is == null) {
            throw new FileNotFoundException(
                    "Could not find jpox.properties file that defines the JPOX persistence setup.");
        }
        properties.load(is);

        PersistenceManagerFactory pmfactory = JDOHelper.getPersistenceManagerFactory(properties);
        PersistenceManager pm = pmfactory.getPersistenceManager();

        return pm;
    }
}
