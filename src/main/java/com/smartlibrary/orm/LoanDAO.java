package com.smartlibrary.orm;

//cambio file
import com.smartlibrary.db.DatabaseConnection;
import com.smartlibrary.model.Prestito;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {
    public boolean hasActiveLoan(int userId, String isbn) {
        String sql = "SELECT 1 FROM loans WHERE user_id = ? AND item_isbn = ? AND returned_at IS NULL";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, isbn);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void createLoan(int userId, String isbn) {
        String sql = "INSERT INTO loans (user_id, item_isbn, start_date, due_date) VALUES (?, ?, CURRENT_DATE, CURRENT_DATE + 30)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, isbn);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Prestito> findExpiredLoans(int userId) {
        List<Prestito> list = new ArrayList<>();
        String sql = "SELECT l.*, i.titolo FROM loans l " +
                "JOIN library_items i ON l.item_isbn = i.isbn " +
                "WHERE l.user_id = ? AND l.returned_at IS NULL AND l.due_date < CURRENT_DATE";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void extendLoan(int loanId) {
        String sql = "UPDATE loans SET due_date = due_date + 14, renewal_count = renewal_count + 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, loanId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeLoan(int loanId) {
        String sql = "UPDATE loans SET returned_at = CURRENT_DATE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, loanId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeLoanByIsbn(String isbn, int userId) {
        String sql = "UPDATE loans SET returned_at = CURRENT_DATE WHERE item_isbn = ? AND user_id = ? AND returned_at IS NULL";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Prestito> getHistory(int userId) {
        List<Prestito> list = new ArrayList<>();
        String sql = "SELECT l.*, i.titolo FROM loans l " +
                "JOIN library_items i ON l.item_isbn = i.isbn " +
                "WHERE l.user_id = ? ORDER BY l.start_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Prestito mapResultSet(ResultSet rs) throws SQLException {
        return new Prestito(
                rs.getInt("id"),
                rs.getString("titolo"),
                rs.getString("item_isbn"),
                rs.getDate("start_date"),
                rs.getDate("due_date"),
                rs.getDate("returned_at"),
                rs.getInt("renewal_count"));
    }
}