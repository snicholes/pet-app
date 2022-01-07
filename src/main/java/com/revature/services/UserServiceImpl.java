package com.revature.services;

import java.util.Set;
import java.util.stream.Collectors;

import com.revature.beans.Person;
import com.revature.beans.Pet;
import com.revature.beans.Status;
import com.revature.data.PersonDAO;
import com.revature.data.PetDAO;
import com.revature.data.StatusDAO;
import com.revature.data.postgres.PersonPostgres;
import com.revature.data.postgres.PetPostgres;
import com.revature.data.postgres.StatusPostgres;
import com.revature.exceptions.AlreadyAdoptedException;
import com.revature.exceptions.IncorrectCredentialsException;
import com.revature.exceptions.UsernameAlreadyExistsException;

public class UserServiceImpl implements UserService {
	private PersonDAO personDao = new PersonPostgres();
	private PetDAO petDao = new PetPostgres();
	private StatusDAO statusDao = new StatusPostgres();

	@Override
	public Person register(Person newUser) throws UsernameAlreadyExistsException {
		int newId = personDao.create(newUser);
		if (newId > 0) {
			newUser.setId(newId);
			return newUser;
		} else if (newId == -1) {
			throw new UsernameAlreadyExistsException();
		}
		return null;
	}

	@Override
	public Person logIn(String username, String password) throws IncorrectCredentialsException {
		Person personFromDatabase = personDao.getByUsername(username);
		if (personFromDatabase != null && personFromDatabase.getPassword().equals(password)) {
			return personFromDatabase;
		} else {
			throw new IncorrectCredentialsException();
		}
	}
	
	@Override
	public Person getUserById(int id) {
		return personDao.getById(id);
	}
	
	@Override
	public Person updateUser(Person userToUpdate) {
		if (personDao.getById(userToUpdate.getId()) != null) {
			personDao.update(userToUpdate);
			userToUpdate = personDao.getById(userToUpdate.getId());
			return userToUpdate;
		}
		return null;
	}

	@Override
	public Person adoptPet(int petId, Person newOwner) throws AlreadyAdoptedException {
		Pet petToAdopt = petDao.getById(petId);
		if (petToAdopt!=null && petToAdopt.getStatus().getName().equals("Available")) {
			Status adoptedStatus = statusDao.getByName("Adopted");
			petToAdopt.setStatus(adoptedStatus);
			newOwner.getPets().add(petToAdopt);
			
			petDao.update(petToAdopt);
			personDao.update(newOwner);
			return newOwner;
		} else {
			throw new AlreadyAdoptedException();
		}
	}

	@Override
	public Set<Pet> viewAvailablePets() {
		return petDao.getByStatus("Available");
	}

	@Override
	public Set<Pet> searchAvailablePetsBySpecies(String species) {
		Set<Pet> availablePets = petDao.getByStatus("Available");
		
		/* 
		   using a Stream to filter the pets
		   "filter" takes in a Predicate (functional interface)
		   and iterates through each pet, adding the pet to the stream
		   if the predicate returns "true"
		*/
		availablePets = availablePets.stream()
				.filter(pet -> pet.getSpecies().toLowerCase().contains(species.toLowerCase()))
				.collect(Collectors.toSet());
		
		return availablePets;
	}
}
