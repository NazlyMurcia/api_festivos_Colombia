package udea.validador_festivos.service;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import udea.validador_festivos.entity.Festivo;
import udea.validador_festivos.repository.FestivoRepository;

@Service
public class FestivoService {

    @Autowired
    private FestivoRepository festivoRepository;

    /**
     * Verifica si una fecha es festiva (incluye el cálculo de todos los tipos de festivos).
     *
     * @param fecha La fecha a verificar.
     * @return "Es festivo" si la fecha es un festivo, "No es festivo" en caso contrario.
     */

    public String verificarFechaFestiva(LocalDate fecha) {
        if (fecha == null) {
            return "Fecha no válida";
        }
        try {
            int year = fecha.getYear();
            List<Festivo> festivosAnualesCalculados = obtenerFestivosPorAño(year);

            for (Festivo festivo : festivosAnualesCalculados) {
                LocalDate fechaDelFestivoCalculado = LocalDate.of(year, festivo.getMes(),festivo.getDia());
                if (fecha.equals(fechaDelFestivoCalculado)) {
                    return "Es festivo";
                }
            }
            return "No es festivo";
            
        } catch (DateTimeException e){
            System.err.println("Error al procesar la fecha: " + fecha + "-" + e.getMessage());
            return "Fecha no válida";
        }
    }

    /**
     * Obtiene una lista de todos los festivos para un año específico, con sus fechas calculadas.
     *
     * @param year Año para el cual se listarán los festivos.
     * @return Lista de objetos Festivo con sus fechas calculadas para el año.
     */

    public List<Festivo> obtenerFestivosPorAño(int year) {
        List<Festivo> festivosCalculadosParaElAño = new ArrayList<>();
        List<Festivo> reglasFestivos = festivoRepository.findAll();
        
        for (Festivo regla : reglasFestivos) {
            LocalDate fechaCalculada = calcularFechaFestivo(regla, year);
            // Creamos un nuevo objeto Festivo para representar la fecha calculada para ese año
            Festivo festivoConFecha = new Festivo();

            if (regla.getId() != null) {
                festivoConFecha.setId(regla.getId());
            }

            festivoConFecha.setNombre(regla.getNombre());
            festivoConFecha.setDia(fechaCalculada.getDayOfMonth());
            festivoConFecha.setMes(fechaCalculada.getMonthValue());
            festivoConFecha.setDiaspascua(regla.getDiaspascua());
            festivoConFecha.setIdtipo(regla.getIdtipo());
            
            festivosCalculadosParaElAño.add(festivoConFecha);
        }
        return festivosCalculadosParaElAño;
    }
    /**
     * Calcula la fecha exacta de un festivo para un año dado, basándose en su tipo.
     *
     * @param festivo Objeto Festivo que contiene las reglas de cálculo (días, mes, idTipo, días desde Pascua).
     * @param year Año para el cual se realizará el cálculo.
     * @return La fecha exacta del festivo.
     * @throws IllegalArgumentException si el tipo de festivo no es reconocido.
     */

    private LocalDate calcularFechaFestivo(Festivo festivo, int year) {
        LocalDate fechaResultante;

        switch (festivo.getIdtipo()) {
            case 1: // Tipo 1: Fijo (ej. Año Nuevo, Independencia de Colombia)
                fechaResultante = LocalDate.of(year, festivo.getMes(), festivo.getDia());
                break;
            case 2: // Tipo 2: Ley de "Puente festivo" (se mueve al siguiente lunes si no cae en lunes)
                fechaResultante = LocalDate.of(year, festivo.getMes(), festivo.getDia());
                fechaResultante = moverAlSiguienteLunes(fechaResultante);
                break;
            case 3: // Tipo 3: Basado en el Domingo de Pascua (ej. Jueves Santo, Viernes Santo, Domingo de Pascua)
                LocalDate domingoPascua = calcularDomingoPascua(year);
                fechaResultante = domingoPascua.plusDays(festivo.getDiaspascua());
                break;
            case 4: // Tipo 4: Basado en el Domingo de Pascua y Ley de "Puente festivo" (ej. Ascensión del Señor, Corpus Christi, Sagrado Corazón de Jesús)
                LocalDate domingoPascuaPuente = calcularDomingoPascua(year);
                fechaResultante = domingoPascuaPuente.plusDays(festivo.getDiaspascua());
                fechaResultante = moverAlSiguienteLunes(fechaResultante);
                break;
            default:
                throw new IllegalArgumentException("Tipo de festivo no reconocido en la base de datos: " + festivo.getIdtipo());
        }
        return fechaResultante;
    }

    /**
     * Mueve una fecha dada al siguiente lunes si no es ya lunes.
     * @param fecha La fecha a evaluar.
     * @return La fecha del siguiente lunes.
     */
    private LocalDate moverAlSiguienteLunes(LocalDate fecha) {
        while (fecha.getDayOfWeek() != DayOfWeek.MONDAY) {
            fecha = fecha.plusDays(1);
        }
        return fecha;
    }
    
    /**
     * Calcula la fecha del Domingo de Pascua para un año específico usando la fórmula del PDF.
     * @param year Año para el cual calcular el Domingo de Pascua.
     * @return La fecha del Domingo de Pascua.
     */
    public LocalDate calcularDomingoPascua(int year) {
        int a = year % 19;
        int b = year % 4;
        int c = year % 7;
        int d = (19 * a + 24) % 30;
        int e = (2 * b + 4 * c + 6 * d + 5) % 7;

        int diasParaDomingoRamosDesdeMarzo15 = d + e;
        LocalDate domingoRamos = LocalDate.of(year, 3, 15).plusDays(diasParaDomingoRamosDesdeMarzo15);
        return domingoRamos.plusDays(7);
    }
}

