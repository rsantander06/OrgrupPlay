package controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import models.Contacto;
import models.Mensaje;
import models.Usuario;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import views.html.agenda.*;

public class Mensajes extends Controller {
	
	public static Result mensajesRecibidos() {
		return ok(mensajesRecibidos.render(Usuario.find.byId(session("email")), Mensaje.listaMensajesRecibidos(session("email"))));
	}
	
	public static Result mensajesEnviados() {
		return ok(mensajesEnviados.render(Usuario.find.byId(session("email")), Mensaje.listaMensajesEnviados(Usuario.find.byId(session("email")))));
	}
	
	public static Result crearMensaje() {
		return ok(crearMensaje.render(Usuario.find.byId(session("email")), Contacto.listaAmigos(Usuario.find.byId(session("email")))));
	}
	
	public static Result leerMensaje(Long id) {
		Mensaje.cambiaEstadoLeido(id, session("email"));
		return ok(muestraMensaje.render(Usuario.find.byId(session("email")), Mensaje.muestraId(id)));
	}
	
	public static Result enviaMensaje() throws ParseException {
		Form<Mensaje> mensajeForm = form(Mensaje.class).bindFromRequest();
		
		if(mensajeForm.hasErrors()) {
			return ok(crearMensaje.render(Usuario.find.byId(session("email")), Contacto.listaAmigos(Usuario.find.byId(session("email")))));
		}else {
			Mensaje mensaje = mensajeForm.get();
			Date fecha2 = new Date();
			mensaje.fecha = fecha2;
			mensaje.save();
			return ok(crearMensaje.render(Usuario.find.byId(session("email")), Contacto.listaAmigos(Usuario.find.byId(session("email")))));
		}
	}
	
	public static Result mensajesNuevos() {
		return ok(mensajesNuevos.render(Usuario.find.byId(session("email")), Mensaje.mensajesNuevosRecibidos(session("email"))));
	}
	
}
