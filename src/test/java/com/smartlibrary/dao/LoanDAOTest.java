package com.smartlibrary.dao;

import com.smartlibrary.db.DatabaseConnection;
import com.smartlibrary.orm.LoanDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.contains;

class LoanDAOTest {

    @Mock private Connection mockConn;
    @Mock private PreparedStatement mockStmt;
    private LoanDAO loanDAO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loanDAO = new LoanDAO();
    }

    @Test
    void testCreateLoan() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = Mockito.mockStatic(DatabaseConnection.class)) {
            DatabaseConnection instanceMock = mock(DatabaseConnection.class);
            dbMock.when(DatabaseConnection::getInstance).thenReturn(instanceMock);
            when(instanceMock.getConnection()).thenReturn(mockConn);
            
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);

            loanDAO.createLoan(10, "978-88");

            verify(mockConn).prepareStatement(contains("INSERT INTO loans"));
            verify(mockStmt).setInt(1, 10);      
            verify(mockStmt).setString(2, "978-88"); 
            verify(mockStmt).executeUpdate();
        }
    }

    @Test
    void testExtendLoan() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = Mockito.mockStatic(DatabaseConnection.class)) {
            DatabaseConnection instanceMock = mock(DatabaseConnection.class);
            dbMock.when(DatabaseConnection::getInstance).thenReturn(instanceMock);
            when(instanceMock.getConnection()).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);

            loanDAO.extendLoan(42);

            verify(mockConn).prepareStatement(contains("due_date + 14"));
            verify(mockConn).prepareStatement(contains("renewal_count = renewal_count + 1"));
            verify(mockStmt).setInt(1, 42);
            verify(mockStmt).executeUpdate();
        }
    }

    @Test
    void testCloseLoan() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = Mockito.mockStatic(DatabaseConnection.class)) {
            DatabaseConnection instanceMock = mock(DatabaseConnection.class);
            dbMock.when(DatabaseConnection::getInstance).thenReturn(instanceMock);
            when(instanceMock.getConnection()).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);

            loanDAO.closeLoan(99);

            verify(mockConn).prepareStatement(contains("returned_at = CURRENT_DATE"));
            verify(mockStmt).setInt(1, 99);
            verify(mockStmt).executeUpdate();
        }
    }

}