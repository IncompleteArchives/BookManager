package org.example.bookapp.console;

import org.example.bookapp.exception.AuthorException;
import org.example.bookapp.exception.DatabaseOperationException;
import org.example.bookapp.exception.InvalidAuthorException;
import org.example.bookapp.exception.InvalidBookException;
import org.example.bookapp.model.Author;
import org.example.bookapp.model.Book;
import org.example.bookapp.service.AuthorService;
import org.example.bookapp.service.BookService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
public class ConsoleApplication {

    private final BookService service;
    private final AuthorService authorService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String COMMANDS =
            """
                    add                 Adds new book to the table.
                    list                Prints all books from the table.
                    find                Finds book by its name and prints it.
                    add-author          Adds new author to the table.
                    add-author-books    Adds new author along with books.
                    find-author         Finds author by id and prints it.
                    rm-author           Removes author from the table.
                    transfer-book       Transfers book to another author.
                    exit                Terminates the application.""";


    @Autowired
    public ConsoleApplication(BookService service, AuthorService authorService) {
        this.service = service;
        this.authorService = authorService;
    }

    public void start() {

        System.out.println(">>> ConsoleApplication successfully run from Spring Context! <<<");
        Scanner scanner = new Scanner(System.in);
        printCommands();

        while (true) {
            System.out.print("\nEnter command: ");
            String command = scanner.nextLine().trim().toLowerCase();

            try {
                switch (command) {
                    case "add" -> addBook(scanner);
                    case "list" -> listBooks();
                    case "find" -> findBook(scanner);
                    case "add-author" -> addAuthor(scanner);
                    case "add-author-books" -> addAuthorWithBooks(scanner);
                    case "find-author" -> findAuthor(scanner);
                    case "rm-author" -> deleteAuthor(scanner);
                    case "transfer-book" -> transferBook(scanner);
                    case "exit" -> {
                        return;
                    }
                    default -> {
                        System.out.println("Invalid command");
                        printCommands();
                    }
                }
            } catch (DatabaseOperationException e) {
                System.out.println(e.getMessage());
            }

        }

    }

    private void transferBook(Scanner scanner) {

        try {
            System.out.print("Enter book ID: ");
            Integer bookId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter author ID: ");
            Integer authorId = Integer.parseInt(scanner.nextLine().trim());

            service.transferBook(bookId, authorId);

            System.out.println("Book transferred successfully");

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID");
        } catch (InvalidBookException | InvalidAuthorException e) {
            System.out.println("Book was not transferred: " + e.getMessage());
        }

    }


    private void addAuthorWithBooks(Scanner scanner) {

        try {
            Author author = readAuthor(scanner);
            List<Book> books = readBooks(scanner);

            if (books.isEmpty()) {
                System.out.println("Author was not added: no books provided");
                return;
            }

            Integer authorId = authorService.addAuthorWithBooks(author, books);
            System.out.println("Author (ID=" + authorId + ") and books added successfully");

        } catch (RuntimeException e) {
            System.out.println("Author and books was not added: " + e.getMessage());
        }

    }

    private List<Book> readBooks(Scanner scanner) {
        System.out.print("Books amount: ");
        int amount;

        try {
            amount = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount");
            return List.of();
        }

        List<Book> books = new ArrayList<>();

        for (int i = 1; i <= amount; i++) {
            System.out.println("\nBook #" + i);

            System.out.print("Name: ");
            String name = scanner.nextLine();

            System.out.print("Publication year: ");
            Integer year;

            try {
                year = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                throw new InvalidBookException("Invalid year format");
            }

            books.add(new Book(name, year));
        }

        return books;

    }

    private Author readAuthor(Scanner scanner) {

        System.out.print("First name: ");
        String firstName = scanner.nextLine();

        System.out.print("Middle name: ");
        String middleName = scanner.nextLine();

        System.out.print("Last name: ");
        String lastName = scanner.nextLine();

        System.out.print("Gender (M/F): ");
        String gender = scanner.nextLine().trim().toUpperCase();

        if (gender.isBlank()) gender = null;

        System.out.print("Date of birth (dd.MM.yyyy): ");
        LocalDate birthDate;

        try {
            birthDate = LocalDate.parse(scanner.nextLine(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidAuthorException("Invalid date format");
        }

        return new Author(firstName, middleName, lastName, gender, birthDate);

    }

    private void deleteAuthor(Scanner scanner) {

        System.out.print("Enter author ID: ");

        Integer id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid author ID");
            return;
        }

        try {
            authorService.deleteAuthor(id);
            System.out.println("Author (ID=" + id + ") has been removed");
        } catch (AuthorException e) {
            System.out.println("Author (ID=" + id + ") was not removed: " + e.getMessage());
        }
    }

    private void findAuthor(Scanner scanner) {

        System.out.print("Enter author ID: ");

        Integer id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid author ID");
            return;
        }

        Optional<Author> authorWithBooks = authorService.findById(id);

        authorWithBooks.ifPresentOrElse(
                author -> {
                    System.out.println("Author found:\n" + author);
                    System.out.println("\nBooks by author:");
                    printBooks(author.getBooks());
                },
                () -> System.out.println("Author not found")
        );

    }

    private void addAuthor(Scanner scanner) {

        Author author = null;
        try {
            author = readAuthor(scanner);
        } catch (InvalidAuthorException e) {
            System.out.println("Author was not added: " + e.getMessage());
        }

        if (author == null) return;

        try {
            Integer authorId = authorService.addAuthor(author);
            System.out.println("Author (ID=" + authorId +") added successfully");
        } catch (InvalidAuthorException e) {
            System.out.println("Author was not added: " + e.getMessage());
        }

    }

    private void findBook(Scanner scanner) {

        System.out.print("Enter book name: ");
        String name = scanner.nextLine().trim();

        try {
            List<Book> books = service.findByNameContains(name);
            System.out.println("Found books: ");
            printBooks(books);
        } catch (InvalidBookException e) {
            System.out.println(e.getMessage());
        }

    }

    private void listBooks() {

        List<Book> books = service.getAllBooks();
        printBooks(books);
    }

    private void printBooks(List<Book> books) {

        if (books.isEmpty()) {
            System.out.println("No books found");
            return;
        }

        books.forEach(System.out::println);
    }

    private void addBook(Scanner scanner) {

        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("Publication year: ");
        Integer year;

        try {
            year = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Book was not added: invalid year format");
            return;
        }

        System.out.print("Author ID: ");
        Integer authorId;

        try {
            authorId = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Book was not added: invalid Author ID format");
            return;
        }

        try {
            service.addBook(name, year, authorId);
            System.out.println("Book added successfully");
        } catch (InvalidBookException | InvalidAuthorException e) {
            System.out.println("Book was not added: " + e.getMessage());
        }

    }

    private void printCommands() {
        System.out.println("\nAvailable commands are:\n" + COMMANDS);
    }
}
