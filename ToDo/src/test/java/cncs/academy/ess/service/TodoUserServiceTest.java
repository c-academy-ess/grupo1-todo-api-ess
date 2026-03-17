package cncs.academy.ess.service;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoUserServiceTest {

    @Mock
    private UserRepository userRepository;

    private TodoUserService service;

    private static final byte[] HMAC_SECRET = "test-secret-key-for-hmac-256-bit".getBytes();

    @BeforeEach
    void setUp() {
        service = new TodoUserService(userRepository, HMAC_SECRET);
    }

    @Test
    void login_shouldReturnValidJWTTokenWhenCredentialsMatch() throws Exception {
        // Arrange: criar um utilizador com password corretamente hashed
        // usando addUser, que internamente chama hashPassword
        when(userRepository.save(any(User.class))).thenReturn(1);
        User createdUser = service.addUser("alice", "pass123");

        // Configurar o mock para devolver o utilizador quando procurado por username
        when(userRepository.findByUsername("alice")).thenReturn(createdUser);

        // Act: chamar login com as credenciais corretas
        String result = service.login("alice", "pass123");

        // Assert 1: verificar que o resultado começa com "Bearer "
        assertNotNull(result);
        assertTrue(result.startsWith("Bearer "), "O token deve começar com 'Bearer '");

        // Assert 2: extrair o JWT e verificar a estrutura (header.payload.signature)
        String jwt = result.substring("Bearer ".length());
        String[] parts = jwt.split("\\.");
        assertEquals(3, parts.length, "O JWT deve ter 3 partes: header.payload.signature");

        // Verificar o header do JWT
        String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode header = mapper.readTree(headerJson);
        assertEquals("HS256", header.get("alg").asText());
        assertEquals("JWT", header.get("typ").asText());

        // Verificar os claims do payload
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
        JsonNode payload = mapper.readTree(payloadJson);
        assertEquals("1", payload.get("sub").asText(), "O claim 'sub' deve corresponder ao ID do utilizador");
        assertEquals("alice", payload.get("username").asText(), "O claim 'username' deve corresponder ao username");
        assertTrue(payload.has("iat"), "O JWT deve conter o claim 'iat' (issued at)");
        assertTrue(payload.has("exp"), "O JWT deve conter o claim 'exp' (expiration)");
        assertTrue(payload.get("exp").asLong() > payload.get("iat").asLong(),
                "O claim 'exp' deve ser posterior ao 'iat'");

        // Verificar que a signature não está vazia
        assertFalse(parts[2].isEmpty(), "A signature do JWT não deve estar vazia");

        // Verificar a interação com o mock
        verify(userRepository).findByUsername("alice");
    }
}
