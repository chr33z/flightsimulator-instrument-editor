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
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;

import de.easemotion.fie.EditorApplication;
import de.easemotion.fie.model.graphics.GraphicSurface;
import de.easemotion.fie.model.graphics.Layer;

public class LuaEditorPanel extends BoxPane implements Observer {
	
	private static final String TAG = LuaEditorPanel.class.getSimpleName();

	private GraphicSurface surface;

	private EditorApplication editor;
	
	private TextArea textArea;

	public LuaEditorPanel(EditorApplication editor, final GraphicSurface surface){
		this.editor = editor;
		this.surface = surface;

		try {
			BXMLSerializer s = new BXMLSerializer();
			Component component = (Component) s.readObject(LuaEditorPanel.class, "lua_pane.bxml");
			
			textArea = (TextArea) s.getNamespace().get("lua_text_area");
			
			textArea.getComponentKeyListeners().add(new ComponentKeyListener() {
				
				@Override
				public boolean keyTyped(Component component, char character) {
					
					// save content from textArea to layer
					Layer layer = surface.getActiveLayer();
					if(layer != null){
						layer.setLuaScript(textArea.getText());
					}
					return false;
				}
				
				@Override
				public boolean keyReleased(Component component, int keyCode,
						KeyLocation keyLocation) {
					// TODO Auto-generated method stub
					return false;
				}
				
				@Override
				public boolean keyPressed(Component component, int keyCode,
						KeyLocation keyLocation) {

					switch (keyCode) {
					case KeyCode.TAB:
						textArea.setText(textArea.getText() + '\t');
						break;
					default:
						break;
					}
					
					return true;
				}
			});
			
			this.add(component);
		} catch (IOException | SerializationException e) {
			e.printStackTrace();
		}
	}
	
	private void updateScript(){
		Layer layer = surface.getActiveLayer();
		
		if(layer != null){
			textArea.setEditable(true);
			textArea.setEnabled(true);
			textArea.setText(layer.getLuaScript());
		} else {
			textArea.setEditable(false);
			textArea.setEnabled(false);
			textArea.setText("");
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		updateScript();
	}
}
