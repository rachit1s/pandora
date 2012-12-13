using System;
using System.Text;
using System.Collections;
using System.Collections.Generic;
using Outlook = Microsoft.Office.Interop.Outlook;
using Office = Microsoft.Office.Core;
using System.Windows.Forms;
using Word = Microsoft.Office.Interop.Word;
using System.Text.RegularExpressions;

namespace Qutbits
{
    internal class InspectorWrapper : WrapperClass
    {
        public Outlook.Inspector inspector;
        Office.CommandBar _CommandBar = null;
        Office.CommandBarButton _TrackButton;
        Office.CommandBarComboBox _BAComboBox = null;
        Office.CommandBarComboBox _DueDateText;
        Office.CommandBarButton _DueDateBtn;
        WindowHandle owner;
                
        //Office.CommandBarButton _CategoryBtn;
        //Office.CommandBarButton _StatusBtn;
        Office.CommandBarButton _TBitsBtn;
        Office.CommandBarButton _SmsBtn;

        //Office.CommandBarComboBox _CategoryComboBox;
        Office.CommandBarComboBox _StatusComboBox;

        DatePicker dp;

        //Word.Document wordDoc;
        Outlook.MailItem curMailItem = null;
        BAXmlParser baXmlParser = BAXmlParser.GetInstance();
        BA curBA = null;
        string category, status;
        Regex fwPattern= new Regex("FW: +[a-zA-Z]+#[0-9]+:.*");
        
        string toolbarName = "tBits Plugin";
        bool newCompose = false;
        bool firstActivate = true;
        const int startIndex = 1;
               
        protected object _Missing = System.Reflection.Missing.Value;

        string newLine = "\n";//Environment.NewLine;
       
        public InspectorWrapper(Outlook.Inspector inspector)
        {   
            try
            {                
                this.inspector = inspector;            
                curMailItem = (Outlook.MailItem)inspector.CurrentItem;
                ConnectEvents();
                currentId = Id.ToString();
                owner = new WindowHandle(inspector);
                if (!inspector.IsWordMail())
                {
                    //wordDoc = ((Word.Document)(inspector.WordEditor));
                    //owner = new WindowHandle(wordDoc.ActiveWindow);
                    CreateToolBar();                   
                }
            }
            catch (System.Exception ex)
            {
                MessageBox.Show(owner,"Exception while initializing inspector wrapper: \n "+ex.StackTrace+
                    "\n\nMessage: \n"+ex.Message);
            }
        }

        void ConnectEvents()
        {
            ((Outlook.InspectorEvents_10_Event)inspector).Activate += 
                new Outlook.InspectorEvents_10_ActivateEventHandler(InspectorWrapper_Activate);            
            ((Outlook.InspectorEvents_10_Event)inspector).Close +=
                new Outlook.InspectorEvents_10_CloseEventHandler(InspectorWrapper_Close);
            ((Outlook.InspectorEvents_10_Event)inspector).Deactivate += 
                new Outlook.InspectorEvents_10_DeactivateEventHandler(InspectorWrapper_Deactivate);
            ((Outlook.ItemEvents_10_Event)curMailItem).Send +=
                new Microsoft.Office.Interop.Outlook.ItemEvents_10_SendEventHandler(InspectorWrapper_Send);
            ((Outlook.ItemEvents_10_Event)curMailItem).Reply +=
                new Microsoft.Office.Interop.Outlook.ItemEvents_10_ReplyEventHandler(InspectorWrapper_Reply);
            ((Outlook.ItemEvents_10_Event)curMailItem).Forward +=
                new Outlook.ItemEvents_10_ForwardEventHandler(InspectorWrapper_Forward);
        }

        void DisconnectEvents()
        {
            ((Outlook.InspectorEvents_10_Event)inspector).Activate -= 
                new Outlook.InspectorEvents_10_ActivateEventHandler(InspectorWrapper_Activate);            
            ((Outlook.InspectorEvents_10_Event)inspector).Close -= 
                new Outlook.InspectorEvents_10_CloseEventHandler(InspectorWrapper_Close);
            ((Outlook.InspectorEvents_10_Event)inspector).Deactivate -= 
                new Outlook.InspectorEvents_10_DeactivateEventHandler(InspectorWrapper_Deactivate);
            ((Outlook.ItemEvents_10_Event)curMailItem).Send -=
               new Microsoft.Office.Interop.Outlook.ItemEvents_10_SendEventHandler(InspectorWrapper_Send);
            ((Outlook.ItemEvents_10_Event)curMailItem).Reply -=
                new Microsoft.Office.Interop.Outlook.ItemEvents_10_ReplyEventHandler(InspectorWrapper_Reply);
            ((Outlook.ItemEvents_10_Event)curMailItem).Forward -=
                new Outlook.ItemEvents_10_ForwardEventHandler(InspectorWrapper_Forward);
        }    

        void InspectorWrapper_Close()
        {
            DisconnectEvents();
            inspector = null;
            GC.Collect();
            GC.WaitForPendingFinalizers();
            OnClosed();
        }           

        #region Activate / Deactivate
        void InspectorWrapper_Activate()
        {
            if (inspector.IsWordMail() && (firstActivate))
            {
                CreateToolBar();
                firstActivate = false;
            }           
            currentId = Id.ToString();
        }
        
        void InspectorWrapper_Deactivate()
        {
            previousId = Id.ToString();          
        }

        void InspectorWrapper_Send(ref bool cancel)
        {            
            string assigneeStr = null;
            string trackStr = null;
            string subscriberStr = null;

            if (_TrackButton.State == Office.MsoButtonState.msoButtonDown)
            {
                try
                {                   
                    assigneeStr = curMailItem.To;
                    curMailItem.RecipientReassignmentProhibited = false;
                    curMailItem.To = curBA.email;
                    if (curMailItem.CC != null)
                    {
                        subscriberStr = curMailItem.CC;
                        curMailItem.CC = "";
                    }

                    curMailItem.Recipients.ResolveAll();

                    MessageBox.Show(owner,"Tracking Enabled. Mail will be redirected through tBits as shown below:\n"
                        + "user(" + curMailItem.SenderEmailAddress + ") -> " + curMailItem.To + " -> " + assigneeStr);

                    //Don't set assignee string if it a reply to tbits mail
                    if (isBA(assigneeStr))
                        assigneeStr = "";
                    else
                    {
                        trackStr = "/assignee:" + assigneeStr + newLine; 
                    }

                    if (subscriberStr != null)
                        trackStr = trackStr+"/subscribers:" + subscriberStr + newLine;

                    if (_DueDateText.Text == "")
                        if (newCompose)
                            trackStr = trackStr + "/dueby:" + DateTime.Today.AddDays(2).ToShortDateString()
                                + newLine;
                        else
                            trackStr = trackStr + newLine;
                    else
                        trackStr = trackStr + "/dueby:" + _DueDateText.Text + newLine;

                    //if ((_CategoryBtn.State == Office.MsoButtonState.msoButtonDown) &&
                    if (category != null)
                        trackStr = trackStr + "/category_id:" + category + newLine;

                    //if ((_StatusBtn.State == Office.MsoButtonState.msoButtonDown) &&
                    if (status != null)
                        trackStr = trackStr + "/status:" + status + newLine;

                    if (_SmsBtn.State == Office.MsoButtonState.msoButtonDown)
                        trackStr = trackStr + "/SendSMS:" + true + newLine;                    
                    
                    curMailItem.Body = trackStr + newLine + curMailItem.Body.Replace("\\n", Environment.NewLine);
                }
                catch (Exception exp)
                {
                    MessageBox.Show(owner,exp.StackTrace + "\n" + "Message:" + exp.Message);
                }
            }
        }

        void InspectorWrapper_Reply(Object obj, ref bool cancel)
        {
            newCompose = false;
        }

        void InspectorWrapper_Forward(Object obj, ref bool cancel)
        {
            newCompose = true;
        }

        #endregion

        private void CreateToolBar()
        {           
            try
            {
                //baXmlParser.reloadXml();
                if (inspector == null) return;

                if (inspector.IsWordMail())
                {                    
                    // check, if there is already our CommandBar
                    foreach (Office.CommandBar bar in inspector.CommandBars)
                    {
                        if (((bar.Name).Contains("tBits Plugin")) &&
                            inspector.IsWordMail())
                        {
                            bar.Visible = false;
                            bar.Enabled = false;
                        }
                    }
                    _CommandBar = inspector.CommandBars.Add("tBits Plugin" + Id.ToString(), _Missing, _Missing, true);
                }
                else 
                {
                    foreach (Office.CommandBar bar in inspector.CommandBars)
                    {
                        if (bar.Name == toolbarName)
                        {
                            _CommandBar = bar;
                            foreach (Office.CommandBarControl ctrl in _CommandBar.Controls)
                            {
                                ctrl.Delete(false);
                            }
                            break;
                        }
                    }            

                    // If we found our CommandBar, we can use it
                    if (_CommandBar == null)
                    {
                        // if not we create one
                        try
                        {
                            _CommandBar = inspector.CommandBars.Add(toolbarName,
                                _Missing, _Missing, true);
                        }
                        catch (Exception exp)
                        {
                            MessageBox.Show(owner,"Exception positioning inspector commandbar: " + exp.StackTrace
                                +"\n\nMessage: "+exp.Message);
                        }
                    }                                  
                }
                
                // Set newCompose value based on whether the opened window is New message/reply/forward window
                setNewCompose(curMailItem);
                                                
                // Add _ button
                _TrackButton = (Office.CommandBarButton)_CommandBar.Controls.Add(
                    Office.MsoControlType.msoControlButton, 1, _Missing, 1, 1);

                _TrackButton.Caption = "Track";
                _TrackButton.Tag = Id.ToString();
                _TrackButton.Style = Office.MsoButtonStyle.msoButtonCaption;

                // Register for Click event
                _TrackButton.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(
                    _Track_Btn_Click);
               
                // Add _ ComboBox Control
                _BAComboBox = (Office.CommandBarComboBox)_CommandBar.Controls.Add(
                    Office.MsoControlType.msoControlComboBox, 1, _Missing, 2, 1);
                _BAComboBox.Caption = "BA:";
                _BAComboBox.Width = 150;
                _BAComboBox.Style = Office.MsoComboStyle.msoComboLabel;
                _BAComboBox.Tag = Id.ToString();
                // Fill the ComboBox
                fillBAComboBox();
                _BAComboBox.Change += new Microsoft.Office.Core._CommandBarComboBoxEvents_ChangeEventHandler(
                    _BA_ComboBox_Change);

                //Button to set due date
                _DueDateText = (Office.CommandBarComboBox)_CommandBar.Controls.Add(
                    Office.MsoControlType.msoControlEdit, 1, _Missing, 3, 1);
                _DueDateText.BeginGroup = true;
                _DueDateText.Width = 115;
                _DueDateText.Caption = "Due date:";
                _DueDateText.Style = Office.MsoComboStyle.msoComboLabel;
                string curSubject = null;
                if (curMailItem.Subject == null)
                    curSubject = "";
                else
                    curSubject = curMailItem.Subject;
                if ((newCompose) && (!fwPattern.IsMatch(curSubject)))
                    _DueDateText.Text = DateTime.Today.AddDays(2).ToShortDateString();
                else
                    _DueDateText.Text = null;
                _DueDateText.Visible = true;
                _DueDateText.Tag = Id.ToString();
                _DueDateText.Change += new Office._CommandBarComboBoxEvents_ChangeEventHandler(
                    _Due_Date_Change_Event);

                _DueDateBtn = (Office.CommandBarButton)_CommandBar.Controls.Add(
                    Office.MsoControlType.msoControlButton, 1, _Missing, 4, 1);
                _DueDateBtn.Width = 10;
                _DueDateBtn.Style = Office.MsoButtonStyle.msoButtonIcon;
                _DueDateBtn.FaceId = 125;
                _DueDateBtn.Tag = Id.ToString()+"_DueDateBtn";
                _DueDateBtn.Click += new Office._CommandBarButtonEvents_ClickEventHandler(
                    _Due_Date_Btn_Click);

                dp = new DatePicker();
                dp.Visible = false;

                //_CategoryBtn = (Office.CommandBarButton)_CommandBar.Controls.Add(
                //    Office.MsoControlType.msoControlButton, 1, _Missing, 5, 1);
                //_CategoryBtn.Width = 50;
                //_CategoryBtn.BeginGroup = true;
                //_CategoryBtn.Caption = "Category:";
                //_CategoryBtn.Style = Office.MsoButtonStyle.msoButtonCaption;
                //_CategoryBtn.FaceId = 125;
                //_CategoryBtn.Tag = Id.ToString().ToString() + "_CategoryBtn";
                //_CategoryBtn.Enabled = true;
                //_CategoryBtn.Click += new Office._CommandBarButtonEvents_ClickEventHandler(
                //    _Category_Btn_Click);

                //_CategoryComboBox = (Office.CommandBarComboBox)_CommandBar.Controls.Add(
                //   Office.MsoControlType.msoControlComboBox, 1, _Missing, 5, 1);
                //_CategoryComboBox.Caption = "Category";
                //_CategoryComboBox.Style = Office.MsoComboStyle.msoComboLabel;
                //_CategoryComboBox.Width = 200;
                //_CategoryComboBox.Enabled = true;
                //fillCategoryComboBox();
                //_CategoryComboBox.Tag = Id.ToString().ToString() + "_CategoryComboBox";
                //_CategoryComboBox.Change += new Office._CommandBarComboBoxEvents_ChangeEventHandler(
                //    _Category_Change_Event);

                //_StatusBtn = (Office.CommandBarButton)_CommandBar.Controls.Add(
                //    Office.MsoControlType.msoControlButton, 1, _Missing, 7, 1);
                //_StatusBtn.Width = 50;
                //_StatusBtn.BeginGroup = true;
                //_StatusBtn.Caption = "Status:";
                //_StatusBtn.Style = Office.MsoButtonStyle.msoButtonCaption;
                //_StatusBtn.FaceId = 125;
                //_StatusBtn.Tag = Id.ToString().ToString() + "_StatusBtn";
                //_StatusBtn.Enabled = true;
                //_StatusBtn.Click += new Office._CommandBarButtonEvents_ClickEventHandler(
                //    _Status_Btn_Click);

                _StatusComboBox = (Office.CommandBarComboBox)_CommandBar.Controls.Add(
                   Office.MsoControlType.msoControlComboBox, 1, _Missing, 5, 1);
                _StatusComboBox.Caption = "Status";
                _StatusComboBox.Style = Office.MsoComboStyle.msoComboLabel;
                _StatusComboBox.Width = 150;
                _StatusComboBox.Enabled = true;
                fillStatusComboBox();
                _StatusComboBox.Tag = Id.ToString().ToString() + "_StatusComboBox";
                _StatusComboBox.Change += new Office._CommandBarComboBoxEvents_ChangeEventHandler(
                    _Status_Change_Event);

                _SmsBtn = (Office.CommandBarButton)_CommandBar.Controls.Add(
                    Office.MsoControlType.msoControlButton, 1, _Missing, 6, 1);
                _SmsBtn.Width = 30;
                _SmsBtn.Caption = "SMS";
                _SmsBtn.BeginGroup = true;
                _SmsBtn.Style = Office.MsoButtonStyle.msoButtonCaption;
                _SmsBtn.Tag = Id.ToString().ToString() + "_SmsBtn";
                _SmsBtn.Click += new Office._CommandBarButtonEvents_ClickEventHandler(
                    _Sms_Btn_Click);
                _SmsBtn.Visible = true;

                _TBitsBtn = (Office.CommandBarButton)_CommandBar.Controls.Add(
                    Office.MsoControlType.msoControlButton, 1, _Missing, 7, 1);
                _TBitsBtn.Visible = true;
                _TBitsBtn.Caption = "tBits";
                _TBitsBtn.TooltipText = "Link to tBits login page";
                _TBitsBtn.Tag = Id.ToString().ToString() + "tBitsBtn";
                _TBitsBtn.Style = Office.MsoButtonStyle.msoButtonIconAndCaption;
                _TBitsBtn.Click += new Office._CommandBarButtonEvents_ClickEventHandler(
                    _TBits_Btn_Click);
                try
                {
                    Clipboard.Clear();
                    // Read the bitmap from resources
                    System.Drawing.Bitmap bmp = Properties.Resources.logo;
                    // Copy image to clipboard
                    Clipboard.SetImage(bmp);
                    _TBitsBtn.PasteFace();
                }
                catch (Exception exp)
                {
                    MessageBox.Show(owner,"Image loading:" + exp.StackTrace);
                }

                // Make Menu Visible in TOP of Menus
                _CommandBar.Visible = true;
                _CommandBar.Enabled = true;
                _CommandBar.Position = Office.MsoBarPosition.msoBarTop;

                disableTrackButtons();
                setTrackForReplyForward();
            }
            catch (System.Exception ex)
            {
                MessageBox.Show(owner,"StackTrace: \n "+ex.StackTrace+"\n\nMessage: \n"+ex.Message);
            }
        }

        #region Toolbar events

        ///<summary>
        /// The TrackButton Click eventhandler
        /// </summary>
        /// <param name="Ctrl"></param>
        /// <param name="CancelDefault"></param>
        /// 
        void _Track_Btn_Click(Office.CommandBarButton Ctrl, ref bool cancelDefault)
        {
            if (Ctrl.Tag != currentId.ToString())
            {
                //MessageBox.Show("Id's did not match");
                return;
            }

            switch (Ctrl.State)
            {
                case (Office.MsoButtonState.msoButtonUp):
                    {
                        Ctrl.State = Office.MsoButtonState.msoButtonDown;                                   
                        MessageBox.Show(owner,"Track is enabled");
                        enableTrackButtons();
                        break;
                    }
                case (Office.MsoButtonState.msoButtonDown):
                    {
                        Ctrl.State = Office.MsoButtonState.msoButtonUp;
                        disableTrackButtons();                        
                        MessageBox.Show(owner,"Track is disabled");                        
                        break;
                    }
            }
        }

        void _BA_ComboBox_Change(Office.CommandBarComboBox Ctrl)
        {
            try
            {
                //For inspector window
                foreach (BA bizArea in baXmlParser.getBusinessAreas())
                {
                    if (bizArea.displayName == _BAComboBox.Text)
                    {
                        curBA = bizArea;
                        break;
                    }
                }
                //_CategoryComboBox.Clear();
                //fillCategoryComboBox();
                _StatusComboBox.Clear();
                fillStatusComboBox();                
            }
            catch (Exception exp)
            {
                MessageBox.Show(owner,"Exception while changing BA:\n" + exp.StackTrace +
                    "\nMessage:\n" + exp.Message);
            }
        }

        void _Category_Change_Event(Office.CommandBarComboBox categoryCtrl)
        {
            if (categoryCtrl.Tag != Id.ToString() + "_CategoryComboBox") return;
            category = categoryCtrl.Text;
        }

        void _Status_Change_Event(Office.CommandBarComboBox statusCtrl)
        {
            if (statusCtrl.Tag != Id.ToString() + "_StatusComboBox") return;
            status = statusCtrl.Text;
        }

        void _Due_Date_Change_Event(Office.CommandBarComboBox ctrl)
        {
            //msgTextArea.SetFocus();
        }

        void _Due_Date_Btn_Click(Office.CommandBarButton ctrl, ref bool cancelDefault)
        {
            if (ctrl.Tag == Id.ToString() + "_DueDateBtn")
                try
                {                    
                    dp.Size = new System.Drawing.Size(110, 40);
                    dp.Location = new System.Drawing.Point(ctrl.Left, ctrl.Top + ctrl.Height);
                    dp.dateTimePicker1.TextChanged += new EventHandler(_DP_Date_Change_Event);
                    dp.AllowDrop = false;                    
                    dp.ShowDialog(owner);
                    _DueDateText.Visible = true;
                    dp.BringToFront();
                    dp.Activate();
                    dp.dateTimePicker1.BringToFront();
                    dp.dateTimePicker1.Focus();
                }
                catch (Exception exp)
                {
                    MessageBox.Show(owner,"Exception while creating date picker:\n" + exp.StackTrace
                        + "Message:\n" + exp.Message);
                }
        }

        void _DP_Date_Change_Event(Object ctrl, EventArgs evArgs)
        {
            _DueDateText.Text = dp.dateTimePicker1.Text;
            _DueDateText.Visible = true;
        }

        void _Status_Btn_Click(Office.CommandBarButton statusBtnCtrl, ref bool cancelDefault)
        {
            if (statusBtnCtrl.Tag == Id.ToString() + "_StatusBtn")
                if (statusBtnCtrl.State == Office.MsoButtonState.msoButtonUp)
                {
                    statusBtnCtrl.State = Office.MsoButtonState.msoButtonDown;
                    _StatusComboBox.Enabled = true;
                }
                else
                {
                    statusBtnCtrl.State = Office.MsoButtonState.msoButtonUp;
                    _StatusComboBox.Enabled = false;
                }
        }

        void _Category_Btn_Click(Office.CommandBarButton categoryBtnCtrl, ref bool cancelDefault)
        {
            if (categoryBtnCtrl.Tag == Id.ToString() + "_CategoryBtn")
                if (categoryBtnCtrl.State == Office.MsoButtonState.msoButtonUp)
                {
                    categoryBtnCtrl.State = Office.MsoButtonState.msoButtonDown;
                    //_CategoryComboBox.Enabled = true;
                }
                else
                {
                    categoryBtnCtrl.State = Office.MsoButtonState.msoButtonUp;
                    //_CategoryComboBox.Enabled = false;
                }
        }

        void _Sms_Btn_Click(Office.CommandBarButton smsCtrl, ref bool cancelDefault)
        {
            if (smsCtrl.Tag == Id.ToString() + "_SmsBtn")
                if (smsCtrl.State == Office.MsoButtonState.msoButtonUp)
                {
                    smsCtrl.State = Office.MsoButtonState.msoButtonDown;
                    MessageBox.Show(owner,"Send sms option is enabled");
                }
                else
                {
                    smsCtrl.State = Office.MsoButtonState.msoButtonUp;
                }
        }

        void _TBits_Btn_Click(Office.CommandBarButton tBitsCtrl, ref bool cancelDefault)
        {
            try
            {
                tBitsCtrl.TooltipText = baXmlParser.getTBitsWebLink();
                tBitsCtrl.HyperlinkType = Office.MsoCommandBarButtonHyperlinkType.msoCommandBarButtonHyperlinkOpen;
            }
            catch (Exception exp)
            {
                MessageBox.Show(owner,exp.StackTrace + "\n Message: " + exp.Message);
            }
        }

        #endregion

        #region Toolbar supporting functions

        void enableTrackButtons()
        {
            _BAComboBox.Enabled = true;
            _DueDateText.Enabled = true;
            _DueDateBtn.Enabled = true;
            _SmsBtn.Enabled = true;
            //_CategoryComboBox.Enabled = true;
            _StatusComboBox.Enabled = true;
        }

        void disableTrackButtons()
        {
            _BAComboBox.Enabled = false;
            _DueDateText.Enabled = false;
            _DueDateBtn.Enabled = false;
            _SmsBtn.Enabled = false;
            //_CategoryComboBox.Enabled = false;
            _StatusComboBox.Enabled = false;
        }

        void fillBAComboBox()
        {
            bool defValueSet = false;
            int count = startIndex;

            foreach (BA bizArea in baXmlParser.getBusinessAreas())
            {
                try
                {
                    if (bizArea != null)
                    {
                        _BAComboBox.AddItem(bizArea.displayName, count++);
                        if (!defValueSet)
                        {
                            _BAComboBox.Text = bizArea.displayName;
                            curBA = bizArea;
                            defValueSet = true;
                        }
                    }
                    else
                    {
                        return;
                    }
                }
                catch (Exception exp)
                {
                    MessageBox.Show(owner,exp.StackTrace + "Message:" + exp.Message);
                }
            }
        }

        void fillCategoryComboBox()
        {
            try
            {
                foreach (BA bizArea in baXmlParser.getBusinessAreas())
                {
                    if (bizArea.displayName == _BAComboBox.Text)
                    {
                        foreach (string str in bizArea.baCategory)
                            //_CategoryComboBox.AddItem(str, startIndex);
                        break;
                    }
                }
            }
            catch (Exception exp)
            {
                MessageBox.Show(owner,"Exception occured while filling category combobox\n" + exp.StackTrace
                    + "\n\n Message:\n" + exp.Message);
            }
        }

        void fillStatusComboBox()
        {
            try
            {
                foreach (BA bizArea in baXmlParser.getBusinessAreas())
                {
                    if (bizArea.displayName == _BAComboBox.Text)
                    {
                        foreach (string str in bizArea.baStatus)
                            _StatusComboBox.AddItem(str, startIndex);
                        break;
                    }
                }
            }
            catch (Exception exp)
            {
                MessageBox.Show(owner,"Exception occured while filling category combobox\n" + exp.StackTrace
                    + "\n\n Message:\n" + exp.Message);
            }
        }

        #endregion

        #region auxiliary functions

        bool isBA(string mailId)
        {
            foreach (BA tempBA in baXmlParser.ba)
            {
                if (tempBA == null)
                    break;
                if (tempBA.email == mailId)
                {
                    return true;
                }
            }
            return false;
        }

        void setNewCompose(Outlook.MailItem item)
        {
            //Sets the newCompose variable for checking whether the 
            //opened composed window is for new message or for reply/forward
            if (item.To == null)
                //if (item.Subject != null)
                //    newCompose = false;
                //else
                newCompose = true;
            else
                newCompose = false;
        }

        private void setTrackForReplyForward()
        {
            try
            {
                BA matchedBA = null;
                //Outlook.MailItem mItem = (Outlook.MailItem)((Outlook.Inspector)activeObj).CurrentItem;
                //Check if the mail is being replied        
                matchedBA = getBAFromEmail(curMailItem.To);

                //Check if the mail is being forwarded fwPattern 
                if ((curMailItem.Subject != null) && ((curMailItem.Subject).StartsWith("FW")))
                    if (fwPattern.IsMatch(curMailItem.Subject))
                    {
                        matchedBA = getBaFromPrefix(curMailItem.Subject);
                    }
                //if anyone of the above is true, set track controls 
                if (matchedBA != null)
                    setTrackOptions(matchedBA);
            }
            catch (Exception exp)
            {
                MessageBox.Show(owner,"Exception while checking for existing BA" + exp.StackTrace
                    + "\n Message: " + exp.Message);
            }
        }

        private void setTrackOptions(BA matchedBA)
        {
            //Set the values for track controls based on the business area
            _TrackButton.State = Office.MsoButtonState.msoButtonDown;
            enableTrackButtons();
            _BAComboBox.Text = matchedBA.displayName;
            curBA = matchedBA;
        }

        BA getBaFromPrefix(string subject)
        {
            string prefix = null;
            int sIndex = 4;
            prefix = getSubString(subject, sIndex);
            if (prefix != null)
                return getPrefix(prefix);
            else
                return null;
        }

        string getSubString(string subject)
        {
            int defaultStartIndex = 0;
            return getSubString(subject, defaultStartIndex);
        }

        string getSubString(string subject, int startIndex)
        {
            string subString = null;
            int charIndex = subject.IndexOf('#');
            subString = subject.Substring(startIndex, (charIndex - startIndex));
            return subString;
        }

        BA getBAFromEmail(string mailId)
        {
            foreach (BA tempBA in baXmlParser.ba)
            {
                if (tempBA == null)
                    break;
                if (tempBA.email == mailId)
                {
                    return tempBA;
                }
            }
            return null;
        }

        BA getPrefix(string prefix)
        {
            foreach (BA tempBA in baXmlParser.ba)
            {
                if (tempBA == null)
                    break;
                if (tempBA.prefix == prefix)
                {
                    return tempBA;
                }
            }
            return null;
        }

        #endregion
    }    
}
