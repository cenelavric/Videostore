package tv.beenius.videostore.exception;

import java.util.Set;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

@ApplicationException(rollback = true)
@SuppressWarnings("serial")
public class EjbConstraintViolationException extends ConstraintViolationException {

  public EjbConstraintViolationException(Set<? 
      extends ConstraintViolation<?>> constraintViolations) {
    super(constraintViolations);
  }
}
