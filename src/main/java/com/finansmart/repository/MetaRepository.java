package com.finansmart.repository;

import com.finansmart.model.Meta;
import com.finansmart.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MetaRepository extends JpaRepository<Meta, Long> {

    List<Meta> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario);

    List<Meta> findByUsuarioAndCompletadaFalseOrderByFechaCreacionDesc(Usuario usuario);

    long countByUsuarioAndCompletadaFalse(Usuario usuario);

    @Query("select m from Meta m where m.usuario = :usuario and m.completada = false order by (m.montoActual / m.montoObjetivo) desc")
    List<Meta> topMetasPorAvance(@Param("usuario") Usuario usuario);

    @Query("SELECT m.nombre, m.montoActual, m.montoObjetivo " +
           "FROM Meta m WHERE m.usuario.id = :usuarioId")
    List<Object[]> progresoDeMetasPorUsuario(@Param("usuarioId") Long usuarioId);
}
