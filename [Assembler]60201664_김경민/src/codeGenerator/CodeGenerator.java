package codeGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import lexicalAnalyzer.LexicalAnalyzer;
import node.Instruction;
import node.Program;
import node.SymbolEntity.EType;
import node.SymbolTable;

public class CodeGenerator {
	public enum ERegisters {
		eR0("r0"),
		eR1("r1"),
		eR2("r2"),
		eR7("r7"),
		eR8("r8"),
		eSP("sp"),
		eFP("fp");
		
		private String registerNum;
		private ERegisters(String registerNum) {this.registerNum = registerNum;}
		public String getRegister() { return this.registerNum; }
	}
	private LexicalAnalyzer lexicalAnalyzer;
	private SymbolTable symbolTable;
	private Hashtable<String,Integer> opcodeTable;
	private File file;
	
	public CodeGenerator(String fileName) {
		file = new File(fileName);
		
		this.opcodeTable = new Hashtable<>();
		opcodeTable.put("HALT", 0);
		opcodeTable.put("LDA", 1);
		opcodeTable.put("STA", 2);
		opcodeTable.put("ADD", 3);
		opcodeTable.put("AND", 4);
		opcodeTable.put("JMP", 5);
		opcodeTable.put("ZERO", 6);
		opcodeTable.put("BZ", 7);
		opcodeTable.put("CMP", 8);
		opcodeTable.put("NOT", 9);
		opcodeTable.put("SHR", 10);
		opcodeTable.put("MOVER", 11);
		opcodeTable.put("MOVEC", 12);
		opcodeTable.put("PUSH", 13);
		opcodeTable.put("POP", 14);
		opcodeTable.put("STAO", 15);
		opcodeTable.put("LDAO", 16);
		opcodeTable.put("JMPR", 17);
		opcodeTable.put("MOVEA", 18);
		opcodeTable.put("STORER", 19);
	}
	public void associate(LexicalAnalyzer lexicalAnalyzer, SymbolTable symbolTable) {
		this.lexicalAnalyzer = lexicalAnalyzer;
		this.symbolTable = symbolTable;
	}
	
	public void codeGeneration(Program program) throws Exception {
		Vector<String> result = new Vector<>();
		
		String segmentSize = program.generate();
        StringTokenizer tokenizer = new StringTokenizer(segmentSize);
        while (tokenizer.hasMoreTokens()) {
            int number = Integer.parseInt(tokenizer.nextToken());
            String hexString = Integer.toHexString(number); 
            hexString = String.format("%012x", number);
            
            result.add(hexString);
        }
		Vector<String> instructions = program.getBinaryLines();
		for(int i=0; i<instructions.size(); i++) {
			String[] iArr = instructions.get(i).split(" ");
			String opcode = iArr[0];
			String binaryline = Integer.toString(this.opcodeTable.get(opcode))+" ";
			for(int j=1; j<iArr.length; j++) {
				binaryline += iArr[j]+" ";
			}
			instructions.set(i, binaryline);
		}
		
        for (String instruction : instructions) {
        	String[] numbers = instruction.split(" ");
            StringBuilder hexLine = new StringBuilder();
            for (String number : numbers) {
            	int decimalValue = 0;
            	if(number.equals("fp")) decimalValue = 99;
            	else if(number.equals("sp")) decimalValue = 100;
            	else decimalValue = Integer.parseInt(number);
                String hexValue = String.format("%04x", decimalValue);
                hexLine.append(hexValue);
            }
            result.add(hexLine.toString().trim());
        }
        for (String hexLine : result) {
            System.out.println(hexLine);
        }
        makeBinaryFile(result);
	}
	
	private void makeBinaryFile(Vector<String> binaryStringV) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String str : binaryStringV) {
                writer.write(str);
                writer.newLine(); 
            }
            System.out.println("파일 작성 완료");
        } catch (IOException e) {e.printStackTrace();}
	}
	public Vector<Instruction> macroExtension(int extensionType, Instruction instruction, int line) {
		Vector<Instruction> extensionLines = new Vector<>();
		switch(extensionType) {
			case 1: // move memory constant
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable,
						Instruction.ECommand.PUSH, ERegisters.eR8.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.MOVEC, ERegisters.eR8.getRegister(), instruction.getOperand()[1]));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.STA, instruction.getOperand()[0], ERegisters.eR8.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.POP, ERegisters.eR8.getRegister()));
				break;
			case 2: // cmp memory memory 
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.PUSH, ERegisters.eR7.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.PUSH, ERegisters.eR8.getRegister()));
				
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.LDA, ERegisters.eR7.getRegister(), instruction.getOperand()[0]));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.LDA, ERegisters.eR8.getRegister(), instruction.getOperand()[1]));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.CMP, ERegisters.eR7.getRegister(), ERegisters.eR8.getRegister()));
				
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.POP, ERegisters.eR8.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.POP, ERegisters.eR7.getRegister()));
				break;
			case 3: // add register constant
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.PUSH, ERegisters.eR8.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.MOVEC, ERegisters.eR8.getRegister(), instruction.getOperand()[1]));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.ADD, instruction.getOperand()[0], ERegisters.eR8.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.POP, ERegisters.eR8.getRegister()));
				break;
			case 4: // add register memory
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.PUSH, ERegisters.eR8.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.LDA, ERegisters.eR8.getRegister(), instruction.getOperand()[1]));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.ADD, instruction.getOperand()[0], ERegisters.eR8.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.POP, ERegisters.eR8.getRegister()));
				break;
			case 5: // ge label
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.ZERO, instruction.getOperand()[0]));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.BZ, instruction.getOperand()[0]));
				break;
			case 6: // push memory
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.LDA, ERegisters.eR8.getRegister(), instruction.getOperand()[0]));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.PUSH, ERegisters.eR8.getRegister()));
				break;
			case 7: //new ObjectName
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.PUSH, ERegisters.eR0.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.PUSH, ERegisters.eR1.getRegister()));
				
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.MOVEC, ERegisters.eR0.getRegister(), this.symbolTable.getOffset(EType.eData, instruction.getOperand()[0])));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.MOVEC, ERegisters.eR1.getRegister(), Integer.toString(70)));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.STAO, ERegisters.eR0.getRegister(), ERegisters.eR1.getRegister()));
				
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.MOVEC, ERegisters.eR1.getRegister(), Integer.toString(4)));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.ADD, ERegisters.eR0.getRegister(), ERegisters.eR1.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.MOVEC, ERegisters.eR1.getRegister(), Integer.toString(80)));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.STAO, ERegisters.eR0.getRegister(), ERegisters.eR1.getRegister()));
				
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.POP, ERegisters.eR1.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.POP, ERegisters.eR0.getRegister()));
				break;
			case 8: //call functionName(label)
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.MOVEC, ERegisters.eR8.getRegister(), Integer.toString(line+4)));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.PUSH, ERegisters.eR8.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.PUSH, ERegisters.eFP.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.JMP, instruction.getOperand()[0]));
				break;
			case 9: //ret register
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.POP, ERegisters.eR0.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.POP, ERegisters.eR1.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.POP, ERegisters.eR2.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.MOVER, ERegisters.eSP.getRegister(), ERegisters.eR1.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.MOVER, ERegisters.eFP.getRegister(), ERegisters.eSP.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.JMPR, ERegisters.eR2.getRegister()));
				break;
			case 10: //min register constant
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.PUSH, ERegisters.eR8.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.MOVEC, ERegisters.eR8.getRegister(), instruction.getOperand()[1]));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.NOT, ERegisters.eR8.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.ADD, instruction.getOperand()[0], ERegisters.eR8.getRegister()));
				extensionLines.add(new Instruction(this.lexicalAnalyzer, this.symbolTable, 
						Instruction.ECommand.POP, ERegisters.eR8.getRegister()));
				break;
		}
		return extensionLines;
	}

	public void initialize() {
		
	}

	public void finish() {
		
	}
}