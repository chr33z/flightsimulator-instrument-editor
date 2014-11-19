package de.easemotion.fie.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.JsePlatform;

import com.sun.org.apache.bcel.internal.generic.LUSHR;

import de.easemotion.fie.model.ImageLayer;
import de.easemotion.fie.model.Instrument;
import de.easemotion.fie.model.Layer;
import de.easemotion.fie.model.TextLayer;
import de.easemotion.fie.utils.Constants;
import de.easemotion.fie.utils.Utils;

/**
 * This class saves the current state of the instument designer and stores all needed files in a *.zip file
 * 
 * There are several steps to save a configuration
 * 1. Check if user provided directory exists and file name is valid
 * 2. Create a temporary directory with name [name] and subdirectory "images". This is the directory we are going to pack
 * 3. Read instrument configuration and copy all used images to "[name]/images/"
 * 4. Change all image paths in instrument to "images/[image name]" (copy instrument to keep original file)
 * 5. Generate script from the instrument
 * 6. zip directory [name]
 * 7. when zip package is generated without errors, delete directory [name] 
 * 
 * @author Christopher Gebhardt
 * @date Nov 6, 2014
 * @project Flightsimulator-Instrument-Editor
 *
 */
public class FileHandler {

	public interface LoadInstrumentListener {

		public void onSuccess(Instrument instrument);

		public void onError(Error error);
	}

	public enum Error {
		FILE_MISSING,
		DIRECTORY_INVALID,
		FILE_NAME_INVALID,
		WRITE_SCRIPT_TO_FILE,
		COPY_IMAGES,
		ZIP_FAILED,
		LUA_PARSE_ERROR,
		NONE
	}

	private static HashMap<Error, Boolean> errors = new HashMap<>();

	/**
	 * Load an instrument from a zip file
	 * 
	 * There are several steps to load an instrument configuration
	 * 1. Get and comfirm zip file
	 * 2. Unzip contents to a temp directory in the same directory as the zip
	 * 3. Load contained lua script as a lua script
	 * 4. Parse the layout attributes and generate the layers
	 * 5. read the contents of the separate layer functions and store them in each layer
	 * 6. Read the contents of the separate encoder functions and store them in the instrument
	 * 8. (delete temp folder)
	 * 
	 * @param file
	 * @return
	 */
	public static void load(File file, LoadInstrumentListener listener){
		if(listener == null){
			throw new IllegalArgumentException("LoadInstrumentListener must not be null!");
		}
		/*
		 * 1. Step
		 */
		if(file == null || !file.exists()){
			listener.onError(Error.FILE_MISSING);
			return;
		}

		/*
		 * 2. Step
		 */
		File outputDirectory = new File(file.getParentFile(), 
				file.getName().substring(0, file.getName().lastIndexOf(".")));
		unzipDirectory(file, outputDirectory);

		/*
		 * 3. Step
		 */
		String script = "";
		File[] files = outputDirectory.listFiles();
		for (File f : files) {
			int i = f.getName().lastIndexOf('.');
			if (i > 0) {
				String extension = f.getName().substring(i+1);

				if(extension.equals("lua")){
					script = f.getAbsolutePath();
					break;
				}
			}
		}

		if(script.equals("")){
			listener.onError(Error.FILE_MISSING);
			return;
		}

		// create standard global lua variables
		Globals globals = JsePlatform.standardGlobals();

		// load lua script
		try {
			globals.load(new FileReader(script), "script").call();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// get instrument table and iterate over layers
		LuaTable instrumentTable = (LuaTable) globals.get("instrument");
		
		// create new instrument to load the layers into
		Instrument instrument = new Instrument();
		instrument.setInstrumentName(instrumentTable.get("name").tojstring());
		
		/*
		 * 4. Step
		 */
		LuaValue layer = LuaValue.NIL;
		
		/**
		 * If everything works fine we can extract the layer order from
		 * the layout file in the lua script. If not we provide a 
		 * backup order that starts to kick in when we detect that the
		 * layout order is read wrong from the script
		 */
		int orderBackup = Constants.integer.MAX_LAYER_COUNT-1;
		int firstOrder = -1;
		
		while ( true ) {
			Varargs n = instrumentTable.next(layer);
			if ( (layer = n.arg1()).isnil() ){
				break;
			}
			LuaValue parameter = n.arg(2);

			if(parameter.istable()){
				
				// order in editor layer panel
				int order = parameter.get("order").toint();
				
				if(firstOrder == -1){
					firstOrder = order;
				}
				
				if(firstOrder != -1 && order == 0){
					order = orderBackup;
				}
				
				Layer newLayer = parseLayer(layer, (LuaTable) parameter, outputDirectory);
				if(newLayer != null){
					instrument.addLayer(order, newLayer);
				}

			}
			orderBackup--;
		}

		// Read script line by line
		List<String> scriptLines = new ArrayList<>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(script));
			String line = "";
			while ((line = br.readLine()) != null) {
				scriptLines.add(line);
			}
			br.close();
		} catch (IOException e) {
			/*
			 * TODO errorhandling: show user dialog "script is bad"
			 */
			e.printStackTrace();
		}

		/*
		 * 5. Step
		 */
		String[] functionNames = new String[instrument.getLayers().size()];
		for (int i = 0; i < instrument.getLayers().size(); i++) {
			Layer l = instrument.getLayers().get(i);
			
			String layerName = l != null ? l.getId() : "~";
			functionNames[i] = "function_" + layerName;
		}

		Iterator<String> iter = scriptLines.iterator();
		while(iter.hasNext()){
			String line = iter.next();

			for (int i = 0; i < functionNames.length; i++) {
				String f = functionNames[i];

				// when function start is found, store all lines until we reach "end"
				if(line.startsWith(f)){
					String content = "";
					String end = iter.next();

					while(iter.hasNext() && !end.equals("end")){
						content += end + "\n";
						end = iter.next();
					}
					System.out.println("Function: "+f);
					System.out.println(content);
					
					Layer l = instrument.getLayers().get(i);
					if(l != null){
						l.setLuaScript(content);
					}
				}
			}
		}

		/*
		 * 6. Step
		 */
		String[] encoderNames = new String[]{ "function_encoder_left", "function_encoder_right" };
		iter = scriptLines.iterator();
		while(iter.hasNext()){
			String line = iter.next();
			
			for (int i = 0; i < encoderNames.length; i++) {
				String f = encoderNames[i];

				// when function start is found, store all lines until we reach "end"
				if(line.startsWith(f)){
					String content = "";
					String end = iter.next();

					while(iter.hasNext() && !end.equals("end")){
						content += end + "\n";
						end = iter.next();
					}
					System.out.println("Encoder: "+f);
					System.out.println(content);
					if(i == 0){
						instrument.setCodeEncoderLeft(content);
					} else if(i == 1){
						instrument.setCodeEncoderRight(content);
					}
				}
			}
		}

		listener.onSuccess(instrument);
	}

	/**
	 * Parse a layer defined in a layout table of our script
	 * @param l
	 * @param parameter
	 * @param scriptDirectory
	 * @return a parsed layer or null
	 */
	private static Layer parseLayer(LuaValue l, LuaTable parameter, File scriptDirectory){
		String type = parameter.get("type").tojstring();

		if(type.equals("image")){
			ImageLayer layer = new ImageLayer();
			layer.setId(l.tojstring());
			layer.setTop(parameter.get("top").toint());
			layer.setLeft(parameter.get("left").toint());
			layer.setPivotX(parameter.get("pivot_left").toint());
			layer.setPivotY(parameter.get("pivot_top").toint());

			String day = !parameter.get("image_day").isnil() ? parameter.get("image_day").tojstring() : "";
			layer.setImageDay(new File(scriptDirectory, day));
			String night = !parameter.get("image_night").isnil() ? parameter.get("image_night").tojstring() : "";
			layer.setImageNight(new File(scriptDirectory, night));

			return layer;

		} else if(type.equals("text")){
			TextLayer layer = new TextLayer();
			layer.setId(l.tojstring());
			layer.setTop(parameter.get("top").toint());
			layer.setLeft(parameter.get("left").toint());
			layer.setFont(parameter.get("font").tojstring());
			layer.setFontSize(parameter.get("font_size").toint());
			
			return layer;
		}

		return null;
	}

	/**
	 * Unzip it
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
				File newFile = new File(outputDirectory.getAbsolutePath() + File.separator + fileName);

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

	/**
	 * Save a instrument configuration in a zip file
	 * 
	 * There are several steps to save a configuration
	 * 1. Check if user provided directory exists and file name is valid
	 * 2. Create a temporary directory with name [name] and subdirectory "images". This is the directory we are going to pack
	 * 3. Read instrument configuration and copy all used images to "[name]/images/"
	 * 4. Change all image paths in instrument to "images/[image name]" (copy instrument to keep original file)
	 * 5. Generate script from the instrument
	 * 6. zip directory [name]
	 * 7. when zip package is generated without errors, delete directory [name] 
	 * 
	 * @param directory
	 * @param fileName
	 * @param instrument
	 * @return
	 */
	public static void saveAndPack(File directory, String fileName, Instrument instrument, LoadInstrumentListener listener){
		if(listener == null){
			throw new IllegalArgumentException("LoadInstrumentListener must not be null!");
		}

		// Step 1
		if(!directory.exists()){
			if(!directory.mkdirs()){
				listener.onError(Error.DIRECTORY_INVALID);
				return;
			}
		}

		// Step 2
		File tmpDir = new File(directory, fileName+"/");
		File tmpImageDir = new File(directory, fileName+"/images/");
		File tmpLuaScript = new File(directory, fileName+"/"+fileName+".lua");
		tmpDir.mkdirs();
		tmpImageDir.mkdirs();

		// Step 3
		for (Layer layer : instrument.getLayers()) {
			if(layer != null && layer instanceof ImageLayer){
				// copy day image
				try {
					File dayImageCopy = new File(tmpImageDir, (((ImageLayer) layer).getImage().imageDay.getName()));
					dayImageCopy.mkdirs();
					dayImageCopy.createNewFile();
					Utils.file.copy(((ImageLayer) layer).getImage().imageDay, dayImageCopy);
				} catch (IOException | NullPointerException e) {
					e.printStackTrace();
				}

				// copy night image
				try {
					File nightImageCopy = new File(tmpImageDir, (((ImageLayer) layer).getImage().imageNight.getName()));
					nightImageCopy.mkdirs();
					nightImageCopy.createNewFile();
					Utils.file.copy( ((ImageLayer) layer).getImage().imageDay, nightImageCopy);
				} catch (IOException | NullPointerException e) {
					e.printStackTrace();
				}
			}
		}

		// Step 4
		Instrument instrumentCopy = instrument.copy();
		for (Layer l : instrumentCopy.getLayers()) {
			if(l != null && l instanceof ImageLayer){
				ImageLayer layer = (ImageLayer) l;

				try {
					layer.setImageDay(new File("images/"+ layer.getImage().imageDay.getName()));
				} catch(NullPointerException e){
					e.printStackTrace();
					// has no day image
				}

				try {
					layer.setImageNight(new File("images/"+ layer.getImage().imageNight.getName()));
				} catch(NullPointerException e){
					e.printStackTrace();
					// has no night image
				}
			}
		}

		// Step 5
		String script = LuaScriptParser.instrumentToLua(instrumentCopy);
		if(script != null && !script.equals("")){

			try {
				FileWriter fw = new FileWriter(tmpLuaScript);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(script);
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				listener.onError(Error.WRITE_SCRIPT_TO_FILE);
				return;
			}
		}

		// Step 6
		try {
			Utils.zip.zipDirectory(tmpDir, Constants.extension.EMI_ZIP);
		} catch (IOException e) {
			e.printStackTrace();
			listener.onError(Error.ZIP_FAILED);
			return;
		}
		
		/*
		 * Try deleting old files to copy
		 */
		try {
			Utils.file.delete(tmpDir);
		} catch (IOException e) {
			/*
			 * TODO tell user that files remain
			 */
			e.printStackTrace();
		}
		
	}
}
