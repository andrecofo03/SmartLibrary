package com.smartlibrary.model;

import org.junit.jupiter.api.Test;
import java.sql.Date;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class PrestitoTest {

    @Test
    void testIsScaduto_ScadenzaPassata_NonRestituito() {
        Date scadenzaIeri = Date.valueOf(LocalDate.now().minusDays(1));
        Date inizio = Date.valueOf(LocalDate.now().minusDays(31));

        Prestito p = new Prestito(1, "Libro", "123", inizio, scadenzaIeri, null, 0);

        assertTrue(p.isExpired());
    }

    @Test
    void testIsScaduto_ScadenzaPassata_MaRestituito() {
        Date scadenzaIeri = Date.valueOf(LocalDate.now().minusDays(1));
        Date restituitoOggi = Date.valueOf(LocalDate.now());

        Prestito p = new Prestito(1, "Libro", "123", null, scadenzaIeri, restituitoOggi, 0);

        assertFalse(p.isExpired());
    }

    @Test
    void testIsRinnovabile() {
        Prestito p0 = new Prestito(1, "T", "1", null, null, null, 0);
        assertTrue(p0.isRenewable());

        Prestito p1 = new Prestito(1, "T", "1", null, null, null, 1);
        assertTrue(p1.isRenewable());

        Prestito p2 = new Prestito(1, "T", "1", null, null, null, 2);
        assertFalse(p2.isRenewable());
    }
}