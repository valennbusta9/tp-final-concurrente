# TP Final - Programación Concurrente

Simulación de una liga de fútbol con 4 hilos coordinados con semáforos (ejercicio 9.7).

## El problema

Tenemos una liga de 20 equipos que juegan todos contra todos, una sola vuelta: son 19 jornadas de 10 partidos cada una. Hay que simular el campeonato con cuatro hilos que trabajan a la vez:

1. **Goles Local**: sortea los goles que hace el local en cada partido de la jornada.
2. **Goles Visitante**: lo mismo, pero para los visitantes.
3. **Clasificación**: con los goles de la jornada, actualiza la tabla de posiciones.
4. **Impresión**: muestra la tabla en pantalla.

Lo difícil es que ninguno puede ir por su cuenta:

- La tabla no se puede calcular hasta que estén los goles de los dos.
- Los generadores no pueden empezar la jornada siguiente hasta que la tabla haya leído los goles de la anterior, porque si no los pisarían.
- Impresión no puede imprimir una tabla que todavía no está lista.
- Clasificación no puede dejar una tabla nueva si Impresión todavía no retiró la anterior.

## Cómo lo resolvimos

La idea es que cada hilo espere con un semáforo lo que necesita y avise con otro cuando termina. Usamos cinco, todos en [Liga.java](Liga.java):

| Semáforo | Arranca en | Para qué sirve |
|---|---|---|
| `golesListos` | 0 | Los dos generadores avisan a Clasificación que terminaron. Ella espera dos avisos. |
| `puedeSimularLocal` | 1 | Clasificación le da permiso al generador local para la jornada siguiente. |
| `puedeSimularVisitante` | 1 | Lo mismo, para el generador visitante. |
| `hayFoto` | 0 | Clasificación avisa que dejó una tabla lista para imprimir. |
| `hayLugar` | 1 | Impresión avisa que ya retiró la tabla y la bandeja está libre. |

Algunas decisiones que tomamos:

- **Cada generador escribe solo en su arreglo.** El local usa `golesLocal` y el visitante `golesVisitante`, así nunca se pisan entre ellos.
- **Un semáforo por generador para habilitar la jornada siguiente.** Los dos esperan lo mismo, pero cada uno tiene el suyo para que uno no se lleve el permiso del otro.
- **Entre Clasificación e Impresión hay una "bandeja" de un solo lugar.** Es el clásico productor-consumidor con buffer de tamaño 1.
- **Impresión trabaja con una copia de la tabla.** Clasificación le deja una foto y sigue calculando la jornada siguiente. Así mientras una imprime, la otra ya avanza, y nunca se lee una tabla a medio modificar.
- **No usamos `synchronized` ni `volatile`.** Los semáforos de Java ya garantizan que lo que se escribe antes de un `release()` lo ve el hilo que después hace el `acquire()`.
- **El orden de `start()` no importa.** Los semáforos son los que ordenan todo, no el orden en que lanzamos los hilos.

## Qué se ve al ejecutarlo

- Los mensajes de los hilos intercalados, con las pausas aleatorias que usamos para que se note la concurrencia.
- La tabla de cada jornada, con `(C)` para los primeros 4 puestos (copas) y `(D)` para los últimos 3 (descenso).
- Al final, el campeón y el subcampeón.

Como los goles son al azar, el campeón cambia en cada ejecución.

## Cómo correrlo

Desde la carpeta del proyecto:

```bash
javac *.java
java Main
```

## Archivos

- [Main.java](Main.java): crea la liga, lanza los 4 hilos y espera a que terminen.
- [Liga.java](Liga.java): lo que comparten los hilos y los semáforos.
- [GeneradorGoles.java](GeneradorGoles.java): hilos 1 y 2 (es la misma clase para local y visitante).
- [Clasificacion.java](Clasificacion.java): hilo 3.
- [Impresion.java](Impresion.java): hilo 4.
- [Equipo.java](Equipo.java): estadísticas de cada equipo.
- [Fixture.java](Fixture.java): arma el calendario de partidos.
