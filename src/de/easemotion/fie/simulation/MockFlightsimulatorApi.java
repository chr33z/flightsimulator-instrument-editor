package de.easemotion.fie.simulation;

import java.util.HashMap;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

import de.easemotion.fie.model.ImageLayer;
import de.easemotion.fie.model.Instrument;
import de.easemotion.fie.model.Layer;
import de.easemotion.fie.simulation.SimulationData.Data;

/**
 * This class provides an API to connect to lua script. Every subclass 
 * is one function that can be called from lua and has to be registeres in the base class
 * {@link #call(LuaValue, LuaValue)} function. The name "api" given in 
 * 
 * <code>
 * env.set( "api", library );
 * </code>
 * 
 * is used to reference the function from lua, for example api.getSimConnectVariable().
 * Functions have to extend from a class OneArgFunction, TwoArgFunction or ThreeArgFuntion.
 * For every function that requires more than three parameter, an own class has to be implemented
 * 
 * See details: http://luaj.org/luaj/README.html
 * 
 * @author Christopher Gebhardt
 * @date Nov 24, 2014
 * @project Flightsimulator-Instrument-Editor
 *
 */
public class MockFlightsimulatorApi extends TwoArgFunction {

	private static HashMap<String, Double> values = new HashMap<String, Double>();
	
	private static Instrument instrument;

	public MockFlightsimulatorApi(Instrument instrument, SimulationData data) {
		setVariable(data);
	}
	
	public void setData(SimulationData data){
		values.clear();
		setVariable(data);
	}
	
	private void setVariable(SimulationData data){
		if(data != null){
			for (int i = 0; i < SimulationData.MAX_DATA; i++) {
				Data dataTupel = data.getData(i);

				if(dataTupel != null){
					try {
						values.put(dataTupel.variable, Double.valueOf(dataTupel.value));
					} catch(NumberFormatException e){
						values.put(dataTupel.variable, 0.0);
					}
				}
			}
		}
	}

	@Override
	public LuaValue call(LuaValue modname, LuaValue env) {
		LuaValue library = tableOf();
		library.set( "getSimConnectVariable", new getSimConnectVariable() );
		library.set( "radian2Degree", new radian2Degree() );
		library.set( "degree2Radian", new degree2Radian() );
		library.set( "celsius2Fahrenheit", new celsius2Fahrenheit() );
		library.set( "fahrenheit2Celsius", new fahrenheit2Celsius() );
		env.set( "api", library );
		return library;
	}

	/******************
	 * Function to request the value of a sim connect variable
	 */

	static class getSimConnectVariable extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue simConnectString) {
			String scs = simConnectString.tojstring();
			if(scs != null){
				Double value = values.get(scs);
				if(value != null){
					return LuaValue.valueOf(value);
				}
			}
			return valueOf(0.0);
		}
	}
	
	/**********************
	 * Conversion functions
	 */
	
	static class radian2Degree extends OneArgFunction {
		
		@Override
		public LuaValue call(LuaValue radian) {
			try {
				double r = radian.todouble();
				double degree = (r * 180) / 3.14159;
				return valueOf(degree);
			} catch(LuaError e){
				return valueOf(0.0);
			}
		}
	}
	
	static class degree2Radian extends OneArgFunction {
		
		@Override
		public LuaValue call(LuaValue degree) {
			try {
				double d = degree.todouble();
				double radian = (d * 3.14159) / 180.0;
				return valueOf(radian);
			} catch(LuaError e){
				return valueOf(0.0);
			}
		}
	}
	
	static class celsius2Fahrenheit extends OneArgFunction {
		
		@Override
		public LuaValue call(LuaValue celsius) {
			try {
				double c = celsius.todouble();
				double fahrenheit = c * (9.0 / 5.0) + 32;
				return valueOf(fahrenheit);
			} catch(LuaError e){
				return valueOf(0.0);
			}
		}
	}
	
	static class fahrenheit2Celsius extends OneArgFunction {
		
		@Override
		public LuaValue call(LuaValue fahrenheit) {
			try {
				double f = fahrenheit.todouble();
				double celsius = (f - 32) * (5.0 / 9.0);
				return valueOf(celsius);
			} catch(LuaError e){
				return valueOf(0.0);
			}
		}
	}
	
}
