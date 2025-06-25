package udea.validador_festivos.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import udea.validador_festivos.entity.Festivo;
import udea.validador_festivos.repository.FestivoRepository;

@Service
public class FestivoService {

    @Autowired
    private FestivoRepository festivoRepository;

    /**
     * Verifica si una fecha es festiva (incluye el cálculo de Semana Santa).
     *
     * @param fecha La fecha a verificar.
     * @return Nombre del festivo o mensaje indicando que no es festivo.
     */
    public String verificarFechaFestiva(LocalDate fecha) {
        // Buscar festivos en la base de datos
        List<Festivo> festivos = festivoRepository.findByDiaAndMes(fecha.getDayOfMonth(), fecha.getMonthValue());
    
        if (!festivos.isEmpty()) {
            return "Es Festivo: " + festivos.get(0).getNombre();
        }

        // Si no está en la base, verificar si corresponde a Semana Santa
        if (esSemanaSanta(fecha)) {
            return "Es Festivo: Semana Santa";
        }

        return "No es Festivo";
    }
    
        /**
     * Calcula si una fecha es parte de la Semana Santa.
     *
     * @param fecha La fecha a verificar.
     * @return Verdadero si es Jueves Santo o Viernes Santo.
     */
    private boolean esSemanaSanta(LocalDate fecha) {
        int year = fecha.getYear();
        LocalDate domingoDePascua = calcularDomingoDePascua(year);

        LocalDate juevesSanto = domingoDePascua.minusDays(3);
        LocalDate viernesSanto = domingoDePascua.minusDays(2);

        return fecha.equals(juevesSanto) || fecha.equals(viernesSanto);
    }

    /**
     * Calcula el Domingo de Pascua para un año específico.
     *
     * @param year Año para el cual se calcula.
     * @return Fecha del Domingo de Pascua.
     */
    public LocalDate calcularDomingoDePascua(int year) {
        int a = year % 19;
        int b = year % 4;
        int c = year % 7;
        int d = (19 * a + 24) % 30;
        int e = (2 * b + 4 * c + 6 * d + 5) % 7;

        int diasDesdeMarzo15 = d + e;
        return LocalDate.of(year, 3, 15).plusDays(diasDesdeMarzo15);
    }

    public List<Festivo> obtenerFestivosPorAño(int year) {
        // 1. Calcula el Domingo de Pascua para el año dado
        LocalDate domingoDePascua = calcularDomingoDePascua(year);

        // 2. Obtiene los festivos almacenados en la base de datos
        List<Festivo> festivosDeBaseDeDatos = new ArrayList<>(festivoRepository.findAll());

        // 3. Mapea los valores calculados basados en Pascua
        Map<String, Festivo> festivosCalculados = Map.of(
                "Jueves Santo", new Festivo(
                        "Jueves Santo",
                        domingoDePascua.minusDays(3).getDayOfMonth(),
                        domingoDePascua.minusDays(3).getMonthValue(),
                        -3,
                        3
                ),
                "Viernes Santo", new Festivo(
                        "Viernes Santo",
                        domingoDePascua.minusDays(2).getDayOfMonth(),
                        domingoDePascua.minusDays(2).getMonthValue(),
                        -2,
                        3
                ),
                "Domingo de Pascua", new Festivo(
                        "Domingo de Pascua",
                        domingoDePascua.getDayOfMonth(),
                        domingoDePascua.getMonthValue(),
                        0,
                        3
                ),
                "Corpus Christi", new Festivo(
                        "Corpus Christi",
                        domingoDePascua.plusDays(60).getDayOfMonth(),
                        domingoDePascua.plusDays(60).getMonthValue(),
                        60,
                        4
                ),
                "Sagrado Corazón de Jesús", new Festivo(
                        "Sagrado Corazón de Jesús",
                        domingoDePascua.plusDays(68).getDayOfMonth(),
                        domingoDePascua.plusDays(68).getMonthValue(),
                        68,
                        4
                ),
                "Ascensión del Señor", new Festivo(
                        "Ascensión del Señor",
                        domingoDePascua.plusDays(40).getDayOfMonth(),
                        domingoDePascua.plusDays(40).getMonthValue(),
                        40,
                        4
                )
        );

        // 4. Reemplaza los valores en la lista original
        for (Festivo festivo : festivosDeBaseDeDatos) {
            if (festivosCalculados.containsKey(festivo.getNombre())) {
                Festivo festivoCalculado = festivosCalculados.get(festivo.getNombre());
                festivo.setDia(festivoCalculado.getDia());
                festivo.setMes(festivoCalculado.getMes());
            }
        }

        // 5. Retorna la lista actualizada
        return festivosDeBaseDeDatos;
    }
}

