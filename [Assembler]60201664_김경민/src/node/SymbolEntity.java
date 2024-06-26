package node;

public class SymbolEntity {
	public enum EType {
		eData,
//		eObject,
		eLabel;
	}
	private String name;
	private EType type;
	private int size;
	private int offset;
	
	public SymbolEntity(String name, EType type, int size, int offset) {
		this.name = name;
		this.type = type;
		this.size = size;
		this.offset = offset;
	}
	
	public String getName() {return name;}
	public void setName(String name) {this.name = name;}
	public EType getType() {return type;}
	public void setType(EType type) {this.type = type;}
	public int getSize() {return size;}
	public void setSize(int size) {this.size = size;}
	public int getOffset() {return offset;}
	public void setOffset(int offset) {this.offset = offset;}
}
