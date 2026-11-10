import java.util.List;

/**
 * Hilo 4: imprime la tabla de cada jornada y, al final, el campeon.
 *
 * Retira la copia de la bandeja y la libera enseguida, asi Clasificacion
 * puede seguir con la jornada siguiente mientras esta imprime.
 */
public class Impresion extends Thread {

    private static final int PUESTOS_COPAS = 4;     // los primeros N van a copas
    private static final int PUESTOS_DESCENSO = 3;  // los ultimos N descienden

    private final Liga liga;

    public Impresion(Liga liga) {
        super("Impresion");
        this.liga = liga;
    }

    @Override
    public void run() {
        try {
            List<Equipo> ultimaFoto = null;
            for (int j = 1; j <= liga.cantidadJornadas; j++) {
                // Espero que haya una tabla en la bandeja y la retiro
                liga.hayFoto.acquire();
                List<Equipo> foto = liga.retirarFoto();
                ultimaFoto = foto;
                int jornada = liga.getJornadaFoto();
                // Bandeja libre otra vez
                liga.hayLugar.release();

                // De aca en adelante la copia es solo mia
                Liga.demorar(50, 150); // imprimo lento a proposito, para ver el paralelismo
                System.out.print(formatear(foto, jornada));
            }

            // La ultima tabla ya viene ordenada: primero es el campeon, segundo el subcampeon
            if (ultimaFoto != null) {
                Equipo campeon = ultimaFoto.get(0);
                Equipo subcampeon = ultimaFoto.get(1);
                System.out.printf("%n*** CAMPEON: %s con %d puntos (DG %+d) ***%n",
                        campeon.getNombre(), campeon.getPuntos(), campeon.getDiferencia());
                System.out.printf("    Subcampeon: %s con %d puntos (DG %+d)%n",
                        subcampeon.getNombre(), subcampeon.getPuntos(), subcampeon.getDiferencia());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Devuelve (C) para copas, (D) para descenso o vacio si esta en la mitad de la tabla. */
    private String marcaZona(int pos, int totalEquipos) {
        if (pos <= PUESTOS_COPAS) {
            return "(C)";
        }
        if (pos > totalEquipos - PUESTOS_DESCENSO) {
            return "(D)";
        }
        return "";
    }

    /** Arma la tabla en un solo String para que no se mezcle con los logs de otros hilos. */
    private String formatear(List<Equipo> tabla, int jornada) {
        StringBuilder sb = new StringBuilder();
        String linea = "-".repeat(72) + "\n";
        sb.append("\n").append(linea);
        sb.append(String.format("  CLASIFICACION - JORNADA %d%n", jornada));
        sb.append(linea);
        sb.append(String.format("%-4s %-24s %3s %3s %3s %3s %4s %4s %4s %4s%n",
                "Pos", "Equipo", "PJ", "G", "E", "P", "GF", "GC", "DG", "Pts"));
        int pos = 1;
        for (Equipo e : tabla) {
            sb.append(String.format("%-4d %-24s %3d %3d %3d %3d %4d %4d %+4d %4d  %s%n",
                    pos, e.getNombre(), e.getPj(), e.getPg(), e.getPe(), e.getPp(),
                    e.getGf(), e.getGc(), e.getDiferencia(), e.getPuntos(),
                    marcaZona(pos, tabla.size())));
            pos++;
        }
        sb.append(linea);
        sb.append(String.format("  (C) Copas: primeros %d    (D) Descenso: ultimos %d%n",
                PUESTOS_COPAS, PUESTOS_DESCENSO));
        return sb.toString();
    }
}
