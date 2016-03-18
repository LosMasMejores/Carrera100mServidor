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
	
	static Integer NUM_ATLETAS = 6; // Numero de atletas totales que participan
	static Integer NUM_CARRERAS = 3; // Numero de MainCarrera que participan
	
	Resultado resultado = new Resultado();
	long t_inicio, t_llegada;
	int num_preparados, num_listos, num_terminadas, num_carreras = 0;
	
	
	@Path("/reinicio")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String reinicio() {
		
		Integer dorsal; // Indica el dorsal que le corresponde a cada MainCarrera
						// (dorsal, dorsal - 1, ..., dorsal - i; i = numero de corredores de MainCarrera)
		
		synchronized(NUM_CARRERAS) {
			
			// Si no hay carreras esperando, reiniciamos los contadores
			// Si ya se ha alcanzado el tope de carreras, avisamos
			if (num_carreras == 0) {
				t_inicio = t_llegada = num_preparados = num_listos = num_terminadas = 0;
			} else if (num_carreras == NUM_CARRERAS) {
				return "COMPLETO"; // Se ha alcanzado el numero de MainCarrera esperado
			}
			
			num_carreras++;
			dorsal = (NUM_ATLETAS / NUM_CARRERAS) * num_carreras; // Determinamos que dorsal le corresponde a MainCarrera
			
			if (num_carreras < NUM_CARRERAS) {
				try {
					NUM_CARRERAS.wait(); // Esperamos por el resto de MainCarreras
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				NUM_CARRERAS.notifyAll(); // Avisamos a las demas MainCarrera
			}
		}
		
		return dorsal.toString();
	}
	
	
	@Path("/preparado")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String preparado() {
		
		synchronized (NUM_ATLETAS) {
			
			num_preparados++;
			
			if (num_preparados < NUM_ATLETAS)
				try {
					NUM_ATLETAS.wait(); // Esperamos por el resto de Atletas
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			else
				NUM_ATLETAS.notifyAll(); // Avisamos a las demas Atletas
			
		}
		
		return "listos!";
	}
	
	
	@Path("/listo")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String listo() {
		
		synchronized (NUM_ATLETAS) {
			
			num_listos++;
			
			if (num_listos < NUM_ATLETAS)
				try {
					NUM_ATLETAS.wait(); // Esperamos por el resto de Atletas
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			else
				NUM_ATLETAS.notifyAll(); // Avisamos a las demas Atletas
			
		}
		
		t_inicio = System.currentTimeMillis();
		
		return "ya!";
	}
	
	
	@Path("/llegada")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String llegada(@DefaultValue("0") @QueryParam(value="dorsal") int dorsal) {
		
		t_llegada = System.currentTimeMillis();
		this.resultado.map.put(dorsal, t_llegada - t_inicio); // Almacenamos en Resultado los resultados
		
		return Long.toString(t_llegada - t_inicio);
	}
	
	
	@Path("/resultados")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Resultado resultados() {
		
		synchronized(NUM_CARRERAS) {
			
			num_terminadas++;
			
			if (num_terminadas < NUM_CARRERAS) {
				try {
					NUM_CARRERAS.wait(); // Esperamos por el resto de MainCarreras
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				num_carreras = 0;
				NUM_CARRERAS.notifyAll(); // Avisamos a las demas MainCarrera
			}
		}
		
		return this.resultado;
	}
	
}
