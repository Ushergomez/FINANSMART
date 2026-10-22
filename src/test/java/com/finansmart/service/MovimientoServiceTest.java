package com.finansmart.service;

import com.finansmart.model.Movimiento;
import com.finansmart.model.Usuario;
import com.finansmart.repository.MovimientoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock
    private MovimientoRepository movimientoRepository;

    @InjectMocks
    private MovimientoService movimientoService;

    @Test
    void guardarDevuelveElMovimientoGuardado() {
        // Arrange: preparar los datos y el repositorio simulado.
        Movimiento movimiento = mock(Movimiento.class);
        when(movimientoRepository.save(movimiento))
                .thenReturn(movimiento);

        // Act: ejecutar el servicio real.
        Movimiento resultado = movimientoService.guardar(movimiento);

        // Assert: comprobar el resultado y la interacción.
        assertSame(movimiento, resultado);
        verify(movimientoRepository).save(movimiento);
    }

    @Test
    void listarPorUsuarioDevuelveSusMovimientos() {
        Usuario usuario = mock(Usuario.class);
        Movimiento movimiento = mock(Movimiento.class);
        List<Movimiento> esperados = List.of(movimiento);

        when(movimientoRepository.findByUsuarioOrderByFechaDesc(usuario))
                .thenReturn(esperados);

        List<Movimiento> resultado =
                movimientoService.listarPorUsuario(usuario);

        assertEquals(esperados, resultado);
        verify(movimientoRepository)
                .findByUsuarioOrderByFechaDesc(usuario);
    }

    @Test
    void buscarPorIdDevuelveElMovimientoCuandoExiste() {
        Long id = 1L;
        Movimiento movimiento = mock(Movimiento.class);
        when(movimientoRepository.findById(id))
                .thenReturn(Optional.of(movimiento));

        Movimiento resultado = movimientoService.buscarPorId(id);

        assertSame(movimiento, resultado);
        verify(movimientoRepository).findById(id);
    }

    @Test
    void buscarPorIdDevuelveNullCuandoNoExiste() {
        Long id = 99L;
        when(movimientoRepository.findById(id))
                .thenReturn(Optional.empty());

        Movimiento resultado = movimientoService.buscarPorId(id);

        assertNull(resultado);
        verify(movimientoRepository).findById(id);
    }

    @Test
    void eliminarInvocaAlRepositorioConElIdIndicado() {
        Long id = 1L;

        movimientoService.eliminar(id);

        verify(movimientoRepository).deleteById(id);
    }

    @Test
    void listarPorUsuarioYRangoConsultaLasFechasIndicadas() {
        Usuario usuario = mock(Usuario.class);
        LocalDate inicio = LocalDate.of(2026, 10, 1);
        LocalDate fin = LocalDate.of(2026, 10, 31);
        Movimiento movimiento = mock(Movimiento.class);
        List<Movimiento> esperados = List.of(movimiento);

        when(movimientoRepository
                .findByUsuarioAndFechaBetweenOrderByFechaDesc(
                        usuario, inicio, fin))
                .thenReturn(esperados);

        List<Movimiento> resultado =
                movimientoService.listarPorUsuarioYRango(
                        usuario, inicio, fin);

        assertEquals(esperados, resultado);
        verify(movimientoRepository)
                .findByUsuarioAndFechaBetweenOrderByFechaDesc(
                        usuario, inicio, fin);
    }
}