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

package org.compass.sample.petclinic.hibernate;

import java.util.Collection;

import org.compass.sample.petclinic.Clinic;
import org.compass.sample.petclinic.Owner;
import org.compass.sample.petclinic.Pet;
import org.compass.sample.petclinic.Visit;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate implementation of the Clinic interface.
 * 
 * <p>
 * The mappings are defined in "petclinic.hbm.xml", located in the root of the
 * class path.
 * 
 * @author Juergen Hoeller
 * @since 19.10.2003
 */
public class HibernateClinic extends HibernateDaoSupport implements Clinic {

    public Collection getPets() throws DataAccessException {
        return getHibernateTemplate().find("from Pet pet order by pet.name");
    }

    public Collection getVets() throws DataAccessException {
        return getHibernateTemplate().find("from Vet vet order by vet.lastName, vet.firstName");
    }

    public Collection getPetTypes() throws DataAccessException {
        return getHibernateTemplate().find("from PetType type order by type.name");
    }

    public Collection findOwners(String lastName) throws DataAccessException {
        return getHibernateTemplate().find("from Owner owner where owner.lastName like ?", lastName + "%");
    }

    public Owner loadOwner(int id) throws DataAccessException {
        return (Owner) getHibernateTemplate().load(Owner.class, new Integer(id));
    }

    public Pet loadPet(int id) throws DataAccessException {
        return (Pet) getHibernateTemplate().load(Pet.class, new Integer(id));
    }

    public void storeOwner(Owner owner) throws DataAccessException {
        // Note: Hibernate3's merge operation does not reassociate the object
        // with the
        // current Hibernate Session. Instead, it will always copy the state
        // over to
        // a registered representation of the entity. In case of a new entity,
        // it will
        // register a copy as well, but will not update the id of the passed-in
        // object.
        // To still update the ids of the original objects too, we need to
        // register
        // Spring's IdTransferringMergeEventListener on our SessionFactory.
        getHibernateTemplate().merge(owner);
    }

    public void storePet(Pet pet) throws DataAccessException {
        getHibernateTemplate().merge(pet);
    }

    public void storeVisit(Visit visit) throws DataAccessException {
        getHibernateTemplate().merge(visit);
    }

}
