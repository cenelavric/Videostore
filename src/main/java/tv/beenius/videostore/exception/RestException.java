package tv.beenius.videostore.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SuppressWarnings("serial")
public class RestException extends WebApplicationException {

  public RestException(String message, Response.Status status) {
    super(Response.status(status).entity(message).type(MediaType.TEXT_PLAIN).build());
  }

}
