package com.finansmart.service;

import com.finansmart.model.Movimiento;
import com.finansmart.model.Usuario;
import com.finansmart.repository.MovimientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class MovimientoService {

    @Autowired
    private MovimientoRepository movimientoRepository;

    public List<Movimiento> listarPorUsuario(Usuario usuario) {
        return movimientoRepository.findByUsuarioOrderByFechaDesc(usuario);
    }

    public Movimiento guardar(Movimiento movimiento) {
        return movimientoRepository.save(movimiento);
    }

    public void eliminar(Long id) {
        movimientoRepository.deleteById(id);
    }

    public BigDecimal totalIngresosMes(Usuario usuario) {
        YearMonth ym = YearMonth.now();
        LocalDate ini = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();
        return movimientoRepository.totalIngresosMes(usuario, ini, fin);
    }

    public BigDecimal totalGastosMes(Usuario usuario) {
        YearMonth ym = YearMonth.now();
        LocalDate ini = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();
        return movimientoRepository.totalGastosMes(usuario, ini, fin);
    }

    public List<Movimiento> listarPorUsuarioYRango(Usuario usuario, LocalDate inicio, LocalDate fin) {
        return movimientoRepository.findByUsuarioAndFechaBetweenOrderByFechaDesc(usuario, inicio, fin);
    }

    public Movimiento buscarPorId(Long id) {
        return movimientoRepository.findById(id).orElse(null);
    }

    public List<Object[]> obtenerIngresosYGastosPorMes(Usuario usuario) {
        return movimientoRepository.findTotalesPorMes(usuario.getId());
    }

    public List<Object[]> gastosPorCategoriaEnMes(Usuario usuario, java.time.LocalDate inicio, java.time.LocalDate fin) {
        return movimientoRepository.findGastosPorCategoria(usuario.getId(), inicio, fin);
    }

    public List<Object[]> obtenerIngresosYGastosPorMes(Usuario usuario, LocalDate inicio, LocalDate fin) {
        return movimientoRepository.findTotalesPorMesRango(usuario.getId(), inicio, fin);
    }

}
