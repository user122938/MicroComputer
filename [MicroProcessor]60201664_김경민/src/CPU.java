public class CPU {
	public enum EDeviceId{
		eCPU,
		eMemory
	}
	public enum EStatus {
		eZero(0xFE, 0x01, 0x01),
		eBZ(0xFD, 0x01, 0x01),
		eEQ(0xFB, 0x01, 0x01);
		private int nClear;
		private int nSet;
		private int nGet;
		EStatus(int nClear, int nSet, int nGet) {
			this.nClear = nClear;
			this.nSet = nSet;
			this.nGet = nGet;
		}
		public int getNClear() {return this.nClear;}
		public int getNSet() {return this.nSet;}
		public int getNGet() {return this.nGet;}
	}
	//component
	public enum ERegisters { 
		eMAR,
		eMBR,
		ePC,
		eIR,
		eCS,
		eDS,
		eSS,
		eHS,
		eSP,
		eFP,
		eR0,
		eR1,
		eR2,
		eR3,
		eR4,
		eR5,
		eR6,
		eR7,
		eR8,
		eStatus
	}
	long registers[] = new long[ERegisters.values().length];
	
	//associations
	private Bus bus;
	private boolean bPowerOn;
	
	int instructionSize;
	public CPU() {
		bPowerOn = true;
	}
	public void associate(Bus bus) {
		this.bus = bus;
	}
	
	public void initialize() {
		instructionSize = 8;
	}
	public void setRegisterAddress() {
		int SegmentAddress = 32;
		for(int i=0; i<4; i++) {
			fetch();
			long segmentSize = get(ERegisters.eIR) & 0x00000000FFFF;
			ERegisters targetSegment = ERegisters.values()[ERegisters.eCS.ordinal() + i]; 
			if(i==3) set(targetSegment, SegmentAddress+segmentSize);
			else set(targetSegment, SegmentAddress);
			SegmentAddress += segmentSize+4;
		}
		set(ERegisters.ePC, get(ERegisters.eCS));
		set(ERegisters.eSP, get(ERegisters.eSS));
		set(ERegisters.eFP, get(ERegisters.eSS));
		System.out.println("CS: "+get(ERegisters.eCS));
		System.out.println("DS: "+get(ERegisters.eDS));
		System.out.println("SS: "+get(ERegisters.eSS));
		System.out.println("HS: "+get(ERegisters.eHS));
	}
	private long get(ERegisters eRegister) {return registers[eRegister.ordinal()];}
	private void set(ERegisters eRegister, long value) {registers[eRegister.ordinal()] = value;}
	private void setZero(boolean bResult) {
		if(bResult) { //0으로 세팅
			this.registers[ERegisters.eStatus.ordinal()]
					= this.registers[ERegisters.eStatus.ordinal()] & EStatus.eZero.getNClear();
		} else { // 1로 세팅
			this.registers[ERegisters.eStatus.ordinal()]
					= this.registers[ERegisters.eStatus.ordinal()] | EStatus.eZero.getNSet();
		}
	}
	private void setBZ(boolean bResult) {
		if(bResult) { //0으로 세팅
			this.registers[ERegisters.eStatus.ordinal()]
					= this.registers[ERegisters.eStatus.ordinal()] & EStatus.eBZ.getNClear();
		} else { // 1로 세팅
			this.registers[ERegisters.eStatus.ordinal()]
					= this.registers[ERegisters.eStatus.ordinal()] | EStatus.eBZ.getNSet();
		}
	}
	private boolean getZero() {
		return (registers[ERegisters.eStatus.ordinal()] & EStatus.eZero.getNGet()) == 1;
	}
	private boolean getBZ() {
		return (registers[ERegisters.eStatus.ordinal()] & EStatus.eBZ.getNGet()) == 1;
	}
	//instructions
	private void move(ERegisters eTarget, ERegisters eSource) {
		registers[eTarget.ordinal()] = registers[eSource.ordinal()];
	}
	
	//instruction execution cycle
	private void fetch() {
		move(ERegisters.eMAR, ERegisters.ePC);
		set(ERegisters.eMBR, bus.load(EDeviceId.eMemory, (int) get(ERegisters.eMAR)));
		move(ERegisters.eIR, ERegisters.eMBR);
		set(ERegisters.ePC, get(ERegisters.ePC)+instructionSize);
	}
	
	private void decode(int line) { 
		System.out.println("-----------------------------------------");
		System.out.println();
        long instruction = Long.parseUnsignedLong(Long.toHexString(get(ERegisters.eIR)), 16);
        System.out.printf(line+" Instruction: 0x%012x\n", instruction);
        int opCode = (int) ((instruction >> 32) & 0xFFFF);
        switch(opCode) {
        case 0:
        	halt();
        	break;
        case 1:
        	loadA(instruction);
        	break;
        case 2:
        	storeA(instruction);
        	break;
        case 3:
        	add(instruction);
        	break;
        case 4:
			and(instruction);
			break;
        case 5:
			jump(instruction);
			break;
        case 6:
			zero(instruction);
			break;
        case 7:
			belowZero(instruction);
			break;
        case 8:
        	compare(instruction);
        	break;
        case 9:
			not(instruction);
			break;
        case 10:
			shiftRight(instruction);
			break;
        case 11:
			moveR(instruction);
			break;
        case 12:
			moveC(instruction);
			break;
        case 13:
			push(instruction);
			break;
        case 14:
			pop(instruction);
			break;
        case 15:
        	storeObjectV(instruction);
			break;
        case 16:
        	loadObjectV(instruction);
			break;
        case 17:
        	jumpRegister(instruction);
			break;
        case 18:
        	loadMemoryInregister(instruction);
			break;
        case 19:
        	storeMemoryInregister(instruction);
			break;
        }
	}

	private void halt() {
		System.out.println("Halt");
		bPowerOn = false;
	}
	private void loadA(long instruction) {
		long operand1 = (instruction >> 16) & 0xFFFF;
		ERegisters targetAC = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand1)]; 
		long operand2 = instruction & 0xFFFF;
		set(targetAC, bus.load(EDeviceId.eMemory, (int) (get(ERegisters.eDS)+operand2)));
		System.out.println("LDA: address("+operand2+") value: "+get(targetAC)+" to "+targetAC);
	}
	private void storeA(long instruction) {
		long operand1 = (instruction >> 16) & 0xFFFF;
		long operand2 = instruction & 0xFFFF;
		ERegisters targetAC = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand2)];
		move(ERegisters.eMBR, targetAC);
		bus.store(EDeviceId.eMemory, (int) (get(ERegisters.eDS)+operand1), get(ERegisters.eMBR));
		System.out.println("STA: MAR:"+operand1+" MBR: "+get(ERegisters.eMBR));
	}
	private void add(long instruction) {
		long operand1 = (instruction >> 16) & 0xFFFF;
		ERegisters targetAC1 = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand1)];
		long operand2 = instruction & 0xFFFF;
		ERegisters targetAC2 = null;
		if(operand2==99 || operand2==100) {
			if(operand2==99) targetAC2 = ERegisters.eFP;
			else if(operand2==100) targetAC2 = ERegisters.eSP;
			
			int address = (int) get(targetAC2);
			set(ERegisters.eMBR, bus.load(EDeviceId.eMemory, address));
			System.out.println("ADD: "+targetAC1+": "+get(targetAC1)+" + "+get(ERegisters.eMBR));
			set(targetAC1, get(targetAC1)+get(ERegisters.eMBR));
		} else {
			targetAC2 = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand2)]; 
			System.out.println("ADD: "+targetAC1+": "+get(targetAC1)+" + "+get(targetAC2));
			set(targetAC1, get(targetAC1)+get(targetAC2));
		}
	}
	private void and(long instruction) {
		long operand1 = (instruction >> 16) & 0xFFFF;
		ERegisters targetAC1 = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand1)];
		long operand2 = instruction & 0xFFFF;
		ERegisters targetAC2 = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand2)];
		if(get(targetAC1)==get(targetAC2)) {
			System.out.println("eAND: set EQ-flag 1");
			setZero(true);
		} else {
			System.out.println("eAND: set EQ-flag 0");
			setZero(false);
		}
	}
	private void jump(long instruction) {
		long operand1 = (instruction >> 16) & 0xFFFF;
		long targetLine = operand1 * instructionSize;
		System.out.println("eJmp: goto "+operand1+" line");
		set(ERegisters.ePC, get(ERegisters.eCS)+targetLine);
	}
	private void zero(long instruction) {
		if(getZero()) {
			long operand1 = (instruction >> 16) & 0xFFFF;
			long targetLine = operand1 * instructionSize;
			System.out.println("eZERO: Z-flag is 1. goto "+operand1+" line");
			set(ERegisters.ePC, get(ERegisters.eCS)+targetLine);
		} else {
			System.out.println("eZERO: Z-flag is 0. Jump Failed");
		}
	}
	private void belowZero(long instruction) {
		if(getBZ()) {
			long operand1 = (instruction >> 16) & 0xFFFF;
			long targetLine = operand1 * instructionSize;
			System.out.println("eBZ: BZ-flag is 1. goto "+operand1+" line");
			set(ERegisters.ePC, get(ERegisters.eCS)+targetLine);
		} else {
			System.out.println("eBZ: BZ-flag is 0. Jump Failed");
		}
	}
	private void compare(long instruction) {
		long operand1 = (instruction >> 16) & 0xFFFF;
		ERegisters targetAC1 = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand1)];
		long operand2 = instruction & 0xFFFF;
		ERegisters targetAC2 = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand2)];
		if(get(targetAC1)-get(targetAC2)>0) {
			System.out.println("eCMP: set BZ-flag 1");
			setBZ(false);
			setZero(true);
		} else if(get(targetAC1)-get(targetAC2)==0){
			System.out.println("eCMP: set Z-flag 1");
			setBZ(true);
			setZero(false);
		} else {
			System.out.println("CMP: set BZ, Z-flag 0");
			setBZ(true);
			setZero(true);
		}
	}
	private void not(long instruction) {
		long operand1 = (instruction >> 16) & 0xFFFF;
		ERegisters targetAC = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand1)];
		set(targetAC, -get(targetAC));
		System.out.println("eNOT: "+get(targetAC));
	}
	private void shiftRight(long instruction) {
		long operand1 = (instruction >> 16) & 0xFFFF;
		ERegisters targetAC = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand1)];
		set(targetAC, get(targetAC)>>1);
		System.out.println("eSHR: "+get(targetAC));
	}
	private void moveR(long instruction) {
		long operand1 = (instruction >> 16) & 0xFFFF;
		long operand2 = instruction & 0xFFFF;
		ERegisters targetAC1, targetAC2; 
		if(operand1==99) targetAC1 = ERegisters.eFP;
		else if(operand1==100) targetAC1 = ERegisters.eSP;
		else targetAC1 = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand1)]; 
		
		if(operand2==99) targetAC2 = ERegisters.eFP;
		else if(operand2==100) targetAC2 = ERegisters.eSP;
		else targetAC2 = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand2)]; 
		
		move(targetAC1, targetAC2);
		System.out.println("Move: "+operand1+" <- "+operand2+"("+get(targetAC2)+")");
	}
	private void moveC(long instruction) {
		long operand1 = (instruction >> 16) & 0xFFFF;
		ERegisters targetAC = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand1)]; 
		long operand2 = instruction & 0xFFFF;
		set(targetAC, operand2);
		System.out.println("MOVEC: "+targetAC+" at Value: #"+operand2);
	}
	private void push(long instruction) {
		if(get(ERegisters.eSP)>=get(ERegisters.eHS)+1024) {
			System.out.println("Memory Overflow: Stack Segment");
			bPowerOn = false;
			return;
		}
		long operand1 = (instruction >> 16) & 0xFFFF;
		ERegisters targetAC; 
		if(operand1==99) targetAC = ERegisters.eFP;
		else targetAC = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand1)]; 
		
		move(ERegisters.eMBR, targetAC);
		bus.store(EDeviceId.eMemory, (int) (get(ERegisters.eSP)), get(ERegisters.eMBR));
		System.out.println("PUSH: "+targetAC+"("+get(targetAC)+") at offset "+(get(ERegisters.eSP)-get(ERegisters.eSS)));
		set(ERegisters.eSP, get(ERegisters.eSP)+4);
	}
	private void pop(long instruction) {
		if(get(ERegisters.eSP)<=get(ERegisters.eSS)) {
			System.out.println("Stack Segment is Empty");
			bPowerOn = false;
			return;
		}
		long operand1 = (instruction >> 16) & 0xFFFF;
		ERegisters targetAC = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand1)]; 
		set(ERegisters.eSP, get(ERegisters.eSP)-4);
		set(targetAC, bus.load(EDeviceId.eMemory, (int)get(ERegisters.eSP)));
		System.out.println("POP value: "+get(targetAC)+" to "+targetAC+". SP: "+(get(ERegisters.eSP)-get(ERegisters.eSS)));
	}
	private void storeObjectV(long instruction) {
		long operand1 = (instruction >> 16) & 0xFFFF; //저장할 offset이 있는 레지스터
		long operand2 = instruction & 0xFFFF; //저장할 값이 있는 레지스터
		ERegisters targetAC1 = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand1)];
		ERegisters targetAC2 = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand2)];
		move(ERegisters.eMBR, targetAC2);
		bus.store(EDeviceId.eMemory, (int) (get(ERegisters.eHS)+get(targetAC1)), get(ERegisters.eMBR));
		System.out.println("STAO: MAR:"+get(targetAC1)+" MBR: "+get(ERegisters.eMBR));
	}
	private void loadObjectV(long instruction) {
		long operand1 = (instruction >> 16) & 0xFFFF; // 값을 가져올 레지스터
		long operand2 = instruction & 0xFFFF; //가져올 값이 있는 offset
		ERegisters targetAC = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand1)];
		
		int objectAddress = ((int)get(ERegisters.eSP)-16);
		set(ERegisters.eMBR, bus.load(EDeviceId.eMemory, objectAddress)); //객체주소
		
		int heapAddress = (int)get(ERegisters.eHS);
		int variableAddress = (int) (heapAddress+get(ERegisters.eMBR)+operand2);
		set(targetAC, bus.load(EDeviceId.eMemory, variableAddress));
		System.out.println("LDAO: address("+(int)operand2+") value: "+get(targetAC)+" to "+targetAC);
	}
	private void jumpRegister(long instruction) {
		long operand1 = (instruction >> 16) & 0xFFFF;
		ERegisters targetAC = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand1)];
		long targetLine = get(targetAC) * instructionSize;
		System.out.println("JMPR: goto "+(int) get(targetAC)+" line");
		set(ERegisters.ePC, get(ERegisters.eCS)+targetLine);
	}
	private void loadMemoryInregister(long instruction) {
		long operand1 = (instruction >> 16) & 0xFFFF; 
		long operand2 = instruction & 0xFFFF; 
		ERegisters targetAC1 = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand1)];
		ERegisters targetAC2 = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand2)];
		
		set(ERegisters.eMBR, bus.load(EDeviceId.eMemory, (int)get(targetAC2))); 
		move(targetAC1, ERegisters.eMBR);
		System.out.println("MOVEA: set "+targetAC1+" for "+get(targetAC1));
	}
	private void storeMemoryInregister(long instruction) {
		long operand1 = (instruction >> 16) & 0xFFFF;
		long operand2 = instruction & 0xFFFF;
		ERegisters targetAC1, targetAC2; 
		if(operand1==99) targetAC1 = ERegisters.eFP;
		else if(operand1==100) targetAC1 = ERegisters.eSP;
		else targetAC1 = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand1)]; 
		
		if(operand2==99) targetAC2 = ERegisters.eFP;
		else if(operand2==100) targetAC2 = ERegisters.eSP;
		else targetAC2 = ERegisters.values()[(int) (ERegisters.eR0.ordinal() + operand2)]; 
		
		set(ERegisters.eMBR, bus.load(EDeviceId.eMemory, (int)get(targetAC2))); 
		move(ERegisters.eMBR, targetAC2);
		move(ERegisters.eMAR, targetAC1);
		bus.store(EDeviceId.eMemory, (int) (get(ERegisters.eMAR)), get(ERegisters.eMBR));
		System.out.println("STOREA: store "+targetAC1+" for "+get(targetAC1));
	}
	private void execute() {
		System.out.println();
	}
	
	public boolean run(int i) {
		this.fetch();
		this.decode(i);
		this.execute();	
		return bPowerOn;
	}
	
	public void finish() {
		
	}
}