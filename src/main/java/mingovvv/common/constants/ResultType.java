package mingovvv.common.constants;

import org.springframework.http.HttpStatus;

public interface ResultType {

    String getCode();
    String getMessage();
    HttpStatus getHttpStatus();

}
