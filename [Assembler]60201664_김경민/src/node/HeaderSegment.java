package node;

import lexicalAnalyzer.EKeyword;
import lexicalAnalyzer.LexicalAnalyzer;

public class HeaderSegment extends Node {
	private int sizeStack;
	private int sizeHeap;
	
	public HeaderSegment() {
	}
	public void associate(LexicalAnalyzer lexicalAnalyzer) {
		this.lexicalAnalyzer = lexicalAnalyzer;
	}
	@Override
	public String parse(String token) throws Exception {
		String keyWord = this.lexicalAnalyzer.getToken();
		while(!keyWord.equals(EKeyword.eData.getText())) {
			String size = this.lexicalAnalyzer.getToken();
			if(keyWord.equals(EKeyword.eStack.getText())) {
				this.sizeStack = Integer.parseInt(size);
			} else if(keyWord.equals(EKeyword.eHeap.getText())) {
				this.sizeHeap = Integer.parseInt(size);
			} else {
				throw new Exception();
			}
			keyWord = this.lexicalAnalyzer.getToken();
		}
		return keyWord;
	}

	@Override
	public String generate() throws Exception {
		return Integer.toString(sizeStack)+" "+Integer.toString(sizeHeap);
	}
}