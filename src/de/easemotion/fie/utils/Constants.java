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
	
	public static class dir {
		public static final File BASE = new File(System.getProperty("user.dir"));
		public static final File IMAGE_ASSETS = new File(BASE + "/assets/images/");
		public static final File FONT_ASSETS = new File(BASE + "/assets/fonts/");
	}

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
		public static Font FONT_CODE = null;
		public static Font FONT_REGULAR = null;
		
		static {
			try {
				Font font = Font.createFont(Font.TRUETYPE_FONT, new File(Constants.dir.FONT_ASSETS + "/QUARTZ.TTF"));
				FONT_QUARZ = font.deriveFont(14.0f);
			} catch (FontFormatException | IOException e) {
				e.printStackTrace();
			}
			
			try {
				Font font = Font.createFont(Font.TRUETYPE_FONT, new File(Constants.dir.FONT_ASSETS + "/GLASSGA_0.TTF"));
				FONT_GLASS = font.deriveFont(14.0f);
			} catch (FontFormatException | IOException e) {
				e.printStackTrace();
			}
			
			try {
				Font font = Font.createFont(Font.TRUETYPE_FONT, new File(Constants.dir.FONT_ASSETS + "/OpenSans-Regular.ttf"));
				FONT_REGULAR = font.deriveFont(14.0f);
			} catch (FontFormatException | IOException e) {
				e.printStackTrace();
			}
			
			try {
				Font font = Font.createFont(Font.TRUETYPE_FONT, new File(Constants.dir.FONT_ASSETS + "/SourceCodePro-Regular.ttf"));
				FONT_CODE = font.deriveFont(14.0f);
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
		public static final String TEXT_PRIMARY = "#b6dab6";
	}
}
