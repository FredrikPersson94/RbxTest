package macros;

import gantt.plugin.BaseModelAPI;
import gantt.plugin.PluginAPI;
import gantt.utils.command.MacroExecutionContext;
import user.UserPlugin;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;


/**
 * Class for handling incomming REST calls.
 * 
 * @author Fredrik Persson (Fredrik.persson@novotek.com)
 * @version 1.0
 * @since 2020-04-17
 */
public class ReceiveRESTMacro extends MacroBase {

	public static String CMD_NAME = "ReceiveRESTMacro";
	public static String CMD_DESC = "Recieves order information from a REST source";
	private BaseModelAPI bAPI;

	public ReceiveRESTMacro(PluginAPI pAPI) {
		super(pAPI, CMD_NAME, CMD_DESC);

		this.bAPI = pAPI.getBaseModelAPI();
	}

	public static class ReceiveRESTMacroFactory extends MacroBaseFactory {

		private PluginAPI pAPI;

		public ReceiveRESTMacroFactory(PluginAPI pAPI) {
			this.pAPI = pAPI;
		}

		public MacroBase createMacro() {
			return new ReceiveRESTMacro(pAPI);
		}

	}

	// example:
	// http://localhost:9998/scheduler/v1/plan/macro/main/REST?updOpr={id:R364,oprName:SG+SKONA,oprStart:2020-04-25}
	public void customRun(final MacroExecutionContext ctx) {
		try {
			bAPI.beginAllChange();
			Map<String, Object> attachments = ctx.getAttachments();
			
			Logger logger = UserPlugin.getLogger();
			for (Entry<String, Object> entry : attachments.entrySet()) {
				System.out.println("Attachment:");

				if (logger == null) {
					System.out.println(entry.getKey() + " " + entry.getValue());
				} else {
					logger.info(entry.getKey() + " " + entry.getValue());

				}
			}
			

		} catch (Exception e) {
			System.err.println("Error in ReccieveREST: " + e);
			e.printStackTrace();
		} finally {
			bAPI.endAllChange();
		}
	}
}
