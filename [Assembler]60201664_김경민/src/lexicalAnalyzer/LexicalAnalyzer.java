package lexicalAnalyzer;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LexicalAnalyzer {
	
	private String fileName;
	private Scanner scanner;
	
	public LexicalAnalyzer(String fileName) {
		this.fileName = fileName;
	}
	public void initialize() {
		try {
			File file = new File("source/"+fileName+".txt");
			this.scanner = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public void finish() {
		scanner.close();
	}
	public String getToken() {
		if(this.scanner.hasNext()) {
			String token = this.scanner.next();
			return token;
		}
		return null;
	}
}