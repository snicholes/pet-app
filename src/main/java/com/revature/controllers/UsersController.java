package com.revature.controllers;

import java.util.HashMap;
import java.util.Map;

import com.revature.beans.Person;
import com.revature.exceptions.IncorrectCredentialsException;
import com.revature.exceptions.UsernameAlreadyExistsException;
import com.revature.services.UserService;
import com.revature.services.UserServiceImpl;

import io.javalin.http.Context;
import io.javalin.http.HttpCode;

public class UsersController {
	private static UserService userServ = new UserServiceImpl();
	
	public static void register(Context ctx) {
		Person newUser = ctx.bodyAsClass(Person.class);
		try {
			newUser = userServ.register(newUser);
			Map<String, Integer> newIdMap = new HashMap<>();
			newIdMap.put("generatedId", newUser.getId());
			ctx.status(HttpCode.CREATED);
			ctx.json(newIdMap);
		} catch (UsernameAlreadyExistsException e) {
			ctx.status(409); // conflict
			ctx.result(e.getMessage());
		}
	}
	
	public static void logIn(Context ctx) {
		Map<String,String> credentials = ctx.bodyAsClass(Map.class);
		String username = credentials.get("username");
		String password = credentials.get("password");
		
		try {
			Person person = userServ.logIn(username, password);
			String token = Integer.toString(person.getId());
			ctx.result(token);
		} catch (IncorrectCredentialsException e) {
			ctx.status(404);
			ctx.result(e.getMessage());
		}
	}
	
	public static void checkLogin(Context ctx) {
		String token = ctx.body();
		try {
			int id = Integer.parseInt(ctx.pathParam("id"));
			Person loggedInPerson = userServ.getUserById(id);
			if (loggedInPerson!=null) {
				ctx.json(loggedInPerson);
			} else {
				ctx.status(HttpCode.UNAUTHORIZED);
			}
		} catch (NumberFormatException e) {
			ctx.status(400);
			ctx.result("User ID and token must be numeric values");
		}
	}
	
	public static void getUserById(Context ctx) {
		try {
			int userId = Integer.parseInt(ctx.pathParam("id")); // num format exception
			Person user = userServ.getUserById(userId);
			if (user != null)
				ctx.json(user);
			else
				ctx.status(404);
		} catch (NumberFormatException e) {
			ctx.status(400);
			ctx.result("User ID must be a numeric value");
		}
	}
	
	public static void updateUser(Context ctx) {
		try {
			int userId = Integer.parseInt(ctx.pathParam("id")); // num format exception
			Person userToEdit = ctx.bodyAsClass(Person.class);
			if (userToEdit != null && userToEdit.getId() == userId) {
				userToEdit = userServ.updateUser(userToEdit);
				if (userToEdit != null)
					ctx.json(userToEdit);
				else
					ctx.status(404);
			} else {
				ctx.status(HttpCode.CONFLICT);
				ctx.result("The user provided did not match the ID.");
			}
		} catch (NumberFormatException e) {
			ctx.status(400);
			ctx.result("User ID must be a numeric value");
		}
	}
}
