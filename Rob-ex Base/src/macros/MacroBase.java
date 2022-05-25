package macros;

import gantt.plugin.PluginAPI;
import gantt.utils.command.CmdExecuteException;
import gantt.utils.command.GanttMacroCmdItem;
import gantt.utils.command.GanttMemento;
import gantt.utils.command.MacroExecutionContext;
import gantt.gui.logmanager.LogManager;
import gantt.gui.logmanager.LogObject;
import gantt.gui.logmanager.LogSession;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for custom macros
 * 
 * @author fredrikp
 *
 */
public abstract class MacroBase extends GanttMacroCmdItem {
	private static final Logger log = LoggerFactory.getLogger(MacroBase.class);
	private String macroDescription;
	private PluginAPI pAPI;

	/** Example boolean parameter on the macro */
	private boolean debugOn = false;

	/** Added just so user has some info. Cannot be changed by the user */

	public MacroBase(PluginAPI pluginAPI, String macroName, String macroDescription) {
		this.pAPI = pluginAPI;
		this.macroDescription = macroDescription;
		setName(macroName);
	}

	public GanttMemento createMemento() {
		return null;
	}

	public GanttMemento createMemento(GanttMemento arg0) {
		return null;
	}

	public void execute() throws CmdExecuteException {
	}

	public void execute(final MacroExecutionContext ctx) throws CmdExecuteException {
		Runnable r = new Runnable() {
			public void run() {
				log.info("Macro " + getName() + " started.");
				try {
					customRun(ctx);

				} catch (Exception e) {
					log.error("", e);
				} finally {
				}
			}
		};
		try {
			if (!SwingUtilities.isEventDispatchThread())
				SwingUtilities.invokeAndWait(r);
			else
				r.run();

		} catch (Exception e) {
			log.error("Macro " + getName() + ": Error running the macro!", e);
			Throwable causingException = e;
			if (e instanceof InvocationTargetException)
				causingException = ((InvocationTargetException) e).getCause();

			// Display an error in the Standard Log window
			LogSession logSession = pAPI.getBaseModelAPI()
					.getLogSession(LogManager.DEFAULT_LOG_SESSION_KEY /* pAPI.getIntegrationLogSessionKey() */);
			logSession.createAndShowLogObject(LogObject.ERROR_OBJECT, "Macro " + getName(), "Execute action",
					"Unexpected error", causingException);

			throw new CmdExecuteException("Macro " + getName() + " unknown exception: " + e.toString(),
					causingException);
		}
	}

	public void showMsgToUser(int type, String source, String action, String title, String desc) {
		LogSession logSession = pAPI.getBaseModelAPI()
				.getLogSession(LogManager.DEFAULT_LOG_SESSION_KEY /* pAPI.getIntegrationLogSessionKey() */);
		logSession.createAndShowLogObject(type, source, action, title, desc);
	}

	/**
	 * @return the debugOn
	 */
	public boolean isDebugOn() {
		return debugOn;
	}

	/**
	 * @param debugOn the debugOn to set
	 */
	public void setDebugOn(boolean debugOn) {
		this.debugOn = debugOn;
	}

	/**
	 * @return the macroDescription
	 */
	public String getMacroDescription() {
		return macroDescription;
	}

	public void setMemento(GanttMemento arg0) {
	}

	public abstract void customRun(final MacroExecutionContext ctx);

}
