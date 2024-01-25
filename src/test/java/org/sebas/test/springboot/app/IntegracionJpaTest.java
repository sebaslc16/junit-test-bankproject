package org.sebas.test.springboot.app;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.sebas.test.springboot.app.models.Banco;
import org.sebas.test.springboot.app.models.Cuenta;
import org.sebas.test.springboot.app.repositories.BancoRepository;
import org.sebas.test.springboot.app.repositories.CuentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

//@DataJpaTest habilita lo referente a base de datos de prueba, jpa y repositorios para realizar pruebas unitarias de integracion con base de datos
@Tag("integracion_jpa")
@DataJpaTest
public class IntegracionJpaTest {
    @Autowired
    CuentaRepository cuentaRepository;

    @Autowired
    BancoRepository bancoRepository;

    @Tag("cuenta")
    @Tag("banco")
    @DisplayName("Probando atributos de cuenta y banco!")
    @Test
    void testFindById() {
        Optional<Cuenta> cuenta = cuentaRepository.findById(3L);
        assertTrue(cuenta.isPresent());
        assertEquals("Jorge", cuenta.orElseThrow().getPersona(), () -> "El nombre de la persona no era el esperado");

        Optional<Banco> banco = bancoRepository.findById(1L);
        assertTrue(banco.isPresent());
        assertEquals("Banco Financiero", banco.orElseThrow().getNombre(), () -> "El nombre de la cuenta no era el esperado");
        assertTrue(banco.get().getTotalTransferencias() >= 0);
    }

    @Tag("cuenta")
    @DisplayName("Test para el metodo de buscar persona por nombre!")
    @Test
    void testFindByPersona() {
        Optional<Cuenta> cuenta = cuentaRepository.findByPersona("Sebastian");
        assertTrue(cuenta.isPresent());
        assertEquals("Sebastian", cuenta.orElseThrow().getPersona());
        assertEquals("1000.00", cuenta.orElseThrow().getSaldo().toPlainString());
    }


    @Tag("cuenta")
    @Tag("exception")
    @DisplayName("Test de excepcion cuando no existe la persona buscada!")
    @Test
    void testFindByPersonaThrowException() {
        Optional<Cuenta> cuenta = cuentaRepository.findByPersona("Pablo");

        assertThrows(NoSuchElementException.class, cuenta::orElseThrow,()->"No se encuentra la persona con le nombre buscado!");
        assertFalse(cuenta.isPresent());

    }

    @Test
    void testFindAll() {
        List<Cuenta> cuentas = cuentaRepository.findAll();
        assertFalse(cuentas.isEmpty());
        assertEquals(3, cuentas.size());
    }

    @Test
    void testSave() {
        //given
        Cuenta cuentaPepe = new Cuenta(null, "Pepe", new BigDecimal("3000"));
        //Banco bancoNew = new Banco(null, "Banco generico", 0);

        //When
        //Cuenta cuenta = cuentaRepository.findByPersona("Pepe").orElseThrow();
        Cuenta cuenta = cuentaRepository.save(cuentaPepe);
        //Banco banco = BancoRepository.save(bancoNew);
        //Cuenta cuenta = cuentaRepository.findById(cuentaSave.getId()).orElseThrow();

        //then
        assertEquals("Pepe", cuenta.getPersona());
        assertEquals("3000", cuenta.getSaldo().toPlainString());
        assertEquals(cuenta.getId().getClass(),Long.class);
        //assertEquals(3, cuenta.getId());
    }

    @Test
    void testUpdate() {
        //given
        Cuenta cuentaPepe = new Cuenta(null, "Pepe", new BigDecimal("3000"));

        //When
        Cuenta cuenta = cuentaRepository.save(cuentaPepe);
        //Cuenta cuenta = cuentaRepository.findByPersona("Pepe").orElseThrow();
        //Cuenta cuenta = cuentaRepository.findById(cuentaSave.getId()).orElseThrow();

        //then
        assertEquals("Pepe", cuenta.getPersona());
        assertEquals("3000", cuenta.getSaldo().toPlainString());
        //assertEquals(3, cuenta.getId());

        cuenta.setSaldo(new BigDecimal("3800"));
        cuenta.setPersona("Jorge");

        //When
        Cuenta cuentaUpdated = cuentaRepository.save(cuenta);

        //Then
        assertEquals("3800", cuentaUpdated.getSaldo().toPlainString());
        assertEquals("Jorge", cuentaUpdated.getPersona());

    }

    @Test
    void testDelete() {
        //given
        Cuenta cuenta = cuentaRepository.findById(2L).orElseThrow();

        //then
        assertEquals("John", cuenta.getPersona());

        //when
        cuentaRepository.delete(cuenta);

        //then
        assertThrows(NoSuchElementException.class, () -> {
            cuentaRepository.findByPersona("John").orElseThrow();
        });
        assertEquals(2, cuentaRepository.findAll().size());
    }
}
