using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using System.Windows.Forms;

namespace tBitsOutlookPlugin
{
    public class BAUser
    {
        //Replace these public variables with methods.
        public string userId;
        public string firstName;
        public string lastName;
        public string displayName;
        public string login;
      
        //Variable for indexing of attribute 
        private static int userIdIndex = 0;
        
        //Variables for indexing of child nodes
        private static int loginIndex = 0;
        private static int fNameIndex = 1;
        private static int lNameIndex = 2;
        private static int dNameIndex = 3;
       
        public BAUser(XmlNode node) 
        {
            this.setUserAttributes(node);
            this.setUserValues(node);
        }

        private void setUserAttributes(XmlNode node)
        {
            this.userId = node.Attributes[userIdIndex].Value;            
        }

        private void setUserValues(XmlNode node)
        {           
           
                this.firstName = node.ChildNodes[fNameIndex].InnerText;
                this.lastName = node.ChildNodes[lNameIndex].InnerText;
                this.displayName = node.ChildNodes[dNameIndex].InnerText;
                this.login = node.ChildNodes[loginIndex].InnerText;
        }    
    }
}
