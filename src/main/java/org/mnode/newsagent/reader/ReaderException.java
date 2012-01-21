package org.mnode.newsagent.reader;

public class ReaderException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2617761734775297306L;

	public ReaderException(String message) {
		super(message);
	}

	public ReaderException(Throwable cause) {
		super(cause);
	}

	public ReaderException(String message, Throwable cause) {
		super(message, cause);
	}

}
