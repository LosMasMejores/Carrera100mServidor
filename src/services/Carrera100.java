package services;

import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


@Path("/carrera100")
@Singleton
public class Carrera100 {

	static Integer MAX_ATLETAS = 4;
	Resultado resultado = new Resultado();
	double t_inicio, t_llegada;
	int numPreparados, numListos;
	
	
	@Path("/reinicio")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String reinicio() {
		
		resultado.map.clear();
		t_inicio = t_llegada = numPreparados = numListos = 0;
		
		return "Reiniciado t_inicio: " +
				this.t_inicio +
				", t_llegada: " +
				this.t_llegada +
				", atletas inscritos: " +
				this.numPreparados;
	}
	
	
	@Path("/preparado")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String preparado() {
		
		synchronized (MAX_ATLETAS) {
			
			if (numPreparados == MAX_ATLETAS)
				return "no hay sitio";
			
			numPreparados++;
			
			if (numPreparados < MAX_ATLETAS)
				try {
					MAX_ATLETAS.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			else
				MAX_ATLETAS.notifyAll();
			
		}
		
		return "listos!";
	}
	
	
	@Path("/listo")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String listo() {
		
		synchronized (MAX_ATLETAS) {
			
			if (numListos == MAX_ATLETAS)
				return "no hay sitio";
			
			numListos++;
			
			if (numListos < MAX_ATLETAS)
				try {
					MAX_ATLETAS.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			else
				MAX_ATLETAS.notifyAll();
			
		}
		
		return "ya!";
	}
	
	
	@Path("/llegada")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String llegada(@DefaultValue("0") @QueryParam(value="dorsal") int dorsal) {
		
		long tiempo = System.currentTimeMillis();
		this.resultado.map.put(dorsal, tiempo);
		
		return Long.toString(tiempo);
	}
	
	
	@Path("/resultados")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Resultado resultados() {
		
		return this.resultado;
	}
}
