package cncs.academy.ess.controller;

import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.Handler;
import io.javalin.http.HandlerType;
import io.javalin.http.UnauthorizedResponse;
import org.casbin.jcasbin.main.Enforcer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

public class AuthorizationMiddleware implements Handler {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationMiddleware.class);
    private final byte[] hmacSecret;
    private final Enforcer enforcer;

    public AuthorizationMiddleware(byte[] hmacSecret, Enforcer enforcer) {
        this.hmacSecret = hmacSecret;
        this.enforcer = enforcer;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        // if method is OPTIONS bypass auth middleware
        if (ctx.method() == HandlerType.OPTIONS) {
            return;
        }

        // Allow unauthenticated requests to /login only
        if (ctx.path().equals("/login") && ctx.method().name().equals("POST"))
            return;

        // Check if authorization header exists
        String authorizationHeader = ctx.header("Authorization");
        String path = ctx.path();
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.info("Authorization header is missing or invalid '{}' for path '{}'", authorizationHeader, path);
            throw new UnauthorizedResponse();
        }

        // Extract token from authorization header
        String token = authorizationHeader.substring(7); // Remove "Bearer "

        // Validate JWT token and extract claims
        String[] claims = validateTokenAndGetClaims(token);
        if (claims == null) {
            logger.info("Authorization token is invalid");
            throw new UnauthorizedResponse();
        }

        int userId = Integer.parseInt(claims[0]);
        String username = claims[1];

        // Add user ID to context for use in route handlers
        ctx.attribute("userId", userId);

        // RBAC check with Casbin
        String method = ctx.method().name();
        if (!enforcer.enforce(username, path, method)) {
            logger.info("Access denied for user '{}' on {} {}", username, method, path);
            throw new ForbiddenResponse();
        }
    }

    /**
     * Validates the JWT token and returns [userId, username] or null if invalid.
     */
    private String[] validateTokenAndGetClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String headerPayload = parts[0] + "." + parts[1];
            String providedSignature = parts[2];

            // Recalculate HMAC signature and compare (constant-time)
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(hmacSecret, "HmacSHA256");
            mac.init(keySpec);
            byte[] computedSignatureBytes = mac.doFinal(headerPayload.getBytes());
            String computedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(computedSignatureBytes);

            if (!MessageDigest.isEqual(computedSignature.getBytes(), providedSignature.getBytes())) {
                logger.info("JWT signature verification failed");
                return null;
            }

            // Decode payload and extract claims
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

            // Extract "sub" (userId)
            String sub = extractJsonValue(payloadJson, "sub");
            if (sub == null) {
                return null;
            }

            // Extract "username"
            String username = extractJsonValue(payloadJson, "username");
            if (username == null) {
                return null;
            }

            // Check expiration
            String expStr = extractJsonValue(payloadJson, "exp");
            if (expStr != null) {
                long exp = Long.parseLong(expStr);
                long now = System.currentTimeMillis() / 1000;
                if (now > exp) {
                    logger.info("JWT token has expired");
                    return null;
                }
            }

            return new String[]{sub, username};
        } catch (Exception e) {
            logger.info("Error validating JWT token: {}", e.getMessage());
            return null;
        }
    }

    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) {
            return null;
        }
        int valueStart = keyIndex + searchKey.length();
        // Skip whitespace
        while (valueStart < json.length() && json.charAt(valueStart) == ' ') {
            valueStart++;
        }
        if (valueStart >= json.length()) {
            return null;
        }

        // Check if value is a string (starts with quote)
        if (json.charAt(valueStart) == '"') {
            int valueEnd = json.indexOf('"', valueStart + 1);
            if (valueEnd == -1) {
                return null;
            }
            return json.substring(valueStart + 1, valueEnd);
        }

        // Numeric value
        int valueEnd = valueStart;
        while (valueEnd < json.length() && json.charAt(valueEnd) != ',' && json.charAt(valueEnd) != '}') {
            valueEnd++;
        }
        return json.substring(valueStart, valueEnd).trim();
    }
}
