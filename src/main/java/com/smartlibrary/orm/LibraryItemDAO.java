package com.smartlibrary.orm;

import com.smartlibrary.db.DatabaseConnection;
import com.smartlibrary.factory.ElementoFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.smartlibrary.model.ElementoBibliotecario;

public class LibraryItemDAO {

    public List<ElementoBibliotecario> findByCorsoEAnno(String corso, int anno) {
        List<ElementoBibliotecario> results = new ArrayList<>();
        
        String sql = "SELECT l.*, cb.corso_studi, cb.anno_accademico " +
                     "FROM library_items l " +
                     "JOIN course_books cb ON l.isbn = cb.book_isbn " +
                     "WHERE cb.corso_studi = ? AND cb.anno_accademico = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, corso);
            stmt.setInt(2, anno);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return results;
    }

    public void addItem(String isbn, String tipo, String titolo, String autore, String corsoStudi, int annoAccademico, Object param) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false); 

            if (!exists(conn, isbn)) {
                String sqlLibro = "INSERT INTO library_items (isbn, tipo, titolo, autore, copie_totali, copie_disponibili, download_url) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlLibro)) {
                    stmt.setString(1, isbn);
                    stmt.setString(2, tipo);
                    stmt.setString(3, titolo);
                    stmt.setString(4, autore);
                    
                    if ("CARTACEO".equals(tipo)) {
                        int copie = (Integer) param;
                        stmt.setInt(5, copie);
                        stmt.setInt(6, copie);
                        stmt.setNull(7, Types.VARCHAR);
                    } else {
                        stmt.setInt(5, 0);
                        stmt.setInt(6, 0);
                        stmt.setString(7, (String) param);
                    }
                    stmt.executeUpdate();
                }
            }
            String sqlAssoc = "INSERT INTO course_books (book_isbn, corso_studi, anno_accademico) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlAssoc)) {
                stmt.setString(1, isbn);
                stmt.setString(2, corsoStudi);     
                stmt.setInt(3, annoAccademico);   
                stmt.executeUpdate();
            }

            conn.commit(); 

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private boolean exists(Connection conn, String isbn) throws SQLException {
        String sql = "SELECT 1 FROM library_items WHERE isbn = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            return stmt.executeQuery().next();
        }
    }


    public ElementoBibliotecario findByIsbn(String isbn) {
        String sql = "SELECT * FROM library_items WHERE isbn = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapResultSetToItemBasic(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private ElementoBibliotecario mapResultSetToItem(ResultSet rs) throws SQLException {
        String tipo = rs.getString("tipo");
        Object param = "CARTACEO".equals(tipo) ? rs.getInt("copie_disponibili") : rs.getString("download_url");
        
        return ElementoFactory.creaElemento(
            tipo,
            rs.getString("isbn"),
            rs.getString("titolo"),
            rs.getString("autore"),
            rs.getString("corso_studi"),    
            rs.getInt("anno_accademico"),   
            param
        );
    }

    private ElementoBibliotecario mapResultSetToItemBasic(ResultSet rs) throws SQLException {
        String tipo = rs.getString("tipo");
        Object param = "CARTACEO".equals(tipo) ? rs.getInt("copie_disponibili") : rs.getString("download_url");
        
        return ElementoFactory.creaElemento(
            tipo,
            rs.getString("isbn"),
            rs.getString("titolo"),
            rs.getString("autore"),
            "N/A",
            0,     
            param
        );
    }
    
        public String updateQuantita(String isbn, int nuoveCopie) {
        String sqlUpdate = "UPDATE library_items SET copie_totali = copie_totali + ?, copie_disponibili = copie_disponibili + ? WHERE isbn = ?";
        String sqlSelect = "SELECT titolo FROM library_items WHERE isbn = ?";
        
        Connection conn = null;
        String titoloTrovato = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sqlSelect)) {
                stmt.setString(1, isbn);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    titoloTrovato = rs.getString("titolo");
                }
            }

            if (titoloTrovato != null) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                    stmt.setInt(1, nuoveCopie);
                    stmt.setInt(2, nuoveCopie);
                    stmt.setString(3, isbn);
                    stmt.executeUpdate();
                }
            }
            
            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return null;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        
        return titoloTrovato;
    }
    
    public void decrementaCopie(String isbn) {
        String sql = "UPDATE library_items SET copie_disponibili = copie_disponibili - 1 WHERE isbn = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void incrementaCopie(String isbn) {
        String sql = "UPDATE library_items SET copie_disponibili = copie_disponibili + 1 WHERE isbn = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean deleteItem(String isbn) {
        String sql = "DELETE FROM library_items WHERE isbn = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, isbn);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println(">> Errore SQL durante l'eliminazione: " + e.getMessage());
            return false;
        }
    }
}