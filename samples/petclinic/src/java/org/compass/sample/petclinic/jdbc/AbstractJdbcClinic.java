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

package org.compass.sample.petclinic.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.compass.sample.petclinic.Clinic;
import org.compass.sample.petclinic.Entity;
import org.compass.sample.petclinic.Owner;
import org.compass.sample.petclinic.Pet;
import org.compass.sample.petclinic.PetType;
import org.compass.sample.petclinic.Specialty;
import org.compass.sample.petclinic.Vet;
import org.compass.sample.petclinic.Visit;
import org.compass.sample.petclinic.util.EntityUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * Base class for JDBC implementations of the Clinic interface.
 * 
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Rob Harrop
 */
public abstract class AbstractJdbcClinic extends JdbcDaoSupport implements Clinic, CachingClinic {

    private VetsQuery vetsQuery;

    private SpecialtiesQuery specialtiesQuery;

    private VetSpecialtiesQuery vetSpecialtiesQuery;

    private OwnersByNameQuery ownersByNameQuery;

    private OwnerQuery ownerQuery;

    private OwnerInsert ownerInsert;

    private OwnerUpdate ownerUpdate;

    private PetsByOwnerQuery petsByOwnerQuery;

    private PetQuery petQuery;

    private PetInsert petInsert;

    private PetUpdate petUpdate;

    private PetTypesQuery petTypesQuery;

    private VisitsQuery visitsQuery;

    private VisitInsert visitInsert;

    private final List vets = new ArrayList();

    protected void initDao() {
        this.vetsQuery = new VetsQuery(getDataSource());
        this.specialtiesQuery = new SpecialtiesQuery(getDataSource());
        this.vetSpecialtiesQuery = new VetSpecialtiesQuery(getDataSource());
        this.petTypesQuery = new PetTypesQuery(getDataSource());
        this.ownersByNameQuery = new OwnersByNameQuery(getDataSource());
        this.ownerQuery = new OwnerQuery(getDataSource());
        this.ownerInsert = new OwnerInsert(getDataSource());
        this.ownerUpdate = new OwnerUpdate(getDataSource());
        this.petsByOwnerQuery = new PetsByOwnerQuery(getDataSource());
        this.petQuery = new PetQuery(getDataSource());
        this.petInsert = new PetInsert(getDataSource());
        this.petUpdate = new PetUpdate(getDataSource());
        this.visitsQuery = new VisitsQuery(getDataSource());
        this.visitInsert = new VisitInsert(getDataSource());
    }

    public void refreshVetsCache() throws DataAccessException {
        synchronized (this.vets) {
            logger.info("Refreshing vets cache");

            // Retrieve the list of all vets.
            this.vets.clear();
            this.vets.addAll(this.vetsQuery.execute());

            // Retrieve the list of all possible specialties.
            List specialties = this.specialtiesQuery.execute();

            // Build each vet's list of specialties.
            Iterator vi = this.vets.iterator();
            while (vi.hasNext()) {
                Vet vet = (Vet) vi.next();
                List vetSpecialtiesIds = this.vetSpecialtiesQuery.execute(vet.getId().intValue());
                Iterator vsi = vetSpecialtiesIds.iterator();
                while (vsi.hasNext()) {
                    int specialtyId = ((Integer) vsi.next()).intValue();
                    Specialty specialty = (Specialty) EntityUtils.getById(specialties, Specialty.class, specialtyId);
                    vet.addSpecialty(specialty);
                }
            }
        }
    }

    // START of Clinic implementation section *******************************

    public Collection getPets() throws DataAccessException {
        // No need to implement
        return null;
    }
    
    public Collection getVets() throws DataAccessException {
        synchronized (this.vets) {
            if (this.vets.isEmpty()) {
                refreshVetsCache();
            }
            return this.vets;
        }
    }

    public Collection getPetTypes() throws DataAccessException {
        return this.petTypesQuery.execute();
    }

    /** Method loads owners plus pets and visits if not already loaded */
    public Collection findOwners(String lastName) throws DataAccessException {
        List owners = this.ownersByNameQuery.execute(lastName + "%");
        loadOwnersPetsAndVisits(owners);
        return owners;
    }

    /** Method loads an owner plus pets and visits if not already loaded */
    public Owner loadOwner(int id) throws DataAccessException {
        Owner owner = (Owner) this.ownerQuery.findObject(id);
        if (owner == null) {
            throw new ObjectRetrievalFailureException(Owner.class, new Integer(id));
        }
        loadPetsAndVisits(owner);
        return owner;
    }

    public Pet loadPet(int id) throws DataAccessException {
        JdbcPet pet = (JdbcPet) this.petQuery.findObject(id);
        if (pet == null) {
            throw new ObjectRetrievalFailureException(Pet.class, new Integer(id));
        }
        Owner owner = loadOwner(pet.getOwnerId());
        owner.addPet(pet);
        loadVisits(pet);
        return pet;
    }

    public void storeOwner(Owner owner) throws DataAccessException {
        if (owner.isNew()) {
            this.ownerInsert.insert(owner);
        } else {
            this.ownerUpdate.update(owner);
        }
    }

    public void storePet(Pet pet) throws DataAccessException {
        if (pet.isNew()) {
            this.petInsert.insert(pet);
        } else {
            this.petUpdate.update(pet);
        }
    }

    public void storeVisit(Visit visit) throws DataAccessException {
        if (visit.isNew()) {
            this.visitInsert.insert(visit);
        } else {
            throw new UnsupportedOperationException("Visit update not supported");
        }
    }

    // END of Clinic implementation section *******************************

    /**
     * Method maps a List of Entity objects keyed to their ids.
     * 
     * @param list
     *            List containing Entity objects
     * @return Map containing Entity objects
     */
    protected final Map mapEntityList(List list) {
        Map map = new HashMap();
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();
            map.put(entity.getId(), entity);
        }
        return map;
    }

    /**
     * Method to retrieve the <code>Visit</code> data for a <code>Pet</code>.
     */
    protected void loadVisits(JdbcPet pet) {
        pet.setType((PetType) EntityUtils.getById(getPetTypes(), PetType.class, pet.getTypeId()));
        List visits = this.visitsQuery.execute(pet.getId().intValue());
        Iterator vi = visits.iterator();
        while (vi.hasNext()) {
            Visit visit = (Visit) vi.next();
            pet.addVisit(visit);
        }
    }

    /**
     * Method to retrieve the <code>Pet</code> and <code>Visit</code> data
     * for an <code>Owner</code>.
     */
    protected void loadPetsAndVisits(Owner owner) {
        List pets = this.petsByOwnerQuery.execute(owner.getId().intValue());
        Iterator pi = pets.iterator();
        while (pi.hasNext()) {
            JdbcPet pet = (JdbcPet) pi.next();
            owner.addPet(pet);
            loadVisits(pet);
        }
    }

    /**
     * Method to retrieve a <code>List</code> of <code>Owner</code>s and
     * their <code>Pet</code> and <code>Visit</code> data.
     * 
     * @param owners
     *            <code>List</code>.
     * @see #loadPetsAndVisits(Owner)
     */
    protected void loadOwnersPetsAndVisits(List owners) {
        Iterator oi = owners.iterator();
        while (oi.hasNext()) {
            Owner owner = (Owner) oi.next();
            loadPetsAndVisits(owner);
        }
    }

    /**
     * Retrieve and set the identity for the given entity, assuming that the
     * last executed insert affected that entity and generated an auto-increment
     * value for it.
     * 
     * @param entity
     *            the entity object to retrieved the id for
     * @see #getIdentityQuery
     */
    protected void retrieveIdentity(Entity entity) {
        entity.setId(new Integer(getJdbcTemplate().queryForInt(getIdentityQuery())));
    }

    /**
     * Return the identity query for the particular database: a query that can
     * be used to retrieve the id of a row that has just been inserted.
     * 
     * @return the identity query
     */
    protected abstract String getIdentityQuery();

    // ************* Operation Objects section ***************

    /**
     * Base class for all <code>Vet</code> Query Objects.
     */
    protected class VetsQuery extends MappingSqlQuery {

        /**
         * Create a new instance of VetsQuery.
         * 
         * @param ds
         *            the DataSource to use for the query
         * @param sql
         *            SQL string to use for the query
         */
        protected VetsQuery(DataSource ds, String sql) {
            super(ds, sql);
        }

        /**
         * Create a new instance of VetsQuery that returns all vets.
         * 
         * @param ds
         *            the DataSource to use for the query
         */
        protected VetsQuery(DataSource ds) {
            super(ds, "SELECT id,first_name,last_name FROM vets ORDER BY last_name,first_name");
            compile();
        }

        protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
            Vet vet = new Vet();
            vet.setId(new Integer(rs.getInt("id")));
            vet.setFirstName(rs.getString("first_name"));
            vet.setLastName(rs.getString("last_name"));
            return vet;
        }
    }

    /**
     * All <code>Vet</code>s specialties Query Object.
     */
    protected class SpecialtiesQuery extends MappingSqlQuery {

        /**
         * Create a new instance of SpecialtiesQuery.
         * 
         * @param ds
         *            the DataSource to use for the query
         */
        protected SpecialtiesQuery(DataSource ds) {
            super(ds, "SELECT id,name FROM specialties");
            compile();
        }

        protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
            Specialty specialty = new Specialty();
            specialty.setId(new Integer(rs.getInt("id")));
            specialty.setName(rs.getString("name"));
            return specialty;
        }
    }

    /**
     * A particular <code>Vet</code>'s specialties Query Object.
     */
    protected class VetSpecialtiesQuery extends MappingSqlQuery {

        /**
         * Create a new instance of VetSpecialtiesQuery.
         * 
         * @param ds
         *            the DataSource to use for the query
         */
        protected VetSpecialtiesQuery(DataSource ds) {
            super(ds, "SELECT specialty_id FROM vet_specialties WHERE vet_id=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
            return new Integer(rs.getInt("specialty_id"));
        }
    }

    /**
     * Abstract base class for all <code>Owner</code> Query Objects.
     */
    protected abstract class OwnersQuery extends MappingSqlQuery {

        /**
         * Create a new instance of OwnersQuery.
         * 
         * @param ds
         *            the DataSource to use for the query
         * @param sql
         *            SQL string to use for the query
         */
        protected OwnersQuery(DataSource ds, String sql) {
            super(ds, sql);
        }

        protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
            Owner owner = new Owner();
            owner.setId(new Integer(rs.getInt("id")));
            owner.setFirstName(rs.getString("first_name"));
            owner.setLastName(rs.getString("last_name"));
            owner.setAddress(rs.getString("address"));
            owner.setCity(rs.getString("city"));
            owner.setTelephone(rs.getString("telephone"));
            return owner;
        }
    }

    /**
     * <code>Owner</code>s by last name Query Object.
     */
    protected class OwnersByNameQuery extends OwnersQuery {

        /**
         * Create a new instance of OwnersByNameQuery.
         * 
         * @param ds
         *            the DataSource to use for the query
         */
        protected OwnersByNameQuery(DataSource ds) {
            super(ds, "SELECT id,first_name,last_name,address,city,telephone FROM owners WHERE last_name like ?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }
    }

    /**
     * <code>Owner</code> by id Query Object.
     */
    protected class OwnerQuery extends OwnersQuery {

        /**
         * Create a new instance of OwnerQuery.
         * 
         * @param ds
         *            the DataSource to use for the query
         */
        protected OwnerQuery(DataSource ds) {
            super(ds, "SELECT id,first_name,last_name,address,city,telephone FROM owners WHERE id=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }
    }

    /**
     * <code>Owner</code> Insert Object.
     */
    protected class OwnerInsert extends SqlUpdate {

        /**
         * Create a new instance of OwnerInsert.
         * 
         * @param ds
         *            the DataSource to use for the insert
         */
        protected OwnerInsert(DataSource ds) {
            super(ds, "INSERT INTO owners VALUES(?,?,?,?,?,?,?)");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            compile();
        }

        protected void insert(Owner owner) {
            Object[] objs = new Object[] { null, owner.getFirstName(), owner.getLastName(), owner.getAddress(),
                    owner.getCity(), owner.getTelephone(), new Timestamp(new java.util.Date().getTime()) };
            super.update(objs);
            retrieveIdentity(owner);
        }
    }

    /**
     * <code>Owner</code> Update Object.
     */
    protected class OwnerUpdate extends SqlUpdate {

        /**
         * Create a new instance of OwnerUpdate.
         * 
         * @param ds
         *            the DataSource to use for the update
         */
        protected OwnerUpdate(DataSource ds) {
            super(ds, "UPDATE owners SET first_name=?,last_name=?,address=?,city=?,telephone=?,version=? WHERE id=?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        /**
         * Method to update an <code>Owner</code>'s data.
         * 
         * @param owner
         *            to update
         * @return the number of rows affected by the update
         */
        protected int update(Owner owner) {
            return this.update(new Object[] { owner.getFirstName(), owner.getLastName(), owner.getAddress(),
                    owner.getCity(), owner.getTelephone(), new Timestamp(new java.util.Date().getTime()), owner.getId() });
        }
    }

    /**
     * Abstract base class for all <code>Pet</code> Query Objects.
     */
    protected abstract class PetsQuery extends MappingSqlQuery {

        /**
         * Create a new instance of PetsQuery.
         * 
         * @param ds
         *            the DataSource to use for the query
         * @param sql
         *            SQL string to use for the query
         */
        protected PetsQuery(DataSource ds, String sql) {
            super(ds, sql);
        }

        protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
            JdbcPet pet = new JdbcPet();
            pet.setId(new Integer(rs.getInt("id")));
            pet.setName(rs.getString("name"));
            pet.setBirthDate(rs.getDate("birth_date"));
            pet.setTypeId(rs.getInt("type_id"));
            pet.setOwnerId(rs.getInt("owner_id"));
            return pet;
        }
    }

    /**
     * <code>Pet</code>s by <code>Owner</code> Query Object.
     */
    protected class PetsByOwnerQuery extends PetsQuery {

        /**
         * Create a new instance of PetsByOwnerQuery.
         * 
         * @param ds
         *            the DataSource to use for the query
         */
        protected PetsByOwnerQuery(DataSource ds) {
            super(ds, "SELECT id,name,birth_date,type_id,owner_id FROM pets WHERE owner_id=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }
    }

    /**
     * <code>Pet</code> by id Query Object.
     */
    protected class PetQuery extends PetsQuery {

        /**
         * Create a new instance of PetQuery.
         * 
         * @param ds
         *            the DataSource to use for the query
         */
        protected PetQuery(DataSource ds) {
            super(ds, "SELECT id,name,birth_date,type_id,owner_id FROM pets WHERE id=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }
    }

    /**
     * <code>Pet</code> Insert Object.
     */
    protected class PetInsert extends SqlUpdate {

        /**
         * Create a new instance of PetInsert.
         * 
         * @param ds
         *            the DataSource to use for the insert
         */
        protected PetInsert(DataSource ds) {
            super(ds, "INSERT INTO pets VALUES(?,?,?,?,?,?)");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.DATE));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            compile();
        }

        /**
         * Method to insert a new <code>Pet</code>.
         * 
         * @param pet
         *            to insert
         */
        protected void insert(Pet pet) {
            Object[] objs = new Object[] { null, pet.getName(), new java.sql.Date(pet.getBirthDate().getTime()),
                    pet.getType().getId(), pet.getOwner().getId(), new Timestamp(new java.util.Date().getTime()) };
            super.update(objs);
            retrieveIdentity(pet);
        }
    }

    /**
     * <code>Pet</code> Update Object.
     */
    protected class PetUpdate extends SqlUpdate {

        /**
         * Create a new instance of PetUpdate.
         * 
         * @param ds
         *            the DataSource to use for the update
         */
        protected PetUpdate(DataSource ds) {
            super(ds, "UPDATE pets SET name=?,birth_date=?,type_id=?,owner_id=?,version=? WHERE id=?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.DATE));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        /**
         * Method to update an <code>Pet</code>'s data.
         * 
         * @param pet
         *            to update
         * @return the number of rows affected by the update
         */
        protected int update(Pet pet) {
            return this.update(new Object[] { pet.getName(), new java.sql.Date(pet.getBirthDate().getTime()),
                    pet.getType().getId(), pet.getOwner().getId(), new Timestamp(new Date().getTime()), pet.getId() });
        }
    }

    /**
     * All <code>Pet</code> types Query Object.
     */
    protected class PetTypesQuery extends MappingSqlQuery {

        /**
         * Create a new instance of PetTypesQuery.
         * 
         * @param ds
         *            the DataSource to use for the query
         */
        protected PetTypesQuery(DataSource ds) {
            super(ds, "SELECT id,name FROM types ORDER BY name");
            compile();
        }

        protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
            PetType type = new PetType();
            type.setId(new Integer(rs.getInt("id")));
            type.setName(rs.getString("name"));
            return type;
        }
    }

    /**
     * <code>Visit</code>s by <code>Pet</code> Query Object.
     */
    protected class VisitsQuery extends MappingSqlQuery {

        /**
         * Create a new instance of VisitsQuery.
         * 
         * @param ds
         *            the DataSource to use for the query
         */
        protected VisitsQuery(DataSource ds) {
            super(ds, "SELECT id,visit_date,description FROM visits WHERE pet_id=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
            Visit visit = new Visit();
            visit.setId(new Integer(rs.getInt("id")));
            visit.setDate(rs.getDate("visit_date"));
            visit.setDescription(rs.getString("description"));
            return visit;
        }
    }

    /**
     * <code>Visit</code> Insert Object.
     */
    protected class VisitInsert extends SqlUpdate {

        /**
         * Create a new instance of VisitInsert.
         * 
         * @param ds
         *            the DataSource to use for the insert
         */
        protected VisitInsert(DataSource ds) {
            super(ds, "INSERT INTO visits VALUES(?,?,?,?,?)");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.DATE));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            compile();
        }

        /**
         * Method to insert a new <code>Visit</code>.
         * 
         * @param visit
         *            to insert
         */
        protected void insert(Visit visit) {
            super.update(new Object[] { null, visit.getPet().getId(), new java.sql.Date(visit.getDate().getTime()),
                    visit.getDescription(), new Timestamp(new java.util.Date().getTime()) });
            retrieveIdentity(visit);
        }
    }

}
