package com.finansmart.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "meta")
public class Meta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "monto_objetivo", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoObjetivo;

    @Column(name = "monto_actual", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoActual = BigDecimal.ZERO;

    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    @Column(nullable = false)
    private Boolean completada = false;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getMontoObjetivo() { return montoObjetivo; }
    public void setMontoObjetivo(BigDecimal montoObjetivo) { this.montoObjetivo = montoObjetivo; }

    public BigDecimal getMontoActual() { return montoActual; }
    public void setMontoActual(BigDecimal montoActual) { this.montoActual = montoActual; }

    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }

    public Boolean getCompletada() { return completada; }
    public void setCompletada(Boolean completada) { this.completada = completada; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    @Transient
    public BigDecimal getProgreso() {
        if (montoObjetivo == null || montoObjetivo.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        if (montoActual == null) return BigDecimal.ZERO;
        return montoActual.divide(montoObjetivo, 4, java.math.RoundingMode.HALF_UP);
    }

    @Transient
    public boolean isActiva() {
        return Boolean.FALSE.equals(completada);
    }

    @Transient
    public int getProgresoPct() {
        if (montoObjetivo == null || montoObjetivo.compareTo(BigDecimal.ZERO) == 0) return 0;
        BigDecimal actual = (montoActual == null) ? BigDecimal.ZERO : montoActual;
        BigDecimal ratio = actual.divide(montoObjetivo, 4, java.math.RoundingMode.HALF_UP);
        int pct = ratio.multiply(new BigDecimal("100"))
                       .setScale(0, java.math.RoundingMode.HALF_UP)
                       .intValue();
        if (pct < 0) pct = 0;
        if (pct > 100) pct = 100;
        return pct;
    }
}
