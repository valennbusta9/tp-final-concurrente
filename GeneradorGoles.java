import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Hilos 1 y 2: generan los goles de los locales o de los visitantes.
 *
 * Es la misma clase para los dos. Lo unico que cambia es el arreglo donde
 * escribe cada uno y el semaforo que lo habilita.
 */
public class GeneradorGoles extends Thread {

    private final Liga liga;
    private final int[] goles;              // golesLocal o golesVisitante
    private final Semaphore puedeSimular;   // puedeSimularLocal o puedeSimularVisitante

    public GeneradorGoles(String nombre, Liga liga, int[] goles, Semaphore puedeSimular) {
        super(nombre);
        this.liga = liga;
        this.goles = goles;
        this.puedeSimular = puedeSimular;
    }

    @Override
    public void run() {
        try {
            for (int j = 1; j <= liga.cantidadJornadas; j++) {
                // No arranco la jornada j hasta que Clasificacion termine la anterior
                puedeSimular.acquire();

                // Cargo los goles en mi arreglo (el otro generador usa el suyo)
                for (int p = 0; p < liga.partidosPorJornada; p++) {
                    goles[p] = ThreadLocalRandom.current().nextInt(0, 6); // de 0 a 5 goles
                }
                Liga.demorar(20, 120); // para que se note la concurrencia
                Liga.log("jornada " + j + ": goles cargados");

                // Le aviso a Clasificacion que ya termine
                liga.golesListos.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
