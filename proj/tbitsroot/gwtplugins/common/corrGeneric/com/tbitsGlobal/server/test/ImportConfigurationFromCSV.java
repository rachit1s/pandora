package corrGeneric.com.tbitsGlobal.server.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class ImportConfigurationFromCSV 
{
	public static String DefaultFieldSeparator = ";";
	public static String DefaultValueSeparator = ",";
	
	public static void main(String argv[])
	{
		try
		{
			if( argv.length < 3 )
			{
				System.out.println("Usage : " + getUsage() );
				return ;
			}	

			System.out.println("your parameters : " + argv );
			
			String fileName = argv[0];
			String tableName = argv[1];
			String noOfColumns = argv[2];
			int nc = 0 ;
			try
			{
				nc = Integer.parseInt(noOfColumns);
			}
			catch(NumberFormatException nfe)
			{
//				nfe.printStackTrace();
				System.out.println("3rd parameter should a integer telling the number of columns in the table.");
				System.out.println("Usage : " + getUsage());
				return;
			}
			
			String fs = DefaultFieldSeparator ; // default field separator
			String vs = DefaultValueSeparator;
			if( argv.length > 3 )
				fs = argv[3];
			if( argv.length > 4 )
				vs = argv[4];
				
			File f = new File(argv[0]);
			
			BufferedReader br = new BufferedReader( new FileReader(f));
			
			String insertStatement = "insert into " + tableName + " values (" ;
			String row = null;
			int rowNo = 0 ;
			while( (row = br.readLine()) != null )
			{
				rowNo++;
				ArrayList<String> vals =  splitToArrayList(row, fs);// row.split(fs);
				if( vals.size() != nc )
				{
					System.out.println("Incorrect format of row no. " + rowNo + ". It has " + vals.size() + "" +
							" number of colums. Where as expected number of columns are : " + nc + ".\n\nThe row is : " + row );
					return;
				}
				
				
				ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
				int max[] = new int[nc];
				for( int i = 0 ; i < nc ; i++ )
				{
					ArrayList<String> v = splitToArrayList(vals.get(i),vs );
					values.add(v);
					max[i] = v.size();
				}
				
				int size[] = new int[nc];			                     
				int cc = 0 ;
				while(size[0] <= max[0])
				{
					if( size[cc] <= max[cc] && cc < nc )
						cc++;
					if( cc == nc )
					{
						for( int i = 0 ; i < max[cc] ; i++)
						{
							String str = print(values,0,nc-2,size);
							str += "," + values.get(cc).get(i);
							System.out.println(str);
						}
						
						int p = cc -1 ;
						while(p >= 0 && size[p] == max[p] )
						{
							p-- ;
						}
						
						size[p]++;
					}
				}
//				for( ArrayList<String> colVals : values )
//				{
//					for(String vl : colVals )
//					{
//						
//					}
//				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static String print(ArrayList<ArrayList<String>> values, int i,	int j, int[] size) 
	{
		String str = "" ;
		for( int x = i ; x <= j ; x++ )
		{
			ArrayList<String> arr = values.get(x);
			str += arr.get(size[x]) + ",";
		}
		
		return str;
	}

	public static ArrayList<String> splitToArrayList(String str, String separator)
	{
		if( null == str )
			return null;
		
		ArrayList<String> strings = new ArrayList<String>();
		String [] vars = str.split(separator);
		for( String var : vars )
		{
			var = var.trim();
			strings.add(var);
		}
		
		return strings;
	}
	private static String getUsage() {
		return "java " + ImportConfigurationFromCSV.class.getName() + " CSVFileName DBTableName NumberOfColumnsInTable fieldSeparator valueSeparator\n" +
				"Ex : java " + ImportConfigurationFromCSV.class.getName() + " my_onbehalfCSV.csv corr_on_behalf_map 6 ; ,"   ;
	}
}
