package com.revature.services;

import com.revature.beans.Pet;
import com.revature.data.PetDAO;
import com.revature.data.postgres.PetPostgres;

public class EmployeeServiceImpl implements EmployeeService {
	private PetDAO petDao = new PetPostgres();

	@Override
	public int addNewPet(Pet newPet) {
		return petDao.create(newPet);
	}

	@Override
	public Pet editPet(Pet petToEdit) {
		Pet petFromDatabase = petDao.getById(petToEdit.getId());
		if (petFromDatabase != null) {
			petDao.update(petToEdit);
			return petDao.getById(petToEdit.getId());
		}
		return null;
	}

	@Override
	public Pet getPetById(int id) {
		return petDao.getById(id);
	}

}
