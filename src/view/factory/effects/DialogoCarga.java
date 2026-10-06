package view.factory.effects;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.BorderFactory;

/**
 * Ejecuta una tarea lenta fuera del hilo de eventos mostrando un indicador
 * mientras dura.
 *
 * <p><b>Por qué hace falta.</b> Swing pinta y atiende los clics en un único
 * hilo, el EDT. Cualquier cosa que tarde —una consulta a MongoDB Atlas, una
 * petición a la API de correo— bloquea ese hilo: la ventana deja de
 * repintarse y Windows la marca como "no responde". Hasta ahora todo el
 * proyecto corría en el EDT, lo que no se notaba con el almacén en memoria y
 * sí se nota en cuanto hay red de por medio.</p>
 *
 * <p>El trabajo va en un {@link SwingWorker}: {@code doInBackground} corre en
 * otro hilo y {@code done} vuelve al EDT, que es el único sitio desde el que
 * se puede tocar la interfaz. El diálogo es <b>modal</b> a propósito: impide
 * que el usuario pulse dos veces "comprar" mientras la primera compra viaja.</p>
 *
 * <p><b>No pinta nada a mano.</b> Reutiliza {@link IndicadorCarga}, que ya
 * llama a {@code super.paintComponent(g)} antes de trazar su arco. Va sin
 * decoración porque es un aviso efímero que se cierra solo, igual que la
 * transición de acceso.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class DialogoCarga {

    private static final int TAM_INDICADOR = 26;

    private final JDialog ventana;
    private final IndicadorCarga indicador;

    /**
     * @param padre     componente sobre el que se centra
     * @param mensaje   qué se está haciendo ("Guardando...", "Conectando...")
     * @param indicador indicador giratorio ya creado por la fábrica
     * @param fondo     color de fondo del panel
     * @param borde     color del contorno
     * @param texto     color del mensaje
     * @param fuente    tipografía del mensaje
     */
    public DialogoCarga(Component padre, String mensaje, IndicadorCarga indicador,
                        java.awt.Color fondo, java.awt.Color borde, java.awt.Color texto,
                        Font fuente) {
        this.indicador = indicador;

        JPanel contenido = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 20));
        contenido.setBackground(fondo);
        contenido.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borde, 1), new EmptyBorder(4, 18, 4, 22)));

        JLabel etiqueta = new JLabel(mensaje);
        etiqueta.setFont(fuente);
        etiqueta.setForeground(texto);
        contenido.add(indicador);
        contenido.add(etiqueta);

        this.ventana = new JDialog(SwingUtilities.getWindowAncestor(padre), "",
                Dialog.ModalityType.APPLICATION_MODAL);
        ventana.setUndecorated(true);
        ventana.setContentPane(contenido);
        ventana.pack();
        ventana.setLocationRelativeTo(padre);
    }

    /**
     * Añade un botón para abandonar la espera.
     *
     * <p>Solo para esperas que dependen del usuario y no de la red, como el
     * acceso con Google: si cierra la pestaña del navegador, sin este botón
     * quedaría atrapado detrás de un diálogo modal hasta que venza el plazo.
     * Cancelar no cierra el diálogo: le pide a la tarea que termine, y el
     * diálogo se cierra, como siempre, cuando la tarea vuelve.</p>
     *
     * @param boton      botón ya estilizado por la fábrica
     * @param alCancelar cómo se le pide a la tarea que termine
     */
    public void permitirCancelar(JButton boton, Runnable alCancelar) {
        boton.addActionListener(e -> {
            boton.setEnabled(false);
            alCancelar.run();
        });
        ventana.getContentPane().add(boton);
        ventana.pack();
        ventana.setLocationRelativeTo(ventana.getOwner());
    }

    /**
     * Lanza la tarea y muestra el indicador hasta que termine.
     *
     * <p>El orden importa: el {@code SwingWorker} se arranca <em>antes</em> de
     * mostrar el diálogo, porque {@code setVisible(true)} sobre un modal
     * bloquea aquí mismo hasta que alguien lo cierre. Cerrarlo es lo primero
     * que hace {@code done()}, ya de vuelta en el EDT.</p>
     *
     * @param <T>       tipo del resultado de la tarea
     * @param tarea     trabajo lento; se ejecuta FUERA del hilo de eventos, así
     *                  que no debe tocar componentes Swing
     * @param alTerminar qué hacer con el resultado; se ejecuta ya en el EDT
     * @param alFallar  qué hacer si la tarea lanzó una excepción, también en
     *                  el EDT; recibe el fallo para poder explicarlo
     */
    public <T> void ejecutar(Supplier<T> tarea, Consumer<T> alTerminar,
                             Consumer<Exception> alFallar) {
        SwingWorker<T, Void> trabajador = new SwingWorker<>() {
            @Override
            protected T doInBackground() {
                return tarea.get();
            }

            @Override
            protected void done() {
                indicador.detener();
                ventana.dispose();
                T resultado;
                try {
                    resultado = get();
                } catch (ExecutionException ex) {
                    // El fallo del trabajo de fondo llega envuelto, y el
                    // mensaje del envoltorio es "java.lang.X: ...": se entrega
                    // la causa, que es la que lleva el mensaje para el usuario.
                    Throwable causa = ex.getCause();
                    alFallar.accept(causa instanceof Exception e ? e : new RuntimeException(causa));
                    return;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    alFallar.accept(ex);
                    return;
                }
                // Fuera del try a propósito: si alTerminar falla, es un error
                // de quien continúa, no del trabajo de fondo, y no debe
                // presentarse como "no se pudo completar la operación".
                alTerminar.accept(resultado);
            }
        };

        indicador.iniciar();
        trabajador.execute();
        ventana.setVisible(true);
    }
}
