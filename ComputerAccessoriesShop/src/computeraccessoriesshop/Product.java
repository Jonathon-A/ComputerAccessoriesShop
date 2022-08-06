package computeraccessoriesshop;

public abstract class Product implements ProductMethods {

	// Product properties
	final protected int Barcode;
	final protected String Brand;
	final protected String Colour;
	final protected ConnectivityType Connectivity;
	protected int Quantity;
	final protected double OriginalCost;
	final protected double RetailPrice;

	// Product constructor
	public Product(int Barcode, String Brand, String Colour, ConnectivityType Connectivity, int Quantity, double OriginalCost,
			double RetailPrice) {
		this.Barcode = Barcode;
		this.Brand = Brand;
		this.Colour = Colour;
		this.Connectivity = Connectivity;
		this.Quantity = Quantity;
		this.OriginalCost = OriginalCost;
		this.RetailPrice = RetailPrice;
	}

	// Returns the quantity in stock for this product
	final public int getQuantity() {
		return Quantity;
	}

	// Sets the quantity in stock for this product
	final public void setQuantity(int Quantity) {
		this.Quantity = Quantity;
	}

	// Returns the barcode for this product
	final public int getBarcode() {
		return Barcode;
	}

	// Returns the brand of this product
	final public String getBrand() {
		return Brand;
	}

	// Returns the retail price of this product
	final public double getRetailPrice() {
		return RetailPrice;
	}

	// Override the clone method from Object class (this is then overridden by Mouse
	// and Keyboard classes)
	@Override
	public abstract Product clone();

	// Abstract toStringArray method (this is overridden by Mouse and Keyboard
	// classes)
	public abstract String[] toStringArray(boolean IsAdmin);
	
	public static enum ConnectivityType {
		WIRED("wired"), WIRELESS("wireless");

		private final String Name;

		ConnectivityType(String Name) {
			this.Name = Name;
		}

		// Gets name of connectivity type enumerator
		final public String getName() {
			return this.Name;
		}
	}
}
