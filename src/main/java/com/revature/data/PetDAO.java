package com.revature.data;

import java.util.Set;

import com.revature.beans.Pet;

//the PetDAO extends the GenericDAO in order to
//inherit the CRUD methods, and it sets the type of the
//data to be Pet objects
public interface PetDAO extends GenericDAO<Pet> {
	// here, we could add any additional behaviors that are
	// unique to accessing Pet data (not just basic CRUD)
	public Set<Pet> getByStatus(String status);
}
