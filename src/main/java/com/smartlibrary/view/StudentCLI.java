package com.smartlibrary.view;

import com.smartlibrary.model.Studente;
import com.smartlibrary.orm.CourseDAO;
import com.smartlibrary.model.Prestito;
import com.smartlibrary.logic.StudentLoanController;
import com.smartlibrary.utils.InputManager.*;
import com.smartlibrary.view.command.StudentCommandPattern.*;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;



public class StudentCLI {
    private ValidatedReader input;
    private Studente currentStudent;
    private StudentLoanController controller;
    private CourseDAO courseDAO;

    private Map<Integer, Command> comandiMenu;
    private Map<Integer, String> corsiMap;

    public StudentCLI(Scanner scanner, Studente student) {
        this.input = new ValidatedReader(new ConsoleReader(scanner));
        this.currentStudent = student;
        this.controller = new StudentLoanController();
        this.courseDAO = new CourseDAO();

        inizializzaCorsi();
        inizializzaComandi();
    }

    private void inizializzaCorsi() {
        corsiMap = courseDAO.getCorsiAsMap();

        if (corsiMap.isEmpty()) {
            System.out.println(">> ATTENZIONE: Nessun corso trovato");
        }
    }

    private void inizializzaComandi() {
        comandiMenu = new TreeMap<>();
        
        comandiMenu.put(1, new SearchCommand(input, controller, courseDAO, corsiMap));
        comandiMenu.put(2, new LoanCommand(input, controller, currentStudent));
        comandiMenu.put(3, new ReturnCommand(input, controller, currentStudent));
        comandiMenu.put(4, new HistoryCommand(controller, currentStudent));
    }

    public void start() {
        checkScadenzeCritiche();

        boolean running = true;
        while (running) {
            System.out.println("\n==========================================");
            System.out.println("   AREA STUDENTE: " + currentStudent.getNome());
            System.out.println("==========================================");
            
            for (Map.Entry<Integer, Command> entry : comandiMenu.entrySet()) {
                System.out.println(entry.getKey() + ". " + entry.getValue().getDescription());
            }
            System.out.println("0. Esci");
            
            int scelta = input.readInt("Seleziona > ", 0, comandiMenu.size());

            if (scelta == 0) {
                running = false;
            } else {
                Command cmd = comandiMenu.get(scelta);
                if (cmd != null) {
                    cmd.execute();
                    System.out.println(); 
                    input.readLine(">> Premi INVIO per tornare al menu...");
                }
            }
        }
    }

    private void checkScadenzeCritiche() {
        List<Prestito> scaduti = controller.getScaduti(currentStudent.getId());
        
        if (!scaduti.isEmpty()) {
            System.out.println("\n hai " + scaduti.size() + " prestiti scaduti:");
            
            for (Prestito p : scaduti) {
                System.out.println("- " + p.getTitoloLibro() + " (Rinnovi: " + p.getRenewalCount() + "/2)");
                
                if (!p.isRenewable()) {
                    System.out.println("  >> Limite rinnovi raggiunto");
                    controller.terminaPrestitoScaduto(p.getId(), p.getIsbn());
                } else {
                    boolean rinnova = input.readYesNo("  >> Vuoi rinnovare (+14gg)? (s/n): ");
                    
                    if (rinnova) {
                        controller.estendiPrestito(p.getId());
                        System.out.println("  >> Rinnovato");
                    } else {
                        controller.terminaPrestitoScaduto(p.getId(), p.getIsbn());
                        System.out.println("  >> Restituito");
                    }
                }
            }
            System.out.println("------------------------------------------");
            input.readLine("Premi INVIO per continuare...");
        }
    }
}