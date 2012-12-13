using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using System.Windows.Forms;

namespace tBitsOutlookPlugin
{
    public class BA
    {
        // Should replace these methods instead of public variables
        public string baName;        
        public string displayName;
        public string[] baUsers;
        public string baAssignee;
        public string email;
        public string prefix;
        public string[] baCategory;
        public string[] baStatus;
              
        public BA(XmlNode node) 
        {            
            this.setBAAttributes(node);
            this.setBAValues(node);
        }

        private void setBAAttributes(XmlNode node)
        {
            this.baName = node.Attributes[0].Value;            
            this.displayName = node.Attributes[1].Value;
        }

        private void setBAValues(XmlNode node)
        {            
            this.baUsers = node.ChildNodes[0].InnerText.Split(',');
            this.baAssignee = node.ChildNodes[1].InnerText;
            this.email = node.ChildNodes[2].InnerText;
            this.baCategory = node.ChildNodes[3].InnerText.Split(',');
            this.baStatus = node.ChildNodes[4].InnerText.Split(',');
            this.prefix = node.ChildNodes[5].InnerText;
        }
    }
}



