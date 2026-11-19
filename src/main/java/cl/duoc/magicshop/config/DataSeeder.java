package cl.duoc.magicshop.config;

import cl.duoc.magicshop.entity.Categoria;
import cl.duoc.magicshop.entity.Producto;
import cl.duoc.magicshop.entity.Usuario;
import cl.duoc.magicshop.repository.CategoriaRepository;
import cl.duoc.magicshop.repository.ProductoRepository;
import cl.duoc.magicshop.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    public record SeedData(List<Categoria> categorias, List<Producto> productos) {
    }

    private final UsuarioRepository usuarios;
    private final CategoriaRepository categorias;
    private final ProductoRepository productos;
    private final PasswordEncoder encoder;
    private final ObjectMapper mapper;
    private final String adminEmail;
    private final String adminPassword;
    private final String userEmail;
    private final String userPassword;

    public DataSeeder(UsuarioRepository usuarios, CategoriaRepository categorias, ProductoRepository productos,
                      PasswordEncoder encoder, ObjectMapper mapper,
                      @Value("${app.seed.admin-email}") String adminEmail,
                      @Value("${app.seed.admin-password}") String adminPassword,
                      @Value("${app.seed.user-email}") String userEmail,
                      @Value("${app.seed.user-password}") String userPassword) {
        this.usuarios = usuarios;
        this.categorias = categorias;
        this.productos = productos;
        this.encoder = encoder;
        this.mapper = mapper;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (usuarios.count() == 0) {
            usuarios.save(usuario("11111111-1", "Admin", "Magic-Shop", adminEmail, adminPassword, Usuario.ROL_ADMIN,
                    "Región Metropolitana de Santiago", "Santiago", "Av. Libertador 1234"));
            usuarios.save(usuario("12345678-5", "Felipe", "Palma", userEmail, userPassword, Usuario.ROL_CLIENTE,
                    "Región Metropolitana de Santiago", "Providencia", "Los Crisantemos 603"));
            log.info("Usuarios de prueba creados.");
        }

        if (categorias.count() == 0 || productos.count() == 0) {
            try (InputStream entrada = new ClassPathResource("seed/seed.json").getInputStream()) {
                SeedData datos = mapper.readValue(entrada, SeedData.class);
                if (categorias.count() == 0) {
                    datos.categorias().forEach(c -> c.setId(null));
                    categorias.saveAll(datos.categorias());
                }
                if (productos.count() == 0) {
                    productos.saveAll(datos.productos());
                }
                log.info("Catálogo inicial cargado: {} categorías y {} productos.",
                        datos.categorias().size(), datos.productos().size());
            }
        }
    }

    private Usuario usuario(String run, String nombre, String apellidos, String email, String password, String rol,
                            String region, String comuna, String direccion) {
        Usuario u = new Usuario();
        u.setRun(run);
        u.setNombre(nombre);
        u.setApellidos(apellidos);
        u.setEmail(email.toLowerCase());
        u.setPassword(encoder.encode(password));
        u.setRol(rol);
        u.setRegion(region);
        u.setComuna(comuna);
        u.setDireccion(direccion);
        return u;
    }
}
