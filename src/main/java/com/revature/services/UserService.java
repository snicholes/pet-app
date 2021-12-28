package com.revature.services;

import java.util.Set;

import com.revature.beans.Person;
import com.revature.beans.Pet;
import com.revature.exceptions.AlreadyAdoptedException;
import com.revature.exceptions.IncorrectCredentialsException;
import com.revature.exceptions.UsernameAlreadyExistsException;

public interface UserService {
	// services represent business logic - actual user activities.
	// what can a user do?
	public Person register(Person newUser) throws UsernameAlreadyExistsException;
	public Person logIn(String username, String password) throws IncorrectCredentialsException;
	public Person getUserById(int id);
	public Person updateUser(Person userToUpdate);
	public Person adoptPet(int petId, Person newOwner) throws AlreadyAdoptedException;
	public Set<Pet> viewAvailablePets();
	public Set<Pet> searchAvailablePetsBySpecies(String species);
}
