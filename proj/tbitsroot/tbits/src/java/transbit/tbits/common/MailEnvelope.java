package transbit.tbits.common;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;

import transbit.tbits.domain.Request;

enum MailEnvelopeStatus
{
	Commited,
	COMMIT_PENDING,
	ROLLED_BACK
}

public class MailEnvelope {
	private Request request;
	private MailEnvelopeStatus mailEnvelopeStatus; 
	public MailEnvelope(Request request, MailEnvelopeStatus status)
	{
		this.request = request;
		this.setMailEnvelopeStatus(status);
	}
	public MailEnvelope(Request request)
	{
		this(request, MailEnvelopeStatus.COMMIT_PENDING);
	}
	
	public MailEnvelopeStatus getMailEnvelopeStatus() {
		return mailEnvelopeStatus;
	}
	
	public Request getRequest() {
		return request;
	}
	

	public void setMailEnvelopeStatus(MailEnvelopeStatus mailEnvelopeStatus) {
		this.mailEnvelopeStatus = mailEnvelopeStatus;
	}
}
