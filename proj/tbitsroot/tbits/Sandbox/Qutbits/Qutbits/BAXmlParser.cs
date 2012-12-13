using System;
using System.Xml;
using System.Collections;
using System.Windows.Forms;

namespace Qutbits
{
    public class BAXmlParser
    {
        private XmlDocument doc;
        private XmlNode root;
        private string xmlPath;
        public ArrayList ba;
        public ArrayList baUser;                
        static private string webLink = null;

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
                MessageBox.Show(exp.StackTrace + "\n Message:" + exp.Message);
            }
        }

        public void loadXml (String xmlPath)
        {
            this.xmlPath = xmlPath;
            //try
            //{
                doc.Load(this.xmlPath);                
            //}
            //catch (Exception)
            //{
            //    //MessageBox.Show("tBits toolbar could not connect to server.\nDetails:\nServer may not be running or the url path is wrong in the configuration file");
            //}
            root = doc.DocumentElement;
            setBusinessAreas();
            setTBitsUsers();
            setTBitsWebLink();
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
                    setTBitsWebLink();
                }
            }
            catch (Exception exp)
            {
                MessageBox.Show(exp.StackTrace + "\n Message:" + exp.Message);
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
                MessageBox.Show(exp.StackTrace + "Message:" + exp.Message);
            }
        }      

        private void setTBitsUsers()
        {
            XmlNode usersNode = getNode("users");
            foreach (XmlNode cNode in usersNode)
            {
                baUser.Clear();
                try
                {
                    baUser.Add(new BAUser(cNode));
                }
                catch (NullReferenceException exp)
                {
                    MessageBox.Show(exp.StackTrace + "\nMessage:" + exp.Message);
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
        
        private void setTBitsWebLink()
        {
            XmlNodeList webLinkNode = doc.GetElementsByTagName("tBits_link");           
            foreach (XmlNode node in webLinkNode)
            {
                webLink= node.InnerText;
            } 
        }

        public string getTBitsWebLink()
        {
            return webLink;
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
