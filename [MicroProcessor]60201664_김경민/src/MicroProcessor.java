import java.io.IOException;

public class MicroProcessor {
	
	private boolean bPowerOn;
	
	private Bus bus;
	private CPU cpu;
	private Memory memory;
	
	public MicroProcessor() throws IOException {
		//attributes
		this.bPowerOn = true;
		
		//Components
		this.bus = new Bus();
		this.cpu = new CPU();
		this.memory = new Memory();
		
		//Associations
		this.cpu.associate(this.bus);
		this.bus.associate(this.memory);
	}

	public void initialize() {
		bus.initialize();
		cpu.initialize();
		memory.initialize();
	}

	public void run() {
		cpu.setRegisterAddress();
		int i = 0;
		while(bPowerOn) {
			bPowerOn = cpu.run(i);
			i++;
		}
	}

	public void finish() {
		
	}
}