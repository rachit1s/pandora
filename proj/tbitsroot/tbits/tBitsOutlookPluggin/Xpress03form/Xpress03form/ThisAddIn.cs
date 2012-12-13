using System;
using System.Windows.Forms;
using Microsoft.VisualStudio.Tools.Applications.Runtime;
using Outlook = Microsoft.Office.Interop.Outlook;
using Office = Microsoft.Office.Core;
using System.Xml;
using System.IO;
using System.Threading;
using log4net;
using log4net.Config;

namespace Xpress03form
{
    public partial class ThisAddIn
    {
        // Create a logger for use in this class
        private static readonly log4net.ILog log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        // Create a logger for use in this class
        String baseDir = AppDomain.CurrentDomain.SetupInformation.ApplicationBase;

        private void ThisAddIn_Startup(object sender, System.EventArgs e)
        {
            #region Add-in Express for VSTO generated code;
            TbitsADXModule.Initialize(this, typeof(TbitsADXModule));
            #endregion;            

            // Set up a simple configuration that logs on the console.
            //BasicConfigurator.Configure();

            FileInfo fileInfo = new FileInfo(AppDomain.CurrentDomain.SetupInformation.ApplicationBase + Properties.Settings.Default.log4netConfig);
            XmlConfigurator.Configure(fileInfo);
            AppDomain.CurrentDomain.UnhandledException += new UnhandledExceptionEventHandler(CurrentDomain_UnhandledException);
            
            //MessageBox.Show("Base Dir: \n" + baseDir);

            if (Properties.Settings.Default.connectToServer)
            {
                try
                {
                    log.Info("Fetching data from the server");
                    BAXMLRetriever xmlRetriever = new BAXMLRetriever(baseDir);
                    Thread xmlThread = new Thread(new ThreadStart(xmlRetriever.loadXmlFromUrl));
                    xmlThread.Start();
                }
                catch (Exception exp) {                    
                    log.Error("Error occurred while retrieving information from the server. \n" + exp.StackTrace + "\n" + exp.Message ,exp);
                   // MessageBox.Show("At loading: \n" + exp.StackTrace + "\n" + exp.Message);
                    //return;
                }
            }
            else
                try
                {
                    log.Info("Loading info from local file : \n" + baseDir + Properties.Settings.Default.localXml);
                    loadBAXml(baseDir + Properties.Settings.Default.localXml);
                }
                catch (Exception exp)
                {
                    log.Error("Error occurred while loading data from local file: " + Properties.Settings.Default.localXml  + "  \n" + exp.StackTrace + "\n" + exp.Message, exp);
                    //return;
                }            
        }

        void CurrentDomain_UnhandledException(object sender, UnhandledExceptionEventArgs e)
        {
            try
            {
                Exception exp = new NotImplementedException();
                log.Fatal("An unhandled exception occurred while loading tBits plugin" + exp.StackTrace + "\n" + exp.Message, exp);
            }
            finally
            {
                TbitsADXModule.Finalize(this);
            }
        }      

        public void loadBAXml(String xmlFilePath)
        {
            BAXmlParser baXmlParser = BAXmlParser.GetInstance();            
            baXmlParser.loadXml(xmlFilePath);
        }      

        private void ThisAddIn_Shutdown(object sender, System.EventArgs e)
        {
            #region Add-in Express for VSTO generated code;
            log.Info("Closing the plugin");
            TbitsADXModule.Finalize(this);
            #endregion;            
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
