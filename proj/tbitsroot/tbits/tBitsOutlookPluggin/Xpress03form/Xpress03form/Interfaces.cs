using System;
using System.Runtime.InteropServices;

namespace Xpress03form
{
	internal class MAPI
	{
		public const int S_OK = 0;

		public const uint PR_TRANSPORT_MESSAGE_HEADERS = 0x7D001E;
		public const uint PR_BODY = 0x1000001E;
		public const uint PR_BODY_HTML = 0x1013001E;
		public const uint PR_HTML = 0x10130102;

		public const uint MV_FLAG			= 0x1000;

		public const uint PT_UNSPECIFIED	= 0;
		public const uint PT_NULL			= 1;
		public const uint PT_I2				= 2;
		public const uint PT_LONG			= 3;
		public const uint PT_R4				= 4;
		public const uint PT_DOUBLE			= 5;
		public const uint PT_CURRENCY		= 6;
		public const uint PT_APPTIME		= 7;
		public const uint PT_ERROR			= 10;
		public const uint PT_BOOLEAN		= 11;
		public const uint PT_OBJECT			= 13;
		public const uint PT_I8				= 20;
		public const uint PT_STRING8		= 30;
		public const uint PT_UNICODE		= 31;
		public const uint PT_SYSTIME		= 64;
		public const uint PT_CLSID			= 72;
		public const uint PT_BINARY			= 258;
		public const uint PT_MV_TSTRING     = (MV_FLAG | PT_STRING8);

		public const uint PR_SENDER_ADDRTYPE = (PT_STRING8 | (0x0C1E << 16));
		public const uint PR_SENDER_EMAIL_ADDRESS   = (PT_STRING8 | (0x0C1F << 16));
		public const uint PR_SENDER_NAME = (PT_STRING8 | (0x0C1A << 16));
		public const uint PR_ADDRTYPE = (PT_STRING8 | (0x3002 << 16));
		public const uint PR_ADDRTYPE_W = (PT_UNICODE | (0x3002 << 16));
		public const uint PR_EMAIL_ADDRESS = (PT_STRING8 | (0x3003 << 16));
		public const uint PR_EMAIL_ADDRESS_W = (PT_UNICODE | (0x3003 << 16));
		public const uint PR_DISPLAY_NAME = (PT_STRING8 | (0x3001 << 16));
		public const uint PR_DISPLAY_NAME_W = (PT_UNICODE | (0x3001 << 16));
		public const uint PR_ENTRYID = (PT_BINARY | (0x0FFF << 16));
		public const uint PR_EMS_AB_PROXY_ADDRESSES = unchecked((uint)(PT_MV_TSTRING | (0x800F << 16)));

			
		public const uint MAPI_NEW_SESSION        = 0x00000002;
		public const uint MAPI_FORCE_DOWNLOAD     = 0x00001000;
		public const uint MAPI_LOGON_UI           = 0x00000001;
		public const uint MAPI_ALLOW_OTHERS       = 0x00000008;
		public const uint MAPI_EXPLICIT_PROFILE   = 0x00000010;
		public const uint MAPI_EXTENDED           = 0x00000020;
		public const uint MAPI_SERVICE_UI_ALWAYS  = 0x00002000;
		public const uint MAPI_NO_MAIL            = 0x00008000;
		public const uint MAPI_USE_DEFAULT        = 0x00000040;

		public const uint AB_NO_DIALOG            = 0x00000001;
		public const uint MAPI_DIALOG             = 0x00000008;

		[DllImport("MAPI32.DLL", CharSet=CharSet.Ansi, EntryPoint="HrGetOneProp@12")]
		public static extern int HrGetOneProp(IntPtr pmp, uint ulPropTag, out IntPtr ppProp);

		[DllImport("MAPI32.DLL", CharSet=CharSet.Ansi, EntryPoint="MAPIFreeBuffer@4")]
		public static extern void MAPIFreeBuffer(IntPtr lpBuffer);

		[DllImport("MAPI32.DLL", CharSet=CharSet.Ansi, EntryPoint="MAPIInitialize@4")]
		public static extern int MAPIInitialize(IntPtr lpMapiInit);
			
		[DllImport("MAPI32.DLL", CharSet=CharSet.Ansi, EntryPoint="MAPILogonEx@20")]
		public static extern int MAPILogonEx(uint ulUIParam, [MarshalAs(UnmanagedType.LPWStr)] string lpszProfileName,
			[MarshalAs(UnmanagedType.LPWStr)] string lpszPassword, uint flFlags, out IntPtr lppSession);
 
	}

	[StructLayout(LayoutKind.Sequential)]
	public struct SPropValue
	{
		public uint ulPropTag;
		public uint dwAlignPad;
		public long Value;
	}
		
	[StructLayout(LayoutKind.Sequential)]
	public struct ADRENTRY
	{
		public uint ulReserved1;
		public uint cValues;
		public IntPtr rgPropVals;
	}
        
	[StructLayout(LayoutKind.Sequential)]
	public struct ADRLIST
	{
		public uint cEntries;
		public ADRENTRY aEntries;
	}

	[
	ComImport, ComVisible(false),
	InterfaceType(ComInterfaceType.InterfaceIsIUnknown), 
	Guid("00020300-0000-0000-C000-000000000046")
	]
	public interface IMAPISession
	{
		int GetLastError(int hResult, uint ulFlags, out IntPtr lppMAPIError);
		int GetMsgStoresTable(uint ulFlags, out IntPtr lppTable);
		int OpenMsgStore(uint ulUIParam, uint cbEntryID, IntPtr lpEntryID, ref Guid lpInterface, uint ulFlags, out IntPtr lppMDB);
		int OpenAddressBook(uint ulUIParam, IntPtr lpInterface, uint ulFlags, out IntPtr lppAdrBook);
		int OpenProfileSection(ref Guid lpUID, ref Guid lpInterface, uint ulFlags, out IntPtr lppProfSect);
		int GetStatusTable(uint ulFlags, out IntPtr lppTable);
		int OpenEntry(uint cbEntryID, IntPtr lpEntryID, ref Guid lpInterface, uint ulFlags, out uint lpulObjType, out IntPtr lppUnk);
		int CompareEntryIDs(uint cbEntryID1, IntPtr lpEntryID1, uint cbEntryID2, IntPtr lpEntryID2, uint ulFlags, out uint lpulResult);
		int Advise(uint cbEntryID, IntPtr lpEntryID, uint ulEventMask, IntPtr lpAdviseSink, out uint lpulConnection);
		int Unadvise(uint ulConnection);
		int MessageOptions(uint ulUIParam, uint ulFlags, [MarshalAs(UnmanagedType.LPWStr)] string lpszAdrType, IntPtr lpMessage);
		int QueryDefaultMessageOpt([MarshalAs(UnmanagedType.LPWStr)] string lpszAdrType, uint ulFlags, out uint lpcValues, out IntPtr lppOptions);
		int EnumAdrTypes(uint ulFlags, out uint lpcAdrTypes, out IntPtr lpppszAdrTypes);
		int QueryIdentity(out uint lpcbEntryID, out IntPtr lppEntryID);
		int Logoff(uint ulUIParam, uint ulFlags, uint ulReserved);
		int SetDefaultStore(uint ulFlags, uint cbEntryID, IntPtr lpEntryID);
		int AdminServices(uint ulFlags, out IntPtr lppServiceAdmin);
		int ShowForm(uint ulUIParam, IntPtr lpMsgStore, IntPtr lpParentFolder, ref Guid lpInterface, uint ulMessageToken,
			IntPtr lpMessageSent, uint ulFlags, uint ulMessageStatus, uint ulMessageFlags, uint ulAccess, [MarshalAs(UnmanagedType.LPWStr)] string lpszMessageClass);
		int PrepareForm(ref Guid lpInterface, IntPtr lpMessage, out uint lpulMessageToken);
	}

	[
	ComImport, ComVisible(false),
	InterfaceType(ComInterfaceType.InterfaceIsIUnknown), 
	Guid("00020303-0000-0000-C000-000000000046")
	]
	public interface IMAPIProp
	{
		int GetLastError(int hResult, uint ulFlags, out IntPtr lppMAPIError);
		int SaveChanges(uint ulFlags);
		int GetProps(IntPtr lpPropTagArray, uint ulFlags, out uint lpcValues, out IntPtr lppPropArray);
		int GetPropList(uint ulFlags, out IntPtr lppPropTagArray);
		int OpenProperty(uint ulPropTag, ref Guid lpiid, uint ulInterfaceOptions, uint ulFlags, out IntPtr lppUnk);
		int SetProps(uint cValues, IntPtr lpPropArray, out IntPtr lppProblems);
		int DeleteProps(IntPtr lpPropTagArray, out IntPtr lppProblems);
		int CopyTo(uint ciidExclude, ref Guid rgiidExclude, IntPtr lpExcludeProps, uint ulUIParam,
			IntPtr lpProgress, ref Guid lpInterface, IntPtr lpDestObj, uint ulFlags, out IntPtr lppProblems);
		int CopyProps(IntPtr lpIncludeProps, uint ulUIParam, IntPtr lpProgress, ref Guid lpInterface,
			IntPtr lpDestObj, uint ulFlags, out IntPtr lppProblems);
		int GetNamesFromIDs(out IntPtr lppPropTags, ref Guid lpPropSetGuid, uint ulFlags,
			out uint lpcPropNames, out IntPtr lpppPropNames);
		int GetIDsFromNames(uint cPropNames, ref IntPtr lppPropNames, uint ulFlags, out IntPtr lppPropTags);
	}

	[
	ComImport, ComVisible(false),
	InterfaceType(ComInterfaceType.InterfaceIsIUnknown), 
	Guid("00020309-0000-0000-C000-000000000046")
	]
	public interface IAddrBook
	{
		int GetLastError(int hResult, uint ulFlags, out IntPtr lppMAPIError);
		int SaveChanges(uint ulFlags);
		int GetProps(IntPtr lpPropTagArray, uint ulFlags, out uint lpcValues, out IntPtr lppPropArray);
		int GetPropList(uint ulFlags, out IntPtr lppPropTagArray);
		int OpenProperty(uint ulPropTag, ref Guid lpiid, uint ulInterfaceOptions, uint ulFlags, out IntPtr lppUnk);
		int SetProps(uint cValues, IntPtr lpPropArray, out IntPtr lppProblems);
		int DeleteProps(IntPtr lpPropTagArray, out IntPtr lppProblems);
		int CopyTo(uint ciidExclude, ref Guid rgiidExclude, IntPtr lpExcludeProps, uint ulUIParam,
			IntPtr lpProgress, ref Guid lpInterface, IntPtr lpDestObj, uint ulFlags, out IntPtr lppProblems);
		int CopyProps(IntPtr lpIncludeProps, uint ulUIParam, IntPtr lpProgress, ref Guid lpInterface,
			IntPtr lpDestObj, uint ulFlags, out IntPtr lppProblems);
		int GetNamesFromIDs(out IntPtr lppPropTags, ref Guid lpPropSetGuid, uint ulFlags,
			out uint lpcPropNames, out IntPtr lpppPropNames);
		int GetIDsFromNames(uint cPropNames, ref IntPtr lppPropNames, uint ulFlags, out IntPtr lppPropTags);

		int OpenEntry(uint cbEntryID, IntPtr lpEntryID,	IntPtr lpInterface, uint ulFlags, out uint lpulObjType, out IntPtr lppUnk);
		int CompareEntryIDs(uint cbEntryID1, IntPtr lpEntryID1, uint cbEntryID2, IntPtr lpEntryID2, uint ulFlags, out uint lpulResult);
		int Advise(uint cbEntryID, IntPtr lpEntryID, uint ulEventMask, IntPtr lpAdviseSink, out uint lpulConnection);
		int Unadvise(uint ulConnection);
		int CreateOneOff([MarshalAs(UnmanagedType.LPWStr)] string lpszName, [MarshalAs(UnmanagedType.LPWStr)] string lpszAdrType,
			[MarshalAs(UnmanagedType.LPWStr)] string lpszAddress, uint ulFlags, out uint lpcbEntryID, out IntPtr lppEntryID);
		int NewEntry(uint ulUIParam, uint ulFlags, uint cbEIDContainer, IntPtr lpEIDContainer, uint cbEIDNewEntryTpl, IntPtr lpEIDNewEntryTpl, out uint lpcbEIDNewEntry, out IntPtr lppEIDNewEntry);
		int ResolveName(uint ulUIParam,	uint ulFlags, [MarshalAs(UnmanagedType.LPWStr)] string lpszNewEntryTitle, IntPtr lpAdrList);
		int Address(out uint lpulUIParam, IntPtr lpAdrParms, out IntPtr lppAdrList);
		int Details(out uint lpulUIParam, IntPtr lpfnDismiss, IntPtr lpvDismissContext, uint cbEntryID, IntPtr lpEntryID,
			IntPtr lpfButtonCallback, IntPtr lpvButtonContext, [MarshalAs(UnmanagedType.LPWStr)] string lpszButtonText, uint ulFlags);
		int RecipOptions(uint ulUIParam, uint ulFlags, IntPtr lpRecip);
		int QueryDefaultRecipOpt([MarshalAs(UnmanagedType.LPWStr)] string lpszAdrType, uint ulFlags, out uint lpcValues, out IntPtr lppOptions);
		int GetPAB(out uint lpcbEntryID, out IntPtr lppEntryID);
		int SetPAB(uint cbEntryID, IntPtr lpEntryID);
		int GetDefaultDir(out uint lpcbEntryID, out IntPtr lppEntryID);
		int SetDefaultDir(uint cbEntryID, IntPtr lpEntryID);
		int GetSearchPath(uint ulFlags, out IntPtr lppSearchPath);
		int SetSearchPath(uint ulFlags, IntPtr lpSearchPath);
		int PrepareRecips(uint ulFlags, IntPtr lpSPropTagArray, IntPtr lpRecipList);
	}
}
