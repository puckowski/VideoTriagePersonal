package com.keypointforensics.videotriage.gui.localfile.wizard;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;

import com.keypointforensics.videotriage.gui.imagepanel.ScalableSimpleImagePanel;
import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ImageUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.util.WriteUtils;

public class FileDropList extends JPanel implements DropTargetListener {

	/*
	 * Author: Daniel Puckowski
	 */
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 2460800562707723383L;
	
	private IconDecoratedListModel listModel = new IconDecoratedListModel();
    private DropTarget dropTarget;
    private JScrollPane jScrollPane1;
    private JList list;

    private final ScalableSimpleImagePanel PREVIEW_PANEL;
    private final JTextArea                VIDEO_INFORMATION_TEXT_AREA;
    
    private boolean mIsEmpty;
    
    private final FileListCellRenderer mCellRenderer;
	
    /**
     * Create the panel.
     */
    public FileDropList(final ScalableSimpleImagePanel previewPanel, final JTextArea videoInformationTextArea) {
    	PREVIEW_PANEL               = previewPanel;
    	VIDEO_INFORMATION_TEXT_AREA = videoInformationTextArea;
    	
    	this.setLayout(new BorderLayout());
    	
        list = new JList();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(10);
        dropTarget = new DropTarget(list, this);
        list.setModel(listModel);
        list.setDragEnabled(true);
        mCellRenderer = new FileListCellRenderer(PREVIEW_PANEL);
        list.setCellRenderer(mCellRenderer);
        jScrollPane1 = new JScrollPane(list);
        WindowUtils.setScrollBarIncrement(jScrollPane1);
        
        this.add(new JLabel("Files To Process"), BorderLayout.NORTH);
        add(jScrollPane1, BorderLayout.CENTER);
        
        checkIfEmpty();
        
        list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 1) { 
					Object selectedObject = list.getSelectedValue();
					
					if(selectedObject instanceof File) {
						File selectedFile = (File) selectedObject;
						
						updateVideoInformationTextArea(selectedFile);
					}
				}
			}
		});
    }
	
    private void loadPreviewImageIfNecessary(final int index, final String absoluteFilePath) {
    	if(mCellRenderer.getShowIconPreviews() == true) {
			WriteUtils.mPreviewExtractionPool.execute(new Runnable() {
				@Override
				public void run() {
					WindowsVideoFrameExtractorLegacy.extractPreviewFrame(absoluteFilePath);
					
					if(mRevalidatingListModel == false) {
						mRevalidatingListModel = true;
						int delay = 1000;
						
						Timer timer = new Timer(delay, new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								EventQueue.invokeLater(new Runnable() {
									public void run() {
										listModel.update(index);
										
										mCellRenderer.revalidate();
										list.revalidate();
										
										mRevalidatingListModel = false;
									}
								});

							}
						});

						timer.setRepeats(false);
						timer.start();
					}
				}
				
			});
		}
    	else {
    		EventQueue.invokeLater(new Runnable() {
				public void run() {
					listModel.update(index);
					
					mCellRenderer.revalidate();
					list.revalidate();
					
					//mRevalidatingListModel = false;
				}
			});
    	}
    }
    
    public JList getList() {
    	return list;
    }
    
    public IconDecoratedListModel getListModel() {
    	return listModel;
    }
    
    public void setShowIconPreviews(final boolean showIconPreviews) {
    	mCellRenderer.setShowIconPreviews(showIconPreviews);
    	
    	updateIconPreviewsIfNecessary();
    }
    
    public void updateIconPreviewsIfNecessary() {
    	File currentFile = null;
    	
    	for(int i = 0; i < listModel.size(); ++i) {
    		if(listModel.get(i) instanceof File) {
    			currentFile = (File) listModel.get(i);
    			loadPreviewImageIfNecessary(i, currentFile.getAbsolutePath());
    		}
    	}
    }
    
    private void clearIfEmpty() {
    	if(mIsEmpty == true) {
    		listModel.removeAllElements();
    	}
    	
    	list.revalidate(); 
    }
    
    private void checkIfEmpty() {
    	if(listModel.isEmpty() == true) {
    		mIsEmpty = true;
    		listModel.addElement(new JLabel("No files added yet. Click \"Select Files\" or drag and drop video files here."));
    		
    		PREVIEW_PANEL.update(ImageUtils.NO_PREVIEW_AVAILABLE_IMAGE);
    		PREVIEW_PANEL.revalidate();
    	}
    	else {
    		mIsEmpty = false;
    	}
    	
    	list.revalidate(); 
    }
    
    public void dragEnter(DropTargetDragEvent arg0) {
        
    }

    public void dragOver(DropTargetDragEvent arg0) {
        
    }

    public void dropActionChanged(DropTargetDragEvent arg0) {
       
    }

    public void dragExit(DropTargetEvent arg0) {
        
    }

    public void drop(DropTargetDropEvent evt) {
    	clearIfEmpty();
			    	
    	int action = evt.getDropAction();
    	evt.acceptDrop(action);
		try {
			Transferable data = evt.getTransferable();
			if (data.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				List<File> files = (List<File>) data.getTransferData(DataFlavor.javaFileListFlavor);
				for (File file : files) {
					listModel.addElement(file);
					
					final int index = listModel.getSize() - 1;
					loadPreviewImageIfNecessary(index, file.getAbsolutePath());
				}
			}
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			evt.dropComplete(true);
		}

		checkIfEmpty();	
    }
    
    private volatile boolean mRevalidatingListModel;
    
    public void addFile(final String absoluteFilePath) {										
		clearIfEmpty();

		listModel.addElement(new File(absoluteFilePath));
		final int index = listModel.getSize() - 1;
		
		if(mCellRenderer.getShowIconPreviews() == true) {
			loadPreviewImageIfNecessary(index, absoluteFilePath);
		}
		
		checkIfEmpty();
    }
    
    public void clearListAndPreview() {
    	listModel.removeAllElements();
    	
    	PREVIEW_PANEL.clear();
    	PREVIEW_PANEL.revalidate();
    	
    	VIDEO_INFORMATION_TEXT_AREA.setText("");
    	VIDEO_INFORMATION_TEXT_AREA.revalidate();
    	
    	checkIfEmpty();
    	
    	list.revalidate(); 
    }
    
    public void removeSelectedIndex() {
    	final int selectedIndex = list.getSelectedIndex();
    	
    	if(selectedIndex < 0) {
    		return;
    	}
    	
    	listModel.remove(selectedIndex);
    	
    	PREVIEW_PANEL.clear();
    	PREVIEW_PANEL.revalidate();
    	
    	VIDEO_INFORMATION_TEXT_AREA.setText("");
    	VIDEO_INFORMATION_TEXT_AREA.revalidate();
    	
    	checkIfEmpty();
    	
    	list.revalidate(); 
    }
    
    public String getFileList() {
    	String fileList = "";

    	boolean firstAppend = true;
    	
    	for(int i = 0; i < listModel.getSize(); ++i)
    		if(listModel.getElementAt(i) instanceof File == false) {
    			continue;
    		}
    		else if(firstAppend) {
    			fileList += listModel.getElementAt(i);
    			firstAppend = false;
    		}
    		else
    			fileList += "\n" + listModel.getElementAt(i);
    	
    	return fileList;
    }
    
    private void updateVideoInformationTextArea(final File videoFile) {
    	VIDEO_INFORMATION_TEXT_AREA.setText("");
		
		SimpleDateFormat lastModifiedDateFormatter = new SimpleDateFormat(Utils.LAST_MODIFIED_DATE_FORMAT);
		final String formattedLastModifiedDate = lastModifiedDateFormatter.format(videoFile.lastModified());
		final String videoName = videoFile.getName();
		final String absoluteVideoPath = videoFile.getAbsolutePath();
		String videoPath = absoluteVideoPath;
		
		if(videoPath.contains(File.separator) == true) {
			videoPath = videoPath.substring(0, videoPath.lastIndexOf(File.separator) + 1);
		}
		
		VIDEO_INFORMATION_TEXT_AREA.append("Name: " + videoName + "\n");
		VIDEO_INFORMATION_TEXT_AREA.append("Path: " + FileUtils.DATABASE_DIRECTORY + "\n");
		VIDEO_INFORMATION_TEXT_AREA.append("Absolute Path: " + absoluteVideoPath + "\n\n");
		VIDEO_INFORMATION_TEXT_AREA.append("Last Modified: " + formattedLastModifiedDate + "\n\n");
		VIDEO_INFORMATION_TEXT_AREA.append("Can Read: " + videoFile.canRead() + "\n");
		VIDEO_INFORMATION_TEXT_AREA.append("Can Write: " + videoFile.canWrite() + "\n");
		VIDEO_INFORMATION_TEXT_AREA.append("Database Size (bytes): " + FileUtils.humanReadableByteCount(videoFile.length()) + "\n");
		VIDEO_INFORMATION_TEXT_AREA.append("\n");
		
		VIDEO_INFORMATION_TEXT_AREA.append("Additional Video File Information:\n");
		VIDEO_INFORMATION_TEXT_AREA.append(WindowsVideoFrameExtractorLegacy.getVideoFileInformation(absoluteVideoPath) + "\n");
	}
}