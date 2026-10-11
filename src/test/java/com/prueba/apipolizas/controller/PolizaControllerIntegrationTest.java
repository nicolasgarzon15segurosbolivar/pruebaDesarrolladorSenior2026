package com.prueba.apipolizas.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integracion sobre los endpoints, levantando el contexto completo
 * y usando los datos de ejemplo cargados por data.sql.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PolizaControllerIntegrationTest {

    private static final String API_KEY = "123456";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sinApiKeyDevuelve401() throws Exception {
        mockMvc.perform(get("/polizas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conApiKeyListaLasPolizas() throws Exception {
        mockMvc.perform(get("/polizas").header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    void filtraPorTipoYEstado() throws Exception {
        mockMvc.perform(get("/polizas")
                        .param("tipo", "COLECTIVA")
                        .param("estado", "VIGENTE")
                        .header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tipo").value("COLECTIVA"))
                .andExpect(jsonPath("$.content[0].estado").value("VIGENTE"));
    }

    @Test
    void polizaInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/polizas/9999").header("x-api-key", API_KEY))
                .andExpect(status().isNotFound());
    }

    @Test
    void renovarPolizaCanceladaDevuelve409() throws Exception {
        // La poliza 3 viene CANCELADA en los datos de ejemplo
        mockMvc.perform(post("/polizas/3/renovar").header("x-api-key", API_KEY))
                .andExpect(status().isConflict());
    }

    @Test
    void agregarRiesgoAIndividualDevuelve409() throws Exception {
        // La poliza 1 es INDIVIDUAL
        String body = "{\"direccionInmueble\":\"Calle X\",\"arrendatario\":\"Pedro\"}";
        mockMvc.perform(post("/polizas/1/riesgos")
                        .header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void agregarRiesgoConBodyInvalidoDevuelve400() throws Exception {
        String body = "{\"direccionInmueble\":\"\",\"arrendatario\":\"\"}";
        mockMvc.perform(post("/polizas/2/riesgos")
                        .header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void agregarRiesgoAColectivaDevuelve201() throws Exception {
        String body = "{\"direccionInmueble\":\"Calle Nueva 123\",\"arrendatario\":\"Luis\"}";
        mockMvc.perform(post("/polizas/2/riesgos")
                        .header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    void coreMockRegistraEvento() throws Exception {
        String body = "{\"evento\":\"ACTUALIZACION\",\"polizaId\":555}";
        mockMvc.perform(post("/core-mock/evento")
                        .header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECIBIDO"));
    }
}
