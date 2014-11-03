package de.easemotion.fie.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Paint;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Constants {

	public static class string {
		public static final String APP_NAME = "Instrument Editor";
	}

	public static class integer {
		public static final int INSTRUMENT_WIDTH = 240;
		public static final int INSTRUMENT_HEIGHT = 240;

		public static final int GRID_SIZE = 10;
		
		
		public static final int MAX_LAYER_COUNT = 10;
	}
	
	public static class font {
		public static final String QUARTZ = "QUARTZ";
		
		public static Font FONT_QUARZ = null;
		public static Font FONT_GLASS = null;
		
		static {
			String dir = System.getProperty("user.dir");
			
			try {
				Font font = Font.createFont(Font.TRUETYPE_FONT, new File(dir + "/assets/fonts/QUARTZ.TTF"));
				FONT_QUARZ = font.deriveFont(16.0f);
			} catch (FontFormatException | IOException e) {
				e.printStackTrace();
			}
			
			try {
				Font font = Font.createFont(Font.TRUETYPE_FONT, new File(dir + "/assets/fonts/GLASSGA_0.TTF"));
				FONT_GLASS = font.deriveFont(16.0f);
			} catch (FontFormatException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static class paint {
		public static final Paint GRID_LIGHT = new Color(0.1f, 0.1f, 0.1f);
		public static final Paint GRID_DARK = new Color(0.2f, 0.2f, 0.2f);
		public static final Paint GRID_ALIGNMENT = new Color(1.0f, 1.0f, 1.0f);
		
		public static final Paint LAYER_ACTIVE_BORDER = new Color(1.0f, 0.0f, 0.0f);
		public static final Paint LAYER_PIVOT = new Color(1.0f, 0.0f, 0.0f);
	}
	
	public static class color {
		public static final String TEXT_DARK_GREY = "#444444";
		public static final String TEXT_LIGHT_GREY = "#dddddd";
	}
}
