package tv.beenius.videostore.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import tv.beenius.videostore.exception.EjbConstraintViolationException;
import tv.beenius.videostore.exception.EjbValidationException;
import tv.beenius.videostore.model.Actor;
import tv.beenius.videostore.model.Image;
import tv.beenius.videostore.model.Movie;
import tv.beenius.videostore.service.RegisterService;
import tv.beenius.videostore.util.ImageUtil;

@Path("/registration")
@RequestScoped
public class RegistrationRestService {
  
  Logger logger = Logger.getLogger(getClass());
 
  private static String MULTIPART_ATTRIBUTES = "attributes";
  private static String MULTIPART_IMAGE      = "image";
  
  @Inject 
  RegisterService registration;
  
  @Inject
  ImageUtil imageUtil;
  
  // Create entities by POST.
  
  /**
   * Posts new actor with associated movies via 
   * {@link tv.beenius.videostore.service.RegisterService#registerActor(Actor)}.
   * 
   * <p>@param actor Registered actor.
   * @return Response contains registered actor 
   *         or error list with status BAD_REQUEST or INTERNAL_SERVER_ERROR.
   */
  @POST
  @Path("/actors") 
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postActor(Actor actor, @Context UriInfo uriInfo) {
    
    Response.ResponseBuilder builder = null;
    Map<String, String> responseObj = new HashMap<>();
    
    try {
      Actor registeredActor = registration.registerActor(actor);  
             
      UriBuilder locationUriBuilder = uriInfo.getAbsolutePathBuilder();
      locationUriBuilder.path(registeredActor.getId().toString());
      
      builder = Response.created(locationUriBuilder.build()).entity(registeredActor);
    } catch (EjbConstraintViolationException cve) {
      responseObj = createViolationMap(cve.getConstraintViolations()); 
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (EjbValidationException ve) {
      responseObj.put("Actor", ve.getMessage());
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (RuntimeException rte) {
      builder = Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }
    
    return builder.build();
  }
  
  /**
   * Posts new movie with associated actors via 
   * {@link tv.beenius.videostore.service.RegisterService#registerMovie(Movie)}.
   * 
   * <p>@param movie Registered movie.
   * @return Response contains registered movie 
   *         or error list with status BAD_REQUEST or INTERNAL_SERVER_ERROR.
   */
  @POST
  @Path("/movies") 
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postMovie(Movie movie, @Context UriInfo uriInfo) {
    
    Response.ResponseBuilder builder = null;
    Map<String, String> responseObj = new HashMap<>();
    
    try {
      Movie registeredMovie = registration.registerMovie(movie);  
             
      UriBuilder locationUriBuilder = uriInfo.getAbsolutePathBuilder();
      locationUriBuilder.path(registeredMovie.getImdbId());
      
      builder = Response.created(locationUriBuilder.build()).entity(registeredMovie);
    } catch (EjbConstraintViolationException cve) {
      responseObj = createViolationMap(cve.getConstraintViolations()); 
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (EjbValidationException ve) {
      responseObj.put("Movie", ve.getMessage());
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (RuntimeException rte) {
      builder = Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }
    
    return builder.build();
  }

  /**
   * Posts new cast between movie and actor via 
   * {@link tv.beenius.videostore.service.RegisterService#registerCast(String, Long)}.
   * 
   * @param imdbId Movie identifier.
   * @param id Actor identifier.
   * @return Response contains registered movie 
   *         or error list with status BAD_REQUEST or INTERNAL_SERVER_ERROR.
   */
  @POST
  @Path("/movies/{imdbId}/actors/{id}") 
  @Produces(MediaType.APPLICATION_JSON)
  public Response postCast(
      @PathParam("imdbId") String imdbId, 
      @PathParam("id") Long id) {
    
    Response.ResponseBuilder builder = null;
    Map<String, String> responseObj = new HashMap<>();
    
    try {
      registration.registerCast(imdbId, id);  
      
      builder = Response.status(Response.Status.CREATED);
    } catch (EjbConstraintViolationException cve) {
      responseObj = createViolationMap(cve.getConstraintViolations()); 
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (EjbValidationException ve) {
      responseObj.put("Movie", ve.getMessage());
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (RuntimeException rte) {
      builder = Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }
    
    return builder.build();
  }
  
  /**
   * Posts new movie image via 
   * {@link tv.beenius.videostore.service.RegisterService#registerMovieImage(String, Image)}.
   * 
   * <p>@param imdbId Movie identifier.
   * @param multiPart Image file attributes and content.
   * @return Response contains id and description of a registered image 
   *         or error list with status BAD_REQUEST or INTERNAL_SERVER_ERROR.
   */
  @POST
  @Path("/movies/{imdbId}/images") 
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postMovieImage(
      @PathParam("imdbId") String imdbId, 
      MultipartFormDataInput  multiPart) {
    
    Response.ResponseBuilder builder = null;
    Map<String, String> responseObj = new HashMap<>();
    
    try {
      Image image = readImageFromMultiPart(multiPart);
      
      Image registeredImage = registration.registerMovieImage(imdbId, image);  
      
      responseObj.put("id", registeredImage.getId().toString());
      responseObj.put("description", registeredImage.getDescription());
      builder = Response.ok().entity(responseObj);
    } catch (EjbConstraintViolationException cve) {
      responseObj = createViolationMap(cve.getConstraintViolations()); 
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (EjbValidationException ve) {
      responseObj.put("Image", ve.getMessage());
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (IOException ioe) {
      responseObj.put("Image", ioe.getMessage());
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (RuntimeException rte) {
      builder = Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }
    
    return builder.build();
  }
   
  // Retrieve entities by GET.
  
  /**
   * Retrieves an actor by Id provided via 
   * {@link tv.beenius.videostore.service.RegisterService#findActorById(Long)}.
   * 
   * <p>@param id Actor identifier.
   * @return Response contains entity when found or error list with
   *         status NOT_FOUND when not found or
   *         status BAD_REQUEST with a list of constraint violations or
   *         status INTERNAL_SERVER_ERROR on server error.
   */
  @GET
  @Path("/actors/{id}") 
  @Produces(MediaType.APPLICATION_JSON)
  public Response getActorById(@PathParam("id") Long id) {
    
    Response.ResponseBuilder builder = null;
    Map<String, String> responseObj = new HashMap<>();

    Optional<Actor> optionalActor;

    try {      
      registration.validateActorId(id);
      optionalActor = registration.findActorById(id);

      if (optionalActor.isPresent()) {
        builder = Response.ok().entity(optionalActor.get());
      } else {
        builder = Response
            .status(Response.Status.NOT_FOUND);
      }
    } catch (EjbConstraintViolationException cve) {
      responseObj = createViolationMap(cve.getConstraintViolations()); 
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (RuntimeException rte) {
      builder = Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }
   
    return builder.build();
  }
  
  /**
   * Retrieves a movie by imdbId provided via 
   * {@link tv.beenius.videostore.service.RegisterService#findMovieById(String)}.
   * 
   * <p>@param imdbId Movie identifier.
   * @return Response contains entity when found or error list with
   *         status NOT_FOUND when not found or
   *         status BAD_REQUEST with a list of constraint violations or
   *         status INTERNAL_SERVER_ERROR on server error.
   */
  @GET
  @Path("/movies/{imdbId}") 
  @Produces(MediaType.APPLICATION_JSON)
  public Response getMovieByImdbId(@PathParam("imdbId") String imdbId) {
    
    Response.ResponseBuilder builder = null;
    Map<String, String> responseObj = new HashMap<>();

    Optional<Movie> optionalMovie;

    try {      
      registration.validateImdbId(imdbId);
      optionalMovie = registration.findMovieById(imdbId);

      if (optionalMovie.isPresent()) {
        builder = Response.ok().entity(optionalMovie.get());
      } else {
        builder = Response
            .status(Response.Status.NOT_FOUND);
      }
    } catch (EjbConstraintViolationException cve) {
      responseObj = createViolationMap(cve.getConstraintViolations()); 
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (RuntimeException rte) {
      builder = Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }
   
    return builder.build();
  }
 
  /**
   * Retrieves a movie image by imdbId and id provided via 
   * {@link tv.beenius.videostore.service.RegisterService#findMovieImageById(Long)}.
   * 
   * <p>@param imdbId Movie identifier.
   * @param id Image identifier
   * @return Response contains entity when found or error list with
   *         status NOT_FOUND when not found or
   *         status BAD_REQUEST with a list of constraint violations or
   *         status INTERNAL_SERVER_ERROR on server error.
   */
  @GET
  @Path("/movies/{imdbId}/images/{id}") 
  @Produces(MediaType.APPLICATION_JSON)
  public Response getMovieImageById(
      @PathParam("imdbId") String imdbId, 
      @PathParam("id") Long id) {
    
    Response.ResponseBuilder builder = null;
    Map<String, String> responseObj = new HashMap<>();

    Optional<Image> optionalImage;

    try {      
      registration.validateImdbId(imdbId);
      registration.validateEntityIdentifier(id, true);
      
      optionalImage = registration.findMovieImageById(imdbId, id);

      if (optionalImage.isPresent()) {
        builder = Response.ok().entity(optionalImage.get().toString());
      } else {
        builder = Response
            .status(Response.Status.NOT_FOUND);
      }
    } catch (EjbConstraintViolationException cve) {
      responseObj = createViolationMap(cve.getConstraintViolations()); 
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (EjbValidationException ve) {
      responseObj.put("MovieImage", ve.getMessage());
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (RuntimeException rte) {
      builder = Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }
   
    return builder.build();
  }

  /**
   * Retrieves a page of actors using offset pagination approach and optional filtering via 
   * {@link tv.beenius.videostore.service.RegisterService#findPageOfActorsByName(int, int, String)}
   * or {@link tv.beenius.videostore.service.RegisterService#findPageOfActors(int, int)}.
   * Records are pre-sorted on last and first name.
   *  
   * @param pageOffset Starting record number.
   * @param pageLimit Maximum number of records returned.
   * @return List of movies or or error list with
   *         status BAD_REQUEST with a list of constraint violations or
   *         status INTERNAL_SERVER_ERROR on server error.
   */
  @GET
  @Path("/actors") 
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPageOfActorsByName(
      @QueryParam("pageOffset") int pageOffset, 
      @QueryParam("pageLimit") int pageLimit,
      @QueryParam("searchFor") String searchFor) {
    
    Response.ResponseBuilder builder = null;
    Map<String, String> responseObj = new HashMap<>();

    List<Actor> actors;

    try {      
      if (searchFor == null) {
        actors = registration.findPageOfActors(pageOffset, pageLimit);
      } else {
        actors = registration.findPageOfActorsByName(pageOffset, pageLimit, searchFor);
      }
      
      builder = Response.ok().entity(actors);
    } catch (EjbConstraintViolationException cve) {
      responseObj = createViolationMap(cve.getConstraintViolations()); 
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (RuntimeException rte) {
      builder = Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }
   
    return builder.build();
  }
  
  /**
   * Retrieves a page of movies using offset pagination approach and optional filtering via 
   * {@link tv.beenius.videostore.service.RegisterService#findPageOfMoviesByTitle(int,int,String)}
   * or {@link tv.beenius.videostore.service.RegisterService#findPageOfMovies(int, int)}.
   * Movie records are pre-sorted on title.
   *  
   * @param pageOffset Starting record number.
   * @param pageLimit Maximum number of records returned.
   * @return List of movies or or error list with
   *         status BAD_REQUEST with a list of constraint violations or
   *         status INTERNAL_SERVER_ERROR on server error.
   */
  @GET
  @Path("/movies") 
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPageOfMoviesByTitle(
      @QueryParam("pageOffset") int pageOffset, 
      @QueryParam("pageLimit") int pageLimit,
      @QueryParam("searchFor") String searchFor) {
    
    Response.ResponseBuilder builder = null;
    Map<String, String> responseObj = new HashMap<>();

    List<Movie> movies;

    try {  
      if (searchFor == null) {
        movies = registration.findPageOfMovies(pageOffset, pageLimit);
      } else {
        movies = registration.findPageOfMoviesByTitle(pageOffset, pageLimit, searchFor);        
      }

      builder = Response.ok().entity(movies);
    } catch (EjbConstraintViolationException cve) {
      responseObj = createViolationMap(cve.getConstraintViolations()); 
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (RuntimeException rte) {
      builder = Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }
   
    return builder.build();
  }

  // Update entities by PUT.
  
  /**
   * Updates actor attributes firstName, lastName and bornDate via
   * {@link tv.beenius.videostore.service.RegisterService#updateActor(Actor)}.
   * 
   * <p>@param actor Actor.
   * @return Response contains response status OK 
   *         or exception messages with status BAD_REQUEST 
   *         or INTERNAL_SERVER_ERROR.
   */
  @PUT
  @Path("/actors/{id}") 
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putActor(
      @FormParam("firstName") String firstName,
      @FormParam("lastName") String lastName,
      @FormParam("bornDate")  LocalDate bornDate,
      @PathParam("id") Long id) {
    
    Response.ResponseBuilder builder = null;
    Map<String, String> responseObj = new HashMap<>();
    
    try {      
      Actor actor = new Actor(id, firstName, lastName, bornDate);
      
      Actor updatedActor = registration.updateActor(actor);
      
      builder = Response.status(Response.Status.OK).entity(updatedActor);
    } catch (EjbConstraintViolationException cve) {
      responseObj = createViolationMap(cve.getConstraintViolations()); 
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (EjbValidationException ve) {
      responseObj.put("Actor", ve.getMessage());
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (RuntimeException rte) {
      builder = Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }
    
    return builder.build();
  }
  
  /**
   * Updates movie attributes title, description and year via
   * {@link tv.beenius.videostore.service.RegisterService#updateMovie(Movie)}.
   * 
   * <p>@param movie Movie.
   * @return Response contains response status OK 
   *         or exception messages with status BAD_REQUEST 
   *         or INTERNAL_SERVER_ERROR.
   */
  @PUT
  @Path("/movies/{imdbId}") 
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putMovie(
      @FormParam("title") String title,
      @FormParam("description") String description,
      @FormParam("year") Integer year,
      @PathParam("imdbId") String imdbId) {
    
    Response.ResponseBuilder builder = null;
    Map<String, String> responseObj = new HashMap<>();
    
    try {      
      Movie movie = new Movie(imdbId, title, year, description);
      
      Movie updatedMovie = registration.updateMovie(movie);
      
      builder = Response.status(Response.Status.OK).entity(updatedMovie);
    } catch (EjbConstraintViolationException cve) {
      responseObj = createViolationMap(cve.getConstraintViolations()); 
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (EjbValidationException ve) {
      responseObj.put("Movie", ve.getMessage());
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (RuntimeException rte) {
      builder = Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }
    
    return builder.build();
  }
  
  /**
   * Put image via 
   * {@link tv.beenius.videostore.service.RegisterService#updateImage(Image)}.
   * 
   * <p>@param imdbId Movie identifier.
   * @param multiPart Image file attributes and content.
   * @return Response contains id and description of a registered image 
   *         or error list with status BAD_REQUEST or INTERNAL_SERVER_ERROR.
   */
  @PUT
  @Path("/images/{id}") 
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putMovieImage(
      @PathParam("id") Long id, 
      MultipartFormDataInput  multiPart) {
    
    Response.ResponseBuilder builder = null;
    Map<String, String> responseObj = new HashMap<>();
    
    try {
      Image image = readImageFromMultiPart(multiPart);
      image.setId(id);
      
      Image updatedImage = registration.updateImage(image);  
      
      responseObj.put("id", updatedImage.getId().toString());
      responseObj.put("description", updatedImage.getDescription());
      builder = Response.ok().entity(responseObj);
    } catch (EjbConstraintViolationException cve) {
      responseObj = createViolationMap(cve.getConstraintViolations()); 
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (EjbValidationException ve) {
      responseObj.put("Image", ve.getMessage());
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (IOException ioe) {
      responseObj.put("Image", ioe.getMessage());
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (RuntimeException rte) {
      builder = Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }
    
    return builder.build();
  }
     
  // Delete entities by DELETE.

  /**
   * Un-register actor with any casts to movies via 
   * {@link tv.beenius.videostore.service.RegisterService#unRegisterActor(Long)}.
   * Operation is idempotent: deleting non-existing entity is supported.
   * 
   * @param id Actor identifier.
   * @return Response contains response status NO_CONTENT 
   *         or exception messages with status BAD_REQUEST 
   *         or INTERNAL_SERVER_ERROR.
   */
  @DELETE
  @Path("/actors/{id}") 
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteActorById(@PathParam("id") Long id) {

    Response.ResponseBuilder builder = null;
    Map<String, String> responseObj = new HashMap<>();

    try {      
      registration.unRegisterActor(id);
      
      builder = Response.status(Response.Status.NO_CONTENT);
    } catch (EjbConstraintViolationException cve) {
      responseObj = createViolationMap(cve.getConstraintViolations()); 
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (RuntimeException rte) {
      builder = Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }

    return builder.build();
  }

  /**
   * Un-register movie with any casts to actors via 
   * {@link tv.beenius.videostore.service.RegisterService#unRegisterMovie(String)}.
   * Operation is idempotent: deleting non-existing entity is supported.
   * 
   * @param imdbId Movie identifier.
   * @return Response contains response status NO_CONTENT 
   *         or exception messages with status BAD_REQUEST 
   *         or INTERNAL_SERVER_ERROR.
   */
  @DELETE
  @Path("/movies/{imdbId}") 
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteMovieByImdbId(@PathParam("imdbId") String imdbId) {

    Response.ResponseBuilder builder = null;
    Map<String, String> responseObj = new HashMap<>();

    try {      
      registration.unRegisterMovie(imdbId);
      
      builder = Response.status(Response.Status.NO_CONTENT);
    } catch (EjbConstraintViolationException cve) {
      responseObj = createViolationMap(cve.getConstraintViolations()); 
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (RuntimeException rte) {
      builder = Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }

    return builder.build();
  }

  /**
   * Un-register cast between movie and actor via 
   * {@link tv.beenius.videostore.service.RegisterService#unRegisterCast(String, Long)}.
   * Operation is idempotent: deleting non-existing relation is supported.
   * 
   * @param imdbId Movie identifier.
   * @param id Actor identifier.
   * @return Response contains response status NO_CONTENT 
   *         or exception messages with status BAD_REQUEST 
   *         or INTERNAL_SERVER_ERROR.
   */
  @DELETE
  @Path("/movies/{imdbId}/actors/{id}") 
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteCast(
      @PathParam("imdbId") String imdbId, 
      @PathParam("id") Long id) {

    Response.ResponseBuilder builder = null;
    Map<String, String> responseObj = new HashMap<>();

    try {      
      registration.unRegisterCast(imdbId, id);
      
      builder = Response.status(Response.Status.NO_CONTENT);
    } catch (EjbConstraintViolationException cve) {
      responseObj = createViolationMap(cve.getConstraintViolations()); 
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (EjbValidationException ve) {
      responseObj.put("Cast", ve.getMessage());
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (RuntimeException rte) {
      builder = Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }

    return builder.build();
  }

  /**
   * Un-register movie image via 
   * {@link tv.beenius.videostore.service.RegisterService#unRegisterMovieImage(String, Long)}.
   * Operation is idempotent: deleting non-existing entity is supported.
   * 
   * @param imdbId Movie identifier.
   * @param id Image identifier.
   * @return Response contains response status NO_CONTENT 
   *         or exception messages with status BAD_REQUEST 
   *         or INTERNAL_SERVER_ERROR.
   */
  @DELETE
  @Path("/movies/{imdbId}/images/{id}") 
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteMovieImage(
      @PathParam("imdbId") String imdbId,
      @PathParam("id") Long id) {
    
    Response.ResponseBuilder builder = null;
    Map<String, String> responseObj = new HashMap<>();

    try {      
      registration.unRegisterMovieImage(imdbId, id);
      
      builder = Response.status(Response.Status.NO_CONTENT);
    } catch (EjbConstraintViolationException cve) {
      responseObj = createViolationMap(cve.getConstraintViolations()); 
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (EjbValidationException ve) {
      responseObj.put("MovieImage", ve.getMessage());
      builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    } catch (RuntimeException rte) {
      builder = Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }

    return builder.build();
  }  

  // Utilities.
  
  private Map<String, String> createViolationMap(Set<ConstraintViolation<?>> violations) {
    
    Map<String, String> responseObj = new HashMap<>();
    
    violations.forEach(violation -> {
      responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
    });
    
    return responseObj;
  }
  
  private Image readImageFromMultiPart(MultipartFormDataInput  multiPart) 
      throws IOException {
    
    Map<String, List<InputPart>> formDataMap = multiPart.getFormDataMap();
    ObjectMapper mapper = new ObjectMapper();
    
    // Retrieve Image attributes w/o content.
    
    List<InputPart> attributesInputParts = formDataMap.get(MULTIPART_ATTRIBUTES);
    
    if (attributesInputParts == null) {
      throw new IOException("Request is mising input parts {" + MULTIPART_ATTRIBUTES + "}.");
    }
    if (attributesInputParts.size() == 0) {
      throw new IOException("Request contains empty input parts {" + MULTIPART_ATTRIBUTES + "}.");
    }
    
    String attributesJson = attributesInputParts.get(0).getBodyAsString();
    
    Image image = mapper.readValue(attributesJson, Image.class);
    
    // Retrieve Image attribute content.
    
    List<InputPart> imageInputParts = formDataMap.get(MULTIPART_IMAGE);
    
    if (imageInputParts == null) {
      logger.log(Level.WARN, "Request is mising input part {" + MULTIPART_IMAGE + "}.");
      return image;
    }
    if (imageInputParts.size() == 0) {
      logger.log(Level.WARN, "Request contains empty input part {" + MULTIPART_IMAGE + "}.");
      return image;
    }
    
    image.setContent(imageUtil.getImageFromInputPart(imageInputParts.get(0)));

    return image;
  }

}
