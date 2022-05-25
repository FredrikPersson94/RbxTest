package imports;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gantt.plugin.WindowAPI;

/**
 * Class for reading files
 * @author fredrikp
 * 
 */
public class FileReader {
	
	
	private static final Logger log = LoggerFactory.getLogger(FileReader.class);
	
	/**
	 * Selects a file with a given critera
	 * @param dir
	 * @param startsWith
	 * @param fileType
	 * @return
	 */
	public static File getFileFromDir(String dir, String startsWith, String fileType) {
		File returnFile = null;
	    try{
	        File[] matchingFiles = new File(dir).listFiles(new FilenameFilter() {
	            
	            @Override
	            public boolean accept(File file, String name) {                
	                return name.startsWith(startsWith) && name.endsWith(fileType);
	            }
	        });
	        for (int i = 0; i < matchingFiles.length; i++) {
	        	File f = matchingFiles[i];
	        	if(returnFile == null || f.lastModified() > returnFile.lastModified()) {
	        		returnFile = f;
	        	}	        		        	
	        }
	    }catch(Exception e){
	        System.err.println("error in getFileFromDir " + e);
	    }
	    
	    return returnFile;
	}
	public static File[] getFilesFromDir(String dir, String startsWith, String fileType) {
		File[] matchingFiles = null;
	    try{
	        matchingFiles = new File(dir).listFiles(new FilenameFilter() {
	            
	            @Override
	            public boolean accept(File file, String name) {                
	                return name.startsWith(startsWith) && name.endsWith(fileType);
	            }
	        });
	    }catch(Exception e){
	        System.err.println("error in getFileFromDir " + e);
	    }
	    return matchingFiles;
	}
	
	/**
	 * Lets the user pick a file from the file explorer. 
	 * @param wAPI
	 * @param filter
	 * @return
	 */
	public static File getFileFromDialouge(WindowAPI wAPI, FileNameExtensionFilter filter) {
		JFileChooser fc = wAPI.createFileChooser();
		if(filter == null) {
			fc.setAcceptAllFileFilterUsed(true);
		}
		else {
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(filter);
		}
		int returnVal = fc.showOpenDialog(null);
		File file = null;
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            //This is where a real application would open the file.
            log.info("Opening: " + file.getName());
        } else {
            log.info("Open command cancelled by user.");
        }
        return file;
	}

}
