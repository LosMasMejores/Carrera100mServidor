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
	
	static Integer NUM_ATLETAS; // Numero de atletas totales que participan
	static Integer NUM_CARRERAS; // Numero de MainCarrera que participan
	
	Resultado resultado;
	long t_inicio, t_llegada;
	int num_preparados, num_listos, num_terminadas, num_carreras = 0;
	
	
	@Path("/reinicio")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String reinicio(@DefaultValue("0") @QueryParam(value="carreras") int carreras,
			@DefaultValue("0") @QueryParam(value="atletas") int atletas) {
		
		Integer dorsal; // Indica el dorsal que le corresponde a cada MainCarrera
						// (dorsal, dorsal - 1, ..., dorsal - i; i = numero de corredores de MainCarrera)
		
		synchronized(this.getClass()) {
			
			// No se han enviado ningun dato
			if (carreras == 0 || atletas == 0) {
				return "INCORRECTO";
			}
			
			// Somos el primer MainCarrera en llamar
			// Reiniciamos los datos
			if (num_carreras == 0) {
				NUM_CARRERAS = carreras;
				NUM_ATLETAS = atletas * carreras;
				resultado = new Resultado();
				t_inicio = t_llegada = num_preparados = num_listos = num_terminadas = 0;
			}
			
			// Se ha alcanzado el numero de MainCarrera esperado
			if (num_carreras == NUM_CARRERAS) {
				return "COMPLETO";
			}
			
			// No coinciden los datos enviados con los esperados
			if (NUM_CARRERAS != carreras || NUM_ATLETAS != atletas * carreras) {
				return "INCORRECTO";
			}
			
			num_carreras++;
			dorsal = (NUM_ATLETAS / NUM_CARRERAS) * num_carreras; // Determinamos que dorsal le corresponde a MainCarrera
			
			if (num_carreras < NUM_CARRERAS) {
				try {
					this.getClass().wait(); // Esperamos por el resto de MainCarreras
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				this.getClass().notifyAll(); // Avisamos a las demas MainCarrera
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
		
		synchronized(this.getClass()) {
			
			num_terminadas++;
			
			if (num_terminadas < NUM_CARRERAS && num_carreras != 0) {
				try {
					this.getClass().wait(); // Esperamos por el resto de MainCarreras
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				num_carreras = 0;
				this.getClass().notifyAll(); // Avisamos a las demas MainCarrera
			}
		}
		
		return this.resultado;
	}
	
}
