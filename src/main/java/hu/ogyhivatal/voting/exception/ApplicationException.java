package hu.ogyhivatal.voting.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationException extends RuntimeException {

	private final ErrorCode errorCode;
	private final HttpStatus httpStatus;

	public ApplicationException(ErrorCode errorCode, HttpStatus httpStatus, String message) {
		super(message);
		this.errorCode = errorCode;
		this.httpStatus = httpStatus;
	}
}
