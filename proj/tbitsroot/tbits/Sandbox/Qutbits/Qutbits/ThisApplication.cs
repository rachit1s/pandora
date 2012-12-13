using System;
using System.Windows.Forms;
using Microsoft.VisualStudio.Tools.Applications.Runtime;
using Outlook = Microsoft.Office.Interop.Outlook;
using Office = Microsoft.Office.Core;
using Word = Microsoft.Office.Interop.Word;
using System.Text.RegularExpressions;

namespace Qutbits
{
    public partial class ThisApplication
    {     
        Outlook.Inspectors _Inspectors;
        Outlook.Explorers _Explorers;
        Outlook.Application curApp;
        static bool isServerRunning = true;
          
        public string baXmlPath;//= Application.StartupPath + "\\" + baXmlFileName;            

        // This dictionary holds our Wrapped Inspectors, Explorers, Folders, Items, Whatever
        System.Collections.Generic.Dictionary<Guid, WrapperClass> _WrappedObjects;

        private void ThisAddIn_Startup(object sender, System.EventArgs e)
        {
            try
            {               
                //Application object to handle incoming new mails to set them blue flag if it is from tBits
                curApp = new Outlook.Application();
                curApp.NewMail += new Outlook.ApplicationEvents_11_NewMailEventHandler(curApp_NewMail);

                _WrappedObjects = new System.Collections.Generic.Dictionary<Guid, WrapperClass>();

                Properties.Settings.Default.PropertyChanged += new System.ComponentModel.PropertyChangedEventHandler(
                    Default_PropertyChanged);

                try
                {
                    loadBAXml(Properties.Settings.Default.outlookConfigUrl);
                }
                catch(Exception)
                {
                    isServerRunning = false;
                }

                if (!isServerRunning)
                {
                    try
                    {
                        loadBAXml(Properties.Settings.Default.outlookDefXmlPath);
                    }
                    catch (Exception)
                    {
                        MessageBox.Show("Server is not running, or the URL path of the server is not set properly");
                    }
                }
                
                // Inspectors stuff
                _Inspectors = this.Inspectors;
                _Inspectors.NewInspector += new Outlook.InspectorsEvents_NewInspectorEventHandler(_Inspectors_NewInspector);

                // Are there any open Inspector after Startup ?
                for (int i = _Inspectors.Count; i >= 1; i--)
                {
                    // Wrap the Inspector and do something useful with it
                    WrapInspector(_Inspectors[i]);
                }

                // Explorer stuff
                _Explorers = this.Explorers;

                // Are there any open Explorers after Startup ?
                for (int i = _Explorers.Count; i >= 1; i--)
                {
                    // Wrap the Explorer and do some usefull with it
                    WrapExplorer(_Explorers[i]);
                }
                _Explorers.NewExplorer += new Outlook.ExplorersEvents_NewExplorerEventHandler(_Explorers_NewExplorer);
            }            
            catch (Exception exp)
            {
                MessageBox.Show("Exception while starting the toolbar:\n" + exp.StackTrace +
                    "\nMessage:" + exp.Message);
            }
        }

        void _Explorers_NewExplorer(Microsoft.Office.Interop.Outlook.Explorer Explorer)
        {
            WrapExplorer(Explorer);
        }

        void WrapExplorer(Microsoft.Office.Interop.Outlook.Explorer Explorer)
        {
            ExplorerWrapper wrappedExplorer = new ExplorerWrapper(Explorer);
            wrappedExplorer.Closed += new WrapperClosedDelegate(wrappedObject_Closed);
            _WrappedObjects[wrappedExplorer.Id] = wrappedExplorer;
        }


        void _Inspectors_NewInspector(Outlook.Inspector Inspector)
        {
            WrapInspector(Inspector);
            Inspector.WindowState = Outlook.OlWindowState.olNormalWindow;
        }

        void WrapInspector(Microsoft.Office.Interop.Outlook.Inspector Inspector)
        {
            InspectorWrapper wrappedInspector = new InspectorWrapper(Inspector);
            wrappedInspector.Closed += new WrapperClosedDelegate(wrappedObject_Closed);
            _WrappedObjects[wrappedInspector.Id] = wrappedInspector;
        }

        void wrappedObject_Closed(Guid id)
        {
            _WrappedObjects.Remove(id);
        }

        void Default_PropertyChanged (Object obj, System.ComponentModel.PropertyChangedEventArgs eventArgs)
        {
            Properties.Settings.Default.Save();
            Properties.Settings.Default.Reload();
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
                        if (tBitsRegex.IsMatch(item.Subject))
                        {
                            item.FlagIcon = Outlook.OlFlagIcon.olBlueFlagIcon;
                            item.Save();
                        }
                    }
                }
            }
            catch (Exception exp)
            {
                MessageBox.Show("Expection in new message event: \n" + exp.StackTrace
                    + "\n \n Message: \n" + exp.Message);
            }
        }

        void loadBAXml(String xmlFilePath)
        {
            BAXmlParser baXmlParser = BAXmlParser.GetInstance();
            baXmlParser.loadXml(xmlFilePath);           
        }      

        private void ThisAddIn_Shutdown(object sender, System.EventArgs e)
        {
            curApp.NewMail -= new Outlook.ApplicationEvents_11_NewMailEventHandler(curApp_NewMail);
            _WrappedObjects.Clear();
            _Inspectors = null;
            _Explorers = null;
        }

        #region VSTO generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InternalStartup()
        {
            this.Startup += new System.EventHandler(ThisAddIn_Startup);
            this.Shutdown += new System.EventHandler(ThisAddIn_Shutdown);
        }
        #endregion
    }
}


