import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class Memory {
	private Vector<Long> memory;
	public Memory() throws IOException {
		try {
			int memorySize = 4096;
			this.memory = new Vector<Long>(memorySize);
			this.memory.setSize(memorySize);
			for(int i=0; i<memory.size(); i++) this.memory.set(i, (long) -1);
			FileReader fileReader = new FileReader("data/exe");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			int address = 0;
            while ((line = bufferedReader.readLine()) != null) {
            	long instruction = Long.parseLong(line, 16);
                memory.set(address, instruction);
				address += 8;
            }
            bufferedReader.close();
		} catch (FileNotFoundException e) {e.printStackTrace();}
	}

	public void initialize() {
		
	}

	public long load(int mAR) {
		return this.memory.get(mAR);
	}
	
	public void store(int mAR, long mBR) {
		this.memory.set(mAR, mBR);
	}
	
	public void finish() {
		
	}
}