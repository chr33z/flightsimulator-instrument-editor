package de.easemotion.fie.lua;

import de.easemotion.fie.model.graphics.GraphicSurface;
import de.easemotion.fie.model.graphics.Layer;
import de.easemotion.fie.utils.Utils;

/**
 * Parser class to convert either an instrument in eDesigner into a runnable lua script
 * or a runnable luascript (compiled from eDesigner) into a eDesigner compatible
 * layer configuration
 *  
 * @author Christopher Gebhardt
 * @date Oct 29, 2014
 * @project Flightsimulator-Instrument-Editor
 *
 */
public class LuaParser {
	
	private static final String LUA_SCRIPT_LAYOUT = ""
			+ "%s\n"
			+ "\n"
			+ "%s\n"
			+ "\n"
			+ "%s\n";
	
	private static final String INSTRUMENT_LAYOUT_HEADER = "--[[\n"
			+ "\tLayout definition for instrument \"%s\" \n"
			+ "]]\n";
	
	private static final String MAIN_FUNCTION_HEADER = "--[[\n"
			+ "\tDefinition API_MAIN function:\n"
			+ "\tThis function is called in every render step \n"
			+ "]]\n";
	
	private static final String LAYER_FUNCTION_HEADER = "--[[\n"
			+ "\tDefinition layer function \"%s\" \n"
			+ "]]\n";
	
	private static final String LAYER_FUNCTION_TEMPLATE = "function_%s = function()\n"
			+ "%s"
			+ "end\n";
	
	private static final String LAYER_FUNCTION_CALL_TEMPLATE = "function_%s()\n";
	
	private static final String MAIN_FUNCTION_TEMPLATE = "API_MAIN = function()\n"
			+ "%s"
			+ "end\n";

	/**
	 * Parse an instrument to a lua script
	 * 
	 * @param surface
	 * @return instrument as a string describing the lua script
	 */
	public static String instrumentToLua(GraphicSurface surface){
		/*
		 * First part: Lua layout file with all layers
		 */
		String layout = INSTRUMENT_LAYOUT_HEADER;
		layout += "instrument = {\n";
		for (Layer layer : surface.getLayers()) {
			layout += "\t" + layer.getId() + " = {\n";
			layout += "\t\twidth = " + layer.getWidth() + ",\n";
			layout += "\t\theight = " + layer.getHeight() + ",\n";
			layout += "\t\tleft = " + layer.getLeft() + ",\n";
			layout += "\t\ttop = " + layer.getTop() + ",\n";

			// Strip image to filename
			String imageDay = "";
			if(Utils.notEmpty(layer.getImageDay()) && Utils.isFile(layer.getImageDay())){
				imageDay = layer.getImageDay().substring(layer.getImageDay().lastIndexOf("/")+1);
			}
			layout += "\t\timage_day = " + imageDay + "\n";
			
			// Strip image to filename
			String imageNight = "";
			if(Utils.notEmpty(layer.getImageNight()) && Utils.isFile(layer.getImageNight())){
				imageNight = layer.getImageDay().substring(layer.getImageDay().lastIndexOf("/")+1);
			}
			layout += "\t\timage_night = " + imageNight + "\n";

			layout += "\t},\n";
		}
		layout += "}\n";


		/*
		 * Parse layer functions
		 */
		String layerFunctions = "";
		for (Layer layer : surface.getLayers()) {
			layerFunctions += String.format(LAYER_FUNCTION_HEADER, layer.getId());
			layerFunctions += String.format(LAYER_FUNCTION_TEMPLATE, layer.getId(), layer.getLuaScript()) + "\n";
		}
		
		String mainFunctionContent = "";
		for (Layer layer : surface.getLayers()) {
			mainFunctionContent += "\t" + String.format(LAYER_FUNCTION_CALL_TEMPLATE, layer.getId()) + "\n";
		}
		
		String mainFunction = MAIN_FUNCTION_HEADER;
		mainFunction += String.format(MAIN_FUNCTION_TEMPLATE, mainFunctionContent);
		
		/*
		 * Collect all parts and store as script
		 */
		String luaScript = String.format(LUA_SCRIPT_LAYOUT, 
				new Object[]{layout, layerFunctions, mainFunction});
		
		return luaScript;
	}
}
