package de.easemotion.fie.data;

import java.util.ArrayList;
import java.util.List;

import de.easemotion.fie.model.ImageLayer;
import de.easemotion.fie.model.Instrument;
import de.easemotion.fie.model.Layer;

/**
 * Validator to ensure all necessary inputs of an instrument are correct
 * 
 * @author Christopher Gebhardt
 * @date Nov 12, 2014
 * @project Flightsimulator-Instrument-Editor
 *
 */
public class InstrumentValidator {

	/**
	 * 
	 * @param instrument
	 * @return
	 */
	public static List<String> check(Instrument instrument){
		List<String> errors = new ArrayList<>();

		if(instrument.getInstrumentName().equals("")){
			errors.add("Geben Sie einen Namen f�r das Instrument ein");
		}
		
		int count = 0;
		for (int i = 0; i < instrument.getLayers().size(); i++) {
			Layer layer = instrument.getLayers().get(i);

			if(layer != null){
				if(layer.getId().equals("")){
					errors.add("Ebene "+(i+1)+": Geben Sie einen Namen f�r die Ebene ein");
				}
				if(layer instanceof ImageLayer){
					ImageLayer imageLayer = (ImageLayer) layer;
					String layerName = !layer.getId().equals("") ? layer.getId() : "Ebene "+(i+1);

					if(!imageLayer.getImage().imageDay.exists()){
						errors.add(layerName+": F�gen Sie ein Tag-Bild f�r die Ebene hinzu");
					}
					//				if(!imageLayer.getImage().imageNight.exists()){
					//					errors.add(layerName+": Fügen Sie ein Nacht-Bild für die Ebene hinzu");
					//				}
				}
				count++;
			}
		}
		
		if(count == 0){
			errors.add("Sie m�ssen Ebenen hinzuf�gen um ein Instrument speichern zu k�nnen.");
		}

		return errors;
	}
}
