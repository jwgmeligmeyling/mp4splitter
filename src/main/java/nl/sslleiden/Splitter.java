package nl.sslleiden;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.table.AbstractTableModel;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.common.util.concurrent.FutureCallback;

import lombok.extern.slf4j.Slf4j;
import nl.sslleiden.model.CourseModel;
import nl.sslleiden.model.Cursus;
import nl.sslleiden.model.MediaFile;
import nl.sslleiden.model.Niveau;
import nl.sslleiden.model.Vak;
import nl.sslleiden.swing.AbstractColumn;
import nl.sslleiden.swing.Column;
import nl.sslleiden.swing.ComboBox;
import nl.sslleiden.swing.CustomTableModel;
import nl.sslleiden.swing.TextBox;
import nl.sslleiden.swing.OptionDialog;
import nl.sslleiden.swing.OptionDialog.OptionType;
import nl.sslleiden.swing.OptionDialog.MessageType;
import nl.sslleiden.util.Callback;
import nl.sslleiden.util.Proposal;
import nl.youngmediaexperts.data.Marker;
import nl.youngmediaexperts.data.Timestamp;

@Slf4j
public class Splitter extends JPanel {
	
	private static Properties properties = new Properties();
	
	private static final long serialVersionUID = 1611467845983893801L;
	
	private static final int PREFERRED_WIDTH = 700;
	
	private static JFrame frame;
	
	public static void main(String[] args) throws IOException, ParseException {
		properties = new Properties();
		properties.load(Splitter.class.getResourceAsStream("/properties.properties"));
		frame = createFrame(new Splitter(new CourseModel(new File("."))));
	}
	
	public static JFrame createFrame(Splitter splitter) {
		JFrame frame = new JFrame(properties.getProperty("FORM_HEADER"));
		splitter.setOpaque(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(splitter);
		frame.pack();
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		return frame;
	}
	
	private final List<MediaFile> assets;
	private final JTable fileTable, markerTable;

	public Splitter(final CourseModel model) throws IOException {
		super(new BorderLayout());
		this.assets = model.getFiles();
		
		final JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu(properties.getProperty("FILE_TXT"));
		menu.add(new JMenuItem(new AbstractAction(properties.getProperty("IMPORT_FOLDER")) {
			
			private static final long serialVersionUID = 7276657892261685356L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(); 
			    chooser.setCurrentDirectory(model.getRootFolder());
			    chooser.setDialogTitle(properties.getProperty("SEARCH_FOLDER"));
			    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    chooser.setAcceptAllFileFilterUsed(false);
			    
				if (chooser.showOpenDialog(Splitter.this) == JFileChooser.APPROVE_OPTION) {
					try {
						createFrame(new Splitter(new CourseModel(chooser.getSelectedFile())));
						frame.setVisible(false);
					} catch (Exception exception) {
						JOptionPane.showMessageDialog(null, properties.getProperty("FAILED_TO_OPEN"));
						log.error(exception.getMessage(), exception);
					}
				}
			}
			
		}));
		
		menu.addSeparator();
		
		menu.add(new JMenuItem(new AbstractAction(properties.getProperty("OPEN_FILE")) {
			
			private static final long serialVersionUID = -4745097526889775791L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(); 
			    chooser.setCurrentDirectory(model.getRootFolder());
			    chooser.setDialogTitle(properties.getProperty("IMPORT_FILE"));
			    chooser.setFileFilter(new FileNameExtensionFilter(properties.getProperty("TEXT_FILES"), "txt"));
			    
				if (chooser.showOpenDialog(Splitter.this) == JFileChooser.APPROVE_OPTION) {
					try {
						createFrame(new Splitter(new CourseModel(chooser.getSelectedFile())));
						frame.setVisible(false);
					} catch (Exception exception) {
						JOptionPane.showMessageDialog(null, exception.getMessage());
						log.error(exception.getMessage(), exception);
					}
				}
			}
			
		}));
		
		menu.add(new JMenuItem(new AbstractAction(properties.getProperty("EXPORT_FILE")) {
			
			private static final long serialVersionUID = 7276657892261685356L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					model.saveToFile();
				} catch (IOException exception) {
					JOptionPane.showMessageDialog(null, properties.getProperty("FAILED_TO_SAVE"));
					log.error(exception.getMessage(), exception);
				}
			}
			
		}));
		
		menu.addSeparator();
		
		menu.add(new JMenuItem(new AbstractAction(properties.getProperty("GENERATE_SOURCES")) {
			
			private static final long serialVersionUID = -3380026829503854463L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					model.generateSources();
					log.info("Generated sources");
				} catch (Exception exception) {
					JOptionPane.showMessageDialog(null, properties.getProperty("FAILED_TO_SAVE"));
					log.error(exception.getMessage(), exception);
				}
			}
			
		}));
		
		menu.add(new JMenuItem(new AbstractAction(properties.getProperty("CLEAN_FOLDER", "Clean up folder")) {
			
			private static final long serialVersionUID = -3380026829503854463L;

			@Override
			public void actionPerformed(ActionEvent e) {
				model.cleanUpRoot(new Callback<Proposal<List<File>>>() {

					@Override
					public void call(Proposal<List<File>> proposal) {
						
						String CRLF = System.lineSeparator();
						List<File> files = proposal.get();
						StringBuilder sb = new StringBuilder(files.size()*10);
						sb.append(properties.getProperty("DELETE_FILES_Q", "Do you want to delete the following files?")).append(CRLF);
						for(File file : files)
							sb.append(file.getName()).append(CRLF);
						
						OptionDialog.builder(frame, proposal)
							.setMessage(sb.toString())
							.setOptionType(OptionType.YES_NO_OPTION)
							.setMessageType(MessageType.WARNING_MESSAGE)
							.show();
						
					}
					
				});
			}
			
		}));
		
		menu.addSeparator();
		
		menu.add(new JMenuItem(new AbstractAction("Info") {
			
			private static final long serialVersionUID = 6011594875652859146L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final String infoMsg = "Thank you for using Examencursus MRC-1 Videosplitter \u00ae\n"
						+ "This software has been made by Jan-Willem Gmelig Meyling\n"
						+ "\u00a9 2014 by Stichting Studiebegeleiding Leiden";
				JOptionPane.showMessageDialog(null, infoMsg, "Info", JOptionPane.INFORMATION_MESSAGE);
			}
			
		}));
		
		menuBar.add(menu);
		this.add(menuBar, BorderLayout.NORTH);
		
    	fileTable = new JTable(new CustomTableModel(new Column[] {
    			
    	new AbstractColumn(properties.getProperty("NAME")) {

			@Override
			public Object getValueAt(int rowIndex) {
				return assets.get(rowIndex).getFile().getName();
			}
			
    	},
		
		new AbstractColumn(properties.getProperty("INPOINT")) {
		
			@Override
			public Object getValueAt(int rowIndex) {
				return assets.get(rowIndex).getInpoint().toString();
			}
			
		},
		
		new AbstractColumn(properties.getProperty("OUTPOINT")) {
			
			@Override
			public Object getValueAt(int rowIndex) {
				return assets.get(rowIndex).getOutpoint().toString();
			}
		
		},
    	
    	new AbstractColumn(properties.getProperty("DURATION")) {

			@Override
			public Object getValueAt(int rowIndex) {
				return assets.get(rowIndex).getDuration().toString();
			}
    		
    	},
    	
    	new AbstractColumn(properties.getProperty("CREATION")) {

			@Override
			public Object getValueAt(int rowIndex) {
				return assets.get(rowIndex).getCreation().toString();
			}
			
    	},
		    	
    	new AbstractColumn(properties.getProperty("CAMERA")) {

			@Override
			public Object getValueAt(int rowIndex) {
				return assets.get(rowIndex).getCamera();
			}
    		
    	},
    	
    	new AbstractColumn(properties.getProperty("INDEX")) {

			@Override
			public Object getValueAt(int rowIndex) {
				return assets.get(rowIndex).getIndex();
			}
			
    	}}) {
    		
			private static final long serialVersionUID = -3413036624183099077L;

			@Override
			public int getRowCount() {
				return assets.size();
			}
			
    	});
		
		markerTable = new JTable(new AbstractTableModel() {
    		
			private static final long serialVersionUID = 7836885252821248967L;

			private MediaFile getMediaFile() {
				int selectedRow = fileTable.getSelectedRow();
				if(selectedRow != -1 && model.getFiles().size() > 0) {
					return assets.get(selectedRow);
				}
				return null;
			}
			
			final String[] columns = { properties.getProperty("TIMECODE"), properties.getProperty("DESCRIPTION") };
			
			@Override
			public int getRowCount() {
				MediaFile mediaFile = getMediaFile();
				if(mediaFile != null ) {
					return mediaFile.getMarkers().size();
				}
				return 0;
			}

			@Override
			public int getColumnCount() {
				return columns.length;
			}

			@Override
			public String getColumnName(int columnIndex) {
				return columns[columnIndex];
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return String.class;
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return true;
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				MediaFile mediaFile = getMediaFile();
				if(mediaFile != null ) {
					Marker marker = mediaFile.getMarkers().get(rowIndex);
					switch(columnIndex) {
						case 0:
							return marker.getTimestamp().toString();
						case 1:
							return marker.getDescription();
					}
				}
				return null;
				
			}

			@Override
			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
				MediaFile mediaFile = getMediaFile();
				if(mediaFile == null ) return;
				Marker marker = mediaFile.getMarkers().get(rowIndex);
				
				switch(columnIndex) {
				case 0:
					try {
						marker.setTimestamp(new Timestamp(aValue.toString()));
						Collections.sort(mediaFile.getMarkers());
					} catch (ParseException exception) {
						log.info(exception.getMessage(), exception);
					}
					break;
				case 1:
					marker.setDescription(aValue.toString());
					break;
				}
			}
			
		}) {

			private static final long serialVersionUID = 476559071645295842L;

			@Override
			public void setValueAt(Object aValue, int row, int column) {
				super.setValueAt(aValue, row, column);
				updateMarkerTable();
			}
			
    	};
    	
        fileTable.setPreferredScrollableViewportSize(new Dimension(PREFERRED_WIDTH, 300));
        fileTable.setFillsViewportHeight(true);
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(fileTable);
        
        markerTable.setPreferredScrollableViewportSize(new Dimension(PREFERRED_WIDTH, 70));
        markerTable.setFillsViewportHeight(true);
        JScrollPane scrollPane2 = new JScrollPane(markerTable);
        
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, scrollPane2);
        
    	fileTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateMarkerTable();
			}
    		
    	});
    	
    	fileTable.addMouseListener(new MouseAdapter() {

    		@Override
    		public void mouseClicked(MouseEvent e) {
    			if(e.getClickCount() == 2)
    				openSelectedFile();
    		}
    		
    		public MediaFile getSelectedMediaFile() {
				int selectedRow = fileTable.getSelectedRow();
				if(selectedRow != -1) {
					return assets.get(selectedRow);
				}
				return null;
			}
    		
    		private void openSelectedFile() {
    			MediaFile mediaFile = getSelectedMediaFile();
				if (mediaFile != null) {
					File file = mediaFile.getFile();
					if(file != null) {
						try {
							Desktop.getDesktop().open(mediaFile.getFile());
						} catch (IOException exception) {
							JOptionPane.showMessageDialog(null, properties.getProperty("FAILED_TO_OPEN_FILE"));
							log.error(exception.getMessage(), exception);
						}
					}
				}
    		}
    		
    		
    		@Override
    	    public void mousePressed(MouseEvent e){
    			if(SwingUtilities.isRightMouseButton(e)) {
    				// Select the correct row when right click in table
    				Point point = e.getPoint();
    				int rowIndex = fileTable.rowAtPoint(point);
    				fileTable.changeSelection(rowIndex, 0, false, false);
    			}
    	        if (e.isPopupTrigger())
    	            doPop(e);
    	    }

    		@Override
    	    public void mouseReleased(MouseEvent e){
    	        if (e.isPopupTrigger())
    	            doPop(e);
    	    }
    		
    		private void doPop(MouseEvent e){
    	        new JPopupMenu() {
    	        	
					private static final long serialVersionUID = -2966311231568136986L;

				{
    	        	JMenuItem optionAdd = new JMenuItem(new AbstractAction(properties.getProperty("IMPORT_FILE")) {
    	        		
						private static final long serialVersionUID = -3579448911076175227L;

						@Override
						public void actionPerformed(ActionEvent e) {
							JFileChooser chooser = new JFileChooser(); 
						    chooser.setCurrentDirectory(model.getRootFolder());
						    chooser.setDialogTitle(properties.getProperty("IMPORT_FILE"));
						    chooser.setFileFilter(new FileNameExtensionFilter(properties.getProperty("MOVIE_FILES"), "mp4", "m2t", "m2ts", "mpg", "mpeg"));
						    
							if (chooser.showOpenDialog(Splitter.this) == JFileChooser.APPROVE_OPTION) {
								try {
									model.addMediaFile(new MediaFile(chooser.getSelectedFile()));
									updateFileTable();
								} catch (Exception exception) {
									JOptionPane.showMessageDialog(null, exception.getMessage());
									log.error(exception.getMessage(), exception);
								}
							}
						}
    	        		
    	        	}),
    	        	
    	        	optionRemove = new JMenuItem(new AbstractAction(properties.getProperty("REMOVE_FILE")) {
    	        		
						private static final long serialVersionUID = -8926172834189550986L;

						@Override
						public void actionPerformed(ActionEvent e) {
							MediaFile mediaFile = getSelectedMediaFile();
							if (mediaFile != null) {
								model.removeMediaFile(mediaFile);
								fileTable.changeSelection(0, 0, false, false);
								updateFileTable();
							}
						}
    	        		
    	        	}),
    	        	
    	        	optionDuplicate = new JMenuItem(new AbstractAction(properties.getProperty("DUPLICATE")) {
    	        		
						private static final long serialVersionUID = -8281364914863679790L;

						@Override
						public void actionPerformed(ActionEvent e) {
    	        			MediaFile mediaFile = getSelectedMediaFile();
    	        			model.addMediaFile(mediaFile.duplicate());
    	        			
    	        			updateFileTable();
						}
    	        		
    	        	}),
    	        	
    	        	optionRenderSubClip = new JMenuItem(new AbstractAction(properties.getProperty("CONVERT_SUBCLIP")) {
    	        		
						private static final long serialVersionUID = -2470903157015511499L;

						@Override
						public void actionPerformed(ActionEvent e) {
							MediaFile file = getSelectedMediaFile();
							
							if(file != null) {
								file.trim(new FutureCallback<MediaFile>() {

									@Override
									public void onSuccess(MediaFile result) {
										updateFileTable();
										updateMarkerTable();
									}

									@Override
									public void onFailure(Throwable t) {
										JOptionPane.showMessageDialog(null, properties.getProperty("FAILED_TO_CONVERT"));
										log.error(t.getMessage(), t);
									}
									
								});
							}
						}
    	        		
    	        	});
    	    
    	        	
    	        	if(fileTable.getSelectedRow() == -1) {
    	        		optionRemove.setEnabled(false);
    	        		optionDuplicate.setEnabled(false);
    	        		optionRenderSubClip.setEnabled(false);
    	        	} else if (!getSelectedMediaFile().isTrimmable()) {
    	        		optionRenderSubClip.setEnabled(false);
    	        	}

    	        	add(optionAdd);
    	        	add(optionRemove);
    	        	add(optionDuplicate);
    	        	addSeparator();
    	        	add(optionRenderSubClip);
    	        	
    	        }}.show(e.getComponent(), e.getX(), e.getY());
    	    }
    		
    	});
    	
    	markerTable.addMouseListener(new MouseAdapter() {
    		
    		@Override
    	    public void mousePressed(MouseEvent e){
    			if(SwingUtilities.isRightMouseButton(e)) {
    				// Select the correct row when right click in table
    				Point point = e.getPoint();
    				int rowIndex = markerTable.rowAtPoint(point);
    				markerTable.changeSelection(rowIndex, 0, false, e.isShiftDown());
    			}
    	        if (e.isPopupTrigger())
    	            doPop(e);
    	    }

    		@Override
    	    public void mouseReleased(MouseEvent e){
    	        if (e.isPopupTrigger())
    	            doPop(e);
    	    }

    	    private void doPop(MouseEvent e){
    	        new JPopupMenu() {
    	        	
					private static final long serialVersionUID = -2966311231568136986L;
					
					
					public MediaFile getSelectedMediaFile() {
						int selectedRow = fileTable.getSelectedRow();
						if(selectedRow != -1) {
							return assets.get(selectedRow);
						}
						return null;
					}
					
					public Marker[] getSelectedMarkers(MediaFile mediaFile) {
						int count = markerTable.getSelectedRowCount();
						Marker[] result = new Marker[count];
						
						if(count > 0) {
							int[] rows = markerTable.getSelectedRows();
							List<Marker> markers = mediaFile.getMarkers();
							
							for(int i = 0, l = rows.length; i < l; i++)
								result[i] = markers.get(rows[i]);
						}
						
						return result;
					}

				{
    	        	JMenuItem optionAdd = new JMenuItem(new AbstractAction(properties.getProperty("ADD_MARKER")) {
    	        		
						private static final long serialVersionUID = 2485291894928488771L;

						@Override
						public void actionPerformed(ActionEvent e) {
							int selectedRow = fileTable.getSelectedRow();
							if(selectedRow != -1 ) {
								MediaFile mediaFile = assets.get(selectedRow);
								newMarkerFor(mediaFile);
								updateMarkerTable();
							}
						}
						
						public void newMarkerFor(MediaFile mediaFile) {
							List<Marker> markers = mediaFile.getMarkers();
							
							int h = 0, m = 0, s = 0,
								size = markers.size();
							Marker greatest, marker;
							
							if (size > 0) {
								greatest = markers.get(size - 1);
								h = greatest.getTimestamp().getHours();
								m = greatest.getTimestamp().getMinutes() + 1;
							}
							
							marker = new Marker(new Timestamp(h, m, s), properties.getProperty("DEFAULT_MARKER_VALUE"));
							mediaFile.addMarker(marker);
						}
    	        		
    	        	}),
    	        	
    	        	optionRemove = new JMenuItem(new AbstractAction(properties.getProperty("REMOVE_MARKER")) {
    	        		
						private static final long serialVersionUID = -8926172834189550986L;

						@Override
						public void actionPerformed(ActionEvent e) {
							MediaFile mediaFile = getSelectedMediaFile();
							if(mediaFile != null) {
								List<Marker> toBeRemoved = Arrays.asList(getSelectedMarkers(mediaFile));
								mediaFile.getMarkers().removeAll(toBeRemoved);
								updateMarkerTable();
							}
						}
						
    	        	}),
    	        	
    	        	optionMakeSubClip = new JMenuItem(new AbstractAction(properties.getProperty("CREATE_SUBCLIP")) {

						private static final long serialVersionUID = 2640354794662295896L;

						@Override
						public void actionPerformed(ActionEvent e) {
							MediaFile mediaFile = getSelectedMediaFile();
							if(mediaFile != null) {
								Marker[] markers = getSelectedMarkers(mediaFile);
								if(markers.length > 0) {
									MediaFile newFile = mediaFile.createSubClip(markers);
									model.addMediaFile(newFile);
									updateFileTable();
								}
							}
						}
    	        	});
    	        	
    	        	if(markerTable.getSelectedRow() == -1) {
    	        		optionRemove.setEnabled(false);
    	        		optionMakeSubClip.setEnabled(false);
    	        	}
    	        	
    	        	add(optionAdd);
    	        	add(optionRemove);
    	        	addSeparator();
    	        	add(optionMakeSubClip);
    	        	
    	        }}.show(e.getComponent(), e.getX(), e.getY());
    	    }
    	});
    	
        this.add(verticalSplit, BorderLayout.CENTER);
        
        {
        	JPanel additionalAttributes = new JPanel();
        	additionalAttributes.setLayout(new BoxLayout(additionalAttributes, BoxLayout.PAGE_AXIS));
        	
        	additionalAttributes.add(new ComboBox<Integer>(properties.getProperty("PERIOD"), model.getPeriode()) {

				@Override
				public Integer[] getOptions() {
					return new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9,
							10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
				}

				@Override
				public void setOption(Integer option) {
					model.setPeriode(option);
				}
        		
        	}.getPanel());
        	
        	additionalAttributes.add(new TextBox(properties.getProperty("YEAR")) {

				@Override
				public String getValue() {
					return Integer.toString(model.getJaartal());
				}

				@Override
				public void setValue(String value) {
					try {
						if(value != null && value.length() > 0) {
							int jaartal = Integer.parseInt(value);
							model.setJaartal(jaartal);
						}
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
        		
        	}.getPanel());
        	
        	additionalAttributes.add(new TextBox(properties.getProperty("TEACHER")) {

				@Override
				public String getValue() {
					return model.getDocent();
				}

				@Override
				public void setValue(String value) {
					model.setDocent(value);
				}
        		
        	}.getPanel());
        	
        	additionalAttributes.add(new ComboBox<Niveau>(properties.getProperty("LEVEL"), model.getNiveau()) {

				@Override
				public Niveau[] getOptions() {
					return Niveau.values();
				}

				@Override
				public void setOption(Niveau option) {
					model.setNiveau(option);
				}
        		
        	}.getPanel());
        	
        	additionalAttributes.add(new ComboBox<Cursus>(properties.getProperty("TYPE"), model.getCursus()) {

				@Override
				public Cursus[] getOptions() {
					return Cursus.values();
				}

				@Override
				public void setOption(Cursus option) {
					model.setCursus(option);
				}
        		
        	}.getPanel());
        	
        	additionalAttributes.add(new ComboBox<Vak>(properties.getProperty("COURSE"), model.getVak()) {

				@Override
				public Vak[] getOptions() {
					return Vak.values();
				}

				@Override
				public void setOption(Vak option) {
					model.setVak(option);
				}
        		
        	}.getPanel());
        	
        	this.add(additionalAttributes, BorderLayout.SOUTH);
        }
        
        fileTable.getColumnModel().getColumn(5).setPreferredWidth(15);
        fileTable.getColumnModel().getColumn(6).setPreferredWidth(15);
        
        if(model.getFiles().size() > 0) {
        	fileTable.changeSelection(0, 0, false, false);
        	markerTable.changeSelection(0, 0, false, false);
        }

	}
	
	/**
	 * Update the File table
	 */
	public void updateFileTable() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fileTable.updateUI();
			}
		});
	}

	/**
	 * Update the Marker table
	 */
	public void updateMarkerTable() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				markerTable.updateUI();
			}
		});
	}

}
