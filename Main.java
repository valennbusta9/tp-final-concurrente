/**
 * TP Programacion Concurrente - Ejercicio 9.7: Simulacion de una liga de futbol
 * con 4 procesos ligeros sincronizados mediante semaforos.
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        Liga liga = new Liga();

        System.out.printf("Liga de %d equipos: %d jornadas de %d partidos.%n",
                Liga.NOMBRES_EQUIPOS.length, liga.cantidadJornadas, liga.partidosPorJornada);

        Thread golesLocal = new GeneradorGoles(
                "Goles Local", liga, liga.golesLocal, liga.puedeSimularLocal);
        Thread golesVisitante = new GeneradorGoles(
                "Goles Visitante", liga, liga.golesVisitante, liga.puedeSimularVisitante);
        Thread clasificacion = new Clasificacion(liga);
        Thread impresion = new Impresion(liga);

        // Lanzo los 4 hilos. El orden de start() da igual, los semaforos ordenan todo.
        impresion.start();
        clasificacion.start();
        golesVisitante.start();
        golesLocal.start();

        // Espero a que terminen los 4
        golesLocal.join();
        golesVisitante.join();
        clasificacion.join();
        impresion.join();

        System.out.println("\nFin del campeonato.");
    }
}
