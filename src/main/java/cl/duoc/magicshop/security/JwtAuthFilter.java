package cl.duoc.magicshop.security;

import cl.duoc.magicshop.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarios;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarios) {
        this.jwtService = jwtService;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = request.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = cabecera.substring(7);
            jwtService.emailDe(token)
                    .flatMap(usuarios::findByEmailIgnoreCase)
                    .ifPresent(usuario -> {
                        var autoridad = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().toUpperCase());
                        var autenticacion = new UsernamePasswordAuthenticationToken(AuthUser.de(usuario), null, List.of(autoridad));
                        SecurityContextHolder.getContext().setAuthentication(autenticacion);
                    });
        }
        chain.doFilter(request, response);
    }
}
