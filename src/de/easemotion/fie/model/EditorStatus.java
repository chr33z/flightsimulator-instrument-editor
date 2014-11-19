package de.easemotion.fie.model;

public class EditorStatus {

	private static boolean encoderEditLeft = false;
	
	private static boolean encoderEditRight = false;

	public static boolean isEncoderEditRight() {
		return encoderEditRight;
	}

	public static void setEncoderEditRight(boolean edit) {
		EditorStatus.encoderEditRight = edit;
		EditorStatus.encoderEditLeft = false;
	}

	public static boolean isEncoderEditLeft() {
		return encoderEditLeft;
	}

	public static void setEncoderEditLeft(boolean edit) {
		EditorStatus.encoderEditLeft = edit;
		EditorStatus.encoderEditRight = false;
	}
	
	public static void deactivateEncoderEdit(){
		EditorStatus.encoderEditLeft = false;
		EditorStatus.encoderEditRight = false;
	}
}
