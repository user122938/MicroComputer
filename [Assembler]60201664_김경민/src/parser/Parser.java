package parser;

import codeGenerator.CodeGenerator;
import lexicalAnalyzer.EKeyword;
import lexicalAnalyzer.LexicalAnalyzer;
import node.Node;
import node.Program;
import node.SymbolTable;

public class Parser extends Node {
	private Program program;
	private SymbolTable symbolTable;
	private CodeGenerator codeGenerator;
	
	public Parser() {
		super();
		this.symbolTable = new SymbolTable();
	}
	
	public void associate(LexicalAnalyzer lexicalAnalyzer, CodeGenerator codeGenerator) {
		this.lexicalAnalyzer = lexicalAnalyzer;
		this.codeGenerator = codeGenerator;
	}
	
	public Program getProgram() {return program;}
	public SymbolTable getSymbolTable() {return symbolTable;}

	@Override
	public String parse(String token) throws Exception {
		token = this.lexicalAnalyzer.getToken();
		if(token.equals(EKeyword.eProgram.getText())) {
			this.program = new Program();
			this.program.associate(this.lexicalAnalyzer, this.codeGenerator, this.symbolTable);
			this.program.parse(token);
		}
		return token;
	}

	@Override
	public String generate() throws Exception {
		return null;
	}

	public void initialize() {
		
	}

	public void finish() {
		
	}


}