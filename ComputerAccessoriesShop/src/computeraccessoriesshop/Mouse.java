package computeraccessoriesshop;

public final class Mouse extends Product implements ProductMethods {

	// Additional properties for Mouse class
	private final int Buttons;
	private final MouseType Type;

	// Mouse constructor
	public Mouse(int BarCode, String Brand, String Colour, ConnectivityType Connectivity, int Quantity, double OriginalCost,
			double RetailPrice, MouseType Type, int Buttons) {
		super(BarCode, Brand, Colour, Connectivity, Quantity, OriginalCost, RetailPrice);
		this.Buttons = Buttons;
		this.Type = Type;
	}

	// Returns the button count for this mouse
	final public int getButtons() {
		return Buttons;
	}

	// Returns a string array corresponding to the mouse's properties (used as row
	// inside tables)
	@Override
	final public String[] toStringArray(boolean IsAdmin) {
		if (IsAdmin) {
			return new String[] { String.valueOf(Barcode), "mouse", Type.getName(), Brand, Colour, Connectivity.getName(),
					String.valueOf(Quantity), String.valueOf(OriginalCost), String.valueOf(RetailPrice),
					String.valueOf(Buttons) };
		} else {
			return new String[] { String.valueOf(Barcode), "mouse", Type.getName(), Brand, Colour, Connectivity.getName(),
					String.valueOf(Quantity), String.valueOf(RetailPrice), String.valueOf(Buttons) };
		}
	}

	// Makes copy the Mouse object (to avoid object referencing between the same
	// product in the products and basket tables)
	@Override
	final public Mouse clone() {
		return new Mouse(Barcode, Brand, Colour, Connectivity, Quantity, OriginalCost, RetailPrice, Type, Buttons);
	}

	public static enum MouseType {
		STANDARD("standard"), GAMING("gaming");

		private final String Name;

		MouseType(String Name) {
			this.Name = Name;
		}

		// Gets name of mouse type enumerator
		final public String getName() {
			return this.Name;
		}
	}
}
