package de.easemotion.fie.view;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.FileBrowserSheet.Mode;
import org.apache.pivot.wtk.content.ButtonData;

import de.easemotion.fie.EditorApplication;
import de.easemotion.fie.data.FileHandler;
import de.easemotion.fie.data.InstrumentValidator;
import de.easemotion.fie.data.FileHandler.Error;
import de.easemotion.fie.model.Instrument;
import de.easemotion.fie.utils.Constants;
import de.easemotion.fie.utils.IconLoader;
import de.easemotion.fie.utils.IconLoader.Icon;

public class MenuPanel extends BoxPane implements Observer {

	private static final String TAG = MenuPanel.class.getSimpleName();

	private Instrument instrument;

	private EditorApplication editor;

	LinkButton buttonLoad;
	LinkButton buttonSave;
	LinkButton buttonReset;

	public MenuPanel(EditorApplication editor, final Instrument instrument){
		this.editor = editor;
		this.instrument = instrument;

		try {
			BXMLSerializer s = new BXMLSerializer();
			Component component = (Component) s.readObject(MenuPanel.class, "menu_panel.bxml");

			buttonLoad = (LinkButton)s.getNamespace().get("button_menu_load");
			buttonLoad.setButtonData(new ButtonData(IconLoader.icons.get(Icon.LOAD)[IconLoader.DEACTIVE]));
			buttonLoad.getButtonPressListeners().add(actionLoad);

			buttonSave = (LinkButton)s.getNamespace().get("button_menu_save");
			buttonSave.setButtonData(new ButtonData(IconLoader.icons.get(Icon.SAVE)[IconLoader.DEACTIVE]));
			buttonSave.getButtonPressListeners().add(actionSave);

			buttonReset = (LinkButton)s.getNamespace().get("button_menu_reset");
			buttonReset.setButtonData(new ButtonData(IconLoader.icons.get(Icon.RESET)[IconLoader.DEACTIVE]));
			buttonReset.getButtonPressListeners().add(actionReset);

			this.add(component);
		} catch (IOException | SerializationException e) {
			e.printStackTrace();
		}
	}

	private ButtonPressListener actionLoad = new ButtonPressListener() {

		@Override
		public void buttonPressed(Button button) {
			File root = EditorApplication.lastFileBrowserPath;

			final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet();
			fileBrowserSheet.setMode(Mode.OPEN);
			if(root != null && root.exists()){
				fileBrowserSheet.setRootDirectory(root);
			}
			fileBrowserSheet.open(editor.window, new SheetCloseListener() {
				@Override
				public void sheetClosed(Sheet sheet) {
					if (sheet.getResult()) {
						File file = fileBrowserSheet.getSelectedFile();
						if(file != null && file.exists()){
							FileHandler.load(file, new FileHandler.LoadInstrumentListener() {

								@Override
								public void onSuccess(Instrument instrument) {
									editor.setInstrument(instrument);
									System.out.println("[info] instrument loaded");
								}

								@Override
								public void onError(Error error) {
									System.err.println("[error] could not load instrument");
									System.err.println(error.toString());
								}
							});
						}

						EditorApplication.lastFileBrowserPath = fileBrowserSheet.getSelectedFile().getParentFile();
					}
				}
			});
		}
	};

	private ButtonPressListener actionSave = new ButtonPressListener() {

		@Override
		public void buttonPressed(Button button) {
			List<String> errors = InstrumentValidator.check(instrument);
			if(errors.size() == 0){
				// no errors, open file browser
				
				String name = instrument.getInstrumentName();
				name = name.replace(" ", "_");
				
				final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet();
                fileBrowserSheet.setMode(Mode.SAVE_AS);
                fileBrowserSheet.getStyles().put("hideDisabledFiles", true);
        		
                File root = EditorApplication.lastFileBrowserPath;
        		if(root != null && root.exists()){
        			fileBrowserSheet.setRootDirectory(root);
        		}
        		fileBrowserSheet.setDisabledFileFilter(new Filter<File>() {
        			
        			@Override
        			public boolean include(File item) {
        				return (item.isFile() && !item.getName().endsWith(Constants.extension.EMI_ZIP));
        			}
        		});
                
                fileBrowserSheet.open(editor.window, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                        	File file = fileBrowserSheet.getSelectedFile();

    						if(file.getParentFile().exists()){
    							FileHandler.saveAndPack(file.getParentFile(), file.getName(), instrument, 
    									new FileHandler.LoadInstrumentListener() {

    								@Override
    								public void onSuccess(Instrument instrument) {
    									editor.setInstrument(instrument);
    									System.out.println("[info] instrument saved and packed");
    								}

    								@Override
    								public void onError(Error error) {
    									System.err.println("[error] could not save instrument");
    									System.err.println(error.toString());
    								}
    							});
    						}
                        }
                    }
                });
			} else {
				// found some errors, show a dialog
				
				org.apache.pivot.collections.List<String> list = new org.apache.pivot.collections.ArrayList<String>();
				for (String string : errors) {
					list.add("- " + string);
				}
				
				ListView listView = new ListView();
                listView.setListData(list);
                listView.setSelectMode(ListView.SelectMode.NONE);
                listView.getStyles().put("backgroundColor", null);
                
                Component body = null;
    			BXMLSerializer serializer = new BXMLSerializer();
    			try {
    				body = (Component) serializer.readObject(GraphicPanel.class, "check_instrument_dialog.bxml");
    				((BoxPane) serializer.getNamespace().get("content")).add(listView);

    				final Dialog dialog = new Dialog(true);
    				dialog.setContent(body);
    				dialog.setTitle("Es wurden Fehler in der Konfiguration gefunden.");
    				dialog.open(editor.window);

    				PushButton cancel = (PushButton) serializer.getNamespace().get("confirm");
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
		}
	};

	private ButtonPressListener actionReset = new ButtonPressListener() {

		@Override
		public void buttonPressed(Button button) {
			Component body = null;
			BXMLSerializer serializer = new BXMLSerializer();
			try {
				body = (Component) serializer.readObject(GraphicPanel.class, "reset_instrument_dialog.bxml");

				final Dialog dialog = new Dialog(true);
				dialog.setContent(body);
				dialog.setTitle("Instrument zurücksetzen? Alle Einstellungen gehen verloren!");
				dialog.open(editor.window);

				PushButton submit = (PushButton) serializer.getNamespace().get("delete");
				submit.getButtonPressListeners().add(new ButtonPressListener() {

					@Override
					public void buttonPressed(org.apache.pivot.wtk.Button button) {
						instrument.reset();
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
	};

	@Override
	public void update(Observable o, Object arg) {
		instrument = editor.getInstrument();
	}
}
