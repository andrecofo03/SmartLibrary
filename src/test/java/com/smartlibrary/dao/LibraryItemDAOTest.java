package com.smartlibrary.dao;

import com.smartlibrary.db.DatabaseConnection;
import com.smartlibrary.model.ElementoBibliotecario;
import com.smartlibrary.model.Libro;
import com.smartlibrary.orm.LibraryItemDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class LibraryItemDAOTest {

    @Mock private Connection mockConn;
    @Mock private PreparedStatement mockStmt;
    @Mock private ResultSet mockRs;

    private LibraryItemDAO dao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dao = new LibraryItemDAO();
    }

    @Test
    void testFindByIsbn_Trovato() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = Mockito.mockStatic(DatabaseConnection.class)) {
            DatabaseConnection instanceMock = mock(DatabaseConnection.class);
            dbMock.when(DatabaseConnection::getInstance).thenReturn(instanceMock);
            when(instanceMock.getConnection()).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);

            when(mockRs.next()).thenReturn(true);
            when(mockRs.getString("tipo")).thenReturn("CARTACEO");
            when(mockRs.getString("isbn")).thenReturn("978-TEST");
            when(mockRs.getString("titolo")).thenReturn("Test Book");
            when(mockRs.getString("autore")).thenReturn("Autore Test");
            when(mockRs.getInt("copie_disponibili")).thenReturn(5);

            ElementoBibliotecario item = dao.findByIsbn("978-TEST");

            assertNotNull(item);
            assertTrue(item instanceof Libro);
            assertEquals("978-TEST", item.getIsbn());
            verify(mockStmt).setString(1, "978-TEST");
        }
    }

    @Test
    void testFindByIsbn_NonTrovato() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = Mockito.mockStatic(DatabaseConnection.class)) {
            DatabaseConnection instanceMock = mock(DatabaseConnection.class);
            dbMock.when(DatabaseConnection::getInstance).thenReturn(instanceMock);
            when(instanceMock.getConnection()).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);

            ElementoBibliotecario item = dao.findByIsbn("ISBN-INESISTENTE");

            assertNull(item);
        }
    }

    @Test
    void testFindByCorsoEAnno_QueryCorretta() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = Mockito.mockStatic(DatabaseConnection.class)) {
            DatabaseConnection instanceMock = mock(DatabaseConnection.class);
            dbMock.when(DatabaseConnection::getInstance).thenReturn(instanceMock);
            when(instanceMock.getConnection()).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);

            dao.findByCorsoEAnno("Informatica", 2);

            verify(mockConn).prepareStatement(contains("JOIN course_books"));
            verify(mockStmt).setString(1, "Informatica");
            verify(mockStmt).setInt(2, 2);
        }
    }
}