package org.example.bookapp.console;

import org.example.bookapp.exception.InvalidBookException;
import org.example.bookapp.model.Book;
import org.example.bookapp.service.BookService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

@Component
public class ConsoleApplication {

    private final BookService service;
    public static final String COMMANDS =
            """
                    add          Adds new book to the table.
                    list         Prints all books from the table.
                    find         Finds book by its name and prints it.
                    exit         Terminates the application.""";


    @Autowired
    public ConsoleApplication(BookService service) {
        this.service = service;
    }

    public void start() {

        System.out.println(">>> ConsoleApplication successfully run from Spring Context! <<<");
        Scanner scanner = new Scanner(System.in);
        printCommands();

        while (true) {
            System.out.print("\nEnter command: ");
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "add" -> addBook(scanner);
                case "list" -> listBooks();
                case "find" -> findBook(scanner);
                case "exit" -> {
                    return;
                }
                default -> {
                    System.out.println("Invalid command.");
                    printCommands();
                }

            }
        }

    }

    private void findBook(Scanner scanner) {

        System.out.print("Enter book name: ");
        String name = scanner.nextLine().trim();

        service.findBook(name)
                .ifPresentOrElse(
                        book -> System.out.println("Book found:\n" + book),
                        () -> System.out.println("Book not found")
                );
    }

    private void listBooks() {

        List<Book> books = service.getAllBooks();

        if (books.isEmpty()) {
            System.out.println("No books found");
            return;
        }

        books.stream().forEach(System.out::println);
    }

    private void addBook(Scanner scanner) {

        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("Author: ");
        String author = scanner.nextLine();

        System.out.print("Publication year: ");
        Integer year;

        try {
            year = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Book was not added: invalid year format");
            return;
        }

        try {
            service.addBook(name, author, year);
            System.out.println("Book added successfully.");
        } catch (InvalidBookException e) {
            System.out.println("Book was not added: " + e.getMessage());
        }

    }

    private void printCommands() {
        System.out.println("\nAvailable commands are:\n" + COMMANDS);
    }
}
