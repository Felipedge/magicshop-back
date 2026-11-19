package cl.duoc.magicshop.service;

import cl.duoc.magicshop.dto.UsuarioDto;
import cl.duoc.magicshop.dto.UsuarioRequest;
import cl.duoc.magicshop.entity.Usuario;
import cl.duoc.magicshop.exception.ApiException;
import cl.duoc.magicshop.repository.UsuarioRepository;
import cl.duoc.magicshop.security.AuthUser;
import cl.duoc.magicshop.util.Filtros;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class UsuarioService {

    private static final Set<String> CAMPOS_FILTRO = Set.of("id", "email", "rol", "run");
    private static final Set<String> CAMPOS_ORDEN = Set.of("id", "nombre", "email");

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> listar(Map<String, String> params) {
        return usuarios.findAll(Filtros.<Usuario>desde(params, CAMPOS_FILTRO), Filtros.ordenar(params, CAMPOS_ORDEN))
                .stream().map(UsuarioDto::de).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioDto obtener(Long id, AuthUser actor) {
        verificarAcceso(id, actor);
        return UsuarioDto.de(buscar(id));
    }

    @Transactional
    public UsuarioDto crear(UsuarioRequest r) {
        if (esVacio(r.nombre()) || esVacio(r.email()) || esVacio(r.password())) {
            throw ApiException.solicitudInvalida("Nombre, correo y contraseña son obligatorios.");
        }
        String email = r.email().trim().toLowerCase();
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflicto("Ya existe una cuenta con ese correo.");
        }
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword(encoder.encode(r.password()));
        usuario.setRol(rolValido(r.rol(), Usuario.ROL_CLIENTE));
        aplicarDatos(usuario, r);
        return UsuarioDto.de(usuarios.save(usuario));
    }

    @Transactional
    public UsuarioDto actualizar(Long id, UsuarioRequest r, AuthUser actor) {
        verificarAcceso(id, actor);
        Usuario usuario = buscar(id);

        if (!esVacio(r.rol()) && !r.rol().equals(usuario.getRol())) {
            if (!actor.esAdmin()) {
                throw ApiException.prohibido("No puedes cambiar tu rol.");
            }
            String nuevoRol = rolValido(r.rol(), usuario.getRol());
            if (Usuario.ROL_ADMIN.equals(usuario.getRol())) {
                if (actor.id().equals(id)) {
                    throw ApiException.conflicto("No puedes quitarte el rol de administrador.");
                }
                if (usuarios.countByRol(Usuario.ROL_ADMIN) <= 1) {
                    throw ApiException.conflicto("Debe existir al menos un administrador.");
                }
            }
            usuario.setRol(nuevoRol);
        }

        if (!esVacio(r.email())) {
            String email = r.email().trim().toLowerCase();
            if (!email.equalsIgnoreCase(usuario.getEmail()) && usuarios.existsByEmailIgnoreCase(email)) {
                throw ApiException.conflicto("Ya existe una cuenta con ese correo.");
            }
            usuario.setEmail(email);
        }
        if (!esVacio(r.password())) {
            usuario.setPassword(encoder.encode(r.password()));
        }
        aplicarDatos(usuario, r);
        return UsuarioDto.de(usuarios.save(usuario));
    }

    @Transactional
    public void eliminar(Long id, AuthUser actor) {
        Usuario usuario = buscar(id);
        if (actor.id().equals(id)) {
            throw ApiException.conflicto("No puedes eliminar tu propia cuenta.");
        }
        if (Usuario.ROL_ADMIN.equals(usuario.getRol()) && usuarios.countByRol(Usuario.ROL_ADMIN) <= 1) {
            throw ApiException.conflicto("Debe existir al menos un administrador.");
        }
        usuarios.delete(usuario);
    }

    private void aplicarDatos(Usuario usuario, UsuarioRequest r) {
        if (r.run() != null) {
            usuario.setRun(r.run());
        }
        if (!esVacio(r.nombre())) {
            usuario.setNombre(r.nombre().trim());
        }
        if (r.apellidos() != null) {
            usuario.setApellidos(r.apellidos().trim());
        }
        if (r.region() != null) {
            usuario.setRegion(r.region());
        }
        if (r.comuna() != null) {
            usuario.setComuna(r.comuna());
        }
        if (r.direccion() != null) {
            usuario.setDireccion(r.direccion());
        }
    }

    private void verificarAcceso(Long id, AuthUser actor) {
        if (actor == null || (!actor.esAdmin() && !actor.id().equals(id))) {
            throw ApiException.prohibido("No tienes permisos para ver o modificar este usuario.");
        }
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado."));
    }

    private static String rolValido(String rol, String porDefecto) {
        if (esVacio(rol)) {
            return porDefecto;
        }
        if (!Usuario.ROL_ADMIN.equals(rol) && !Usuario.ROL_CLIENTE.equals(rol)) {
            throw ApiException.solicitudInvalida("El rol debe ser 'admin' o 'cliente'.");
        }
        return rol;
    }

    private static boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
