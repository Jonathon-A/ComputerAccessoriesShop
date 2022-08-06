package computeraccessoriesshop;

public final class User {

	// User properties
	private final int userID;
	private final String Username;
	private final String Name;
	private final Address Address;
	private final boolean AdminRole;

	// User constructor
	public User(int userID, String Username, String Name, int HouseNumber, String Postcode, String City,
			boolean AdminRole) {
		this.userID = userID;
		this.Username = Username;
		this.Name = Name;
		this.Address = new Address(HouseNumber, Postcode, City);
		this.AdminRole = AdminRole;
	}

	// Returns the ID of this user
	final public int getUserID() {
		return userID;
	}

	// Returns the name of this user
	final public String getName() {
		return Name;
	}

	// Returns the address of this user
	final public Address getAddress() {
		return Address;
	}

	// Returns the username of this user
	final public String getUsername() {
		return Username;
	}

	// Returns whether or not this user is an admin
	final public boolean isAdminRole() {
		return AdminRole;
	}
}
