package computeraccessoriesshop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultCellEditor;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public final class DataTable extends JScrollPane {

	// DataTable properties
	private Map<Integer, Product> BardcodeMap = new HashMap<>();
	private Map<Integer, Integer> RowMap = new HashMap<>();

	// DataTable constructor
	public DataTable(boolean IsAdmin, int xLoc, int yLoc, ArrayList<Product> FilteredProducts) {

		// Maps barcode to product object
		BardcodeMap = new HashMap<>();
		// Maps row index to barcode
		RowMap = new HashMap<>();
		// List of table data
		TableData = new ArrayList<>(FilteredProducts);
		// Table dimensions
		int xSize = 900;
		int ySize = 420;
		// Table columns
		String[] Columns;
		if (IsAdmin) {
			Columns = new String[] { "Barcode", "Device name", "Device type", "Brand", "Colour", "Connectivity",
					"Quantity", "Original cost", "Retail price", "Additional info" };
		} else {
			Columns = new String[] { "Barcode", "Device name", "Device type", "Brand", "Colour", "Connectivity",
					"Quantity", "Retail price", "Additional info" };
		}
		final String[][] Data = new String[FilteredProducts.size()][Columns.length];
		for (int i = 0; i < FilteredProducts.size(); i++) {
			// Fills in rows
			Data[i] = FilteredProducts.get(i).toStringArray(IsAdmin);
			// Fills in hash maps
			BardcodeMap.put(FilteredProducts.get(i).getBarcode(), FilteredProducts.get(i));
			RowMap.put(FilteredProducts.get(i).getBarcode(), i);
		}

		// Setup table cell properties
		final JTextField Cell = new JTextField();
		Cell.setEditable(false);
		Cell.setFont(Assets.StandardFont);
		Cell.setBackground(Assets.ComponentColour);
		DefaultCellEditor DCF = new DefaultCellEditor(Cell);
		// Setup table properties
		Table = new JTable(Data, Columns);
		Table.setDefaultEditor(Object.class, DCF);
		Table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		Table.setFont(Assets.StandardFont);
		Table.setBackground(Assets.ComponentColour);
		Table.getTableHeader().setFont(Assets.BoldFont);
		Table.getTableHeader().setBackground(Assets.ComponentColour);
		Table.getTableHeader().setReorderingAllowed(false);
		setViewportView(Table);
		setBounds(xLoc, yLoc, xSize, ySize);
		getViewport().setBackground(Assets.ComponentColour);
		setBorder(Assets.LineBorder);
	}

	private JTable Table = new JTable();
	private ArrayList<Product> TableData = new ArrayList<>();

	// Returns an array of selected row indexes
	final public int[] getSelectedRows() {
		return Table.getSelectedRows();
	}

	// Returns the product at a particular row
	final public Product getProductAtRow(int Row) {
		// Returns null if product out of bounds
		if (Row < 0 || Row >= Table.getRowCount()) {
			return null;
		}
		return TableData.get(Row);
	}

	// Returns the row which contains the product with specified barcode
	final public int getRow(int Barcode) {
		return RowMap.get(Barcode);
	}

	// Sets product quantity at a particular row without refreshing the whole table
	final public void SetTableQuantity(int newQuantity, int row) {
		Table.getModel().setValueAt(String.valueOf(newQuantity), row, 6);
	}

	// Returns the product with specified barcode
	final public Product GetExistingProduct(int BarCode) {
		return BardcodeMap.get(BarCode);
	}
}
