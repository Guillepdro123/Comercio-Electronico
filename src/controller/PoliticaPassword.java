package controller;

/**
 * Reglas de la contraseña: qué se acepta y qué tan fuerte es lo que se
 * escribió.
 *
 * <p><b>Un solo sitio para las dos preguntas.</b> El formulario necesita
 * saber dos cosas distintas sobre la misma contraseña: si es válida (para
 * aceptar o rechazar el registro) y qué tan segura es (para el indicador de
 * color que va cambiando mientras se escribe). Ambas salen de aquí, así que
 * no pueden contradecirse: lo que el punto muestra en rojo es exactamente lo
 * que la validación va a rechazar.</p>
 *
 * <p><b>Sin Swing a propósito</b>, igual que {@link ControlIntentosFallidos}:
 * esta clase decide, no pinta. Quien traduce el nivel a un color es la vista.</p>
 *
 * <p><b>Mínimo sí, máximo no.</b> Se exige un piso de longitud y variedad de
 * caracteres, pero ninguna longitud máxima: un tope corto empuja a la gente
 * hacia contraseñas peores sin aportar seguridad.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.0
 */
public class PoliticaPassword {

    /** Longitud mínima exigida. */
    public static final int LONGITUD_MINIMA = 7;

    /** Longitudes que suman punto de complejidad. */
    private static final int LONGITUD_HOLGADA = 10;
    private static final int LONGITUD_LARGA = 14;

    /** A partir de esta cantidad de caracteres distintos, la contraseña es variada. */
    private static final int CARACTERES_DISTINTOS_RICOS = 10;

    /** Puntos necesarios para considerarla segura (el máximo es 5). */
    private static final int PUNTOS_PARA_FUERTE = 4;

    /**
     * Ejemplos que se muestran en el mensaje de error. No es la lista de lo
     * que se acepta: ver {@link #tieneEspecial(String)}.
     */
    private static final String EJEMPLOS_ESPECIALES = "! @ # $ %";

    /** Niveles de fortaleza que puede reportar la política. */
    public enum Nivel {
        /** No cumple los requisitos: el registro la va a rechazar. */
        DEBIL("Insegura"),
        /** Cumple los requisitos, pero es poco compleja. */
        MEDIA("Medianamente segura"),
        /** Cumple los requisitos y además es larga y variada. */
        FUERTE("Segura");

        private final String descripcion;

        Nivel(String descripcion) {
            this.descripcion = descripcion;
        }

        /** @return texto para mostrar junto al indicador */
        public String getDescripcion() {
            return descripcion;
        }
    }

    /**
     * Comprueba si la contraseña es aceptable.
     *
     * @param password contraseña a revisar
     * @return mensaje de error listo para mostrar, o {@code null} si es válida
     */
    public String validar(String password) {
        if (password.isEmpty()) {
            return "La contraseña es obligatoria.";
        }
        if (password.length() < LONGITUD_MINIMA) {
            return "La contraseña debe tener al menos " + LONGITUD_MINIMA + " caracteres.";
        }
        if (!tieneMayuscula(password)) {
            return "La contraseña debe incluir al menos una letra mayúscula.";
        }
        if (!tieneDigito(password)) {
            return "La contraseña debe incluir al menos un número.";
        }
        if (!tieneEspecial(password)) {
            return "La contraseña debe incluir al menos un carácter especial (por ejemplo: "
                    + EJEMPLOS_ESPECIALES + ").";
        }
        return null;
    }

    /**
     * Califica la contraseña para el indicador que acompaña al campo.
     *
     * <p><b>Por complejidad, no solo por longitud.</b> Antes el nivel se
     * decidía con un único corte de longitud (12 caracteres), y eso hacía que
     * el nivel intermedio casi nunca se viera: al escribir seguido, la
     * contraseña solía completar los requisitos cuando ya pasaba de 12
     * caracteres, así que el indicador saltaba de "Insegura" directamente a
     * "Segura". Ahora se suman puntos por los rasgos que de verdad cuestan de
     * adivinar, y la longitud es solo uno de ellos.</p>
     *
     * @param password contraseña a evaluar
     * @return nivel correspondiente; {@code DEBIL} mientras no cumpla los requisitos
     */
    public Nivel evaluar(String password) {
        if (validar(password) != null) {
            return Nivel.DEBIL;
        }
        return puntosDeComplejidad(password) >= PUNTOS_PARA_FUERTE ? Nivel.FUERTE : Nivel.MEDIA;
    }

    /**
     * Cuenta los rasgos que hacen más difícil de adivinar una contraseña que
     * ya cumple los requisitos mínimos. El máximo son 5 puntos.
     *
     * <p>Mayúscula, número y carácter especial no suman: son obligatorios, así
     * que toda contraseña válida los tiene y no distinguen a una de otra.</p>
     *
     * @param password contraseña ya validada
     * @return puntuación de 0 a 5
     */
    private int puntosDeComplejidad(String password) {
        int puntos = 0;
        if (password.length() >= LONGITUD_HOLGADA) {
            puntos++;
        }
        if (password.length() >= LONGITUD_LARGA) {
            puntos++;
        }
        // Mezclar mayúsculas y minúsculas amplía el abanico de combinaciones.
        if (tieneMinuscula(password)) {
            puntos++;
        }
        // Repetir el mismo carácter alarga sin aportar: "Aaaaaaaa1!" es larga
        // y pobre a la vez, y sin este punto quedaría igual que una variada.
        if (caracteresDistintos(password) >= CARACTERES_DISTINTOS_RICOS) {
            puntos++;
        }
        if (contarEspeciales(password) > 1) {
            puntos++;
        }
        return puntos;
    }

    private long caracteresDistintos(String texto) {
        return texto.chars().distinct().count();
    }

    private long contarEspeciales(String texto) {
        return texto.chars().filter(c -> !Character.isLetterOrDigit(c)).count();
    }

    private boolean tieneMayuscula(String texto) {
        return texto.chars().anyMatch(Character::isUpperCase);
    }

    private boolean tieneMinuscula(String texto) {
        return texto.chars().anyMatch(Character::isLowerCase);
    }

    private boolean tieneDigito(String texto) {
        return texto.chars().anyMatch(Character::isDigit);
    }

    /**
     * Es especial todo lo que no sea una letra ni un número.
     *
     * <p>Antes se comparaba contra una lista fija de símbolos del teclado
     * inglés, y eso dejaba fuera los que produce un teclado en español:
     * {@code ¿}, {@code ¡}, {@code °}, {@code ´}, {@code €} y el espacio.
     * Quien escribía "Clave2026¿" veía "Insegura" sin entender por qué, ya
     * que el indicador solo muestra el nivel, no el motivo. Preguntar por lo
     * que <em>no</em> es alfanumérico cubre cualquier distribución de teclado
     * sin tener que mantener una lista.</p>
     *
     * <p>La {@code ñ} y las vocales acentuadas siguen contando como letras,
     * que es lo correcto: son letras del alfabeto español, no símbolos.</p>
     */
    private boolean tieneEspecial(String texto) {
        return texto.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
    }
}
