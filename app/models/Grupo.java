package models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

@Entity
@Table(name="grupo")
public class Grupo extends Model {
	
	@Id
	public Long id;
	
	@Constraints.Required
    @Formats.NonEmpty
    @Column(length=25, nullable=false)
	public String nombre;
	
	@Constraints.Required
    @Formats.NonEmpty
    @Column(length=10, nullable=false)
	public String distintivo;
	
	// Consultas
	
	public static Finder<Long,Grupo> find = new Finder<Long,Grupo>(Long.class, Grupo.class);

	/**
	 * Obtiene todos los grupos que está el usuario, para mostrarlos en el home "/App"
	 * 
	 * @param correo
	 * @return una lista con todos los grupos encontrados.
	 */
	public static List<SqlRow> getGrupos(String correo) {
		String sql = "select g.id, g.nombre from grupo g inner join integrante i on g.id = i.grupo_id where i.usuario_correo = :correo";
		
		return Ebean.createSqlQuery(sql).setParameter("correo", correo).findList();
	}

	/**
	 * Obtiene un grupo determinado, en el que pertenece el usuario, 
	 * para mostrarlo en la vista "/Grupo"
	 * 
	 * @param correo
	 * @param id
	 * 		identificador del grupo seleccionado en la vista home (/App)
	 * @return un objeto con el grupo encontrado.
	 */
	public static SqlRow getGrupo(String correo, Long id) {
		String sql = "SELECT i.usuario_correo, i.tipo, g.id, g.nombre, g.distintivo FROM grupo g INNER JOIN integrante i ON g.id = i.grupo_id WHERE i.usuario_correo = :correo AND g.id = :id";
		
		return Ebean.createSqlQuery(sql).setParameter("correo", correo).setParameter("id", id).findUnique();
	}
}
