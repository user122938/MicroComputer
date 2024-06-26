public class Bus {
	//associations
	private Memory memory;
	
	public Bus() {
		
	}
	public void associate(Memory memory) {
		this.memory = memory;
	}
	public void initialize() {
		
	}
	
	public long load(CPU.EDeviceId eDeviceId, int mAR) {
		if(eDeviceId == CPU.EDeviceId.eMemory) {
			return memory.load(mAR);
		}
		return 0;
	}
	
	public void finish() {
		
	}
	public void store(CPU.EDeviceId eDeviceId, int mAR, long mBR) {
		if(eDeviceId == CPU.EDeviceId.eMemory) {
			memory.store(mAR, mBR);
		}
	}
}