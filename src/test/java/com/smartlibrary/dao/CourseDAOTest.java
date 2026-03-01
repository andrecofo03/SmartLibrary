package com.smartlibrary.dao;

import com.smartlibrary.db.DatabaseConnection;
import com.smartlibrary.orm.CourseDAO;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class CourseDAOTest {

    @Mock private Connection mockConn;
    @Mock private PreparedStatement mockStmt;
    @Mock private ResultSet mockRs;

    private CourseDAO courseDAO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        courseDAO = new CourseDAO();
    }

    @Test
    void testOttieniCorsi() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = Mockito.mockStatic(DatabaseConnection.class)) {
            setupMockConnection(dbMock);

            when(mockRs.next()).thenReturn(true, true, true, false);
            when(mockRs.getString("corso_studi"))
                .thenReturn("Giurisprudenza")
                .thenReturn("Ingegneria Informatica")
                .thenReturn("Medicina e Chirurgia");

            List<String> corsi = courseDAO.getAllCorsi();

            assertEquals(3, corsi.size());
            assertEquals("Giurisprudenza", corsi.get(0));
            assertEquals("Ingegneria Informatica", corsi.get(1));
            assertEquals("Medicina e Chirurgia", corsi.get(2));

            verify(mockConn).prepareStatement(contains("DISTINCT corso_studi"));
        }
    }

    @Test
    void testAGgiungiAssociazioneEsistente() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = Mockito.mockStatic(DatabaseConnection.class)) {
            setupMockConnection(dbMock);
            when(mockRs.next()).thenReturn(true);

            boolean result = courseDAO.addAssociazione("978-DUP", "Informatica", 1);

            assertFalse(result, "Non deve inserire un'associazione duplicata");
        }
    }

    @Test
    void testGetCorsiByIsbn_LibroMultiCorso() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = Mockito.mockStatic(DatabaseConnection.class)) {
            setupMockConnection(dbMock);

            when(mockRs.next()).thenReturn(true, true, false);
            when(mockRs.getString("corso_studi"))
                .thenReturn("Informatica")
                .thenReturn("Matematica");
            when(mockRs.getInt("anno_accademico"))
                .thenReturn(1)
                .thenReturn(2);

            List<String> corsi = courseDAO.getCorsiByIsbn("978-MULTI");

            assertEquals(2, corsi.size());
            assertEquals("Informatica (Anno 1)", corsi.get(0));
            assertEquals("Matematica (Anno 2)", corsi.get(1));
        }
    }

    private void setupMockConnection(MockedStatic<DatabaseConnection> dbMock) throws SQLException {
        DatabaseConnection instanceMock = mock(DatabaseConnection.class);
        dbMock.when(DatabaseConnection::getInstance).thenReturn(instanceMock);
        when(instanceMock.getConnection()).thenReturn(mockConn);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
    }
}