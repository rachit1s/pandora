/*
 * Copyright 2007 Manuel Carrasco Moñino. (manuel_carrasco at users.sourceforge.net) 
 * http://code.google.com/p/gwtupload
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gwtupload.client;

import gwtupload.client.DecoratedFileUpload.FileUploadWithMouseEvents;

import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Interface used by Uploaders to use and configure a customized file input.
 *  
 * Widgets implementing this interface have to render a file input tag because 
 * it will be added to the form which is sent to the server.
 * 
 * This interface has thought to let the user the option to create customizable
 * panels for file inputs.
 * 
 * @author Manolo Carrasco Moñino
 *
 */
public interface IFileInput extends HasChangeHandlers {
  
  /**
   * A DecoratedFileInput implementing the IFileInput interface
   * 
   */
  public class ButtonFileInput extends DecoratedFileUpload implements IFileInput {

    public ButtonFileInput() {
      super();
    }

    public ButtonFileInput(Widget w) {
      super(w);
    }

    public IFileInput newInstance() {
      ButtonFileInput ret = new ButtonFileInput();
      Widget widget = button != null ? button : new Button(this.getText());
      ret.setButton(widget);
      return ret;
    }

    public void setLength(int length) {
    }
  }
  
  
  /**
   * Just a FileUpload which implements the interface IFileInput
   */
  public class BrowserFileInput extends FileUploadWithMouseEvents implements IFileInput {

    public BrowserFileInput() {
      super();
    }

    public Widget getWidget() {
      return this;
    }

    public IFileInput newInstance() {
      return new BrowserFileInput();
    }

    public void setLength(int length) {
      DOM.setElementAttribute(getElement(), "size", "" + length);
    }

    /**
     * It is not possible to change the button text in a input type=file 
     */
    public void setText(String text) {
    }
  }
  
  /**
   * Enum for different IFileInput implementations
   */
  public enum FileInputType implements HasFileInputType {
    BROWSER_INPUT {
      public IFileInput getInstance() {
        return GWT.create(BrowserFileInput.class);
      }
    },
    BUTTON {
      public IFileInput getInstance() {
        return GWT.create(ButtonFileInput.class);
      }
    },
    ANCHOR {
      public IFileInput getInstance() {
        return GWT.create(AnchorFileInput.class);
      }
    },
    LABEL {
      public IFileInput getInstance() {
        return GWT.create(LabelFileInput.class);
      }
    }
  }

  /**
   * interface for FileInputType enum
   */
  interface HasFileInputType {
    IFileInput getInstance(); 
  }
  
  /**
   * A HyperLinkFileInput implementing the IFileInput interface
   * 
   */
  public class AnchorFileInput extends ButtonFileInput {
    public AnchorFileInput() {
      super(new Anchor());
    }
  }

  /**
   * A LabelFileInput implementing the IFileInput interface
   * 
   */
  public class LabelFileInput extends ButtonFileInput {
    public LabelFileInput() {
      super(new Label());
      addChangeHandler(new ChangeHandler() {
        public void onChange(ChangeEvent event) {
          setText(getFilename());
        }
      });
    }
  }

  /**
   * Gets the filename selected by the user. This property has no mutator, as
   * browser security restrictions preclude setting it.
   * 
   * @return the widget's filename
   */
  String getFilename();

  /**
   * Gets the name of this input element.
   * 
   * @return fieldName
   */
  String getName();

  /**
   * Returns the widget which will be inserted in the document.
   */
  Widget getWidget();

  /**
   * Creates a new instance of the current object type.
   * 
   * @return a new instance
   */
  IFileInput newInstance();

  /**
   * Set the length in characters of the fileinput which are shown.
   * 
   * @param length
   */
  void setLength(int length);

  /**
   * Sets the html name for this input element. 
   * It is the name of the form parameter sent to the server.
   *  
   * @param fieldName
   */
  void setName(String fieldName);

  /**
   * Set the size of the widget.
   * 
   * @param width
   * @param height
   */
  void setSize(String width, String height);

  /**
   * Set the text for the link which opens the browse file dialog.
   * 
   * @param text
   */
  void setText(String text);

  void setVisible(boolean b);

}
