using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using System.Windows.Forms;

namespace Xpress03form
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
        public string isPrivate;
              
        public BA(XmlNode node) 
        {            
            setBAAttributes(node);
            setBAValues(node);
        }

        private void setBAAttributes(XmlNode node)
        {
            foreach (XmlAttribute attr in node.Attributes)
            {
                switch (attr.Name)
                {
                    case "name":
                        {
                            baName = attr.Value;
                            break;
                        }
                    case "display_name":
                        {
                            displayName = attr.Value;
                            break;
                        }
                    case "is_private":
                        {
                            isPrivate = attr.Value;
                            break;
                        }
                }
            }
        }

        private void setBAValues(XmlNode node)
        {
            foreach (XmlNode cNode in node)
            { 
                switch (cNode.Name){
                    case "bausers":{
                        baUsers = cNode.InnerText.Split(',');
                        break;
                    }
                    case "assignees":{
                        baAssignee = cNode.InnerText;
                        break;
                    }
                    case "email": {
                        email = cNode.InnerText;
                        break;
                    }
                    case "category": {
                        baCategory = cNode.InnerText.Split(',');
                        break;
                    }
                    case "status": {
                        baStatus = cNode.InnerText.Split(',');
                        break;
                    }
                    case "prefix": {
                        prefix = cNode.InnerText;
                        break;
                    }
                }
            }
        }
    }
}



