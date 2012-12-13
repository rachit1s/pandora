package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.TextDataType;
import transbit.tbits.domain.User;
import transbit.tbits.mail.IMailPreProcessor;

public class TestChangingDescriptionInEmail implements IMailPreProcessor {

	@Override
	public void executeMailPreProcessor(User user, Request request,
			ArrayList<Action> actionList,
			Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash,
			Hashtable<String, Integer> permissions) 
	{
		try 
		{
			/*
			File htmlFile = new File("/home/tbits/Downloads/Tenis.html");
			FileReader fr = new FileReader(htmlFile);
			BufferedReader br = new BufferedReader(fr);
			
			char cbuf[] = new char[1024];
			
			StringBuffer sb = new StringBuffer();
			
			while(br.read(cbuf) != -1 )
				sb.append(cbuf);
			*/
			Action currentAction = null;
			for(Iterator<Action> iter = actionList.iterator(); iter.hasNext() ;)
			{
				Action action = iter.next();
				System.out.println("current Action id : " + action.getActionId() );
				if( action.getActionId() == request.getMaxActionId() )
				{
					currentAction = action;
					break;
				}
			}
			if( null == currentAction )
				return ;
				
			currentAction.setDescription(currentAction.getDescription() + "<html>			<body> 			<h1>My First Heading</h1>		<p>My first paragraph.</p>			</body>			</html>" );
			
//			System.out.println("STARTDESCRIPTION");
//			System.out.println(request.get(Field.DESCRIPTION));
//			System.out.println("ENDDESCRIPTION");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String getMailPreProcessorName() {
		return "Changed description";
	}

	@Override
	public double getMailPreProcessorOrder() {
		return 0;
	}
}
