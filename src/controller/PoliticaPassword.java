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

    /** A partir de esta longitud, una contraseña que cumple todo se considera fuerte. */
    private static final int LONGITUD_FUERTE = 12;

    /** Caracteres aceptados como "especiales". */
    private static final String ESPECIALES = "!@#$%^&*()-_=+[]{};:,.<>/?\\|'\"`~";

    /** Niveles de fortaleza que puede reportar la política. */
    public enum Nivel {
        /** No cumple los requisitos: el registro la va a rechazar. */
        DEBIL("Insegura"),
        /** Cumple los requisitos, pero es corta. */
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
            return "La contraseña debe incluir al menos un carácter especial (por ejemplo: ! @ # $ %).";
        }
        return null;
    }

    /**
     * Califica la contraseña para el indicador que acompaña al campo.
     *
     * @param password contraseña a evaluar
     * @return nivel correspondiente; {@code DEBIL} mientras no cumpla los requisitos
     */
    public Nivel evaluar(String password) {
        if (validar(password) != null) {
            return Nivel.DEBIL;
        }
        boolean larga = password.length() >= LONGITUD_FUERTE;
        return larga && tieneMinuscula(password) ? Nivel.FUERTE : Nivel.MEDIA;
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

    private boolean tieneEspecial(String texto) {
        return texto.chars().anyMatch(c -> ESPECIALES.indexOf(c) >= 0);
    }
}
