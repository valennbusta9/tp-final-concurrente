import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Lo que comparten los 4 hilos y los semaforos que los coordinan.
 *
 * Datos compartidos:
 *  - golesLocal[] y golesVisitante[]: los goles de la jornada en curso. Cada
 *      generador escribe solo en el suyo, y Clasificacion los lee. Se reusan
 *      en cada jornada.
 *  - La bandeja (fotoTabla + jornadaFoto): buffer de tamano 1 entre
 *      Clasificacion (productor) e Impresion (consumidor).
 *
 * No hace falta synchronized ni volatile: lo que se escribe antes de un release()
 * lo ve el hilo que despues hace el acquire() (happens-before de Semaphore).
 */
public class Liga {

    public static final String[] NOMBRES_EQUIPOS = {
        "Boca Juniors", "River Plate", "Rosario Central", "Argentinos Juniors",
        "Velez Sarsfield", "Lanus", "Racing Club", "Independiente",
        "Huracan", "Estudiantes (LP)", "Independiente Rivadavia", "Barracas Central",
        "Talleres (C)", "Gimnasia (LP)", "Defensa y Justicia", "Instituto",
        "Union", "San Lorenzo", "Belgrano", "Gimnasia (M)"
    };

    // Datos de solo lectura: se arman antes de lanzar los hilos
    public final List<List<int[]>> fixture;
    public final int cantidadJornadas;
    public final int partidosPorJornada;

    // Goles de la jornada en curso
    public final int[] golesLocal;      // lo escribe solo el generador local
    public final int[] golesVisitante;  // lo escribe solo el generador visitante

    // La bandeja entre Clasificacion e Impresion
    private List<Equipo> fotoTabla;
    private int jornadaFoto;

    // ---------- Semaforos ----------
    /** Los generadores avisan a Clasificacion que terminaron (arranca en 0). */
    public final Semaphore golesListos = new Semaphore(0);

    /** Clasificacion habilita la jornada siguiente. Uno por generador (arrancan en 1). */
    public final Semaphore puedeSimularLocal = new Semaphore(1);
    public final Semaphore puedeSimularVisitante = new Semaphore(1);

    /** Hay lugar en la bandeja. Arranca en 1 porque empieza vacia. */
    public final Semaphore hayLugar = new Semaphore(1);

    /** Hay una tabla lista para imprimir (arranca en 0). */
    public final Semaphore hayFoto = new Semaphore(0);

    public Liga() {
        this.fixture = Fixture.generar(NOMBRES_EQUIPOS.length);
        this.cantidadJornadas = fixture.size();
        this.partidosPorJornada = NOMBRES_EQUIPOS.length / 2;
        this.golesLocal = new int[partidosPorJornada];
        this.golesVisitante = new int[partidosPorJornada];
    }

    // Estos metodos se llaman siempre con el semaforo ya tomado,
    // asi que no necesitan exclusion mutua extra.

    /** Lo usa Clasificacion despues de hayLugar.acquire(). */
    public void depositarFoto(List<Equipo> foto, int jornada) {
        this.fotoTabla = foto;
        this.jornadaFoto = jornada;
    }

    /** Lo usa Impresion despues de hayFoto.acquire(). */
    public List<Equipo> retirarFoto() {
        List<Equipo> foto = fotoTabla;
        fotoTabla = null;
        return foto;
    }

    public int getJornadaFoto() {
        return jornadaFoto;
    }

    /** Imprime un mensaje con el nombre del hilo, para ver como se intercalan. */
    public static void log(String mensaje) {
        System.out.printf("[%-14s] %s%n", Thread.currentThread().getName(), mensaje);
    }

    /** Pausa aleatoria para simular trabajo. */
    public static void demorar(int minMs, int maxMs) throws InterruptedException {
        Thread.sleep(java.util.concurrent.ThreadLocalRandom.current().nextInt(minMs, maxMs + 1));
    }
}
