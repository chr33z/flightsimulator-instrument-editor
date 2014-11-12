package de.easemotion.fie.view;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.content.ButtonData;

import de.easemotion.fie.EditorApplication;
import de.easemotion.fie.model.Instrument;
import de.easemotion.fie.model.Layer;
import de.easemotion.fie.utils.Constants;
import de.easemotion.fie.utils.IconLoader;
import de.easemotion.fie.utils.IconLoader.Icon;

public class InstrumentNamePanel extends BoxPane implements Observer {
	
	private static final String TAG = InstrumentNamePanel.class.getSimpleName();

	private Instrument instrument;

	private EditorApplication editor;
	
	private TextInput nameInput;
	private LinkButton buttonDelete;

	public InstrumentNamePanel(EditorApplication editor, final Instrument instrument){
		this.editor = editor;
		this.instrument = instrument;

		try {
			BXMLSerializer s = new BXMLSerializer();
			Component component = (Component) s.readObject(InstrumentNamePanel.class, "instrument_name_panel.bxml");
			
			nameInput = (TextInput) s.getNamespace().get("instrument_name");
			nameInput.getStyles().put("color", Constants.color.TEXT_PRIMARY);
			nameInput.getStyles().put("font", Constants.font.FONT_REGULAR);
			nameInput.getComponentKeyListeners().add(new ComponentKeyListener() {
				
				@Override
				public boolean keyTyped(Component component, char character) {
					if(!String.valueOf(character).matches("([A-Za-z0-9\\_ \b\n\r\\x00\\x08\\x0B\\x0C\\x0E-\\x1F]+)")){
						String txt = nameInput.getText();
						nameInput.setText(txt.substring(0, txt.length()-1));
						return true;
					} else {
						instrument.setInstrumentName(nameInput.getText());
					}
					return false;
				}
				
				@Override
				public boolean keyReleased(Component component, int keyCode,
						KeyLocation keyLocation) {
					return false;
				}
				
				@Override
				public boolean keyPressed(Component component, int keyCode,
						KeyLocation keyLocation) {
					return false;
				}
			});
			
			buttonDelete = (LinkButton) s.getNamespace().get("button_delete");
			buttonDelete.setButtonData(new ButtonData(IconLoader.icons.get(Icon.DELETE)[IconLoader.DEACTIVE]));
			buttonDelete.getButtonPressListeners().add(new ButtonPressListener() {
				
				@Override
				public void buttonPressed(Button button) {
					nameInput.setText("");
				}
			});
			
			this.add(component);
		} catch (IOException | SerializationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		instrument = editor.getInstrument();
		if(nameInput != null){
			nameInput.setText(instrument.getInstrumentName());
		}
	}
}
