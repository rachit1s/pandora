using System;
using System.Windows.Forms;
using System.Runtime.InteropServices;
using Microsoft.VisualStudio.Tools.Applications.Runtime;
using Outlook = Microsoft.Office.Interop.Outlook;
using Office = Microsoft.Office.Core;
using System.Resources;
using System.Text.RegularExpressions;
using System.Drawing;
using stdole;

namespace tBitsOutlookPlugin
{
    public partial class ThisApplication
    {
        Outlook.Application curApp;
        Outlook.Explorers explorers;
        Outlook.Inspectors inspectors;

        Office.CommandBarComboBox baComboBox;        
        Office.CommandBarComboBox categoryComboBox;
        Office.CommandBarComboBox statusComboBox;
        Office.CommandBarComboBox dueDateText;

        Office.CommandBarButton dueDateBtn;
        Office.CommandBarButton trackBtn;
        Office.CommandBarButton categoryBtn;
        Office.CommandBarButton statusBtn;
        Office.CommandBarButton tBitsBtn;
        Office.CommandBarButton smsBtn;

        Office.CommandBar newInsToolBar;

        BAXmlParser baXmlParser;
        BA curBA;
        DatePicker dp;
        String curTime = null;

        const int startIndex = 1;
        private string status;
        private string category;

        public string onlineLocation = "http://localhost";
        static string toolBarName = "tBits Toolbar";
        static string companyName = "transbit technologies";
        static string baXmlFileName = "BusinessAreaInfo.xml";
        string baXmlPath = Application.StartupPath + "\\" + companyName + "\\" + baXmlFileName;

        private static bool expWindow = false;
        private static bool newCompose = false;
        Regex fwPattern;
        string curInsId;
        
        private void ThisApplication_Startup(object sender, EventArgs e)
        {
            try
            {
                if (System.IO.File.Exists(baXmlPath))
                {
                    loadBAXml(baXmlPath);
                }
                else
                {
                    throw new System.IO.FileNotFoundException(
                        "Please ensure that xml file containing business area info exists in path:\n" + baXmlPath);
                }

                explorers = this.Explorers;
                curApp = new Outlook.Application();               

                curApp.ItemSend += new Outlook.ApplicationEvents_11_ItemSendEventHandler(Composed_Mail_Send);
                curApp.NewMail+= new Outlook.ApplicationEvents_11_NewMailEventHandler(curApp_NewMail);

                explorers.NewExplorer += new Outlook.ExplorersEvents_NewExplorerEventHandler(new_Explorer_Event);
                                
                inspectors = this.Inspectors;
                inspectors.NewInspector += new Outlook.InspectorsEvents_NewInspectorEventHandler(new_Inspector_Event);
                
                newInsToolBar = null;

                // AddExpToolBar(this.ActiveExplorer(), newExpToolBar);
            }
            catch (Exception exp)
            {
                MessageBox.Show(exp.StackTrace + "\n Message: " + exp.Message);
            }
        }

        void curApp_NewMail()
        {
            try
            {           
                Outlook.MAPIFolder inbox = this.Session.GetDefaultFolder(Outlook.OlDefaultFolders.olFolderInbox);
                Outlook.Items unreadItems = inbox.Items.Restrict("[Unread]=true");                
                                
                for (int i = 1; i <= inbox.Folders.Count; i++)
                {                                
                    Regex tBitsRegex = new Regex("[a-zA-Z]+#[0-9]+:.*");
                    unreadItems = inbox.Folders[i].Items.Restrict("[Unread]=true");
                    foreach (Outlook.MailItem item in unreadItems)
                    {
                        if (tBitsRegex.IsMatch (item.Subject))
                        {
                            item.FlagIcon = Outlook.OlFlagIcon.olBlueFlagIcon;
                            item.Save();
                        }
                    }
                }
            }
            catch (Exception exp)
            {
                MessageBox.Show("Expection in new message event: \n"+ exp.StackTrace
                    +"\n \n Message: \n"+exp.Message);
            }          
        }

        #region Explorer/Inspector events
        private void new_Explorer_Event(Outlook.Explorer new_Explorer)
        {            
            //Right now nothing to do           
        }

        private void new_Inspector_Event(Outlook.Inspector new_Inspector)
        {
            Outlook.MailItem curMail = (Outlook.MailItem)new_Inspector.CurrentItem;
            setNewCompose(curMail);
        
            ((Outlook.InspectorEvents_10_Event)new_Inspector).Close += 
                new Outlook.InspectorEvents_10_CloseEventHandler(Inspector_Close_Event);
                        
            curInsId = new_Inspector.GetHashCode().ToString();
            curTime = DateTime.Now.ToLongTimeString();            
            AddInsToolBar(new_Inspector, newInsToolBar, curTime);
        }

        void setNewCompose(Outlook.MailItem item)
        {
            //Sets the newCompose variable for checking whether the 
            //opened composed window is for new message or for reply/forward
            if (item.To == null)
                if (item.Subject != null) 
                    newCompose = false;
                else
                    newCompose = true;
            else
                newCompose = false;
        }
                    
        #endregion

        #region Add toolbar
        private void AddInsToolBar(Outlook.Inspector inspector, Office.CommandBar insToolBar, string curTime)
        {
            try
            {
                foreach (Office.CommandBar bar in inspector.CommandBars)
                {
                    if (bar.Name == toolBarName)
                    {
                        insToolBar = bar;
                        foreach (Office.CommandBarControl ctrl in insToolBar.Controls)
                        {
                            ctrl.Delete(false);
                        }
                        break;
                    }
                }
            }
            catch (Exception exp)
            {
                MessageBox.Show("Exception in searching for commandbar: " + exp.StackTrace
                    + "\n Message:" + exp.Message);
            }

            // If we found our CommandBar, we can use it
            if (insToolBar == null)
            {
                // if not we create one
                try
                {
                    insToolBar = inspector.CommandBars.Add(toolBarName,
                        missing, missing, true);
                }
                catch (Exception exp)
                {
                    MessageBox.Show("Exception positioning inspector commandbar: " + exp.StackTrace);
                }
            }
            populateToolbar(insToolBar, inspector,curTime);
        }
         
        #endregion

        #region Populate toolbar
        private void populateToolbar(Office.CommandBar newToolBar, Object activeObj,string curTime)
        {
            string activeObjType = activeObj.GetType().Name;

            newToolBar.Visible = true;
            newToolBar.Enabled = true;
            newToolBar.Position = Office.MsoBarPosition.msoBarTop;

            trackBtn = (Office.CommandBarButton)newToolBar.Controls.Add(
            Office.MsoControlType.msoControlButton, 1, missing, 1, 1);
            trackBtn.Width = 40;
            trackBtn.Caption = "Track";
            trackBtn.Style = Office.MsoButtonStyle.msoButtonCaption;
            trackBtn.Click += new Office._CommandBarButtonEvents_ClickEventHandler(
                Track_Btn_Click);
            trackBtn.Visible = true;
            trackBtn.Tag = activeObj.GetHashCode().ToString() + "trackBtn";
    
            //Combobox to select select business area
            baComboBox = (Office.CommandBarComboBox)newToolBar.Controls.Add(
                Office.MsoControlType.msoControlComboBox, 1, missing, 2, 1);
            baComboBox.BeginGroup = true;
            baComboBox.Caption = "BA:";
            baComboBox.Style = Office.MsoComboStyle.msoComboLabel;
            baComboBox.Width = 150;
            baComboBox.Tag = activeObj.GetHashCode().ToString() + "baComboBox";
            fillBAComboBox(baComboBox);
            baComboBox.Change += new Office._CommandBarComboBoxEvents_ChangeEventHandler(
                BA_Change_Event);                    
           
            //Button to set due date
            dueDateText = (Office.CommandBarComboBox)newToolBar.Controls.Add(
                Office.MsoControlType.msoControlEdit, 1, missing, 3, 1);
            dueDateText.BeginGroup = true;
            dueDateText.Width = 115;
            dueDateText.Caption = "Due date:";
            dueDateText.Style = Office.MsoComboStyle.msoComboLabel;
            if (newCompose)
                dueDateText.Text = DateTime.Today.AddDays(2).ToShortDateString();
            else
                dueDateText.Text = null;
            dueDateText.Visible = true;
            dueDateText.Tag = activeObj.GetHashCode().ToString() + "dueDateText";
            dueDateText.Change += new Office._CommandBarComboBoxEvents_ChangeEventHandler(
                Due_Date_Change_Event);

            dueDateBtn = (Office.CommandBarButton)newToolBar.Controls.Add(
                Office.MsoControlType.msoControlButton, 1, missing, 4, 1);
            dueDateBtn.Width = 10;
            dueDateBtn.Style = Office.MsoButtonStyle.msoButtonIcon;
            dueDateBtn.FaceId = 125;
            dueDateBtn.Tag = activeObj.GetHashCode().ToString() + "dueDateBtn";
            dueDateBtn.Click += new Office._CommandBarButtonEvents_ClickEventHandler(
                Due_Date_Btn_Click);

            dp = new DatePicker();
            dp.Visible = false;

            categoryBtn = (Office.CommandBarButton)newToolBar.Controls.Add(
               Office.MsoControlType.msoControlButton, 1, missing, 5, 1);
            categoryBtn.Width = 50;
            categoryBtn.BeginGroup = true;
            categoryBtn.Caption = "Category:";
            categoryBtn.Style = Office.MsoButtonStyle.msoButtonCaption;
            categoryBtn.FaceId = 125;
            categoryBtn.Tag = activeObj.GetHashCode().ToString() + "categoryBtn";
            categoryBtn.Enabled = true;
            categoryBtn.Click += new Office._CommandBarButtonEvents_ClickEventHandler(
                Category_Btn_Click);

            categoryComboBox = (Office.CommandBarComboBox)newToolBar.Controls.Add(
               Office.MsoControlType.msoControlComboBox, 1, missing, 6, 1);
            categoryComboBox.Caption = "Category";
            categoryComboBox.Style = Office.MsoComboStyle.msoComboNormal;
            categoryComboBox.Width = 100;
            categoryComboBox.Enabled = false;
            fillCategoryComboBox(categoryComboBox);
            categoryComboBox.Tag = activeObj.GetHashCode().ToString() + "categoryComboBox";
            categoryComboBox.Change += new Office._CommandBarComboBoxEvents_ChangeEventHandler(
                Category_Change_Event);

            statusBtn = (Office.CommandBarButton)newToolBar.Controls.Add(
                Office.MsoControlType.msoControlButton, 1, missing, 7, 1);
            statusBtn.Width = 50;
            statusBtn.BeginGroup = true;
            statusBtn.Caption = "Status:";
            statusBtn.Style = Office.MsoButtonStyle.msoButtonCaption;
            statusBtn.FaceId = 125;
            statusBtn.Tag = activeObj.GetHashCode().ToString() + "statusBtn";
            statusBtn.Enabled = true;
            statusBtn.Click += new Office._CommandBarButtonEvents_ClickEventHandler(
                Status_Btn_Click);

            statusComboBox = (Office.CommandBarComboBox)newToolBar.Controls.Add(
               Office.MsoControlType.msoControlComboBox, 1, missing, 8, 1);
            statusComboBox.Caption = "Status";
            statusComboBox.Style = Office.MsoComboStyle.msoComboNormal;
            statusComboBox.Width = 100;
            statusComboBox.Enabled = false;
            fillStatusComboBox(statusComboBox);
            statusComboBox.Tag = activeObj.GetHashCode().ToString() + "statusComboBox";
            statusComboBox.Change += new Office._CommandBarComboBoxEvents_ChangeEventHandler(
                Status_Change_Event);           

            smsBtn = (Office.CommandBarButton)newToolBar.Controls.Add(
                Office.MsoControlType.msoControlButton, 1, missing, 9,1);
            smsBtn.Width = 30;
            smsBtn.Caption = "SMS";
            smsBtn.Style = Office.MsoButtonStyle.msoButtonCaption;
            smsBtn.Tag = activeObj.GetHashCode().ToString() + "smsBtn";
            smsBtn.Click += new Office._CommandBarButtonEvents_ClickEventHandler(
                Sms_Btn_Click);
            smsBtn.Visible = true;

            tBitsBtn = (Office.CommandBarButton)newToolBar.Controls.Add(
                Office.MsoControlType.msoControlButton, 1, missing, 10, 1);
            tBitsBtn.Visible = true;
            tBitsBtn.Caption = "tBits";
            tBitsBtn.TooltipText = "Link to tBits login page";
            tBitsBtn.Tag = activeObj.GetHashCode().ToString() + "tBitsBtn";
            tBitsBtn.Style = Office.MsoButtonStyle.msoButtonIconAndCaption;
            tBitsBtn.Click += new Office._CommandBarButtonEvents_ClickEventHandler(
                TBits_Btn_Click);

            try
            {                   
                Clipboard.Clear();
                // Read the bitmap from resources
                Bitmap bmp = Properties.Resources.logo;
                // Copy image to clipboard
                Clipboard.SetImage(bmp);
                tBitsBtn.PasteFace();
            }
            catch (Exception exp)
            {
                MessageBox.Show("Image loading:" + exp.StackTrace);
            }

            disableTrackButtons();
            setTrackForReplyForward(activeObj);            
        }

        void setTrackForReplyForward(Object activeObj) 
        {
            try
            {
                BA matchedBA = null;
                Outlook.MailItem mItem = (Outlook.MailItem)((Outlook.Inspector)activeObj).CurrentItem;
                //Check if the mail is being replied        
                matchedBA = getBAFromEmail(mItem.To);

                //Check if the mail is being forwarded        
                fwPattern = new Regex("FW: +[a-zA-Z]+#[0-9]+:.*");       
                if ((mItem.Subject != null) && ((mItem.Subject).StartsWith("FW")))
                    if (fwPattern.IsMatch(mItem.Subject))
                    {
                        matchedBA = getBaFromPrefix(mItem.Subject);                       
                    }
                //if anyone of the above is true, set track controls 
                if (matchedBA != null)
                    setTrackOptions(matchedBA);                
            }
            catch (Exception exp)
            {
                MessageBox.Show("Exception while checking for existing BA" + exp.StackTrace
                    + "\n Message: " + exp.Message);
            } 
        }
                
        void setTrackOptions(BA matchedBA)
        {
            //Set the values for track controls based on the business area
            trackBtn.State = Office.MsoButtonState.msoButtonDown;
            enableTrackButtons();
            baComboBox.Text = matchedBA.displayName;
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
            subString=subject.Substring(startIndex, (charIndex - startIndex));
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
        
        void Inspector_Reply_Event(Object obj, ref bool Cancel)
        {
            MessageBox.Show("Reply mail event");
            newCompose = false;
        }

        void Inspector_Forward_Event(Object obj, ref bool Cancel)
        {
            MessageBox.Show("Forward mail event");
            newCompose = false;
        }

        void Inspector_Close_Event()
        {
            newCompose = false;
        }       

        bool isBA (string mailId)
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
        
        void loadBAXml(String xmlFilePath)
        {
            try
            {
                baXmlParser = BAXmlParser.GetInstance();
                baXmlParser.getXmlValues(xmlFilePath);
                baXmlParser.setBusinessAreas();
                baXmlParser.setTBitsUsers();
            }
            catch (Exception exp)
            {
                MessageBox.Show("Exception while loading xml:\n" +
                    exp.StackTrace + "Message:" + exp.Message);
            }
        }
                
        #region Toolbar button events
        void Track_Btn_Click(Office.CommandBarButton trackCtrl, ref bool cancelDefault)
        {           
            if (trackCtrl.Tag.Equals(curInsId + "trackBtn"))
            {
                switch (trackCtrl.State)
                {
                    case (Office.MsoButtonState.msoButtonUp):
                        {
                            trackBtn.State = Office.MsoButtonState.msoButtonDown;
                            enableTrackButtons();
                            break;
                        }
                    case (Office.MsoButtonState.msoButtonDown):
                        {
                            trackBtn.State = Office.MsoButtonState.msoButtonUp;
                            disableTrackButtons();
                            MessageBox.Show("Track is disabled");
                            break;
                        }
                }
            }
        }

        void enableTrackButtons()
        {
            baComboBox.Enabled = true;
            dueDateText.Enabled = true;
            dueDateBtn.Enabled = true;
            smsBtn.Enabled = true;
            categoryBtn.Enabled = true;
            statusBtn.Enabled = true;
        }

        void disableTrackButtons()
        {
            baComboBox.Enabled = false;
            dueDateText.Enabled = false;
            dueDateBtn.Enabled = false;
            smsBtn.Enabled = false;
            categoryBtn.Enabled = false;
            statusBtn.Enabled = false;
        }

        void Status_Btn_Click(Office.CommandBarButton statusBtnCtrl, ref bool cancelDefault)
        {
            if (statusBtnCtrl.Tag == curInsId + "statusBtn")
                if (statusBtnCtrl.State == Office.MsoButtonState.msoButtonUp)
                {
                    statusBtnCtrl.State = Office.MsoButtonState.msoButtonDown;
                    statusComboBox.Enabled = true;
                }
                else
                {
                    statusBtnCtrl.State = Office.MsoButtonState.msoButtonUp;
                    statusComboBox.Enabled = false;
                }
        }

        void Category_Btn_Click(Office.CommandBarButton categoryBtnCtrl, ref bool cancelDefault)
        {
            if (categoryBtnCtrl.Tag == curInsId + "categoryBtn")
                if (categoryBtnCtrl.State == Office.MsoButtonState.msoButtonUp)
                {
                    categoryBtnCtrl.State = Office.MsoButtonState.msoButtonDown;
                    categoryComboBox.Enabled = true;
                }
                else
                {
                    categoryBtnCtrl.State = Office.MsoButtonState.msoButtonUp;
                    categoryComboBox.Enabled = false;
                }
        }

        void Sms_Btn_Click(Office.CommandBarButton smsCtrl, ref bool cancelDefault)
        {
            if (smsCtrl.Tag == curInsId + "smsBtn")
                if (smsCtrl.State == Office.MsoButtonState.msoButtonUp)
                {
                    smsCtrl.State = Office.MsoButtonState.msoButtonDown;
                    MessageBox.Show("Send sms option is enabled");
                }
                else
                {
                    smsCtrl.State = Office.MsoButtonState.msoButtonUp;
                }
        }

        void TBits_Btn_Click(Office.CommandBarButton tBitsCtrl, ref bool cancelDefault)
        {
            try
            {
                tBitsCtrl.TooltipText = onlineLocation;
                tBitsCtrl.HyperlinkType = Office.MsoCommandBarButtonHyperlinkType.msoCommandBarButtonHyperlinkOpen;
            }
            catch (Exception exp)
            {
                MessageBox.Show(exp.StackTrace + "\n Message: " + exp.Message);
            }
        }

        private void Composed_Mail_Send(Object obj, ref bool boolVar)
        {
            string assigneeStr = null;
            string trackStr = null;
            Outlook.MailItem curItem = null;
            if (expWindow)
                return;

            if (trackBtn.State == Office.MsoButtonState.msoButtonDown)
            {
                try
                {
                    curItem = (Outlook.MailItem)this.ActiveInspector().CurrentItem;
                    assigneeStr = curItem.To;
                    curItem.RecipientReassignmentProhibited = false;
                    curItem.To = curBA.email;                                                
                    curItem.Recipients.ResolveAll();

                    MessageBox.Show("Tracking Enabled. Mail will be redirected through tBits as shown below:\n"
                        + "user -> " + curItem.To + " -> " + assigneeStr);

                    //Don't set assignee string if it a reply to tbits mail
                    if (isBA(assigneeStr))
                        assigneeStr = "";
                    else
                    {
                        trackStr = "/assignee:" + assigneeStr + Environment.NewLine;
                    }

                    if (dueDateText.Text == "")                        
                        if (newCompose)
                            trackStr = trackStr + "/dueby" + DateTime.Today.AddDays(2).ToShortDateString() 
                                + Environment.NewLine;
                        else
                            trackStr = trackStr + Environment.NewLine;
                    else
                        trackStr = trackStr + "/dueby:" + dueDateText.Text + Environment.NewLine;

                    if ((categoryBtn.State == Office.MsoButtonState.msoButtonDown) &&
                        (category != null))
                        trackStr = trackStr + "/category_id:" + category + Environment.NewLine;

                    if ((statusBtn.State == Office.MsoButtonState.msoButtonDown) &&
                        (status != null))
                        trackStr = trackStr + "/status:" + status + Environment.NewLine;

                    if (smsBtn.State == Office.MsoButtonState.msoButtonDown)
                        trackStr = trackStr + "/SendSMS:" + true + Environment.NewLine;

                    curItem.Body = trackStr + curItem.Body;
                }
                catch (Exception exp)
                {
                    MessageBox.Show(exp.StackTrace + "\n" + "Message:" + exp.Message);
                }
            }
        }
        
        private DialogResult confirmTrack(string str)
        {
            DialogResult trackResult = MessageBox.Show(str, "Tracking message to tbits", MessageBoxButtons.OKCancel);
            return trackResult;
        }

        void Due_Date_Change_Event(Office.CommandBarComboBox ctrl)
        {
            //msgTextArea.SetFocus();
        }

        void Due_Date_Btn_Click(Office.CommandBarButton ctrl, ref bool cancelDefault)
        {
            if (ctrl.Tag == curInsId + "dueDateBtn")
                try
                {                   
                    dp.Size = new System.Drawing.Size(110, 40);
                    dp.Location = new System.Drawing.Point(ctrl.Left, ctrl.Top + ctrl.Height);
                    dp.dateTimePicker1.TextChanged += new EventHandler(DP_Date_Change_Event);
                    dp.AllowDrop = false;
                    dp.ShowDialog();
                    dueDateText.Visible = true;                   
                    dp.BringToFront();
                    dp.Activate(); 
                    dp.dateTimePicker1.BringToFront();
                    dp.dateTimePicker1.Focus();                                      
                }
                catch (Exception exp)
                {
                    MessageBox.Show("Exception while creating date picker:\n" + exp.StackTrace
                        + "Message:\n" + exp.Message);
                }
        }

        void DP_Date_Change_Event(Object ctrl, EventArgs evArgs)
        {
            dueDateText.Text = dp.dateTimePicker1.Text;
            dueDateText.Visible = true;
        }
       
        #endregion

        #region Toolbar combobox events
        void BA_Change_Event(Office.CommandBarComboBox baComboBox)
        {
            try
            {
                if (baComboBox.Tag == curInsId + "baComboBox")
                {
                    //For inspector window
                    foreach (BA bizArea in baXmlParser.getBusinessAreas())
                    {
                        if (bizArea.displayName == baComboBox.Text)
                        {
                            curBA = bizArea;
                            break;
                        }
                    }
                    categoryComboBox.Clear();
                    fillCategoryComboBox(categoryComboBox);
                    statusComboBox.Clear();
                    fillStatusComboBox(statusComboBox);
                }
            }
            catch (Exception exp)
            {
                MessageBox.Show("Exception while changing BA:\n" + exp.StackTrace +
                    "\nMessage:\n" + exp.Message);
            }
        }     

        void Category_Change_Event(Office.CommandBarComboBox categoryCtrl)
        {
            if (categoryCtrl.Tag != curInsId + "categoryBtn") return;
            category = categoryCtrl.Text;
        }

        void Status_Change_Event(Office.CommandBarComboBox statusCtrl)
        {
            if (statusCtrl.Tag != curInsId + "statusComboBox") return;
            status = statusCtrl.Text;
        }

        void fillBAComboBox(Office.CommandBarComboBox baComboBox)
        {
            bool defValueSet = false;
            int count = startIndex;
            foreach (BA bizArea in baXmlParser.getBusinessAreas())
            {
                try
                {
                    if (bizArea != null)
                    {
                        baComboBox.AddItem(bizArea.displayName, count++);
                        if (!defValueSet)
                        {
                            baComboBox.Text = bizArea.displayName;
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
                    MessageBox.Show(exp.StackTrace + "Message:" + exp.Message);
                }
            }
        }
       
        void fillCategoryComboBox(Office.CommandBarComboBox categoryComboBox)
        {
            try
            {
                foreach (BA bizArea in baXmlParser.getBusinessAreas())
                {
                    if (bizArea.displayName == baComboBox.Text)
                    {
                        foreach (string str in bizArea.baCategory)
                            categoryComboBox.AddItem(str, startIndex);
                        break;
                    }
                }
            }
            catch (Exception exp)
            {
                MessageBox.Show("Exception occured while filling category combobox\n" + exp.StackTrace
                    + "\n\n Message:\n" + exp.Message);
            }
        }

        void fillStatusComboBox(Office.CommandBarComboBox statusComboBox)
        {
            try
            {
                foreach (BA bizArea in baXmlParser.getBusinessAreas())
                {
                    if (bizArea.displayName == baComboBox.Text)
                    {
                        foreach (string str in bizArea.baStatus)
                            statusComboBox.AddItem(str, startIndex);
                        break;
                    }
                }
            }
            catch (Exception exp)
            {
                MessageBox.Show("Exception occured while filling category combobox\n" + exp.StackTrace
                    + "\n\n Message:\n" + exp.Message);
            }
        }

        #endregion

        #region VSTO Designer generated code
        private void InternalStartup()
        {
            this.Startup += new System.EventHandler(ThisApplication_Startup);
            this.Shutdown += new System.EventHandler(ThisApplication_Shutdown);
        }
        #endregion

        private void ThisApplication_Shutdown(object sender, System.EventArgs e)
        {
        }
    }

    public class MyHost : AxHost
    {
        public MyHost()
            : base("59EE46BA-677D-4d20-BF10-8D8067CB8B33")
        {
        }
        public static stdole.IPictureDisp GettIPictureDispFromPicture(Image image)
        {
            return (stdole.IPictureDisp)AxHost.GetIPictureDispFromPicture(image);
        }
    }
}
