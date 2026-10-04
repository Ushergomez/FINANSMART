package com.finansmart.controller;

import com.finansmart.model.Usuario;
import com.finansmart.service.UsuarioService;
import com.finansmart.service.MovimientoService;
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
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private MovimientoService movimientoService;

    @Autowired
    private MetaService metaService;

    
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    
    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    
    @PostMapping("/login")
    public String procesarLogin(
            @RequestParam String usernameOrEmail,
            @RequestParam String contrasena,
            HttpSession session,
            Model model) {

        try {
            Usuario usuario = usuarioService.login(usernameOrEmail, contrasena);
            session.setAttribute("usuarioLogueado", usuario);
            return "redirect:/dashboard";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

   
    @PostMapping("/registro")
    public String procesarRegistro(
            @Valid @ModelAttribute("usuario") Usuario usuario,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "registro";
        }

        try {
            usuarioService.registrarUsuario(usuario);
            model.addAttribute("mensaje", "Registro exitoso. Ahora puedes iniciar sesión");
            return "redirect:/login?registro=exitoso";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        }
    }

    
    @GetMapping("/dashboard")
    public String mostrarDashboard(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

       
        BigDecimal ingresos = movimientoService.totalIngresosMes(usuario);
        BigDecimal gastos = movimientoService.totalGastosMes(usuario);
        BigDecimal saldo = ingresos.subtract(gastos);

        model.addAttribute("usuario", usuario);
        model.addAttribute("ingresosMes", ingresos);
        model.addAttribute("gastosMes", gastos);
        model.addAttribute("saldoMes", saldo);

       
        long metasActivas = metaService.contarActivas(usuario);
        model.addAttribute("metasActivas", metasActivas);

        List<com.finansmart.model.Meta> topMetas = metaService.topPorAvance(usuario);
        if (topMetas.size() > 3) topMetas = topMetas.subList(0, 3);
        model.addAttribute("topMetas", topMetas);

        return "dashboard";
    }

    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=exitoso";
    }
}
