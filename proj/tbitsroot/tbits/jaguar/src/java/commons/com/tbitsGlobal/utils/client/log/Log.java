/*
 * Copyright 2009 Fred Sauer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package commons.com.tbitsGlobal.utils.client.log;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.logging.client.NullLogHandler;

// CHECKSTYLE_JAVADOC_OFF
public final class Log {

	private static Logger logger = Logger.getLogger("");
	
	private static TbitsWidgetLogger widgetLogger;
	
	static{
		init();
	}
	
	public static void init() {
		widgetLogger =  GWT.create(TbitsWidgetLogger.class);
	    addHandlerIfNotNull(logger, new ContainerLogHandler(widgetLogger));
	}
	
	private static void addHandlerIfNotNull(Logger l, Handler h) {
	      if (!(h instanceof NullLogHandler)) {
	        l.addHandler(h);
	      }
	}
	
	

  public static void clear() {
  }

  public static void error(String message) {
    error(message, (Throwable) null);
  }

  public static void error(String message, Throwable e) {
	  logger.log(Level.SEVERE, message, e);
  }

  public static int getCurrentLogLevel() {
	  return logger.getLevel().intValue();
  }

  public static String getCurrentLogLevelString() {
	  return logger.getLevel().getName();
  }
  
  public static void setCurrentLogLevel(Level level){
	  logger.setLevel(level);
  }

  public static void info(String message) {
	  info(message, (Throwable) null);
  }

  public static void info(String message, Throwable e) {
	  logger.log(Level.INFO, message, e);
  }

  public static void warn(String message) {
	  warn(message, (Throwable) null);
  }

  public static void warn(String message, Throwable e) {
	  logger.log(Level.WARNING, message, e);
  }
  
  	public static TbitsWidgetLogger getWidgetLogger() {
  		return widgetLogger;
  	}
}
