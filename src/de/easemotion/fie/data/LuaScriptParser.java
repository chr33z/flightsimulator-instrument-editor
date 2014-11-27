package de.easemotion.fie.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sun.font.TextLabel;
import de.easemotion.fie.model.ImageLayer;
import de.easemotion.fie.model.Instrument;
import de.easemotion.fie.model.Layer;
import de.easemotion.fie.model.TextLayer;
import de.easemotion.fie.utils.Constants;
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
public class LuaScriptParser {
	
	public static final String MAIN_FUNCTION_NAME = "API_MAIN";

	private static final String LUA_SCRIPT_LAYOUT = ""
			+ "%s\n" // layout
			+ "\n"
			+ "%s\n" // layer functions
			+ "\n"
			+ "%s\n" // main function
			+ "\n"
			+ "%s\n" // encoder left
			+ "\n"
			+ "%s\n";// encoder right

	private static final String INSTRUMENT_LAYOUT_HEADER = "--[[\n"
			+ "\tLayout definition for instrument \"%s\" \n"
			+ "]]\n";

	private static final String MAIN_FUNCTION_HEADER = "--[[\n"
			+ "\tDefinition API_MAIN function:\n"
			+ "\tThis function is called in every render step \n"
			+ "]]\n";

	private static final String ENCODER_LEFT_HEADER = "--[[\n"
			+ "\tDefinition encoder left function:\n"
			+ "\tThis function is NOT called in every render step \n"
			+ "]]\n";

	private static final String ENCODER_RIGHT_HEADER = "--[[\n"
			+ "\tDefinition encoder right function:\n"
			+ "\tThis function is NOT called in every render step \n"
			+ "]]\n";

	private static final String LAYER_FUNCTION_HEADER = "--[[\n"
			+ "\tDefinition layer function \"%s\" \n"
			+ "]]\n";

	private static final String ENCODER_LEFT_TEMPLATE = "function_encoder_left = function()\n"
			+ "%s\n"
			+ "end\n"
			+ "-- END\n";

	private static final String ENCODER_RIGHT_TEMPLATE = "function_encoder_right = function()\n"
			+ "%s\n"
			+ "end\n"
			+ "-- END\n";

	private static final String LAYER_FUNCTION_TEMPLATE = "function_%s = function()\n"
			+ "%s\n"
			+ "end\n"
			+ "-- END\n";
	
	private static final String LAYER_FUNCTION_TEMPLATE_SIMULATION = "function_%s = function(sim_instrument)\n"
			+ "%s\n"
			+ "end\n"
			+ "-- END\n";

	private static final String LAYER_FUNCTION_CALL_TEMPLATE = "function_%s()\n";
	
	private static final String LAYER_FUNCTION_CALL_TEMPLATE_SIMULATION = "function_%s(sim_instrument)\n";

	private static final String MAIN_FUNCTION_TEMPLATE = MAIN_FUNCTION_NAME +" = function()\n"
			+ "%s\n"
			+ "end\n"
			+ "-- END\n";
	
	private static final String MAIN_FUNCTION_TEMPLATE_SIMULATION = MAIN_FUNCTION_NAME +" = function(sim_instrument)\n"
			+ "%s\n"
			+ "end\n"
			+ "-- END\n";

	/**
	 * Parse an instrument to lua script
	 * 
	 * @param instrument
	 * @return lua script
	 */
	public static String instrumentToLua(Instrument instrument){
		return instrumentToLua(instrument, false);
	}
	
	/**
	 * Parse an instrument to a lua script with the option to set an simulation mode
	 * In this mode the functions of each layer get an additional parameter "instrument"
	 * that is a java object of Instrument. Each api call regarding instrument is then 
	 * diverted to be called on the instrument object directly
	 * 
	 * @param instrument
	 * @return instrument as a string describing the lua script
	 */
	public static String instrumentToLua(Instrument instrument, boolean simulation){
		/*
		 * First part: Lua layout file with all layers
		 */
		String layout = INSTRUMENT_LAYOUT_HEADER;
		layout += "instrument = {\n";
		layout += "\tname = \""+ instrument.getInstrumentName() +"\",\n";

		int order = 0;
		
		// reverse layers to provide correct drawing order
		List<Layer> layerList = new ArrayList<Layer>(instrument.getLayers());
		Collections.reverse(layerList);
		
		for (Layer layer : layerList) {
			if(layer != null){

				layout += "\t" + layer.getId() + " = {\n";
				layout += "\t\torder = " + (Constants.integer.MAX_LAYER_COUNT - 1 - order) + ",\n";
				layout += "\t\tleft = " + layer.getLeft() + ",\n";
				layout += "\t\ttop = " + layer.getTop() + ",\n";

				if(layer instanceof ImageLayer){
					ImageLayer imageLayer = (ImageLayer) layer;

					layout += "\t\ttype = \"image\",\n";
					layout += "\t\twidth = " + imageLayer.getWidth() + ",\n";
					layout += "\t\theight = " + imageLayer.getHeight() + ",\n";
					layout += "\t\tpivot_left = " + imageLayer.getPivotX() + ",\n";
					layout += "\t\tpivot_top = " + imageLayer.getPivotY() + ",\n";
					layout += "\t\tbias = " + imageLayer.getBias() + ",\n";
					layout += "\t\timage_day = \"" + imageLayer.getImage().imageDay.getPath() + "\",\n";
					layout += "\t\timage_night = \"" + imageLayer.getImage().imageNight.getPath() + "\"\n";

				} else if(layer instanceof TextLayer){
					TextLayer textLayer = (TextLayer) layer;

					layout += "\t\ttype = \"text\",\n";
					layout += "\t\tfont_size = " + textLayer.getFontSize() + ",\n";
					layout += "\t\tfont = \"" + textLayer.getFont() + "\"\n";
				}
				layout += "\t},\n";
			}
			order++;
		}
		layout += "}\n";


		/*
		 * Parse layer functions
		 */
		String layerFunctions = "";
		for (Layer layer : instrument.getLayers()) {
			if(layer != null){
				layerFunctions += String.format(LAYER_FUNCTION_HEADER, layer.getId());
				
				if(!simulation){
					layerFunctions += String.format(LAYER_FUNCTION_TEMPLATE, layer.getId(), 
							layer.getLuaScript()) + "\n";
				} else {
					layerFunctions += String.format(LAYER_FUNCTION_TEMPLATE_SIMULATION, layer.getId(), 
							layer.getLuaScript()) + "\n";
				}
			}
		}

		String mainFunctionContent = "";
		for (Layer layer : instrument.getLayers()) {
			if(layer != null){
				
				if(!simulation){
					mainFunctionContent += "\t" + String.format(LAYER_FUNCTION_CALL_TEMPLATE, 
							layer.getId()) + "\n";
				} else {
					mainFunctionContent += "\t" + String.format(LAYER_FUNCTION_CALL_TEMPLATE_SIMULATION, 
							layer.getId()) + "\n";
				}
			}
		}

		String mainFunction = MAIN_FUNCTION_HEADER;
		if(!simulation){
			mainFunction += String.format(MAIN_FUNCTION_TEMPLATE, mainFunctionContent);
		} else {
			mainFunction += String.format(MAIN_FUNCTION_TEMPLATE_SIMULATION, mainFunctionContent);
		}

		String encoderLeft = ENCODER_LEFT_HEADER;
		encoderLeft += String.format(ENCODER_LEFT_TEMPLATE, instrument.getCodeEncoderLeft() + "\n");

		String encoderRight = ENCODER_RIGHT_HEADER;
		encoderRight += String.format(ENCODER_RIGHT_TEMPLATE, instrument.getCodeEncoderRight() + "\n");

		/*
		 * Collect all parts and store as script
		 */
		String luaScript = String.format(LUA_SCRIPT_LAYOUT, 
				new Object[]{layout, layerFunctions, mainFunction, encoderLeft, encoderRight});

		return luaScript;
	}
}
