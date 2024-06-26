package node;

import lexicalAnalyzer.LexicalAnalyzer;

public class Instruction extends Node {
	public enum ECommand {
		halt("halt"),
		move("move"),
		add("add"),
		cmp("cmp"),
		jump("jump"),
		ge("ge"),
		push("push"),
		New("New"),
		call("call"),
		ret("ret"),
		min("min"),
		movea("movea"),
		storer("storer"),
		HALT("HALT"),
		LDA("LDA"),
		STA("STA"),
		ADD("ADD"),
		AND("AND"),
		JMP("JMP"),
		ZERO("ZERO"),
		BZ("BZ"),
		CMP("CMP"),
		NOT("NOT"),
		SHR("SHR"),
		MOVER("MOVER"),
		MOVEC("MOVEC"),
		PUSH("PUSH"),
		POP("POP"),
		STAO("STAO"),
		LDAO("LDAO"),
		JMPR("JMPR"),
		MOVEA("MOVEA"),
		STORER("STORER"),
		label("label"),
		end(".end");
		
		private String text;
		private ECommand(String text) {this.text = text;}
		public String getText() { return this.text; }
		
	    public static ECommand fromString(String command) {
	        if (command == null) { return null; } 
	        else if(command.contains(":")) { return ECommand.label; }
	        try { return ECommand.valueOf(command); } 
	        catch (IllegalArgumentException e) { return null; }
	    }
	}
	private SymbolTable symbolTable;
	private ECommand eCommand;
	private String label;
	private String operand[];

	public Instruction(LexicalAnalyzer lexicalAnalyzer, SymbolTable symbolTable) {
		super(lexicalAnalyzer);
		this.symbolTable = symbolTable;
		this.operand = new String[2];
		label = null;
	}
	public Instruction(LexicalAnalyzer lexicalAnalyzer, SymbolTable symbolTable, ECommand opcode, String operand) {
		super(lexicalAnalyzer);
		this.symbolTable = symbolTable;
		this.eCommand = opcode;
		this.operand = new String[2];
		this.operand[0] = operand;
		label = null;
	}
	public Instruction(LexicalAnalyzer lexicalAnalyzer,SymbolTable symbolTable, ECommand opcode, String operand1, String operand2) {
		super(lexicalAnalyzer);
		this.symbolTable = symbolTable;
		this.eCommand = opcode;
		this.operand = new String[2];
		this.operand[0] = operand1;
		this.operand[1] = operand2;
		label = null;
	}
	
	public ECommand geteCommand() {return this.eCommand;}
	public void seteCommand(ECommand eCommand) {this.eCommand = eCommand;}
	public String[] getOperand() {return this.operand;}
	public void setOperand(String[] operand) {this.operand = operand;}
	public String getLabel() {return label;}
	
	@Override
	public String parse(String token) throws Exception {
		if(token.contains(":")) {
			this.eCommand = ECommand.label;
			this.label = token.replace(":","");
	    	return this.lexicalAnalyzer.getToken();
	    }
		this.eCommand = ECommand.valueOf(token);
		String nextToken = this.lexicalAnalyzer.getToken();
		ECommand eNextCommand = ECommand.fromString(nextToken);
		if(nextToken.equals(ECommand.end.getText()) || eNextCommand==ECommand.label) {
			return nextToken;
		}
		for (int i = 0; i < 2; i++) {
		    this.operand[i] = nextToken;
		    nextToken = this.lexicalAnalyzer.getToken();
		    eNextCommand = ECommand.fromString(nextToken);
		    
		    if (eNextCommand != null) {
		        break;
		    }
		}
		return nextToken;
	}
	
	@Override
	public String generate() throws Exception {
		if(this.eCommand==ECommand.label) {return null;}
		System.out.println(this.eCommand+" "+this.operand[0]+" "+this.operand[1]);
		String binaryLine = "";
		if(this.eCommand==ECommand.move) {
			if(isRegister(this.operand[0]) && searchSymbolTable(this.symbolTable, SymbolEntity.EType.eData, this.operand[1])) {
				this.eCommand = ECommand.LDA;
			} else if(searchSymbolTable(this.symbolTable, SymbolEntity.EType.eData, this.operand[0]) && isRegister(this.operand[1])) {
				this.eCommand = ECommand.STA;
			} else if(isRegister(this.operand[0]) && isInteger(this.operand[1])) {
				this.eCommand = ECommand.MOVEC;
			} else if(isRegister(this.operand[0]) && this.operand[1].charAt(0)=='o') {
				this.eCommand = ECommand.LDAO;
			} else if(isRegister(this.operand[0]) || isRegister(this.operand[1])) {
				this.eCommand = ECommand.MOVER;
			} 
		}
		if(this.eCommand==ECommand.storer) {this.eCommand = ECommand.STORER;}
		if(this.eCommand==ECommand.movea) {this.eCommand = ECommand.MOVEA;}
		if(this.eCommand==ECommand.add) {this.eCommand = ECommand.ADD;}
		if(this.eCommand==ECommand.push) {this.eCommand = ECommand.PUSH;}
		if(this.eCommand==ECommand.jump) {this.eCommand = ECommand.JMP;}
		if(this.eCommand==ECommand.halt) {this.eCommand = ECommand.HALT;}
		binaryLine += this.eCommand.toString()+" ";
		for (int i = 0; i < 2; i++) {
			String operand = this.operand[i];
			if(operand != null) {
				if(operand.charAt(0)=='r' || operand.charAt(0)=='o') {
					binaryLine += operand.substring(1)+" ";
				} else if(searchSymbolTable(this.symbolTable, SymbolEntity.EType.eData, operand)) {
					binaryLine += this.symbolTable.getOffset(SymbolEntity.EType.eData, operand)+" ";
				} else if(searchSymbolTable(this.symbolTable, SymbolEntity.EType.eLabel, operand)) {
					binaryLine += this.symbolTable.getOffset(SymbolEntity.EType.eLabel, operand)+" ";
				} else if(isInteger(operand)) {
					binaryLine += operand+" ";
				} else if(operand.equals("fp") || operand.equals("sp")) {
					binaryLine += operand+" ";
				}
			} else {
				binaryLine += "0000 ";
			}
		}
		System.out.println("=> "+binaryLine);
		return binaryLine;
	}
	
	public int extensionType(SymbolTable symbolTable) {
		if(this.eCommand==ECommand.New) {
			return 7;
		}
		if(this.eCommand==ECommand.call) {
			return 8; //call label
		}
		if(this.eCommand==ECommand.ret) {
			return 9;
		}
		String operand1 = this.operand[0];
		String operand2 = this.operand[1];
		if(operand1!=null && operand2!=null) {
			if(searchSymbolTable(symbolTable, SymbolEntity.EType.eData, operand1)) {
				if(isInteger(operand2) && this.eCommand==ECommand.move) {
					return 1; // move memory constant
				}
				if(searchSymbolTable(symbolTable, SymbolEntity.EType.eData, operand2) && this.eCommand==ECommand.cmp) {
					return 2; // cmp memory memory 
				}
			}
			if(operand1.charAt(0)=='r') {
				if(isInteger(operand2) && this.eCommand==ECommand.add) {
					return 3; // add register constant
				}
				if(searchSymbolTable(symbolTable, SymbolEntity.EType.eData, operand2) && this.eCommand==ECommand.add) {
					return 4; // add register memory
				}
				if(isInteger(operand2) && this.eCommand==ECommand.min) {
					return 10; // min register constant
				}
			}
		}
		if(operand1!=null && operand2==null) {
			if(this.eCommand==ECommand.ge) {
				return 5; // ge label
			}
			if(this.eCommand==ECommand.push && searchSymbolTable(symbolTable, SymbolEntity.EType.eData, operand1)) {
				return 6; //push memory
			}
		}
		return 0;
	}
	private boolean searchSymbolTable(SymbolTable symbolTable, SymbolEntity.EType type, String operand) {
		for(SymbolEntity entity : symbolTable.getSymbolTable()) {
			if(entity.getType()==type && entity.getName().equals(operand)) {
				return true; 
			}
		}
		return false;
	}
	private boolean isInteger(String operand) {
	  for (int i = 0; i < operand.length(); i++) {
	    if (!Character.isDigit(operand.charAt(i))) {
	      return false;
	    }
	  }
	  return true;
	}
	private boolean isRegister(String operand) {
		if(operand.charAt(0)=='r' || operand.equals("fp") || operand.equals("sp")) return true;
		return false;
	}
}