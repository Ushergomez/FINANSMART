package com.finansmart.service;

import com.finansmart.model.Meta;
import com.finansmart.repository.MetaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetaServiceTest {

    @Mock
    private MetaRepository metaRepository;

    @InjectMocks
    private MetaService metaService;

    @Test
    void guardarCompletaLaMetaCuandoAlcanzaElObjetivo() {
        // Arrange
        Meta meta = mock(Meta.class);
        when(meta.getMontoObjetivo())
                .thenReturn(new BigDecimal("100000"));
        when(meta.getMontoActual())
                .thenReturn(new BigDecimal("100000"));
        when(metaRepository.save(meta)).thenReturn(meta);

        // Act
        Meta resultado = metaService.guardar(meta);

        // Assert
        assertSame(meta, resultado);
        verify(meta).setCompletada(true);
        verify(metaRepository).save(meta);
    }

    @Test
    void guardarCompletaLaMetaCuandoSuperaElObjetivo() {
        Meta meta = mock(Meta.class);
        when(meta.getMontoObjetivo())
                .thenReturn(new BigDecimal("100000"));
        when(meta.getMontoActual())
                .thenReturn(new BigDecimal("120000"));

        metaService.guardar(meta);

        verify(meta).setCompletada(true);
        verify(metaRepository).save(meta);
    }

    @Test
    void guardarMarcaComoPendienteSiNoAlcanzaElObjetivo() {
        Meta meta = mock(Meta.class);
        when(meta.getMontoObjetivo())
                .thenReturn(new BigDecimal("100000"));
        when(meta.getMontoActual())
                .thenReturn(new BigDecimal("40000"));

        metaService.guardar(meta);

        verify(meta).setCompletada(false);
        verify(metaRepository).save(meta);
    }

    @Test
    void aportarSumaUnAportePositivoAlMontoActual() {
        Meta meta = mock(Meta.class);
        when(meta.getMontoActual())
                .thenReturn(new BigDecimal("40000"));

        metaService.aportar(meta, new BigDecimal("10000"));

        verify(meta).setMontoActual(new BigDecimal("50000"));
        verify(metaRepository).save(meta);
    }

    @Test
    void aportarUsaCeroCuandoElMontoActualEsNull() {
        Meta meta = mock(Meta.class);

        metaService.aportar(meta, new BigDecimal("15000"));

        verify(meta).setMontoActual(new BigDecimal("15000"));
        verify(metaRepository).save(meta);
    }

    @Test
    void aportarIgnoraUnAporteNull() {
        Meta meta = mock(Meta.class);

        metaService.aportar(meta, null);

        verifyNoInteractions(meta, metaRepository);
    }

    @Test
    void aportarIgnoraUnAporteIgualACero() {
        Meta meta = mock(Meta.class);

        metaService.aportar(meta, BigDecimal.ZERO);

        verifyNoInteractions(meta, metaRepository);
    }

    @Test
    void aportarIgnoraUnAporteNegativo() {
        Meta meta = mock(Meta.class);

        metaService.aportar(meta, new BigDecimal("-10000"));

        verifyNoInteractions(meta, metaRepository);
    }

    @Test
    void buscarPorIdDevuelveNullCuandoLaMetaNoExiste() {
        Long id = 99L;
        when(metaRepository.findById(id))
                .thenReturn(Optional.empty());

        Meta resultado = metaService.buscarPorId(id);

        assertNull(resultado);
        verify(metaRepository).findById(id);
    }

    @Test
    void completarMarcaLaMetaYLaGuarda() {
        Meta meta = mock(Meta.class);

        metaService.completar(meta);

        verify(meta).setCompletada(true);
        verify(metaRepository).save(meta);
    }
}