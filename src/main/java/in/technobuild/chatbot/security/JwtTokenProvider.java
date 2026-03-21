package in.technobuild.chatbot.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            log.error("JWT validation failed", ex);
            return false;
        }
    }

    public UserPrincipal extractUserPrincipal(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String userId = resolveClaim(claims, "userId", claims.getSubject());
        String username = resolveClaim(claims, "userName", userId);
        String role = resolveClaim(claims, "role", "USER");

        return UserPrincipal.builder()
                .userId(userId)
                .username(username)
                .role(role)
                .build();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    private String resolveClaim(Claims claims, String key, String fallback) {
        Object value = claims.get(key);
        return value != null ? String.valueOf(value) : fallback;
    }
}
