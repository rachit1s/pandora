package corrGeneric.com.tbitsGlobal.server.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TempSqlScriptUserMap {
	public static void main(String argv[]) throws IOException
	{
		File file = new File(argv[0]);
		
		BufferedReader br = new BufferedReader( new FileReader(file));
		String tableName = "corr_user_map" ;
		
		String insertStatement = "insert into " + tableName + " values (" ;
		String fs = ";" ;
		String vs = "," ;
		int nc = 8 ;
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
			
			ArrayList<ArrayList<String>> xyz = new ArrayList<ArrayList<String>>();
			for( String colValue : vals )
			{
				ArrayList<String> v2 = splitToArrayList(colValue, vs);
				xyz.add(v2);
			}
			
			String str = "" ;
			for( int a = 0 ; a < xyz.get(0).size() ; a++ )
			{
				for( int b = 0 ; b < xyz.get(1).size() ; b++ )
				{
					for( int c = 0 ; c < xyz.get(2).size() ; c++ )
					{
						for( int d = 0 ; d < xyz.get(3).size() ; d++ )
						{
							for( int e = 0 ; e < xyz.get(4).size() ; e++ )
							{
								for( int f = 0 ; f < xyz.get(5).size() ; f++ )
								{
									for( int g = 0 ; g < xyz.get(6).size() ; g++ )
									{
										str += insertStatement ;
										if( xyz.get(0).get(a).equals("") )
											str += "null,";
										else str += "'" + xyz.get(0).get(a) + "',";

										if( xyz.get(1).get(b).equals("") )
											str += "null,";
										else str += "'" + xyz.get(1).get(b) + "',";

										if( xyz.get(2).get(c).equals("") )
											str += "null,";
										else str += "'" + xyz.get(2).get(c) + "',";

										if( xyz.get(3).get(d).equals("") )
											str += "null,";
										else str += "'" + xyz.get(3).get(d) + "',";

										if( xyz.get(4).get(e).equals("") )
											str += "null,";
										else str += "'" + xyz.get(4).get(e) + "'," ;

										if( xyz.get(5).get(f).equals("") )
											str += "null,";
										else str += "'" + xyz.get(5).get(f) + "'," ;
										
										if( xyz.get(6).get(g).equals("") )
											str += "null";
										else str += "'" + xyz.get(6).get(g) + "'" ;
										
										str += ") ;\n";
									}
								}
							}
						}
					}
				}
			}
		}
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
}
