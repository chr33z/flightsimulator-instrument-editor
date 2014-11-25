package de.easemotion.fie.simulation;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import org.apache.log4j.varia.ReloadingPropertyConfigurator;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import de.easemotion.fie.data.LuaScriptParser;
import de.easemotion.fie.model.Instrument;
import de.easemotion.fie.model.Layer;
import de.easemotion.fie.simulation.SimulationData.Data;

public class SimulationInstrument extends Observable {

	public Instrument instrument;

	private SimulationData data;

	private MockFlightsimulatorApi api;

	private String script;

	// create standard global lua variables
	Globals globals;

	LuaValue main = null;
	
	/** 
	 * the instrument as lua representation to pass#
	 * to our main function
	 */
	LuaValue luaInstrument;

	/**
	 * A list of all layer function names occuring in the script.
	 * Is updated as soon as a new instrument is loaded
	 */
	List<String> functionNames = new ArrayList<String>();

	/**
	 * Update the instrument and simulation data
	 * @param instrument
	 * @param data
	 */
	public void init(Instrument instrument, SimulationData data){
		if(instrument == null || data == null){
			// TODO error handling
			System.err.println("[warning] Instrument or data is null when performing update");
			return;
		}

		// parse lua script from instrument
		script = LuaScriptParser.instrumentToLua(instrument, true);
		// replace api calls with simulation api calls
		script = prepareScriptForSimulation(script);
		
		String[] codeLines = script.split("\n");
		int index = 1;
		for (String string : codeLines) {
			System.out.println(index+": "+string);
			index++;
		}

		// copy instrument to keep original data
		this.instrument = instrument.copy();
		
		// get all function names occurring in the script
//		updateFunctionNames();

		if(api == null){
			api = new MockFlightsimulatorApi(this.instrument, data);
		}

		update(data);
	}

	/**
	 * Update Simulation values
	 * @param data
	 */
	public void update(SimulationData data){
		if(data == null || instrument == null){
			System.err.println("[warning] Instrument or data is null when performing update");
			return;
		}

		if(api == null){
			this.data = data;
			api = new MockFlightsimulatorApi(instrument, data);
		}

		api.setData(data);
		reloadLuaScript();
	}

	/**
	 * Load a lua script from the current instrument
	 * @param instrument
	 */
	private void reloadLuaScript(){
		// initialize global environment
		globals = JsePlatform.standardGlobals();

		// expose api functions to lua code
		globals.load(api);

		// load lua script
		if(script != null && !script.equals("")){
			globals.load(new StringReader(script), "script").call();
		}
		
		// convert instrument to lua code
		luaInstrument = CoerceJavaToLua.coerce(instrument);

		// get main function from script
		main = globals.get(LuaScriptParser.MAIN_FUNCTION_NAME);
	}

	/**
	 * Run one render step on the data
	 */
	public void run(){
		if(globals == null){
			System.err.println("[warning] Globals are null");
			return;
		}
		System.out.println("[info] running");
		
		// call main function
		if (!main.isnil()) {  
			main.call(luaInstrument);  
		} else {  
			System.out.println("Main function not found");  
		} 

		updateObservers();
	}

	public void updateObservers(){
		setChanged();
		notifyObservers();
	}

	public Instrument getInstrument(){
		return instrument;
	}

	/**
	 * Get all function names from instrument and place them
	 * in {@value SimulationInstrument.funtionNames}
	 * 
	 * @param instrument
	 */
	private void updateFunctionNames(){
		functionNames.clear();

		for (Layer layer : instrument.getLayers()) {
			if(layer != null){
				functionNames.add("function_" + layer.getId());
			}
		}
	}

	/**
	 * Note: See documentation for simulation at the top of this class
	 * 
	 * Replaces every occurence of an api call that is meant to manipulate
	 * a layer (e.g "api.rotateLayerAbs()) to an call aacti upon an instance
	 * of instrument (e.g. instrument:rotateLayerAbs())
	 * 
	 * @param script
	 * @return prepared script
	 */
	private String prepareScriptForSimulation(String script){
		script = script.replace("api.rotate", "sim_instrument:rotate");
		script = script.replace("api.translate", "sim_instrument:translate");

		return script;
	}
}
