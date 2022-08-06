package computeraccessoriesshop;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;

public final class ComputerAccessoriesShop {

	public static void main(String[] args) {
		// Stops GUI from scaling
		System.setProperty("sun.java2d.uiScale", "1");
		// System startup
		ComputerAccessoriesShop Main = new ComputerAccessoriesShop();
		Main.GetProductsFromFile();
		Main.GetUsersFromFile();
		Main.GUISetup();
		// Starts message removal loop in separate thread
		Main.RemoveMessage();
	}

	final private JLayeredPane Panel = new JLayeredPane();
	final private JFrame Window = new JFrame();

	private final ArrayList<User> AllUsers = new ArrayList<>();
	private User CurrentUser = null;

	private final TrieTreeNode TrieTree = new TrieTreeNode();

	final private void GetUsersFromFile() {

		try {
			// Reads every line from UserAccounts.txt
			String inputLine;
			BufferedReader read = new BufferedReader(new FileReader(Assets.UserAccountDir));
			while ((inputLine = read.readLine()) != null) {
				// Splits the line at every occurrence of ", "
				inputLine = inputLine.trim();
				String[] UserDetails = inputLine.split(", ");
				// Instantiates new user
				AllUsers.add(new User(Integer.valueOf(UserDetails[0]), // User ID
						UserDetails[1], // Username
						UserDetails[2], // Name
						Integer.valueOf(UserDetails[3]), // House number
						UserDetails[4], // Postcode
						UserDetails[5], // City
						UserDetails[6].equals("admin"))); // Admin role
			}
			read.close();
			// If users has been found
			if (!AllUsers.isEmpty()) {
				// Sets current user to first user found
				CurrentUser = AllUsers.get(0);
			}
		} catch (IOException e) {
			// Error catching
			System.out.println("Error GetProductsFromFile : " + e);
		}
	}

	// List of all products
	private final ArrayList<Product> AllProducts = new ArrayList<>();
	// List of all cloned products in basket
	private final ArrayList<Product> BasketProducts = new ArrayList<>();
	// Set of all barcodes (set so it can quickly be searched)
	private final Set<Integer> AllBarcodes = new HashSet<>();
	// Map of button count to mouses with that button count (Used when filtering by
	// button count - to avoid searching through array)
	private final Map<Integer, ArrayList<Product>> ButtonMap = new HashMap<>();

	final private void GetProductsFromFile() {

		try {
			// Reads every line from Stock.txt
			String inputLine;
			BufferedReader read = new BufferedReader(new FileReader(Assets.StockDir));
			while ((inputLine = read.readLine()) != null) {
				// Splits the line at every occurrence of ", "
				inputLine = inputLine.trim();
				String[] ProductDetails = inputLine.split(", ");

				//Finds corresponding ConnectivityType enum
				Product.ConnectivityType Connectivity = Product.ConnectivityType.WIRED;
				if (ProductDetails[5].equals("wireless")) {
					Connectivity = Product.ConnectivityType.WIRELESS;
				}

				// If product type is mouse
				if (ProductDetails[1].equals("mouse")) {
					// Instantiates new mouse
					Mouse newMouse = new Mouse(Integer.valueOf(ProductDetails[0]), // Barcode
							ProductDetails[3], // Brand
							ProductDetails[4], // Colour
							Connectivity, // Connectivity
							Integer.valueOf(ProductDetails[6]), // Quantity
							Double.valueOf(ProductDetails[7]), // OriginalCost
							Double.valueOf(ProductDetails[8]), // RetailPrice
							Mouse.MouseType.valueOf(ProductDetails[2].toUpperCase()), // ProductDetails[2], //Type
							Integer.valueOf(ProductDetails[9])); // Buttons
					// Adds new mouse to array of all products
					AllProducts.add(newMouse);
					// Adds barcode to set of all barcodes
					AllBarcodes.add(newMouse.getBarcode());
					// Adds brand to trie (prefix) tree
					TrieTree.Add(newMouse.getBrand().toLowerCase(), 0);
					// Adds new mouse to button map
					if (!ButtonMap.containsKey(newMouse.getButtons())) {
						ButtonMap.put(newMouse.getButtons(), new ArrayList<>());
					}
					ButtonMap.get(newMouse.getButtons()).add(newMouse);

				} // Otherwise (If product type is keyboard)
				else {
					// Instantiates new keyboard
					Keyboard newKeyboard = new Keyboard(Integer.valueOf(ProductDetails[0]), // Barcode
							ProductDetails[3], // Brand
							ProductDetails[4], // Colour
							Connectivity, // Connectivity
							Integer.valueOf(ProductDetails[6]), // Quantity
							Double.valueOf(ProductDetails[7]), // OriginalCost
							Double.valueOf(ProductDetails[8]), // RetailPrice
							Keyboard.KeyboardType.valueOf(ProductDetails[2].toUpperCase()), // Type
							ProductDetails[9]); // Layout
					// Adds new keyboard to array of all products
					AllProducts.add(newKeyboard);
					// Adds barcode to set of all barcodes
					AllBarcodes.add(newKeyboard.getBarcode());
					// Adds brand to trie (prefix) tree
					TrieTree.Add(newKeyboard.getBrand().toLowerCase(), 0);

				}
			}
			read.close();
		} catch (IOException e) {
			// Error catching
			System.out.println("Error GetProductsFromFile : " + e);
		}
		// Sorts all products by retail price in ascending order using the merge sort
		// algorithm
		MergeSort(AllProducts, 0, AllProducts.size() - 1);
	}

	final private void SaveProductsToFile() {

		try {
			// Empties Stock.txt
			FileWriter writeToFile = new FileWriter(Assets.StockDir, false);
			PrintWriter printToFile = new PrintWriter(writeToFile);
			// Writes all products to Stock.txt
			for (int i = 0; i < AllProducts.size(); i++) {

				String[] newLineArray = AllProducts.get(i).toStringArray(true);
				String newLine = newLineArray[0];
				for (int j = 1; j < newLineArray.length; j++) {
					newLine += ", " + newLineArray[j];
				}
				printToFile.println(newLine);
			}
			printToFile.close();
			writeToFile.close();
		} catch (IOException e) {
			// Error catching
			System.out.println("Error SaveProductsToFile : " + e);
		}
	}

	final private void AddProduct(Product NewProduct) {
		// Inserts product into list of all products
		InsertProduct(NewProduct);
		// Adds barcode to set of all barcodes
		AllBarcodes.add(NewProduct.getBarcode());
		// Adds brand to trie (prefix) tree
		TrieTree.Add(NewProduct.getBrand().toLowerCase(), 0);
		// If the product is a mouse
		if (NewProduct instanceof Mouse) {
			// Adds mouse to button map
			if (!ButtonMap.containsKey(((Mouse) NewProduct).getButtons())) {
				ButtonMap.put(((Mouse) NewProduct).getButtons(), new ArrayList<>());
			}
			ButtonMap.get(((Mouse) NewProduct).getButtons()).add(NewProduct);
			// Updates the mouse button drop down with the new options
			RefreshMouseButtonDropDownOptions();
		}
		// Filters the list of all products and displays them on products table
		FilterProducts();

		try {
			FileWriter writeToFile = new FileWriter(Assets.StockDir, true);
			PrintWriter printToFile = new PrintWriter(writeToFile);
			// Writes new product to Stock.txt
			String[] newLineArray = NewProduct.toStringArray(true);
			String newLine = newLineArray[0];
			for (int i = 1; i < newLineArray.length; i++) {
				newLine += ", " + newLineArray[i];
			}
			printToFile.println(newLine);
			printToFile.close();
			writeToFile.close();
		} catch (IOException e) {
			// Error catching
			System.out.println("Error AddProduct : " + e);
		}
	}

	private void MergeSort(ArrayList<Product> arr, int low, int high) {
		// Sorts products list by retail price in ascending order using the merge sort
		// algorithm
		if (low < high) {
			// Finds mid point in this section of the list
			int mid = low + (high - low) / 2;
			// Sorts the section left of the mid point
			MergeSort(arr, low, mid);
			// Sorts the section right of the mid point
			MergeSort(arr, mid + 1, high);
			// Merges the two sorted sections into one sorted section
			Merge(arr, low, mid, high);
		}
	}

	private void Merge(ArrayList<Product> arr, int low, int mid, int high) {

		int left = low;
		int right = mid + 1;
		// Temporary list to store merged products
		ArrayList<Product> temp = new ArrayList<>(high - low + 1);
		// Merges left and right sublists
		while (left <= mid && right <= high) {
			if (arr.get(left).getRetailPrice() < arr.get(right).getRetailPrice()) {
				temp.add(arr.get(left));
				left++;
			} else {
				temp.add(arr.get(right));
				right++;
			}
		}
		// Adds remaining values from left sublist
		while (left <= mid) {
			temp.add(arr.get(left));
			left++;
		}
		// Adds remaining values from right sublist
		while (right <= high) {
			temp.add(arr.get(right));
			right++;
		}
		// Copies products from the temporary list to the sorted list
		for (int i = low; i <= high; i++) {
			arr.set(i, temp.get(i - low));
		}
	}

	final private void InsertProduct(Product NewProduct) {
		// Gets index at which to insert the new product in order to maintain the
		// ascending retail price order
		int index = BinarySearchIndex(AllProducts, NewProduct.getRetailPrice(), 0, AllProducts.size() - 1);
		// Adds the new product at this index
		AllProducts.add(index, NewProduct);
	}

	final private int BinarySearchIndex(ArrayList<Product> arr, double value, int low, int high) {
		// Uses binary search to find the index at which to insert the new product in
		// order to maintain the ascending retail price order
		while (low <= high) {
			// Finds mid point in this section of the list
			int mid = low + (high - low) / 2;
			// If the retail value at the midpoint equals the new product's retail value
			if (arr.get(mid).getRetailPrice() == value) {
				// Returns the index of the midpoint
				return mid;
			}
			// Otherwise, if the retail value at the midpoint is greater than new product's
			// retail value
			if (arr.get(mid).getRetailPrice() > value) {
				// Search right of the midpoint
				high = mid - 1;
			} // Otherwise (if the retail value at the midpoint is lower than new product's
				// retail value)
			else {
				// Search left of the midpoint
				low = mid + 1;
			}
		}
		// If no product is found then return the low pointer
		return low;
	}

	final private void GUISetup() {

		// Sets Window properties
		Window.getContentPane().setLayout(null);
		Window.getContentPane().setBackground(Assets.WindowColour);
		Window.setResizable(true);
		Window.setLayout(null);
		// Sets taskbar icon image
		Window.setLocation(0, 0);
		try {
			BufferedImage IconImage = ImageIO.read(new File(Assets.IconImageDir));
			Window.setIconImage(IconImage);
		} catch (IOException ex) {
			System.out.println("Getting icon image error: " + ex);
		}
		// Program will close when window is closed
		Window.setDefaultCloseOperation(EXIT_ON_CLOSE);
		Window.setVisible(true);
		// GUI size properties
		final int xSize = 1850;
		final int ySize = 680;
		final int xOffset = Window.getInsets().left + Window.getInsets().right;
		final int yOffset = Window.getInsets().top + Window.getInsets().bottom;
		Window.setMinimumSize(new Dimension(xSize + xOffset, ySize + yOffset));
		// Sets Panel properties and adds it to the window
		Panel.setBackground(Assets.BackGroundColour);
		Panel.setBorder(Assets.LineBorder);
		Panel.setOpaque(true);
		Panel.setLayout(null);
		// Adds resize listener to window
		Window.addComponentListener(new ComponentAdapter() {
			// If the window is resized
			@Override
			public void componentResized(ComponentEvent e) {
				// Recentre the panel with small offset to account for title bar
				Panel.setBounds(((Window.getWidth() - xOffset) / 2 - xSize / 2),
						((Window.getHeight() - yOffset) / 2 - ySize / 2), xSize, ySize);
			}
		});
		// Initially centres the panel with small offset to account for title bar
		Panel.setBounds(((Window.getWidth() - xOffset) / 2 - xSize / 2),
				((Window.getHeight() - yOffset) / 2 - ySize / 2), xSize, ySize);
		Window.add(Panel);

		// Setup for transition animation
		TransitionPanelSetup(xSize, ySize);
		// Setup for tile image/text
		TitleSetup();
		// Setup for user selection drop down
		UserSelectionSetup();
		// Setup for search bar and button count drop down
		SearchGUISetup();
		// Sets products table title properties and adds it to the panel
		JTextField ProductsTitle = new JTextField();
		ProductsTitle.setBounds(20, 80, 900, 20);
		ProductsTitle.setFont(Assets.HeaderFont);
		ProductsTitle.setEditable(false);
		ProductsTitle.setText("Products:");
		ProductsTitle.setOpaque(false);
		ProductsTitle.setBorder(null);
		Panel.add(ProductsTitle);
		// If the current user is a customer
		if (CurrentUser == null || !CurrentUser.isAdminRole()) {
			// Setup GUI for customer
			CustomerGUISetup();
		} // Otherwise (if current user is an admin)
		else {
			// Setup GUI for Admin
			AdminGUISetup();
		}
	}

	final private void TitleSetup() {
		// Try to use image as title
		try {
			// Setup title image properties and adds it to panel
			BufferedImage TitleImage = ImageIO.read(new File(Assets.TitleImageDir));
			JLabel Title = new JLabel();
			Title.setBounds(435, 15, 1000, 60);
			Title.setIcon(new ImageIcon(TitleImage));
			Panel.add(Title);

		} // If error getting image than use text
		catch (IOException ex) {
			System.out.println("ERROR: Title Image: " + ex);
			// Setup title text properties and adds it to panel
			JTextField Title = new JTextField();
			Title.setBounds(20, 0, 900, 70);
			Title.setFont(Assets.TitleFont);
			Title.setEditable(false);
			Title.setText("C.A.S Computer Accessories Shop");
			Title.setOpaque(false);
			Title.setBorder(Assets.SmallPaddingBorder);
			Panel.add(Title);
		}
	}

	final private void UserSelectionSetup() {
		// Setup welcome message properties and adds it to the panel
		JTextArea WelcomeMessage = new JTextArea();
		WelcomeMessage.setBounds(20, 20, 400, 60);
		WelcomeMessage.setFont(Assets.HeaderFont);
		WelcomeMessage.setEditable(false);
		if (CurrentUser != null) {
			String Message = "Welcome, " + CurrentUser.getName() + "!";
			if (CurrentUser.isAdminRole()) {
				Message += " (Admin)";
			}
			WelcomeMessage.setText(Message);
		} else {
			WelcomeMessage.setText("");
		}
		WelcomeMessage.setOpaque(false);
		WelcomeMessage.setBorder(Assets.PaddingBorder);
		Panel.add(WelcomeMessage);

		// Hash map of user ID to user object's index in array of all users
		Map<String, Integer> UserIDMap = new HashMap<>();
		String[] DropDownOptions = new String[AllUsers.size()];
		// Adds values for drop down options and user ID map
		for (int i = 0; i < AllUsers.size(); i++) {
			DropDownOptions[i] = "User: " + AllUsers.get(i).getUsername() + " (" + AllUsers.get(i).getUserID() + ")";
			UserIDMap.put(String.valueOf(AllUsers.get(i).getUserID()), i);
		}
		// Setup user selection properties and adds it to panel
		final JComboBox<String> UserSelectionDropDown = new JComboBox<>(DropDownOptions);
		UserSelectionDropDown.setBounds(1612, 20, 218, 60);
		UserSelectionDropDown.setFont(Assets.BoldFont);
		UserSelectionDropDown.setBackground(Assets.ComponentColour);
		UserSelectionDropDown
				.setBorder(BorderFactory.createCompoundBorder(Assets.RoundedLineBorder, Assets.SmallPaddingBorder));
		UserSelectionDropDown.setFocusable(false);
		UserSelectionDropDown.setUI(new BasicComboBoxUI());
		((JLabel) UserSelectionDropDown.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
		// Adds selection change listener to user selection combo box
		UserSelectionDropDown.addItemListener((ItemEvent e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				// Gets the selected user
				final String SelectedDropDown = String.valueOf(UserSelectionDropDown.getSelectedItem());
				// Gets user ID
				String UserID = "";
				for (int i = SelectedDropDown.length() - 2; i >= 0; i--) {
					if (SelectedDropDown.charAt(i) == '(') {
						break;
					}
					UserID = SelectedDropDown.charAt(i) + UserID;
				}
				CurrentUser = AllUsers.get(UserIDMap.get(UserID));
				// Updates welcome message
				if (CurrentUser != null) {
					String Message = "Welcome, " + CurrentUser.getName() + "!";
					if (CurrentUser.isAdminRole()) {
						Message += " (Admin)";
					}
					WelcomeMessage.setText(Message);
				} else {
					WelcomeMessage.setText("");
				}
				// Starts transition
				TransitionStart();
				// If the current user is a customer
				if (CurrentUser == null || !CurrentUser.isAdminRole()) {
					// Setup GUI for customer
					CustomerGUISetup();
				} // Otherwise (if current user is an admin)
				else {
					// Setup GUI for Admin
					AdminGUISetup();
				}
				// Ends transition
				TransitionEnd();
			}
		});
		Panel.add(UserSelectionDropDown);

	}

	private DataTable ProductsTable = null;

	final private void ProductsTableSetup(boolean Admin, ArrayList<Product> FilteredProducts) {
		// Removes previous products table
		if (ProductsTable != null) {
			Panel.remove(ProductsTable);
		}
		// Creates new products table
		ProductsTable = new DataTable(Admin, 20, 100, FilteredProducts);
		Panel.add(ProductsTable);
	}

	private DataTable BasketTable = null;

	final private void BasketTableSetup(ArrayList<Product> BasketProducts) {
		// Removes previous basket table
		if (BasketTable != null) {
			Panel.remove(BasketTable);
		}
		// Creates new basket table
		BasketTable = new DataTable(false, 930, 100, BasketProducts);
		Panel.add(BasketTable);
	}

	final private void ClearComponenets() {
		// Removes all components relating to admin GUI
		for (int i = 0; i < AdminComponents.size(); i++) {
			Panel.remove(AdminComponents.get(i));
		}
		AdminComponents.clear();
		// Removes all components relating to customer GUI
		for (int i = 0; i < CustomerComponents.size(); i++) {
			Panel.remove(CustomerComponents.get(i));
		}
		CustomerComponents.clear();
		// Removes previous basket table after emptying it
		if (BasketTable != null) {
			EmptyBasket();
			Panel.remove(BasketTable);
		}
		// Resets search bar
		SearchBar.setText("Search for brand...");
		SearchBar.setForeground(Assets.UnFocusedTextColor);
		SearchBar.setFocusable(false);
		SearchBar.setFocusable(true);

		// Resets button count drop down
		ButtonCountDropDown.setSelectedIndex(0);
	}

	final private void EmptyBasket() {
		// Loops through all products in basket table
		for (int i = 0; i < BasketProducts.size(); i++) {
			// Gets products from basket table
			Product BasketProduct = BasketTable.getProductAtRow(i);
			// Gets matching product from products table
			Product CurrentProduct = ProductsTable.GetExistingProduct(BasketProduct.getBarcode());
			// Increases the quantity of product in the proucts table by the quantity of
			// product in the basket table
			CurrentProduct.setQuantity(CurrentProduct.getQuantity() + BasketProduct.getQuantity());
			ProductsTable.SetTableQuantity(CurrentProduct.getQuantity(),
					ProductsTable.getRow(CurrentProduct.getBarcode()));
		}
		// Clears the list of all basket products
		BasketProducts.clear();
	}

	private final ArrayList<JComponent> CustomerComponents = new ArrayList<>();

	final private void CustomerGUISetup() {
		// Removes all components relating to admin and customer GUI
		ClearComponenets();
		// Creates products and basket table
		ProductsTableSetup(false, AllProducts);
		BasketTableSetup(BasketProducts);
		// Setup for user payment entry
		PaymentEntrySetup(true);

		JTextField BasketTitle = new JTextField();
		BasketTitle.setBounds(930, 80, 900, 20);
		BasketTitle.setFont(Assets.HeaderFont);
		BasketTitle.setEditable(false);
		BasketTitle.setText("Basket:");
		BasketTitle.setOpaque(false);
		BasketTitle.setBorder(null);
		// Adds the connect to database at the front most position
		Panel.add(BasketTitle);
		CustomerComponents.add(BasketTitle);

		// Setup add selected items button properties and adds it to panel
		final JButton AddSelectedItemButton = new JButton();
		AddSelectedItemButton.setBounds(930, 530, 217, 60);
		AddSelectedItemButton.setFont(Assets.BoldFont);
		AddSelectedItemButton.setText("Add selected Items");
		AddSelectedItemButton.setBackground(Assets.ComponentColour);
		AddSelectedItemButton.setBorder(Assets.RoundedLineBorder);
		AddSelectedItemButton.setFocusPainted(false);
		// Adds rollover listener to button that highlights it
		Assets.AddButtonHoverColorHighlight(AddSelectedItemButton);
		// Adds mouse press listener to button
		AddSelectedItemButton.addActionListener((final ActionEvent ae) -> {
			// Gets selected rows in products table
			int[] SelectedRows = ProductsTable.getSelectedRows();
			// If no rows have been selected
			if (SelectedRows.length == 0) {
				// Displays "No products selected."
				SetMessage("No products selected.");
			}
			// Loops through the selected rows
			for (int i = 0; i < SelectedRows.length; i++) {
				// Gets selected product
				Product SelectedProduct = ProductsTable.getProductAtRow(SelectedRows[i]);
				// If the selected product's quantity is greater than zero (still in stock)
				if (SelectedProduct != null && SelectedProduct.getQuantity() > 0) {
					// Reduces selected product's quantity by 1
					SelectedProduct.setQuantity(SelectedProduct.getQuantity() - 1);
					// Gets matching product from the basket table
					Product ExistingProduct = BasketTable.GetExistingProduct(SelectedProduct.getBarcode());
					// If this matching product does not exit
					if (ExistingProduct == null) {
						// Clones the product from the products table and adds it to the basket table
						Product NewItem = SelectedProduct.clone();
						NewItem.setQuantity(1);
						BasketProducts.add(NewItem);
						BasketTableSetup(BasketProducts);
					} else {
						// Increases matching product's quantity by 1
						ExistingProduct.setQuantity(ExistingProduct.getQuantity() + 1);
						BasketTable.SetTableQuantity(ExistingProduct.getQuantity(),
								BasketTable.getRow(ExistingProduct.getBarcode()));
					}
					ProductsTable.SetTableQuantity(SelectedProduct.getQuantity(), SelectedRows[i]);
				} // If the selected product's quantity is zero
				else {
					// Displays "One or more selected items are out of stock."
					SetMessage("One or more selected items are out of stock.");
				}
			}
		});
		Panel.add(AddSelectedItemButton);
		CustomerComponents.add(AddSelectedItemButton);

		// Setup empty basket button properties and adds it to panel
		final JButton EmptyBasketButton = new JButton();
		EmptyBasketButton.setBounds(1157, 530, 217, 60);
		EmptyBasketButton.setFont(Assets.BoldFont);
		EmptyBasketButton.setText("Empty Basket");
		EmptyBasketButton.setBackground(Assets.ComponentColour);
		EmptyBasketButton.setBorder(Assets.RoundedLineBorder);
		EmptyBasketButton.setFocusPainted(false);
		// Adds roll-over listener to button that highlights it
		Assets.AddButtonHoverColorHighlight(EmptyBasketButton);
		// Adds mouse press listener to button
		EmptyBasketButton.addActionListener((final ActionEvent ae) -> {
			// Empties basket table
			if (!BasketProducts.isEmpty()) {
				EmptyBasket();
				BasketTableSetup(BasketProducts);
			} else {
				// Displays "Basket already empty."
				SetMessage("Basket already empty.");
			}

		});
		Panel.add(EmptyBasketButton);
		CustomerComponents.add(EmptyBasketButton);

		// Setup payment method drop down properties and adds it to panel
		final String DropDownOptions[] = { "Payment by: PayPal", "Payment by: Credit Card" };
		final JComboBox<String> PaymentMethodDropDown = new JComboBox<>(DropDownOptions);
		PaymentMethodDropDown.setBounds(1384, 530, 218, 60);
		PaymentMethodDropDown.setFont(Assets.BoldFont);
		PaymentMethodDropDown.setBackground(Assets.ComponentColour);
		PaymentMethodDropDown
				.setBorder(BorderFactory.createCompoundBorder(Assets.RoundedLineBorder, Assets.SmallPaddingBorder));
		PaymentMethodDropDown.setFocusable(false);
		PaymentMethodDropDown.setUI(new BasicComboBoxUI());
		((JLabel) PaymentMethodDropDown.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
		// Adds selection change listener to payment method combo box
		PaymentMethodDropDown.addItemListener((ItemEvent e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				// Setup necessary payment entry components depending on the selected option
				final String SelectedDropDown = String.valueOf(PaymentMethodDropDown.getSelectedItem());
				PaymentEntrySetup(SelectedDropDown.equals(DropDownOptions[0]));
			}
		});
		Panel.add(PaymentMethodDropDown);
		CustomerComponents.add(PaymentMethodDropDown);

		// Setup add pay button properties and adds it to panel
		final JButton PayButton = new JButton();
		PayButton.setBounds(1612, 530, 218, 60);
		PayButton.setFont(Assets.BoldFont);
		PayButton.setText("Pay for items");
		PayButton.setBackground(Assets.ComponentColour);
		PayButton.setBorder(Assets.RoundedLineBorder);
		PayButton.setFocusPainted(false);
		// Adds roll-over listener to button that highlights it
		Assets.AddButtonHoverColorHighlight(PayButton);
		// Adds mouse press listener to button
		PayButton.addActionListener((final ActionEvent ae) -> {

			final String SelectedDropDown = String.valueOf(PaymentMethodDropDown.getSelectedItem());
			// If PayPal payment method selected
			if (SelectedDropDown.equals(DropDownOptions[0])) {
				// If valid email entered
				if (EmailValidationMessage.equals("Valid")) {
					// If basket is not empty
					if (!BasketProducts.isEmpty()) {
						// Pays for items
						PayForItems(true);
						//EmailEntry.setText("");
					} // Otherwise (if basket is empty)
					else {
						// Displays "Your basket is empty."
						SetMessage("Your basket is empty.");
					}
				} // Otherwise (if invalid email entered)
				else {
					// Displays what is invalid
					SetMessage(EmailValidationMessage);
				}
			} // Otherwise (if credit card payment method selected)
			else {
				// If valid card number and security code entered
				if (CardNumberValidationMessage.equals("Valid") && SercurityCodeValidationMessage.equals("Valid")) {
					// If basket is not empty
					if (!BasketProducts.isEmpty()) {
						// Pays for items
						PayForItems(false);
					} // Otherwise (if basket is empty)
					else {
						// Displays "Your basket is empty."
						SetMessage("Your basket is empty.");
					}
				} // Otherwise (if invalid card number or security code entered)
				else {
					// Displays what is invalid
					String ValidationMessage = "";
					if (!CardNumberValidationMessage.equals("Valid")) {
						ValidationMessage += CardNumberValidationMessage + ".\n";
					}
					if (!SercurityCodeValidationMessage.equals("Valid")) {
						ValidationMessage += SercurityCodeValidationMessage + ".";
					}
					SetMessage(ValidationMessage);
				}
			}
		});
		Panel.add(PayButton);
		CustomerComponents.add(PayButton);
		Panel.revalidate();
		Panel.repaint();
	}

	private final ArrayList<JComponent> AdminComponents = new ArrayList<>();

	final private void AdminGUISetup() {
		// Removes all components relating to admin and customer GUI
		ClearComponenets();
		// Creates products table
		ProductsTableSetup(true, AllProducts);
		// Setup components for creating a new product
		NewProductEntrySetup();
		Panel.revalidate();
		Panel.repaint();
	}

	final private void NewProductEntrySetup() {
		// Setup new product title properties and adds it to the panel
		JTextField NewProductTitle = new JTextField();
		NewProductTitle.setBounds(930, 80, 900, 20);
		NewProductTitle.setFont(Assets.HeaderFont);
		NewProductTitle.setEditable(false);
		NewProductTitle.setText("Add new product:");
		NewProductTitle.setOpaque(false);
		NewProductTitle.setBorder(null);
		Panel.add(NewProductTitle);
		AdminComponents.add(NewProductTitle);

		// boolean array corresponding to the validation of the 7 text fields
		final boolean[] ValidationArray = new boolean[7];

		// Setup barcode message properties and adds it to the panel
		JTextField BarcodeMessage = new JTextField();
		BarcodeMessage.setBounds(1485, 143, 345, 33);
		BarcodeMessage.setFont(Assets.StandardFont);
		BarcodeMessage.setEditable(false);
		BarcodeMessage.setText("");
		BarcodeMessage.setOpaque(false);
		BarcodeMessage.setBorder(Assets.SmallPaddingBorder);
		Panel.add(BarcodeMessage);
		AdminComponents.add(BarcodeMessage);

		// Setup barcode entry properties and adds it to the panel
		String BarcodeDefualtEntry = "Enter barcode...";
		JTextField BarcodeEntry = new JTextField();
		BarcodeEntry.setBorder(
				BorderFactory.createCompoundBorder(Assets.RoundedInvalidLineBorder, Assets.SmallPaddingBorder));
		BarcodeEntry.setBounds(930, 143, 545, 33);
		BarcodeEntry.setBackground(Assets.ComponentColour);
		BarcodeEntry.setForeground(Assets.UnFocusedTextColor);
		BarcodeEntry.setFont(Assets.StandardFont);
		BarcodeEntry.setText(BarcodeDefualtEntry);
		// Adds use listener to the component
		BarcodeEntry.getDocument().addDocumentListener(new DocumentListener() {
			// When a character is inserted into the entry
			@Override
			public void insertUpdate(DocumentEvent de) {
				// Validates entry
				boolean CorrectLength = BarcodeEntry.getText().length() == 6;
				boolean NotEmpty = isNotEmptyEntry(BarcodeEntry.getText(), BarcodeDefualtEntry);
				boolean PositiveInt = isPositiveInteger(BarcodeEntry.getText());
				boolean Unique = true;
				if (NotEmpty && CorrectLength && PositiveInt) {
					Unique = !AllBarcodes.contains(Integer.valueOf(BarcodeEntry.getText()));
				}
				ValidationArray[0] = CorrectLength && NotEmpty && PositiveInt && Unique;
				// Changes border and validation message based on entry validation
				if (ValidationArray[0]) {
					BarcodeEntry.setBorder(
							BorderFactory.createCompoundBorder(Assets.RoundedLineBorder, Assets.SmallPaddingBorder));
					BarcodeMessage.setText("");
				} else {
					BarcodeEntry.setBorder(BorderFactory.createCompoundBorder(Assets.RoundedInvalidLineBorder,
							Assets.SmallPaddingBorder));
					if (NotEmpty) {
						String Message = "";
						if (!CorrectLength) {
							Message += "Must be 6-digits. ";
						}
						if (!PositiveInt) {
							Message += "Must be a positive integer. ";
						}
						if (!Unique) {
							Message += "Must be an unique barcode. ";
						}
						BarcodeMessage.setText(Message);
					} else {
						BarcodeMessage.setText("");
					}
				}
			}

			// When a character is removed from the entry
			@Override
			public void removeUpdate(DocumentEvent de) {
				insertUpdate(de);
			}

			@Override
			public void changedUpdate(DocumentEvent de) {
			}
		});
		// Adds focus listener to the component
		BarcodeEntry.addFocusListener(new FocusListener() {
			// When the component gains focus
			@Override
			public void focusGained(FocusEvent focusEvent) {
				// If the entry text equals the default entry
				if (BarcodeEntry.getText().equals(BarcodeDefualtEntry)) {
					// Empties the entry
					BarcodeEntry.setText("");
					// Changes entry text colour to black
					BarcodeEntry.setForeground(Assets.TextColor);
				}

			}

			// When the component loses focus
			@Override
			public void focusLost(FocusEvent focusEvent) {
				// If the entry is empty
				if (BarcodeEntry.getText().isEmpty()) {
					// Entry text set to default entry
					BarcodeEntry.setText(BarcodeDefualtEntry);
					// Changes entry text colour to grey
					BarcodeEntry.setForeground(Assets.UnFocusedTextColor);
				}
			}
		});
		Panel.add(BarcodeEntry);
		AdminComponents.add(BarcodeEntry);

		// Setup brand entry properties and adds it to the panel
		String BrandDefualtEntry = "Enter brand...";
		JTextField BrandEntry = new JTextField();
		BrandEntry.setBorder(
				BorderFactory.createCompoundBorder(Assets.RoundedInvalidLineBorder, Assets.SmallPaddingBorder));
		BrandEntry.setBounds(930, 186, 545, 33);
		BrandEntry.setBackground(Assets.ComponentColour);
		BrandEntry.setForeground(Assets.UnFocusedTextColor);
		BrandEntry.setFont(Assets.StandardFont);
		BrandEntry.setText(BrandDefualtEntry);
		// Adds use listener to the component
		BrandEntry.getDocument().addDocumentListener(new DocumentListener() {
			// When a character is inserted into the entry
			@Override
			public void insertUpdate(DocumentEvent de) {
				// Validates entry
				ValidationArray[1] = isNotEmptyEntry(BrandEntry.getText(), BrandDefualtEntry);
				// Changes border and validation message based on entry validation
				if (ValidationArray[1]) {
					BrandEntry.setBorder(
							BorderFactory.createCompoundBorder(Assets.RoundedLineBorder, Assets.SmallPaddingBorder));
				} else {
					BrandEntry.setBorder(BorderFactory.createCompoundBorder(Assets.RoundedInvalidLineBorder,
							Assets.SmallPaddingBorder));
				}
			}

			// When a character is removed from the entry
			@Override
			public void removeUpdate(DocumentEvent de) {
				insertUpdate(de);
			}

			@Override
			public void changedUpdate(DocumentEvent de) {
			}
		});
		// Adds focus listener to the component
		BrandEntry.addFocusListener(new FocusListener() {
			// When the component gains focus
			@Override
			public void focusGained(FocusEvent focusEvent) {
				// If the entry text equals the default entry
				if (BrandEntry.getText().equals(BrandDefualtEntry)) {
					// Empties the entry
					BrandEntry.setText("");
					// Changes entry text colour to black
					BrandEntry.setForeground(Assets.TextColor);
				}

			}

			// When the component loses focus
			@Override
			public void focusLost(FocusEvent focusEvent) {
				// If the entry is empty
				if (BrandEntry.getText().isEmpty()) {
					// Entry text set to default entry
					BrandEntry.setText(BrandDefualtEntry);
					// Changes entry text colour to grey
					BrandEntry.setForeground(Assets.UnFocusedTextColor);
				}
			}
		});
		Panel.add(BrandEntry);
		AdminComponents.add(BrandEntry);

		// Setup colour entry properties and adds it to the panel
		String ColourDefualtEntry = "Enter colour...";
		JTextField ColourEntry = new JTextField();
		ColourEntry.setBorder(
				BorderFactory.createCompoundBorder(Assets.RoundedInvalidLineBorder, Assets.SmallPaddingBorder));
		ColourEntry.setBounds(930, 229, 545, 33);
		ColourEntry.setBackground(Assets.ComponentColour);
		ColourEntry.setForeground(Assets.UnFocusedTextColor);
		ColourEntry.setFont(Assets.StandardFont);
		ColourEntry.setText(ColourDefualtEntry);
		// Adds use listener to the component
		ColourEntry.getDocument().addDocumentListener(new DocumentListener() {
			// When a character is inserted into the entry
			@Override
			public void insertUpdate(DocumentEvent de) {
				// Validates entry
				ValidationArray[2] = isNotEmptyEntry(ColourEntry.getText(), ColourDefualtEntry);
				// Changes border and validation message based on entry validation
				if (ValidationArray[2]) {
					ColourEntry.setBorder(
							BorderFactory.createCompoundBorder(Assets.RoundedLineBorder, Assets.SmallPaddingBorder));
				} else {
					ColourEntry.setBorder(BorderFactory.createCompoundBorder(Assets.RoundedInvalidLineBorder,
							Assets.SmallPaddingBorder));
				}
			}

			// When a character is removed from the entry
			@Override
			public void removeUpdate(DocumentEvent de) {
				insertUpdate(de);
			}

			@Override
			public void changedUpdate(DocumentEvent de) {
			}
		});
		// Adds focus listener to the component
		ColourEntry.addFocusListener(new FocusListener() {
			// When the component gains focus
			@Override
			public void focusGained(FocusEvent focusEvent) {
				// If the entry text equals the default entry
				if (ColourEntry.getText().equals(ColourDefualtEntry)) {
					// Empties the entry
					ColourEntry.setText("");
					// Changes entry text colour to black
					ColourEntry.setForeground(Assets.TextColor);
				}
			}

			// When the component loses focus
			@Override
			public void focusLost(FocusEvent focusEvent) {
				// If the entry is empty
				if (ColourEntry.getText().isEmpty()) {
					// Entry text set to default entry
					ColourEntry.setText(ColourDefualtEntry);
					// Changes entry text colour to grey
					ColourEntry.setForeground(Assets.UnFocusedTextColor);
				}
			}
		});
		Panel.add(ColourEntry);
		AdminComponents.add(ColourEntry);

		// Setup connectivity drop down properties and adds it to the panel
		final String ConnectivityDropDownOptions[] = { "Connectivity: wired", "Connectivity: wireless" };
		final JComboBox<String> ConnectivityDropDown = new JComboBox<>(ConnectivityDropDownOptions);
		ConnectivityDropDown.setBounds(930, 272, 545, 33);
		ConnectivityDropDown.setFont(Assets.BoldFont);
		ConnectivityDropDown.setBackground(Assets.ComponentColour);
		ConnectivityDropDown
				.setBorder(BorderFactory.createCompoundBorder(Assets.RoundedLineBorder, Assets.SmallPaddingBorder));
		ConnectivityDropDown.setFocusable(false);
		ConnectivityDropDown.setUI(new BasicComboBoxUI());
		Panel.add(ConnectivityDropDown);
		AdminComponents.add(ConnectivityDropDown);

		// Setup quantity message properties and adds it to the panel
		JTextField QuantityMessage = new JTextField();
		QuantityMessage.setBounds(1485, 315, 345, 33);
		QuantityMessage.setFont(Assets.StandardFont);
		QuantityMessage.setEditable(false);
		QuantityMessage.setText("");
		QuantityMessage.setOpaque(false);
		QuantityMessage.setBorder(Assets.SmallPaddingBorder);
		// Adds the connect to database at the front most position
		Panel.add(QuantityMessage);
		AdminComponents.add(QuantityMessage);

		// Setup quantity entry properties and adds it to the panel
		String QuantityDefualtEntry = "Enter quantity...";
		JTextField QuantityEntry = new JTextField();
		QuantityEntry.setBorder(
				BorderFactory.createCompoundBorder(Assets.RoundedInvalidLineBorder, Assets.SmallPaddingBorder));
		QuantityEntry.setBounds(930, 315, 545, 33);
		QuantityEntry.setBackground(Assets.ComponentColour);
		QuantityEntry.setForeground(Assets.UnFocusedTextColor);
		QuantityEntry.setFont(Assets.StandardFont);
		QuantityEntry.setText(QuantityDefualtEntry);
		// Adds use listener to the component
		QuantityEntry.getDocument().addDocumentListener(new DocumentListener() {
			// When a character is inserted into the entry
			@Override
			// Validates entry
			public void insertUpdate(DocumentEvent de) {
				boolean NotEmpty = isNotEmptyEntry(QuantityEntry.getText(), QuantityDefualtEntry);
				boolean PositiveInt = isPositiveInteger(QuantityEntry.getText());
				ValidationArray[3] = NotEmpty && PositiveInt;
				// Changes border and validation message based on entry validation
				if (ValidationArray[3]) {
					QuantityEntry.setBorder(
							BorderFactory.createCompoundBorder(Assets.RoundedLineBorder, Assets.SmallPaddingBorder));
					QuantityMessage.setText("");
				} else {
					QuantityEntry.setBorder(BorderFactory.createCompoundBorder(Assets.RoundedInvalidLineBorder,
							Assets.SmallPaddingBorder));
					if (NotEmpty) {
						QuantityMessage.setText("Must be a positive integer.");
					} else {
						QuantityMessage.setText("");
					}
				}
			}

			// When a character is removed from the entry
			@Override
			public void removeUpdate(DocumentEvent de) {
				insertUpdate(de);
			}

			@Override
			public void changedUpdate(DocumentEvent de) {
			}
		});
		// Adds focus listener to the component
		QuantityEntry.addFocusListener(new FocusListener() {
			// When the component losses focus
			@Override
			public void focusGained(FocusEvent focusEvent) {
				// If the entry text equals the default entry
				if (QuantityEntry.getText().equals(QuantityDefualtEntry)) {
					// Empties the entry
					QuantityEntry.setText("");
					// Changes entry text colour to black
					QuantityEntry.setForeground(Assets.TextColor);
				}

			}

			// When the component loses focus
			@Override
			public void focusLost(FocusEvent focusEvent) {
				// If the entry is empty
				if (QuantityEntry.getText().isEmpty()) {
					// Entry text set to default entry
					QuantityEntry.setText(QuantityDefualtEntry);
					// Changes entry text colour to grey
					QuantityEntry.setForeground(Assets.UnFocusedTextColor);
				}
			}
		});

		Panel.add(QuantityEntry);
		AdminComponents.add(QuantityEntry);

		// Setup original cost message properties and adds it to the panel
		JTextField OriginalCostMessage = new JTextField();
		OriginalCostMessage.setBounds(1485, 358, 345, 33);
		OriginalCostMessage.setFont(Assets.StandardFont);
		OriginalCostMessage.setEditable(false);
		OriginalCostMessage.setText("");
		OriginalCostMessage.setOpaque(false);
		OriginalCostMessage.setBorder(Assets.SmallPaddingBorder);
		// Adds the connect to database at the front most position
		Panel.add(OriginalCostMessage);
		AdminComponents.add(OriginalCostMessage);

		// Setup original cost entry properties and adds it to the panel
		String OriginalCostDefualtEntry = "Enter original cost...";
		JTextField OriginalCostEntry = new JTextField();
		OriginalCostEntry.setBorder(
				BorderFactory.createCompoundBorder(Assets.RoundedInvalidLineBorder, Assets.SmallPaddingBorder));
		OriginalCostEntry.setBounds(930, 358, 545, 33);
		OriginalCostEntry.setBackground(Assets.ComponentColour);
		OriginalCostEntry.setForeground(Assets.UnFocusedTextColor);
		OriginalCostEntry.setFont(Assets.StandardFont);
		OriginalCostEntry.setText(OriginalCostDefualtEntry);
		// Adds use listener to the component
		OriginalCostEntry.getDocument().addDocumentListener(new DocumentListener() {
			// When a character is inserted into the entry
			@Override
			public void insertUpdate(DocumentEvent de) {
				// Validates entry
				boolean NotEmpty = isNotEmptyEntry(OriginalCostEntry.getText(), OriginalCostDefualtEntry);
				boolean PositiveDouble = isPositiveDouble(OriginalCostEntry.getText());
				ValidationArray[4] = NotEmpty && PositiveDouble;
				// Changes border and validation message based on entry validation
				if (ValidationArray[4]) {
					OriginalCostEntry.setBorder(
							BorderFactory.createCompoundBorder(Assets.RoundedLineBorder, Assets.SmallPaddingBorder));
					OriginalCostMessage.setText("");
				} else {
					OriginalCostEntry.setBorder(BorderFactory.createCompoundBorder(Assets.RoundedInvalidLineBorder,
							Assets.SmallPaddingBorder));
					if (NotEmpty) {
						OriginalCostMessage.setText("Must be a positive double.");
					} else {
						OriginalCostMessage.setText("");
					}
				}
			}

			// When a character is removed from the entry
			@Override
			public void removeUpdate(DocumentEvent de) {
				insertUpdate(de);
			}

			@Override
			public void changedUpdate(DocumentEvent de) {
			}
		});
		// Adds focus listener to the component
		OriginalCostEntry.addFocusListener(new FocusListener() {
			// When the component gains focus
			@Override
			public void focusGained(FocusEvent focusEvent) {
				// If the entry text equals the default entry
				if (OriginalCostEntry.getText().equals(OriginalCostDefualtEntry)) {
					// Empties the entry
					OriginalCostEntry.setText("");
					// Changes entry text colour to black
					OriginalCostEntry.setForeground(Assets.TextColor);
				}

			}

			// When the component loses focus
			@Override
			public void focusLost(FocusEvent focusEvent) {
				// If the entry is empty
				if (OriginalCostEntry.getText().isEmpty()) {
					// Entry text set to default entry
					OriginalCostEntry.setText(OriginalCostDefualtEntry);
					// Changes entry text colour to grey
					OriginalCostEntry.setForeground(Assets.UnFocusedTextColor);
				}
			}
		});

		Panel.add(OriginalCostEntry);
		AdminComponents.add(OriginalCostEntry);

		// Setup retail price message properties and adds it to the panel
		JTextField RetailPriceMessage = new JTextField();
		RetailPriceMessage.setBounds(1485, 401, 345, 33);
		RetailPriceMessage.setFont(Assets.StandardFont);
		RetailPriceMessage.setEditable(false);
		RetailPriceMessage.setText("");
		RetailPriceMessage.setOpaque(false);
		RetailPriceMessage.setBorder(Assets.SmallPaddingBorder);

		Panel.add(RetailPriceMessage);
		AdminComponents.add(RetailPriceMessage);

		// Setup retail price entry properties and adds it to the panel
		String RetailPriceDefualtEntry = "Enter retail price...";
		JTextField RetailPriceEntry = new JTextField();
		RetailPriceEntry.setBorder(
				BorderFactory.createCompoundBorder(Assets.RoundedInvalidLineBorder, Assets.SmallPaddingBorder));
		RetailPriceEntry.setBounds(930, 401, 545, 33);
		RetailPriceEntry.setBackground(Assets.ComponentColour);
		RetailPriceEntry.setForeground(Assets.UnFocusedTextColor);
		RetailPriceEntry.setFont(Assets.StandardFont);
		RetailPriceEntry.setText(RetailPriceDefualtEntry);
		// Adds use listener to the component
		RetailPriceEntry.getDocument().addDocumentListener(new DocumentListener() {
			// When a character is inserted into the entry
			@Override
			public void insertUpdate(DocumentEvent de) {
				// Validates entry
				boolean NotEmpty = isNotEmptyEntry(RetailPriceEntry.getText(), RetailPriceDefualtEntry);
				boolean PositiveDouble = isPositiveDouble(RetailPriceEntry.getText());
				ValidationArray[5] = NotEmpty && PositiveDouble;
				// Changes border and validation message based on entry validation
				if (ValidationArray[5]) {
					RetailPriceEntry.setBorder(
							BorderFactory.createCompoundBorder(Assets.RoundedLineBorder, Assets.SmallPaddingBorder));
					RetailPriceMessage.setText("");
				} else {
					RetailPriceEntry.setBorder(BorderFactory.createCompoundBorder(Assets.RoundedInvalidLineBorder,
							Assets.SmallPaddingBorder));
					if (NotEmpty) {
						RetailPriceMessage.setText("Must be a positive double.");
					} else {
						RetailPriceMessage.setText("");
					}
				}
			}

			// When a character is removed from the entry
			@Override
			public void removeUpdate(DocumentEvent de) {
				insertUpdate(de);
			}

			@Override
			public void changedUpdate(DocumentEvent de) {
			}
		});
		// Adds focus listener to the component
		RetailPriceEntry.addFocusListener(new FocusListener() {
			// When the component gains focus
			@Override
			public void focusGained(FocusEvent focusEvent) {
				// If the entry text equals the default entry
				if (RetailPriceEntry.getText().equals(RetailPriceDefualtEntry)) {
					// Empties the entry
					RetailPriceEntry.setText("");
					// Changes entry text colour to black
					RetailPriceEntry.setForeground(Assets.TextColor);
				}

			}

			// When the component loses focus
			@Override
			public void focusLost(FocusEvent focusEvent) {
				// If the entry is empty
				if (RetailPriceEntry.getText().isEmpty()) {
					// Entry text set to default entry
					RetailPriceEntry.setText(RetailPriceDefualtEntry);
					// Changes entry text colour to grey
					RetailPriceEntry.setForeground(Assets.UnFocusedTextColor);
				}
			}
		});

		Panel.add(RetailPriceEntry);
		AdminComponents.add(RetailPriceEntry);

		// Setup mouse type drop down properties and adds it to the panel
		final String[] MouseTypeDropDownOptions = { "Type: standard", "Type: gaming" };
		final JComboBox<String> MouseTypeDropDown = new JComboBox<>(MouseTypeDropDownOptions);
		MouseTypeDropDown.setBounds(930, 444, 545, 33);
		MouseTypeDropDown.setFont(Assets.BoldFont);
		MouseTypeDropDown.setBackground(Assets.ComponentColour);
		MouseTypeDropDown
				.setBorder(BorderFactory.createCompoundBorder(Assets.RoundedLineBorder, Assets.SmallPaddingBorder));
		MouseTypeDropDown.setFocusable(false);
		MouseTypeDropDown.setUI(new BasicComboBoxUI());
		Panel.add(MouseTypeDropDown);
		AdminComponents.add(MouseTypeDropDown);

		// Setup keyboard type drop down properties
		final String[] KeyboardTypeDropDownOptions = { "Type: standard", "Type: flexible", "Type: gaming" };
		final JComboBox<String> KeyboardTypeDropDown = new JComboBox<>(KeyboardTypeDropDownOptions);
		KeyboardTypeDropDown.setBounds(930, 444, 545, 33);
		KeyboardTypeDropDown.setFont(Assets.BoldFont);
		KeyboardTypeDropDown.setBackground(Assets.ComponentColour);
		KeyboardTypeDropDown
				.setBorder(BorderFactory.createCompoundBorder(Assets.RoundedLineBorder, Assets.SmallPaddingBorder));
		KeyboardTypeDropDown.setFocusable(false);
		KeyboardTypeDropDown.setUI(new BasicComboBoxUI());

		// Setup button count message properties and adds it to the panel
		JTextField ButtonCountMessage = new JTextField();
		ButtonCountMessage.setBounds(1485, 487, 345, 33);
		ButtonCountMessage.setFont(Assets.StandardFont);
		ButtonCountMessage.setEditable(false);
		ButtonCountMessage.setText("");
		ButtonCountMessage.setOpaque(false);
		ButtonCountMessage.setBorder(Assets.SmallPaddingBorder);

		Panel.add(ButtonCountMessage);
		AdminComponents.add(ButtonCountMessage);

		// Setup button count entry properties and adds it to the panel
		String ButtonCountDefualtEntry = "Enter mouse button count...";
		JTextField ButtonCountEntry = new JTextField();
		ButtonCountEntry.setBorder(
				BorderFactory.createCompoundBorder(Assets.RoundedInvalidLineBorder, Assets.SmallPaddingBorder));
		ButtonCountEntry.setBounds(930, 487, 545, 33);
		ButtonCountEntry.setBackground(Assets.ComponentColour);
		ButtonCountEntry.setForeground(Assets.UnFocusedTextColor);
		ButtonCountEntry.setFont(Assets.StandardFont);
		ButtonCountEntry.setText(ButtonCountDefualtEntry);
		// Adds use listener to the component
		ButtonCountEntry.getDocument().addDocumentListener(new DocumentListener() {
			// When a character is inserted into the entry
			@Override
			public void insertUpdate(DocumentEvent de) {
				// Validates entry
				boolean NotEmpty = isNotEmptyEntry(ButtonCountEntry.getText(), ButtonCountDefualtEntry);
				boolean PositiveInt = isPositiveInteger(ButtonCountEntry.getText());
				ValidationArray[6] = NotEmpty && PositiveInt;
				// Changes border and validation message based on entry validation
				if (ValidationArray[6]) {
					ButtonCountEntry.setBorder(
							BorderFactory.createCompoundBorder(Assets.RoundedLineBorder, Assets.SmallPaddingBorder));
					ButtonCountMessage.setText("");
				} else {
					ButtonCountEntry.setBorder(BorderFactory.createCompoundBorder(Assets.RoundedInvalidLineBorder,
							Assets.SmallPaddingBorder));
					if (NotEmpty) {
						ButtonCountMessage.setText("Must be a positive integer.");
					} else {
						ButtonCountMessage.setText("");
					}
				}

			}

			// When a character is removed from the entry
			@Override
			public void removeUpdate(DocumentEvent de) {
				insertUpdate(de);
			}

			@Override
			public void changedUpdate(DocumentEvent de) {
			}
		});

		// Adds focus listener to the component
		ButtonCountEntry.addFocusListener(new FocusListener() {
			// When the component gains focus
			@Override
			public void focusGained(FocusEvent focusEvent) {
				// If the entry text equals the default entry
				if (ButtonCountEntry.getText().equals(ButtonCountDefualtEntry)) {
					// Empties the entry
					ButtonCountEntry.setText("");
					// Changes entry text colour to black
					ButtonCountEntry.setForeground(Assets.TextColor);
				}
			}

			// When the component loses focus
			@Override
			public void focusLost(FocusEvent focusEvent) {
				// If the entry is empty
				if (ButtonCountEntry.getText().isEmpty()) {
					// Entry text set to defualt entry
					ButtonCountEntry.setText(ButtonCountDefualtEntry);
					// Changes entry text colour to grey
					ButtonCountEntry.setForeground(Assets.UnFocusedTextColor);
				}
			}
		});
		Panel.add(ButtonCountEntry);
		AdminComponents.add(ButtonCountEntry);

		// Setup layout drop down properties
		final String[] LayoutDropDownOptions = { "Layout: UK", "Layout: US" };
		final JComboBox<String> LayoutDropDown = new JComboBox<>(LayoutDropDownOptions);
		LayoutDropDown.setBounds(930, 487, 545, 33);
		LayoutDropDown.setFont(Assets.BoldFont);
		LayoutDropDown.setBackground(Assets.ComponentColour);
		LayoutDropDown
				.setBorder(BorderFactory.createCompoundBorder(Assets.RoundedLineBorder, Assets.SmallPaddingBorder));
		LayoutDropDown.setFocusable(false);
		LayoutDropDown.setUI(new BasicComboBoxUI());

		// Setup device type drop down properties and adds it to the panel
		final String DeviceTypeDropDownOptions[] = { "Device type: mouse", "Device type: keyboard" };
		final JComboBox<String> DeviceTypeDropDown = new JComboBox<>(DeviceTypeDropDownOptions);
		DeviceTypeDropDown.setBounds(930, 100, 545, 33);
		DeviceTypeDropDown.setFont(Assets.BoldFont);
		DeviceTypeDropDown.setBackground(Assets.ComponentColour);
		DeviceTypeDropDown
				.setBorder(BorderFactory.createCompoundBorder(Assets.RoundedLineBorder, Assets.SmallPaddingBorder));
		DeviceTypeDropDown.setFocusable(false);
		DeviceTypeDropDown.setUI(new BasicComboBoxUI());
		// Adds selection change listener to device type combo box
		DeviceTypeDropDown.addItemListener((ItemEvent e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				// Gets the selected device type
				final String SelectedDropDown = String.valueOf(DeviceTypeDropDown.getSelectedItem());
				// Resets all other text field entries to default entries
				BarcodeEntry.setText(BarcodeDefualtEntry);
				BarcodeEntry.setForeground(Assets.UnFocusedTextColor);
				BrandEntry.setText(BrandDefualtEntry);
				BrandEntry.setForeground(Assets.UnFocusedTextColor);
				ColourEntry.setText(ColourDefualtEntry);
				ColourEntry.setForeground(Assets.UnFocusedTextColor);
				QuantityEntry.setText(QuantityDefualtEntry);
				QuantityEntry.setForeground(Assets.UnFocusedTextColor);
				OriginalCostEntry.setText(OriginalCostDefualtEntry);
				OriginalCostEntry.setForeground(Assets.UnFocusedTextColor);
				RetailPriceEntry.setText(RetailPriceDefualtEntry);
				RetailPriceEntry.setForeground(Assets.UnFocusedTextColor);
				ButtonCountEntry.setText(ButtonCountDefualtEntry);
				ButtonCountEntry.setForeground(Assets.UnFocusedTextColor);
				// If mouse option selected
				if (SelectedDropDown.substring(13, SelectedDropDown.length()).equals("mouse")) {
					// Add and remove necessary components
					Panel.remove(LayoutDropDown);
					Panel.add(ButtonCountEntry);
					Panel.remove(KeyboardTypeDropDown);
					Panel.add(MouseTypeDropDown);
					AdminComponents.remove(LayoutDropDown);
					AdminComponents.remove(KeyboardTypeDropDown);
					AdminComponents.add(ButtonCountEntry);
					AdminComponents.add(MouseTypeDropDown);
					ButtonCountEntry.repaint();
					MouseTypeDropDown.repaint();
				} // Otherwise (if keyboard option selected)
				else {
					// Add and remove necessary components
					Panel.remove(ButtonCountEntry);
					Panel.add(LayoutDropDown);
					Panel.remove(MouseTypeDropDown);
					Panel.add(KeyboardTypeDropDown);
					AdminComponents.remove(ButtonCountEntry);
					AdminComponents.remove(MouseTypeDropDown);
					AdminComponents.add(LayoutDropDown);
					AdminComponents.add(KeyboardTypeDropDown);
					LayoutDropDown.revalidate();
					LayoutDropDown.repaint();
					KeyboardTypeDropDown.revalidate();
					KeyboardTypeDropDown.repaint();
					ValidationArray[6] = true;
				}
				ButtonCountMessage.setText("");
			}
		});
		Panel.add(DeviceTypeDropDown);
		AdminComponents.add(DeviceTypeDropDown);

		// Setup add product button and add it to the panel
		final JButton AddProductButton = new JButton();
		AddProductButton.setBounds(930, 530, 900, 60);
		AddProductButton.setFont(Assets.BoldFont);
		AddProductButton.setText("Add Product");
		AddProductButton.setBackground(Assets.ComponentColour);
		AddProductButton.setBorder(Assets.RoundedLineBorder);
		AddProductButton.setFocusPainted(false);
		// Adds roll-over listener to button that highlights it
		Assets.AddButtonHoverColorHighlight(AddProductButton);
		// Adds mouse press listener to button
		AddProductButton.addActionListener((final ActionEvent ae) -> {
			// Check if all text field entries are valid
			boolean ValidProductDetails = true;
			for (int i = 0; i < 7; i++) {
				// If one is invalid
				if (!ValidationArray[i]) {
					// Displays "Invalid product details entered."
					SetMessage("Invalid product details entered.");
					ValidProductDetails = false;
					break;
				}
			}
			// If all text field entries are valid
			if (ValidProductDetails) {
				// Adds new product
				String DeviceType = String.valueOf(DeviceTypeDropDown.getSelectedItem());

				String ConnectivityInput = String.valueOf(ConnectivityDropDown.getSelectedItem());
				ConnectivityInput = ConnectivityInput.substring(14);

				//Finds corresponding ConnectivityType enum
				Product.ConnectivityType Connectivity = Product.ConnectivityType.WIRED;
				if (ConnectivityInput.equals("wireless")) {
					Connectivity = Product.ConnectivityType.WIRELESS;
				}

				// If device type is mouse
				if (DeviceType.substring(13).equals("mouse")) {
					// Adds new mouse
					String Type = String.valueOf(MouseTypeDropDown.getSelectedItem());
					Type = Type.substring(6);

					Mouse NewMouse = new Mouse(Integer.valueOf(BarcodeEntry.getText()), // Barcode
							BrandEntry.getText(), // Brand
							ColourEntry.getText(), // Colour
							Connectivity, // Connectivity
							Integer.valueOf(QuantityEntry.getText()), // Quantity
							Double.valueOf(OriginalCostEntry.getText()), // OriginalCost
							Double.valueOf(RetailPriceEntry.getText()), // RetailPrice
							Mouse.MouseType.valueOf(Type.toUpperCase()), // Type
							Integer.valueOf(ButtonCountEntry.getText())); // Buttons
					AddProduct(NewMouse);
				} // Otherwise (if device type is keyboard)
				else {
					// Adds new keyboard
					String Type = String.valueOf(KeyboardTypeDropDown.getSelectedItem());
					Type = Type.substring(6);

					String Layout = String.valueOf(LayoutDropDown.getSelectedItem());
					Layout = Layout.substring(8);

					Keyboard NewKeyboard = new Keyboard(Integer.valueOf(BarcodeEntry.getText()), // Barcode
							BrandEntry.getText(), // Brand
							ColourEntry.getText(), // Colour
							Connectivity, // Connectivity
							Integer.valueOf(QuantityEntry.getText()), // Quantity
							Double.valueOf(OriginalCostEntry.getText()), // OriginalCost
							Double.valueOf(RetailPriceEntry.getText()), // RetailPrice
							Keyboard.KeyboardType.valueOf(Type.toUpperCase()), // Type
							Layout); // Layout
					AddProduct(NewKeyboard);
				}
				// Resets all other text field entries to default entries
				BarcodeEntry.setText(BarcodeDefualtEntry);
				BarcodeEntry.setForeground(Assets.UnFocusedTextColor);
				BrandEntry.setText(BrandDefualtEntry);
				BrandEntry.setForeground(Assets.UnFocusedTextColor);
				ColourEntry.setText(ColourDefualtEntry);
				ColourEntry.setForeground(Assets.UnFocusedTextColor);
				QuantityEntry.setText(QuantityDefualtEntry);
				QuantityEntry.setForeground(Assets.UnFocusedTextColor);
				OriginalCostEntry.setText(OriginalCostDefualtEntry);
				OriginalCostEntry.setForeground(Assets.UnFocusedTextColor);
				RetailPriceEntry.setText(RetailPriceDefualtEntry);
				RetailPriceEntry.setForeground(Assets.UnFocusedTextColor);
				ButtonCountEntry.setText(ButtonCountDefualtEntry);
				ButtonCountEntry.setForeground(Assets.UnFocusedTextColor);

				final String SelectedDropDown = String.valueOf(DeviceTypeDropDown.getSelectedItem());
				if (!SelectedDropDown.substring(13, SelectedDropDown.length()).equals("mouse")) {
					ValidationArray[6] = true;
				}
			}
		});
		Panel.add(AddProductButton);
		AdminComponents.add(AddProductButton);
	}

	private JComboBox<String> ButtonCountDropDown = new JComboBox<>();
	private JTextField SearchBar = new JTextField();

	final private void RefreshMouseButtonDropDownOptions() {
		// Updates the mouse button count options for the mouse button count drop down
		Object[] ButtonOptions = ButtonMap.keySet().toArray();
		Arrays.sort(ButtonOptions);
		final String DropDownOptions[] = new String[ButtonOptions.length + 1];
		DropDownOptions[0] = "Mouse button count: any";
		for (int i = 0; i < ButtonOptions.length; i++) {
			DropDownOptions[i + 1] = "Mouse button count: " + String.valueOf(ButtonOptions[i]);
		}
		ButtonCountDropDown.removeAllItems();
		for (String Option : DropDownOptions) {
			ButtonCountDropDown.insertItemAt(Option, ButtonCountDropDown.getItemCount());
		}
		ButtonCountDropDown.setSelectedIndex(0);
	}

	private int SuggestionsIndex = 0;
	private int ScrollIndex = 0;
	private String[] SuggestionsArray = new String[0];

	final private void SearchGUISetup() {
		// Setup mouse button count drop down properties and adds it to the panel
		ButtonCountDropDown = new JComboBox<>();
		RefreshMouseButtonDropDownOptions();
		ButtonCountDropDown.setBounds(702, 530, 218, 60);
		ButtonCountDropDown.setFont(Assets.BoldFont);
		ButtonCountDropDown.setBackground(Assets.ComponentColour);
		ButtonCountDropDown
				.setBorder(BorderFactory.createCompoundBorder(Assets.RoundedLineBorder, Assets.SmallPaddingBorder));
		ButtonCountDropDown.setFocusable(false);
		ButtonCountDropDown.setUI(new BasicComboBoxUI());
		((JLabel) ButtonCountDropDown.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
		// Adds selection change listener to mouse button count combo box
		ButtonCountDropDown.addItemListener((ItemEvent e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				// Filters products in products table
				FilterProducts();
			}
		});
		Panel.add(ButtonCountDropDown);

		// Setup suggestions list properties and adds it to the panel
		JList<String> Suggestions = new JList<>();
		Suggestions.setBorder(
				BorderFactory.createCompoundBorder(Assets.SmallRoundedLineBorder, Assets.SmallPaddingBorder));
		Suggestions.setBounds(20, 589, 672, 0);
		Suggestions.setFont(Assets.SmallFont);
		Suggestions.setFocusable(false);
		Suggestions.setBackground(Assets.ComponentColour);
		Suggestions.setForeground(Assets.TextColor);
		Suggestions.setVisible(false);
		// Adds mouse scroll listener to suggestions list
		Suggestions.addMouseWheelListener((MouseWheelEvent e) -> {
			// If scrolling mouse when up
			if (e.getWheelRotation() < 0) {
				// Scrolls up suggestions list
				SuggestionsIndex--;
				if (SuggestionsIndex < 0) {
					SuggestionsIndex = 0;
				}

				if (ScrollIndex > 0) {
					ScrollIndex--;
				} else {
					String[] SuggestionsList = new String[Math.min(4, SuggestionsArray.length)];

					System.arraycopy(SuggestionsArray, SuggestionsIndex, SuggestionsList, 0, SuggestionsList.length);

					Suggestions.setListData(SuggestionsList);
				}
				Suggestions.setSelectedIndex(ScrollIndex);
			} // Otherwise iIf scrolling mouse wheen up)
			else {
				// Scrolls down suggestions list
				SuggestionsIndex++;
				if (SuggestionsIndex >= SuggestionsArray.length) {
					SuggestionsIndex = SuggestionsArray.length - 1;
				}

				if (ScrollIndex < 3) {
					ScrollIndex++;
				} else {
					String[] SuggestionsList2 = new String[Math.min(4, SuggestionsArray.length)];

					System.arraycopy(SuggestionsArray, SuggestionsIndex - 3, SuggestionsList2, 0,
							SuggestionsList2.length);

					Suggestions.setListData(SuggestionsList2);
				}
				Suggestions.setSelectedIndex(ScrollIndex);
			}
		});
		// Adds mouse click listener to suggestions list
		Suggestions.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// Selects list value that was clicked on
				int Difference = Suggestions.getSelectedIndex() - ScrollIndex;
				ScrollIndex = Suggestions.getSelectedIndex();

				SuggestionsIndex += Difference;

				// If double click
				if (e.getClickCount() == 2) {
					// Fills in the search bar with the suggested suggestion when "Enter" is pressed
					if (Suggestions.getSelectedValue() != null) {
						SearchBar.setText(String.valueOf(Suggestions.getSelectedValue()));
					}
				}
			}
		});
		Panel.add(Suggestions);

		// Setup search bar properties and adds it to the panel
		String SearchDefualtEntry = "Search for brand...";
		SearchBar = new JTextField();
		SearchBar.setBorder(BorderFactory.createCompoundBorder(Assets.RoundedLineBorder, Assets.PaddingBorder));
		SearchBar.setBounds(20, 530, 672, 60);
		SearchBar.setBackground(Assets.ComponentColour);
		SearchBar.setForeground(Assets.UnFocusedTextColor);
		SearchBar.setFont(Assets.StandardFont);
		SearchBar.setText(SearchDefualtEntry);
		// Adds use listener to the component
		SearchBar.getDocument().addDocumentListener(new DocumentListener() {
			// When a character is inserted into the entry
			@Override
			public void insertUpdate(DocumentEvent de) {
				// Uses trie tree to get list of brands that contain the entered brand as a
				// prefix
				if (TrieTree.Contains(SearchBar.getText(), 0, true)) {
					SuggestionsArray = TrieTree.PrefixOf(SearchBar.getText().toLowerCase()).toArray(new String[0]);
				} else {
					SuggestionsArray = new String[0];
				}
				// Sorts list by string length
				Arrays.sort(SuggestionsArray, (String t1, String t2) -> Integer.compare(t1.length(), t2.length()));
				// Copy up to first 4 elements to array that will be displayed
				String[] SuggestionsList = new String[Math.min(4, SuggestionsArray.length)];
				System.arraycopy(SuggestionsArray, 0, SuggestionsList, 0, SuggestionsList.length);
				// Displays array in list
				int Offset = 15;
				if (SuggestionsList.length == 0) {
					Offset = 0;
				}
				Suggestions.setSize(672, Math.min(81, Offset + 18 * SuggestionsArray.length));
				Suggestions.setListData(SuggestionsList);
				// Filters products in products table
				FilterProducts();
				// selects the first element in list
				Suggestions.setSelectedIndex(0);
				ScrollIndex = 0;
				SuggestionsIndex = 0;
			}

			// When a character is removed from the entry
			@Override
			public void removeUpdate(DocumentEvent de) {
				insertUpdate(de);
			}

			@Override
			public void changedUpdate(DocumentEvent de) {
			}
		});
		// Adds focus listener to the component
		SearchBar.addFocusListener(new FocusListener() {
			// When the component gains focus
			@Override
			public void focusGained(FocusEvent focusEvent) {
				// If the entry text equals the default entry
				if (SearchBar.getText().equals(SearchDefualtEntry)) {
					// Empties the entry
					SearchBar.setText("");
					// Changes entry text colour to black
					SearchBar.setForeground(Assets.TextColor);
				}
				// Makes suggestions list visible and selects the first element in it
				Suggestions.setVisible(true);
				Suggestions.setSelectedIndex(0);
				ScrollIndex = 0;
				SuggestionsIndex = 0;
			}

			// When the component loses focus
			@Override
			public void focusLost(FocusEvent focusEvent) {
				// If the entry is empty
				if (SearchBar.getText().isEmpty()) {
					// Entry text set to default entry
					SearchBar.setText(SearchDefualtEntry);
					// Changes entry text colour to grey
					SearchBar.setForeground(Assets.UnFocusedTextColor);
				}
				// Makes suggestions list invisible
				Suggestions.setVisible(false);
			}
		});
		// Adds key listener
		SearchBar.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent ke) {
			}

			// When a key is pressed
			@Override
			public void keyPressed(KeyEvent ke) {
				int keyCode = ke.getKeyCode();
				switch (keyCode) {
				// If up arrow is pressed
				case KeyEvent.VK_UP:
					// Scrolls up suggestions list
					SuggestionsIndex--;
					if (SuggestionsIndex < 0) {
						SuggestionsIndex = 0;
					}

					if (ScrollIndex > 0) {
						ScrollIndex--;
					} else {
						String[] SuggestionsList = new String[Math.min(4, SuggestionsArray.length)];

						System.arraycopy(SuggestionsArray, SuggestionsIndex, SuggestionsList, 0,
								SuggestionsList.length);

						Suggestions.setListData(SuggestionsList);
					}
					Suggestions.setSelectedIndex(ScrollIndex);
					break;
				// If down arrow is pressed
				case KeyEvent.VK_DOWN:
					// Scrolls down suggestions list
					SuggestionsIndex++;
					if (SuggestionsIndex >= SuggestionsArray.length) {
						SuggestionsIndex = SuggestionsArray.length - 1;
					}

					if (ScrollIndex < 3) {
						ScrollIndex++;
					} else {
						String[] SuggestionsList2 = new String[Math.min(4, SuggestionsArray.length)];

						System.arraycopy(SuggestionsArray, SuggestionsIndex - 3, SuggestionsList2, 0,
								SuggestionsList2.length);

						Suggestions.setListData(SuggestionsList2);
					}
					Suggestions.setSelectedIndex(ScrollIndex);
					break;
				}
			}

			@Override
			public void keyReleased(KeyEvent ke) {
			}

		});
		// Adds action listener to search bar
		SearchBar.addActionListener((ActionEvent e) -> {
			// Fills in the search bar with the suggested suggestion when "Enter" is pressed
			if (Suggestions.getSelectedValue() != null) {
				SearchBar.setText(String.valueOf(Suggestions.getSelectedValue()));
			}
		});
		Panel.add(SearchBar);
		// Setup message properties and adds it to the panel
		MessageField = new JTextArea();
		MessageField.setBounds(20, 600, 900, 60);
		MessageField.setFont(Assets.StandardFont);
		MessageField.setEditable(false);
		MessageField.setText("");
		MessageField.setOpaque(false);
		MessageField.setBorder(Assets.PaddingBorder);
		Panel.add(MessageField);
	}

	private JTextArea MessageField;

	final private void SetMessage(String Message) {
		// Displays specified message
		MessageField.setForeground(Assets.TextColor);
		MessageField.setText(Message);
		// Message displayed for 2 seconds
		MessageTime = 200;
	}

	private int MessageTime = 0;

	final private void RemoveMessage() {
		Thread T1 = new Thread(() -> {
			// Continually loops
			while (true) {
				// Reduces message time
				if (MessageTime > 0) {
					MessageTime--;
					// Fades message text
					if (MessageTime > 0 && MessageTime < 15) {
						MessageField.setForeground(new Color(Assets.TextColor.getRed(), Assets.TextColor.getGreen(),
								Assets.TextColor.getBlue(), MessageTime * (255 / 15)));
					}
					// Remove message once time is up
					if (MessageTime == 0) {
						MessageField.setText("");
					}
				}
				// 10 millisecond delay
				try {
					TimeUnit.MILLISECONDS.sleep(10);
				} catch (InterruptedException ex) {
					Logger.getLogger(ComputerAccessoriesShop.class.getName()).log(Level.SEVERE, null, ex);
					System.out.println("Error RemoveMessage: " + ex);
				}
			}
		});
		T1.start();
	}

	final private void FilterProducts() {
		// Gets selected button count from button count drop down
		String ButtonCount = String.valueOf(ButtonCountDropDown.getSelectedItem());
		ButtonCount = ButtonCount.substring(20);
		// Gets brand from search bar
		String Brand = SearchBar.getText();
		if (Brand.equals("Search for brand...")) {
			Brand = "";
		}

		ArrayList<Product> FilteredProducts;
		// Gets list mapped by button count
		if (ButtonCount.equals("any")) {
			FilteredProducts = AllProducts;
		} else {
			FilteredProducts = ButtonMap.get(Integer.valueOf(ButtonCount));
		}

		ArrayList<Product> BrandFilteredProducts = new ArrayList<>();
		// Pattern to find all brands that contain the entered brand
		final Pattern regExPattern = Pattern.compile(".*" + Brand.toLowerCase() + ".*");
		// Further filters the filtered products by searched brand
		for (int i = 0; i < FilteredProducts.size(); i++) {
			final Matcher matcher = regExPattern.matcher(FilteredProducts.get(i).getBrand().toLowerCase());
			if (matcher.matches()) {
				BrandFilteredProducts.add(FilteredProducts.get(i));
			}
		}
		// Creates product table
		boolean isAdmin = !(CurrentUser == null || !CurrentUser.isAdminRole());
		ProductsTableSetup(isAdmin, BrandFilteredProducts);
	}

	private JTextField EmailEntry = new JTextField();
	private JTextField CardNumberEntry = new JTextField();
	private JTextField SecurityCodeEntry = new JTextField();

	final private void PaymentEntrySetup(boolean IsPayPal) {
		//Resets validation message to defaults
		EmailValidationMessage = "Please enter your Email address";
		CardNumberValidationMessage = "Please enter your Card Number";
		SercurityCodeValidationMessage = "Please enter your Sercurity Code";
		// Remove previous payment entry components
		Panel.remove(CardNumberEntry);
		Panel.remove(SecurityCodeEntry);
		Panel.remove(EmailEntry);
		CustomerComponents.remove(CardNumberEntry);
		CustomerComponents.remove(SecurityCodeEntry);
		CustomerComponents.remove(EmailEntry);
		// If PayPal payment method selected
		if (IsPayPal) {
			// Setup email entry properties and add it to the panel
			String EmailDefualtEntry = "Enter email address...";
			EmailEntry = new JTextField();
			EmailEntry.setBorder(
					BorderFactory.createCompoundBorder(Assets.RoundedInvalidLineBorder, Assets.PaddingBorder));
			EmailEntry.setBounds(930, 600, 900, 60);
			EmailEntry.setBackground(Assets.ComponentColour);
			EmailEntry.setForeground(Assets.UnFocusedTextColor);
			EmailEntry.setFont(Assets.StandardFont);
			EmailEntry.setText(EmailDefualtEntry);
			// Adds use listener to component
			EmailEntry.getDocument().addDocumentListener(new DocumentListener() {
				// When a character is inserted into the entry
				@Override
				public void insertUpdate(DocumentEvent de) {
					// Changes border based on email validation
					if (ValidateEmail(EmailEntry.getText(), EmailDefualtEntry)) {
						EmailEntry.setBorder(
								BorderFactory.createCompoundBorder(Assets.RoundedLineBorder, Assets.PaddingBorder));
					} else {
						EmailEntry.setBorder(BorderFactory.createCompoundBorder(Assets.RoundedInvalidLineBorder,
								Assets.PaddingBorder));
					}
				}

				// When a character is removed from the entry
				@Override
				public void removeUpdate(DocumentEvent de) {
					insertUpdate(de);
				}

				@Override
				public void changedUpdate(DocumentEvent de) {
				}
			});
			// Adds focus listener to the component
			EmailEntry.addFocusListener(new FocusListener() {
				// When the component gains focus
				@Override
				public void focusGained(FocusEvent focusEvent) {
					// If the entry text equals the default entry
					if (EmailEntry.getText().equals(EmailDefualtEntry)) {
						// Empties the entry
						EmailEntry.setText("");
						// Changes entry text colour to black
						EmailEntry.setForeground(Assets.TextColor);
					}
				}

				// When the component loses focus
				@Override
				public void focusLost(FocusEvent focusEvent) {
					// If the entry is empty
					if (EmailEntry.getText().isEmpty()) {
						// Entry text set to default entry
						EmailEntry.setText(EmailDefualtEntry);
						// Changes entry text colour to grey
						EmailEntry.setForeground(Assets.UnFocusedTextColor);
					}
				}
			});
			Panel.add(EmailEntry);
			CustomerComponents.add(EmailEntry);
		} // Otherwise (if credit card payment method selected)
		else {
			// Setup card number entry properties and add it to the panel
			String CardNumberDefualtEntry = "Enter card number...";
			CardNumberEntry = new JTextField();
			CardNumberEntry.setBorder(
					BorderFactory.createCompoundBorder(Assets.RoundedInvalidLineBorder, Assets.PaddingBorder));
			CardNumberEntry.setBounds(930, 600, 595, 60);
			CardNumberEntry.setBackground(Assets.ComponentColour);
			CardNumberEntry.setForeground(Assets.UnFocusedTextColor);
			CardNumberEntry.setFont(Assets.StandardFont);
			CardNumberEntry.setText(CardNumberDefualtEntry);
			// Adds use listener to component
			CardNumberEntry.getDocument().addDocumentListener(new DocumentListener() {
				// When a character is inserted into the entry
				@Override
				public void insertUpdate(DocumentEvent de) {
					// Changes border based on email validation
					if (ValidateCardNumber(CardNumberEntry.getText(), CardNumberDefualtEntry)) {
						CardNumberEntry.setBorder(
								BorderFactory.createCompoundBorder(Assets.RoundedLineBorder, Assets.PaddingBorder));
					} else {
						CardNumberEntry.setBorder(BorderFactory.createCompoundBorder(Assets.RoundedInvalidLineBorder,
								Assets.PaddingBorder));
					}
				}

				// When a character is removed from the entry
				@Override
				public void removeUpdate(DocumentEvent de) {
					insertUpdate(de);
				}

				@Override
				public void changedUpdate(DocumentEvent de) {
				}
			});
			// Adds focus listener to the component
			CardNumberEntry.addFocusListener(new FocusListener() {
				// When the component gains focus
				@Override
				public void focusGained(FocusEvent focusEvent) {
					// If the entry text equals the default entry
					if (CardNumberEntry.getText().equals(CardNumberDefualtEntry)) {
						// Empties the entry
						CardNumberEntry.setText("");
						// Changes entry text colour to black
						CardNumberEntry.setForeground(Assets.TextColor);
					}

				}

				// When the component loses focus
				@Override
				public void focusLost(FocusEvent focusEvent) {
					// If the entry is empty
					if (CardNumberEntry.getText().isEmpty()) {
						// Entry text set to default entry
						CardNumberEntry.setText(CardNumberDefualtEntry);
						// Changes map code entry text field text colour to grey
						CardNumberEntry.setForeground(Assets.UnFocusedTextColor);
					}
				}
			});
			Panel.add(CardNumberEntry);
			CustomerComponents.add(CardNumberEntry);

			// Setup security code entry properties and add it to the panel
			String SecurityCodeDefualtEntry = "Enter security code...";
			SecurityCodeEntry = new JTextField();
			SecurityCodeEntry.setBorder(
					BorderFactory.createCompoundBorder(Assets.RoundedInvalidLineBorder, Assets.PaddingBorder));
			SecurityCodeEntry.setBounds(1535, 600, 295, 60);
			SecurityCodeEntry.setBackground(Assets.ComponentColour);
			SecurityCodeEntry.setForeground(Assets.UnFocusedTextColor);
			SecurityCodeEntry.setFont(Assets.StandardFont);
			SecurityCodeEntry.setText(SecurityCodeDefualtEntry);
			// Adds use listener to the component
			SecurityCodeEntry.getDocument().addDocumentListener(new DocumentListener() {
				// When a character is inserted into the entry
				@Override
				public void insertUpdate(DocumentEvent de) {
					// Changes border based on email validation
					if (ValidateSercurityCode(SecurityCodeEntry.getText(), SecurityCodeDefualtEntry)) {
						SecurityCodeEntry.setBorder(
								BorderFactory.createCompoundBorder(Assets.RoundedLineBorder, Assets.PaddingBorder));
					} else {
						SecurityCodeEntry.setBorder(BorderFactory.createCompoundBorder(Assets.RoundedInvalidLineBorder,
								Assets.PaddingBorder));
					}
				}

				// When a character is removed from the entry
				@Override
				public void removeUpdate(DocumentEvent de) {
					insertUpdate(de);
				}

				@Override
				public void changedUpdate(DocumentEvent de) {
				}
			});
			// Adds focus listener to the component
			SecurityCodeEntry.addFocusListener(new FocusListener() {
				// When the component gains focus
				@Override
				public void focusGained(FocusEvent focusEvent) {
					// If the entry text equals the defualt entry
					if (SecurityCodeEntry.getText().equals(SecurityCodeDefualtEntry)) {
						// Empties the entry
						SecurityCodeEntry.setText("");
						// Changes entry text colour to black
						SecurityCodeEntry.setForeground(Assets.TextColor);
					}
				}

				// When the component loses focus
				@Override
				public void focusLost(FocusEvent focusEvent) {
					// If the entry is empty
					if (SecurityCodeEntry.getText().isEmpty()) {
						// Entry text set to default entry
						SecurityCodeEntry.setText(SecurityCodeDefualtEntry);
						// Changes map code entry text field text colour to grey
						SecurityCodeEntry.setForeground(Assets.UnFocusedTextColor);
					}
				}
			});
			Panel.add(SecurityCodeEntry);
			CustomerComponents.add(SecurityCodeEntry);
		}
		Panel.revalidate();
		Panel.repaint();
	}

	final private void PayForItems(boolean IsPaypal) {
		// Calculates total cost of all items in basket
		double Amount = 0;
		for (int i = 0; i < BasketProducts.size(); i++) {
			Product BasketProduct = BasketTable.getProductAtRow(i);
			Amount += (BasketProduct.getRetailPrice() * BasketProduct.getQuantity());
		}
		// Removes all products from basket
		BasketProducts.clear();
		BasketTableSetup(BasketProducts);
		// Rounds total cost to 2 d.p. (to avoid floating point precision errors)
		String RoundedAmount = new DecimalFormat("####0.00").format(Amount);
		// Saves products to Stock.txt
		SaveProductsToFile();
		// If PayPal payment method selected
		if (IsPaypal) {
			// Display PayPal transaction details
			SetMessage(RoundedAmount + " paid using PayPal, and the delivery address is "
					+ CurrentUser.getAddress().toString() + ".");
		} // Otherwise (if credit card payment method selected)
		else {
			// Display credit card transaction details
			SetMessage(RoundedAmount + " paid using Credit Card, and the delivery address is "
					+ CurrentUser.getAddress().toString() + ".");
		}
	}

	private String EmailValidationMessage = "Please enter your Email address";

	final private boolean ValidateEmail(String EmailAddressEntryText, String DefualtEntry) {
		// Returns false if the email address is empty and sets appropriate validation
		// message
		if (!isNotEmptyEntry(EmailAddressEntryText, DefualtEntry)) {
			EmailValidationMessage = "Please enter your Email address";
			return false;
		}

		// Checks if email has valid format
		// Pattern from http://regexlib.com/Search.aspx?k=email
		final Pattern regExPattern = Pattern.compile("^[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+"
				+ "(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+"
				+ "(?:[a-zA-Z]{2}|aero|asia|biz|cat|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|net|org|pro|tel|travel)$");
		final Matcher matcher = regExPattern.matcher(EmailAddressEntryText);
		// Returns true if the email address has the correct format
		if (matcher.matches()) {
			EmailValidationMessage = "Valid";
			return true;
		} else {
			EmailValidationMessage = "Invalid Email address";
			return false;
		}
	}

	private String CardNumberValidationMessage = "Please enter your Card Number";

	final private boolean ValidateCardNumber(String CardNumberEntryText, String DefualtEntry) {
		// Returns false if the card number is empty and sets appropriate validation
		// message
		if (!isNotEmptyEntry(CardNumberEntryText, DefualtEntry)) {
			CardNumberValidationMessage = "Please enter your Card Number";
			return false;
		}
		// Returns false if the card number is not a positive integer and sets
		// appropriate validation message
		if (!isPositiveInteger(CardNumberEntryText)) {
			CardNumberValidationMessage = "Invalid Card Number - Please enter a positive integer value";
			return false;
		}
		// Returns false if the card number is not 6-digits and sets appropriate
		// validation message
		if (CardNumberEntryText.length() != 6) {
			CardNumberValidationMessage = "Invalid Card Number length - Please enter a six-digit integer value";
			return false;
		}
		CardNumberValidationMessage = "Valid";
		return true;

	}

	private String SercurityCodeValidationMessage = "Please enter your Sercurity Code";

	final private boolean ValidateSercurityCode(String SercurityCodeEntryText, String DefualtEntry) {
		// Returns false if the security code is empty and sets appropriate validation
		// message
		if (!isNotEmptyEntry(SercurityCodeEntryText, DefualtEntry)) {
			SercurityCodeValidationMessage = "Please enter your Sercurity Code";
			return false;
		}
		// Returns false if the security code is not a positive integer and sets
		// appropriate validation message
		if (!isPositiveInteger(SercurityCodeEntryText)) {
			SercurityCodeValidationMessage = "Invalid Sercurity Code - Please enter a positive integer value";
			return false;
		}
		// Returns false if the security code is not 3-digits and sets appropriate
		// validation message
		if (SercurityCodeEntryText.length() != 3) {
			SercurityCodeValidationMessage = "Invalid Sercurity Code length - Please enter a three-digit integer value";
			return false;
		}
		SercurityCodeValidationMessage = "Valid";
		return true;

	}

	final private boolean isNotEmptyEntry(String Entry, String DefualtEntry) {
		// Return true if entry is not the DefualtEntry or nothing
		return !(Entry.equals("") || Entry.equals(DefualtEntry));
	}

	final private boolean isPositiveInteger(String Entry) {
		try {
			int num = Integer.valueOf(Entry);
			// Returns false if entry is below zero
			if (num < 0) {
				return false;
			}
		} // Returns false if entry is not a integer
		catch (NumberFormatException e) {
			return false;
		}
		// return true if entry is a positive integer
		return true;
	}

	final private boolean isPositiveDouble(String Entry) {
		try {

			double num = Double.valueOf(Entry);
			// Returns false if entry is below zero
			if (num < 0) {
				return false;
			}
		} // Returns false if entry is not a double
		catch (NumberFormatException e) {
			return false;
		}
		// return true if entry is a positive double
		return true;
	}

	private JPanel TransitionPanel;
	private int TransitionOpacity = 0;

	@SuppressWarnings("serial")
	final private void TransitionPanelSetup(int xSize, int ySize) {
		// Setup transition panel properties
		TransitionPanel = new JPanel() {

			@Override
			protected void paintComponent(final Graphics g) {
				super.paintComponent(g);
				final Graphics2D gx = (Graphics2D) g;

				gx.addRenderingHints(
						new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED));

				// Paints transition overlay with current transition opacity
				gx.setStroke(new BasicStroke(0));
				gx.setColor(new Color(Assets.BackGroundColour.getRed(), Assets.BackGroundColour.getGreen(),
						Assets.BackGroundColour.getBlue(), TransitionOpacity));
				gx.fillRect(0, 0, xSize, ySize);
			}
		};

		TransitionPanel.setBounds(2, 80, xSize - 4, ySize - 82);
		TransitionPanel.setLayout(null);
		TransitionPanel.setOpaque(false);
		TransitionPanel.setVisible(false);
		// Adds the transition panel to the panel at the front most position
		Panel.add(TransitionPanel, 1);
	}

	final private void TransitionStart() {
		// Transition starts
		TransitionPanel.setVisible(true);
		// Transition panel fades to foreground
		for (int Opacity = 0; Opacity <= 255; Opacity = Opacity + 15) {
			try {
				TimeUnit.MILLISECONDS.sleep(2);
			} catch (InterruptedException ex) {
				System.out.println("Transition start error : " + ex);
			}
			TransitionOpacity = Opacity;
			TransitionPanel.paintImmediately(0, 0, TransitionPanel.getWidth(), TransitionPanel.getHeight());
		}
	}

	final private void TransitionEnd() {
		// Brief delay before ending transition
		try {
			TimeUnit.MILLISECONDS.sleep(20);
		} catch (InterruptedException ex) {
			System.out.println("Transition end error : " + ex);
		}
		// Transition panel fades from foreground
		for (int Opacity = 255; Opacity >= 0; Opacity = Opacity - 15) {
			try {
				TimeUnit.MILLISECONDS.sleep(2);
			} catch (InterruptedException ex) {
				System.out.println("Transition end error : " + ex);
			}
			TransitionOpacity = Opacity;
			TransitionPanel.paintImmediately(0, 0, TransitionPanel.getWidth(), TransitionPanel.getHeight());
		}
		// Transition ends
		TransitionPanel.setVisible(false);
	}
}
