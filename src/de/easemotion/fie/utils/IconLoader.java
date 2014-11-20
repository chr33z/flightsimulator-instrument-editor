package de.easemotion.fie.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.media.Image;

public class IconLoader {

	public final static int DEACTIVE = 0;
	public final static int ACTIVE = 1;
	public final static int LOADED = 2;
	public final static int MISSING = 3;

	public enum Icon {
		DAY("B_Day30_"),
		NIGHT("B_Night30_"),
		EDIT("B_Edit30_"),
		DELETE("B_Delete30_"),
		DOWN("B_Down_"),
		UP("B_Up30_"),
		VIEW("B_View30_"),
		DAY_L("B_Day60_"),
		NICHT_L("B_Night60_"),
		MASK_L("B_Mask60_"),
		GRID_L("B_Grid60_"),
		LOAD("B_TLoad60_"),
		SAVE("B_TPackSave120_"),
		RESET("B_TReset60_"),
		TEXT("B_Text30_"),
		OFF_L("B_Off60_"),
		OFF("B_Off30_"),
		SIM("B_CM15_");
		
		private final String icon;

		Icon(String icon) {
			this.icon = icon;
		}

		private String value(){
			return icon;
		}
	}

	public static HashMap<Icon, Image[]> icons = new HashMap<>();

	public static void loadIcons(){
		String dir = System.getProperty("user.dir");

		for (Icon icon : Icon.values()) {
			Image[] imageSet = new Image[4];
			for (int j = 0; j < 4; j++) {
				Image image = null;
				try {
					URL url = new File(Constants.dir.IMAGE_ASSETS+ "/" + icon.value() + j + ".png").toURI().toURL();
					image = Image.load(url);
					imageSet[j] = image;
				} catch (MalformedURLException | TaskExecutionException e) {
					if(j == 0){
						e.printStackTrace();
					}
				}
			}
			icons.put(icon, imageSet);
		}

		System.err.println("Images loaded");
	}
}
