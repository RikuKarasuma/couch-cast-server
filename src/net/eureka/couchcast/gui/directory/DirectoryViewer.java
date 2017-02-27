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
import net.eureka.couchcast.foundation.init.ApplicationGlobals;
import net.eureka.couchcast.gui.lang.LanguageDelegator;
import net.eureka.couchcast.gui.lang.Languages;
import net.eureka.couchcast.gui.playlist.PercentageTableColumn;

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
	
	private final TableView<DirectoryItem> directoryList = new TableView<DirectoryItem>();
	
	private final ListHandler listHandler = new ListHandler();
	
	public DirectoryViewer(BorderPane gui_scene) 
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
		setUpData();
		this.setUpColumns();
		this.addColumnMonitor();
		gui_scene.setCenter(this.setUpScroller());
	}
	
	public void setUpData()
	{
		if(ApplicationGlobals.getMonitoredSize() == 0)
			setUpNoData();
		else
			setUpdatedData();
	}
	
	private void setUpNoData()
	{
		final byte[] no_media_detected = "No folders added".getBytes(), non_applicable = "N/A".getBytes(), non_applicable_int = ByteBuffer.allocate(4).putInt(0).array();
		directoryList.setItems(FXCollections.observableArrayList(new DirectoryItem(no_media_detected, non_applicable, non_applicable_int)));
	}
	
	private void setUpdatedData()
	{
		directoryList.setItems(createPlaylistData());
	}
	
	private ObservableList<DirectoryItem> createPlaylistData()
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
	
	private VBox setUpScroller()
	{
		VBox box = new VBox(5);
		box.setPadding(new Insets(10, 0, 0, 10));
		box.getChildren().addAll(directoryList);
		return box;
	}
	
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
		playlist_columns[2].setCellValueFactory(new PropertyValueFactory<DirectoryItem, Integer>("size"));
		directoryList.getColumns().addAll(playlist_columns);
		
	}
	
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
	
	public String[] selectedIndexes()
	{
		return ListHandler.indexes;
	}
	
	public void resetSelectedIndexes()
	{
		directoryList.getItems().removeAll(ListHandler.selectedItems);
		reset();
	}
	
	public void resetAllIndexes()
	{
		directoryList.getItems().clear();
		reset();
	}
	
	private void reset()
	{
		setUpData();
		ListHandler.indexes = null;
		new Configuration(true);
	}
	
	public static class DirectoryItem
	{
		private SimpleStringProperty name;
		private SimpleStringProperty path;
		private SimpleIntegerProperty size;
		
		public DirectoryItem(byte[] name_bytes, byte[] path_bytes, byte[] size_bytes)
		{
			this.setName(new SimpleStringProperty(new String(name_bytes)));
			this.setPath(new SimpleStringProperty(new String(path_bytes)));
			ByteBuffer buffer = ByteBuffer.wrap(size_bytes);
			this.setSize(new SimpleIntegerProperty(buffer.getInt()));
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

		public int getSize()
		{
			return size.get();
		}

		public void setSize(SimpleIntegerProperty size) 
		{
			this.size = size;
		}
		
		public IntegerProperty sizeProperty()
		{
			return this.size;
		}
	}
	
	private static class ListHandler implements EventHandler<MouseEvent> 
	{
		private static String[] indexes = null;
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
			indexes = new String[size];
			
			for(int i = 0; i < size; i++ )
				indexes[i] = selectedItems.get(i).getPath();
		}
    }
}
