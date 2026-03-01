package com.smartlibrary.view;

import com.smartlibrary.logic.WaitingListManager;
import com.smartlibrary.model.ElementoBibliotecario;
import com.smartlibrary.model.Libro;
import com.smartlibrary.orm.CourseDAO;
import com.smartlibrary.orm.LibraryItemDAO;
import com.smartlibrary.utils.InputManager.*;

import java.util.Scanner;
import java.util.List;
import java.util.Map;

public class AdminCLI {
    private ValidatedReader input;
    private LibraryItemDAO itemDAO;
    private CourseDAO courseDAO;
    private Map<Integer, String> corsiMap;

    public AdminCLI(Scanner scanner) {
        this.input = new ValidatedReader(new ConsoleReader(scanner));
        this.itemDAO = new LibraryItemDAO();
        this.courseDAO = new CourseDAO();
        aggiornaCorsiMap();
    }

    private void aggiornaCorsiMap() {
        corsiMap = courseDAO.getCorsiAsMap();
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n==========================================");
            System.out.println("   AREA AMMINISTRATORE");
            System.out.println("==========================================");
            System.out.println("1. Visualizza Catalogo Completo (Per Corso)");
            System.out.println("2. Aggiungi / Aggiorna Risorsa nel Catalogo");
            System.out.println("3. Rimuovi Libro/Ebook");
            System.out.println("4. Associa Libro a Corso di Studio");
            System.out.println("5. Rimuovi Associazione Libro-Corso");
            System.out.println("6. Visualizza Corsi di Studio");
            System.out.println("0. Esci");

            int scelta = input.readInt("Seleziona > ", 0, 6);

            switch (scelta) {
                case 1: handleVisualizzaCatalogo(); break;
                case 2: handleGestioneRisorsa(); break;
                case 3: handleRimozione(); break;
                case 4: handleAssociaLibroCorso(); break;
                case 5: handleRimuoviAssociazione(); break;
                case 6: handleVisualizzaCorsi(); break;
                case 0:
                    running = false;
                    System.out.println(">> Logout effettuato.");
                    break;
            }

            if (running) {
                System.out.println();
                input.readLine(">> Premi INVIO per tornare al menu...");
            }
        }
    }

    private void handleVisualizzaCatalogo() {
        System.out.println("\n--- FILTRA CATALOGO ---");

        if (corsiMap.isEmpty()) {
            System.out.println(">> Nessun corso presente nel sistema. Inserisci prima un libro.");
            return;
        }

        stampaCorsi();
        int idCorso = input.readInt("Seleziona ID corso: ", 1, corsiMap.size());
        String nomeCorso = corsiMap.get(idCorso);

        List<Integer> anniDisponibili = courseDAO.getAnniByCorso(nomeCorso);
        if (anniDisponibili.isEmpty()) {
            System.out.println(">> Nessun anno trovato per questo corso.");
            return;
        }

        System.out.println("Anni disponibili: " + anniDisponibili);
        int anno = input.readInt("Anno accademico: ",
                anniDisponibili.get(0),
                anniDisponibili.get(anniDisponibili.size() - 1));

        List<ElementoBibliotecario> risultati = itemDAO.findByCorsoEAnno(nomeCorso, anno);

        if (risultati.isEmpty()) {
            System.out.println(">> Nessun libro trovato per " + nomeCorso + " (Anno " + anno + ").");
        } else {
            System.out.printf("\n%-15s %-30s %-20s %-15s\n", "ISBN", "TITOLO", "AUTORE", "DISPONIBILITÀ");
            System.out.println("------------------------------------------------------------------------------------");
            for (ElementoBibliotecario e : risultati) {
                String disp = (e instanceof Libro)
                              ? "Copie: " + ((Libro) e).getCopieDisponibili()
                              : "EBOOK";

                System.out.printf("%-15s %-30s %-20s %-15s\n",
                    e.getIsbn(),
                    truncate(e.getTitolo(), 28),
                    truncate(e.getAutore(), 18),
                    disp);
            }
        }
    }


    private void handleGestioneRisorsa() {
        System.out.println("\n[AGGIUNGI / AGGIORNA RISORSA]");

        String isbn = input.readIsbn("Inserisci ISBN (13 cifre, es. 97888...): ");

        ElementoBibliotecario esistente = itemDAO.findByIsbn(isbn);

        if (esistente != null) {
            handleAggiornamentoEsistente(isbn, esistente);
        } else {
            handleInserimentoNuovo(isbn);
        }
    }

    private void handleAggiornamentoEsistente(String isbn, ElementoBibliotecario esistente) {
        System.out.println(">> Risorsa TROVATA nel sistema:");
        System.out.println("   Titolo: " + esistente.getTitolo());
        System.out.println("   Autore: " + esistente.getAutore());

        List<String> corsiAssociati = courseDAO.getCorsiByIsbn(isbn);
        if (!corsiAssociati.isEmpty()) {
            System.out.println("   Corsi:  ");
            corsiAssociati.forEach(c -> System.out.println("     - " + c));
        }

        if (esistente instanceof Libro) {
            System.out.println("   Tipo:   Cartaceo");
            System.out.println("   Copie disponibili: " + ((Libro) esistente).getCopieDisponibili());

            boolean aggiorna = input.readYesNo("Vuoi aggiungere copie? (s/n): ");
            if (aggiorna) {
                int copieAdd = input.readInt("Numero copie da aggiungere (Min 1): ", 1, 999);

                String titoloAggiornato = itemDAO.updateQuantita(isbn, copieAdd);
                if (titoloAggiornato != null) {
                    System.out.println(">> Magazzino aggiornato con successo.");
                    WaitingListManager.getInstance().itemReturned(isbn, titoloAggiornato);
                    System.out.println(">> [SYSTEM] Controllo lista d'attesa completato.");
                } else {
                    System.out.println(">> Errore durante l'aggiornamento.");
                }
            }
        } else {
            System.out.println("   Tipo:   Ebook (nessuna quantità da aggiornare)");
        }

        boolean nuovaAssociazione = input.readYesNo("Vuoi associare questo libro a un altro corso? (s/n): ");
        if (nuovaAssociazione) {
            String corso = richiediCorso();
            int anno = richiediAnno(corso);

            if (courseDAO.existsAssociazione(isbn, corso, anno)) {
                System.out.println(">> Questa associazione esiste già.");
            } else {
                courseDAO.addAssociazione(isbn, corso, anno);
                System.out.println(">> Nuova associazione creata: " + corso + " (Anno " + anno + ")");
                aggiornaCorsiMap();
            }
        }
    }

    private void handleInserimentoNuovo(String isbn) {
        System.out.println(">> ISBN non presente in catalogo. Nuovo inserimento.");

        System.out.println("\nTipo di risorsa:");
        System.out.println("1. Libro");
        System.out.println("2. Ebook");
        int tipoScelta = input.readInt("Seleziona > ", 1, 2);
        String tipo = (tipoScelta == 1) ? "CARTACEO" : "EBOOK";

        String titolo = input.readText("Inserisci Titolo: ");
        String autore = input.readText("Inserisci Autore: ");

        System.out.println("\n ASSOCIAZIONE CORSO ");
        String corso = richiediCorso();
        int anno = richiediAnno(corso);

        Object param;

        if ("CARTACEO".equals(tipo)) {
            int copie = input.readInt("Numero copie iniziali: ", 1, 1000);
            param = copie;
        } else {
            String cleanAutore = autore.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            String cleanCorso = corso.substring(0, Math.min(3, corso.length())).toLowerCase();
            String url = "https://lib.it/dl/" + cleanAutore + "_" + cleanCorso + "_" + anno + ".pdf";
            System.out.println(">> URL generato: " + url);
            param = url;
        }

        try {
            itemDAO.addItem(isbn, tipo, titolo, autore, corso, anno, param);
            System.out.println(">> " + tipo + " inserito correttamente nel catalogo");
            aggiornaCorsiMap();
        } catch (Exception e) {
            System.out.println(">> Errore durante l'inserimento: " + e.getMessage());
        }
    }


    private void handleRimozione() {
        System.out.println("\n[RIMOZIONE LIBRO/EBOOK]");
        String isbn = input.readIsbn("Inserisci ISBN del libro da rimuovere: ");

        ElementoBibliotecario item = itemDAO.findByIsbn(isbn);
        if (item == null) {
            System.out.println(">> Nessun libro trovato con questo ISBN");
            return;
        }

        List<String> corsiAssociati = courseDAO.getCorsiByIsbn(isbn);

        System.out.println("--------------------------------------------------");
        System.out.println("Trovato: " + item.getTitolo());
        System.out.println("Autore:  " + item.getAutore());
        System.out.println("Tipo:    " + (item instanceof Libro ? "Cartaceo" : "Ebook"));
        if (!corsiAssociati.isEmpty()) {
            System.out.println("Corsi:   ");
            corsiAssociati.forEach(c -> System.out.println("   - " + c));
        }
        System.out.println("--------------------------------------------------");
        System.out.println("ATTENZIONE: Procedendo verra' cancellato definitivamente dal catalogo");
        System.out.println("Verranno rimosse anche tutte le associazioni ai corsi");
        System.out.println("Se ci sono prestiti attivi non restituiti, l'operazione potrebbe fallire");

        boolean conferma = input.readYesNo("Procedere? (s/n): ");

        if (conferma) {
            boolean successo = itemDAO.deleteItem(isbn);
            if (successo) {
                System.out.println(">> Libro rimosso con successo");
                aggiornaCorsiMap();
            } else {
                System.out.println(">> Impossibile rimuovere il libro \n controlla se ci sono prestiti attivi associati a questo ISBN");
            }
        } else {
            System.out.println(">> Operazione annullata");
        }
    }

    private void handleAssociaLibroCorso() {
        System.out.println("\n[ASSOCIA LIBRO A CORSO DI STUDIO]");
        String isbn = input.readIsbn("ISBN del libro: ");

        ElementoBibliotecario item = itemDAO.findByIsbn(isbn);
        if (item == null) {
            System.out.println(">> ERRORE: Nessun libro trovato con ISBN " + isbn);
            System.out.println("   Devi prima inserire il libro nel catalogo");
            return;
        }

        System.out.println(">> Libro trovato: " + item.getTitolo() + " (" + item.getAutore() + ")");

        List<String> corsiAttuali = courseDAO.getCorsiByIsbn(isbn);
        if (!corsiAttuali.isEmpty()) {
            System.out.println(">> Associazioni già presenti:");
            corsiAttuali.forEach(c -> System.out.println("   - " + c));
        } else {
            System.out.println(">> Nessuna associazione esistente");
        }

        System.out.println("\n NUOVA ASSOCIAZIONE ");
        String corso = richiediCorso();
        int anno = richiediAnno(corso);

        if (courseDAO.existsAssociazione(isbn, corso, anno)) {
            System.out.println(">> ERRORE: Questa associazione esiste già");
        } else {
            boolean inserito = courseDAO.addAssociazione(isbn, corso, anno);
            if (inserito) {
                System.out.println(">> Associazione creata: " + item.getTitolo() + " → " + corso + " (Anno " + anno + ")");
                aggiornaCorsiMap();
            } else {
                System.out.println(">> Errore durante l'inserimento dell'associazione");
            }
        }
    }


    private void handleRimuoviAssociazione() {
        System.out.println("\n[RIMUOVI ASSOCIAZIONE LIBRO-CORSO]");
        String isbn = input.readIsbn("ISBN del libro: ");

        ElementoBibliotecario item = itemDAO.findByIsbn(isbn);
        if (item == null) {
            System.out.println(">> Nessun libro trovato con questo ISBN");
            return;
        }

        System.out.println(">> Libro: " + item.getTitolo());

        List<String> corsiAssociati = courseDAO.getCorsiByIsbn(isbn);
        if (corsiAssociati.isEmpty()) {
            System.out.println(">> Questo libro non è associato a nessun corso");
            return;
        }

        System.out.println(">> Associazioni attuali:");
        for (int i = 0; i < corsiAssociati.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + corsiAssociati.get(i));
        }

        System.out.println("\n--- SELEZIONA ASSOCIAZIONE DA RIMUOVERE ---");
        String corso = input.readText("Nome Corso: ");
        int anno = input.readInt("Anno Accademico: ", 1, 6);

        boolean conferma = input.readYesNo("Confermi la rimozione di '" + corso + " (Anno " + anno + ")'? (s/n): ");

        if (conferma) {
            boolean rimosso = courseDAO.removeAssociazione(isbn, corso, anno);
            if (rimosso) {
                System.out.println(">> Associazione rimossa con successo");
                aggiornaCorsiMap();
            } else {
                System.out.println(">> ERRORE: Associazione non trovata, verifica il nome del corso e l'anno.");
            }
        } else {
            System.out.println(">> Operazione annullata");
        }
    }

    private void handleVisualizzaCorsi() {
        System.out.println("\n--- CORSI DI STUDIO REGISTRATI ---");

        List<String> corsi = courseDAO.getAllCorsi();

        if (corsi.isEmpty()) {
            System.out.println(">> Nessun corso presente nel sistema");
            return;
        }

        System.out.printf("%-5s %-35s %-20s\n", "#", "CORSO DI STUDIO", "ANNI ATTIVI");
        System.out.println("-------------------------------------------------------------");

        int i = 1;
        for (String corso : corsi) {
            List<Integer> anni = courseDAO.getAnniByCorso(corso);
            System.out.printf("%-5d %-35s %-20s\n", i, truncate(corso, 33), anni.toString());
            i++;
        }

        System.out.println("-------------------------------------------------------------");
        System.out.println("Totale corsi: " + corsi.size());
    }

    private String richiediCorso() {
        aggiornaCorsiMap();

        if (!corsiMap.isEmpty()) {
            System.out.println("Corsi esistenti:");
            stampaCorsi();
            System.out.println((corsiMap.size() + 1) + " Inserisci corso");

            int scelta = input.readInt("Seleziona > ", 1, corsiMap.size() + 1);

            if (scelta <= corsiMap.size()) {
                return corsiMap.get(scelta);
            }
        }

        return input.readText("Nome del nuovo corso: ");
    }

    private int richiediAnno(String corso) {
        List<Integer> anniEsistenti = courseDAO.getAnniByCorso(corso);

        if (!anniEsistenti.isEmpty()) {
            System.out.println("Anni già presenti per " + corso + ": " + anniEsistenti);
        }

        return input.readInt("Anno accademico: ", 1, 6);
    }

    private void stampaCorsi() {
        corsiMap.forEach((k, v) -> System.out.println(k + ". " + v));
    }

    private String truncate(String s, int len) {
        if (s == null) return "";
        return s.length() > len ? s.substring(0, len - 3) + "..." : s;
    }
}