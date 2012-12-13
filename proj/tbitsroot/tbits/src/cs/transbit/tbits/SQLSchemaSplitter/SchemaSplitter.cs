using System;
using System.Text.RegularExpressions;
using System.Text;
using System.IO;
using System.Collections;

namespace SQLSchemaSplitter
{
	
	/// <summary>
	/// Summary description for SchemaSplitter.
	/// </summary>
	public class SchemaSplitter
	{
		public const string SPLIT_PATTERN = "SET ANSI_NULLS (ON|OFF)";
		public const String OBJ_PATTERN = @"IF NOT EXISTS \(SELECT \* FROM dbo.sysobjects WHERE id = OBJECT_ID\(N'\[dbo\]\.\[(.+)\]'\)";

		public string Schema = "";
		public SchemaSplitter()
		{
			
		}
		public SchemaSplitter(string schema) : this()
		{
			Schema = schema;
		}
		public string[] split()
		{
			return this.split(SPLIT_PATTERN);
		}
		public string[] split(string pattern)
		{
			//string [] all = Regex.Split(Schema,pattern,RegexOptions.Multiline);
			ArrayList list = new ArrayList();
			StringReader tr = new StringReader(Schema);
			StringBuilder chunk = null;
			String line;
			while((line = tr.ReadLine()) != null)
			{
				if(isDelimiterMatching(line))
				{
					if(chunk != null)
						list.Add(chunk.ToString());
					//chunkStart = true;
					chunk = new StringBuilder(line);
				}
				else
				{
					if(chunk == null)
					{
						throw new Exception("The sql script file doesnt start with Split Pattern: '" + SPLIT_PATTERN + "'");
					}
					chunk.Append(Environment.NewLine)
						.Append(line);
				}
			}
			if(chunk != null)
				list.Add(chunk.ToString());

			return (string[])list.ToArray(typeof(string));
		}
		bool isDelimiterMatching(String line)
		{
			return Regex.IsMatch(line, SPLIT_PATTERN);
		}
		public static string getName(String frag)
		{
			Match m = Regex.Match(frag, OBJ_PATTERN);
			return m.Groups[1].Value;
		}

		public static void Usage()
		{
			Console.Error.WriteLine("SchemaSplit <output_base_folder> <input_file_name>");
			Console.Error.WriteLine("SchemaSplit <output_base_folder>");
			Console.Error.WriteLine("SchemaSplit ");
			Console.Error.WriteLine("If output_base_folder is not supplied, the current folder is used.");
			Console.Error.WriteLine("If input_file_name is not supplied, stdin is considered");
		}

		public static void Main(String[] args)
		{
			if(args.Length > 2)
			{
				Console.Error.WriteLine("Invalid number of arguments.");
				Usage();
			}
			else if(args.Length == 2)
			{
				String folderName = args[0];
				
				StreamReader streamReader = null;
				try
				{
					streamReader = new StreamReader(args[1]);
					SchemaSplit(args[0], streamReader);
				}
				catch(Exception exp)
				{
					Console.Error.WriteLine("Error while splitting '" + args[1] + "'. " + exp.Message);
					Console.Error.WriteLine(exp.StackTrace);
					return;
				}
				finally
				{
					if(streamReader != null)
						streamReader.Close();
				}
			}
			else if(args.Length == 1)
			{
				SchemaSplit(args[0], Console.In);
			}
			else if(args.Length == 0)
			{
				SchemaSplit(args[0], Console.In);
			}
			
		}
		public static void SchemaSplit(String baseOutputFolderName, TextReader input)
		{
			if(baseOutputFolderName.Length == 0)
			{
				Console.Error.WriteLine("Invalid base output folder: " + baseOutputFolderName);
				return;
			}
			if(!Directory.Exists(baseOutputFolderName))
			{
				Directory.CreateDirectory(baseOutputFolderName);
				Console.Error.WriteLine("Created base output folder: " + baseOutputFolderName);
			}
			String inputText = input.ReadToEnd();
			SchemaSplitter schemaSplit = new SchemaSplitter(inputText);
			foreach(String s  in schemaSplit.split())
			{
				if(s.Trim().Length == 0)
					continue;
				String fileName = SchemaSplitter.getName(s);
				String completeName = baseOutputFolderName + "\\" + fileName;
				TextWriter tx = null;
				try
				{
					if(File.Exists(completeName))
					{
						if(input != Console.In)
						{
							Console.Error.WriteLine("File '" + completeName 
								+ "' already exists.");
							char inputChar;
							do
							{
								Console.WriteLine("[O]verrite/[C]ancel/[S]kip?");
								inputChar = Char.ToLower((char)Console.Read());
							}
							while((inputChar != 'o') && (inputChar != 'c')	&& (inputChar != 's'));
							
							if(inputChar == 'o')
							{
								Console.Error.WriteLine("Overwriting..");
								File.Delete(completeName);
							}
							else if(inputChar == 'c')
							{
								Console.Error.WriteLine("Aborting..");
								break;
							}
							else if(inputChar == 's')
							{
								Console.Error.WriteLine("Skipping and continuing.");
								continue;
							}							

						}
						else
						{
							Console.Error.WriteLine("File '" + completeName + "' already exists. Aborting!");
						}
						return;
					}
					tx  = new StreamWriter(completeName,false);
					tx.WriteLine(s);
					Console.Error.WriteLine("Saved '" + completeName + "'");
				}
				catch(Exception exp)
				{
					Console.Error.WriteLine("Unable to write data to '" + completeName + "'.");
					Console.Error.WriteLine("Exception: " + exp.Message + Environment.NewLine +  exp.StackTrace);
					return;
				}
				finally
				{
					if(tx != null)
						tx.Close();
				}
			}
		}
	}
}
