package cncs.academy.ess.repository.memory;

import cncs.academy.ess.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserRepositoryTest {

    private InMemoryUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
    }

    // Testa guardar um utilizador sem ID definido.
    // O repositório deve gerar automaticamente um ID positivo.
    @Test
    void testSaveUserWithoutId() {
        User user = new User("alice", "pass123");
        int id = repository.save(user);
        assertTrue(id > 0);
    }

    // Testa guardar um utilizador com um ID explícito (10).
    // Confirma que o repositório respeita e preserva o ID fornecido.
    @Test
    void testSaveUserWithId() {
        User user = new User(10, "bob", "pass456");
        int id = repository.save(user);
        assertEquals(10, id);
    }

    // Testa a busca de um utilizador pelo seu ID.
    // Guarda um utilizador com ID 1 e confirma que o findById retorna
    // o utilizador correto com o username esperado.
    @Test
    void testFindById() {
        User user = new User(1, "alice", "pass123");
        repository.save(user);
        User found = repository.findById(1);
        assertNotNull(found);
        assertEquals("alice", found.getUsername());
    }

    // Testa o comportamento ao procurar um ID inexistente (999).
    // Confirma que o repositório retorna null em vez de lançar uma exceção.
    @Test
    void testFindByIdNotFound() {
        User found = repository.findById(999);
        assertNull(found);
    }

    // Valida que o método findAll() retorna todos os utilizadores guardados.
    // Insere 2 utilizadores e verifica que a lista devolvida tem tamanho 2.
    @Test
    void testFindAll() {
        repository.save(new User(1, "alice", "pass1"));
        repository.save(new User(2, "bob", "pass2"));
        List<User> all = repository.findAll();
        assertEquals(2, all.size());
    }

    // Valida a remoção de um utilizador pelo ID.
    // Guarda um utilizador, apaga-o, e confirma que findById retorna null,
    // provando que a eliminação foi bem-sucedida.
    @Test
    void testDeleteById() {
        User user = new User(1, "alice", "pass123");
        repository.save(user);
        repository.deleteById(1);
        assertNull(repository.findById(1));
    }

    // Verifica que é possível encontrar um utilizador pelo seu username exato.
    // Insere dois utilizadores e procura por "bob", confirmando que o resultado
    // corresponde ao utilizador correto.
    @Test
    void testFindByUsername() {
        repository.save(new User(1, "alice", "pass123"));
        repository.save(new User(2, "bob", "pass456"));
        User found = repository.findByUsername("bob");
        assertNotNull(found);
        assertEquals("bob", found.getUsername());
    }

    // Verifica que ao procurar um username que não existe no repositório,
    // o resultado é null — não lança erro nem retorna dados incorretos.
    @Test
    void testFindByUsernameNotFound() {
        User found = repository.findByUsername("nonexistent");
        assertNull(found);
    }

    // Confirma que os IDs gerados automaticamente são sequenciais (1, 2, 3)
    // quando vários utilizadores são guardados sem ID definido.
    @Test
    void testSaveMultipleUsersWithoutId_SequentialIds() {
        int id1 = repository.save(new User("alice", "pass1"));
        int id2 = repository.save(new User("bob", "pass2"));
        int id3 = repository.save(new User("charlie", "pass3"));
        assertEquals(1, id1);
        assertEquals(2, id2);
        assertEquals(3, id3);
    }

    // Confirma que guardar um utilizador com um ID já existente substitui o anterior.
    // O repositório deve manter apenas um utilizador com esse ID.
    @Test
    void testSaveOverwritesExistingUser() {
        repository.save(new User(10, "alice", "pass1"));
        repository.save(new User(10, "bob", "pass2"));
        User found = repository.findById(10);
        assertNotNull(found);
        assertEquals("bob", found.getUsername());
        assertEquals(1, repository.findAll().size());
    }

    // Verifica que findAll() retorna uma lista vazia (não null)
    // quando o repositório não contém utilizadores.
    @Test
    void testFindAllEmptyRepository() {
        List<User> all = repository.findAll();
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    // Confirma que a lista retornada por findAll() é uma cópia defensiva.
    // Limpar a lista devolvida não afeta os dados internos do repositório.
    @Test
    void testFindAllReturnsDefensiveCopy() {
        repository.save(new User(1, "alice", "pass1"));
        List<User> all = repository.findAll();
        all.clear();
        assertEquals(1, repository.findAll().size());
    }

    // Testa que apagar um ID inexistente não lança exceção.
    @Test
    void testDeleteByIdNonExistent() {
        assertDoesNotThrow(() -> repository.deleteById(999));
    }

    // Guarda um utilizador sem ID e depois encontra-o pelo ID gerado automaticamente.
    // Confirma que tanto o username como a password estão corretos.
    @Test
    void testSaveWithoutIdThenFindById() {
        User user = new User("alice", "pass123");
        int id = repository.save(user);
        User found = repository.findById(id);
        assertNotNull(found);
        assertEquals("alice", found.getUsername());
        assertEquals("pass123", found.getPassword());
    }

    // Confirma que buscas parciais por username não retornam resultado.
    // Procurar "ali" não deve encontrar o utilizador "alice".
    @Test
    void testFindByUsernameDoesNotMatchPartial() {
        repository.save(new User(1, "alice", "pass123"));
        User found = repository.findByUsername("ali");
        assertNull(found);
    }
}
