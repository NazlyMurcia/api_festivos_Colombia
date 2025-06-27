package udea.validador_festivos.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import udea.validador_festivos.entity.Festivo;
import udea.validador_festivos.service.FestivoService;

@RestController
@RequestMapping("/festivos")
public class FestivoController {

    private static final Logger logger = LoggerFactory.getLogger(FestivoController.class);
    
    @Autowired
    private FestivoService festivoService;
    
    @GetMapping("/verificar/{year}/{month}/{day}")
    public ResponseEntity<String> verificarFecha(
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String day) {
        
        String fechaStr = null;

        try {
            //Asegura que el mes y el día tengan dos dígitos (ej. 2 -> 02)
            String formattedMonth = String.format("%02d", Integer.parseInt(month));
            String formattedDay = String.format("%02d", Integer.parseInt(day));
            
            fechaStr = year + "-" + formattedMonth + "-" + formattedDay;
            logger.info("Intentando parsear fechaStr (formato YYYY-MM-DD): {}", fechaStr);
            
            LocalDate fecha = LocalDate.parse(fechaStr, DateTimeFormatter.ISO_LOCAL_DATE);
            
            logger.info("Fecha parseada exitosamente: {}", fecha);

            String resultado = festivoService.verificarFechaFestiva(fecha);

            return ResponseEntity.ok(resultado);

        } catch (NumberFormatException e) {
            // Captura si 'year', 'month' o 'day' no pueden ser convertidos a número.
            logger.error("NumberFormatException: Año, mes o día no son números válidos. Entrada: {}/{}/{} - Mensaje: {}", year, month, day, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Año, mes o día no son números válidos.");
        } catch (DateTimeParseException e) {
            // Captura si la fecha formateada no es una fecha real (ej. 2025-02-30)
            logger.error("DateTimeParseException capturada para fechaStr: {} - Mensaje: {}", fechaStr, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Fecha no válida");
        } catch (Exception e) {
            // Captura cualquier otra excepción inesperada
            logger.error("Excepción inesperada en verificarFecha para fechaStr: {} - Tipo: {} - Mensaje: {}", fechaStr, e.getClass().getName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @GetMapping("/listar/{year}")
    public ResponseEntity<List<Festivo>> listarFestivosPorAño(@PathVariable int year) {
        List<Festivo> festivos = festivoService.obtenerFestivosPorAño(year);
        return ResponseEntity.ok(festivos);
    }
}