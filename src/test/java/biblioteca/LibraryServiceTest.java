package biblioteca;

import biblioteca.data.Library;
import biblioteca.model.Book;
import biblioteca.model.Loan;
import biblioteca.service.LibraryService;
import biblioteca.service.LoanResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LibraryServiceTest {

    private LibraryService service;

    @BeforeEach
    void setUp() {
        service = new LibraryService(new Library());
    }

    @Test
    @DisplayName("membro sem nenhum empréstimo consegue pegar um livro emprestado")
    void permiteEmprestimo() {
        LoanResult result = service.borrowBook(2, 4);
        assertTrue(result.success());
    }

    @Test
    @DisplayName("não permite empréstimo quando não há exemplar disponível")
    void semExemplarDisponivel() {
        LoanResult result = service.borrowBook(6, 3);
        assertFalse(result.success());
    }

    @Test
    @DisplayName("membro que já possui 3 empréstimos ativos não consegue pegar mais um livro emprestado")
    void limiteDeEmprestimosAtingido() {
        LoanResult resultFirstLoan = service.borrowBook(2, 4);
        assertTrue(resultFirstLoan.success());

        LoanResult resultSecondLoan = service.borrowBook(9, 4);
        assertTrue(resultSecondLoan.success());

        LoanResult resultThirdLoan = service.borrowBook(11, 4);
        assertTrue(resultThirdLoan.success());

        LoanResult resultFourthLoan = service.borrowBook(7, 4);
        assertFalse(resultFourthLoan.success());
    }

    @Test
    @DisplayName("membro com empréstimo em atraso não consegue pegar livro emprestado")
    void comEmprestimoAtrasado() {
        LoanResult result = service.borrowBook(2, 1);
        assertFalse(result.success());
    }

    @Test
    @DisplayName("não permite empréstimo de um livro inexistente")
    void livroInexistente() {
        LoanResult result = service.borrowBook(99, 4);
        assertFalse(result.success());
    }

    @Test
    @DisplayName("membro inexistente não consegue realizar empréstimo")
    void membroInexistente() {
        LoanResult result = service.borrowBook(1, 99);
        assertFalse(result.success());
    }

    @Test
    @DisplayName("devolver um empréstimo que o membro 1 realmente tem em aberto")
    void permiteDevolucao() {
        LoanResult result = service.returnBook(5, 1);
        assertTrue(result.success());
    }

    @Test
    @DisplayName("devolver o mesmo empréstimo de novo depois de já ter devolvido")
    void devolucaoRepetida() {
        LoanResult resultFirstReturn = service.returnBook(1, 1);
        assertTrue(resultFirstReturn.success());

        LoanResult resultSecondReturn = service.returnBook(1, 1);
        assertFalse(resultSecondReturn.success());
    }

    @Test
    @DisplayName("tentar devolver uma combinação livro/membro que nunca foi emprestada")
    void devolucaoInexistente() {
        LoanResult result = service.returnBook(12, 1);
        assertFalse(result.success());
    }

    @Test
    @DisplayName("empréstimo de 20 dias atrás, mas já devolvido, não conta como atrasado")
    void emprestimoDevolvidoNaoContaComoAtrasado() {
        Loan loan = new Loan(5, 1, LocalDate.now().minusDays(20), true);
        boolean loanStatus = service.isLoanLate(loan);

        assertFalse(loanStatus);
    }

    @Test
    @DisplayName("devolver um livro aumenta a quantidade de exemplares disponíveis")
    void devolucaoAumentaExemplaresDisponiveis() {
        Book book = service.findBook(6).orElseThrow();
        int availableBeforeReturn = service.availableCopies(book);

        LoanResult returnResult = service.returnBook(6, 2);
        assertTrue(returnResult.success());

        int availableAfterReturn = service.availableCopies(book);
        assertEquals(availableBeforeReturn + 1, availableAfterReturn);
    }

    @Test
    @DisplayName("não retorna empréstimos já devolvidos na lista do membro")
    void naoRetornaEmprestimosJaDevolvidos() {
        List<Loan> loansBeforeReturn = service.memberLoans(1);
        assertEquals(2, loansBeforeReturn.size());

        LoanResult returnResult = service.returnBook(1, 1);
        assertTrue(returnResult.success());

        List<Loan> loansAfterReturn = service.memberLoans(1);
        assertEquals(1, loansAfterReturn.size());
        assertTrue(loansAfterReturn.stream().noneMatch(loan -> loan.bookId() == 1));
    }

    @Test
    @DisplayName("searchBook ignora acentuação e caixa alta na busca")
    void buscaIgnoraAcentuacaoECaixaAlta() {
        List<Book> foundBooks = service.searchBook("José SARAMAGO");

        assertEquals(1, foundBooks.size());
        assertEquals("Ensaio sobre a Cegueira", foundBooks.get(0).title());
    }

    @Test
    @DisplayName("empréstimo no dia exato do vencimento ainda não conta como atrasado")
    void emprestimoNoLimiteDoAtraso() {
        Loan loan = new Loan(11, 4, LocalDate.now().minusDays(15), false);

        boolean result = service.isLoanLate(loan);
        assertFalse(result);
    }

    @Test
    @DisplayName("empréstimo um dia após o vencimento já conta como atrasado")
    void emprestimoUmDiaAposOVencimento() {
        Loan loan = new Loan(11, 4, LocalDate.now().minusDays(16), false);

        boolean result = service.isLoanLate(loan);
        assertTrue(result);
    }

    @Test
    @DisplayName("busca com termo que não bate com nenhum livro devolve lista vazia")
    void buscaSemResultadoDevolveListaVazia() {
        List<Book> foundBooks = service.searchBook("xyzabc");

        assertTrue(foundBooks.isEmpty());
    }
}
