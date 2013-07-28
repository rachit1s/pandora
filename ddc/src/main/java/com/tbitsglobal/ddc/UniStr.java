import java.io.OutputStreamWriter;
import java.io.PrintStream;


public class UniStr {
	public static void main(String[] args) throws Exception {
		String str = new String("A\u2029B");
		System.out.println(str);
		System.out.println(str.length());
		for(int j=0;j<str.length();j++){
			System.out.printf("%c",str.codePointAt(j));
			System.out.println(str.codePointAt(j));
		}
		
//		OutputStreamWriter out=new OutputStreamWriter(System.out,"UTF-8");
		PrintStream out = new PrintStream(System.out, true, "UTF-8");
	    out.println(str);
	    out.println("\u2297\u0035\u039e\u322F\u2029\u5193");
	    System.out.println(str);
	    System.out.println(Character.toChars(0x2028));
	}
}
