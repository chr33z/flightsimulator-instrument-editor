package de.easemotion.fie.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import de.easemotion.fie.utils.Constants.extension;

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

		public static boolean copy(File source, File target) {
			return copy(source.getAbsolutePath(), target.getAbsolutePath());
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

		public static void delete(File f) throws IOException {
			if (f.isDirectory()) {
				for (File c : f.listFiles())
					delete(c);
			}
			if (!f.delete())
				throw new FileNotFoundException("Failed to delete file: " + f);
		}
	}

	/**
	 * Methods to zip a directory or file
	 * 
	 * Code taken from:
	 * http://www.avajava.com/tutorials/lessons/how-do-i-zip-a-directory-and-all-its-contents.html
	 * 
	 * @author Christopher Gebhardt
	 * @date Nov 6, 2014
	 * @project Flightsimulator-Instrument-Editor
	 *
	 */
	public static class zip {

		public static void zipDirectory(File directoryToZip, String extension) throws IOException {
			List<File> fileList = new ArrayList<>();
			System.out.println("---Getting references to all files in: " + directoryToZip.getCanonicalPath());
			getAllFiles(directoryToZip, fileList);
			System.out.println("---Creating zip file");
			writeZipFile(directoryToZip, fileList, extension);
			System.out.println("---Done");
		}

		private static void getAllFiles(File dir, List<File> fileList) {
			try {
				File[] files = dir.listFiles();
				for (File file : files) {
					fileList.add(file);
					if (file.isDirectory()) {
						System.out.println("directory:" + file.getCanonicalPath());
						getAllFiles(file, fileList);
					} else {
						System.out.println("     file:" + file.getCanonicalPath());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public static void writeZipFile(File directoryToZip, List<File> fileList, String extenstion) {
			File destination = new File(directoryToZip.getParentFile(), directoryToZip.getName() + extenstion);

			try {
				FileOutputStream fos = new FileOutputStream(destination);
				ZipOutputStream zos = new ZipOutputStream(fos);

				for (File file : fileList) {
					if (!file.isDirectory()) { // we only zip files, not directories
						addToZip(directoryToZip, file, zos);
					}
				}

				zos.close();
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public static void addToZip(File directoryToZip, File file, ZipOutputStream zos) throws FileNotFoundException,
		IOException {

			FileInputStream fis = new FileInputStream(file);

			// we want the zipEntry's path to be a relative path that is relative
			// to the directory being zipped, so chop off the rest of the path
			String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1,
					file.getCanonicalPath().length());
			System.out.println("Writing '" + zipFilePath + "' to zip file");
			ZipEntry zipEntry = new ZipEntry(zipFilePath);
			zos.putNextEntry(zipEntry);

			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zos.write(bytes, 0, length);
			}

			zos.closeEntry();
			fis.close();
		}

		/**
		 * Unzip a file to a directory
		 * 
		 * @param zipFile input zip file
		 * @param output zip file output folder
		 */
		public static void unzipDirectory(File zipFile, File outputDirectory){

			byte[] buffer = new byte[1024];

			try{
				//create output directory is not exists
				if(!outputDirectory.exists()){
					outputDirectory.mkdir();
				}

				//get the zip file content
				ZipInputStream zis = 
						new ZipInputStream(new FileInputStream(zipFile));

				//get the zipped file list entry
				ZipEntry ze = zis.getNextEntry();

				while(ze != null){

					String fileName = ze.getName();
					
					/*
					 * NOTE: 
					 * Some EMI files can contain Windows style separator (WES) characters
					 * that act as escape character in lua code. We want to replace
					 * them with unix style separators "/"
					 * 
					 * Unfortunately emi-files that have been stored with WES can't be recognized
					 * as an image in a subfolder.
					 */
					if(fileName.startsWith("images\\")){
						fileName = fileName.replace("\\", "/");
					}
					
					File newFile = new File(outputDirectory.getAbsolutePath() + "/" + fileName);

					System.out.println("file unzip : "+ newFile.getAbsoluteFile());

					//create all non exists folders
					//else you will hit FileNotFoundException for compressed folder
					new File(newFile.getParent()).mkdirs();

					FileOutputStream fos = new FileOutputStream(newFile);             

					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}

					fos.close();   
					ze = zis.getNextEntry();
				}

				zis.closeEntry();
				zis.close();

				System.out.println("Done");

			}catch(IOException ex){
				ex.printStackTrace(); 
			}
		}    
	}
}
