package services;

import java.util.concurrent.Semaphore;

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
	static Semaphore SEM_CARRERAS = new Semaphore(1);
	Resultado resultado = new Resultado();
	long t_inicio, t_llegada;
	int numPreparados, numListos;
	
	
	@Path("/reinicio")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String reinicio() {
		
		int num = SEM_CARRERAS.drainPermits();
		
		if (num == 1) {
			resultado.map.clear();
			t_inicio = t_llegada = numPreparados = numListos = 0;
			
			try {
				SEM_CARRERAS.acquire();
				SEM_CARRERAS.release();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			SEM_CARRERAS.release();
		}
		
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
		
		t_inicio = System.currentTimeMillis();
		
		return "ya!";
	}
	
	
	@Path("/llegada")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String llegada(@DefaultValue("0") @QueryParam(value="dorsal") int dorsal) {
		
		t_llegada = System.currentTimeMillis();
		this.resultado.map.put(dorsal, t_llegada - t_inicio);
		
		return Long.toString(t_llegada - t_inicio);
	}
	
	
	@Path("/resultados")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Resultado resultados() {
		
		return this.resultado;
	}
}
