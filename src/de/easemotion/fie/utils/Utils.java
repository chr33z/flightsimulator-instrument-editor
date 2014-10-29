package de.easemotion.fie.utils;

import java.io.File;

public class Utils {

	/*******************
	 * Validation
	 *******************/
	
	public static boolean notEmpty(String string){
		if(string == null || string.equals("")){
			return false;
		} else {
			return true;
		}
	}
	
	public static boolean isFile(String path){
		File file = new File(path);
		if(file.exists()){
			return true;
		} else {
			return false;
		}
	}
}
