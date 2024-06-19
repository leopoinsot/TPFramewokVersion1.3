package poinsot.framework;

import java.util.concurrent.*;

public class AdaptadorCallable implements RunnableFuture {
	private Accion accion;

	@Override
	public void run() {
		accion.ejecutar();
	}

	public AdaptadorCallable(Accion accion) {
		this.accion = accion;
	}


	public String nombreItemMenu() {
		return accion.nombreItemMenu();
	}

	public String descripcionItemMenu() {
		return accion.descripcionItemMenu();
	}

	@Override
	public String toString() {
		return accion.nombreItemMenu() + " - " + accion.descripcionItemMenu();
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return false;
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		return null;
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return null;
	}
}
