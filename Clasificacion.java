import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Hilo 3: despues de cada jornada actualiza la tabla de posiciones.
 *
 * Es el que une a los demas: espera los goles de L y V, calcula la tabla,
 * les da permiso para seguir con la jornada siguiente y le pasa una copia
 * de la tabla a Impresion.
 */
public class Clasificacion extends Thread {

    /** Desempate: puntos, diferencia de gol, goles a favor y por ultimo el nombre. */
    private static final Comparator<Equipo> ORDEN_TABLA =
            Comparator.comparingInt(Equipo::getPuntos).reversed()
                      .thenComparing(Comparator.comparingInt(Equipo::getDiferencia).reversed())
                      .thenComparing(Comparator.comparingInt(Equipo::getGf).reversed())
                      .thenComparing(Equipo::getNombre);

    private final Liga liga;
    private final Equipo[] equipos;   // mismo indice que en el fixture
    private final List<Equipo> tabla; // la tabla real, solo la toca este hilo

    public Clasificacion(Liga liga) {
        super("Clasificacion");
        this.liga = liga;
        this.equipos = new Equipo[Liga.NOMBRES_EQUIPOS.length];
        this.tabla = new ArrayList<>();
        for (int i = 0; i < equipos.length; i++) {
            equipos[i] = new Equipo(Liga.NOMBRES_EQUIPOS[i]);
            tabla.add(equipos[i]);
        }
    }

    @Override
    public void run() {
        try {
            for (int j = 1; j <= liga.cantidadJornadas; j++) {
                // Espero que terminen los dos generadores (uno avisa por cada release)
                liga.golesListos.acquire();
                liga.golesListos.acquire();

                // Leo los goles de la jornada y actualizo a cada equipo
                List<int[]> partidos = liga.fixture.get(j - 1);
                for (int p = 0; p < partidos.size(); p++) {
                    Equipo local = equipos[partidos.get(p)[0]];
                    Equipo visitante = equipos[partidos.get(p)[1]];
                    int gl = liga.golesLocal[p];
                    int gv = liga.golesVisitante[p];
                    local.registrarResultado(gl, gv);
                    visitante.registrarResultado(gv, gl);
                }
                tabla.sort(ORDEN_TABLA);
                Liga.demorar(10, 60);
                Liga.log("jornada " + j + ": tabla calculada");

                // Ya lei los goles, los generadores pueden pisarlos con la jornada siguiente
                liga.puedeSimularLocal.release();
                liga.puedeSimularVisitante.release();

                // Hago la copia antes de esperar la bandeja, asi no pierdo tiempo
                List<Equipo> foto = new ArrayList<>();
                for (Equipo e : tabla) {
                    foto.add(new Equipo(e));
                }

                // Espero que Impresion haya retirado la foto anterior, la dejo y aviso
                liga.hayLugar.acquire();
                liga.depositarFoto(foto, j);
                liga.hayFoto.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
