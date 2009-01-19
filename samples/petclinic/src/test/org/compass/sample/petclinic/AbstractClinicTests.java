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

package org.compass.sample.petclinic;

import java.util.Collection;
import java.util.Date;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.compass.core.Compass;
import org.compass.core.CompassCallbackWithoutResult;
import org.compass.core.CompassDetachedHits;
import org.compass.core.CompassException;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTemplate;
import org.compass.gps.ActiveMirrorGpsDevice;
import org.compass.gps.CompassGps;
import org.compass.gps.CompassGpsDevice;
import org.compass.sample.petclinic.util.EntityUtils;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Base class for Clinic tests. Allows subclasses to specify context locations.
 * <p/>
 * This class extends AbstractTransactionalDataSourceSpringContextTests, one of
 * the valuable test superclasses provided in the org.springframework.test
 * package. This represents best practice for integration tests with Spring. The
 * AbstractTransactionalDataSourceSpringContextTests superclass provides the
 * following services:
 * <li>Injects test dependencies, meaning that we don't need to perform
 * application context lookups. See the setClinic() method. Injection uses
 * autowiring by type.
 * <li>Executes each test method in its own transaction, which is automatically
 * rolled back by default. This means that even if tests insert or otherwise
 * change database state, there is no need for a teardown or cleanup script.
 * <li>Provides useful inherited protected fields, such as a JdbcTemplate that
 * can be used to verify database state after test operations, or verify the
 * results of queries performed by application code. An ApplicationContext is
 * also inherited, and can be used for explicit lookup if necessary.
 * <p/>
 * The AbstractTransactionalDataSourceSpringContextTests and related classes are
 * shipped in the spring-mock.jar.
 *
 * @author Ken Krebs
 * @author Rod Johnson
 * @see org.springframework.test.AbstractTransactionalDataSourceSpringContextTests
 */
public abstract class AbstractClinicTests extends TestCase {

    private ConfigurableApplicationContext applicationContext;

    private JdbcTemplate jdbcTemplate;

    private TransactionTemplate transactionTemplate;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    protected Clinic clinic;

    /**
     * This method is provided to set the Clinic instance being tested by the
     * Dependency Injection injection behaviour of the superclass from the
     * org.springframework.test package.
     *
     * @param clinic clinic to test
     */
    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
    }

    // <!-- COMPASS START
    // Makes the tests run without a running HSQL instance

    protected CompassGps compassGps;

    protected CompassGpsDevice compassGpsDevice;

    protected CompassTemplate compassTemplate;

    protected Compass compass;

    public void setCompassGps(CompassGps compassGps) {
        this.compassGps = compassGps;
        // use read commited transaction isolation since we are performing the
        // index operation and testing within the same transaction
        //((SingleCompassGps) compassGps).setIndexTransactionIsolation(TransactionIsolation.READ_COMMITTED);
    }

    public void setCompass(Compass compass) {
        this.compass = compass;
        this.compassTemplate = new CompassTemplate(compass);
    }

    public void setCompassGpsDevice(CompassGpsDevice compassGpsDevice) {
        this.compassGpsDevice = compassGpsDevice;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    protected abstract boolean hasClassMappings();

    protected abstract String[] getConfigLocations();

    /**
     * If the gps device is active mirror type, perform the mirroring
     */
    protected void doPerformMirroringIfNeeded() {
        if (compassGpsDevice instanceof ActiveMirrorGpsDevice) {
            ((ActiveMirrorGpsDevice) compassGpsDevice).performMirroring();
        }
    }

    protected void setUp() throws Exception {
        this.applicationContext = new ClassPathXmlApplicationContext(getConfigLocations());
        this.applicationContext.getBeanFactory().autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
    }

    protected void tearDown() throws Exception {
        this.applicationContext.close();
    }


    private void performIndex() {
        compassGps.index();
    }

    // COMPASS END -->

    public void testGetVets() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                Collection vets = clinic.getVets();

                // Use the inherited JdbcTemplate (from
                // AbstractTransactionalDataSourceSpringContextTests)
                // to verify the results of the query
                assertEquals("JDBC query must show the same number of vets", jdbcTemplate
                        .queryForInt("SELECT COUNT(0) FROM VETS"), vets.size());
                Vet v1 = (Vet) EntityUtils.getById(vets, Vet.class, 2);
                assertEquals("Leary", v1.getLastName());
                assertEquals(1, v1.getNrOfSpecialties());
                assertEquals("radiology", ((Specialty) v1.getSpecialties().get(0)).getName());
                Vet v2 = (Vet) EntityUtils.getById(vets, Vet.class, 3);
                assertEquals("Douglas", v2.getLastName());
                assertEquals(2, v2.getNrOfSpecialties());
                assertEquals("dentistry", ((Specialty) v2.getSpecialties().get(0)).getName());
                assertEquals("surgery", ((Specialty) v2.getSpecialties().get(1)).getName());
            }
        });
    }

    public void testGetPetTypes() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                Collection petTypes = clinic.getPetTypes();
                assertEquals("JDBC query must show the same number of pet typess", jdbcTemplate
                        .queryForInt("SELECT COUNT(0) FROM TYPES"), petTypes.size());
                PetType t1 = (PetType) EntityUtils.getById(petTypes, PetType.class, 1);
                assertEquals("cat", t1.getName());
                PetType t4 = (PetType) EntityUtils.getById(petTypes, PetType.class, 4);
                assertEquals("snake", t4.getName());
            }
        });
    }

    public void testFindOwners() {
        Collection owners = this.clinic.findOwners("Davis");
        assertEquals(2, owners.size());
        owners = this.clinic.findOwners("Daviss");
        assertEquals(0, owners.size());
    }

    public void testLoadOwner() {
        Owner o1 = this.clinic.loadOwner(1);
        assertTrue(o1.getLastName().startsWith("Franklin"));
        Owner o10 = this.clinic.loadOwner(10);
        assertEquals("Carlos", o10.getFirstName());
    }

    public void testInsertOwner() {
        Collection owners = this.clinic.findOwners("Schultz");
        int found = owners.size();
        Owner owner = new Owner();
        owner.setLastName("Schultz");
        this.clinic.storeOwner(owner);
        owners = this.clinic.findOwners("Schultz");
        assertEquals(found + 1, owners.size());
    }

    public void testUpdateOwner() throws Exception {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                Owner o1 = clinic.loadOwner(1);
                String old = o1.getLastName();
                o1.setLastName(old + "X");
                clinic.storeOwner(o1);
                o1 = clinic.loadOwner(1);
                assertEquals(old + "X", o1.getLastName());
            }
        });
    }

    public void testLoadPet() {
        Collection types = this.clinic.getPetTypes();
        Pet p7 = this.clinic.loadPet(7);
        assertTrue(p7.getName().startsWith("Samantha"));
        assertEquals(EntityUtils.getById(types, PetType.class, 1).getId(), p7.getType().getId());
        assertEquals("Jean", p7.getOwner().getFirstName());
        Pet p6 = this.clinic.loadPet(6);
        assertEquals("George", p6.getName());
        assertEquals(EntityUtils.getById(types, PetType.class, 4).getId(), p6.getType().getId());
        assertEquals("Peter", p6.getOwner().getFirstName());
    }

    public void testInsertPet() {
        Owner o6 = this.clinic.loadOwner(6);
        int found = o6.getPets().size();
        Pet pet = new Pet();
        pet.setName("bowser");
        o6.addPet(pet);
        Collection types = this.clinic.getPetTypes();
        pet.setType((PetType) EntityUtils.getById(types, PetType.class, 2));
        pet.setBirthDate(new Date());
        assertEquals(found + 1, o6.getPets().size());
        this.clinic.storePet(pet);
        o6 = this.clinic.loadOwner(6);
        assertEquals(found + 1, o6.getPets().size());
    }

    public void testUpdatePet() throws Exception {
        Pet p7 = this.clinic.loadPet(7);
        String old = p7.getName();
        p7.setName(old + "X");
        this.clinic.storePet(p7);
        p7 = this.clinic.loadPet(7);
        assertEquals(old + "X", p7.getName());
    }

    public void testInsertVisit() {
        Pet p7 = this.clinic.loadPet(7);
        int found = p7.getVisits().size();
        Visit visit = new Visit();
        p7.addVisit(visit);
        visit.setDescription("test");
        this.clinic.storeVisit(visit);
        assertEquals(found + 1, p7.getVisits().size());
    }

    public void testCompassReindex() {
        // reindex the database
        performIndex();
        // use sporadic data to test that we reindexed the db
        compassTemplate.execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                CompassHits hits = session.find("Harold");
                assertEquals(1, hits.getLength());
                assertEquals("Harold", hits.resource(0).get(Petclinic.MetaData.FirstName.Name));
                if (hasClassMappings()) {
                    Owner owner = (Owner) hits.data(0);
                    assertEquals("Harold", owner.getFirstName());
                    assertEquals(1, owner.getPets().size());
                    Pet pet = (Pet) owner.getPets().get(0);
                    assertEquals("Iggy", pet.getName());
                }
            }
        });

        compassTemplate.execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                CompassHits hits = session.find("Leo");
                assertEquals(1, hits.getLength());
                assertEquals("Leo", hits.resource(0).get(Petclinic.MetaData.Name.Name));
                if (hasClassMappings()) {
                    Pet pet = (Pet) hits.data(0);
                    assertEquals("Leo", pet.getName());
                    assertEquals("George", pet.getOwner().getFirstName());
                }
            }
        });

        compassTemplate.execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                CompassHits hits = session.find("James");
                assertEquals(1, hits.getLength());
                assertEquals("James", hits.resource(0).get(Petclinic.MetaData.FirstName.Name));
                if (hasClassMappings()) {
                    Vet vet = (Vet) hits.data(0);
                    assertEquals("James", vet.getFirstName());
                }
            }
        });

        compassTemplate.execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                if (hasClassMappings()) {
                    Pet samantha = (Pet) session.load(Pet.class, new Integer(7));
                    assertEquals(2, samantha.getVisits().size());
                }
            }
        });

        compassTemplate.execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                CompassHits hits = session.find("George");
                assertEquals(2, hits.getLength());
                if (hasClassMappings()) {
                    hits = session.find("radiology");
                    assertEquals(2, hits.length());
                }
            }
        });
    }

    public void testInsertOwnerCompassMirror() {
        // reindex the database
        performIndex();
        // test that Schultz is not in compass
        compassTemplate.execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                CompassHits hits = session.find("Schultz");
                assertEquals(0, hits.getLength());
            }
        });
        // do the orig spring test
        Collection owners = this.clinic.findOwners("Schultz");
        int found = owners.size();
        Owner owner = new Owner();
        owner.setLastName("Schultz");
        this.clinic.storeOwner(owner);
        owners = this.clinic.findOwners("Schultz");
        assertEquals(found + 1, owners.size());
        // test that Schultz is in compass as well
        doPerformMirroringIfNeeded();
        compassTemplate.execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                CompassHits hits = session.find("Schultz");
                assertEquals(1, hits.getLength());
                assertEquals("Schultz", hits.resource(0).get(Petclinic.MetaData.LastName.Name));
                if (hasClassMappings()) {
                    Owner owner = (Owner) hits.data(0);
                    assertEquals("Schultz", owner.getLastName());
                }
            }
        });
    }

    public void testUpdateOwnerCompassMirror() throws Exception {
        // reindex the database
        performIndex();
        Owner o1 = this.clinic.loadOwner(1);
        String old = o1.getLastName();
        if (hasClassMappings()) {
            // load the owner using compass
            Owner oldCompassOwner = (Owner) compassTemplate.load(Owner.class, new Integer(1));
            // validate that it is the same as the db
            assertEquals(old, oldCompassOwner.getLastName());
        }
        CompassDetachedHits hits = compassTemplate.findWithDetach(old + "X");
        assertEquals(0, hits.getLength());
        // update and check db
        o1.setLastName(old + "X");
        this.clinic.storeOwner(o1);

        // we need to cause the ORM tool to flush it's data
        // which it won't if we call the loadOwner method
        // most ORM will flush as a result of a query on
        // the object
        // Note, that we do it since we want to test the change
        // WITHIN the same transaction, usually, we won't check
        // compass within the current transaction
        this.clinic.findOwners("Test");

        o1 = this.clinic.loadOwner(1);
        assertEquals(old + "X", o1.getLastName());
        // check with compass
        doPerformMirroringIfNeeded();
        hits = compassTemplate.findWithDetach(old + "X");
        assertEquals(1, hits.getLength());
        if (hasClassMappings()) {
            Owner newCompassOwner = (Owner) compassTemplate.load(Owner.class, new Integer(1));
            assertEquals(old + "X", newCompassOwner.getLastName());
        }
    }

    public void testInsertPetCompassMirror() {
        // reindex the database
        performIndex();
        // test that browser is not in compass
        compassTemplate.execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                CompassHits hits = session.find("bowser");
                assertEquals(0, hits.getLength());
            }
        });

        Owner o6 = this.clinic.loadOwner(6);
        int found = o6.getPets().size();
        Pet pet = new Pet();
        pet.setName("bowser");
        o6.addPet(pet);
        Collection types = this.clinic.getPetTypes();
        pet.setType((PetType) EntityUtils.getById(types, PetType.class, 2));
        pet.setBirthDate(new Date());
        assertEquals(found + 1, o6.getPets().size());
        this.clinic.storePet(pet);
        o6 = this.clinic.loadOwner(6);
        assertEquals(found + 1, o6.getPets().size());

        // test that bowser is in compass as well
        doPerformMirroringIfNeeded();
        compassTemplate.execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                CompassHits hits = session.find("bowser");
                assertEquals(1, hits.getLength());
                assertEquals("bowser", hits.resource(0).get(Petclinic.MetaData.Name.Name));
                if (hasClassMappings()) {
                    Pet pet = (Pet) hits.data(0);
                    assertEquals("bowser", pet.getName());
                }
            }
        });
    }

    public void testUpdatePetCompassMirror() throws Exception {
        // reindex the database
        performIndex();

        Pet p7 = this.clinic.loadPet(7);
        String old = p7.getName();

        // load the old pet
        if (hasClassMappings()) {
            Pet oldPet = (Pet) compassTemplate.load(Pet.class, new Integer(7));
            // check the same
            assertEquals(old, oldPet.getName());
        }
        CompassDetachedHits hits = compassTemplate.findWithDetach(old + "X");
        assertEquals(0, hits.getLength());

        p7.setName(old + "X");
        this.clinic.storePet(p7);
        p7 = this.clinic.loadPet(7);
        assertEquals(old + "X", p7.getName());

        // flush the database
        this.clinic.getPets();

        // check the updae in compass
        doPerformMirroringIfNeeded();
        if (hasClassMappings()) {
            Pet newPet = (Pet) compassTemplate.load(Pet.class, new Integer(7));
            assertEquals(old + "X", newPet.getName());
            hits = compassTemplate.findWithDetach(old + "X");
            assertEquals(1, hits.getLength()); // one pet (visits are accessed
            // using OSEM)
        } else {
            hits = compassTemplate.findWithDetach(old + "X");
            assertEquals(3, hits.getLength()); // one pet and two visits
        }
    }
}
