package computeraccessoriesshop;

public interface ProductMethods {

	// Override the clone method from Object class (this is then overridden by
	// Product, Mouse, and Keyboard classes)
	public abstract Product clone();

	// Abstract toStringArray method (this is overridden by Product, Mouse, and
	// Keyboard classes)
	public abstract String[] toStringArray(boolean IsAdmin);

}
