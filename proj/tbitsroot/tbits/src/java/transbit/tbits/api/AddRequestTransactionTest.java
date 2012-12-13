package transbit.tbits.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;

import javax.mail.MessagingException;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

public class AddRequestTransactionTest extends TestCase {
//	Wiser wiser = null;
//
//	@Before
//	public void setUp() throws Exception {
//
//		if (wiser == null) {
//			wiser = new Wiser();
//			wiser.setPort(2500);
//			wiser.setSession(Mail.getSession());
//			wiser.start();
//		}
//
//	}
//
//	@After
//	public void tearDown() throws Exception {
//
//		if (wiser != null) {
//			wiser.stop();
//			wiser = null;
//		}
//
//	}

	// Test if addition is Working
	public void testAddRequest() throws APIException, MessagingException, TBitsException {

		// start mail server

		// generate a random subject
		String subj = getRandomString();

		// prepare hash
		Hashtable<String, String> requestHash = new Hashtable<String, String>();
		requestHash.put(Field.BUSINESS_AREA, "tbits");
		requestHash.put(Field.SUBJECT, subj);
		requestHash.put(Field.USER, "root");

		// add a request without transactions.
		AddRequest addRequest = new AddRequest();
		Request request = addRequest.addRequest(requestHash);

		System.out.println("Added The Requests.");

		// check tbits for a request with the random subject
		Request addedrequest = null;
		try {
			System.out.println("tring to get the request details");
			addedrequest = Request.lookupBySystemIdAndRequestId(request
					.getSystemId(), request.getRequestId());
			System.out.println("Got the request details from the database");
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertNotNull(addedrequest);
		assertEquals(addedrequest.getSubject(), request.getSubject());
		assertEquals(request.getSubject(), subj);

//		// wait for some time
//		try {
//			System.out.println("Going to sleep");
//			Thread.sleep(10000);
//			//System.out.println("Requesting stop.");
//			//MailTransportEngine.getInstance().requestStop();
//			//System.out.println("Requested stop.");
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

//		// check if the mail is recieved
//		List<WiserMessage> messages = wiser.getMessages();
//		assertTrue(messages.size() > 0);
//		MimeMessage mm = messages.get(0).getMimeMessage();
//		assertTrue(mm.getSubject().endsWith(subj));

		// shutdown mail server

	}

	public void testMultipleAddRequest() throws MessagingException, TBitsException
	{
		try {
			BusinessArea.lookupBySystemId(1);
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int[] nreqs = new int[]{ 50, 100, 150};
		long[][] timeConsumed = new long[3][3];
		for(int i=0; i< nreqs.length; i++)
		{
			for(int j=0; j < 3; j++)
			{
				long time = addNRequests(nreqs[i]);
				timeConsumed[i][j] = time;
				System.out.println("##Time: (" + nreqs[i] + "): " + time);
			}
		}
		
		System.out.println("Number of requests\tTime taken (msecs)");
		for(int i = 0; i < 3; i ++)
		{
			for(int j=0; j < 3; j++)
			{
				System.out.println(nreqs[i] + "\t" + timeConsumed[i][j]);
			}
		}
	}
	public long addNRequests(int n) throws TBitsException
	{

		try {
			BusinessArea.lookupBySystemId(1);
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Date start = new Date();
		int TOTAL_NUM = n;
		// start mail server

		ArrayList<String> subjects = new ArrayList<String>();
		// generate a random subject
		for (int i = 0; i < TOTAL_NUM; i++) {
			String subject = getRandomString();
			subjects.add(subject);
			// prepare hash
			Hashtable<String, String> requestHash = new Hashtable<String, String>();
			requestHash.put(Field.BUSINESS_AREA, "tbits");
			requestHash.put(Field.SUBJECT, subject);
			requestHash.put(Field.USER, "root");

			Request request = null;
			// add a request without transactions.
			AddRequest addRequest = new AddRequest();
			try {
				request = addRequest.addRequest(requestHash);
			} catch (APIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			assertNotNull(request);
			assertEquals(request.getSubject(), subject);
		}

//		// wait for some time
//		try {
//			System.out.println("Going to sleep");
//			Thread.sleep(TOTAL_NUM * 4000);
//			System.out.println("Requesting stop.");
//			//MailTransportEngine.getInstance().requestStop();
//			System.out.println("Requested stop.");
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		Date end = new Date();
		//System.out.println("Differece (for " + TOTAL_NUM + "): " + (end.getTime() - start.getTime()));
		return end.getTime() - start.getTime();
		
//		// check if the mail is recieved
//		List<WiserMessage> messages = wiser.getMessages();
//		assertTrue(messages.size() >= 5);
//
//		ArrayList<String> subjectsList = new ArrayList<String>();
//		for (String s : subjects) {
//			boolean found = false;
//			for (WiserMessage w : messages) {
//				String ws = w.getMimeMessage().getSubject();
//				if (ws.endsWith(s)) {
//					found = true;
//					break;
//				}
//			}
//			assertTrue(found);
//		}
	}
	// Test if addition is Working
	public void ignoretestMultipleAddRequestThread() throws APIException,
			MessagingException {

		int TOTAL_NUM = 10;
		// start mail server

		// generate a random subject
		String[] subjects = new String[TOTAL_NUM];
		RequestAdder[] adders = new RequestAdder[TOTAL_NUM];
		for (int i = 0; i < TOTAL_NUM; i++) {
			subjects[i] = getRandomString();
			RequestAdder adder = new RequestAdder(subjects[i]);
			adders[i] = adder;
			adder.start();
		}

		Request[] requests = new Request[TOTAL_NUM];
		for (int i = 0; i < TOTAL_NUM; i++) {
			try {
				adders[i].join();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			requests[i] = adders[i].request;
			assertNotNull(requests[i]);
			// check tbits for a request with the random subject
			Request addedrequest = null;
			try {
				System.out.println("tring to get the request details");
				addedrequest = Request.lookupBySystemIdAndRequestId(requests[i]
						.getSystemId(), requests[i].getRequestId());
				System.out.println("Got the request details from the database");
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			assertNotNull(addedrequest);
			assertEquals(addedrequest.getSubject(), requests[i].getSubject());
			assertEquals(requests[i].getSubject(), subjects[i]);
		}

//		// wait for some time
//		try {
//			System.out.println("Going to sleep");
//			Thread.sleep(TOTAL_NUM * 4000);
//			System.out.println("Requesting stop.");
//			//MailTransportEngine.getInstance().requestStop();
//			System.out.println("Requested stop.");
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

//		// check if the mail is recieved
//		List<WiserMessage> messages = wiser.getMessages();
//		assertTrue(messages.size() >= 5);
//
//		ArrayList<String> subjectsList = new ArrayList<String>();
//		for (String s : subjects) {
//			boolean found = false;
//			for (WiserMessage w : messages) {
//				String ws = w.getMimeMessage().getSubject();
//				if (ws.endsWith(s)) {
//					found = true;
//					break;
//				}
//			}
//			assertTrue(found);
//		}

		// shutdown mail server

	}

	public static String getRandomString() {

		Random r = new Random();
		String token = Long.toString(Math.abs(r.nextLong()), 36);

		return token;

	}

	// Test if the rollback is working
	public void testRollback() {

		// add request

	}

	public static void main(String[] args) {

		//TestRunner tr = new TestRunner();
		TestRunner.run(AddRequestTransactionTest.class);

	}
}

class RequestAdder extends Thread {
	private String subject;
	public Request request = null;

	public RequestAdder(String subject) {
		super();
		this.subject = subject;
	}

	public void run() {
		// prepare hash
		Hashtable<String, String> requestHash = new Hashtable<String, String>();
		requestHash.put(Field.BUSINESS_AREA, "tbits");
		requestHash.put(Field.SUBJECT, subject);
		requestHash.put(Field.USER, "root");

		// add a request without transactions.
		AddRequest addRequest = new AddRequest();
		try {
			request = addRequest.addRequest(requestHash);
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
