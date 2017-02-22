package net.eureka.androidcast.gui.net;


import net.eureka.androidcast.Static;
import net.eureka.androidcast.foundation.config.Configuration;
import net.eureka.androidcast.foundation.init.ApplicationGlobals;

import java.net.InetAddress;
import java.util.ArrayList;

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

public final class NetViewer
{
	
	private static final String INFO_MESSAGE = new String("Please select your Network Interface.");
	
	private final TableView<DirectoryItem> netList = new TableView<DirectoryItem>();
	
	private final ListHandler listHandler = new ListHandler();
	
	public NetViewer(BorderPane gui_scene) 
	{
		netList.setId("playlist");
		netList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		netList.setMinWidth(350);
		netList.setMinHeight(150);
		netList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		netList.setPrefSize(390, 1200);
		netList.getSelectionModel().setCellSelectionEnabled(true);
		netList.setOnMouseClicked(listHandler);
		Tooltip playlist_tip = new Tooltip("The found active network interfaces.");
		playlist_tip.setId("tool_tip");
		Tooltip.install(netList, playlist_tip);
		setUpData();
		this.setUpColumns();
		this.addColumnMonitor();
		gui_scene.setCenter(this.setUpScroller());
	}
	
	public void setUpData()
	{
		if(Static.getInetAddresses().isEmpty())
			setUpNoData();
		else
			createPlaylistData();
	}
	
	private void setUpNoData()
	{
		final byte[] no_media_detected = "No interfaces detected. Please install network drivers.".getBytes();
		netList.setItems(FXCollections.observableArrayList(new DirectoryItem(no_media_detected)));
	}
	
	private ObservableList<DirectoryItem> createPlaylistData()
	{
		ObservableList<DirectoryItem> media_list = FXCollections.observableArrayList();
		DirectoryItem folder_item = null;
		ArrayList<String> interface_names = Static.getInterfaceNames();
		for(String row_data : interface_names)
		{
			folder_item = new DirectoryItem(row_data.getBytes());
			media_list.add(folder_item);
		}
		
		return media_list;
	}
	
	private VBox setUpScroller()
	{
		VBox box = new VBox(5);
		box.setPadding(new Insets(10, 0, 0, 10));
		box.getChildren().addAll(netList);
		return box;
	}
	
	@SuppressWarnings("unchecked")
	private void setUpColumns()
	{
		@SuppressWarnings("rawtypes")
		TableColumn[] playlist_columns = new TableColumn[1];
		// Set up name column.
		playlist_columns[0] = new TableColumn<Object, Object>(INFO_MESSAGE);
		playlist_columns[0].setCellValueFactory(new PropertyValueFactory<DirectoryItem, String>("name"));
		netList.getColumns().addAll(playlist_columns);
	}
	
	private void addColumnMonitor()
	{
		netList.getColumns().addListener(new ListChangeListener<Object>()
		{
	        @Override
	        public void onChanged(Change<?> change) 
	        {
	            netList.resizeColumn(netList.getColumns().get(0), 1);
            }
	    });
	}
	
	public String[] selectedIndexes()
	{
		return ListHandler.indexes;
	}
	
	public void resetSelectedIndexes()
	{
		netList.getItems().removeAll(ListHandler.selectedItems);
		reset();
	}
	
	public void resetAllIndexes()
	{
		netList.getItems().clear();
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
		
		public DirectoryItem(byte[] name_bytes)
		{
			this.setName(new SimpleStringProperty(new String(name_bytes)));
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
		}
    }
}
