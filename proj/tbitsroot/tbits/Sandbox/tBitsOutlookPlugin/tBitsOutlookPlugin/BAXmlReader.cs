using System;
using System.Xml;
using System.Collections;
using System.Windows.Forms;

namespace tBitsOutlookPlugin
{
    public class BAXmlReader
    {
        private XmlDocument doc;
        private XmlNode node;
        public BA[] ba;
        public BAUser[] baUser;        
        static private int MaxBusinessAreas = 50;
        static private int MaxUsers = 1000;
        //public string webLink = null;

        private static BAXmlReader instance;
                
        public static BAXmlReader GetInstance()
        {
            if (instance == null)
                instance = new BAXmlReader();            
            return instance;
        }

        private BAXmlReader()
        {
            ba = new BA[MaxBusinessAreas];
            baUser = new BAUser[MaxUsers];                       
            doc = new XmlDocument();           
        }

        public void getXmlValues(String xmlPath)
        {
            doc.Load(xmlPath);
            node = doc.DocumentElement;
        }

        public string lastchild()
        {
            return node.LastChild.ChildNodes[0].Attributes[1].Value;
        }

        /// <summary>
        /// Gets a list of BA
        /// </summary>
        /// <returns></returns>
        public void setBusinessAreas()
        {
            XmlNode baNode = null;
            for (int i = 0; i < node.LastChild.ChildNodes.Count; i++)
            {
                try
                {
                    baNode = node.LastChild.ChildNodes[i];
                    ba[i] = new BA(baNode);
                }
                catch (Exception exp)
                {
                    MessageBox.Show(exp.StackTrace + "Message:" + exp.Message);
                }
            }
        }

        public void setTBitsUsers()
        {
            XmlNode curUser = null;
            for (int i = 0; i < node.FirstChild.ChildNodes.Count; i++)
            {
                try
                {
                    curUser = node.FirstChild.ChildNodes[i];
                    baUser[i] = new BAUser(curUser);
                }
                catch (NullReferenceException exp)
                {
                    MessageBox.Show(exp.StackTrace + "Message:" + exp.Message);
                }
            }
        }

        public BA[] getBusinessAreas()
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

        public BAUser[] getTBitsUsers()
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
        /*
        public string getTBitsWebLink()
        {
            foreach (XmlNode child in node.ChildNodes)
            {
                MessageBox.Show(child.Value);
                if (child.Name == "tbits_Web_Link")
                {
                    return child.Value;
                }
            }
            return null;
        }
         * */

    }
}
