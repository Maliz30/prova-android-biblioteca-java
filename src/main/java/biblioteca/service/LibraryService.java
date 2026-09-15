package biblioteca.service;

import biblioteca.data.Library;
import biblioteca.model.Book;
import biblioteca.model.Loan;
import biblioteca.model.Member;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Onde moram as regras da biblioteca.
 *
 * <p>Nada aqui dentro imprime na tela nem lê do teclado.
 * Esta classe recebe perguntas e devolve dados. Quem conversa com o usuário é a
 * camada de {@code cli}.
 */
public class LibraryService {

    private final Library library;

    public LibraryService(Library library) {
        this.library = library;
    }

    /**
     * O acervo inteiro, na ordem em que está cadastrado.
     */
    public List<Book> catalog() {
        return library.books();
    }

    /**
     * Quantos exemplares de um livro estão livres para empréstimo agora.
     *
     * @param book livro cuja disponibilidade será calculada.
     */
    public int availableCopies(Book book) {
        long loanCount = library.loans().stream()
            .filter(loan -> loan.bookId() == book.id() && !loan.wasReturned())
            .count();

        return book.copies() - (int) loanCount;
    }

    /**
     * Livros cujo título, autor ou gênero contém o termo buscado.
     *
     * @param searchText termo buscado.
     */
    public List<Book> searchBook(String searchText) {
        String normalizedSearch = normalize(searchText);

        return library.books().stream()
            .filter(book ->
                normalize(book.title()).contains(normalizedSearch) ||
                normalize(book.author()).contains(normalizedSearch) ||
                normalize(book.genre()).contains(normalizedSearch))
            .toList();
    }

    /**
     * Remove acentos e caixa alta de um texto, para comparação na busca.
     *
     * @param text texto a ser normalizado.
     */
    private String normalize(String text) {
        String withoutAccents = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase();
    }

    /**
     * Empresta um livro a um membro, aplicando as regras de negócio da biblioteca:
     * o livro precisa ter exemplar disponível, o membro não pode ter 3 empréstimos
     * em aberto, e nem estar com algum empréstimo atrasado.
     *
     * @param bookId id do livro a ser emprestado.
     * @param memberId id do membro que está pegando o livro emprestado.
     */
    public LoanResult borrowBook(int bookId, int memberId) {
        Optional<Member> member = library.findMember(memberId);
        if (member.isEmpty()) {
            return new LoanResult(false, "O membro informado não foi encontrado.");
        }

        Optional<Book> book = library.findBook(bookId);
        if (book.isEmpty()) {
            return new LoanResult(false, "O livro informado não foi encontrado.");
        }
        if (availableCopies(book.get()) <= 0) {
            return new LoanResult(false, "Não há unidades disponíveis para empréstimo.");
        }

        long loanCount = library.loans().stream()
            .filter(loan -> loan.memberId() == memberId && !loan.wasReturned())
            .count();

        if (loanCount >= 3) {
            return new LoanResult(false, "Não foi possível realizar o empréstimo, o membro informado já possui 3 empréstimos em seu nome.");
        }

        for (Loan loan : library.loans()) {
            if (loan.memberId() == memberId){
                String bookName = library.findBook(loan.bookId()).get().title();

                if (isLoanLate(loan)) {
                    return new LoanResult(false,"Não foi possível realizar o empréstimo, o membro está com o empréstimo do livro " + bookName + " atrasado.");
                }
            }
        }

        library.loans().add(new Loan(bookId, memberId, LocalDate.now(), false));
        return new LoanResult(true, "Livro emprestado com sucesso.");
    }

    /**
     * Devolve um livro emprestado por um membro.
     *
     * @param bookId id do livro a ser devolvido.
     * @param memberId id do membro que está devolvendo o livro.
     */
    public LoanResult returnBook(int bookId, int memberId) {
        Optional<Member> member = library.findMember(memberId);
        if (member.isEmpty()) {
            return new LoanResult(false, "O membro informado não foi encontrado.");
        }

        Optional<Book> book = library.findBook(bookId);
        if (book.isEmpty()) {
            return new LoanResult(false, "O livro informado não foi encontrado.");
        }

        Optional<Loan> loan = library.loans().stream()
            .filter(l -> l.bookId() == bookId && l.memberId() == memberId)
            .findFirst();

        if (loan.isEmpty()) {
            return new LoanResult(false, "Não foi encontrado um empréstimo com os parâmetros informados.");
        }
        if (loan.get().wasReturned()){
            return new LoanResult(false, "A devolução do empréstimo informado já foi realizada.");
        }

        Loan updatedLoan = new Loan(bookId, memberId, loan.get().borrowedAt(), true);

        int loanPosition = library.loans().indexOf(loan.get());
        library.loans().set(loanPosition, updatedLoan);
        return new LoanResult(true, "Devolução realizada com sucesso.");
    }

    /**
     * Busca um membro com o id informado, se existir.
     *
     * @param memberId id do membro buscado.
     */
    public Optional<Member> findMember(int memberId) {
        return library.findMember(memberId);
    }

    /**
     * Busca um livro com o id informado, se existir.
     *
     * @param bookId id do livro buscado.
     */
    public Optional<Book> findBook(int bookId) {
        return library.findBook(bookId);
    }

    /**
     * Calcula a data limite para devolução de um empréstimo, a partir da data em que foi feito.
     *
     * @param borrowedAt data em que o empréstimo foi realizado.
     */
    public LocalDate calculateReturnDate(LocalDate borrowedAt) {
        return borrowedAt.plusDays(15);
    }

    /**
     * Empréstimos ativos, ainda não devolvidos, de um membro.
     *
     * @param memberId id do membro.
     */
    public List<Loan> memberLoans(int memberId) {
        return library.loans().stream()
            .filter(loan -> loan.memberId() == memberId && !loan.wasReturned())
            .toList();
    }

    /**
     * Verifica se um empréstimo está com a devolução atrasada.
     *
     * @param loan empréstimo a ser verificado.
     */
    public boolean isLoanLate(Loan loan) {
        return !loan.wasReturned() && calculateReturnDate(loan.borrowedAt()).isBefore(LocalDate.now());
    }

    /**
     * Todos os empréstimos atualmente atrasados, de qualquer membro.
     */
    public List<Loan> generateReport() {
        return library.loans().stream()
            .filter(loan -> isLoanLate(loan))
            .toList();
    }
}
