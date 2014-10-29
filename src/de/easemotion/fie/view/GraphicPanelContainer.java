package de.easemotion.fie.view;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.FillPane;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;

import de.easemotion.fie.EditorApplication;
import de.easemotion.fie.model.graphics.GraphicSurface;
import de.easemotion.fie.model.graphics.Layer;

public class GraphicPanelContainer extends BoxPane implements Observer {
	
	private GraphicPanel graphicPanel;

	public GraphicPanelContainer(EditorApplication editor, final GraphicSurface surface){
		try {
			BXMLSerializer s = new BXMLSerializer();
			Component component = (Component) s.readObject(GraphicPanelContainer.class, "graphic_panel_container.bxml");
			
			FillPane panel = (FillPane) s.getNamespace().get("graphic_panel");
			graphicPanel = new GraphicPanel(editor, surface);
			panel.add(graphicPanel);
			
			this.add(component);
		} catch (IOException | SerializationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		graphicPanel.update(o, arg);
	}
}
