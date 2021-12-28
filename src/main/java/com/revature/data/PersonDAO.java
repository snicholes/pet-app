package com.revature.data;

import com.revature.beans.Person;

// the PersonDAO extends the GenericDAO in order to
// inherit the CRUD methods, and it sets the type of the
// data to be Person objects
public interface PersonDAO extends GenericDAO<Person> {
	// here, we could add any additional behaviors that are
	// unique to accessing Person data (not just basic CRUD)
	public Person getByUsername(String username);
}
