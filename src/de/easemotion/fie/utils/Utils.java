package de.easemotion.fie.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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

	public static class file {
		
		public static boolean copy(String source, String target){
			try {
				Files.copy(Paths.get(source), Paths.get(target), StandardCopyOption.REPLACE_EXISTING);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		/**
		 * @param path
		 * @return true if File at path is a file
		 */
		public static boolean isFile(String path){
			File file = new File(path);
			if(file.exists()){
				return true;
			} else {
				return false;
			}
		}

	}
}
