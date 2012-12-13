using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using System.IO;
using System.Windows.Forms;

[assembly: log4net.Config.XmlConfigurator(ConfigFile = "tBitsLogger.config", Watch = true)]

namespace Xpress03form
{
    public class BAXMLRetriever
    {
        bool isServerRunning = true;
        String baseDir = String.Empty;
        String tempFilePath = String.Empty;
        String baXmlPath = String.Empty;

        // Create a logger for use in this class
        private static readonly log4net.ILog log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        public BAXMLRetriever(String baseDir) 
        {
            try
            {
                log.Info("Retrieving info from tBits server");
                this.baseDir = baseDir;
                this.tempFilePath = baseDir + Properties.Settings.Default.tempFile;
                this.baXmlPath = baseDir + Properties.Settings.Default.localXml;
            }
            catch (Exception e) {
                log.Error("Error occurred while retrieving information from tBits server.", e);
                /*MessageBox.Show("Exception while creating BAXMLRetriever: \n" + e.Message +
                    "\n" + e.StackTrace);
                return;*/
            }
        }

        public void loadXmlFromUrl()
        {
            int attempt = 0;           
           
            try
            {
                XmlUrlResolver resolver = new XmlUrlResolver();
                resolver.Credentials = System.Net.CredentialCache.DefaultCredentials;

                // Point the resolver at the desired resource and resolve as a stream.
                Uri baseUri = new Uri(Properties.Settings.Default.tBtisUrl);
                Uri fulluri = resolver.ResolveUri(baseUri, Properties.Settings.Default.servletName);
                retreiveFromUrl(resolver, fulluri, attempt);
                //load xml
                BAXmlParser baXmlParser = BAXmlParser.GetInstance();
                if (isServerRunning)
                {
                    //MessageBox.Show("tempfilepath: \n" + tempFilePath);
                    baXmlParser.loadXml(tempFilePath);
                    writeXmlFromFile(tempFilePath, baXmlPath);
                }
                else
                {
                    //MessageBox.Show("baXmlpath: \n" + baXmlPath);
                    baXmlParser.loadXml(baXmlPath);
                }
            }
            catch (Exception e)
            {
                log.Error("Error while create xml file BAXMLInfo.xml", e);
            }
        }

        private void retreiveFromUrl(XmlUrlResolver resolver, Uri fulluri, int attempt)
        {
            try
            {
                attempt = attempt + 1;
                Stream s = (Stream)resolver.GetEntity(fulluri, null, typeof(Stream));
                writeXmlFromUrl(s, tempFilePath);                
            }
            catch (Exception e)
            {
                if (attempt == Properties.Settings.Default.serverConnectionCount)
                {
                    isServerRunning = false;
                    log.Info("Could not retrieve information from tBits server. Please check if server is down..", e);
                    return;
                }
                else
                    retreiveFromUrl(resolver, fulluri, attempt);
            }
        }

        private void writeXmlFromUrl(Stream s, string outputFilePath)
        {
            StreamReader reader = new StreamReader(s);
            writeXmlToFile(reader, outputFilePath);
        }

        private void writeXmlFromFile (string inputFilePath, string outputFilePath)
        {
            StreamReader reader = new StreamReader (inputFilePath);
            writeXmlToFile(reader, outputFilePath);
        }

        private void writeXmlToFile(StreamReader reader, string outputFilePath)
        {
            StreamWriter writer = File.CreateText(outputFilePath);
            while (!reader.EndOfStream)
            {
                writer.WriteLine(reader.ReadLine());
                writer.Flush();
            }
            writer.Dispose();
        }
    }
}
