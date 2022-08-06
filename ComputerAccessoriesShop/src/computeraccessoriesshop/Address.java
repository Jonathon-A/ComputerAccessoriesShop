package computeraccessoriesshop;

public final class Address {

	// Address properties
	private final int HouseNumber;
	private final String Postcode;
	private final String City;

	// Address constructor
	public Address(int HouseNumber, String Postcode, String City) {
		this.HouseNumber = HouseNumber;
		this.Postcode = Postcode;
		this.City = City;
	}

	// Returns the address as a single string
	@Override
	final public String toString() {
		return HouseNumber + " " + Postcode + " " + City;
	}
}
