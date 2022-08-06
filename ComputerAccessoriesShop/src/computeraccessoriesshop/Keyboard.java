package computeraccessoriesshop;

public final class Keyboard extends Product implements ProductMethods {

	// Additional properties for Keyboard class
	private final String Layout;
	private final KeyboardType Type;

	// Keyboard constructor
	public Keyboard(int BarCode, String Brand, String Colour, ConnectivityType Connectivity, int Quantity, double OriginalCost,
			double RetailPrice, KeyboardType Type, String Layout) {
		super(BarCode, Brand, Colour, Connectivity, Quantity, OriginalCost, RetailPrice);
		this.Layout = Layout;
		this.Type = Type;
	}

	// Returns a string array corresponding to the keyboard's properties (used as
	// row inside tables)
	@Override
	final public String[] toStringArray(boolean IsAdmin) {
		if (IsAdmin) {
			return new String[] { String.valueOf(Barcode), "keyboard", Type.getName(), Brand, Colour, Connectivity.getName(),
					String.valueOf(Quantity), String.valueOf(OriginalCost), String.valueOf(RetailPrice), Layout };
		} else {
			return new String[] { String.valueOf(Barcode), "keyboard", Type.getName(), Brand, Colour, Connectivity.getName(),
					String.valueOf(Quantity), String.valueOf(RetailPrice), Layout };
		}
	}

	// Makes copy the keyboard object (to avoid object referencing between the same
	// product in the products and basket tables)
	@Override
	final public Keyboard clone() {
		return new Keyboard(Barcode, Brand, Colour, Connectivity, Quantity, OriginalCost, RetailPrice, Type, Layout);
	}

	public static enum KeyboardType {
		STANDARD("standard"), FLEXIBLE("flexible"), GAMING("gaming");

		private final String Name;

		KeyboardType(String Name) {
			this.Name = Name;
		}

		// Gets name of keyboard type enumerator
		final public String getName() {
			return this.Name;
		}
	}
}
