package com.finansmart.controller;

import jakarta.servlet.http.HttpSession;
import com.finansmart.model.Usuario;
import com.finansmart.service.MovimientoService;
import com.finansmart.service.MetaService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private MovimientoService movimientoService;

    @Autowired
    private MetaService metaService;

    @GetMapping
    public String verReportes(
        Model model,
        HttpSession session,
        @RequestParam(value = "inicio", required = false) String inicioStr,
        @RequestParam(value = "fin", required = false) String finStr
    ) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) {
            return "redirect:/login";
        }

        
        LocalDate inicio, fin;
        YearMonth ym = YearMonth.now();
        if (inicioStr != null && !inicioStr.isEmpty() && finStr != null && !finStr.isEmpty()) {
            inicio = LocalDate.parse(inicioStr);
            fin = LocalDate.parse(finStr);
        } else {
            inicio = ym.atDay(1);
            fin = ym.atEndOfMonth();
        }
        model.addAttribute("fechaInicio", inicio.toString());
        model.addAttribute("fechaFin", fin.toString());

        List<Object[]> totalesPorMes = movimientoService.obtenerIngresosYGastosPorMes(u, inicio, fin);
        List<String> meses = new ArrayList<>();
        List<BigDecimal> ingresos = new ArrayList<>();
        List<BigDecimal> gastos = new ArrayList<>();
        for (Object[] fila : totalesPorMes) {
            meses.add((String) fila[0]);
            ingresos.add((BigDecimal) fila[1]);
            gastos.add((BigDecimal) fila[2]);
        }
        model.addAttribute("meses", meses);
        model.addAttribute("ingresos", ingresos);
        model.addAttribute("gastos", gastos);

        List<Object[]> gastosPorCategoria = movimientoService.gastosPorCategoriaEnMes(u, inicio, fin);
        List<String> categorias = new ArrayList<>();
        List<BigDecimal> montosCategoria = new ArrayList<>();
        for (Object[] fila : gastosPorCategoria) {
            categorias.add((String) fila[0]);
            montosCategoria.add((BigDecimal) fila[1]);
        }
        model.addAttribute("catLabels", categorias);
        model.addAttribute("catValues", montosCategoria);

        List<Object[]> metas = metaService.progresoPorMeta(u.getId());
        List<String> metaNombres = new ArrayList<>();
        List<BigDecimal> progreso = new ArrayList<>();

        for (Object[] row : metas) {
            metaNombres.add((String) row[0]);
            BigDecimal actual = (BigDecimal) row[1];
            BigDecimal meta = (BigDecimal) row[2];
            progreso.add(meta.compareTo(BigDecimal.ZERO) > 0
                ? actual.divide(meta, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO);
        }
        model.addAttribute("metaNombres", metaNombres);
        model.addAttribute("progreso", progreso);

        model.addAttribute("usuario", u);
        return "reportes";
    }
}
