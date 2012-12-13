using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using System.Windows.Forms;

namespace Xpress03form
{
    public class BAUser
    {
        //Replace these public variables with methods.
        public string userId;
        public string firstName;
        public string lastName;
        public string displayName;
        public string login;
        public string defaultBA;
        public string email;
      
        //Variable for indexing of attribute 
        private static int userIdIndex = 0;

        public BAUser(XmlNode node) 
        {
            setUserAttributes(node);
            setUserValues(node);
        }

        private void setUserAttributes(XmlNode node)
        {
           userId = node.Attributes[userIdIndex].Value;            
        }

        private void setUserValues(XmlNode node)
        {
            foreach (XmlNode cNode in node)
            {
                switch (cNode.Name)
                {
                    case "login":
                    {
                        login = cNode.InnerText;
                        break;
                    }
                    case "first_name":
                    {
                        firstName = cNode.InnerText;
                        break;
                    }
                    case "last_name":
                    {
                        lastName = cNode.InnerText;                        
                        break;
                    }
                    case "display_name":
                    {
                        displayName = cNode.InnerText;
                        break;
                    }
                    case "email_id":
                    {
                        email = cNode.InnerText;
                        break;
                    }
                    case "default_BA":
                    {
                        defaultBA = cNode.InnerText;
                        break;
                    }
                }
            }
        }    
    }
}
