package main;

import codeGenerator.CodeGenerator;
import lexicalAnalyzer.LexicalAnalyzer;
import parser.Parser;

public class Assembler { 
	private LexicalAnalyzer lexicalAnalyzer;
	private Parser parser;
	private CodeGenerator codeGenerator;
	
	public Assembler() {
		this.lexicalAnalyzer = new LexicalAnalyzer("test2");
		this.parser = new Parser();
		this.codeGenerator = new CodeGenerator("exe");
		
		this.parser.associate(this.lexicalAnalyzer, this.codeGenerator);
		this.codeGenerator.associate(lexicalAnalyzer, this.parser.getSymbolTable());
	}
	//methods
	public void initalize() {
		this.lexicalAnalyzer.initialize();
		this.parser.initialize();
		this.codeGenerator.initialize();
	}
	public void finish() {
		this.lexicalAnalyzer.finish();
		this.parser.finish();
		this.codeGenerator.finish();
	}
	
	public void run() {
		try {
			this.parser.parse(null);
			this.codeGenerator.codeGeneration(this.parser.getProgram());
		} catch (Exception e) { e.printStackTrace(); }
	}
}