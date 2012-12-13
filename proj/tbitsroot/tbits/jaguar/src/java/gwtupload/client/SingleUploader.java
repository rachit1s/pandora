/*
 * Copyright 2009 Manuel Carrasco Moñino. (manuel_carrasco at users.sourceforge.net) 
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

import gwtupload.client.IFileInput.FileInputType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FormPanel;

/**
 * <p>
 * Implementation of a single uploader panel with a submit button.
 * </p>
 * 
 * @author Manolo Carrasco Moñino
 * 
 * <p>
 * When the user selects a file, the button changes its style
 * so the she could realize that she has to push the button.
 * </p>
 *
 */
public class SingleUploader extends Uploader {

  private Button button;

  /**
   * Default constructor.
   * Uses the standard browser input, a basic status widget, and
   * creates a standard button to send the file
   * 
   */
  public SingleUploader() {
    this(FileInputType.BROWSER_INPUT);
  }

  /**
   * Use a basic status widget, and creates 
   * a standard button to send the file
   * 
   * @param type
   *        file input to use
   */
  public SingleUploader(FileInputType type) {
    this(type, null);
  }

  /**
   * Uses the standard browser input, customized status, and creates a 
   * standard button to send the file
   * 
   * @param status
   *        Customized status widget to use
   */
  public SingleUploader(IUploadStatus status) {
    this(FileInputType.BROWSER_INPUT, status);
  }

  /**
   * Creates a standard button to send the file
   * 
   * @param type
   *        file input to use
   * @param status
   *        Customized status widget to use
   */
  public SingleUploader(FileInputType type, IUploadStatus status) {
    this(type, status, new Button());
  }

  /**
   * Constructor
   * 
   * @param type
   *        file input to use
   * @param status
   *        Customized status widget to use
   * @param button
   *        Customized button which submits the form
   */
  public SingleUploader(FileInputType type, IUploadStatus status, Button button) {
    this(type, status, button, null);
  }

  /**
   * This constructor allows to use an existing form panel.
   * 
   * @param type
   *        file input to use
   * @param status
   *        Customized status widget to use
   * @param button
   *        Customized button which submits the form
   * @param form
   *        Customized form panel
   */
  public SingleUploader(FileInputType type, IUploadStatus status, Button button, FormPanel form) {
    super(type, form);

    final Uploader thisInstance = this;

    if (status == null) {
      status = new ModalUploadStatus();
    }
    super.setStatusWidget(status);

    this.button = button;
    if (button.getText().length() == 0) {
      button.setText(i18nStrs.uploaderSend());
    }

    button.addStyleName("submit");
    button.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        thisInstance.submit();
      }
    });

    // The user could have attached the button anywhere in the page.
    if (button.getParent() == null) {
      super.add(button);
    }
  }

  /**
   * 
   * @param status
   *        Customized status widget to use
   * @param button
   *        Customized button which submits the form
   */
  public SingleUploader(IUploadStatus status, Button button) {
    this(FileInputType.BROWSER_INPUT, status, button, null);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.Uploader#setI18Constants(gwtupload.client.IUploader.UploaderConstants)
   */
  @Override
  public void setI18Constants(UploaderConstants strs) {
    super.setI18Constants(strs);
    button.setText(strs.uploaderSend());
  }

  /* (non-Javadoc)
   * @see gwtupload.client.Uploader#onChangeInput()
   */
  @Override
  protected void onChangeInput() {
    super.onChangeInput();
    button.addStyleName("changed");
    button.setFocus(true);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.Uploader#onFinishUpload()
   */
  @Override
  protected void onFinishUpload() {
    super.onFinishUpload();
    button.setEnabled(true);
    button.removeStyleName("changed");
  }

  /* (non-Javadoc)
   * @see gwtupload.client.Uploader#onStartUpload()
   */
  @Override
  protected void onStartUpload() {
    super.onStartUpload();
    button.setEnabled(false);
    button.removeStyleName("changed");
  }

}
