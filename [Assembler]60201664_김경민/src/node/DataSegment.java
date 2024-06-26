package node;
import lexicalAnalyzer.EKeyword;
import lexicalAnalyzer.LexicalAnalyzer;

public class DataSegment extends Node {
	
	private SymbolTable symbolTable;
	
	public DataSegment() {
	}
	public void associate(LexicalAnalyzer lexicalAnalyzer, SymbolTable symbolTable) {
		this.lexicalAnalyzer = lexicalAnalyzer;
		this.symbolTable = symbolTable;
	}

	@Override
	public String parse(String token) throws Exception {
		String name = this.lexicalAnalyzer.getToken();
		int offset = 0;
		while(!name.equals(EKeyword.eCode.getText())) {
			int size = Integer.parseInt(this.lexicalAnalyzer.getToken());
			
			SymbolEntity symbol = new SymbolEntity(name, SymbolEntity.EType.eData, size, offset);
			this.symbolTable.Add(symbol);
			
			name = this.lexicalAnalyzer.getToken();
			offset += size;
		}
		return name;
	}

	@Override
	public String generate() throws Exception {
		return null;
	}
}
