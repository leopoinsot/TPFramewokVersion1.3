package poinsot.framework;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Configuracion {

	private FileReader fileReader;
	private List<String> clasesAcciones;
	private int maxThreads=-1;

	public Configuracion(FileReader fileReader) {
		this.fileReader = fileReader;
		cargarConfiguracion();
	}

	private void cargarConfiguracion() {
		JsonObject jsonObject = JsonParser.parseReader(fileReader).getAsJsonObject();
		clasesAcciones = new ArrayList<>();
		jsonObject.getAsJsonArray("acciones").forEach(jsonElement -> {
			clasesAcciones.add(jsonElement.getAsString());
		});
		maxThreads = jsonObject.get("max-threads").getAsInt(); // Leer el valor de max-threads
	}

	public List<String> obtenerClases() {
		return clasesAcciones;
	}
	public int obtenerMaxThreads() { // Nuevo método para obtener el valor de max-threads
		if(maxThreads == -1){
			throw new RuntimeException("No se especifico ningun maxThreads");
		}
		return maxThreads;
	}
}