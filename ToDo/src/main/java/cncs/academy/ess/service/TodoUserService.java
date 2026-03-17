package cncs.academy.ess.service;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class TodoUserService {
    private static final int PBKDF2_ITERATIONS = 65536;
    private static final int HASH_KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final long TOKEN_EXPIRATION_MS = 3600000; // 1 hora

    private final UserRepository repository;
    private final byte[] hmacSecret;

    public TodoUserService(UserRepository userRepository, byte[] hmacSecret) {
        this.repository = userRepository;
        this.hmacSecret = hmacSecret;
    }

    public User addUser(String username, String password) throws NoSuchAlgorithmException {
        String hashedPassword = hashPassword(password);
        User user = new User(username, hashedPassword);
        int id = repository.save(user);
        user.setId(id);
        return user;
    }

    public User getUser(int id) {
        return repository.findById(id);
    }

    public void deleteUser(int id) {
        repository.deleteById(id);
    }

    public String login(String username, String password) throws NoSuchAlgorithmException {
        User user = repository.findByUsername(username);
        if (user == null) {
            return null;
        }
        if (verifyPassword(password, user.getPassword())) {
            return "Bearer " + createAuthToken(user);
        }
        return null;
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);

        byte[] hash = pbkdf2(password, salt);

        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hashBase64 = Base64.getEncoder().encodeToString(hash);
        return saltBase64 + ":" + hashBase64;
    }

    private boolean verifyPassword(String password, String storedPassword) throws NoSuchAlgorithmException {
        String[] parts = storedPassword.split(":");
        if (parts.length != 2) {
            return false;
        }
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] storedHash = Base64.getDecoder().decode(parts[1]);

        byte[] computedHash = pbkdf2(password, salt);

        return MessageDigest.isEqual(storedHash, computedHash);
    }

    private byte[] pbkdf2(String password, byte[] salt) throws NoSuchAlgorithmException {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            SecretKey key = factory.generateSecret(spec);
            spec.clearPassword();
            return key.getEncoded();
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Error computing PBKDF2 hash", e);
        }
    }

    private String createAuthToken(User user) {
        try {
            String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            long now = System.currentTimeMillis() / 1000;
            long exp = now + (TOKEN_EXPIRATION_MS / 1000);
            String payload = "{\"sub\":\"" + user.getId() + "\",\"username\":\"" + user.getUsername() + "\",\"iat\":" + now + ",\"exp\":" + exp + "}";

            Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
            String headerBase64 = encoder.encodeToString(header.getBytes());
            String payloadBase64 = encoder.encodeToString(payload.getBytes());

            String content = headerBase64 + "." + payloadBase64;
            String signature = hmacSha256(content);

            return content + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("Error creating auth token", e);
        }
    }

    private String hmacSha256(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(hmacSecret, "HmacSHA256");
        mac.init(keySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
    }

    public byte[] getHmacSecret() {
        return hmacSecret;
    }
}
