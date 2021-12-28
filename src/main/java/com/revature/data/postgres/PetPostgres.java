package com.revature.data.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import com.revature.beans.Pet;
import com.revature.beans.Status;
import com.revature.data.PetDAO;
import com.revature.utils.ConnectionUtil;

public class PetPostgres implements PetDAO {
	private ConnectionUtil connUtil = ConnectionUtil.getConnectionUtil();

	@Override
	public int create(Pet dataToAdd) {
		int generatedId = 0;
		
		try (Connection conn = connUtil.getConnection()) {
			conn.setAutoCommit(false);
			
			String sql = "insert into pet (id,name,species,description,age,status_id) "
					+ "values (default, ?, ?, ?, ?, ?)";
			String[] keys = {"id"};
			PreparedStatement pStmt = conn.prepareStatement(sql, keys);
			pStmt.setString(1, dataToAdd.getName());
			pStmt.setString(2, dataToAdd.getSpecies());
			pStmt.setString(3, dataToAdd.getDescription());
			pStmt.setInt(4, dataToAdd.getAge());
			pStmt.setInt(5, dataToAdd.getStatus().getId());
			
			pStmt.executeUpdate();
			ResultSet resultSet = pStmt.getGeneratedKeys();
			
			if (resultSet.next()) {
				generatedId = resultSet.getInt("id");
				conn.commit();
			} else {
				conn.rollback();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		return generatedId;
	}

	@Override
	public Pet getById(int id) {
		Pet pet = null;
		
		try (Connection conn = connUtil.getConnection()) {
			String sql = "select pet.id,"
					+ " pet.name,"
					+ " species,"
					+ " description,"
					+ " age,"
					+ " status_id,"
					+ " status.name as status_name" + 
					" from pet" + 
					" join status on pet.status_id=status.id" + 
					" where id=?";
			PreparedStatement pStmt = conn.prepareStatement(sql);
			pStmt.setInt(1, id);
			
			ResultSet resultSet = pStmt.executeQuery();
			
			if (resultSet.next()) {
				pet = new Pet();
				pet.setId(id);
				pet.setName(resultSet.getString("name"));
				pet.setSpecies(resultSet.getString("species"));
				pet.setDescription(resultSet.getString("description"));
				pet.setAge(resultSet.getInt("age"));
				
				Status status = new Status();
				status.setId(resultSet.getInt("status_id"));
				status.setName(resultSet.getString("status_name"));
				pet.setStatus(status);
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return pet;
	}

	@Override
	public Set<Pet> getAll() {
		Set<Pet> allPets = new HashSet<>();
		
		try (Connection conn = connUtil.getConnection()) {
			String sql = "select pet.id,"
					+ " pet.name,"
					+ " species,"
					+ " description,"
					+ " age,"
					+ " status_id,"
					+ " status.name as status_name" + 
					" from pet" + 
					" join status on pet.status_id=status.id";
			Statement stmt = conn.createStatement();
			ResultSet resultSet = stmt.executeQuery(sql);
			
			// while the result set has another row
			while (resultSet.next()) {
				// create the Pet object
				Pet pet = new Pet();
				// pull the data from each row in the result set
				// and put it into the java object so that we can use it here
				pet.setId(resultSet.getInt("id"));
				pet.setName(resultSet.getString("name"));
				pet.setSpecies(resultSet.getString("species"));
				pet.setDescription(resultSet.getString("description"));
				pet.setAge(resultSet.getInt("age"));
				
				Status status = new Status();
				status.setId(resultSet.getInt("status_id"));
				status.setName(resultSet.getString("status_name"));
				pet.setStatus(status);
				
				allPets.add(pet);
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return allPets;
	}

	@Override
	public void update(Pet dataToUpdate) {
		try (Connection conn = connUtil.getConnection()) {
			conn.setAutoCommit(false);
			
			String sql = "update pet set "
					+ "name=?,species=?,description=?,age=?,status_id=? "
					+ "where id=?";
			PreparedStatement pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, dataToUpdate.getName());
			pStmt.setString(2, dataToUpdate.getSpecies());
			pStmt.setString(3, dataToUpdate.getDescription());
			pStmt.setInt(4, dataToUpdate.getAge());
			pStmt.setInt(5, dataToUpdate.getStatus().getId());
			pStmt.setInt(6, dataToUpdate.getId());
			
			int rowsAffected = pStmt.executeUpdate();
			
			if (rowsAffected==1) {
				conn.commit();
			} else {
				conn.rollback();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void delete(Pet dataToDelete) {
		try (Connection conn = connUtil.getConnection()) {
			conn.setAutoCommit(false);

			String sql = "delete from pet "
					+ "where id=?";
			PreparedStatement pStmt = conn.prepareStatement(sql);
			pStmt.setInt(1, dataToDelete.getId());

			int rowsAffected = pStmt.executeUpdate();

			if (rowsAffected==1) {
				sql="delete from pet_owner where pet_id=?";
				PreparedStatement pStmt2 = conn.prepareStatement(sql);
				pStmt2.setInt(1, dataToDelete.getId());
				rowsAffected = pStmt2.executeUpdate();
				
				if (rowsAffected<=1) {
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
	public Set<Pet> getByStatus(String status) {
		Set<Pet> allPets = new HashSet<>();

		try (Connection conn = connUtil.getConnection()) {
			String sql = "select pet.id,"
					+ " pet.name,"
					+ " species,"
					+ " description,"
					+ " age,"
					+ " status_id,"
					+ " status.name as status_name" + 
					" from pet" + 
					" join status on pet.status_id=status.id" + 
					" where status.name=?";
			PreparedStatement pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, status);
	
			ResultSet resultSet = pStmt.executeQuery();

			// while the result set has another row
			while (resultSet.next()) {
				// create the Pet object
				Pet pet = new Pet();
				// pull the data from each row in the result set
				// and put it into the java object so that we can use it here
				pet.setId(resultSet.getInt("id"));
				pet.setName(resultSet.getString("name"));
				pet.setSpecies(resultSet.getString("species"));
				pet.setDescription(resultSet.getString("description"));
				pet.setAge(resultSet.getInt("age"));

				Status petStatus = new Status();
				petStatus.setId(resultSet.getInt("status_id"));
				petStatus.setName(resultSet.getString("status_name"));
				pet.setStatus(petStatus);

				allPets.add(pet);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return allPets;
	}

}
