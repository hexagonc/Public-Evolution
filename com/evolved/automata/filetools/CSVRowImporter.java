package com.evolved.automata.filetools;

public interface CSVRowImporter {
	public void ImportCellData(String cell);
	public void ImportCellData(String cell, int row, int column);
}
