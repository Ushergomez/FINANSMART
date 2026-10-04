package com.finansmart.repository;

import com.finansmart.model.Movimiento;
import com.finansmart.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByUsuarioOrderByFechaDesc(Usuario usuario);

    List<Movimiento> findByUsuarioAndFechaBetweenOrderByFechaDesc(Usuario usuario, LocalDate inicio, LocalDate fin);

    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM Movimiento m WHERE m.usuario = :usuario AND m.tipo = 'INGRESO' AND m.fecha BETWEEN :inicio AND :fin")
    BigDecimal totalIngresosMes(Usuario usuario, LocalDate inicio, LocalDate fin);

    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM Movimiento m WHERE m.usuario = :usuario AND m.tipo = 'GASTO' AND m.fecha BETWEEN :inicio AND :fin")
    BigDecimal totalGastosMes(Usuario usuario, LocalDate inicio, LocalDate fin);

    @Query("SELECT " +
            "FUNCTION('DATE_FORMAT', m.fecha, '%Y-%m') AS mes, " +
            "SUM(CASE WHEN m.tipo = 'INGRESO' THEN m.monto ELSE 0 END) AS ingresos, " +
            "SUM(CASE WHEN m.tipo = 'GASTO' THEN m.monto ELSE 0 END) AS gastos " +
            "FROM Movimiento m " +
            "WHERE m.usuario.id = :usuarioId " +
            "GROUP BY mes " +
            "ORDER BY mes ASC")
    List<Object[]> findTotalesPorMes(@Param("usuarioId") Long usuarioId);

    @Query("SELECT m.categoria, SUM(m.monto) FROM Movimiento m " +
            "WHERE m.usuario.id = :usuarioId " +
            "AND m.tipo = 'GASTO' " +
            "AND m.fecha BETWEEN :inicio AND :fin " +
            "GROUP BY m.categoria " +
            "ORDER BY SUM(m.monto) DESC")
    List<Object[]> findGastosPorCategoria(
        @Param("usuarioId") Long usuarioId,
        @Param("inicio") java.time.LocalDate inicio,
        @Param("fin") java.time.LocalDate fin
    );

    @Query("SELECT FUNCTION('DATE_FORMAT', m.fecha, '%Y-%m') AS mes, " +
           "SUM(CASE WHEN m.tipo = 'INGRESO' THEN m.monto ELSE 0 END) AS ingresos, " +
           "SUM(CASE WHEN m.tipo = 'GASTO' THEN m.monto ELSE 0 END) AS gastos " +
           "FROM Movimiento m WHERE m.usuario.id = :usuarioId " +
           "AND m.fecha >= :inicio AND m.fecha <= :fin " +
           "GROUP BY mes ORDER BY mes ASC")
    List<Object[]> findTotalesPorMesRango(
        @Param("usuarioId") Long usuarioId,
        @Param("inicio") LocalDate inicio,
        @Param("fin") LocalDate fin
    );

}
