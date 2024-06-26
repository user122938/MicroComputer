package lexicalAnalyzer;
public enum EKeyword {
	eProgram(".program"),
	eHeader(".header"),
	eStack("stack"),
	eHeap("heap"),
	eData(".data"),
	eCode(".code"),
	eEnd(".end");

	private String text;
	private EKeyword(String text) {
		this.text = text;
	}
	public String getText() { return this.text; }
}