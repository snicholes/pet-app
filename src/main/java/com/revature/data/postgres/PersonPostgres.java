package com.revature.data.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.revature.beans.Person;
import com.revature.beans.Pet;
import com.revature.beans.Role;
import com.revature.beans.Status;
import com.revature.data.PersonDAO;
import com.revature.utils.ConnectionUtil;

public class PersonPostgres implements PersonDAO {
	private ConnectionUtil connUtil = ConnectionUtil.getConnectionUtil();

	@Override
	public int create(Person dataToAdd) {
		int generatedId = 0;
		
		// try-with-resources auto-closes resources
		try (Connection conn = connUtil.getConnection()) {
			// when you run DML statements, you want to manage the TCL
			conn.setAutoCommit(false);
			
			String sql = "insert into person (id,full_name,username,passwd,role_id) "
					+ "values (default, ?, ?, ?, ?)";
			String[] keys = {"id"}; // the name of the primary key column that will be autogenerated
			// creating the prepared statement
			PreparedStatement pStmt = conn.prepareStatement(sql, keys);
			// we need to set the values of the question marks
			pStmt.setString(1, dataToAdd.getFullName()); // question mark index starts at 1
			pStmt.setString(2, dataToAdd.getUsername());
			pStmt.setString(3, dataToAdd.getPassword());
			pStmt.setInt(4, dataToAdd.getRole().getId());
			
			// after setting the values, we can run the statement
			pStmt.executeUpdate();
			ResultSet resultSet = pStmt.getGeneratedKeys();
			
			if (resultSet.next()) { // "next" goes to the next row in the result set (or the first row)
				// getting the ID value from the result set
				generatedId = resultSet.getInt("id");
				conn.commit(); // running the TCL commit statement
			} else {
				conn.rollback();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			if (e.getMessage().contains("unique") || e.getCause().getMessage().contains("unique")) {
				return -1;
			}
		}
		
		return generatedId;
	}

	@Override
	public Person getById(int id) {
		Person person = null;
		
		try (Connection conn = connUtil.getConnection()) {
			String sql = "select id,full_name,username,passwd,"
					+ " role_id,user_role.name as role_name"
					+ " from person join user_role where id=?";
			PreparedStatement pStmt = conn.prepareStatement(sql);
			pStmt.setInt(1, id);
			
			ResultSet resultSet = pStmt.executeQuery();
			
			if (resultSet.next()) {
				person = new Person();
				person.setId(id);
				person.setFullName(resultSet.getString("full_name"));
				person.setUsername(resultSet.getString("username"));
				person.setPassword(resultSet.getString("passwd"));
				Role role = new Role();
				role.setId(resultSet.getInt("role_id"));
				role.setName(resultSet.getString("role_name"));
				person.setRole(role);

				person.setPets(getPetsByOwner(conn, person.getId()));
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return person;
	}

	@Override
	public Set<Person> getAll() {
		Set<Person> allPeople = new HashSet<>();
		
		try (Connection conn = connUtil.getConnection()) {
			String sql = "select id,full_name,username,passwd,"
					+ " role_id,user_role.name as role_name"
					+ " from person join user_role";
			Statement stmt = conn.createStatement();
			ResultSet resultSet = stmt.executeQuery(sql);
			
			// while the result set has another row
			while (resultSet.next()) {
				// create the Pet object
				Person person = new Person();
				// pull the data from each row in the result set
				// and put it into the java object so that we can use it here
				person.setId(resultSet.getInt("id"));
				person.setFullName(resultSet.getString("full_name"));
				person.setUsername(resultSet.getString("username"));
				person.setPassword(resultSet.getString("passwd"));
				Role role = new Role();
				role.setId(resultSet.getInt("role_id"));
				role.setName(resultSet.getString("role_name"));
				person.setRole(role);

				person.setPets(getPetsByOwner(conn, person.getId()));
				
				allPeople.add(person);
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return allPeople;
	}

	@Override
	public void update(Person dataToUpdate) {
		try (Connection conn = connUtil.getConnection()) {
			conn.setAutoCommit(false);
			
			String sql = "update person set "
					+ "full_name=?,username=?,passwd=?,role_id=? "
					+ "where id=?";
			PreparedStatement pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, dataToUpdate.getFullName());
			pStmt.setString(2, dataToUpdate.getUsername());
			pStmt.setString(3, dataToUpdate.getPassword());
			pStmt.setInt(4, dataToUpdate.getRole().getId());
			pStmt.setInt(5, dataToUpdate.getId());
			
			int rowsAffected = pStmt.executeUpdate();
			
			boolean petsUpdated = addNewOwnedPets(conn, dataToUpdate);
			
			if (rowsAffected<=1 && petsUpdated) {
				conn.commit();
			} else {
				conn.rollback();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void delete(Person dataToDelete) {
		try (Connection conn = connUtil.getConnection()) {
			conn.setAutoCommit(false);
			
			String sql = "delete from person "
					+ "where id=?";
			PreparedStatement pStmt = conn.prepareStatement(sql);
			pStmt.setInt(1, dataToDelete.getId());
			
			int rowsAffected = pStmt.executeUpdate();
			
			if (rowsAffected==1) {
				sql="delete from pet_owner where owner_id=?";
				PreparedStatement pStmt2 = conn.prepareStatement(sql);
				pStmt2.setInt(1, dataToDelete.getId());
				rowsAffected = pStmt2.executeUpdate();
				
				if (rowsAffected==dataToDelete.getPets().size()) {
					conn.commit();
				} else {
					conn.rollback();
				}
			} else {
				conn.rollback();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Person getByUsername(String username) {
		Person person = null;
		
		try (Connection conn = connUtil.getConnection()) {
			String sql = "select id,full_name,username,passwd,"
					+ " role_id,user_role.name as role_name"
					+ " from person join user_role where username=?";
			PreparedStatement pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, username);
			
			ResultSet resultSet = pStmt.executeQuery();
			
			if (resultSet.next()) {
				person = new Person();
				person.setId(resultSet.getInt("id"));
				person.setFullName(resultSet.getString("full_name"));
				person.setUsername(resultSet.getString("username"));
				person.setPassword(resultSet.getString("passwd"));
				Role role = new Role();
				role.setId(resultSet.getInt("role_id"));
				role.setName(resultSet.getString("role_name"));
				person.setRole(role);

				person.setPets(getPetsByOwner(conn, person.getId()));
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return person;
	}

	private List<Pet> getPetsByOwner(Connection conn, int personId) throws SQLException {
		List<Pet> pets = new LinkedList<>();
		
		String sql = "select p.id,"
				+ " p.name,"
				+ " species,"
				+ " description,"
				+ " age,"
				+ " status_id,"
				+ " status.name as status_name,"
				+ " pet_id,"
				+ " owner_id" + 
				" from"
				+ " (select * from pet join pet_owner on pet.id=pet_owner.pet_id where owner_id=?) p" + 
				" join status on p.status_id=status.id";
		PreparedStatement pStmt = conn.prepareStatement(sql);
		pStmt.setInt(1, personId);
		
		ResultSet resultSet = pStmt.executeQuery();
		
		while (resultSet.next()) {
			Pet pet = new Pet();
			pet.setId(resultSet.getInt("id"));
			pet.setName(resultSet.getString("name"));
			pet.setSpecies(resultSet.getString("species"));
			pet.setDescription(resultSet.getString("description"));
			pet.setAge(resultSet.getInt("age"));
			
			Status status = new Status();
			status.setId(resultSet.getInt("status_id"));
			status.setName(resultSet.getString("status_name"));
			pet.setStatus(status);
			
			pets.add(pet);
		}
		
		return pets;
	}
	
	private boolean addNewOwnedPets(Connection conn, Person person) throws SQLException {
		String sql = "insert into pet_owner (pet_id,owner_id) values ";
		for (int i = 0; i < person.getPets().size(); i++) {
			sql += "(?,?)";
			if (i!=person.getPets().size()-1) sql+= ",";
		}
		
		PreparedStatement pStmt = conn.prepareStatement(sql);
		
		int parameterIndex = 1;
		for (Pet pet : person.getPets()) {
			pStmt.setInt(parameterIndex++, pet.getId());
			pStmt.setInt(parameterIndex++, person.getId());
		}
		
		int rowsAffected = pStmt.executeUpdate();
		if (rowsAffected==person.getPets().size()) {
			return true;
		}
		return false;
	}
}