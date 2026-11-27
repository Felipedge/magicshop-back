package cl.duoc.magicshop.security;

import cl.duoc.magicshop.entity.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas unitarias de la generación y verificación del JWT (sin levantar Spring). */
class JwtServiceTest {

    private static final String SECRETO = "clave-de-prueba-de-al-menos-32-caracteres-123";

    private static Usuario usuario() {
        Usuario u = new Usuario();
        u.setId(7L);
        u.setEmail("ana@correo.cl");
        u.setRol(Usuario.ROL_CLIENTE);
        return u;
    }

    @Test
    void tokenGeneradoSeVerificaYDevuelveElCorreo() {
        JwtService jwt = new JwtService(SECRETO, 60);
        String token = jwt.generar(usuario());
        assertEquals("ana@correo.cl", jwt.emailDe(token).orElseThrow());
    }

    @Test
    void tokenVencidoSeRechaza() {
        JwtService jwt = new JwtService(SECRETO, -1);
        String token = jwt.generar(usuario());
        assertTrue(jwt.emailDe(token).isEmpty());
    }

    @Test
    void tokenFirmadoConOtraClaveSeRechaza() {
        String ajeno = new JwtService("otra-clave-distinta-de-al-menos-32-caracteres", 60).generar(usuario());
        assertTrue(new JwtService(SECRETO, 60).emailDe(ajeno).isEmpty());
    }

    @Test
    void textoQueNoEsUnTokenSeRechaza() {
        JwtService jwt = new JwtService(SECRETO, 60);
        assertTrue(jwt.emailDe("no-es-un-token").isEmpty());
        assertTrue(jwt.emailDe("").isEmpty());
    }

    @Test
    void secretoCortoNoSeAcepta() {
        assertThrows(IllegalStateException.class, () -> new JwtService("corto", 60));
    }
}
