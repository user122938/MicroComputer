import java.io.IOException;

public class Main {
	
	private MicroProcessor microprocessor;
	public Main() throws IOException {
		this.microprocessor = new MicroProcessor();
	}
	
	private void initialize() {
		this.microprocessor.initialize();
	}
	
	private void run() {
		this.microprocessor.run();
	}
	
	private void finish() {
		this.microprocessor.finish();
	}
	
	
	public static void main(String[] args) throws IOException {
		Main main = new Main();
		main.initialize();
		main.run();
		main.finish();
	}
}