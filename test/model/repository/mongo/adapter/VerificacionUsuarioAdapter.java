package model.repository.mongo.adapter;

import model.entity.Cliente;
import model.entity.Proveedor;
import model.entity.Usuario;
import org.bson.Document;

/**
 * Comprobación automática del formato de documento de {@link UsuarioAdapter}.
 *
 * <p>Cambiar un adaptador es cambiar el formato de los datos que ya están en
 * MongoDB Atlas. Esta clase fija lo que no puede romperse: un Cliente de
 * formulario sigue produciendo los mismos 7 campos de siempre, los campos
 * nuevos solo viajan si tienen valor, y un documento guardado antes de que
 * existieran se sigue leyendo. Convierte en memoria: no se conecta a Atlas.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class VerificacionUsuarioAdapter {

    private static int fallos = 0;

    private VerificacionUsuarioAdapter() {
    }

    private static void comprobar(String que, boolean ok) {
        System.out.println((ok ? "  OK    " : "  FALLA ") + que);
        if (!ok) {
            fallos++;
        }
    }

    /**
     * @param args no se usan
     */
    public static void main(String[] args) {
        UsuarioAdapter adaptador = new UsuarioAdapter();

        System.out.println("Documentos que ya existían");
        Document ana = adaptador.aDocumento(
                new Cliente("5551234", "Ana", "ana@x.com", "x", "Calle 1 # 2-3"));
        comprobar("un Cliente de formulario produce el documento de siempre (7 campos)",
                ana.size() == 7 && !ana.containsKey(UsuarioAdapter.CAMPO_CEDULA));
        Document antiguo = new Document("identificacion", "1").append("nombres", "X")
                .append("correo", "x@x.com").append("password", "p").append("telefono", "")
                .append("tipoCuenta", "Proveedor").append("datoEspecifico", "NIT");
        Usuario deAntes = adaptador.aEntidad(antiguo);
        comprobar("un proveedor guardado antes se lee con empresa y dirección vacías",
                deAntes.puedeVender() && deAntes.getNombreEmpresa().isEmpty()
                && deAntes.getDireccionEnvio().isEmpty());

        System.out.println("Campos nuevos");
        Usuario sofia = new Cliente("google-777", "Sofía", "sofia@gmail.com", "AUTH_GOOGLE", "");
        sofia.setCedula("1098765432");
        sofia.setDireccionEnvio("Calle 45 # 12-30 Cali");
        Usuario sofiaLeida = adaptador.aEntidad(adaptador.aDocumento(sofia));
        comprobar("la cédula de una cuenta de Google viaja en el documento",
                "1098765432".equals(sofiaLeida.getCedula())
                && sofiaLeida.datosDeEnvioCompletos());
        Proveedor luis = new Proveedor("100200", "Luis", "luis@x.com", "x", "900123-1");
        luis.setNombreEmpresa("Tech");
        luis.setDireccionEnvio("Cra 7 # 1-1 Bogotá");
        Usuario luisLeido = adaptador.aEntidad(adaptador.aDocumento(luis));
        comprobar("empresa, dirección y NIT del proveedor viajan en el documento",
                "Tech".equals(luisLeido.getNombreEmpresa())
                && "Cra 7 # 1-1 Bogotá".equals(luisLeido.getDireccionEnvio())
                && "900123-1".equals(luisLeido.getDatoEspecifico()));

        System.out.println(fallos == 0 ? "\nTODO CORRECTO" : "\n" + fallos + " FALLAS");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
