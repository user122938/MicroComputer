package node;

import lexicalAnalyzer.LexicalAnalyzer;

public abstract class Node {
	protected LexicalAnalyzer lexicalAnalyzer;
	
	public Node(LexicalAnalyzer lexicalAnalyzer) {
		this.lexicalAnalyzer = lexicalAnalyzer;
	}
	public Node() {
	}
	public abstract String parse(String token) throws Exception;
	public abstract String generate() throws Exception;
}