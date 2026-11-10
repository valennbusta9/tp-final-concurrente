/**
 * Un equipo con sus estadisticas de la tabla.
 * Solo lo modifica el hilo Clasificacion. Impresion trabaja siempre con una copia.
 */
public class Equipo {

    private final String nombre;
    private int pj, pg, pe, pp; // jugados, ganados, empatados, perdidos
    private int gf, gc;         // goles a favor y en contra
    private int puntos;

    public Equipo(String nombre) {
        this.nombre = nombre;
    }

    /** Constructor de copia: lo uso para armar la foto de la tabla que va a la bandeja. */
    public Equipo(Equipo otro) {
        this.nombre = otro.nombre;
        this.pj = otro.pj;
        this.pg = otro.pg;
        this.pe = otro.pe;
        this.pp = otro.pp;
        this.gf = otro.gf;
        this.gc = otro.gc;
        this.puntos = otro.puntos;
    }

    /** Suma un partido jugado, mirando los goles desde este equipo. Gana 3 puntos, empata 1. */
    public void registrarResultado(int golesPropios, int golesRival) {
        pj++;
        gf += golesPropios;
        gc += golesRival;
        if (golesPropios > golesRival) {
            pg++;
            puntos += 3;
        } else if (golesPropios == golesRival) {
            pe++;
            puntos += 1;
        } else {
            pp++;
        }
    }

    public String getNombre()     { return nombre; }
    public int getPj()            { return pj; }
    public int getPg()            { return pg; }
    public int getPe()            { return pe; }
    public int getPp()            { return pp; }
    public int getGf()            { return gf; }
    public int getGc()            { return gc; }
    public int getDiferencia()    { return gf - gc; }
    public int getPuntos()        { return puntos; }
}
