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

package org.compass.sample.library;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import junit.framework.TestCase;

import org.compass.core.Compass;
import org.compass.core.CompassCallbackWithoutResult;
import org.compass.core.CompassException;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTemplate;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;

/**
 * Some tests that show the power of compass using a simple library domain
 * model. Populated with some books and authors, and by no means it is a
 * complete list.
 * 
 * @author kimchy
 */
public class LibraryTests extends TestCase {

    private Compass compass;

    private CompassTemplate compassTemplate;

    private Author jackLondon;

    private Book whiteFang;

    private Book callOfTheWild;

    private Author jamesClavell;

    private Book shogun;

    private Book taipan;

    protected void setUp() throws Exception {
        super.setUp();
        CompassConfiguration config = new CompassConfiguration().configure(
                "/org/compass/sample/library/compass.cfg.xml").addClass(Author.class).addClass(Article.class)
                .addClass(Book.class);
        compass = config.buildCompass();
        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().createIndex();
        compassTemplate = new CompassTemplate(compass);
    }

    protected void tearDown() throws Exception {
        compass.close();
        super.tearDown();
    }

    private void assertJackLondon(Author author) {
        assertEquals(jackLondon.getId(), author.getId());
        SimpleDateFormat sdf = new SimpleDateFormat(Library.MetaData.Birthdate.Format);
        assertEquals(sdf.format(jackLondon.getBirthdate()), sdf.format(author.getBirthdate()));
        assertEquals(jackLondon.getName().getFirstName(), author.getName().getFirstName());
        assertEquals(jackLondon.getName().getLastName(), author.getName().getLastName());
        assertEquals(2, author.getBooks().size());
        assertEquals("White Fang", ((Book) author.getBooks().get(0)).getTitle());
        assertEquals("The Call of the Wild", ((Book) author.getBooks().get(1)).getTitle());
    }

    public void setUpData() throws Exception {
        CompassSession session = compass.openSession();
        CompassTransaction tx = session.beginTransaction();

        jackLondon = new Author();
        jackLondon.setId(new Long(1));
        jackLondon.setName(new Name("Mr", "Jack", "London"));
        Calendar c = Calendar.getInstance();
        c.set(1876, 0, 12);
        jackLondon.setBirthdate(c.getTime());
        jackLondon.setKeywords(new String[] { "american author" });

        whiteFang = new Book();
        whiteFang.setId(new Long(1));
        whiteFang.setTitle("White Fang");
        c.set(1906, 0, 1);
        whiteFang.setPublishDate(c.getTime());
        whiteFang.setSummary("The remarkable story of a fiercely independent creature of the wild");
        whiteFang.setKeywords(new String[] { "jack london", "call of the wild" });
        jackLondon.addBook(whiteFang);
        // Need to save it explicitly for now (no cascading)
        session.save(whiteFang);

        callOfTheWild = new Book();
        callOfTheWild.setId(new Long(2));
        callOfTheWild.setTitle("The Call of the Wild");
        c.set(1903, 0, 1);
        callOfTheWild.setPublishDate(c.getTime());
        callOfTheWild.setSummary("The Call of the Wild is a tale about unbreakable spirit");
        callOfTheWild.setKeywords(new String[] { "jack london", "buck", "white fang" });
        jackLondon.addBook(callOfTheWild);
        // Need to save it explicitly for now (no cascading)
        session.save(callOfTheWild);

        session.save(jackLondon);

        jamesClavell = new Author();
        jamesClavell.setId(new Long(2));
        jamesClavell.setName(new Name("Mr", "James", "Clavell"));
        c.set(1924, 9, 10);
        jamesClavell.setBirthdate(c.getTime());
        jamesClavell.setKeywords(new String[] { "far east", "shogun", "japan", "hong kong" });

        shogun = new Book();
        shogun.setId(new Long(3));
        shogun.setTitle("Shogun");
        c.set(1975, 0, 1);
        shogun.setPublishDate(c.getTime());
        shogun.setSummary("A story of a hero who is not a person but a place and a time,"
                + " medieval Japan on the threshold of becoming a sea power");
        shogun.setKeywords(new String[] { "james clavell", "Blackthorne", "Toranaga", "japan" });
        jamesClavell.addBook(shogun);
        session.save(shogun);

        taipan = new Book();
        taipan.setId(new Long(4));
        taipan.setTitle("Taipan");
        c.set(1966, 0, 1);
        taipan.setPublishDate(c.getTime());
        taipan.setSummary("Tai-Pan is chinese for \"supreme leader\". This is the man with real power "
                + "to his hands. And such a Tai-Pan is Dirk Struan who is obsessed by his plan to make Hong Kong "
                + "the \"jewel in the crown of her British Majesty\". In 1841 he achieves his goal but he has many "
                + "enemies who try to destroy his plans. Will they succeed?");
        taipan.setKeywords(new String[] { "james clavell", "Dirk Struan", "joss", "hong kong" });
        jamesClavell.addBook(taipan);

        session.save(taipan);

        session.save(jamesClavell);

        tx.commit();
        session.close();
    }

    public void testSetUpData() throws Exception {
        setUpData();
        // The only test not using the template...
        CompassSession session = compass.openSession();
        CompassTransaction tx = null;
        try {
            tx = session.beginTransaction();
            Author author = (Author) session.load(Author.class, jackLondon.getId());
            assertJackLondon(author);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        } finally {
            session.close();
        }
    }

    public void testDeleteJackLondon() throws Exception {
        setUpData();
        compassTemplate.execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                // load jack london
                Author author = (Author) session.load(Author.class, jackLondon.getId());
                assertJackLondon(author);
                // delete it
                session.delete(author);
                // verify that we deleted jack
                author = (Author) session.get(Author.class, jackLondon.getId());
                assertNull(author);
                // no jack london books are deleted, since we don't support
                // cascading (yet)
                Book book = (Book) session.load(Book.class, whiteFang.getId());
                assertNotNull(book);
            }
        });
    }

    public void testUpdateJackLondon() throws Exception {
        setUpData();
        compassTemplate.execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                Author author = (Author) session.load(Author.class, jackLondon.getId());
                assertJackLondon(author);

                author.getName().setFirstName("New Jack");
                // have to save it (no automatic persistance yet)
                session.save(author);

                author = (Author) session.load(Author.class, jackLondon.getId());
                assertEquals("New Jack", author.getName().getFirstName());
            }
        });
    }

    public static void main(final String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Must specify query string");
            return;
        }

        String tempQuery = "";
        for (int i = 0; i < args.length; i++) {
            tempQuery += args[i] + " ";
        }
        final String query = tempQuery;

        LibraryTests libraryTests = new LibraryTests();
        libraryTests.setUp();
        libraryTests.setUpData();

        libraryTests.compassTemplate.execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                CompassHits hits = session.find(query);

                System.out.println("Found [" + hits.getLength() + "] hits for [" + args[0] + "] query");
                System.out.println("======================================================");
                for (int i = 0; i < hits.getLength(); i++) {
                    print(hits, i);
                }

                hits.close();
            }
        });

        libraryTests.tearDown();
    }

    public static void print(CompassHits hits, int hitNumber) {
        Object value = hits.data(hitNumber);
        Resource resource = hits.resource(hitNumber);
        System.out.println("ALIAS [" + resource.getAlias() + "] ID [" + ((Identifiable) value).getId() + "] SCORE ["
                + hits.score(hitNumber) + "]");
        System.out.println(":::: " + value);
        System.out.println("");
    }
}
