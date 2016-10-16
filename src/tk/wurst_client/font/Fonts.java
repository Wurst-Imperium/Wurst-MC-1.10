/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.font;

import java.awt.Font;

public class Fonts
{
	public static WolframFontRenderer segoe22;
	public static WolframFontRenderer segoe18;
	public static WolframFontRenderer segoe15;
	
	public static void loadFonts()
	{
		segoe22 = new WolframFontRenderer(new Font("Segoe UI", Font.PLAIN, 22),
			true, 8);
		segoe18 = new WolframFontRenderer(new Font("Segoe UI", Font.PLAIN, 18),
			true, 8);
		segoe15 = new WolframFontRenderer(new Font("Segoe UI", Font.PLAIN, 15),
			true, 8);
	}
}
