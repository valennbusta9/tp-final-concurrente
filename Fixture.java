import java.util.ArrayList;
import java.util.List;

/**
 * Arma el fixture de todos contra todos, a una sola rueda (metodo del circulo):
 * el primer equipo queda fijo y los demas van rotando una posicion por jornada.
 * Con N equipos (N par) salen N-1 jornadas de N/2 partidos.
 *
 * Se arma antes de lanzar los hilos y despues solo se lee, asi que no necesita sincronizacion.
 */
public class Fixture {

    /**
     * @return una lista de jornadas; cada jornada es una lista de partidos
     *         y cada partido es un par {indiceLocal, indiceVisitante}.
     */
    public static List<List<int[]>> generar(int cantidadEquipos) {
        if (cantidadEquipos % 2 != 0) {
            throw new IllegalArgumentException("La cantidad de equipos debe ser par");
        }

        int[] rueda = new int[cantidadEquipos];
        for (int i = 0; i < cantidadEquipos; i++) {
            rueda[i] = i;
        }

        List<List<int[]>> jornadas = new ArrayList<>();
        int mitad = cantidadEquipos / 2;

        for (int j = 0; j < cantidadEquipos - 1; j++) {
            List<int[]> partidos = new ArrayList<>();
            for (int i = 0; i < mitad; i++) {
                int a = rueda[i];
                int b = rueda[cantidadEquipos - 1 - i];
                // Cambio quien es local en cada jornada para repartir la localia
                if (j % 2 == 0) {
                    partidos.add(new int[]{a, b});
                } else {
                    partidos.add(new int[]{b, a});
                }
            }
            jornadas.add(partidos);

            // Roto: el de la posicion 0 no se mueve, los demas avanzan un lugar
            int ultimo = rueda[cantidadEquipos - 1];
            for (int k = cantidadEquipos - 1; k > 1; k--) {
                rueda[k] = rueda[k - 1];
            }
            rueda[1] = ultimo;
        }
        return jornadas;
    }
}
