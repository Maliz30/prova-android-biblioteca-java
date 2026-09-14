package biblioteca.data;

import biblioteca.model.Book;
import biblioteca.model.Loan;
import biblioteca.model.Member;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * O acervo da biblioteca, em memória.
 *
 * <p>Faz o papel de banco de dados: guarda os dados e não conhece nenhuma
 * regra. Os empréstimos já vêm com alguns registros em aberto.
 */
public class Library {

    private final List<Book> books = List.of(
            new Book(1, "Duna", "Frank Herbert", "Ficção Científica", 3),
            new Book(2, "Ensaio sobre a Cegueira", "José Saramago", "Romance", 2),
            new Book(3, "Memórias Póstumas de Brás Cubas", "Machado de Assis", "Romance", 4),
            new Book(4, "Cem Anos de Solidão", "Gabriel García Márquez", "Realismo Mágico", 2),
            new Book(5, "O Hobbit", "J. R. R. Tolkien", "Fantasia", 3),
            new Book(6, "Neuromancer", "William Gibson", "Ficção Científica", 1),
            new Book(7, "A Revolução dos Bichos", "George Orwell", "Fábula", 2),
            new Book(8, "O Cortiço", "Aluísio Azevedo", "Naturalismo", 1),
            new Book(9, "Vidas Secas", "Graciliano Ramos", "Romance", 2),
            new Book(10, "Fahrenheit 451", "Ray Bradbury", "Ficção Científica", 1),
            new Book(11, "O Nome do Vento", "Patrick Rothfuss", "Fantasia", 2),
            new Book(12, "Torto Arado", "Itamar Vieira Junior", "Romance", 3));

    private final List<Member> members = List.of(
            new Member(1, "Ana Prado"),
            new Member(2, "Bruno Sales"),
            new Member(3, "Carla Nunes"),
            new Member(4, "Diego Farias"));

    private final List<Loan> loans = new ArrayList<>(List.of(
            new Loan(1, 1, LocalDate.now().minusDays(3), false),
            new Loan(5, 1, LocalDate.now().minusDays(20), false),
            new Loan(6, 2, LocalDate.now().minusDays(5), false),
            new Loan(1, 3, LocalDate.now().minusDays(10), false)));

    public List<Book> books() {
        return books;
    }

    public List<Member> members() {
        return members;
    }

    public List<Loan> loans() {
        return loans;
    }

    public Optional<Book> findBook(int id) {
        return books.stream().filter(book -> book.id() == id).findFirst();
    }

    public Optional<Member> findMember(int id) {
        return members.stream().filter(member -> member.id() == id).findFirst();
    }
}
