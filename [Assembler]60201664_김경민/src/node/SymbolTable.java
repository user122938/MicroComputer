package node;

import java.util.Vector;

import node.SymbolEntity.EType;

public class SymbolTable {
	private Vector<SymbolEntity> symbolTable;
	
	public SymbolTable() {
		symbolTable = new Vector<>();
	}
	public Vector<SymbolEntity> getSymbolTable() {return symbolTable;}
	
	public void Add(SymbolEntity symbolEntity) {
		symbolTable.add(symbolEntity);
	}
	public void setLabelOffset(String targetLabel, int offset) {
		for(SymbolEntity entity : symbolTable) {
			if(entity.getType()==SymbolEntity.EType.eLabel && entity.getName().equals(targetLabel)) {
				entity.setOffset(offset);
			}
		}
	}
	public String getOffset(EType targetType, String symbolName) {
		String offset = null;
		for(SymbolEntity entity : symbolTable) {
			if(entity.getType()==targetType && entity.getName().equals(symbolName)) {
				offset = Integer.toString(entity.getOffset());
			}
		}
		return offset;
	}
	public int getLabelCount() {
		int labelCount = 0;
		for(SymbolEntity entity : symbolTable) {
			if(entity.getType()==SymbolEntity.EType.eLabel) {
				labelCount++;
			}
		}
		return labelCount;
	}
}