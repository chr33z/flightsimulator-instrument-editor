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
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;

import de.easemotion.fie.EditorApplication;
import de.easemotion.fie.model.EditorStatus;
import de.easemotion.fie.model.Instrument;
import de.easemotion.fie.model.Layer;
import de.easemotion.fie.utils.IconLoader;
import de.easemotion.fie.utils.IconLoader.Icon;

public class EncoderSetupPanel extends BoxPane implements Observer {
	
	private static final String TAG = EncoderSetupPanel.class.getSimpleName();

	private Instrument instrument;

	private EditorApplication editor;
	
	LinkButton buttonEncLeftEdit;
	LinkButton buttonEncLeftDelete;
	LinkButton buttonEncRightEdit;
	LinkButton buttonEncRightDelete;
	
	public EncoderSetupPanel(final EditorApplication editor, final Instrument instrument){
		this.editor = editor;
		this.instrument = instrument;

		try {
			BXMLSerializer s = new BXMLSerializer();
			Component component = (Component) s.readObject(EncoderSetupPanel.class, "encoder_setup_panel.bxml");
			
			// left encoder setup
			buttonEncLeftEdit = (LinkButton) s.getNamespace().get("button_encoder_left_edit");
			buttonEncLeftEdit.getButtonPressListeners().add(new ButtonPressListener() {
				
				@Override
				public void buttonPressed(Button button) {
					instrument.setLayersInactive();
					
					editor.enableEncoderEdit(true);
					buttonEncLeftEdit.setButtonData(IconLoader.icons.get(Icon.EDIT)[IconLoader.ACTIVE]);
					buttonEncRightEdit.setButtonData(IconLoader.icons.get(Icon.EDIT)[IconLoader.DEACTIVE]);
				}
			});
			buttonEncLeftDelete = (LinkButton) s.getNamespace().get("button_encoder_left_delete");
			buttonEncLeftDelete.getButtonPressListeners().add(new ButtonPressListener() {
				
				@Override
				public void buttonPressed(Button button) {
					deleteEncoderSetup(true);
				}
			});
			
			// right encoder setup
			buttonEncRightEdit = (LinkButton) s.getNamespace().get("button_encoder_right_edit");
			buttonEncRightEdit.getButtonPressListeners().add(new ButtonPressListener() {
				
				@Override
				public void buttonPressed(Button button) {
					instrument.setLayersInactive();
					
					editor.enableEncoderEdit(false);
					buttonEncRightEdit.setButtonData(IconLoader.icons.get(Icon.EDIT)[IconLoader.ACTIVE]);
					buttonEncLeftEdit.setButtonData(IconLoader.icons.get(Icon.EDIT)[IconLoader.DEACTIVE]);
				}
			});
			buttonEncRightDelete = (LinkButton) s.getNamespace().get("button_encoder_right_delete");
			buttonEncRightDelete.getButtonPressListeners().add(new ButtonPressListener() {
				
				@Override
				public void buttonPressed(Button button) {
					deleteEncoderSetup(false);
				}
			});
			
			this.add(component);
		} catch (IOException | SerializationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Show conformation popup and delete encoder setup
	 * @param left
	 */
	private void deleteEncoderSetup(final boolean left){
		Component body = null;
		BXMLSerializer serializer = new BXMLSerializer();
		try {
			body = (Component) serializer.readObject(GraphicPanel.class, "encoder_delete_dialog.bxml");

			final Dialog dialog = new Dialog(true);
			dialog.setContent(body);
			
			if(left){
				dialog.setTitle("Setup für linken Encoder löschen?");
			} else {
				dialog.setTitle("Setup für rechten Encoder löschen?");
			}
			
			dialog.open(editor.window);

			PushButton submit = (PushButton) serializer.getNamespace().get("delete");
			submit.getButtonPressListeners().add(new ButtonPressListener() {

				@Override
				public void buttonPressed(org.apache.pivot.wtk.Button button) {
					if(left){
						instrument.setCodeEncoderLeft("");
					} else {
						instrument.setCodeEncoderRight("");
					}
					instrument.updateObservers();
					dialog.close();
				}
			});
			PushButton cancel = (PushButton) serializer.getNamespace().get("cancel");
			cancel.getButtonPressListeners().add(new ButtonPressListener() {

				@Override
				public void buttonPressed(org.apache.pivot.wtk.Button button) {
					dialog.close();
				}
			});
		} catch(Exception exception) {
			System.err.println(exception);
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		buttonEncLeftEdit.setButtonData(IconLoader.icons.get(Icon.EDIT)[IconLoader.DEACTIVE]);
		buttonEncRightEdit.setButtonData(IconLoader.icons.get(Icon.EDIT)[IconLoader.DEACTIVE]);
	}
}
