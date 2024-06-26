package node;

import java.util.Vector;

import codeGenerator.CodeGenerator;
import lexicalAnalyzer.EKeyword;
import lexicalAnalyzer.LexicalAnalyzer;

public class Program extends Node {
	
	private String name;
	private SymbolTable symbolTable;
	private CodeGenerator codeGenerator;
	private HeaderSegment headerSegment;
	private DataSegment dataSegment;
	private CodeSegment codeSegment;
	private Vector<String> binaryLines;
	
	public Program() {
		super();
		this.binaryLines = new Vector<>();
	}
	public Vector<String> getBinaryLines() {return binaryLines;}

	public void associate(LexicalAnalyzer lexicalAnalyzer, CodeGenerator codeGenerator, SymbolTable symbolTable) {
		this.lexicalAnalyzer = lexicalAnalyzer;
		this.codeGenerator = codeGenerator;
		this.symbolTable = symbolTable;
	}	
	
	@Override
	public String parse(String token) throws Exception {
		this.name = this.lexicalAnalyzer.getToken();
		token = this.lexicalAnalyzer.getToken();
		if(token.equals(EKeyword.eHeader.getText())) {
			headerSegment = new HeaderSegment();
			headerSegment.associate(this.lexicalAnalyzer);
			token = headerSegment.parse(token);
		} else {
			throw new Exception();
		}
		if(token.equals(EKeyword.eData.getText())) {
			dataSegment = new DataSegment();
			dataSegment.associate(this.lexicalAnalyzer, this.symbolTable);
			token = dataSegment.parse(token);
		}  else {
			throw new Exception();
		}
		if(token.equals(EKeyword.eCode.getText())) {
			codeSegment = new CodeSegment();
			codeSegment.associate(this.lexicalAnalyzer, this.symbolTable, this.codeGenerator);
			token = codeSegment.parse(token);
		} else {
			throw new Exception();
		}
		return token;
	}

	@Override
	public String generate() throws Exception {
		String sshsSize = this.headerSegment.generate();
		this.dataSegment.generate();
		String csSize = this.codeSegment.generate();
		String segmentSize = showStatus(sshsSize, csSize);
		binaryLines = this.codeSegment.generateBinaryCode();
		return segmentSize;
	}
	
	private String showStatus(String sshsSize, String csSize) {
		System.out.println("-------------  PCB  -------------");
		System.out.println("Program Name: "+this.name);
		System.out.println("Stack Segment Size: "+sshsSize.split(" ")[0]);
		System.out.println("Heap Segment Size: "+sshsSize.split(" ")[1]);
		System.out.println("Code Segment Size: "+Integer.parseInt(csSize));
		System.out.println("Data Segment Size: "+this.symbolTable.getSymbolTable().size()*4);
		System.out.println("---------  SymbolTable  ---------");
		System.out.printf("  %-5s %10s   %s %s\n","name","type","size","offset");
		int index = 0;
		for(SymbolEntity symbol : this.symbolTable.getSymbolTable()) {
			System.out.printf("%d %-10s %-6s   %2s %5d\n", index, symbol.getName(), symbol.getType().toString(),
					symbol.getSize(), symbol.getOffset());
			index++;
		}
		String segmentSize = csSize+" "+
				Integer.toString(this.symbolTable.getSymbolTable().size())+" "+sshsSize;
		System.out.println("SegmentSizeList: "+segmentSize);
		System.out.println();
		return segmentSize;
	}
}