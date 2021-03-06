package models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Page;
import com.avaje.ebean.SqlRow;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
@Table(name="contacto")
public class Contacto extends Model {

    @Id
    public Long id;

    @ManyToOne
    public Usuario usuario1;

    @ManyToOne
    public Usuario usuario2;

    @Formats.NonEmpty
    @Column(length=2, nullable=false)
    public String amigo;

    @Formats.NonEmpty
    @Column(length=2, nullable=false)
    public String notificado;

    // Consultas

    public static Finder<Long,Contacto> find = new Finder<Long,Contacto>(Long.class, Contacto.class);

    /**
     * Lista los contactos pendientes.
     *
     * @param email
     * @return
     */
    public static List<Contacto> listaContactosPendientes(String email){
        return Contacto.find.where().eq("usuario2_correo", email).eq("amigo", "no").findList();
    }

    /**
     * Cambia de estado al agregar un amigo.
     *
     * @param id
     */
    public static void estado(Long id){
        Contacto contacto = find.ref(id);
        contacto.amigo = "si";
        contacto.update();
    }

    /**
     * Lista todos los amigos del usuario.
     *
     * @param email
     * @return lista de amigos.
     */
    public static List<Contacto> listaAmigos(String email) {
        return Contacto.find.where()
                .eq("usuario1_correo", email)
                .eq("amigo", "si")
                .order().asc("usuario2_correo")
                .findList();
    }

    /**
     * Busca si es usuario ya esta agregado, para mostrar o no el boton agregar al buscar usuarios
     *
     * @param usuario1
     * @param usuario2
     * @return
     */
    public static String compruebaUsuarioExistente(String usuario1, String usuario2) {
        Contacto contacto = Ebean.find(Contacto.class)
                .select("usuario2_correo")
                .where()
                .eq("usuario1_correo", usuario1)
                .eq("usuario2_correo", usuario2)
                .eq("amigo", "si")
                .findUnique();
        try {
            if (contacto.usuario2.correo.toString().isEmpty()) {
                return "";
            } else {
                return contacto.usuario2.correo.toString();
            }
        } catch(Exception e) {}
        return "";
    }

    /**
     * Busca si el usuario esta agregado pero todavia no es aceptado, para mostrar boton con solicitud enviada
     *
     * @param usuario1
     * @param usuario2
     * @return
     */
    public static String compruebaSolicitud(String usuario1, String usuario2) {
        Contacto contacto = Ebean.find(Contacto.class)
                .select("usuario2_correo")
                .where()
                .eq("usuario1_correo", usuario1)
                .eq("usuario2_correo", usuario2)
                .eq("amigo", "no")
                .findUnique();
        try {
            if (contacto.usuario2.correo.toString().isEmpty()) {
                return "";
            } else {
                return contacto.usuario2.correo.toString();
            }
        } catch(Exception e) {}
        return "";
    }

    /**
     * cambia el estado de la solicitud enviado por el usuario1
     *
     * @param usuario1
     * @param usuario2
     */
    public static void cambiaEstado(String usuario1, String usuario2) {
        Ebean.createSqlUpdate(
                "update Contacto set amigo = 'si' where " +
                "usuario1_correo = '"+usuario1+"' and usuario2_correo = '"+usuario2+"'"
                ).execute();
    }

    /**
     * Obtiene el id de la solicitud de amistad en caso de que no la acepte el usuario2
     * Tambien se usa para obtener el id y luego usarlo para eliminar el campo de contacto (usuario1, usuario2) #PARTE1
     *
     * @param usuario1
     * @param usuario2
     * @return
     */
    public static Long obtieneId(String usuario1, String usuario2) {
        Contacto contacto = Ebean.find(Contacto.class)
                .select("id")
                .where()
                .eq("usuario1_correo", usuario1)
                .eq("usuario2_correo", usuario2)
                .findUnique();
        return contacto.id;
    }

    /**
     * Tambien se usa para obtener el id y luego usarlo para eliminar el campo de contacto (usuario1, usuario2) #PARTE2
     *
     * @param usuario1
     * @param usuario2
     * @return
     */
    public static Long obtieneId2(String usuario1, String usuario2) {
        Contacto contacto = Ebean.find(Contacto.class)
                .select("id")
                .where()
                .eq("usuario1_correo", usuario2)
                .eq("usuario2_correo", usuario1)
                .findUnique();
        return contacto.id;
    }

    /**
     * Elimina la solicitud de amistad
     *
     * @param id
     */
    public static void eliminaSolicitudAmistad(Long id) {
        Ebean.createSqlUpdate("delete from contacto where " +
                "id = '"+id+"'"
                ).execute();
    }

    /**
     * Elimina un contacto de la lista de amigos
     *
     * @param id
     */
    public static void eliminaContacto(Long id) {
        Ebean.createSqlUpdate(
                "delete from contacto where " +
                "id = '"+id+"'"
                ).execute();
    }

    /**
     * Busca en la base de datos 10 campos desde la pagina que le envian
     * Es para los contactos(amigos) que posee una persona
     * @param page
     * @param filter
     * @param email
     * @return
     */
    public static Page<Contacto> page(int page, String filter, String email) {
        return
                find
                    .where()
                    .eq("usuario1_correo", email)
                    .eq("amigo", "si")
                    .orderBy().asc("usuario2_correo")
                    .findPagingList(10)
                    .getPage(page);
    }

    public static Page<Contacto> page2(int page, String filter, String email) {
        return
                find
                    .where()
                    .eq("usuario2_correo", email)
                    .eq("amigo", "no")
                    .order().asc("usuario1_correo")
                    .findPagingList(10)
                    .getPage(page);
    }
}
