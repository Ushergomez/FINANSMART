package com.finansmart.controller;

import com.finansmart.model.Meta;
import com.finansmart.model.Usuario;
import com.finansmart.service.MetaService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/metas")
public class MetaController {

    
    @Autowired
    private MetaService metaService;

   
    @GetMapping
    public String listar(Model model, HttpSession session,
                         @RequestParam(required = false, defaultValue = "todas") String filtro,
                         @RequestParam(required = false) java.time.LocalDate hasta) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";

        List<Meta> metas = "activas".equalsIgnoreCase(filtro)
                ? metaService.listarActivas(u)
                : metaService.listarPorUsuario(u);

        if (hasta != null) {
            metas = metas.stream()
                    .filter(m -> m.getFechaLimite() == null || !m.getFechaLimite().isAfter(hasta))
                    .toList();
            model.addAttribute("hasta", hasta);
        }

        long activas = metaService.contarActivas(u);

        model.addAttribute("metas", metas);
        model.addAttribute("filtro", filtro);
        model.addAttribute("activas", activas);
        return "metas-lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model, HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";

        Meta m = new Meta();
        m.setMontoActual(BigDecimal.ZERO);
        model.addAttribute("meta", m);
        return "metas-form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("meta") Meta meta,
                          BindingResult result,
                          HttpSession session,
                          Model model,
                          org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {

        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";

        if (result.hasErrors()) {
            model.addAttribute("error", "Revisa los campos del formulario.");
            return "metas-form";
        }

        if (meta.getMontoObjetivo() == null || meta.getMontoObjetivo().compareTo(new BigDecimal("0.00")) <= 0) {
            model.addAttribute("error", "El monto objetivo debe ser mayor a 0.");
            return "metas-form";
        }
        if (meta.getMontoActual() == null) meta.setMontoActual(BigDecimal.ZERO);

        meta.setUsuario(u);
        meta.setCompletada(meta.getMontoActual().compareTo(meta.getMontoObjetivo()) >= 0);

        try {
            metaService.guardar(meta);
            redirectAttrs.addFlashAttribute("success", "Meta creada correctamente.");
            return "redirect:/metas";
        } catch (Exception e) {
            model.addAttribute("error", "No se pudo guardar la meta.");
            return "metas-form";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, HttpSession session, Model model,
                         org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";

        Meta m = metaService.buscarPorId(id);
        if (m == null || !m.getUsuario().getId().equals(u.getId())) {
            redirectAttrs.addFlashAttribute("error", "Meta no encontrada.");
            return "redirect:/metas";
        }

        model.addAttribute("meta", m);
        return "metas-form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("meta") Meta metaForm,
                             BindingResult result,
                             HttpSession session,
                             org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs,
                             Model model) {

        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";

        if (result.hasErrors()) {
            model.addAttribute("error", "Revisa los campos del formulario.");
            return "metas-form";
        }

        Meta existente = metaService.buscarPorId(id);
        if (existente == null || !existente.getUsuario().getId().equals(u.getId())) {
            redirectAttrs.addFlashAttribute("error", "Meta no encontrada.");
            return "redirect:/metas";
        }

        if (metaForm.getMontoObjetivo() == null || metaForm.getMontoObjetivo().compareTo(new BigDecimal("0.00")) <= 0) {
            redirectAttrs.addFlashAttribute("error", "El monto objetivo debe ser mayor a 0.");
            return "redirect:/metas/" + id + "/editar";
        }

        try {
            existente.setNombre(metaForm.getNombre());
            existente.setMontoObjetivo(metaForm.getMontoObjetivo());
            existente.setMontoActual(metaForm.getMontoActual());
            existente.setFechaLimite(metaForm.getFechaLimite());
            existente.setCompletada(metaForm.getCompletada() != null ? metaForm.getCompletada() : existente.getCompletada());

            metaService.guardar(existente);
            redirectAttrs.addFlashAttribute("success", "Meta actualizada.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "No se pudo actualizar la meta.");
        }
        return "redirect:/metas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, HttpSession session,
                           org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";

        Meta m = metaService.buscarPorId(id);
        if (m == null || !m.getUsuario().getId().equals(u.getId())) {
            redirectAttrs.addFlashAttribute("error", "Meta no encontrada.");
            return "redirect:/metas";
        }

        try {
            metaService.eliminar(id);
            redirectAttrs.addFlashAttribute("success", "Meta eliminada.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "No se pudo eliminar la meta.");
        }
        return "redirect:/metas";
    }

    @PostMapping("/{id}/aporte")
    public String aportar(@PathVariable Long id,
                          @RequestParam BigDecimal monto,
                          HttpSession session,
                          org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";

        Meta m = metaService.buscarPorId(id);
        if (m == null || !m.getUsuario().getId().equals(u.getId())) {
            redirectAttrs.addFlashAttribute("error", "Meta no encontrada.");
            return "redirect:/metas";
        }

        try {
            metaService.aportar(m, monto);
            redirectAttrs.addFlashAttribute("success", "Aporte registrado.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "No se pudo registrar el aporte.");
        }
        return "redirect:/metas";
    }

    @PostMapping("/{id}/completar")
    public String completar(@PathVariable Long id,
                            HttpSession session,
                            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";

        Meta m = metaService.buscarPorId(id);
        if (m == null || !m.getUsuario().getId().equals(u.getId())) {
            redirectAttrs.addFlashAttribute("error", "Meta no encontrada.");
            return "redirect:/metas";
        }

        try {
            metaService.completar(m);
            redirectAttrs.addFlashAttribute("success", "Meta marcada como completada.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "No se pudo completar la meta.");
        }
        return "redirect:/metas";
    }
}
