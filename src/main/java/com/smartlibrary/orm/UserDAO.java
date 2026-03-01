package com.smartlibrary.orm;

import com.smartlibrary.db.DatabaseConnection;
import com.smartlibrary.model.*;
import com.smartlibrary.utils.SecurityUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public Utente login(String matricola, String passwordInChiaro) {
        String passwordHash = SecurityUtils.hashPassword(passwordInChiaro);
        String sql = "SELECT * FROM users WHERE matricola = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, matricola);
            stmt.setString(2, passwordHash);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String ruolo = rs.getString("ruolo");
                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                String email = rs.getString("email");

                if ("ADMIN".equals(ruolo)) {
                    return new Admin(id, matricola, nome, email);
                } else {
                    return new Studente(id, matricola, nome, email);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean register(String matricola, String password, String nome, String email, String ruolo) {
        String passwordHash = SecurityUtils.hashPassword(password);
        String sql = "INSERT INTO users (matricola, password, nome, email, ruolo) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, matricola);
            stmt.setString(2, passwordHash);
            stmt.setString(3, nome);
            stmt.setString(4, email);
            stmt.setString(5, ruolo);

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}