package cl.duoc.magicshop.security;

import cl.duoc.magicshop.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final SecretKey clave;
    private final long minutosExpiracion;

    public JwtService(@Value("${app.jwt.secret:}") String secreto,
                      @Value("${app.jwt.expiration-minutes:120}") long minutosExpiracion) {
        if (secreto == null || secreto.isBlank()) {
            byte[] aleatorio = new byte[48];
            new SecureRandom().nextBytes(aleatorio);
            this.clave = Keys.hmacShaKeyFor(aleatorio);
        } else {
            byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) {
                throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres.");
            }
            this.clave = Keys.hmacShaKeyFor(bytes);
        }
        this.minutosExpiracion = minutosExpiracion;
    }

    public String generar(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("uid", usuario.getId())
                .claim("rol", usuario.getRol())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusSeconds(minutosExpiracion * 60)))
                .signWith(clave)
                .compact();
    }

    public Optional<String> emailDe(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
