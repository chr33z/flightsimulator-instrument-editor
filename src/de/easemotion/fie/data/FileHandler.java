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
import java.io.StringReader;
import java.nio.file.DirectoryNotEmptyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.sun.org.apache.bcel.internal.generic.LUSHR;

import de.easemotion.fie.model.ImageLayer;
import de.easemotion.fie.model.Instrument;
import de.easemotion.fie.model.Layer;
import de.easemotion.fie.model.TextLayer;
import de.easemotion.fie.simulation.MockFlightsimulatorApi;
import de.easemotion.fie.simulation.SimulationData;
import de.easemotion.fie.utils.Constants;
import de.easemotion.fie.utils.Utils;

/**
 * This class saves the current state of the instument designer and stores all needed files in a *.zip file
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
		File outputDirectory = Files.createTempDir();
		System.out.println(outputDirectory);
		Utils.zip.unzipDirectory(file, outputDirectory);

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
			//			try {
			//				Utils.file.delete(outputDirectory);
			//			} catch (IOException e1) {
			//				e1.printStackTrace();
			//			}
			listener.onError(Error.FILE_MISSING);
			return;
		}
		
		// Read file as string
		String scriptContent = null;
		try {
			scriptContent = Files.toString(new File(script), Charsets.UTF_8);
			
			// replace all \ with / because of escape characters in luascript
			scriptContent = scriptContent.replaceAll("\\\\", "/");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// create standard global lua variables
		Globals globals = JsePlatform.standardGlobals();
		globals.load(new StringReader(scriptContent), "script").call();

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
		 * 
		 * NOTE: Does not work as expected... deactivated for now
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
					//					order = orderBackup;
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
			//			try {
			//				Utils.file.delete(outputDirectory);
			//			} catch (IOException e1) {
			//				e1.printStackTrace();
			//			}
			e.printStackTrace();
		}

		/*
		 * 5. Step
		 * 
		 * IMPORTANT NOTE:
		 * Read script line by line and extract function content
		 * To do this a function must end with "-- END" after the last
		 * end TAG!
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
					List<String> content = new ArrayList<String>();
					String end = iter.next();

					while(iter.hasNext() && !end.equals("-- END")){
						content.add(end + "\n");
						end = iter.next();
					}

					Layer l = instrument.getLayers().get(i);
					if(l != null){
						String appended = "";
						/*
						 * Append all but the last line, which is the end tag of our function.
						 * We dont need that
						 */
						for (int j = 0; j < content.size()-1; j++) {
							appended += content.get(j);
						}
						l.setLuaScript(appended);
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

		//		try {
		//			Utils.file.delete(outputDirectory);
		//		} catch (IOException e1) {
		//			e1.printStackTrace();
		//		}
		
		System.out.println(System.getProperty("file.separator"));
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
			layer.setBias(parameter.get("bias").toint());

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
	 * Save a instrument configuration in a zip file
	 * 
	 * There are several steps to save a configuration
	 * 1. Check if Lua-Script is valid
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


		/*
		 * Step 1
		 * 
		 * Check against Simulation api first. Then parse lua script without simulation
		 * properties and proceed
		 */
		String scriptToCheck = LuaScriptParser.instrumentToLua(instrument, true);

		MockFlightsimulatorApi api = new MockFlightsimulatorApi(instrument, new SimulationData());
		Globals globals = JsePlatform.standardGlobals();
		globals.load(api);
		
		/*
		 * To check the script we replace the api calls (like for the simulation)
		 * and check against our mock api
		 */
		scriptToCheck = scriptToCheck.replace("api.rotate", "sim_instrument:rotate");
		scriptToCheck = scriptToCheck.replace("api.translate", "sim_instrument:translate");
		
		// Print for debugging
		String[] codeLines = scriptToCheck.split("\n");
		int index = 1;
		for (String string : codeLines) {
			System.out.println(index+": "+string);
			index++;
		}

		globals.load(new StringReader(scriptToCheck), "script").call();

		LuaValue luaInstrument = CoerceJavaToLua.coerce(instrument);
		if(luaInstrument.isnil()){
			System.out.println("[error] Lua instrument is nil");  
		}

		LuaValue main = globals.get(LuaScriptParser.MAIN_FUNCTION_NAME);
		if (!main.isnil()) {  
			main.call(luaInstrument);  
		} else {  
			System.out.println("[error] Main function not found");  
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
					Utils.file.copy( ((ImageLayer) layer).getImage().imageNight, nightImageCopy);
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

				/**
				 * When setting an image the pivot is automatically reset.
				 * We have to store the pivots and reset them after image is loaded
				 */
				int pivX = layer.getPivotX();
				int pivY = layer.getPivotY();

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

				layer.setPivotX(pivX);
				layer.setPivotY(pivY);
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
