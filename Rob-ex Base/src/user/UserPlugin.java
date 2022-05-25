package user;
/*
 *  Project:      
 *  Author:       Fredrik Persson
 *
 */

import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JPanel;

import macros.ReceiveRESTMacro;
import gantt.plugin.Plugin;
import gantt.plugin.PluginAPI;
import gantt.utils.command.MacroManager;
import gantt.utils.file.CombinedResourceBundle;

/**
 * UserPlugin implements the gantt.plugin.Plugin interface, and adds
 * Customization of labels and tooltip text on operations
 */
public class UserPlugin extends JPanel implements Plugin {

	private static final long serialVersionUID = 1L;
	private static PluginAPI pAPI;
	private static FileHandler fh;
	private static final String pluginVersion = "v1";
//	private WindowAPI wAPI = null;

	// Property language file location
	private static String propertyFile = "userproperties";

	private static ResourceBundle properties = null;

	/**
	 * The constructor, usually will not do anything, the real construction will
	 * take place in the init() method instead.
	 */
	public UserPlugin() {
	}

	/**
	 * Returns the version number for this plugin - is visible to users in the About
	 * menu inside ROB-EX Gantt
	 *
	 * @return A version String of the format x.xxx, eg. 1.001
	 */
	public String getVersionInfo() {
		
		return pluginVersion;
	}

	/**
	 * The init method is called from ROB-EX Gantt when the plugin is loaded.
	 *
	 * @param api the object for accessing the different ROB-EX Gantt API's
	 * @return true on succesfull initialization
	 */
	public boolean init(PluginAPI api) {
		// Obtain the language locale
//		System.setProperty("javax.net.ssl.trustStore", "C:/Program Files/Java/jre1.8.0_181/lib/security/cacerts");
//		 System.setProperty("java.util.logging.SimpleFormatter.format", 
//				 "=%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$-6s %2$s %5$s%6$s%n");
		Locale myLocale = api.getLocale();
		new CombinedResourceBundle(myLocale, propertyFile, propertyFile, null);

		try {
			// obtain the different api's we'll need in order to communicate with
			// the ROB-EX Gantt system
			pAPI = api;
			initMacros();
			return true;
		} catch (Exception e) {
			// some error occured
			e.printStackTrace();
		}

		// returning false will inform ROB-EX Gantt that the initialisation failed
		return false;
	}

	public static void initFileHandler(String service) {

//		SimpleDateFormat format = new SimpleDateFormat("M-d");
		try {
			fh = new FileHandler(".\\Data\\Rbx-log " + service + ".log", 15000000, 10, true); // " - " +
																								// format.format(Calendar.getInstance().getTime())
																								// +
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		fh.setFormatter(new SimpleFormatter() {
			private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

			@Override
			public String formatMessage(LogRecord record) {
				return String.format(format, new Date(record.getMillis()), record.getLevel().getLocalizedName(),
						record.getMessage());
			}
		});
	}

	public static FileHandler getFileHandler() {
		return fh;
	}

	public static Logger getLogger() {
		try {

			Logger logger = Logger.getLogger(UserPlugin.class.getName());
			Handler[] handlers = logger.getHandlers();
			if (handlers.length == 0) {
				if(fh != null) {
					logger.addHandler(fh);
				}
			}

			return logger;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void initMacros() {
		MacroManager macroManager = pAPI.getMacroManager();

		macroManager.addCmdItemFactory(ReceiveRESTMacro.CMD_NAME, new ReceiveRESTMacro.ReceiveRESTMacroFactory(pAPI));
	}

	/**
	 * Called when gantt is closing the plugins (ie. when gantt is closing down)
	 *
	 * @param api the object for accessing the Gantt API's
	 */
	public void destroy(PluginAPI api) {
		System.out.println("Destroy on UserPlugin called from Gantt");
	}

	/**
	 * Get a property string from the language bundle
	 *
	 * @param key the property key to lookup
	 * @return the string matching the key or the key if no match is found
	 */
	public static String getString(String key) {
		try {
			String returnValue = null;
			if (properties != null) {
				returnValue = properties.getString(key);
			}
			return returnValue;
		} catch (MissingResourceException e) {
			e.printStackTrace();
			return key;
		}
	}
}
