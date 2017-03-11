package net.eureka.couchcast.gui.directory;


import java.nio.ByteBuffer;


import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import net.eureka.couchcast.foundation.config.Configuration;
import net.eureka.couchcast.foundation.file.manager.DirectoryFactory;
import net.eureka.couchcast.foundation.file.manager.FileFactory;
import net.eureka.couchcast.foundation.init.ApplicationGlobals;
import net.eureka.couchcast.gui.SettingsMenu;
import net.eureka.couchcast.gui.lang.LanguageDelegator;
import net.eureka.couchcast.gui.lang.Languages;
import net.eureka.couchcast.gui.playlist.PercentageTableColumn;

/**
 * Instantiated and managed from {@link SettingsMenu}. Creates a list of monitored directories to be manipulated by
 * controls found in the settings menu. The list of monitored directories is saved/read via the {@link Configuration}
 * file and loaded into the {@link ApplicationGlobals}. These directories are used by the {@link DirectoryFactory} to 
 * find media files and add them to the {@link FileFactory}. 
 * 
 * @author Owen McMonagle.
 * 
 * @see SettingsMenu
 * @See Configuration
 * @see ApplicationGlobals
 * @see DirectoryFactory
 * @see FileFactory
 * 
 * @version 0.1
 */
public final class DirectoryViewer
{
	
	/**
	 * {@link JTable} column names.
	 * Language specific column names. In the order: name, location, size and index number.
 	 * Uses the {@link LanguageDelegator} to decide which text to choose. The {@link Languages} 
 	 * enumeration helps retrieve the appropriate stored text. 
	 */
	private static final String[] COLUMN_STRINGS = new String[]
	{
		// Name
		LanguageDelegator.getLanguageOfComponent(Languages.NAME),
		// Location
		LanguageDelegator.getLanguageOfComponent(Languages.LOCATION),
		// Size
		LanguageDelegator.getLanguageOfComponent(Languages.NUMBER),
	};
	
	/**
	 * Table view that contains each directory as a list. Each directory is denoted by the object 
	 * {@link DirectoryItem}.
	 */
	private final TableView<DirectoryItem> directoryList = new TableView<DirectoryItem>();
	
	/**
	 * An {@link EventHandler} for the directoryList table view.
	 */
	private final ListHandler listHandler = new ListHandler();
	
	/**
	 * Sets up 
	 * @param gui_scene
	 */
	public DirectoryViewer(BorderPane gui_scene) 
	{
		setUpDirectoryList();
		load();
		setUpColumns();
		addColumnMonitor();
		gui_scene.setCenter(this.setUpScroller());
	}
	
	/**
	 * Sets up the CSS id, selection model, width, height, resize policy, mouse listener and tool tip.
	 */
	private void setUpDirectoryList()
	{
		directoryList.setId("playlist");
		directoryList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		directoryList.setMinWidth(350);
		directoryList.setMinHeight(150);
		directoryList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		directoryList.setPrefSize(390, 1200);
		directoryList.autosize();
		directoryList.getSelectionModel().setCellSelectionEnabled(true);
		directoryList.setOnMouseClicked(listHandler);
		Tooltip playlist_tip = new Tooltip("Media folders added to server. Current number of paths are: "+ApplicationGlobals.getMonitoredList().size()+". Deep Search: "+((ApplicationGlobals.isDeepSearch()) ? "Enabled" : "Disabled"));
		playlist_tip.setId("tool_tip");
		Tooltip.install(directoryList, playlist_tip);
	}
	
	/**
	 * Attempts to load the monitored directories. If getMonitoredSize from {@link ApplicationGlobals} is zero,
	 * then a placeholder added. Otherwise each monitored directory is loaded from {@link ApplicationGlobals}.
	 */
	public void load()
	{
		if(ApplicationGlobals.getMonitoredSize() == 0)
			loadNoData();
		else
			loadData();
	}
	
	/**
	 * Adds a placeholder to the directory list for when there are no monitored directories. 
	 */
	private void loadNoData()
	{
		final byte[] no_media_detected = "No folders added".getBytes(), non_applicable = "N/A".getBytes(), non_applicable_int = ByteBuffer.allocate(4).putInt(0).array();
		directoryList.setItems(FXCollections.observableArrayList(new DirectoryItem(no_media_detected, non_applicable, non_applicable_int)));
	}
	
	/**
	 * Adds each monitored directory from its list located within {@link ApplicationGlobals}.
	 */
	private void loadData()
	{
		directoryList.setItems(createMonitoredList());
	}
	
	/**
	 * Creates and returns an observable list. The list is created from a three dimensional byte array that is
	 * retrieved from {@link ApplicationGlobals} and contains each monitored directory. Inside the array is the
	 * name, path and index. 
	 * 
	 * @return A list filled with the names, paths and indexes of each monitored directory.
	 */
	private ObservableList<DirectoryItem> createMonitoredList()
	{
		ObservableList<DirectoryItem> media_list = FXCollections.observableArrayList();
		byte[][][] folder_data = ApplicationGlobals.getFolderVectors();
		DirectoryItem folder_item = null;
		for(byte[][] row_data : folder_data)
		{
			folder_item = new DirectoryItem(row_data[0], row_data[1], row_data[2]);
			media_list.add(folder_item);
		}
		
		return media_list;
	}
	
	/**
	 * Sets up the directory list inside a {@link VBox} and then returns that vbox. The vbox is to be added to a
	 * parent gui component.
	 * 
	 * @return A vbox with the directory list inside. 
	 */
	private VBox setUpScroller()
	{
		VBox box = new VBox(5);
		box.setPadding(new Insets(10, 0, 0, 10));
		box.getChildren().addAll(directoryList);
		return box;
	}
	
	/**
	 * Creates three columns and adds them to the directory list.
	 */
	@SuppressWarnings("unchecked")
	private void setUpColumns()
	{
		@SuppressWarnings("rawtypes")
		TableColumn[] playlist_columns = new TableColumn[3];
		// Set up name column.
		playlist_columns[0] = new PercentageTableColumn<Object, Object>(COLUMN_STRINGS[0]);
		playlist_columns[0].setMinWidth(100);
		playlist_columns[0].setCellValueFactory(new PropertyValueFactory<DirectoryItem, String>("name"));
		// Set up path column.
		playlist_columns[1] = new PercentageTableColumn<Object, Object>(COLUMN_STRINGS[1]);
		playlist_columns[1].setMinWidth(80);
		playlist_columns[1].setCellValueFactory(new PropertyValueFactory<DirectoryItem, String>("path"));
		// Set up size column.
		playlist_columns[2] = new PercentageTableColumn<Object, Object>(COLUMN_STRINGS[2]);
		playlist_columns[2].setMinWidth(50);
		playlist_columns[2].setCellValueFactory(new PropertyValueFactory<DirectoryItem, Integer>("index"));
		directoryList.getColumns().addAll(playlist_columns);
	}
	
	/**
	 * Adds a column listener. Resizes the first column in the directory list once a change has been detected. 
	 * This ensure that the columns fit the width of the table view.
	 */
	private void addColumnMonitor()
	{
		directoryList.getColumns().addListener(new ListChangeListener<Object>()
		{
			@Override
			public void onChanged(Change<?> change) 
			{
				directoryList.resizeColumn(directoryList.getColumns().get(0), 1);
			}
		});
	}
	
	/**
	 * Returns the currently selected paths from {@link ListHandler}. This is used in {@link SettingsMenu}.
	 * @return
	 */
	public String[] selectedPaths()
	{
		return ListHandler.paths;
	}
	
	/**
	 * Resets the currently selected paths from {@link ListHandler} and updates the directory list and the
	 * {@link Configuration} file.
	 */
	public void resetSelectedPaths()
	{
		directoryList.getItems().removeAll(ListHandler.selectedItems);
		reset();
	}
	
	/**
	 * Clears the entire directory list, updates it and then updates the {@link Configuration} file.
	 */
	public void resetAllIndexes()
	{
		directoryList.getItems().clear();
		reset();
	}
	
	/**
	 * Attempts to update the directory list via load. Nulls the {@link ListHandler}s currently selected paths.
	 * Finally it updates the {@link Configuration} file.
	 */
	private void reset()
	{
		load();
		ListHandler.paths = null;
		new Configuration(true);
	}
	
	/**
	 * Used to provide a POJO for javafx's {@link ObservableList}. To avoid extra memory consumption, I try to 
	 * use byte arrays that contain UTF-8 strings where ever I can.
	 * 
	 * @author Owen McMonagle.
	 * 
	 * @version 0.1
	 * 
	 * @see DirectoryViewers
	 */
	public static class DirectoryItem
	{
		private SimpleStringProperty name;
		private SimpleStringProperty path;
		private SimpleIntegerProperty index;
		
		public DirectoryItem(byte[] name_bytes, byte[] path_bytes, byte[] index_bytes)
		{
			this.setName(new SimpleStringProperty(new String(name_bytes)));
			this.setPath(new SimpleStringProperty(new String(path_bytes)));
			ByteBuffer buffer = ByteBuffer.wrap(index_bytes);
			this.setIndex(new SimpleIntegerProperty(buffer.getInt()));
		}

		public String getName() 
		{
			return name.get();
		}

		public void setName(SimpleStringProperty name)
		{
			this.name = name;
		}
		
		public StringProperty nameProperty()
		{
			return this.name;
		}

		public String getPath() 
		{
			return path.get();
		}

		public void setPath(SimpleStringProperty path) 
		{
			this.path = path;
		}
		
		public StringProperty pathProperty()
		{
			return this.path;
		}

		public int getIndex()
		{
			return index.get();
		}

		public void setIndex(SimpleIntegerProperty index) 
		{
			this.index = index;
		}
		
		public IntegerProperty indexProperty()
		{
			return this.index;
		}
	}
	
	/**
	 * An event handler for selecting/removing monitored directories on the {@link ObservableList}. Also used by 
	 * {@link SettingsMenu} to remove monitored directories from {@link ApplicationGlobals}. 
	 * 
	 * @author Owen McMonagle.
	 * 
	 * @version 0.2
	 * 
	 * @see DirectoryViewer
	 * @see SettingsMenu
	 */
	private static class ListHandler implements EventHandler<MouseEvent> 
	{
		private static String[] paths = null;
		private static ObservableList<DirectoryItem> selectedItems = null;

		@Override
		public void handle(MouseEvent event)
		{
			@SuppressWarnings("unchecked")
			TableView<DirectoryItem> view = (TableView<DirectoryItem>) event.getSource();
			selectedItems = view.getSelectionModel().getSelectedItems();
			setIndexes();
		}
		
		private static void setIndexes()
		{
			final int size = selectedItems.size();
			paths = new String[size];
			
			for(int i = 0; i < size; i++ )
				paths[i] = selectedItems.get(i).getPath();
		}
    }
}
