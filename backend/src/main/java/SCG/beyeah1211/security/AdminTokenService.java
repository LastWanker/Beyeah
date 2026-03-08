package SCG.beyeah1211.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

@Service
public class AdminTokenService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Value("${app.admin-auth.token-secret:${app.auth.token-secret:change-me-in-production}-admin}")
    private String tokenSecret;

    @Value("${app.admin-auth.token-expire-seconds:43200}")
    private long tokenExpireSeconds;

    public String issueToken(Integer adminUserId) {
        long expireAt = System.currentTimeMillis() + tokenExpireSeconds * 1000;
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String payload = adminUserId + ":" + expireAt + ":" + nonce;
        String payloadBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String signature = sign(payloadBase64);
        return payloadBase64 + "." + signature;
    }

    public Integer resolveAdminIdFromAuthorization(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return null;
        }
        return resolveAdminIdFromToken(authorizationHeader.substring(TOKEN_PREFIX.length()).trim());
    }

    public Integer resolveAdminIdFromToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            return null;
        }

        String payloadBase64 = parts[0];
        String signature = parts[1];
        String expected = sign(payloadBase64);
        if (!MessageDigest.isEqual(signature.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8))) {
            return null;
        }

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(payloadBase64);
            String payload = new String(decoded, StandardCharsets.UTF_8);
            String[] payloadParts = payload.split(":");
            if (payloadParts.length != 3) {
                return null;
            }
            int adminId = Integer.parseInt(payloadParts[0]);
            long expireAt = Long.parseLong(payloadParts[1]);
            if (adminId <= 0 || expireAt < System.currentTimeMillis()) {
                return null;
            }
            return adminId;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String sign(String payloadBase64) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(tokenSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] signBytes = mac.doFinal(payloadBase64.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signBytes);
        } catch (Exception e) {
            throw new IllegalStateException("sign admin token failed", e);
        }
    }
}
