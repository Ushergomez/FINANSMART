package com.finansmart.controller;

import com.finansmart.model.Movimiento;
import com.finansmart.model.Usuario;
import com.finansmart.service.MovimientoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/movimientos")
public class MovimientoController {

    private static final Logger log = LoggerFactory.getLogger(MovimientoController.class);

    @Autowired
    private MovimientoService movimientoService;

    @Autowired
    private com.finansmart.repository.MovimientoRepository movimientoRepository;

    @GetMapping
    public String listar(
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate desde,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate hasta,
            Model model,
            HttpSession session) {

        log.info("GET /movimientos recibido");

        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) {
            log.warn("Sesión nula al listar movimientos. Redirigiendo a /login");
            return "redirect:/login";
        }
        log.info("Usuario en sesión: {}", u.getUsername());

        try {
            YearMonth ym = YearMonth.now();
            LocalDate iniDef = ym.atDay(1);
            LocalDate finDef = ym.atEndOfMonth();

            LocalDate ini = (desde != null) ? desde : iniDef;
            LocalDate fin = (hasta != null) ? hasta : finDef;

            List<Movimiento> lista = movimientoService.listarPorUsuarioYRango(u, ini, fin);
            BigDecimal ingresosRango = movimientoRepository.totalIngresosMes(u, ini, fin);
            BigDecimal gastosRango   = movimientoRepository.totalGastosMes(u, ini, fin);
            BigDecimal saldoRango    = ingresosRango.subtract(gastosRango);

            model.addAttribute("movimientos", lista);
            model.addAttribute("ingresosMes", ingresosRango);
            model.addAttribute("gastosMes", gastosRango);
            model.addAttribute("saldoMes", saldoRango);

            model.addAttribute("desde", ini);
            model.addAttribute("hasta", fin);

            log.info("Movimientos listados correctamente. Total registros en rango: {}", lista.size());
            return "movimientos-lista";
        } catch (Exception e) {
            log.error("Error listando movimientos: {}", e.getMessage(), e);
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model, HttpSession session) {
        log.info("GET /movimientos/nuevo recibido");

        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) {
            log.warn("Sesión nula al crear movimiento. Redirigiendo a /login");
            return "redirect:/login";
        }

        Movimiento m = new Movimiento();
        m.setTipo(Movimiento.TipoMovimiento.INGRESO); // valor por defecto
        model.addAttribute("movimiento", m);
        return "movimientos-form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("movimiento") Movimiento movimiento,
                        BindingResult result,
                        HttpSession session,
                        Model model,
                        org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {

        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";

        if (result.hasErrors()) {
            model.addAttribute("error", "Revisa los campos del formulario.");
            return "movimientos-form";
        }

        // Validaciones mínimas para evitar 500
        if (movimiento.getMonto() == null || movimiento.getMonto().compareTo(new BigDecimal("0.00")) <= 0) {
            model.addAttribute("error", "El monto es obligatorio y debe ser mayor a 0.");
            return "movimientos-form";
        }
        if (movimiento.getFecha() == null) {
            model.addAttribute("error", "La fecha es obligatoria.");
            return "movimientos-form";
        }

        movimiento.setUsuario(u);

        try {
            movimientoService.guardar(movimiento);
            redirectAttrs.addFlashAttribute("success", "Movimiento creado correctamente.");
            return "redirect:/movimientos";
        } catch (Exception e) {
            log.error("Error al guardar movimiento: {}", e.getMessage(), e);
            model.addAttribute("error", "No se pudo guardar el movimiento.");
            return "movimientos-form";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";

        Movimiento m = movimientoService.buscarPorId(id);
        if (m == null || !m.getUsuario().getId().equals(u.getId())) {
            return "redirect:/movimientos";
        }

        model.addAttribute("movimiento", m);
        return "movimientos-form"; 
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("movimiento") Movimiento movimiento,
                            BindingResult result,
                            HttpSession session,
                            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {

        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";

        if (result.hasErrors()) {
            return "movimientos-form";
        }

        Movimiento existente = movimientoService.buscarPorId(id);
        if (existente == null || !existente.getUsuario().getId().equals(u.getId())) {
            redirectAttrs.addFlashAttribute("error", "No se encontró el movimiento.");
            return "redirect:/movimientos";
        }

        if (movimiento.getMonto() == null || movimiento.getMonto().compareTo(new BigDecimal("0.00")) <= 0) {
            redirectAttrs.addFlashAttribute("error", "El monto debe ser mayor a 0.");
            return "redirect:/movimientos/" + id + "/editar";
        }
        if (movimiento.getFecha() == null) {
            redirectAttrs.addFlashAttribute("error", "La fecha es obligatoria.");
            return "redirect:/movimientos/" + id + "/editar";
        }

        existente.setTipo(movimiento.getTipo());
        existente.setCategoria(movimiento.getCategoria());
        existente.setMonto(movimiento.getMonto());
        existente.setDescripcion(movimiento.getDescripcion());
        existente.setFecha(movimiento.getFecha());

        try {
            movimientoService.guardar(existente);
            redirectAttrs.addFlashAttribute("success", "Movimiento actualizado.");
        } catch (Exception e) {
            log.error("Error al actualizar movimiento {}: {}", id, e.getMessage(), e);
            redirectAttrs.addFlashAttribute("error", "No se pudo actualizar el movimiento.");
        }
        return "redirect:/movimientos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id,
                           HttpSession session,
                           org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {
        log.info("POST /movimientos/{}/eliminar recibido", id);

        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) {
            log.warn("Sesión nula al eliminar movimiento. Redirigiendo a /login");
            return "redirect:/login";
        }

        try {
            movimientoService.eliminar(id);
            log.info("Movimiento {} eliminado correctamente", id);
            redirectAttrs.addFlashAttribute("success", "Movimiento eliminado.");
        } catch (Exception e) {
            log.error("Error eliminando movimiento {}: {}", id, e.getMessage(), e);
            redirectAttrs.addFlashAttribute("error", "No se pudo eliminar el movimiento.");
        }
        return "redirect:/movimientos";
    }

}

