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

package org.compass.sample.petclinic.ojb;

import java.util.Collection;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.Query;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.compass.sample.petclinic.Clinic;
import org.compass.sample.petclinic.Owner;
import org.compass.sample.petclinic.Pet;
import org.compass.sample.petclinic.PetType;
import org.compass.sample.petclinic.Vet;
import org.compass.sample.petclinic.Visit;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ojb.support.PersistenceBrokerDaoSupport;

/**
 * OJB PersistenceBroker implementation of the Clinic interface.
 *
 * <p>The mappings are defined in "OJB-repository.xml",
 * located in the root of the class path.
 *
 * @author Juergen Hoeller
 * @since 04.07.2004
 */
public class PersistenceBrokerClinic extends PersistenceBrokerDaoSupport implements Clinic {

    public Collection getPets() throws DataAccessException {
        QueryByCriteria query = new QueryByCriteria(Pet.class);
        query.addOrderByAscending("name");
        return getPersistenceBrokerTemplate().getCollectionByQuery(query);
    }

    public Collection getVets() throws DataAccessException {
        QueryByCriteria query = new QueryByCriteria(Vet.class);
        query.addOrderByAscending("lastName");
        query.addOrderByAscending("firstName");
        return getPersistenceBrokerTemplate().getCollectionByQuery(query);
    }

    public Collection getPetTypes() throws DataAccessException {
        QueryByCriteria query = new QueryByCriteria(PetType.class);
        query.addOrderByAscending("name");
        return getPersistenceBrokerTemplate().getCollectionByQuery(query);
    }

    public Collection findOwners(String lastName) throws DataAccessException {
        Criteria criteria = new Criteria();
        criteria.addLike("lastName", lastName + "%");
        Query query = new QueryByCriteria(Owner.class, criteria);
        return getPersistenceBrokerTemplate().getCollectionByQuery(query);
    }

    public Owner loadOwner(int id) throws DataAccessException {
        return (Owner) getPersistenceBrokerTemplate().getObjectById(Owner.class, new Integer(id));
    }

    public Pet loadPet(int id) throws DataAccessException {
        return (Pet) getPersistenceBrokerTemplate().getObjectById(Pet.class, new Integer(id));
    }

    public void storeOwner(Owner owner) throws DataAccessException {
        getPersistenceBrokerTemplate().store(owner);
    }

    public void storePet(Pet pet) throws DataAccessException {
        getPersistenceBrokerTemplate().store(pet);
    }

    public void storeVisit(Visit visit) {
        getPersistenceBrokerTemplate().store(visit);
    }

}
