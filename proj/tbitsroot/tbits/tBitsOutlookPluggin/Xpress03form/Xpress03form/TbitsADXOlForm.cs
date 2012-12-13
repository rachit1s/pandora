using System;
using System.Windows.Forms;
using System.Drawing;
using System.Runtime.InteropServices;
using Outlook = Microsoft.Office.Interop.Outlook;
using System.Text.RegularExpressions;
using System.IO;
 
namespace Xpress03form
{
    /// <summary>
    /// Summary description for TbitsADXOlForm.
    /// </summary>

    public class TbitsADXOlForm : AddinExpress.OL.ADXOlForm
    {
        // Create a logger for use in this class
        private static readonly log4net.ILog log = log4net.LogManager.GetLogger("Xpress03form.ThisAddIn");

        private CheckBox trackCheckBox;
        private DateTimePicker dueDatePicker;
        private ComboBox categoryComboBox;
        private ComboBox statusComboBox;
        private CheckBox smsCheckBox;
        private LinkLabel tBitsLink;
        private Label dueDateLabel;
        private ComboBox baComboBox;             
        private Label status;
        private Label category;
        private Label baLabel;
        private Button refreshButton;
        private System.ComponentModel.IContainer components = null;   

        Outlook.Inspector inspector;
        Outlook.MailItem curItem;

        private BAXmlParser baXmlParser = BAXmlParser.GetInstance();
        BA curBA = null;
        static string prevBA = String.Empty;
        bool newCompose = false;
        string iucString;

        Regex fwPattern = new Regex("FW: +[a-zA-Z]+#[0-9]+:.*");
        private string statusStr = String.Empty;
        private string categoryStr = String.Empty;
        private bool sendSms = false;
        string assigneeStr = null;
        string subscriberStr = null;        
        string trackStr = null;

        //IUCs
        string ASSIGNEE_IUC = "";

        public TbitsADXOlForm()
 	     {
            // This call is required by the Windows Form Designer.
            InitializeComponent();
 
            // TODO: Add any initialization after the InitializeComponent call 
            //MessageBox.Show("app path:\n" + AppDomain.CurrentDomain.BaseDirectory);             
        }
 
        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        protected override void Dispose( bool disposing )
        {
            if( disposing )
            {
                if(components != null)
                {
                    components.Dispose();
                }
            }
            base.Dispose( disposing );
        }
                 
        #region Designer generated code
        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(TbitsADXOlForm));
            this.trackCheckBox = new System.Windows.Forms.CheckBox();
            this.dueDatePicker = new System.Windows.Forms.DateTimePicker();
            this.categoryComboBox = new System.Windows.Forms.ComboBox();
            this.statusComboBox = new System.Windows.Forms.ComboBox();
            this.smsCheckBox = new System.Windows.Forms.CheckBox();
            this.tBitsLink = new System.Windows.Forms.LinkLabel();
            this.dueDateLabel = new System.Windows.Forms.Label();
            this.baComboBox = new System.Windows.Forms.ComboBox();
            this.status = new System.Windows.Forms.Label();
            this.category = new System.Windows.Forms.Label();
            this.baLabel = new System.Windows.Forms.Label();
            this.refreshButton = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // trackCheckBox
            // 
            this.trackCheckBox.AutoSize = true;
            this.trackCheckBox.Location = new System.Drawing.Point(3, 5);
            this.trackCheckBox.Name = "trackCheckBox";
            this.trackCheckBox.Size = new System.Drawing.Size(54, 17);
            this.trackCheckBox.TabIndex = 0;
            this.trackCheckBox.Text = "Track";
            this.trackCheckBox.UseVisualStyleBackColor = true;
            this.trackCheckBox.CheckedChanged += new System.EventHandler(this.trackCheckBox_CheckedChanged);
            // 
            // dueDatePicker
            // 
            this.dueDatePicker.Checked = false;
            this.dueDatePicker.CustomFormat = "dd-MMM-yy";
            this.dueDatePicker.Enabled = false;
            this.dueDatePicker.Format = System.Windows.Forms.DateTimePickerFormat.Custom;
            this.dueDatePicker.Location = new System.Drawing.Point(268, 2);
            this.dueDatePicker.Name = "dueDatePicker";
            this.dueDatePicker.ShowCheckBox = true;
            this.dueDatePicker.Size = new System.Drawing.Size(100, 20);
            this.dueDatePicker.TabIndex = 2;
            this.dueDatePicker.ValueChanged += new System.EventHandler(this.dueDatePicker_ValueChanged);
            // 
            // categoryComboBox
            // 
            this.categoryComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.categoryComboBox.Enabled = false;
            this.categoryComboBox.FormattingEnabled = true;
            this.categoryComboBox.Location = new System.Drawing.Point(428, 2);
            this.categoryComboBox.Name = "categoryComboBox";
            this.categoryComboBox.Size = new System.Drawing.Size(121, 21);
            this.categoryComboBox.TabIndex = 3;
            this.categoryComboBox.SelectedIndexChanged += new System.EventHandler(this.categoryComboBox_SelectedIndexChanged);
            // 
            // statusComboBox
            // 
            this.statusComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.statusComboBox.Enabled = false;
            this.statusComboBox.FormattingEnabled = true;
            this.statusComboBox.Location = new System.Drawing.Point(596, 2);
            this.statusComboBox.Name = "statusComboBox";
            this.statusComboBox.Size = new System.Drawing.Size(121, 21);
            this.statusComboBox.TabIndex = 4;
            this.statusComboBox.SelectedIndexChanged += new System.EventHandler(this.statusComboBox_SelectedIndexChanged);
            // 
            // smsCheckBox
            // 
            this.smsCheckBox.AutoSize = true;
            this.smsCheckBox.Enabled = false;
            this.smsCheckBox.Location = new System.Drawing.Point(724, 5);
            this.smsCheckBox.Name = "smsCheckBox";
            this.smsCheckBox.Size = new System.Drawing.Size(49, 17);
            this.smsCheckBox.TabIndex = 5;
            this.smsCheckBox.Text = "SMS";
            this.smsCheckBox.UseVisualStyleBackColor = true;
            this.smsCheckBox.CheckedChanged += new System.EventHandler(this.smsCheckBox_CheckedChanged);
            // 
            // tBitsLink
            // 
            this.tBitsLink.AutoSize = true;
            this.tBitsLink.ImageAlign = System.Drawing.ContentAlignment.MiddleLeft;
            this.tBitsLink.Location = new System.Drawing.Point(809, 6);
            this.tBitsLink.Name = "tBitsLink";
            this.tBitsLink.Size = new System.Drawing.Size(53, 13);
            this.tBitsLink.TabIndex = 6;
            this.tBitsLink.TabStop = true;
            this.tBitsLink.Tag = "";
            this.tBitsLink.Text = "Web tBits";
            this.tBitsLink.LinkClicked += new System.Windows.Forms.LinkLabelLinkClickedEventHandler(this.linkLabel1_LinkClicked);
            // 
            // dueDateLabel
            // 
            this.dueDateLabel.AutoSize = true;
            this.dueDateLabel.Location = new System.Drawing.Point(208, 6);
            this.dueDateLabel.Name = "dueDateLabel";
            this.dueDateLabel.Size = new System.Drawing.Size(59, 13);
            this.dueDateLabel.TabIndex = 7;
            this.dueDateLabel.Text = "Due Date :";
            this.dueDateLabel.Click += new System.EventHandler(this.label1_Click);
            // 
            // baComboBox
            // 
            this.baComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.baComboBox.Enabled = false;
            this.baComboBox.FormattingEnabled = true;
            this.baComboBox.Location = new System.Drawing.Point(83, 2);
            this.baComboBox.Name = "baComboBox";
            this.baComboBox.Size = new System.Drawing.Size(121, 21);
            this.baComboBox.TabIndex = 8;
            this.baComboBox.SelectedIndexChanged += new System.EventHandler(this.comboBox1_SelectedIndexChanged);
            // 
            // status
            // 
            this.status.AutoSize = true;
            this.status.Location = new System.Drawing.Point(552, 6);
            this.status.Name = "status";
            this.status.Size = new System.Drawing.Size(43, 13);
            this.status.TabIndex = 9;
            this.status.Text = "Status :";
            this.status.Click += new System.EventHandler(this.label1_Click_1);
            // 
            // category
            // 
            this.category.AutoSize = true;
            this.category.Location = new System.Drawing.Point(371, 6);
            this.category.Name = "category";
            this.category.Size = new System.Drawing.Size(55, 13);
            this.category.TabIndex = 10;
            this.category.Text = "Category :";
            // 
            // baLabel
            // 
            this.baLabel.AutoSize = true;
            this.baLabel.Location = new System.Drawing.Point(56, 6);
            this.baLabel.Name = "baLabel";
            this.baLabel.Size = new System.Drawing.Size(27, 13);
            this.baLabel.TabIndex = 11;
            this.baLabel.Text = "BA :";
            // 
            // refreshButton
            // 
            this.refreshButton.BackgroundImage = ((System.Drawing.Image)(resources.GetObject("refreshButton.BackgroundImage")));
            this.refreshButton.BackgroundImageLayout = System.Windows.Forms.ImageLayout.Center;
            this.refreshButton.FlatStyle = System.Windows.Forms.FlatStyle.Popup;
            this.refreshButton.Location = new System.Drawing.Point(771, 2);
            this.refreshButton.Name = "refreshButton";
            this.refreshButton.Size = new System.Drawing.Size(24, 21);
            this.refreshButton.TabIndex = 12;
            this.refreshButton.UseVisualStyleBackColor = false;
            this.refreshButton.Click += new System.EventHandler(this.refreshButton_Click);
            // 
            // TbitsADXOlForm
            // 
            this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
            this.BackgroundImageLayout = System.Windows.Forms.ImageLayout.Zoom;
            this.ClientSize = new System.Drawing.Size(867, 25);
            this.Controls.Add(this.refreshButton);
            this.Controls.Add(this.baLabel);
            this.Controls.Add(this.category);
            this.Controls.Add(this.status);
            this.Controls.Add(this.baComboBox);
            this.Controls.Add(this.dueDateLabel);
            this.Controls.Add(this.tBitsLink);
            this.Controls.Add(this.smsCheckBox);
            this.Controls.Add(this.statusComboBox);
            this.Controls.Add(this.categoryComboBox);
            this.Controls.Add(this.dueDatePicker);
            this.Controls.Add(this.trackCheckBox);
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.Name = "TbitsADXOlForm";
            this.Text = "TbitsADXOlForm";
            this.Load += new System.EventHandler(this.TbitsADXOlForm_Load);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        void refreshButton_Click(object sender, EventArgs e)
        {
            
            BAXMLRetriever retriever = new BAXMLRetriever(AppDomain.CurrentDomain.SetupInformation.ApplicationBase);
            retriever.loadXmlFromUrl();
            fillXmlInfo();
            
            //StreamReader reader = new StreamReader(Properties.Settings.Default.tempFile);
            //StreamWriter writer = File.CreateText(Properties.Settings.Default.localXml);
            //while (!reader.EndOfStream)
            //{
            //    writer.WriteLine(reader.ReadLine());
            //    writer.Flush();
            //}
            //writer.Dispose();
            //reader.Dispose();            
        }
        #endregion     

        private void TbitsADXOlForm_Load(object sender, EventArgs e)
        {
            try
            {
                string curSubject = null;
                iucString = String.Empty;
                inspector = (Outlook.Inspector)InspectorObj;
                curItem = (Outlook.MailItem)inspector.CurrentItem;
                fillXmlInfo();
                setNewCompose();

                //Check if the current message is new/reply/forward
                if (curItem.Subject == null)
                    curSubject = String.Empty;
                else
                    curSubject = curItem.Subject;

                if ((newCompose) && (!fwPattern.IsMatch(curSubject)))
                {
                    this.dueDatePicker.Text = DateTime.Today.AddDays(Properties.Settings.Default.dueDateCount).ToShortDateString();
                    if (Properties.Settings.Default.checkDueDate)
                        this.dueDatePicker.Checked = true;
                    else
                        this.dueDatePicker.Checked = false;
                }
                else
                    this.dueDatePicker.Checked = false;

                //Check and set track button based on new/reply/forward message
                setTrackForReplyForward();
                ((Outlook.ItemEvents_10_Event)curItem).Send += new Outlook.ItemEvents_10_SendEventHandler(TbitsADXOlForm_Send);
            }
            catch (Exception exp) 
            {
                MessageBox.Show (exp.StackTrace);
            }
        }

        private void setTrackForReplyForward()
        {
            try
            {
                BA matchedBA = null;
                //Check if the mail is being replied        
                matchedBA = getBAFromEmail(curItem.To);

                //Check if the mail is being forwarded fwPattern 
                if ((curItem.Subject != null) && ((curItem.Subject).StartsWith("FW")))
                    if (fwPattern.IsMatch(curItem.Subject))
                    {
                        matchedBA = getBaFromPrefix(curItem.Subject);
                    }
                //if anyone of the above is true, set track controls 
                if (matchedBA != null)
                {
                    setTrackOptions(matchedBA);
                    //this.refreshButton.Hide() ;
                }
            }
            catch (Exception exp)
            {
                log.Error("Exception while checking for existing BA" + exp.StackTrace
                    + "\n Message: " + exp.Message, exp);
            }
        }

        private void setTrackOptions(BA matchedBA)
        {
            //Set the values for track controls based on the business area
            this.trackCheckBox.Checked = true;
            enableControls();
            baComboBox.Text = matchedBA.displayName;
            curBA = matchedBA;
        }

        private void trackCheckBox_CheckedChanged(object sender, EventArgs e)
        {
            if (this.trackCheckBox.Checked)
                enableControls();   
            else
                disableControls();
        }

        void TbitsADXOlForm_Send(ref bool cancel)
        {
            string rEmail = String.Empty;
            if (this.trackCheckBox.Checked)
            {
                try
                {
                    if (curItem.Recipients.ResolveAll())
                    {
                        try
                        {
                            object currentItem = null;
                            string email = String.Empty;
                            Outlook.MailItem mail = null;
                            Outlook._NameSpace ns = null;
                            Outlook.Recipient recpt = null;
                            IntPtr sessionPtr = IntPtr.Zero;
                            Outlook._Inspector inspector = null;
                            try
                            {
                                bool firstAssignee = true;
                                bool firstSub = true;
                                Object hostApp = TbitsADXModule.CurrentInstance.HostApplication;
                                inspector = ((Outlook._Application)hostApp).ActiveInspector();
                                currentItem = inspector.CurrentItem;
                                mail = currentItem as Outlook.MailItem;

                                if (mail == null) return;

                                mail.Save();

                                if (mail.Recipients.Count == 0) return;

                                int rCount = mail.Recipients.Count;
                                for (int i = rCount; i > 0; i--)
                                {                                    
                                    email = mail.Recipients[i].Address;
                                    email = getUserEmailUsingRegex(email);

                                    if ((Outlook.OlMailRecipientType)mail.Recipients[i].Type == Outlook.OlMailRecipientType.olTo)
                                    {
                                        if (firstAssignee)
                                        {
                                            assigneeStr = email;
                                            firstAssignee = false;
                                        }
                                        else
                                            assigneeStr = assigneeStr + "," + email;
                                    }
                                    else if ((Outlook.OlMailRecipientType)mail.Recipients[i].Type == Outlook.OlMailRecipientType.olCC)
                                    {
                                        if (firstSub)
                                        {
                                            subscriberStr = email;
                                            firstSub = false;
                                        }
                                        else
                                            subscriberStr = subscriberStr + "," + email;
                                    }
                                }
                            }
                            finally
                            {
                                if (sessionPtr != IntPtr.Zero)
                                    Marshal.Release(sessionPtr);
                                if (ns != null)
                                    Marshal.ReleaseComObject(ns);
                                if (recpt != null)
                                    Marshal.ReleaseComObject(recpt);
                                if (currentItem != null)
                                    Marshal.ReleaseComObject(currentItem);
                                if (inspector != null)
                                    Marshal.ReleaseComObject(inspector);
                            }
                        }
                        catch (Exception err)
                        {
                            log.Error("Error occurred while reading to and cc fields to collect assingees and subscribers", err);
                        }
                       
                        //assigneeStr = curItem.To;
                        //assigneeStr.Replace(';', ',');
                        curItem.RecipientReassignmentProhibited = false;
                        curItem.To = curBA.email;
                        if (curItem.CC != null)
                        {
                            //subscriberStr = curItem.CC;
                            curItem.CC = String.Empty;
                        }
                    }                                 

                    curItem.Recipients.ResolveAll();

                    //Don't set assignee string if it a reply to tbits mail
                    if (isBA(assigneeStr))
                        assigneeStr = String.Empty;
                    else
                        trackStr = "/assignee:" + assigneeStr + "\n";

                    if (subscriberStr != null)
                        trackStr = trackStr + "/subscribers:" + subscriberStr + "\n";
                                        
                    if (this.dueDatePicker.Checked)
                        trackStr = trackStr + "/dueby:" + this.dueDatePicker.Text + "\n";
                    else
                        trackStr = trackStr + "\n";

                    if ((this.categoryStr != "None") && (this.categoryStr!= String.Empty))
                        trackStr = trackStr + "/category_id:" + this.categoryStr + "\n";

                    if ((this.statusStr != "None") && (this.statusStr!= String.Empty))
                        trackStr = trackStr + "/status:" + this.statusStr + "\n";

                    if (this.sendSms)
                        trackStr = trackStr + "/SendSMS:" + this.sendSms + "\n";
                    
                    if (this.iucString != String.Empty)
                        trackStr = trackStr + this.iucString;

                    if (curItem.Body != null)
                        curItem.Body = trackStr + "\n" + curItem.Body.Replace("\\n", Environment.NewLine);
                    else
                        curItem.Body = trackStr + "\n";

                    log.Info("IUC string: " + trackStr);

                    //curItem.To = curItem.To + "("+assigneeStr + ")";
                    //curItem.CC = subscriberStr;
                }
                catch (Exception exp)
                {
                    log.Error("Error occurred while creating IUCs string" + exp.StackTrace + "\n" + "Message:" + exp.Message, exp);
                }
            }
        }    

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

        void setNewCompose()
        {
            //Sets the newCompose variable for checking whether the 
            //opened composed window is for new message or for reply/forward
            if (curItem.To == null)
                //if (item.Subject != null)
                //    newCompose = false;
                //else
                newCompose = true;
            else
                newCompose = false;
        }

        private void disableControls()
        {
            this.baComboBox.Enabled = false;
            this.dueDatePicker.Enabled = false;
            this.categoryComboBox.Enabled = false;
            this.statusComboBox.Enabled = false;
            this.smsCheckBox.Enabled = false;
        }

        private void enableControls()
        {
            this.baComboBox.Enabled = true;            
            this.dueDatePicker.Enabled = true;
            this.categoryComboBox.Enabled = true;
            this.statusComboBox.Enabled = true;
            this.smsCheckBox.Enabled  = true;
        }

        private void linkLabel1_LinkClicked(object sender, LinkLabelLinkClickedEventArgs e)
        {
            WebBrowser browser = new WebBrowser();
            browser.Navigate(Properties.Settings.Default.tBtisUrl, true);
        }

        void fillXmlInfo()
        {
            this.baComboBox.Items.Clear();
            fillBAComboBox();
            this.categoryComboBox.Items.Clear();
            fillCategoryComboBox();
            this.statusComboBox.Items.Clear();
            fillStatusComboBox();
        }

        void fillBAComboBox()
        {
            Outlook._NameSpace ns = null;
            BAUser user = null;
            try
            { 
                Object hostApp = TbitsADXModule.CurrentInstance.HostApplication;
                ns = ((Outlook._Application)hostApp).Session;

                string tmpString = ns.CurrentUser.Address;
                log.Info("Current user address: \"" + tmpString + "\"");
                string userEmail = getUserEmailUsingRegex(tmpString);

                log.Info("User address of the current user: " + userEmail.ToLower()) ;
                user = getBAUserFromEmail(userEmail.ToLower());
                log.Info("Matched user profile: " + user.login);
            }
            catch (Exception ex)
            {
                log.Error("Exception occured while retrieving user: " + ns.CurrentUser.Address + "\n" + ex.StackTrace
                    + "\n\n Message:\n" + ex.Message, ex);
            }   
            
            bool defValueSet = false; 
            if (user != null)         
            foreach (BA bizArea in baXmlParser.getBusinessAreas())
            {
                try
                {
                    if ((bizArea != null) && (bizArea.baUsers != null))
                        foreach (string id in bizArea.baUsers)
                            if (id == user.userId)
                            {
                                this.baComboBox.Items.Add(bizArea.displayName);
                                if (!defValueSet)
                                    if (prevBA == String.Empty)
                                    {
                                        this.baComboBox.Text = bizArea.displayName;
                                        curBA = bizArea;
                                        defValueSet = true;
                                        prevBA = this.baComboBox.Text;
                                    }
                                    else
                                        if (bizArea.displayName == prevBA)
                                        {
                                            this.baComboBox.Text = prevBA;
                                            curBA = bizArea;
                                            defValueSet = true;
                                        }
                            }
                }
                catch (Exception exp)
                {
                    log.Error("Error occurred while filling BA combobox" + exp.StackTrace + "Message:" + exp.Message, exp);
                }
            }
        }

        private static string getUserEmailUsingRegex(string tmpString)
        {
            string userEmail = "";
            string loginRegex = Properties.Settings.Default.loginRegex;            
            if (Regex.IsMatch(tmpString, loginRegex))
                userEmail = Regex.Replace(tmpString, loginRegex, "${1}");
            else
                userEmail = tmpString.Trim();
            return userEmail;
        }

        void fillCategoryComboBox()
        {
            try
            {
                foreach (BA bizArea in baXmlParser.getBusinessAreas())
                {
                    if (bizArea.displayName == this.baComboBox.Text)
                    {
                        this.categoryComboBox.Items.Add("None");
                        foreach (string str in bizArea.baCategory)
                            this.categoryComboBox.Items.Add(str);
                        break;                        
                    }
                }
            }
            catch (Exception exp)
            {
                log.Error("Exception occured while filling category combobox\n" + exp.StackTrace
                    + "\n\n Message:\n" + exp.Message, exp);
            }
        }

        void fillStatusComboBox()
        {
            try
            {
                foreach (BA bizArea in baXmlParser.getBusinessAreas())
                {
                    if (bizArea.displayName == this.baComboBox.Text)
                    {
                        this.statusComboBox.Items.Add("None");
                        foreach (string str in bizArea.baStatus)
                            this.statusComboBox.Items.Add(str);
                        break;                        
                    }
                }
            }
            catch (Exception exp)
            {
                log.Error("Exception occured while filling category combobox\n" + exp.StackTrace
                    + "\n\n Message:\n" + exp.Message, exp);
            }
        }

        private void comboBox1_SelectedIndexChanged(object sender, EventArgs e)
        {
            try
            {
                //For inspector window                
                foreach (BA bizArea in baXmlParser.getBusinessAreas())
                {
                    try
                    {
                        if (bizArea.displayName == this.baComboBox.Text)
                        {
                            curBA = bizArea;
                            prevBA = bizArea.displayName;                            
                            break;
                        }
                    }
                    catch (Exception)
                    {
                        return;
                    }
                }
                this.categoryComboBox.Items.Clear();
                this.categoryComboBox.Text = String.Empty;
                fillCategoryComboBox();
                this.statusComboBox.Items.Clear();
                this.statusComboBox.Text = String.Empty;
                fillStatusComboBox();
            }
            catch (Exception exp)
            {
                log.Error("Exception while changing BA:\n" + exp.StackTrace +
                    "\nMessage:\n" + exp.Message, exp);
            }
        }

        private void dueDatePicker_ValueChanged(object sender, EventArgs e)
        {
            //iucString = iucString + "/dueby:" + dueDatePicker.Text + "\n";            
        }

        private void smsCheckBox_CheckedChanged(object sender, EventArgs e)
        {
            sendSms = smsCheckBox.Checked;
        }

        private void statusComboBox_SelectedIndexChanged(object sender, EventArgs e)
        {
            statusStr = statusComboBox.Text;
        }

        private void categoryComboBox_SelectedIndexChanged(object sender, EventArgs e)
        {
            categoryStr = categoryComboBox.Text;
        }

        private void label1_Click(object sender, EventArgs e)
        {
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

        BAUser getBAUserFromEmail(string mailId) 
        {
            log.Info("Searching for matching tBits user profile...");
            foreach (BAUser user in baXmlParser.baUser)
            {
                string userLogin = user.login.ToLower();
                //MessageBox.Show("Retrieving id: " + user.userId+ "\n" +user.email);
                if (user == null)
                    break;
                
                if ((user.email == mailId) || (mailId == userLogin))
                {
                    log.Info("Matching User" + mailId + " and matched user: " + userLogin);
                    return user;
                }               
            }
            log.Warn("Could not find any matching profile in tBits for user: " + mailId + ". Please check if the user exists in tBits and is atleast member of one Business Area.");
            return null;
        }

        private void label1_Click_1(object sender, EventArgs e)
        {

        }
        
        #region email-info-retrieval

        //private string GetSMTPAddress(string address, IntPtr addrBookPtr)
        //{
        //    object addrBookObj = null;
        //    string smtpAddress = String.Empty;
        //    try
        //    {
        //        addrBookObj = Marshal.GetObjectForIUnknown(addrBookPtr);
        //        IAddrBook addrBook = addrBookObj as IAddrBook;
        //        if (addrBook != null)
        //        {                    
        //            IntPtr szPtr = IntPtr.Zero;
        //            IntPtr propValuePtr = Marshal.AllocHGlobal(16);
        //            IntPtr adrListPtr = Marshal.AllocHGlobal(16);

        //            Marshal.WriteInt32(propValuePtr, (int)MAPI.PR_DISPLAY_NAME);
        //            Marshal.WriteInt32(new IntPtr(propValuePtr.ToInt32() + 4), 0);
        //            szPtr = Marshal.StringToHGlobalAnsi(address);
        //            Marshal.WriteInt64(new IntPtr(propValuePtr.ToInt32() + 8), szPtr.ToInt32());

        //            Marshal.WriteInt32(adrListPtr, 1);
        //            Marshal.WriteInt32(new IntPtr(adrListPtr.ToInt32() + 4), 0);
        //            Marshal.WriteInt32(new IntPtr(adrListPtr.ToInt32() + 8), 1);
        //            Marshal.WriteInt32(new IntPtr(adrListPtr.ToInt32() + 12), propValuePtr.ToInt32());

        //            try
        //            {
        //                if (addrBook.ResolveName(0, MAPI.MAPI_DIALOG, null, adrListPtr) == MAPI.S_OK)
        //                {
        //                    SPropValue spValue = new SPropValue();
        //                    int pcount = Marshal.ReadInt32(new IntPtr(adrListPtr.ToInt32() + 8));
        //                    IntPtr props = new IntPtr(Marshal.ReadInt32(new IntPtr(adrListPtr.ToInt32() + 12)));
        //                    for (int i = 0; i < pcount; i++)
        //                    {
        //                        IntPtr addrEntryPtr = IntPtr.Zero;
        //                        IntPtr propAddressPtr = IntPtr.Zero;
        //                        try
        //                        {
        //                            spValue = (SPropValue)Marshal.PtrToStructure(
        //                                new IntPtr(props.ToInt32() + (16 * i)), typeof(SPropValue));
        //                            if (spValue.ulPropTag == MAPI.PR_ENTRYID)
        //                            {
        //                                uint objType = 0;
        //                                uint cb = (uint)(spValue.Value & 0xFFFFFFFF);
        //                                IntPtr entryID = new IntPtr((int)(spValue.Value >> 32));
        //                                if (addrBook.OpenEntry(cb, entryID, IntPtr.Zero, 0, out objType, out addrEntryPtr) == MAPI.S_OK)
        //                                {
        //                                    if (MAPI.HrGetOneProp(addrEntryPtr, MAPI.PR_EMS_AB_PROXY_ADDRESSES, out propAddressPtr) == MAPI.S_OK)
        //                                    {
        //                                        IntPtr emails = IntPtr.Zero;
        //                                        SPropValue addrValue = (SPropValue)Marshal.PtrToStructure(propAddressPtr, typeof(SPropValue));
        //                                        int acount = (int)(addrValue.Value & 0xFFFFFFFF);
        //                                        IntPtr pemails = new IntPtr((int)(addrValue.Value >> 32));
        //                                        for (int j = 0; j < acount; j++)
        //                                        {
        //                                            MessageBox.Show("Inside inner loop");
        //                                            emails = new IntPtr(Marshal.ReadInt32(new IntPtr(pemails.ToInt32() + (4 * j))));
        //                                            smtpAddress = Marshal.PtrToStringAnsi(emails);
        //                                            MessageBox.Show("smtpAddress:" + smtpAddress);
        //                                            if (smtpAddress.IndexOf("SMTP:") == 0)
        //                                            {
        //                                                smtpAddress = smtpAddress.Substring(5, smtpAddress.Length - 5);
        //                                                MessageBox.Show("smtpAddress in if:" + smtpAddress);
        //                                                break;
        //                                            }
        //                                        }
        //                                    }
        //                                }
        //                            }
        //                        }
        //                        catch 
        //                        {
        //                        }
        //                        finally
        //                        {
        //                            if (propAddressPtr != IntPtr.Zero)
        //                                Marshal.Release(propAddressPtr);
        //                            if (addrEntryPtr != IntPtr.Zero)
        //                                Marshal.Release(addrEntryPtr);
        //                        }
        //                    }
        //                }
        //            }
        //            finally
        //            {
        //                if (szPtr != IntPtr.Zero)
        //                    Marshal.FreeHGlobal(szPtr);
        //                if (propValuePtr != IntPtr.Zero)
        //                    Marshal.FreeHGlobal(propValuePtr);
        //                if (adrListPtr != IntPtr.Zero)
        //                    Marshal.FreeHGlobal(adrListPtr);
        //            }
        //        }
        //    }
        //    finally
        //    {
        //        if (addrBookObj != null)
        //            Marshal.ReleaseComObject(addrBookObj);
        //        if (addrBookPtr != IntPtr.Zero)
        //            Marshal.Release(addrBookPtr);
        //    }
        //    return smtpAddress;
        //}

        #endregion   
    }
}

