package de.easemotion.fie.view;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;

import de.easemotion.fie.EditorApplication;
import de.easemotion.fie.model.graphics.Instrument;
import de.easemotion.fie.model.graphics.Layer;

public class InstrumentNamePanel extends BoxPane implements Observer {
	
	private static final String TAG = InstrumentNamePanel.class.getSimpleName();

	private Instrument surface;

	private EditorApplication editor;
	
	private TextInput nameInput;

	public InstrumentNamePanel(EditorApplication editor, final Instrument surface){
		this.editor = editor;
		this.surface = surface;

		try {
			BXMLSerializer s = new BXMLSerializer();
			Component component = (Component) s.readObject(InstrumentNamePanel.class, "instrument_name_panel.bxml");
			
			nameInput = (TextInput) s.getNamespace().get("instrument_name_input");
			
			this.add(component);
		} catch (IOException | SerializationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		
	}
}
