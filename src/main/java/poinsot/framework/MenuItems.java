package poinsot.framework;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MenuItems {
	private List<AdaptadorCallable> acciones = new ArrayList<>();
	private int threads;

	public MenuItems(FileReader fileReader) {
		cargarAcciones(fileReader);
	}

	private void cargarAcciones(FileReader fileReader) {
		Configuracion configuracion = new Configuracion(fileReader);
		List<String> clasesAcciones = configuracion.obtenerClases();
		for (String clase : clasesAcciones) {
			try {
				Class<?> clazz = Class.forName(clase);
				Accion accion = (Accion) clazz.getDeclaredConstructor().newInstance();
				AdaptadorCallable adaptador = new AdaptadorCallable(accion);
				acciones.add(adaptador);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException |
					 InvocationTargetException e) {
				throw new RuntimeException("Error al cargar la clase: " + clase, e);
			}
		}
		threads = configuracion.obtenerMaxThreads();
	}
	public void iniciar() {
		DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
		try (Screen screen = terminalFactory.createScreen()) {
			screen.startScreen();

			// Crear GUI y ventana principal
			WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);
			BasicWindow mainWindow = new BasicWindow("Menú de Opciones");

			// Crear panel con layout para la ventana principal
			Panel mainPanel = new Panel(new GridLayout(1));
			mainPanel.addComponent(new Label("Bienvenido, estas son sus opciones:"));

			// Crear un CheckBoxList para seleccionar múltiples AdaptadorCallable
			CheckBoxList<AdaptadorCallable> checkBoxList = new CheckBoxList<>();
			for (AdaptadorCallable adaptador : acciones) {
				checkBoxList.addItem(adaptador);
			}
			mainPanel.addComponent(checkBoxList);

			// Agregar botón para ejecutar las acciones seleccionadas
			mainPanel.addComponent(new Button("Ejecutar", () -> {
				// Obtener los adaptadores seleccionados en el CheckBoxList
				List<AdaptadorCallable> selectedAdapters = checkBoxList.getCheckedItems();
				// Crear un servicio de ejecución con un número fijo de hilos
				ExecutorService executorService = Executors.newFixedThreadPool(threads);
				for (AdaptadorCallable adaptador : selectedAdapters) {
					// Enviar cada acción seleccionada para su ejecución en el servicio de ejecución
					executorService.submit(() -> ejecutarAccion(adaptador));
				}
				// Iniciar el proceso de cierre del servicio de ejecución
				executorService.shutdown();
				try {
					// Esperar a que todas las tareas se completen o el tiempo de espera expire
					if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
						// Forzar el cierre del servicio de ejecución si el tiempo de espera expira
						executorService.shutdownNow();
					}
				} catch (InterruptedException e) {
					// Forzar el cierre del servicio de ejecución si se interrumpe la espera
					executorService.shutdownNow();
				}
			}));

			// Agregar botón de salir
			mainPanel.addComponent(new Button("Salir", () -> {
				try {
					// Detener la pantalla del terminal y salir del programa
					screen.stopScreen();
				} catch (IOException e) {
					throw new RuntimeException("Error:", e);
				}
				System.out.println("Saliendo...");
				System.exit(0);
			}));

			mainWindow.setComponent(mainPanel);
			textGUI.addWindowAndWait(mainWindow);
		} catch (IOException e) {
			throw new RuntimeException("Error:", e);
		}
	}
	private void ejecutarAccion(AdaptadorCallable accion) {
		DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
		try (Screen screen = terminalFactory.createScreen()) {
			screen.startScreen();

			// Crear GUI y ventana emergente
			WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);
			BasicWindow popupWindow = new BasicWindow("Acción Ejecutada");

			// Crear panel con layout para la ventana emergente
			Panel popupPanel = new Panel(new GridLayout(1));

			// Ejecutar la acción y obtener la descripción
			accion.run();

			String descripcionAccion = accion.descripcionItemMenu();

			// Agregar componentes al panel emergente
			popupPanel.addComponent(new Label("Se ejecutó la acción:"));
			popupPanel.addComponent(new Label(accion.nombreItemMenu()));

			// Agregar espacio vacío para separación visual en la interfaz gráfica
			popupPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));

			// Mostrar la descripción de la acción ejecutada
			popupPanel.addComponent(new Label(descripcionAccion));

			// Agregar espacio vacío para separación visual en la interfaz gráfica
			popupPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));

			// Agregar botón para cerrar la ventana emergente
			popupPanel.addComponent(new Button("Aceptar", popupWindow::close));

			// Agregar panel emergente a la ventana emergente
			popupWindow.setComponent(popupPanel);

			// Mostrar ventana emergente y esperar a que el usuario interactúe con ella
			textGUI.addWindowAndWait(popupWindow);

		} catch (IOException e) {
			throw new RuntimeException("Error", e);
		}
	}
}
