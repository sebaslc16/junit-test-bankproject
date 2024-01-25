package org.sebas.test.springboot.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.sebas.test.springboot.app.models.Cuenta;
import org.sebas.test.springboot.app.models.TransaccionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integracion_rt")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CuentaControllerTestRestTemplateTest {

    @Autowired
    private TestRestTemplate client;

    private ObjectMapper objectMapper;

    @LocalServerPort
    private int puerto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    private String crearUri(String uri) {
        return "http://localhost:"+ puerto + uri;
    }

    @Test
    @Order(1)
    void testTransferir() throws JsonProcessingException {
        TransaccionDto dto = new TransaccionDto();
        dto.setMonto(new BigDecimal("100"));
        dto.setCuentaDestinoId(2L);
        dto.setCuentaOrigenId(1L);
        dto.setBancoId(1L);

        ResponseEntity<String> response = client.postForEntity(crearUri("/api/cuentas/transferir"), dto, String.class);

        System.out.println(puerto);

        String json = response.getBody();

        System.out.println(json);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON,response.getHeaders().getContentType());
        assertNotNull(json);
        assertTrue(json.contains("Transferencia realizado con éxito!"));

        JsonNode jsonNode = objectMapper.readTree(json);

        assertEquals("Transferencia realizado con éxito!", jsonNode.path("mensaje").asText());
        assertEquals(LocalDate.now().toString(), jsonNode.path("date").asText());
        assertEquals("100", jsonNode.path("transaccion").path("monto").asText());
        assertEquals(1L, jsonNode.path("transaccion").path("cuentaOrigenId").asLong());

        Map<String, Object> response2 = new HashMap<>();
        response2.put("date", LocalDate.now().toString());
        response2.put("status", "0K");
        response2.put("mensaje", "Transferencia realizado con éxito!");
        response2.put("transaccion", dto);

        assertEquals(objectMapper.writeValueAsString(response2), json);

    }

    @Test
    @Order(2)
    void testDetalle() {

        ResponseEntity<Cuenta> respuesta = client.getForEntity(crearUri("/api/cuentas/1"), Cuenta.class);
        Cuenta cuenta = respuesta.getBody();

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, respuesta.getHeaders().getContentType());

        assertNotNull(cuenta);
        assertEquals(1L, cuenta.getId());
        assertEquals("Sebastian", cuenta.getPersona());
        assertEquals("900.00", cuenta.getSaldo().toPlainString());
        assertEquals(new Cuenta(1L, "Sebastian", new BigDecimal("900.00")), cuenta);
    }

    @Test
    @Order(3)
    void testListar() throws JsonProcessingException {
        ResponseEntity<Cuenta[]> respuesta = client.getForEntity(crearUri("/api/cuentas"), Cuenta[].class);
        List<Cuenta> cuentas = Arrays.asList(respuesta.getBody());

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, respuesta.getHeaders().getContentType());

        assertEquals(3, cuentas.size());

        assertEquals(1L, cuentas.get(0).getId());
        assertEquals("Sebastian", cuentas.get(0).getPersona());
        assertEquals("900.00", cuentas.get(0).getSaldo().toPlainString());

        assertEquals(2L, cuentas.get(1).getId());
        assertEquals("John", cuentas.get(1).getPersona());
        assertEquals("2100.00", cuentas.get(1).getSaldo().toPlainString());

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(cuentas));

        assertEquals(1L, json.get(0).path("id").asLong());
        assertEquals("Sebastian", json.get(0).path("persona").asText());
        assertEquals("900.0", json.get(0).path("saldo").asText());

        assertEquals(2L, json.get(1).path("id").asLong());
        assertEquals("John", json.get(1).path("persona").asText());
        assertEquals("2100.0", json.get(1).path("saldo").asText());
    }

    @Test
    @Order(4)
    void testGuardar() {

        //GIVEN
        Cuenta cuenta = new Cuenta(null, "Pepa", new BigDecimal("3800"));

        ResponseEntity<Cuenta> respuesta = client.postForEntity(crearUri("/api/cuentas"), cuenta, Cuenta.class);
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, respuesta.getHeaders().getContentType());

        Cuenta cuentaCreada = respuesta.getBody();

        assertNotNull(cuentaCreada);
        assertEquals(4L, cuentaCreada.getId());
        assertEquals("Pepa", cuentaCreada.getPersona());
        assertEquals("3800", cuentaCreada.getSaldo().toPlainString());
    }

    @Test
    @Order(5)
    void testEliminar() {

        ResponseEntity<Cuenta[]> respuesta = client.getForEntity(crearUri("/api/cuentas"), Cuenta[].class);
        List<Cuenta> cuentas = Arrays.asList(respuesta.getBody());
        assertEquals(4, cuentas.size());

        //client.delete(crearUri("/api/cuentas/3"));

        //Forma enviando directamente el id en la uri
        //ResponseEntity<Void> exchange = client.exchange(crearUri("/api/cuentas/3"), HttpMethod.DELETE, null, Void.class);

        //Forma enviando el id como una pathvariable
        Map<String, Long> pathVariable = new HashMap<>();
        pathVariable.put("id", 3L);
        ResponseEntity<Void> exchange = client.exchange(crearUri("/api/cuentas/{id}"), HttpMethod.DELETE, null, Void.class,
                pathVariable);

        assertEquals(HttpStatus.NO_CONTENT, exchange.getStatusCode());
        assertFalse(exchange.hasBody());

        respuesta = client.getForEntity(crearUri("/api/cuentas"), Cuenta[].class);
        cuentas = Arrays.asList(respuesta.getBody());
        assertEquals(3, cuentas.size());

        ResponseEntity<Cuenta> respuestaDetalle = client.getForEntity(crearUri("/api/cuentas/3"), Cuenta.class);
        assertEquals(HttpStatus.NOT_FOUND, respuestaDetalle.getStatusCode());
        assertFalse(respuestaDetalle.hasBody());
    }
}