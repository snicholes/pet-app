package com.revature.data.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.revature.beans.Status;
import com.revature.data.StatusDAO;
import com.revature.utils.ConnectionUtil;

public class StatusPostgres implements StatusDAO {
	private ConnectionUtil connUtil = ConnectionUtil.getConnectionUtil();

	@Override
	public int create(Status dataToAdd) {
		int generatedId = 0;
		
		PreparedStatement pStmt = null;
		try (Connection conn = connUtil.getConnection()) {
			// when you run DML statements, you want to manage the TCL
			conn.setAutoCommit(false);
			
			String sql = "insert into status (id,name) "
					+ "values (default, ?)";
			String[] keys = {"id"};
			pStmt = conn.prepareStatement(sql, keys);
			pStmt.setString(1, dataToAdd.getName());
			
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
		} finally {
			try {
				pStmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		
		return generatedId;
	}

	@Override
	public Status getById(int id) {
		Status status = null;
		
		PreparedStatement pStmt = null;
		try (Connection conn = connUtil.getConnection()) {
			String sql = "select * from status where id=?";
			pStmt = conn.prepareStatement(sql);
			pStmt.setInt(1, id);
			
			ResultSet resultSet = pStmt.executeQuery();
			
			if (resultSet.next()) {
				status = new Status();
				status.setId(id);
				status.setName(resultSet.getString("name"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				pStmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return status;
	}

	@Override
	public Set<Status> getAll() {
		Set<Status> statuses = new HashSet<>();
		
		PreparedStatement pStmt = null;
		try (Connection conn = connUtil.getConnection()) {
			String sql = "select * from status";
			pStmt = conn.prepareStatement(sql);
			
			ResultSet resultSet = pStmt.executeQuery();
			
			while (resultSet.next()) {
				Status status = new Status();
				status.setId(resultSet.getInt("id"));
				status.setName(resultSet.getString("name"));
				statuses.add(status);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				pStmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return statuses;
	}

	@Override
	public void update(Status dataToUpdate) {
		PreparedStatement pStmt = null;
		
		try (Connection conn = connUtil.getConnection()) {
			conn.setAutoCommit(false);
			
			String sql = "update status set "
					+ "name=? "
					+ "where id=?";
			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, dataToUpdate.getName());
			
			int rowsAffected = pStmt.executeUpdate();
			
			if (rowsAffected==1) {
				conn.commit();
			} else {
				conn.rollback();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				pStmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void delete(Status dataToDelete) {
		PreparedStatement pStmt = null;
		try (Connection conn = connUtil.getConnection()) {
			conn.setAutoCommit(false);

			String sql = "delete from status "
					+ "where id=?";
			pStmt = conn.prepareStatement(sql);
			pStmt.setInt(1, dataToDelete.getId());

			int rowsAffected = pStmt.executeUpdate();

			if (rowsAffected==1) {
				conn.commit();
			} else {
				conn.rollback();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				pStmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public Status getByName(String name) {
		Status status = null;
		
		PreparedStatement pStmt = null;
		try (Connection conn = connUtil.getConnection()) {
			String sql = "select * from status where name=?";
			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, name);
			
			ResultSet resultSet = pStmt.executeQuery();
			
			if (resultSet.next()) {
				status = new Status();
				status.setId(resultSet.getInt("id"));
				status.setName(resultSet.getString("name"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				pStmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return status;
	}

}
