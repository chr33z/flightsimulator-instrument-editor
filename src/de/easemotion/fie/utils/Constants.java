package de.easemotion.fie.utils;

import java.awt.Color;
import java.awt.Paint;

public class Constants {

	public static class string {
		public static final String APP_NAME = "Instrument Editor";
	}

	public static class integer {
		public static final int INSTRUMENT_WIDTH = 240;
		public static final int INSTRUMENT_HEIGHT = 240;

		public static final int GRID_SIZE = 10;
	}

	public static class paint {
		public static final Paint GRID_LIGHT = new Color(0.8f, 0.8f, 0.8f);
		public static final Paint GRID_DARK = new Color(0.65f, 0.65f, 0.65f);
	}
}
