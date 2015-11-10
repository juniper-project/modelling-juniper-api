package org.modelio.juniper;

@SuppressWarnings("serial")
public class CommunicationException extends Exception {

	public CommunicationException() {
		super();
	}

	public CommunicationException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CommunicationException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommunicationException(String message) {
		super(message);
	}

	public CommunicationException(Throwable cause) {
		super(cause);
	}

}
