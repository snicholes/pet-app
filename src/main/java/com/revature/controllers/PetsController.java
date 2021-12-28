package com.revature.controllers;

import java.util.Set;

import org.eclipse.jetty.http.HttpStatus;

import com.revature.beans.Person;
import com.revature.beans.Pet;
import com.revature.exceptions.AlreadyAdoptedException;
import com.revature.services.EmployeeService;
import com.revature.services.EmployeeServiceImpl;
import com.revature.services.UserService;
import com.revature.services.UserServiceImpl;

import io.javalin.http.Context;
import io.javalin.http.HttpCode;

public class PetsController {
	private static UserService userServ = new UserServiceImpl();
	private static EmployeeService empServ = new EmployeeServiceImpl();

	public static void getPets(Context ctx) {
		// checking if they did /pets?species=
		String speciesSearch = ctx.queryParam("species");
		if (speciesSearch != null && !"".equals(speciesSearch)) {
			Set<Pet> petsFound = userServ.searchAvailablePetsBySpecies(speciesSearch);
			ctx.json(petsFound);
		} else {
			// if they didn't put ?species
			Set<Pet> availablePets = userServ.viewAvailablePets();
			ctx.json(availablePets);
		}
	}
	
	public static void addPet(Context ctx) {
		Pet newPet = ctx.bodyAsClass(Pet.class);
		if (newPet !=null) {
			empServ.addNewPet(newPet);
			ctx.status(HttpStatus.CREATED_201);
		} else {
			ctx.status(HttpStatus.BAD_REQUEST_400);
		}
	}
	
	public static void adoptPet(Context ctx) {
		try {
			int petId = Integer.parseInt(ctx.pathParam("id")); // num format exception
			Person newOwner = ctx.bodyAsClass(Person.class);
			// returns the person with their new pet added
			newOwner = userServ.adoptPet(petId, newOwner);
			ctx.json(newOwner);
		} catch (NumberFormatException e) {
			ctx.status(400);
			ctx.result("Pet ID must be a numeric value");
		} catch (AlreadyAdoptedException e) {
			ctx.status(409); // conflict
			ctx.result(e.getMessage());
		}
	}
	
	public static void getPetById(Context ctx) {
		try {
			int petId = Integer.parseInt(ctx.pathParam("id")); // num format exception
			Pet pet = empServ.getPetById(petId);
			if (pet != null)
				ctx.json(pet);
			else
				ctx.status(404);
		} catch (NumberFormatException e) {
			ctx.status(400);
			ctx.result("Pet ID must be a numeric value");
		}
	}
	
	public static void updatePet(Context ctx) {
		try {
			int petId = Integer.parseInt(ctx.pathParam("id")); // num format exception
			Pet petToEdit = ctx.bodyAsClass(Pet.class);
			if (petToEdit != null && petToEdit.getId() == petId) {
				petToEdit = empServ.editPet(petToEdit);
				if (petToEdit != null)
					ctx.json(petToEdit);
				else
					ctx.status(404);
			} else {
				// conflict: the id doesn't match the id of the pet sent
				ctx.status(HttpCode.CONFLICT);
			}
		} catch (NumberFormatException e) {
			ctx.status(400);
			ctx.result("Pet ID must be a numeric value");
		}
	}
}
