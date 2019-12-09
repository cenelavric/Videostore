package tv.beenius.videostore.exception;

import javax.ejb.ApplicationException;
import javax.validation.ValidationException;

@ApplicationException(rollback = true)
@SuppressWarnings("serial")
public class EjbValidationException extends ValidationException {

  public EjbValidationException(String string) {
    super(string);
  }
  
}
