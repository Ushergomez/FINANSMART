package com.finansmart.service;

import com.finansmart.model.Meta;
import com.finansmart.model.Usuario;
import com.finansmart.repository.MetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MetaService {

    @Autowired
    private MetaRepository metaRepository;

    public List<Meta> listarPorUsuario(Usuario usuario) {
        return metaRepository.findByUsuarioOrderByFechaCreacionDesc(usuario);
    }

    public List<Meta> listarActivas(Usuario usuario) {
        return metaRepository.findByUsuarioAndCompletadaFalseOrderByFechaCreacionDesc(usuario);
    }

    public long contarActivas(Usuario usuario) {
        return metaRepository.countByUsuarioAndCompletadaFalse(usuario);
    }

    public List<Meta> topPorAvance(Usuario usuario) {
        return metaRepository.topMetasPorAvance(usuario);
    }

    public Meta buscarPorId(Long id) {
        return metaRepository.findById(id).orElse(null);
    }

    public Meta guardar(Meta meta) {
        if (meta.getMontoObjetivo() != null && meta.getMontoActual() != null) {
            if (meta.getMontoActual().compareTo(meta.getMontoObjetivo()) >= 0) {
                meta.setCompletada(true);
            } else {
                meta.setCompletada(false);
            }
        }
        return metaRepository.save(meta);
    }

    public void eliminar(Long id) {
        metaRepository.deleteById(id);
    }

    public void aportar(Meta meta, BigDecimal aporte) {
        if (aporte == null || aporte.compareTo(BigDecimal.ZERO) <= 0) return;
        BigDecimal nuevo = (meta.getMontoActual() == null ? BigDecimal.ZERO : meta.getMontoActual()).add(aporte);
        meta.setMontoActual(nuevo);
        guardar(meta);
    }

    public void completar(Meta meta) {
        meta.setCompletada(true);
        metaRepository.save(meta);
    }

    public List<Object[]> progresoPorMeta(Long usuarioId) {
        return metaRepository.progresoDeMetasPorUsuario(usuarioId);
    }

}
