package com.smartlibrary.orm;

import com.smartlibrary.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class CourseDAO {

    public List<String> getAllCorsi() {
        List<String> corsi = new ArrayList<>();
        String sql = "SELECT DISTINCT corso_studi FROM course_books ORDER BY corso_studi";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                corsi.add(rs.getString("corso_studi"));
            }
        } catch (SQLException e) {
            System.out.println("Errore durante il recupero dei corsi di studio: " + e.getMessage());
        }
        return corsi;
    }

    public List<Integer> getAnniByCorso(String corsoStudi) {
        List<Integer> anni = new ArrayList<>();
        String sql = "SELECT DISTINCT anno_accademico FROM course_books WHERE corso_studi = ? ORDER BY anno_accademico";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, corsoStudi);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                anni.add(rs.getInt("anno_accademico"));
            }
        } catch (SQLException e) {
            System.out.println("Errore durante il recupero degli anni per il corso: " + corsoStudi + " - " + e.getMessage());
        }
        return anni;
    }

    public Map<Integer, String> getCorsiAsMap() {
        List<String> corsi = getAllCorsi();
        Map<Integer, String> corsiMap = new LinkedHashMap<>();

        for (int i = 0; i < corsi.size(); i++) {
            corsiMap.put(i + 1, corsi.get(i));
        }
        return corsiMap;
    }

    public boolean existsAssociazione(String isbn, String corsoStudi, int anno) {
        String sql = "SELECT 1 FROM course_books WHERE book_isbn = ? AND corso_studi = ? AND anno_accademico = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            stmt.setString(2, corsoStudi);
            stmt.setInt(3, anno);

            return stmt.executeQuery().next();
        } catch (SQLException e) {
            System.out.println("Errore durante la verifica dell'associazione per ISBN: " + isbn + " - " + e.getMessage());
            return false;
        }
    }

    public boolean addAssociazione(String isbn, String corsoStudi, int anno) {
        if (existsAssociazione(isbn, corsoStudi, anno)) {
            return false;
        }

        String sql = "INSERT INTO course_books (book_isbn, corso_studi, anno_accademico) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            stmt.setString(2, corsoStudi);
            stmt.setInt(3, anno);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Errore durante l'inserimento dell'associazione per ISBN: " + isbn + " - " + e.getMessage());
            return false;
        }
    }

    public boolean removeAssociazione(String isbn, String corsoStudi, int anno) {
        String sql = "DELETE FROM course_books WHERE book_isbn = ? AND corso_studi = ? AND anno_accademico = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            stmt.setString(2, corsoStudi);
            stmt.setInt(3, anno);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Errore durante la rimozione dell'associazione per ISBN: " + isbn + " - " + e.getMessage());
            return false;
        }
    }

    public List<String> getCorsiByIsbn(String isbn) {
        List<String> risultati = new ArrayList<>();
        String sql = "SELECT corso_studi, anno_accademico FROM course_books " +
                     "WHERE book_isbn = ? ORDER BY corso_studi, anno_accademico";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String corso = rs.getString("corso_studi");
                int anno = rs.getInt("anno_accademico");
                risultati.add(corso + " (Anno " + anno + ")");
            }
        } catch (SQLException e) {
            System.out.println("Errore durante il recupero dei corsi per ISBN: " + isbn + " - " + e.getMessage());
        }
        return risultati;
    }
}