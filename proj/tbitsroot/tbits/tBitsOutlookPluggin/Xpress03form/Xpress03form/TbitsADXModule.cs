using System;
using System.Runtime.InteropServices;
using System.ComponentModel;
using System.Windows.Forms;
using Outlook = Microsoft.Office.Interop.Outlook;
using Office = Microsoft.Office.Core;
 
namespace Xpress03form
{
    /// <summary>
    ///   Add-in Express for VSTO Module
    /// </summary>
    [ComVisible(true)]
	public class TbitsADXModule : AddinExpress.VSTO.ADXOutlookAddin
    {
        public TbitsADXModule(object application)
            : base(application)
        {
            InitializeComponent();
        }

        private AddinExpress.OL.ADXOlFormsManager tBitslFormsManager;
        private AddinExpress.OL.ADXOlFormsCollectionItem tBitsCollectionItem;
         
        public TbitsADXModule()
        {
            InitializeComponent();
        }
 
        #region Component Designer generated code
        /// <summary>
        /// Required by designer
        /// </summary>
        private System.ComponentModel.IContainer components;
 
        /// <summary>
        /// Required by designer support - do not modify
        /// the following method
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            this.tBitslFormsManager = new AddinExpress.OL.ADXOlFormsManager(this.components);
            this.tBitsCollectionItem = new AddinExpress.OL.ADXOlFormsCollectionItem(this.components);
            ((System.ComponentModel.ISupportInitialize)(this)).BeginInit();
            // 
            // tBitslFormsManager
            // 
            this.tBitslFormsManager.Items.Add(this.tBitsCollectionItem);
            this.tBitslFormsManager.SetOwner(this);
            // 
            // tBitsCollectionItem
            // 
            this.tBitsCollectionItem.FormClassName = "Xpress03form.TbitsADXOlForm";
            this.tBitsCollectionItem.InspectorItemTypes = AddinExpress.OL.ADXOlInspectorItemTypes.olMail;
            this.tBitsCollectionItem.InspectorLayout = AddinExpress.OL.ADXOlInspectorLayout.TopSubpane;
            ((System.ComponentModel.ISupportInitialize)(this)).EndInit();

        }
        #endregion
 
        #region Add-in Express automatic code
 
        // Required by Add-in Express - do not modify
        // the methods within this region
 
        public override System.ComponentModel.IContainer GetContainer()
        {
            if (components == null)
                components = new System.ComponentModel.Container();
            return components;
        }
 
        #endregion
 
        internal Outlook._Application OutlookApp
        {
            get
            {
                return (HostApplication as Outlook._Application);
            }
        } 
    }    

	public partial class ThisAddIn
	{
		protected override object RequestService(Guid serviceGuid)
		{
			if (serviceGuid == typeof(AddinExpress.VSTO.IRibbonExtensibility).GUID)
			{
				TbitsADXModule.Initialize(this, typeof(TbitsADXModule));
				return TbitsADXModule.CurrentInstance;
			}

			return base.RequestService(serviceGuid);
		}
	}
}

