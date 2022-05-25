package macros;

import org.w3c.dom.Element;

import gantt.utils.command.GanttCmdItem;
import gantt.utils.command.GanttCmdItemFactoryXML;

public abstract class MacroBaseFactory implements GanttCmdItemFactoryXML {

        /**
         * This method is called by the system whenever a new Macro instance is needed
         */
        public GanttCmdItem createGanttCmdItem(Element macroElement) {
          MacroBase cmd = createMacro();
          cmd.applyCommonAttributes(macroElement);
          
          cmd.setDebugOn(
                  Boolean.parseBoolean(getAttributeWithDefault(macroElement, "debugOn", Boolean.FALSE.toString())));
            return cmd;
        }

        /**
         * Helper method. Return the value of specified attribute or the default value
         * if nothing was specified or the value is empty
         * 
         * @param macroElement
         * @param attrName
         * @param defaultValue
         * @return the value of specified attribute or the default value if nothing was
         *         specified or the value is empty
         */
        private String getAttributeWithDefault(Element macroElement, String attrName, String defaultValue) {
            String strAttrValue = macroElement.getAttribute(attrName);
            if (strAttrValue == null || strAttrValue.length() == 0)
                return defaultValue;

            return strAttrValue;
        }

        /** Is the macro allowed in the Viewer or not */
        public boolean isAllowedInViewer() {
            return true;
        }
        
        public abstract MacroBase createMacro();

}