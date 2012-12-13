using System;
using System.Xml;
using System.Collections;
using System.Windows.Forms;
using System.IO;

namespace Xpress03form
{
    public class BAXmlParser
    {
        // Create a logger for use in this class
        private static readonly log4net.ILog log = log4net.LogManager.GetLogger("Xpress03form.ThisAddIn");

        private XmlDocument doc;
        private XmlNode root;
        private string xmlPath;
        public ArrayList ba;
        public ArrayList baUser;                
        
        private static BAXmlParser instance;
                
        public static BAXmlParser GetInstance()
        {
            if (instance == null)
                instance = new BAXmlParser();          
            return instance;
        }

        private BAXmlParser()
        {
            try
            {
                ba = new ArrayList();
                baUser = new ArrayList();
                doc = new XmlDocument();
            }
            catch (Exception exp)
            {
                log.Error(exp.StackTrace + "\n Message:" + exp.Message, exp);
                //return;
            }
        }

        //Takes the file path as a parameter from which the xml info is retrieved
        public void loadXml (String xmlPath)
        {
            this.xmlPath = xmlPath;
            
            try
            {
                log.Info("Loading data from Xml into tBits plugin");
                doc.Load(this.xmlPath);
            }
            catch (XmlException exp)
            {
                log.Error("Could not load information from tBits" + "\nMessage:\n" + exp.Message, exp);
                //return;                
            }

            root = doc.DocumentElement;
            setBusinessAreas();
            setTBitsUsers();
        }

        public void reloadXml()
        {
            try
            {
                if (this.xmlPath != null)
                {
                    doc.Load(this.xmlPath);
                    root = doc.DocumentElement;
                    setBusinessAreas();
                    setTBitsUsers();
                }
            }
            catch (Exception exp)
            {
                log.Error("Error occurred while reload info from local file", exp);
            }
        }     

        public string lastchild()
        {
            return root.LastChild.ChildNodes[0].Attributes[1].Value;
        }

        /// <summary>
        /// Gets a list of BA
        /// </summary>
        /// <returns></returns>
        private void setBusinessAreas()
        {            
            XmlNode basNode = getNode("bas");
            try
            {
                ba.Clear();
                if (basNode.HasChildNodes)
                {
                    foreach (XmlNode baNode in basNode)
                    {
                        ba.Add (new BA(baNode));
                    }
                }
            }
            catch (Exception exp)
            {
                log.Error("Error occurred while setting BA info" + exp.StackTrace + "Message:" + exp.Message, exp);
            }
        }      

        private void setTBitsUsers()
        {
            XmlNode usersNode = getNode("users");
            baUser.Clear();
            foreach (XmlNode cNode in usersNode)
            {                
                try
                {
                    baUser.Add(new BAUser(cNode));
                }
                catch (NullReferenceException exp)
                {
                    log.Error("Error occurred while loading user info" + exp.StackTrace + "\nMessage:" + exp.Message, exp);
                }
            }
        }

        public ArrayList getBusinessAreas()
        {
            if (ba != null)
            {
                return ba;
            }
            else 
            {
                return null;    
            }
        }

        public ArrayList getTBitsUsers()
        {
            if (baUser != null)
            {
                return baUser;
            }
            else
            {
                return null;
            }
        }
                
        XmlNode getNode(String nodeName)
        {
            if (root.HasChildNodes)
                foreach(XmlNode cNode in root)
                    if (cNode.Name == nodeName)
                        return cNode;
            return null;
        }
    }
}
