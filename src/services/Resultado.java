package services;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Resultado {
	public Map<Integer, Long> map;
	
	public Resultado() {
		this.map = new HashMap<>();
	}
}
