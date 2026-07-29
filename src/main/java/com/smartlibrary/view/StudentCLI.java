package com.smartlibrary.view;
import com.smartlibrary.model.Studente;
import com.smartlibrary.orm.CourseDAO;
import com.smartlibrary.orm.LibraryItemDAO;
import com.smartlibrary.orm.LoanDAO;
import com.smartlibrary.model.Prestito;
import com.smartlibrary.model.ElementoBibliotecario;
import com.smartlibrary.model.Libro;
import com.smartlibrary.logic.LoanService;
import com.smartlibrary.utils.ConsoleInputManager;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
public class StudentCLI {
    private ConsoleInputManager input;
    private Studente currentStudent;
    private LibraryItemDAO itemDAO;
    private LoanService loanService;
    private CourseDAO courseDAO;
    private Map<Integer, String> corsiMap;
    private String lastAction = null;
    private String lastIsbn = null;
    public StudentCLI(Scanner scanner, Studente student) {
        this.input = new ConsoleInputManager(scanner);
        this.currentStudent = student;
        this.itemDAO = new LibraryItemDAO();
        this.loanService = new LoanService(this.itemDAO, new LoanDAO());
        this.courseDAO = new CourseDAO();
        inizializzaCorsi();
    }
    private void inizializzaCorsi() {
        corsiMap = courseDAO.getCorsiAsMap();
        if (corsiMap.isEmpty()) {
            System.out.println(">> ATTENZIONE: Nessun corso trovato");
        }
    }
    public void start() {
        checkScadenzeCritiche();
        boolean running = true;
        while (running) {
            System.out.println("\n==========================================");
            System.out.println("   AREA STUDENTE: " + currentStudent.getNome());
            System.out.println("==========================================");
            System.out.println("1. Visualizza Libri Disponibili");
            System.out.println("2. Richiedi prestito / Scarica ebook");
            System.out.println("3. Restituisci libro");
            System.out.println("4. Il tuo storico prestiti");
            System.out.println("9. Annulla l'ultima operazione (Undo)");
            System.out.println("0. Esci");
            int scelta = input.readInt("Seleziona > ", 0, 9);
            if (scelta == 0) {
                running = false;
                System.out.println(">> Logout effettuato.");
            } else if (scelta == 9) {
                if (lastAction != null && lastIsbn != null) {
                    if ("LOAN".equals(lastAction)) {
                        loanService.restituisciLibro(lastIsbn, currentStudent.getId());
                        System.out.println(">> UNDO: Prestito annullato. Il libro è stato restituito.");
                    } else if ("RETURN".equals(lastAction)) {
                        loanService.richiediPrestito(currentStudent, lastIsbn);
                        System.out.println(">> UNDO: Restituzione annullata. Il libro è tornato in tuo possesso.");
                    }
                    lastAction = null;
                    lastIsbn = null;
                } else {
                    System.out.println(">> Nessuna operazione recente da annullare.");
                }
                System.out.println();
                input.readLine(">> Premi INVIO per tornare al menu...");
            } else {
                switch (scelta) {
                    case 1:
                        handleSearch();
                        break;
                    case 2:
                        handleLoan();
                        break;
                    case 3:
                        handleReturn();
                        break;
                    case 4:
                        handleHistory();
                        break;
                    default:
                        System.out.println(">> Scelta non valida.");
                        break;
                }
                System.out.println();
                input.readLine(">> Premi INVIO per tornare al menu...");
            }
        }
    }
    private void handleSearch() {
        System.out.println("\n SELEZIONA IL CORSO ");
        corsiMap.forEach((k, v) -> System.out.println(k + ". " + v));
        int corsoId = input.readInt("Seleziona > ", 1, corsiMap.size());
        String nomeCorso = corsiMap.get(corsoId);
        List<Integer> anniDisponibili = courseDAO.getAnniByCorso(nomeCorso);
        if (anniDisponibili.isEmpty()) {
            System.out.println(">> Nessun anno trovato per questo corso");
            return;
        }
        System.out.println("Anni disponibili: " + anniDisponibili);
        int anno = input.readInt("Scegli anno: ",
                anniDisponibili.get(0),
                anniDisponibili.get(anniDisponibili.size() - 1));
        List<ElementoBibliotecario> risultati = itemDAO.findByCorsoEAnno(nomeCorso, anno);
        if (risultati.isEmpty()) {
            System.out.println(">> Nessun libro trovato per l'anno " + anno + ".");
            boolean vediTutti = input
                    .readYesNo(">> Vuoi visualizzare l'intero catalogo per il corso di " + nomeCorso + "? (s/n): ");
            if (vediTutti) {
                risultati = itemDAO.findByCorso(nomeCorso);
                if (risultati.isEmpty()) {
                    System.out.println(">> Nessun libro presente nel sistema per questo corso.");
                    return;
                }
            } else {
                return;
            }
        }
        System.out.printf("\n%-15s %-35s %-20s\n", "ISBN", "TITOLO", "DISPONIBILITÀ");
        System.out.println("--------------------------------------------------------------------------");
        for (ElementoBibliotecario e : risultati) {
            String disp = (e instanceof Libro)
                    ? "Copie: " + ((Libro) e).getCopieDisponibili()
                    : "EBOOK";
            System.out.printf("%-15s %-35s %-20s\n",
                    truncate(e.getIsbn(), 15),
                    truncate(e.getTitolo(), 33),
                    disp);
        }
    }
    private void handleLoan() {
        String isbn = input.readString("Inserisci ISBN del libro: ");
        String risultato = loanService.richiediPrestito(currentStudent, isbn);
        System.out.println(">> ESITO: " + risultato);
        if (risultato != null && !risultato.startsWith("Errore:")) {
            lastAction = "LOAN";
            lastIsbn = isbn;
        }
    }
    private void handleReturn() {
        String isbn = input.readString("Inserisci ISBN da restituire: ");
        loanService.restituisciLibro(isbn, currentStudent.getId());
        System.out.println(">> Restituzione registrata (se il prestito esisteva)");
        lastAction = "RETURN";
        lastIsbn = isbn;
    }
    private void handleHistory() {
        List<Prestito> storico = loanService.getStorico(currentStudent.getId());
        System.out.println("\n STORICO PRESTITI ");
        System.out.printf("%-15s %-30s %-12s %-12s %-10s\n", "ISBN", "TITOLO", "INIZIO", "SCADENZA", "STATO");
        System.out.println("-------------------------------------------------------------------------------------");
        for (Prestito p : storico) {
            String stato = (p.getDataRestituzione() != null) ? "CHIUSO" : (p.isExpired() ? "SCADUTO" : "ATTIVO");
            System.out.printf("%-15s %-30s %-12s %-12s %-10s\n",
                    p.getIsbn(),
                    (p.getTitoloLibro().length() > 28 ? p.getTitoloLibro().substring(0, 25) + "..."
                            : p.getTitoloLibro()),
                    p.getDataInizioFormatted(),
                    p.getDataScadenzaFormatted(),
                    stato);
        }
        System.out.println("-------------------------------------------------------------------------------------");
    }
    private void checkScadenzeCritiche() {
        List<Prestito> scaduti = loanService.getScaduti(currentStudent.getId());
        if (!scaduti.isEmpty()) {
            System.out.println("\n hai " + scaduti.size() + " prestiti scaduti:");
            for (Prestito p : scaduti) {
                System.out.println("- " + p.getTitoloLibro() + " (Scaduto il: " + p.getDataScadenzaFormatted()
                        + " | Rinnovi: " + p.getRenewalCount() + "/2)");
                if (!p.isRenewable()) {
                    System.out.println("  >> Limite rinnovi raggiunto");
                    loanService.terminaPrestitoScaduto(p.getId(), p.getIsbn());
                } else {
                    boolean rinnova = input.readYesNo("  >> Vuoi rinnovare (+14gg)? (s/n): ");
                    if (rinnova) {
                        loanService.estendiPrestito(p.getId());
                        System.out.println("  >> Rinnovato");
                    } else {
                        loanService.terminaPrestitoScaduto(p.getId(), p.getIsbn());
                        System.out.println("  >> Restituito");
                    }
                }
            }
            System.out.println("------------------------------------------");
            input.readLine(">> Premi INVIO per continuare...");
        }
    }
    private String truncate(String s, int len) {
        if (s == null)
            return "";
        return (s.length() > len) ? s.substring(0, len - 3) + "..." : s;
    }
}