package dev.codewizz.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class Database {

	public static final String PATH = "jdbc:sqlite:test.db";

	private Connection connection;
	private boolean open;
	
	public Database() {

	}

	public void open() {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection(PATH);
			open = true;
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			open = false;
		}
		System.out.println("Opened database successfully");
	}
	
	public boolean setup() {
		if(!open) open();

		if(open) {
			executeSqlFile(connection, "sql/setup.sql");
			return true;
		}
		return false;
	}
	
	public boolean insertPlayer(String name, String ip) {
		try {
			connection.setAutoCommit(false);
			String sql = "INSERT INTO PLAYER (NAME, IP) VALUES (?, ?)";
			PreparedStatement statement = connection.prepareStatement(sql);

			statement.setString(1, name);
			statement.setString(2, ip);

			statement.execute();
			statement.close();
			connection.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void close() {
		open = false;
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void executeSqlFile(Connection conn, String filePath) {
		try (BufferedReader br = new BufferedReader(new FileReader(filePath));
			 Statement stmt = conn.createStatement()) {

			StringBuilder sql = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sql.append(line);
				if (line.trim().endsWith(";")) { // Assumes SQL commands end with semicolons
					stmt.execute(sql.toString());
					sql = new StringBuilder();
				}
			}
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
	}
}
