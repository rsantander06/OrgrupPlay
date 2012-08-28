package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.annotation.Where;

import play.data.format.Formats;
import play.data.validation.Constraints;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
@Table(name="mensaje")
public class Mensaje extends Model{

	@Id
	public Long id;
	
	@Formats.NonEmpty
	@Column(nullable=false)
	@Formats.DateTime(pattern="dd/MM/yyyy HH:mm:ss")
	public Date fecha;
	
	@ManyToOne
	public Usuario remitente;
	
	@Constraints.Required
	@Formats.NonEmpty
	@Column(nullable=false)
	public String destinatario;
	
	@Constraints.Required
	@Formats.NonEmpty
	@Column(length=100, nullable=false)
	public String asunto;
	
	@Constraints.Required
	@Formats.NonEmpty
	@Column(nullable=false)
	public String mensaje;
	
	@Formats.NonEmpty
	@Column(nullable=false)
	public String leido;
	
	public static Finder<Long,Mensaje> find = new Finder<Long,Mensaje>(Long.class, Mensaje.class);
	
	public static List<Mensaje> listaMensajesRecibidos(String email){
		return find.where()
				.eq("destinatario", email)
				.findList();		
	}
	
	public static List<Mensaje> muestraId(Long id) {
		return find.where()
				.eq("id", id)
				.findList();
	}
	
	public static List<Mensaje> listaMensajesEnviados(Usuario email) {
		return find.where()
				.eq("remitente", email)
				.findList();
	}
	
	public static List<Mensaje> mensajesNuevosRecibidos(String email) {
		return find.where()
				.eq("destinatario", email)
				.eq("leido", "no")
				.findList();
	}
	
	/**
	 * Cambia el estado del mensaje(campo leido), cuando lo lee el usuario
	 */
	public static void cambiaEstadoLeido(Long id, String email) {
		Ebean.createSqlUpdate(
				"update mensaje set leido = 'si' where " +
				"remitente_correo != '"+email+"' and id = '"+id+"'"
				).execute();
	}
}
