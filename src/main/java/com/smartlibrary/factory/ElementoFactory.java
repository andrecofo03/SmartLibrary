package com.smartlibrary.factory;

import com.smartlibrary.model.*;

public class ElementoFactory {

    public enum TipoElemento {
        CARTACEO, EBOOK
    }

    public static ElementoBibliotecario creaElemento(
            String tipo, String isbn, String titolo, String autore, 
            String corso, int anno, Object param) {
        
        if (tipo == null || isbn == null || titolo == null) {
            throw new IllegalArgumentException("I parametri obbligatori non possono essere vuoti");
        }

        TipoElemento tipoEnum;
        try {
            tipoEnum = TipoElemento.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo sconosciuto: " + tipo + 
                "Valori validi: " + java.util.Arrays.toString(TipoElemento.values()));
        }

        return switch (tipoEnum) {
            case CARTACEO -> new Libro(isbn, titolo, autore, corso, anno, (Integer) param);
            case EBOOK -> new Ebook(isbn, titolo, autore, corso, anno, (String) param);
        };
    }
}