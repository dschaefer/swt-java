package org.eclipse.swt.widgets;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */

import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.win32.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

/**
 * Instances of this class allow the user to select a color
 * from a predefined set of available colors.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation.
 * </p>
 */

public class ColorDialog extends Dialog {
	RGB rgb;
	
/**
 * Constructs a new instance of this class given only its parent.
 *
 * @param parent a composite control which will be the parent of the new instance (cannot be null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 *
 * @see SWT
 * @see Widget#checkSubclass
 * @see Widget#getStyle
 */
public ColorDialog (Shell parent) {
	this (parent, SWT.NULL);
}

/**
 * Constructs a new instance of this class given its parent
 * and a style value describing its behavior and appearance.
 * <p>
 * The style value is either one of the style constants defined in
 * class <code>SWT</code> which is applicable to instances of this
 * class, or must be built by <em>bitwise OR</em>'ing together 
 * (that is, using the <code>int</code> "|" operator) two or more
 * of those <code>SWT</code> style constants. The class description
 * for all SWT widget classes should include a comment which
 * describes the style constants which are applicable to the class.
 * </p>
 *
 * @param parent a composite control which will be the parent of the new instance (cannot be null)
 * @param style the style of control to construct
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 *
 * @see SWT
 * @see Widget#checkSubclass
 * @see Widget#getStyle
 */
public ColorDialog (Shell parent, int style) {
	super (parent, style);
	checkSubclass ();
}

int CCHookProc (int hdlg, int uiMsg, int lParam, int lpData) {
	switch (uiMsg) {
		case OS.WM_INITDIALOG:
			if (title != null && title.length () != 0) {
				/* Use the character encoding for the default locale */
				TCHAR buffer = new TCHAR (0, title, true);
				OS.SetWindowText (hdlg, buffer);
			}
			break;
	}
	return 0;
}

/**
 * Returns the currently selected color in the receiver.
 *
 * @return the RGB value for the selected color, may be null
 *
 * @see PaletteData#getRGBs
 */
public RGB getRGB () {
	return rgb;
}

/**
 * Makes the receiver visible and brings it to the front
 * of the display.
 *
 * @return the selected color, or null if the dialog was
 *         cancelled, no color was selected, or an error
 *         occurred
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public RGB open () {
	
	/* Get the owner HWND for the dialog */
	int hwndOwner = 0;
	if (parent != null) hwndOwner = parent.handle;

	/* Create the CCHookProc */
	Callback callback = new Callback (this, "CCHookProc", 4);
	int lpfnHook = callback.getAddress ();
	
	/* Open the dialog */
	int hHeap = OS.GetProcessHeap ();
	int lpCustColors = OS.HeapAlloc (hHeap, OS.HEAP_ZERO_MEMORY, 16 * 4);
	CHOOSECOLOR lpcc = new CHOOSECOLOR ();
	lpcc.lStructSize = CHOOSECOLOR.sizeof;
	lpcc.Flags = OS.CC_ANYCOLOR | OS.CC_ENABLEHOOK;
	lpcc.lpfnHook = lpfnHook;
	lpcc.hwndOwner = hwndOwner;
	lpcc.lpCustColors = lpCustColors;
	if (rgb != null) {
		lpcc.Flags |= OS.CC_RGBINIT;
		int red = rgb.red & 0xFF;
		int green = (rgb.green << 8) & 0xFF00;
		int blue = (rgb.blue << 16) & 0xFF0000;
		lpcc.rgbResult = red | green | blue;
	}
	rgb = null;
	if (OS.ChooseColor (lpcc)) {
		int red = lpcc.rgbResult & 0xFF;
		int green = (lpcc.rgbResult >> 8) & 0xFF;
		int blue = (lpcc.rgbResult >> 16) & 0xFF;
		rgb = new RGB (red, green, blue);
	}
	
	/* Free the CCHookProc */
	callback.dispose ();
	
	/* Free the OS memory */
	if (lpCustColors != 0) OS.HeapFree (hHeap, 0, lpCustColors);
	
	/*
	* This code is intentionally commented.  On some
	* platforms, the owner window is repainted right
	* away when a dialog window exits.  This behavior
	* is currently unspecified.
	*/
//	if (hwndOwner != 0) OS.UpdateWindow (hwndOwner);
	
	return rgb;
}

/**
 * Returns the receiver's selected color to be the argument.
 *
 * @param rgb the new RGB value for the selected color, may be
 *        null to let the platform to select a default when
 *        open() is called
 *
 * @see PaletteData#getRGBs
 */
public void setRGB (RGB rgb) {
	this.rgb = rgb;
}

}
