-- =========================================================
-- 1. PULIZIA DATABASE (Ordine inverso per vincoli FK)
-- =========================================================
DROP TABLE IF EXISTS loans;
DROP TABLE IF EXISTS course_books;
DROP TABLE IF EXISTS library_items;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS loans;

-- =========================================================    
-- 2. CREAZIONE TABELLE
-- =========================================================

-- TABELLA UTENTI
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    matricola CHAR(7) UNIQUE NOT NULL,
    password VARCHAR(64) NOT NULL, -- SHA-256 Hash
    nome VARCHAR(100),
    email VARCHAR(100),
    ruolo VARCHAR(20) NOT NULL -- 'STUDENTE' o 'ADMIN'
);

-- TABELLA ITEM (Il libro fisico o digitale, unico nel magazzino)
-- Qui NON mettiamo il corso o l'anno, perché un libro può valere per più corsi
CREATE TABLE library_items (
    isbn VARCHAR(20) PRIMARY KEY, -- Usiamo ISBN numerici come PK
    tipo VARCHAR(20) NOT NULL,    -- 'CARTACEO' o 'EBOOK'
    titolo VARCHAR(255) NOT NULL,
    autore VARCHAR(100) NOT NULL,
    
    -- Campi per LIBRI CARTACEI
    copie_totali INT DEFAULT 0,
    copie_disponibili INT DEFAULT 0,
    
    -- Campi per EBOOK
    download_url VARCHAR(255)
);

-- TABELLA DI ASSOCIAZIONE (Molti-a-Molti)
-- Collega un libro a uno o più Corsi di Laurea / Anni
CREATE TABLE course_books (
    id SERIAL PRIMARY KEY,
    book_isbn VARCHAR(20) NOT NULL,
    corso_studi VARCHAR(100) NOT NULL, -- Es. 'Ingegneria Informatica'
    anno_accademico INT NOT NULL,      -- Es. 1, 2, 3...
    
    FOREIGN KEY (book_isbn) REFERENCES library_items(isbn) ON DELETE CASCADE,
    UNIQUE (book_isbn, corso_studi, anno_accademico) -- Evita duplicati dello stesso libro nello stesso corso/anno
);

CREATE TABLE loans (
    id SERIAL PRIMARY KEY,
    item_isbn VARCHAR(20) NOT NULL,
    user_id INT NOT NULL,
    start_date DATE DEFAULT CURRENT_DATE,
    due_date DATE NOT NULL,
    returned_at DATE,
    renewal_count INT DEFAULT 0, -- NUOVO CAMPO: Conta i rinnovi (Max 2)
    
    FOREIGN KEY (item_isbn) REFERENCES library_items(isbn),
    FOREIGN KEY (user_id) REFERENCES users(id)
);


-- =========================================================
-- 3. INSERIMENTO DATI (SEEDING)
-- =========================================================

-- UTENTE ADMIN (Password: admin123)
INSERT INTO users (matricola, password, nome, email, ruolo) 
VALUES ('0000000', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Super Admin', 'admin@lib.it', 'ADMIN');

-- UTENTE STUDENTE DI PROVA (Password: password)
INSERT INTO users (matricola, password, nome, email, ruolo) 
VALUES ('1234567', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'Mario Rossi', 'mario@studenti.it', 'STUDENTE');


-- --- CASO 1: LIBRO CONDIVISO (Analisi Matematica) ---
-- Inseriamo il libro una volta sola nel magazzino
INSERT INTO library_items (isbn, tipo, titolo, autore, copie_totali, copie_disponibili)
VALUES ('9788808182038', 'CARTACEO', 'Analisi Matematica 1', 'Bramanti', 10, 10);

-- Lo associamo a DUE corsi diversi (Molti-a-Molti)
-- 1. Ingegneria Informatica - Anno 1
INSERT INTO course_books (book_isbn, corso_studi, anno_accademico) 
VALUES ('9788808182038', 'Ingegneria Informatica', 1);

-- 2. Ingegneria Gestionale - Anno 1
INSERT INTO course_books (book_isbn, corso_studi, anno_accademico) 
VALUES ('9788808182038', 'Ingegneria Gestionale', 1);


-- --- CASO 2: LIBRO ESAURITO (Sistemi Operativi) ---
-- Copie disponibili = 0 per testare la Lista d'Attesa (Observer)
INSERT INTO library_items (isbn, tipo, titolo, autore, copie_totali, copie_disponibili)
VALUES ('9780321124352', 'CARTACEO', 'Sistemi Operativi', 'Silberschatz', 5, 0);

-- Associato solo a Informatica - Anno 2
INSERT INTO course_books (book_isbn, corso_studi, anno_accademico) 
VALUES ('9780321124352', 'Ingegneria Informatica', 2);


-- --- CASO 3: EBOOK CON URL FORMATTATO (Software Engineering) ---
-- URL generato con la logica: https://lib.it/dl/ + autore + _ + codicecorso
INSERT INTO library_items (isbn, tipo, titolo, autore, download_url)
VALUES ('9780133943030', 'EBOOK', 'Software Engineering', 'Sommerville', 'https://lib.it/dl/sommerville_inginf.pdf');

-- Associato a Informatica - Anno 3
INSERT INTO course_books (book_isbn, corso_studi, anno_accademico) 
VALUES ('9780133943030', 'Ingegneria Informatica', 3);


-- --- CASO 4: MEDICINA (Anatomia) ---
-- Ciclo unico
INSERT INTO library_items (isbn, tipo, titolo, autore, copie_totali, copie_disponibili)
VALUES ('9788879597349', 'CARTACEO', 'Anatomia Umana', 'Martini', 3, 3);

-- Associato a Medicina e Chirurgia - Anno 1
INSERT INTO course_books (book_isbn, corso_studi, anno_accademico) 
VALUES ('9788879597349', 'Medicina e Chirurgia', 1);


-- --- CASO 5: GIURISPRUDENZA (Diritto Privato) ---
INSERT INTO library_items (isbn, tipo, titolo, autore, copie_totali, copie_disponibili)
VALUES ('9788814226969', 'CARTACEO', 'Manuale di Diritto Privato', 'Torrente', 5, 5);

INSERT INTO course_books (book_isbn, corso_studi, anno_accademico) 
VALUES ('9788814226969', 'Giurisprudenza', 1);

-- Esempio di dato per testare la scadenza (un prestito scaduto ieri)
-- Assumiamo che l'utente ID 2 (Mario) abbia un prestito scaduto
INSERT INTO loans (item_isbn, user_id, start_date, due_date, returned_at)
VALUES ('9788808182038', 2, CURRENT_DATE - 35, CURRENT_DATE - 1, NULL); 