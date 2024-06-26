package node;
import java.util.Vector;

import codeGenerator.CodeGenerator;
import lexicalAnalyzer.LexicalAnalyzer;

public class CodeSegment extends Node {
	private Vector<Instruction> instructions;
	private Vector<String> binaryInstructions;
	private SymbolTable symbolTable;
	private CodeGenerator codeGenerator;
	
	public Vector<Instruction> getInstructions() {return instructions;}


	public CodeSegment() {
		this.instructions = new Vector<>();
		this.binaryInstructions = new Vector<>();
	}
	public void associate(LexicalAnalyzer lexicalAnalyzer, SymbolTable symbolTable, CodeGenerator codeGenerator) {
		this.lexicalAnalyzer = lexicalAnalyzer;
		this.symbolTable = symbolTable;
		this.codeGenerator = codeGenerator;
	}
	
	@Override
	public String parse(String token) throws Exception {
		int labelIndex = 0;
		String command = this.lexicalAnalyzer.getToken();
		
		Instruction.ECommand eCommand = Instruction.ECommand.valueOf(command);
		while(eCommand != null && eCommand != Instruction.ECommand.end) {
			Instruction instruction = new Instruction(this.lexicalAnalyzer, this.symbolTable);
			command = instruction.parse(command);
			eCommand = Instruction.ECommand.fromString(command);
			instructions.add(instruction);
			if(instruction.geteCommand() == Instruction.ECommand.label) {
				SymbolEntity entity = new SymbolEntity(instruction.getLabel(), SymbolEntity.EType.eLabel, 4, labelIndex); 
				this.symbolTable.Add(entity);
				labelIndex++;
			}
		}
		return command;
	}

	@Override
	public String generate() throws Exception {
		for(int i = 0; i<instructions.size(); i++) {
			if(instructions.get(i).geteCommand()==Instruction.ECommand.label) {
				this.symbolTable.setLabelOffset(instructions.get(i).getLabel(), i);
				instructions.remove(i--);
			} 
			else {
				int num = instructions.get(i).extensionType(this.symbolTable);
				if(num>0) {
					Vector<Instruction> lines = this.codeGenerator.macroExtension(num, instructions.get(i), i);
					instructions.remove(i);
					for(int index = 0; index<lines.size(); index++) {
						instructions.add(i+index, lines.get(index));
					}
				}
			}
		}
		return Integer.toString((instructions.size())*8);
	}

	public Vector<String> generateBinaryCode() throws Exception {
		for(int i = 0; i<instructions.size(); i++) {
			System.out.print("line:"+i+" ");
			String line = instructions.get(i).generate();
			if(line!=null) this.binaryInstructions.add(line);
		}
		return binaryInstructions;
	}
}