package de.easemotion.fie.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Datatype to store all simulation data the user entered.
 * This includes (for each of the 5 inputs):
 * 
 * - name of the sim connect variable
 * - its value (numerical)
 * 
 * @author Christopher Gebhardt
 * @date Nov 20, 2014
 * @project Flightsimulator-Instrument-Editor
 *
 */
public class SimulationData extends Observable {
	
	public static final int MAX_DATA = 5;

	List<Data> simulationData = new ArrayList<>(MAX_DATA);

	private boolean simulationActive = false;

	public SimulationData() {
		for (int i = 0; i < MAX_DATA; i++) {
			simulationData.add(i, null);
		}
	}

	public boolean isSimulationActive(){
		return simulationActive;
	}

	public void setSimulationActive(boolean active){
		simulationActive = active;
		updateObservers();
	}

	public void toggleSimulationActive(){
		simulationActive = !simulationActive;
		updateObservers();
	}

	/**
	 * Get a data pair at index or null if there is none
	 * @param index
	 * @return
	 */
	public Data getData(int index){
		return simulationData.get(index);
	}

	/**
	 * Set a pair of values at a given index from 0-4.
	 * Higher or lower values will be set to the smallest/largest
	 * possible value
	 * 
	 * @param index
	 */
	public void setData(int index, Data pair){
		if(index < 0){
			index = 0;
		} else if(index > MAX_DATA-1){
			index = MAX_DATA-1;
		}

		simulationData.set(index, pair);

		updateObservers();
	}

	/**
	 * Set a variable at a data pair at index. If there is no data pair
	 * present a new pair will be created with initial value 0
	 * @param index
	 * @param variable
	 */
	public void setSimConnectVariable(int index, String variable){
		if(index < 0){
			index = 0;
		} else if(index > MAX_DATA-1){
			index = MAX_DATA-1;
		}

		if(variable.equals("")){
			simulationData.set(index, null);
		} else {
			Data pair = simulationData.get(index);
			if(pair != null){
				pair.variable = variable;
			} else {
				pair = new Data(variable, 0);
			}
			simulationData.set(index, pair);
		}

		updateObservers();
	}

	/**
	 * Set a value at a data pair at index. If there is no data pair
	 * present, nothing will be set.
	 * @param index
	 * @param value
	 */
	public void setSimConnectValue(int index, int value){
		if(index < 0){
			index = 0;
		} else if(index > MAX_DATA-1){
			index = MAX_DATA-1;
		}

		Data pair = simulationData.get(index);
		if(pair != null){
			pair.value = value;
			simulationData.set(index, pair);
		}

		updateObservers();
	}

	public void increaseValue(int index){
		Data pair = simulationData.get(index);
		if(pair != null){
			if(pair.multiply){
				pair.value += 10;
			} else {
				pair.value += 1;
			}
		}

		updateObservers();
	}

	public void decreaseValue(int index){
		Data pair = simulationData.get(index);
		if(pair != null){
			if(pair.multiply){
				pair.value -= 10;
			} else {
				pair.value -= 1;
			}
		}

		updateObservers();
	}

	public void toggleMultiply(int index){
		Data data = simulationData.get(index);
		if(data != null){
			data.multiply = !data.multiply;
		}

		updateObservers();
	}

	public void updateObservers(){
		setChanged();
		notifyObservers();
	}

	/**
	 * Simple Datatype to store a variable name, a value and a
	 * flag if multiplyer is set
	 * 
	 * @author Christopher Gebhardt
	 * @date Nov 20, 2014
	 * @project Flightsimulator-Instrument-Editor
	 *
	 * @param <String>
	 * @param <Integer>
	 * @param <Boolean>
	 */
	public class Data {

		public String variable;
		public Integer value;
		public boolean multiply = false;

		public Data(String variable, Integer value) {
			this.variable = variable;
			this.value = value;
		}
	}
}
