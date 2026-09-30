package view.factory.icons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

/**
 * Íconos de línea que acompañan a cada campo del formulario (el "leading icon"
 * de los formularios modernos: da contexto de un vistazo sobre qué se pide).
 *
 * <p>Se dibujan por código con {@code Graphics2D} en vez de cargarse como
 * imágenes: son trazos simples, escalan a cualquier tamaño sin pixelarse y
 * toman el color del tema, así que cambian solos si cambia la paleta. Es la
 * misma técnica autocontenida de {@link IconoOjo} — solo pinta su propia
 * superficie, no toca fondos de contenedores ni {@code opaque} de nadie (ver
 * "Estabilidad de la interfaz" en {@code CLAUDE.md}).</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.0
 */
public class IconoCampo implements Icon {

    /** Glifo a dibujar, elegido por el significado del campo, no por su forma. */
    public enum Tipo {
        /** Documento de identidad. */
        IDENTIFICACION,
        /** Persona (nombres). */
        USUARIO,
        /** Sobre (correo electrónico). */
        CORREO,
        /** Candado (contraseña). */
        CANDADO,
        /** Etiqueta con marca (tipo de cuenta). */
        ETIQUETA,
        /** Pin de mapa (dirección de envío). */
        UBICACION,
        /** Edificio (NIT de la empresa). */
        EMPRESA,
        /** Renglones de párrafo (descripción de un producto). */
        TEXTO,
        /** Etiqueta colgante (precio de un producto). */
        PRECIO,
        /** Símbolo de porcentaje (descuento). */
        DESCUENTO,
        /** Caja de inventario (existencias). */
        CAJA,
        /** Lupa (barra de búsqueda). */
        BUSCAR,
        /** Marco con una montaña (referencia de imagen). */
        IMAGEN
    }

    private final Tipo tipo;
    private final Color color;
    private final int tamano;

    /**
     * @param tipo   glifo a dibujar
     * @param color  color del trazo
     * @param tamano lado del ícono en píxeles (se dibuja cuadrado)
     */
    public IconoCampo(Tipo tipo, Color color, int tamano) {
        this.tipo = tipo;
        this.color = color;
        this.tamano = tamano;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.translate(x, y);

        // Todos los glifos se dibujan sobre una rejilla de 16x16 y se escalan
        // al tamaño pedido: así un solo juego de coordenadas sirve para
        // cualquier tamaño de ícono.
        double escala = tamano / 16.0;
        g2.scale(escala, escala);

        switch (tipo) {
            case IDENTIFICACION -> dibujarIdentificacion(g2);
            case USUARIO -> dibujarUsuario(g2);
            case CORREO -> dibujarCorreo(g2);
            case CANDADO -> dibujarCandado(g2);
            case ETIQUETA -> dibujarEtiqueta(g2);
            case UBICACION -> dibujarUbicacion(g2);
            case EMPRESA -> dibujarEmpresa(g2);
            case TEXTO -> dibujarTexto(g2);
            case PRECIO -> dibujarPrecio(g2);
            case DESCUENTO -> dibujarDescuento(g2);
            case CAJA -> dibujarCaja(g2);
            case BUSCAR -> dibujarBuscar(g2);
            case IMAGEN -> dibujarImagen(g2);
        }
        g2.dispose();
    }

    private void dibujarIdentificacion(Graphics2D g2) {
        g2.drawRoundRect(1, 3, 14, 10, 3, 3);
        g2.drawOval(4, 6, 4, 4);
        g2.drawLine(10, 7, 13, 7);
        g2.drawLine(10, 10, 13, 10);
    }

    private void dibujarUsuario(Graphics2D g2) {
        g2.drawOval(5, 2, 6, 6);
        g2.drawArc(2, 9, 12, 11, 0, 180);
    }

    private void dibujarCorreo(Graphics2D g2) {
        g2.drawRoundRect(1, 3, 14, 10, 2, 2);
        g2.drawLine(1, 4, 8, 9);
        g2.drawLine(15, 4, 8, 9);
    }

    private void dibujarCandado(Graphics2D g2) {
        g2.drawRoundRect(3, 7, 10, 8, 2, 2);
        g2.drawArc(5, 2, 6, 8, 0, 180);
        g2.drawLine(8, 10, 8, 12);
    }

    /**
     * Etiqueta colgante. Antes era un cuadro con un visto dentro, que se leía
     * como una casilla de verificación y no como un rótulo; se comprobó en
     * pantalla y se redibujó.
     */
    private void dibujarEtiqueta(Graphics2D g2) {
        g2.drawPolygon(new int[]{2, 9, 14, 9, 2}, new int[]{3, 3, 8, 13, 13}, 5);
        g2.drawOval(4, 7, 3, 3);
    }

    private void dibujarUbicacion(Graphics2D g2) {
        g2.drawArc(3, 1, 10, 10, 20, 320);
        g2.drawLine(4, 9, 8, 15);
        g2.drawLine(12, 9, 8, 15);
        g2.drawOval(6, 4, 4, 4);
    }

    private void dibujarEmpresa(Graphics2D g2) {
        g2.drawRect(2, 4, 8, 11);
        g2.drawRect(10, 7, 4, 8);
        g2.drawLine(4, 7, 5, 7);
        g2.drawLine(7, 7, 8, 7);
        g2.drawLine(4, 10, 5, 10);
        g2.drawLine(7, 10, 8, 10);
    }

    private void dibujarTexto(Graphics2D g2) {
        g2.drawLine(2, 4, 14, 4);
        g2.drawLine(2, 8, 14, 8);
        g2.drawLine(2, 12, 9, 12);
    }

    /** Moneda: se distingue de la etiqueta, que ya tiene forma de rótulo. */
    private void dibujarPrecio(Graphics2D g2) {
        g2.drawOval(2, 2, 12, 12);
        g2.drawLine(8, 3, 8, 13);
        g2.drawArc(5, 4, 6, 4, 0, 180);
        g2.drawArc(5, 8, 6, 4, 180, 180);
    }

    private void dibujarDescuento(Graphics2D g2) {
        g2.drawOval(3, 3, 4, 4);
        g2.drawOval(9, 9, 4, 4);
        g2.drawLine(13, 3, 3, 13);
    }

    private void dibujarCaja(Graphics2D g2) {
        g2.drawRect(2, 5, 12, 9);
        g2.drawLine(2, 8, 14, 8);
        g2.drawLine(8, 5, 8, 8);
    }

    private void dibujarBuscar(Graphics2D g2) {
        g2.drawOval(2, 2, 9, 9);
        g2.drawLine(10, 10, 14, 14);
    }

    private void dibujarImagen(Graphics2D g2) {
        g2.drawRoundRect(1, 3, 14, 11, 2, 2);
        g2.drawOval(4, 6, 2, 2);
        g2.drawLine(2, 12, 6, 8);
        g2.drawLine(6, 8, 10, 12);
        g2.drawLine(10, 12, 12, 10);
        g2.drawLine(12, 10, 14, 12);
    }

    @Override
    public int getIconWidth() {
        return tamano;
    }

    @Override
    public int getIconHeight() {
        return tamano;
    }
}
