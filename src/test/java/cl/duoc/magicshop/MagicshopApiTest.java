package cl.duoc.magicshop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MagicshopApiTest {

    private static final String ANCESTORS_CHOSEN = "5f8287b1-5bb6-5f4c-ad17-316a40d5bb0c";
    private static final String ANGEL_OF_MERCY = "57aaebc1-850c-503d-9f6e-bb8d00d8bf7c";
    private static final String ANGELIC_WALL = "fadda48c-6226-5ac5-a2b9-e9170d2017cd";
    private static final String AVEN_CLOUDCHASER = "8ac972b5-9f6e-5cc8-91c3-b9a40a98232e";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private JsonNode login(String email, String password) throws Exception {
        String cuerpo = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(cuerpo);
    }

    private String tokenAdmin() throws Exception {
        return "Bearer " + login("admin@magicshop.com", "admin123").get("token").asText();
    }

    private String tokenCliente() throws Exception {
        return "Bearer " + login("comprador@magicshop.com", "comprador123").get("token").asText();
    }

    private static String ordenJson(String productoId, int cantidad, boolean simularRechazo) {
        return "{\"cliente\":{\"nombre\":\"Ana\",\"apellidos\":\"Soto\",\"email\":\"ana@correo.cl\"},"
                + "\"despacho\":{\"calle\":\"Los Olmos 123\",\"region\":\"Región Metropolitana de Santiago\",\"comuna\":\"Santiago\"},"
                + "\"items\":[{\"id\":\"" + productoId + "\",\"cantidad\":" + cantidad + "}],"
                + "\"simularRechazo\":" + simularRechazo + "}";
    }

    @Test
    void loginCorrectoDevuelveTokenYUsuarioSinPassword() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@magicshop.com\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.rol").value("admin"))
                .andExpect(jsonPath("$.usuario.password").doesNotExist());
    }

    @Test
    void loginIncorrectoResponde401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@magicshop.com\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Correo o contraseña incorrectos."));
    }

    @Test
    void catalogoYCategoriasSonPublicos() throws Exception {
        mvc.perform(get("/api/productos")).andExpect(status().isOk()).andExpect(jsonPath("$.length()", greaterThanOrEqualTo(32)));
        mvc.perform(get("/api/categorias")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(6)));
    }

    @Test
    void filtroDeStockCriticoSoloDevuelveStockBajo() throws Exception {
        mvc.perform(get("/api/productos").param("stock_lte", "3").param("eliminado_ne", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.stock > 3)]").isEmpty());
    }

    @Test
    void filtroNoPermitidoResponde400() throws Exception {
        mvc.perform(get("/api/productos").param("password", "x")).andExpect(status().isBadRequest());
    }

    @Test
    void crearProductoExigeSerAdmin() throws Exception {
        String cuerpo = "{\"id\":\"carta-prueba-1\",\"nombre\":\"Carta de Prueba\",\"categoria\":\"criaturas\","
                + "\"imagen\":\"https://ejemplo.cl/carta.png\",\"precio\":1000,\"stock\":5,\"oferta\":0}";

        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/productos").header(HttpHeaders.AUTHORIZATION, tokenCliente())
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/productos").header(HttpHeaders.AUTHORIZATION, tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("carta-prueba-1"));
    }

    @Test
    void precioNegativoSeRechazaConMensajePorCampo() throws Exception {
        mvc.perform(post("/api/productos").header(HttpHeaders.AUTHORIZATION, tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"carta-prueba-2\",\"nombre\":\"Otra\",\"precio\":-5,\"stock\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.precio").value("El precio no puede ser negativo."));
    }

    @Test
    void listaDeUsuariosSoloParaAdminYSinPasswords() throws Exception {
        mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, tokenCliente()))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, tokenAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void registroIgnoraElRolEnviadoYCreaCliente() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"run\":\"12345678-5\",\"nombre\":\"Luis\",\"apellidos\":\"Rojas\","
                                + "\"email\":\"luis@correo.cl\",\"password\":\"clave123\",\"rol\":\"admin\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.rol").value("cliente"))
                .andExpect(jsonPath("$.token").isNotEmpty());

        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Luis\",\"apellidos\":\"Rojas\",\"email\":\"luis@correo.cl\",\"password\":\"clave123\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void compraPagadaDescuentaStockYCalculaTotalEnElServidor() throws Exception {
        mvc.perform(post("/api/ordenes").contentType(MediaType.APPLICATION_JSON).content(ordenJson(ANCESTORS_CHOSEN, 2, false)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("pagada"))
                .andExpect(jsonPath("$.total").value(2900))
                .andExpect(jsonPath("$.items[0].id").value(ANCESTORS_CHOSEN));

        mvc.perform(get("/api/productos/" + ANCESTORS_CHOSEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(8));
    }

    @Test
    void compraConStockInsuficienteQuedaRechazadaYNoDescuenta() throws Exception {
        mvc.perform(post("/api/ordenes").contentType(MediaType.APPLICATION_JSON).content(ordenJson(ANGEL_OF_MERCY, 99, false)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("rechazada"))
                .andExpect(jsonPath("$.motivo").value(containsString("Stock insuficiente")));

        mvc.perform(get("/api/productos/" + ANGEL_OF_MERCY))
                .andExpect(jsonPath("$.stock").value(9));
    }

    @Test
    void pagoRechazadoSimuladoNoDescuentaStock() throws Exception {
        mvc.perform(post("/api/ordenes").contentType(MediaType.APPLICATION_JSON).content(ordenJson(ANGELIC_WALL, 1, true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("rechazada"));

        mvc.perform(get("/api/productos/" + ANGELIC_WALL))
                .andExpect(jsonPath("$.stock").value(12));
    }

    @Test
    void clienteSoloVeSusPropiasOrdenes() throws Exception {
        String token = tokenCliente();
        mvc.perform(post("/api/ordenes").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON).content(ordenJson(AVEN_CLOUDCHASER, 1, false)))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/ordenes").contentType(MediaType.APPLICATION_JSON).content(ordenJson(AVEN_CLOUDCHASER, 1, false)))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/ordenes").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(get("/api/ordenes")).andExpect(status().isUnauthorized());
    }

    @Test
    void refreshConTokenValidoEntregaTokenNuevoUtilizable() throws Exception {
        String cuerpo = mvc.perform(post("/api/auth/refresh").header(HttpHeaders.AUTHORIZATION, tokenCliente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.rol").value("cliente"))
                .andExpect(jsonPath("$.usuario.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String nuevo = "Bearer " + mapper.readTree(cuerpo).get("token").asText();

        mvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, nuevo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("comprador@magicshop.com"));
    }

    @Test
    void refreshSinTokenOConTokenInvalidoResponde401() throws Exception {
        mvc.perform(post("/api/auth/refresh")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/refresh").header(HttpHeaders.AUTHORIZATION, "Bearer token-inventado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminNoPuedeEliminarSuPropiaCuenta() throws Exception {
        JsonNode sesion = login("admin@magicshop.com", "admin123");
        long id = sesion.get("usuario").get("id").asLong();
        mvc.perform(delete("/api/usuarios/" + id).header(HttpHeaders.AUTHORIZATION, "Bearer " + sesion.get("token").asText()))
                .andExpect(status().isConflict());
    }
}
