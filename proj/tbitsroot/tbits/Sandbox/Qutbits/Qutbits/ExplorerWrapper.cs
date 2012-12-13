using System;
using System.Collections.Generic;
using System.Text;
using Outlook = Microsoft.Office.Interop.Outlook;
using Office = Microsoft.Office.Core;

namespace Qutbits
{
    internal class ExplorerWrapper : WrapperClass
    {
        public Outlook.Explorer Explorer;// { get; private set; }

        public ExplorerWrapper(Outlook.Explorer explorer): base()
        {
            Explorer = explorer;            
            ConnectEvents();
        }

        void ConnectEvents()
        {
            ((Outlook.ExplorerEvents_10_Event)Explorer).Activate += 
                new Outlook.ExplorerEvents_10_ActivateEventHandler(ExplorerWrapper_Activate);
            ((Outlook.ExplorerEvents_10_Event)Explorer).Close += 
                new Outlook.ExplorerEvents_10_CloseEventHandler(ExplorerWrapper_Close);
            ((Outlook.ExplorerEvents_10_Event)Explorer).Deactivate += 
                new Outlook.ExplorerEvents_10_DeactivateEventHandler(ExplorerWrapper_Deactivate);            
        }

        void DisconnectEvents()
        {
            ((Outlook.ExplorerEvents_10_Event)Explorer).Activate -= 
                new Outlook.ExplorerEvents_10_ActivateEventHandler(ExplorerWrapper_Activate);
            ((Outlook.ExplorerEvents_10_Event)Explorer).Close -= 
                new Outlook.ExplorerEvents_10_CloseEventHandler(ExplorerWrapper_Close);
            ((Outlook.ExplorerEvents_10_Event)Explorer).Deactivate -= 
                new Outlook.ExplorerEvents_10_DeactivateEventHandler(ExplorerWrapper_Deactivate);
        }

        void ExplorerWrapper_Close()
        {
            DisconnectEvents();
            Explorer = null;
            GC.Collect();
            GC.WaitForPendingFinalizers();
            OnClosed();
        }
                
        #region Activate / Deactivate

        void ExplorerWrapper_Activate()
        {
        }

        void ExplorerWrapper_Deactivate()
        {
        }

        #endregion     
    }
}
