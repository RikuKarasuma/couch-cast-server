package net.eureka.couchcast.gui.playlist;


import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import net.eureka.couchcast.foundation.file.manager.FileFactory;
import net.eureka.couchcast.gui.lang.LanguageDelegator;
import net.eureka.couchcast.gui.lang.Languages;

public final class PlaylistViewer
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
									LanguageDelegator.getLanguageOfComponent(Languages.SIZE),
									// Number
									LanguageDelegator.getLanguageOfComponent(Languages.NUMBER)
								};
	
	
	private TableView<PlaylistItem> playlist = new TableView<PlaylistItem>();
	
	public PlaylistViewer(BorderPane gui_scene) 
	{
		playlist.setId("playlist");
		playlist.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		playlist.setMinWidth(390);
		playlist.setMinHeight(290);
		playlist.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		playlist.setPrefSize(390, 1200);
		playlist.autosize();
		playlist.getSelectionModel().setCellSelectionEnabled(true);
		Tooltip playlist_tip = new Tooltip("Media Files found by server. Enter Settings to set Monitored Directories.");
		playlist_tip.setId("tool_tip");
		Tooltip.install(playlist, playlist_tip);
		this.setUpData();
		this.setUpColumns();
		this.addColumnMonitor();
		gui_scene.setCenter(this.setUpScroller());
	}
	
	public void setUpData()
	{
		if(FileFactory.getListSize() == 0)
			this.setUpNoData();
		else
			this.setUpdatedData();
	}
	
	private void setUpNoData()
	{
		final byte[] no_media_detected = "No media detected".getBytes(), non_applicable = "N/A".getBytes();
		playlist.setItems(FXCollections.observableArrayList(new PlaylistItem(no_media_detected, non_applicable, non_applicable, non_applicable, false)));
	}
	
	private void setUpdatedData()
	{
		playlist.setItems(createPlaylistData());
	}
	
	private ObservableList<PlaylistItem> createPlaylistData()
	{
		ObservableList<PlaylistItem> media_list = FXCollections.observableArrayList();
		byte[][][] media_data = FileFactory.getMediaPlaylistVectors();
		PlaylistItem media_item = null;
		for(byte[][] row_data : media_data)
		{
			media_item = new PlaylistItem(row_data[0], row_data[1], row_data[2], row_data[3], true);
			media_list.add(media_item);
		}
		
		return media_list;
	}
	
	private VBox setUpScroller()
	{
		VBox box = new VBox(5);
		box.setPadding(new Insets(10, 0, 0, 10));
		box.getChildren().addAll(playlist);
		return box;
	}
	
	@SuppressWarnings("unchecked")
	private void setUpColumns()
	{
		@SuppressWarnings("rawtypes")
		TableColumn[] playlist_columns = new TableColumn[4];
		// Set up name column.
		playlist_columns[0] = new PercentageTableColumn<Object, Object>(COLUMN_STRINGS[0]);
		playlist_columns[0].setMinWidth(100);
		playlist_columns[0].setCellValueFactory(new PropertyValueFactory<PlaylistItem, String>("name"));
		// Set up name column.
		playlist_columns[1] = new PercentageTableColumn<Object, Object>(COLUMN_STRINGS[1]);
		playlist_columns[1].setMinWidth(80);
		playlist_columns[1].setCellValueFactory(new PropertyValueFactory<PlaylistItem, String>("path"));
		// Set up name column.
		playlist_columns[2] = new PercentageTableColumn<Object, Object>(COLUMN_STRINGS[2]);
		playlist_columns[2].setMinWidth(50);
		playlist_columns[2].setCellValueFactory(new PropertyValueFactory<PlaylistItem, String>("size"));
		// Set up name column.
		playlist_columns[3] = new PercentageTableColumn<Object, Object>(COLUMN_STRINGS[3]);
		playlist_columns[3].setMinWidth(55);
		playlist_columns[3].setCellValueFactory(new PropertyValueFactory<PlaylistItem, Integer>("index"));
		playlist.getColumns().addAll(playlist_columns);
		
	}
	
	private void addColumnMonitor()
	{
		playlist.getColumns().addListener(new ListChangeListener<Object>()
		{
			@Override
			public void onChanged(Change<?> change) 
			{
				playlist.resizeColumn(playlist.getColumns().get(0), 1);
			}
		});
	}
	
	public static class PlaylistItem
	{
		private static final String EXTENSION_DELIMITER = "."; 
		private SimpleStringProperty name;
		private SimpleStringProperty path;
		private SimpleStringProperty size;
		private SimpleIntegerProperty index;
		
		public PlaylistItem(byte[] name_bytes, byte[] path_bytes, byte[] size_bytes, byte[] index_bytes, boolean remove_extension)
		{
			if(remove_extension)
				this.setName(new SimpleStringProperty(removeNameExtension(name_bytes)));
			else
				this.setName(new SimpleStringProperty(new String(name_bytes)));
			this.setPath(new SimpleStringProperty(new String(path_bytes)));
			this.setSize(new SimpleStringProperty(new String(size_bytes)));
			try
			{
				this.setIndex(new SimpleIntegerProperty(Integer.valueOf(new String(index_bytes))));
			}
			catch(NumberFormatException e)
			{
				this.setIndex(new SimpleIntegerProperty(Integer.valueOf("0")));
			}
		}
		
		private static String removeNameExtension(byte[] name_bytes)
		{
			String name_string = new String(name_bytes);
			try
			{
				name_string = name_string.substring(0, name_string.lastIndexOf(EXTENSION_DELIMITER));
			}
			catch(StringIndexOutOfBoundsException e)
			{
				// IGNORE, THROWS ERROR WHEN MEDIA FILES ARE MOVED.
			}
			return name_string;
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

		public String getSize()
		{
			return size.get();
		}

		public void setSize(SimpleStringProperty size) 
		{
			this.size = size;
		}
		
		public StringProperty sizeProperty()
		{
			return this.size;
		}

		public int getIndex()
		{
			return index.get();
		}

		public void setIndex(SimpleIntegerProperty index)
		{
			this.index = index;
		}
		
		public SimpleIntegerProperty indexProperty()
		{
			return this.index;
		}
	}
}
