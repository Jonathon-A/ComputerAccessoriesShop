package computeraccessoriesshop;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;

public abstract class Assets {
	// Universal read-only constants used to customise the appearance of the GUI
	final public static Border RoundedLineBorder = BorderFactory.createLineBorder(Color.BLACK, 2, true);
	final public static Border RoundedInvalidLineBorder = BorderFactory.createLineBorder(Color.RED, 2, true);
	final public static Border SmallRoundedLineBorder = BorderFactory.createLineBorder(Color.BLACK, 1, true);
	final public static Border LineBorder = BorderFactory.createLineBorder(Color.BLACK, 2, false);
	final public static Border PaddingBorder = BorderFactory.createEmptyBorder(12, 12, 12, 12);
	final public static Border SmallPaddingBorder = BorderFactory.createEmptyBorder(6, 6, 6, 6);
	final public static Color WindowColour = new Color(247, 247, 247);
	final public static Color BackGroundColour = new Color(255, 200, 110);
	final public static Color ComponentColour = Color.WHITE;
	final public static Color ComponentHighlightColour = new Color(204, 223, 255);
	final public static Color TextColor = Color.BLACK;
	final public static Color UnFocusedTextColor = new Color(155, 155, 155);
	final public static Font SmallFont = new Font("Helvetica", Font.PLAIN, 11);
	final public static Font StandardFont = new Font("Helvetica", Font.PLAIN, 12);
	final public static Font BoldFont = new Font("Helvetica", Font.BOLD, 12);
	final public static Font HeaderFont = new Font("Helvetica", Font.BOLD, 14);
	final public static Font TitleFont = new Font("Helvetica", Font.BOLD, 32);
	final public static String TitleImageDir = System.getProperty("user.dir") + "\\TitleImage.png";
	final public static String IconImageDir = System.getProperty("user.dir") + "\\IconImage.png";
	final public static String StockDir = System.getProperty("user.dir") + "\\Stock.txt";
	final public static String UserAccountDir = System.getProperty("user.dir") + "\\UserAccounts.txt";

	final public static void AddButtonHoverColorHighlight(final JButton btn) {
		final Color StandardColor = btn.getBackground();
		// Removes previous mouse cursor hover listeners
		if (btn.getChangeListeners().length == 0) {
			btn.removeChangeListener(btn.getChangeListeners()[0]);
		}
		// Adds mouse cursor hover listener
		btn.getModel().addChangeListener((final ChangeEvent e) -> {
			final ButtonModel model = (ButtonModel) e.getSource();
			// If the mouse cursor is hovering over the button
			if (model.isRollover()) {
				// If the button is not already highlighted
				if (btn.getBackground().equals(StandardColor)) {
					// Highlights button background light blue
					btn.setBackground(ComponentHighlightColour);
				}

			} // If the mouse cursor is not hovering over the button
			else {
				// If the button is already highlighted
				if (btn.getBackground().equals(ComponentHighlightColour)) {
					// Return button background to previous colour
					btn.setBackground(StandardColor);
				}
			}
		});
	}
}
