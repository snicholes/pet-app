package com.revature.app;

import io.javalin.Javalin;
import io.javalin.http.HttpCode;

// this static import is for the path and get/post/put methods
import static io.javalin.apibuilder.ApiBuilder.*;

import com.revature.controllers.*;

public class PetApp {

	public static void main(String[] args) {
		Javalin app = Javalin.create(config -> {
			config.enableCorsForAllOrigins();
		});
		
		app.start();
		
		// this makes sure that anything beyond a basic "get all pets"
		// requires a login token
		app.before("/pets/*", ctx -> {
			String token = ctx.header("Token");
			if (token==null) ctx.status(HttpCode.UNAUTHORIZED);
		});
		
		app.routes(() -> {
			// localhost:8080/pets
			path("/pets", () -> {
				get(PetsController::getPets);
				post(PetsController::addPet);
				
				// localhost:8080/pets/adopt/8
				path("/adopt/{id}", () -> {
					put(PetsController::adoptPet);
				});
				
				// localhost:8080/pets/8
				path("/{id}", () -> {
					get(PetsController::getPetById);
					put(PetsController::updatePet);
				});
			});
			
			path("/users", () -> {
				post(UsersController::register); // register
				path("/auth", () -> {
					post(UsersController::logIn); // login
				});
				path("/{id}", () -> {
					get(UsersController::getUserById); // get user by id
					put(UsersController::updateUser); // update user
					path("/auth", () -> {
						get(UsersController::checkLogin); // check login
					});
				});
			});
		});
	}
	
}
