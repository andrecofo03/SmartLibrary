package com.smartlibrary.model;

import com.smartlibrary.factory.ElementoFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ElementoFactoryTest {

    @Test
    void testFactoryCreationEbook() {
        ElementoBibliotecario item = ElementoFactory.creaElemento(
            "EBOOK", "456", "Digital Title", "Digital Auth",
            "Medicina", 5, "http://url"
        );

        assertTrue(item instanceof Ebook);
        assertEquals("Medicina", item.getCorsoStudi());
        assertTrue(item.isAvailable());
    }

    @Test
    void testFactoryCreationLibro() {
        ElementoBibliotecario item = ElementoFactory.creaElemento(
            "CARTACEO", "789", "Paper Title", "Paper Auth",
            "Giurisprudenza", 1, 10
        );

        assertTrue(item instanceof Libro);
        assertEquals(1, item.getAnnoAccademico());
    }

    @Test
    void testFactoryTipoSconosciuto_LanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> {
            ElementoFactory.creaElemento("AUDIOLIBRO", "999", "Title", "Auth", "Corso", 1, null);
        });
    }
}