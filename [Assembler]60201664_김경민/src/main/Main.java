package main;
public class Main {
	public static void main(String[] args) {
		Main main = new Main();
		main.initalize();
		main.run();
		main.finish();
	}
	
	public Main() {
		this.assembler = new Assembler();
	}
	
	private Assembler assembler;
	
	private void initalize() {
		this.assembler.initalize();
	}

	private void run() {
		this.assembler.run();
	}

	private void finish() {
		this.assembler.finish();
	}
}