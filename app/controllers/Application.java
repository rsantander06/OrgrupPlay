package controllers;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import models.Correo;
import models.Notificaciones;
import models.Usuario;

import play.data.Form;
import play.mvc.*;

import views.html.*;
import views.html.home.*;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.mail.*;

public class Application extends Controller {

    /**
     * Clase para validar el ingreso del usuario.
     *
     */
    public static class Login {
        public String correo;
        public String password;

        public String validate() {
            if (Usuario.authenticate(correo, DigestUtils.shaHex(password)) == null) {
                return "Correo o contraseña invalido";
            }
            return null;
        }
    }

	public static Result index() {
		return ok(index.render());
	}

	public static Result login() {
		return ok(login.render(form(Login.class), ""));
	}

	public static Result registro() {
		return ok(registro.render(form(Usuario.class), ""));
	}

	public static Result recuperar() {
		return ok(olvidoPassword.render(""));
	}

	public static Result contacto() {
		return ok(contacto.render());
	}

	public static Result about() {
        return ok(about.render());
	}

	public static Result logout() {
		// elimina la variable de session.
		session().clear();
        return redirect(routes.Application.index());
	}

	/**
	 * Comprueba el Login y si la cuenta esta activada o no.
	 *
	 * @return si esta activada redirreciona al home,
	 * sino redirecciona al login con un mensaje de error.
	 */
	public static Result comprobarLogin() {
		Form<Login> loginForm = form(Login.class).bindFromRequest();
		if (loginForm.hasErrors()) {
			return badRequest(login.render(loginForm, ""));
		} else {
			Login user = loginForm.get();
			if (Usuario.cuentaActivada(user.correo)) {
				session("email", loginForm.get().correo);
				return redirect(routes.Home.index());
			}
			else if (Usuario.cuentaInactiva(user.correo)) {
				session("email", loginForm.get().correo);
				return redirect(routes.Home.index());
			}
			else {
				return ok(login.render(
						form(Login.class),
						"Esta cuenta no esta activada"));
			}
		}
	}

	/**
	 * Comprueba el registro del usuario, enviandole a la cuenta un enlace para que
	 * posteriormente active la cuenta.
	 *
	 * @return a la pagina de informaciones con el respectivo mensaje de bienvenida.
	 * @throws IOException
	 * @throws EmailException
	 */
	public static Result comprobarRegistro() throws IOException, EmailException {
		Integer id;
		Form<Usuario> formRegistro = form(Usuario.class).bindFromRequest();

		if (formRegistro.hasErrors()) {
            return badRequest(registro.render(form(Usuario.class), ""));
		} else {
			Usuario user = formRegistro.get();
			Notificaciones notificaciones = new Notificaciones();

			if (Usuario.esMiembro(user.correo)) {
				return badRequest(registro.render(
						form(Usuario.class),
						"El correo ya existe"));
			} else {
				do {
					// genera un numero de 9 digitos para usarlo posteriormente
					// en el enlace que se le envia al correo y validar la cuenta.
					id = (int)(Math.random()*1000000000);

				} while (Usuario.estaIdVerificador(id) == true);

				// Crea una imagen para el usuario por defecto, guardandola en /images/usuarios,
				// con el nombre del correo + las extension (.gif).
				String path = "./public/images/usuarios/" + user.correo + ".gif";
				org.apache.commons.io.FileUtils.copyFile(new File("./public/images/usuarios/user.gif"), new File(path));

				// Agrega los datos faltantes al modelo usuario. para guardarlos posteriormente a la BD.
				user.id_verificador = id;
				user.imagen = user.correo + ".gif";
				user.estado = "Desactivada";
				user.colorTareaAlta = "#B71616";
				user.colorTareaMedia = "#381BCA";
				user.colorTareaBaja = "#EBDF32";
				user.notificado = "no";
				String pass = user.password;
				String encript = DigestUtils.shaHex(pass);
				user.password = encript;

				notificaciones.usuario_correo = user.correo;
				notificaciones.tarea = "si";
				notificaciones.contacto = "si";
				notificaciones.mensaje = "si";
				notificaciones.grupoAgregan = "si";
				notificaciones.grupoEliminan = "si";
				notificaciones.grupoAdmin = "si";

				//Guarda el nuevo usuario con las notificaciones en la BD
				user.save();
				notificaciones.save();

			    return ok(informaciones.render(
			    		"Bienvenidos a la red de Orgrup, red de agendas que te permitirá " +
			    		"llevar un registro de todas tus actividades, podrás buscar amigos y colegas para gestionar " +
			    		"reuniones de forma automática, subir apuntes, documentos y más! " +
			    		"Confirme su cuenta a través del enlace enviado a su correo.",
			    		"Registro"));
			}
		}
	}

	/**
	 * Envia la password al usuario por correo.
	 *
	 * @return a la pagina de informaciones con el mensajes correspondiente,
	 * si no encuentra el correo en la BD, el mensaje cambia.
	 * @throws EmailException
	 */
	public static Result recuperarPassword() {
		Form<Usuario> formLogin = form(Usuario.class).bindFromRequest();

		if (formLogin.hasErrors()) {
			return badRequest(olvidoPassword.render(""));
		} else {
			String correo = formLogin.get().correo;

			if (Usuario.getEmail(correo)) {
				Usuario usuario = Usuario.find.byId(correo);
				usuario.notificado = "pw";
				usuario.update();
				return ok(informaciones.render(
			    		"Rebice su correo electronico para completar el cambio de contraseña",
			    		"Recuperar Contraseña"));
			} else {
				return badRequest(olvidoPassword.render("El correo electronico es incorrecto"));
			}
		}
	}

	/**
	 * Guarda un mensaje del usuario en la BD.
	 *
	 * @return a la pagina de informaciones con el mensaje correspondiente.
	 */
	public static Result guardaMensaje() {
		Form<Correo> formContacto = form(Correo.class).bindFromRequest();

		if (formContacto.hasErrors()){
			return badRequest(contacto.render());
		} else {
			Correo mensaje = formContacto.get();
			mensaje.save();
			return ok(informaciones.render(
					"Su mensaje ha sido enviado exitosamente.",
					"Contacto"));
		}
	}

	/**
	 * Valida al usuario anteriormente registrado
	 * comparando el id llegado por la url al que esta en la BD
	 *
	 * @param correo
	 * 		Llegado por la url.
	 * @param id
	 * 		Llegado por la url.
	 * @return a una pagina que le verifica la cuenta.
	 */
	public static Result verificaCuenta(String correo, Integer id) {
		if (Usuario.verificaCuenta(correo, id)) {
			Usuario.actualizaEstado(correo, id);
			return ok(verificaCuenta.render());
		}
		return ok();
	}

	/**
	 * Dirije a la pagina para restablecer la contraseña.
	 *
	 * @param correo del usuario
	 * @param id es el id_verificador creado al registrarse.
	 * @return
	 */
	public static Result reseteaPassword(String correo, Integer id) {
		if (Usuario.verificaCuenta(correo, id))
			return ok(resetear_password.render(correo));
		else
			return redirect(routes.Application.index());
	}

	/**
	 * Actualiza la contraseña nueva del usuario.
	 *
	 * @return a informaciones.
	 */
	public static Result actualizarPassword() {
		Form<Usuario> formP = form(Usuario.class).bindFromRequest();
		if (formP.hasErrors()) {
			return badRequest();
		} else {
			Usuario usuario = Usuario.find.byId(formP.get().correo);
			usuario.password = DigestUtils.shaHex(formP.get().password);
			usuario.update();
			return ok(informaciones.render(
					"Su contraseña se a restablecido exitosamente.",
					"Contraseña recuperada."));
		}
	}
}