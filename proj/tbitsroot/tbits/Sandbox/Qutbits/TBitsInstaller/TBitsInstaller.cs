using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Configuration.Install;
using System.Security.Policy;
using System.Security;

namespace Qutbits
{
    [RunInstaller(true)]
    public partial class TBitsInstaller : Installer
    {
        private readonly string installPolicyLevel = "Machine";
        private readonly string namedPermissionSet = "FullTrust";
        private readonly string codeGroupDescription = "VSTO Permissions for Outlook Plugins";
        private readonly string productName = "tBits Outlook Plugin";
        private readonly bool debugBreakOnInstall = false;
        private string codeGroupName = ""; 

        public TBitsInstaller()
        {            
            InitializeComponent();
        }
       
        /// <summary>
        /// Gets a CodeGroup name based on the productname and URL evidence
        /// </summary>
        
        private string CodeGroupName
        {
            get
            {
                if (codeGroupName.Length == 0)
                {
                    codeGroupName = "["+ productName +"] " + InstallDirectory;
                }
                return codeGroupName;
            }
        } 

        /// <summary>
        /// Gets the installdirectory with a wildcard suffix for use with URL evidence
        /// </summary>
        
        private string InstallDirectory
        {
            get
            {
                // Get the install directory of the current installer
                string assemblyPath = this.Context.Parameters["assemblypath"];
                string installDirectory =
                    assemblyPath.Substring(0, assemblyPath.LastIndexOf("\\"));
                if (!installDirectory.EndsWith(@"\"))
                    installDirectory += @"\";
                installDirectory += "*";
                return installDirectory;
            }
        }

        public override void Install(System.Collections.IDictionary stateSaver)
        {
            base.Install(stateSaver);
            try
            {
                ConfigureCodeAccessSecurity();
                // Method not able to persist configuration to config file:
                // SetPortalUrlFromInstallerParameter();
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show(ex.ToString());
                this.Rollback(stateSaver);
            }
        }

        /// <summary>
        /// Configures FullTrust for the entire installdirectory
        /// </summary>

        private void ConfigureCodeAccessSecurity()
        {
            PolicyLevel machinePolicyLevel = GetPolicyLevel();
            if (null == GetCodeGroup(machinePolicyLevel))
            {
                // Create a new FullTrust permission set
                PermissionSet permissionSet = new NamedPermissionSet(this.namedPermissionSet);
                IMembershipCondition membershipCondition =
                    new UrlMembershipCondition(InstallDirectory);

	            // Create the code group
                PolicyStatement policyStatement = new PolicyStatement(permissionSet);
                CodeGroup codeGroup = new UnionCodeGroup(membershipCondition, policyStatement);
                codeGroup.Description = this.codeGroupDescription;
                codeGroup.Name = this.codeGroupName;

			    // Add the code group
                machinePolicyLevel.RootCodeGroup.AddChild(codeGroup);

                // Save changes
			    SecurityManager.SavePolicy();
            }
        }

        /// <summary>
        /// Gets the currently defined policylevel
        /// </summary>
        /// <returns></returns>

        private System.Security.Policy.PolicyLevel GetPolicyLevel()
        {
            // Find the machine policy level
            PolicyLevel machinePolicyLevel = null;
            System.Collections.IEnumerator policyHierarchy = SecurityManager.PolicyHierarchy();
            while (policyHierarchy.MoveNext())
            {
                PolicyLevel level = (PolicyLevel)policyHierarchy.Current;
                if (level.Label.CompareTo(installPolicyLevel) == 0)
                {
                    machinePolicyLevel = level;
                    break;
                }
            }

            if (machinePolicyLevel == null)
            {
                throw new ApplicationException(
                    "Could not find Machine Policy level. Code Access Security " +
                    "is not configured for this application."
                    );
            }
            return machinePolicyLevel;
        }

        /// <summary>
        /// Gets current codegroup based on CodeGroupName at the given policylevel
        /// </summary>
        /// <param name="policyLevel"></param>
        /// <returns>null if not found</returns>

        private System.Security.Policy.CodeGroup GetCodeGroup(System.Security.Policy.PolicyLevel policyLevel)
        {
            foreach (CodeGroup codeGroup in policyLevel.RootCodeGroup.Children)
            {
                if (codeGroup.Name.CompareTo(CodeGroupName) == 0)
                {
                    return codeGroup;
                }
            }
            return null;
        }
     
        public override void Uninstall(System.Collections.IDictionary savedState)
        {
            if (debugBreakOnInstall)
                System.Diagnostics.Debugger.Break();

            base.Uninstall(savedState);
            try
            {
                this.UninstallCodeAccessSecurity();
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Unable to uninstall code access security:\n\n" + ex.ToString());
            }
        }

        private void UninstallCodeAccessSecurity()
        {
            PolicyLevel machinePolicyLevel = GetPolicyLevel();
            CodeGroup codeGroup = GetCodeGroup(machinePolicyLevel);
            if(codeGroup != null)
            {
                machinePolicyLevel.RootCodeGroup.RemoveChild(codeGroup);

			    // Save changes
                SecurityManager.SavePolicy();
            }
        }
    }
}
