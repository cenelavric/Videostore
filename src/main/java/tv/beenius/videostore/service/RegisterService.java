package tv.beenius.videostore.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

import tv.beenius.videostore.data.ActorRepository;
import tv.beenius.videostore.data.ImageRepository;
import tv.beenius.videostore.data.MovieRepository;
import tv.beenius.videostore.event.ActorEvent;
import tv.beenius.videostore.event.MovieEvent;
import tv.beenius.videostore.exception.EjbConstraintViolationException;
import tv.beenius.videostore.exception.EjbValidationException;
import tv.beenius.videostore.model.Actor;
import tv.beenius.videostore.model.Image;
import tv.beenius.videostore.model.Movie;

@Stateless
public class RegisterService {
  
  Logger logger = Logger.getLogger(getClass());
  
  @PositiveOrZero
  private int pageOffset;
  
  @Positive
  @Max(100) 
  private int pageLimit;
  
  @NotBlank
  String searchFor;

  @Inject
  ActorRepository actorRepo;

  @Inject
  ImageRepository imageRepo;

  @Inject
  MovieRepository movieRepo;
  
  @Inject
  private Validator validator;
  
  @Inject
  @ActorEvent
  private Event<String> actorEventSrc;

  @Inject
  @MovieEvent
  private Event<String> movieEventSrc;

  /**
   * Saves and relates new actor and new movies.
   * 
   * <p>Actor and corresponding movies are persisted.
   * A bi-directional relationship is established between an  actor and movies.
   * Any missing inverse movie to actor relationships are silently established.
   * Registration fails when actor or any of listed movies have already been registered. 
   * Registered actor can be later updated via {@link #updateActor(Actor)}.
   * Registered actor and registered movie can be later related via 
   * {@link #registerCast(String, Long)}.
   * 
   * @param actor Actor.
   * @return Actor Registered actor with associated movies.
   * @throws EjbConstraintViolationException is thrown when entity constraint is violated
   *         on actor or any of his associated movies. 
   * @throws EjbValidationException is thrown when actor is null or has assigned a non-null id 
   *         or any of associated movies has already been registered.
   */
  public Actor registerActor(Actor actor)
      throws EjbConstraintViolationException, EjbValidationException {

    validateEntity(actor);
    validateEntityIdentifier(actor.getId(), false);
    
    // Cannot register actor with already registered movie(s).
    for (Movie movie : actor.getMovies()) {
      validateEntity(movie);
      validateMoviePersistence(movie.getImdbId(), false);
    }

    // Silently establish any missing bi-directional relationships.
    actor.getMovies().forEach(movie -> {
      movie.getActors().add(actor);
    });
    
    final Actor savedActor = actorRepo.save(actor);
    
    actorEventSrc.fire(composeEvent(savedActor));
    if (! savedActor.getMovies().isEmpty()) {
      movieEventSrc.fire(composeEvent(savedActor.getMovies()));
    }
    
    logger.log(Level.INFO, composeEvent(savedActor));   

    return savedActor;
  }

  /**
   * Saves and relates new movie and new actors.
   * 
   * <p>Movie and actors are saved into database.
   * A bi-directional relationship is established between movie and actors.
   * Related movie images are saved to database.
   * Registration fails when movie or any of listed actors have already been registered.
   * Registered movie can be later updated via {@link #updateMovie(Movie)}.
   * Registered movie can be later related to registered actor via  
   * {@link #registerCast(String, Long)}.
   * New movie image can be later assigned to registered movie via 
   * {@link #registerMovieImage(String, Image)}.
   * 
   * @param movie Movie.
   * @return Movie Registered movie.
   * @throws EjbConstraintViolationException is thrown when entity constraint is violated. 
   *     EjbValidationException is thrown when movie has already been registered 
   *     or any of listed actors has already been registered.
   */
  public Movie registerMovie(Movie movie) 
      throws EjbConstraintViolationException, EjbValidationException {

    validateEntity(movie);
    validateMoviePersistence(movie.getImdbId(), false);

    // Cannot register movie with an already registered actor(s).
    for (Actor actor : movie.getActors()) {
      validateEntity(actor);
      validateEntityIdentifier(actor.getId(), false);
    }

    final Movie savedMovie = movieRepo.save(movie);
    
    movieEventSrc.fire(composeEvent(savedMovie));
    if (! savedMovie.getActors().isEmpty()) {
      actorEventSrc.fire(composeEvent(savedMovie.getActors()));
    }
    
    logger.log(Level.INFO, composeEvent(savedMovie));   

    return savedMovie;
  }

  /**
   * Saves and relates a new movie image to a registered movie.
   * 
   * <p>One-to-many relationship  between movie and image is extended with
   * new image. Related image is saved into image database.
   * Registration fails when movie image has already been registered
   * or movie has not been registered.
   * 
   * @param imdbId  Movie identifier.
   * @param image Image.
   * @return Image image.
   * @throws EjbConstraintViolationException 
   *         Exception on validation of movie/image entity attributes.
   * @throws EjbValidationException Exception on validation of movie/image.
   */
  public Image registerMovieImage(String imdbId, Image image) 
      throws EjbConstraintViolationException, EjbValidationException {
    
    validateImdbId(imdbId);
    validateMoviePersistence(imdbId, true);
    
    validateEntity(image);
    validateEntityIdentifier(image.getId(), false);
 
    final Image savedImage = movieRepo.saveMovieImage(imdbId, image);
    
    movieEventSrc.fire(composeEvent(imdbId, savedImage));
    
    logger.log(Level.INFO, composeEvent(imdbId, savedImage));   

    return savedImage;
  }

  /**
   * Registers cast between movie and actor.
   * 
   * <p>A bi-directional relationship is established between movie and actor.
   * Registration fails when movie or actor have not been registered. 
   * Movie can be registered by {@link #registerMovie(Movie)}.
   * Actor can be registered by {@link #registerActor(Actor)}.
   * @param imdbId Movie identifier.
   * @param id Actor identifier.
   * 
   * @throws EjbConstraintViolationException on invalid parameters.
   * @throws EjbValidationException on non-existing entities.
   */
  public void registerCast(String imdbId, Long id)  
      throws EjbConstraintViolationException, EjbValidationException {

    validateImdbId(imdbId);
    final Optional<Movie> optionalFoundMovie = validateMoviePersistence(imdbId, true);
    
    validateEntityIdentifier(id, true);
    validateActorId(id);
    final Optional<Actor> optionalFoundActor = validateActorPersistence(id, true);

    Movie movie = optionalFoundMovie.get();
    Actor actor = optionalFoundActor.get();
    
    validateCastPersistence(movie, actor, false);
    
    movie.getActors().add(actor);
    
    actorEventSrc.fire(composeEvent(imdbId, id));
    movieEventSrc.fire(composeEvent(imdbId, id));
    
    logger.log(Level.INFO, composeEvent(imdbId, id));   
  }

  /**
   * Update actor attributes.
   * 
   * <p>Updates actor attributes by calling 
   * {@link tv.beenius.videostore.data.ActorRepository#update(Actor)}.
   * 
   * @param actor Actor.
   * @return updated Actor-
   * @throws EjbConstraintViolationException is thrown when entity constraint is violated. 
   * @throws EjbValidationException is thrown when movie has not been registered yet.
   */
  public Actor updateActor(Actor actor) 
      throws EjbConstraintViolationException, EjbValidationException {
    
    validateEntityIdentifier(actor.getId(), true);
    validateActorPersistence(actor.getId(), true);
 
    Optional<Actor> updatedOptionalActor = actorRepo.update(actor);
    Actor updatedActor = updatedOptionalActor.get();
    
    actorEventSrc.fire(composeEvent(updatedActor));

    logger.log(Level.INFO, composeEvent(updatedActor));   
    
    return updatedActor;
  }

  /**
   * Update movie attributes.
   * 
   * <p>Updates movie attributes by calling 
   * {@link tv.beenius.videostore.data.MovieRepository#update(Movie)}.
   * 
   * @param movie Movie to be updated.
   * @return updated Movie-
   * @throws EjbConstraintViolationException is thrown when entity constraint is violated. 
   * @throws EjbValidationException is thrown when movie has not been registered yet.
   */
  public Movie updateMovie(Movie movie) 
      throws EjbConstraintViolationException, EjbValidationException {

    validateImdbId(movie.getImdbId());
    validateMoviePersistence(movie.getImdbId(), true);

    Optional<Movie> updatedOptionalMovie = movieRepo.update(movie);
    Movie updatedMovie = updatedOptionalMovie.get();
    
    movieEventSrc.fire(composeEvent(updatedMovie));
    
    logger.log(Level.INFO, composeEvent(updatedMovie));   
    
    return updatedMovie;
  }

  /**
   * Update image attributes.
   * 
   * <p>Updates movie image attributes by calling 
   * {@link tv.beenius.videostore.data.ImageRepository#update(Image)}.
   * 
   * @param image Image.
   * @return Image.
   * @throws EjbConstraintViolationException on constraint violation.
   * @throws EjbValidationException on validation exception.
   */
  public Image updateImage(Image image) 
      throws EjbConstraintViolationException, EjbValidationException {
    
    validateEntityIdentifier(image.getId(), true);
    validateMovieImagePersistence(null, image.getId(), true);

    Optional<Image> updatedOptionalImage = imageRepo.update(image);
    Image updatedImage = updatedOptionalImage.get();
    
    movieEventSrc.fire(composeEvent(updatedImage));
    
    logger.log(Level.INFO, composeEvent(updatedImage));   
    
    return updatedImage;
  }

  /**
   * Removes an actor.
   * 
   * <p>Removes actor by calling 
   * {@link tv.beenius.videostore.data.ActorRepository#removeById(Long)}.
   * 
   * @param id Actor identifier.
   * @throws EjbConstraintViolationException on invalid entity identifier.
   */
  public void unRegisterActor(Long id) throws EjbConstraintViolationException {
    validateEntityIdentifier(id, true);
    validateActorId(id);
    
    if (actorRepo.removeById(id)) {
      actorEventSrc.fire(composeEvent(id));
    }
    
    logger.log(Level.INFO, composeEvent(id));   
  }

  /**
   * Removes a movie.
   * 
   * <p>Removes movie by calling 
   * {@link tv.beenius.videostore.data.MovieRepository#removeById(String)}.
   * 
   * @param imdbId Movie identifier.
   * @throws EjbConstraintViolationException on invalid entity identifier.
   */
  public void unRegisterMovie(String imdbId) throws EjbConstraintViolationException {
    
    validateImdbId(imdbId);   
    
    if (movieRepo.removeById(imdbId)) {
      movieEventSrc.fire(composeEvent(imdbId));
    }
    
    logger.log(Level.INFO, composeEvent(imdbId));   
  }

  /**
   * Removes a movie image.
   * 
   * <p>Removes movie image by calling 
   * {@link tv.beenius.videostore.data.MovieRepository#removeMovieImageById(String, Long)}.
   * 
   * @param imdbId Movie identifier.
   * @param id Movie Image identifier
   * @throws EjbConstraintViolationException on invalid input parameters imdbId.
   * @throws EjbValidationException on movie absence or missing parameter id.
   */
  public void unRegisterMovieImage(String imdbId, Long id) 
      throws EjbConstraintViolationException, EjbValidationException {
    
    validateImdbId(imdbId);
    validateMoviePersistence(imdbId, true);
    
    validateEntityIdentifier(id, true);
    
    if (movieRepo.removeMovieImageById(imdbId, id)) {
      movieEventSrc.fire(composeEvent(imdbId, id));
    }
    
    logger.log(Level.INFO, composeEvent(imdbId, id));   
  }
  
  /**
   * Unregisters cast between movie and actor.
   * 
   * <p>A bi-directional relationship between movie and actor is removed.
   * Method fails when movie or actor have not been registered. 
   * 
   * @param imdbId Movie identifier.
   * @param id Actor identifier.
   * @throws EjbConstraintViolationException on invalid parameter.
   * @throws EjbValidationException on non-existing entity.
   */
  public void unRegisterCast(String imdbId, Long id) 
      throws EjbConstraintViolationException, EjbValidationException {
    
    validateImdbId(imdbId);
    final Optional<Movie> optionalFoundMovie = findMovieById(imdbId);
    
    validateEntityIdentifier(id, true);
    validateActorId(id);
    Optional<Actor> optionalFoundActor = findActorById(id);

    if (optionalFoundMovie.isPresent() && optionalFoundActor.isPresent()) {
      Movie movie = optionalFoundMovie.get();
      Actor actor = optionalFoundActor.get();

      movie.getActors().remove(actor);
      actor.getMovies().remove(movie);
      
      movieEventSrc.fire(composeEvent(imdbId, id));
      actorEventSrc.fire(composeEvent(imdbId, id));
    }
    
    logger.log(Level.INFO, composeEvent(imdbId, id));   
  }

  /**
   * Finds actor by id.
   * 
   * <p>Input parameter validation should be provided by caller.
   * 
   * <p>Retrieves actor by calling 
   * {@link tv.beenius.videostore.data.ActorRepository#findById(Long)}.
   * 
   * @param id Actor identifier
   * @return Actor with related movies.
   */
  public Optional<Actor> findActorById(Long id) {
    return actorRepo.findById(id);
  }

  /**
   * Finds Movie by imdbId.
   * 
   * <p>Input parameter validation should be provided by caller.
   * 
   * <p>Retrieves movie by calling 
   * {@link tv.beenius.videostore.data.MovieRepository#findById(String)}.
   * 
   * @param imdbId Movie identifier.
   * @return Movie together with  related actors and images.
   */
  public Optional<Movie> findMovieById(String imdbId) { 
    return movieRepo.findById(imdbId);
  }

  /**
   * Finds Actor by id, Lazily.
   * 
   * <p>Retrieves anactor by calling 
   * {@link tv.beenius.videostore.data.ActorRepository#findByIdLazily(Long)}.
   * 
   * @param id Actor identifier.
   * @return Actor w/o related movies.
   */
  public Optional<Actor> findActorByIdLazily(Long id) {    
    return actorRepo.findByIdLazily(id);
  }

  /**
   * Finds Movie by imdbId. Lazily.
   * 
   * <p>Retrieves a movie by calling 
   * {@link tv.beenius.videostore.data.MovieRepository#findByIdLazily(String)}.
   * 
   * @param imdbId Movie identifier.
   * @return Movie w/o related actors or images.
   */
  public Optional<Movie> findMovieByIdLazily(String imdbId) {    
    return movieRepo.findByIdLazily(imdbId);
  }

  /**
   * Retrieves a list of movies containing search string..
   * 
   * <p>Retrieves a list of movies containing search string and sorted by title by calling 
   * {@link tv.beenius.videostore.data.MovieRepository#findByTitle(String)}.
   * Movie contains related actors.
   * 
   * <p>Movie does not contain movie images due to lazy fetch.
   * Call {@link #findMovieImageById(Long)} to retrieve specific movie with image.
   * 
   *  
   * @param searchFor Search string.
   * @return Movie
   * @throws Exception on exception from
   * {@link tv.beenius.videostore.data.MovieRepository#findByTitle(String)}.
   */
  public List<Movie> findMovieByTitleSearch(String searchFor) 
      throws EjbConstraintViolationException { 
    
    validateFilteringParameter(searchFor);  
    
    return movieRepo.findByTitle(searchFor);
  }

  /**
   * Retrieves a list of all actors.
   * 
   * <p>IMPORTANT: Retrieving large number of actors can cause performance
   * bottle-necks. Prefer using this method for testing and demonstration purposes
   * only.
   * 
   * <p>Retrieves a sorted (by last and first name) list of all actors by calling 
   * {@link tv.beenius.videostore.data.ActorRepository#findAll()}.
   * Actors contain related movies.
   * 
   * @return List of actors.
   */
  public List<Actor> findAllActors() {
    return actorRepo.findAll();
  }

  /**
   * Retrieves a list of all movies.
   * 
   * <p>IMPORTANT: Retrieving large number of movies can cause performance
   * bottle-necks. Prefer using this method for testing and demonstration purposes
   * only.
   * 
   * <p>Retrieves a sorted (by title) list of all movies by calling
   * {@link tv.beenius.videostore.data.MovieRepository#findAll()}. Movies contain
   * related actors.
   * 
   * <p>Movies do not contain movie images due to lazy fetch. Call
   * {@link #findMovieImages(String)} to retrieve specific movie with
   * images.
   * 
   * @return List of movies.
   */
  public List<Movie> findAllMovies() {
    return movieRepo.findAll();
  }
  
  /**
   * Retrieves list of all movie images for a movie.
   * 
   * <p>Retrieves a sorted list (by description) of all movie images for a movie by calling 
   * {@link tv.beenius.videostore.data.ImageRepository#findMovieImagesById(String)}.
   * 
   * @param imdbId Movie identifier.
   * @return List of Images.
   * @throws EjbConstraintViolationException on invalid movie identifier.
   * @throws EjbValidationException on movie not existing.
   */
  public List<Image> findMovieImages(String imdbId) 
      throws EjbConstraintViolationException, EjbValidationException {
    
    validateImdbId(imdbId);
    Optional<Movie> optionalFoundMovie = validateMoviePersistence(imdbId, true);
 
    return new ArrayList<Image>(optionalFoundMovie.get().getImages());
  }

  /**
   * Retrieves a list of all actors casted to a movie. Lazily.
   *  
   * <p>Retrieves a sorted (by last and first name) list of all actors by calling 
   * {@link tv.beenius.videostore.data.ActorRepository#findMovieActorsLazily(String)}.
   * Actors contain no related movies.
   * 
   * @param imdbId Movie identifier
   * @return List of actors.
   * @throws EjbConstraintViolationException on invalid validation parameter. 
   */
  public List<Actor> findMovieActorsLazily(String imdbId) throws EjbConstraintViolationException {
   
    validateImdbId(imdbId);
    validateMoviePersistence(imdbId, true);
    
    return actorRepo.findMovieActorsLazily(imdbId);
  }

  /**
   * Retrieves a list of all movies casted to an actor. Lazily.
   *  
   * <p>Retrieves a sorted (by last and first name) list of all actors by calling 
   * {@link tv.beenius.videostore.data.MovieRepository#findActorMoviesLazily(Long)}.
   * Actors contain no related movies.
   * 
   * @param id Actor identifier
   * @return List of actors.
   * @throws EjbConstraintViolationException on invalid validation parameter. 
   */
  public List<Movie> findActorMoviesLazily(Long id) 
      throws EjbConstraintViolationException, EjbValidationException {
    
    validateEntityIdentifier(id, true);
    validateActorId(id);
    validateActorPersistence(id, true);
    
    return movieRepo.findActorMoviesLazily(id);
  }

  /**
   * Retrieves an image through movie.
   * 
   * <p>Retrieves movie image by calling 
   * {@link tv.beenius.videostore.data.ImageRepository#findMovieImageById(String, Long)}.
   * 
   * <p>Input parameter validation should be provided by caller.
   * 
   * @param imdbId Movie identifier.
   * @param id Image identifier.
   * @return Optional movie image.
   */
  public Optional<Image> findMovieImageById(String imdbId, Long id) {    
    return imageRepo.findMovieImageById(imdbId, id);
  }

  /**
   * Retrieves an image.
   * 
   * <p>Retrieves movie image by calling 
   * {@link tv.beenius.videostore.data.ImageRepository#findById(Long)}.
   * 
   * <p>Input parameter validation should be provided by caller.
   * 
   * @param id Image identifier.
   * @return Optional movie image.
   */
  public Optional<Image> findMovieImageById(Long id) {
    return imageRepo.findById(id);
  }

  /**
   * Retrieves a page of movies.
   * 
   * <p>Retrieves a page from sorted (by title) list of all movies by calling 
   * {@link tv.beenius.videostore.data.MovieRepository#findPage(int, int)}.
   * 
   * <p>Movies do not contain movie images due to lazy fetch.
   * Call {@link #findMovieImages(String)} to retrieve specific movie with images.
   * 
   * @param pageOffset Starting record for page. Records start with 0.
   * @param pageLimit Maximum page size.
   * @return Sorted list of movies.
   * @throws EjbConstraintViolationException Exception is thrown on invalid input parameters.
   */
  public List<Movie> findPageOfMovies(int pageOffset, int pageLimit) 
      throws EjbConstraintViolationException {  
    validatePagingParameters(pageOffset, pageLimit);   
    return movieRepo.findPage(pageOffset, pageLimit);
  }

  /**
   * Retrieves a page of movies filtered by title filter.
   * 
   * <p>Retrieves a page from filtered and sorted (by title) list of all movies by calling 
   * {@link tv.beenius.videostore.data.MovieRepository#findPageByTitle(int, int, String)}.
   * 
   * <p>Movies do not contain movie images due to lazy fetch.
   * Call {@link #findMovieImages(String)} to retrieve specific movie with images.
   * 
   * @param pageOffset Starting record for page. Records start with 0.
   * @param pageLimit Maximum page size.
   * @param searchFor Search string from title.
   * @return Sorted list of movies.
   * @throws EjbConstraintViolationException Exception is thrown on invalid input parameters.
   */
  public List<Movie> findPageOfMoviesByTitle(
      int pageOffset, 
      int pageLimit,  
      String searchFor) 
      throws EjbConstraintViolationException {
    
    validatePagingParameters(pageOffset, pageLimit);
    validateFilteringParameter(searchFor);
    
    return movieRepo.findPageByTitle(pageOffset, pageLimit, searchFor);
  }

  /**
   * Retrieves page of actors.
   * 
   * <p>Retrieves a page from sorted (by last and first name) list of all actors by calling 
   * {@link tv.beenius.videostore.data.ActorRepository#findPage(int, int)}.
   * Actors contain related movies.
   * 
   * @param pageOffset Starting record for page. Records start with 0.
   * @param pageLimit Maximum page size.
   * @return Sorted list of actors.
   * @throws EjbConstraintViolationException TThrown on invalid input parameters.
   */
  public List<Actor> findPageOfActors(int pageOffset, int pageLimit) 
      throws EjbConstraintViolationException {
    validatePagingParameters(pageOffset, pageLimit); 
    return actorRepo.findPage(pageOffset, pageLimit);
  }

  /**
   * Retrieves a page of actors filtered by name filter.
   * 
   * <p>Retrieves a page from filtered and sorted (by title) list of all actors by calling 
   * {@link tv.beenius.videostore.data.ActorRepository#findPageByName(int, int, String)}.
   * 
   * @param pageOffset Starting record for page. Records start with 0.
   * @param pageLimit Maximum page size.
   * @param searchFor Search string from name.
   * @return Sorted list of actors.
   * @throws EjbConstraintViolationException TThrown on invalid input parameters.
   */
  public List<Actor> findPageOfActorsByName(
      int pageOffset, 
      int pageLimit,  
      String searchFor) 
      throws EjbConstraintViolationException {
    
    validatePagingParameters(pageOffset, pageLimit);
    validateFilteringParameter(searchFor);
    
    return actorRepo.findPageByName(pageOffset, pageLimit, searchFor);
  }
  
  /**
   * Counts all actors.
   * 
   * <p>Counts all actors by calling 
   * {@link tv.beenius.videostore.data.ActorRepository#count()}.
   * 
   * @return Number of actors.
   */
  public long countActors() {
    return actorRepo.count();
  }

  /**
   * Counts all movies.
   * 
   * <p>Counts all movies by calling
   * {@link tv.beenius.videostore.data.MovieRepository#count()}.
   * 
   * @return Number of movies.
   */
  public long countMovies() {
    return movieRepo.count();
  }

  /**
   * Counts all movies with title containing search string.
   * 
   * <p>Counts all movies by calling
   * {@link tv.beenius.videostore.data.MovieRepository#countByTitle(String)}.
   * 
   * @return Number of movies.
   * @throws EjbConstraintViolationException Exception is thrown on invalid input parameter.
   */
  public long countMoviesByTitle(String searchFor) 
      throws EjbConstraintViolationException {  
    validateFilteringParameter(searchFor);
    return movieRepo.countByTitle(searchFor);
  }

  /**
   * Counts all actors with name containing search string.
   * 
   * <p>Counts all actors by calling
   * {@link tv.beenius.videostore.data.ActorRepository#countByName(String)}.
   * 
   * @return Number of actors.
   * @throws EjbConstraintViolationException Exception is thrown on invalid input parameter.
   */
  public long countActorsByName(String searchFor) 
      throws EjbConstraintViolationException {
    
    validateFilteringParameter(searchFor);

    return actorRepo.countByName(searchFor);
  }

  /**
   * Counts all images for a movie.
   * 
   * <p>Counts all images for a movie by calling
   * {@link tv.beenius.videostore.data.MovieRepository#countMovieImages(String)}.
   * 
   * @return Number of movies.
   */
  public long countMovieImages(String imdbId) {
    return movieRepo.countMovieImages(imdbId);
  }
  
  // Utilities.

  /**
   * Validates actor identifier.
   * 
   * @param id Actor identifier.
   * @throws EjbConstraintViolationException on invalid identifier value.
   */
  public void validateActorId(Long id) throws EjbConstraintViolationException {
    
    Set<ConstraintViolation<Actor>> violations = 
        validator.validateValue(Actor.class, "id", id);
    
    if (! violations.isEmpty()) {
      throw new EjbConstraintViolationException(new HashSet<>(violations));
    }
  }

  /**
   * Validates movie identifier.
   * 
   * @param imdbId Movie identifier.
   * @throws EjbConstraintViolationException on invalid identifier value.
   */
  public void validateImdbId(String imdbId) throws EjbConstraintViolationException {
    
    Set<ConstraintViolation<Movie>> violations = 
        validator.validateValue(Movie.class, "imdbId", imdbId);
    
    if (! violations.isEmpty()) {
      throw new EjbConstraintViolationException(new HashSet<>(violations));
    }
  }
  
  /**
   * Validates image identifier.
   * 
   * @param id Image identifier.
   * @throws EjbConstraintViolationException on invalid identifier value.
   */
  public void validateImageId(Long id) throws EjbConstraintViolationException {
    
    Set<ConstraintViolation<Image>> violations = 
        validator.validateValue(Image.class, "id", id);
    
    if (! violations.isEmpty()) {
      throw new EjbConstraintViolationException(new HashSet<>(violations));
    }
  }

  /**
   * Validates paging parameters.
   * 
   * @param pageOffset Page offset.
   * @param pageLimit Page limit.
   * @throws EjbConstraintViolationException on invalid page parameter value.
   */
  private void validatePagingParameters(int pageOffset, int pageLimit) 
      throws EjbConstraintViolationException {
    
    Set<ConstraintViolation<RegisterService>> violations = new HashSet<>();
    
    Set<ConstraintViolation<RegisterService>> pageOffsetViolations = 
        validator.validateValue(RegisterService.class, "pageOffset", pageOffset);
    Set<ConstraintViolation<RegisterService>> pageLimitViolations = 
        validator.validateValue(RegisterService.class, "pageLimit", pageLimit);
    
    violations.addAll(pageOffsetViolations);
    violations.addAll(pageLimitViolations);
    
    if (!violations.isEmpty()) {
      throw new EjbConstraintViolationException(new HashSet<>(violations));
    }
  }
  
  /**
   * Validates text filtering parameter.
   * 
   * @param searchFor Text filtering parameter.
   * @throws EjbConstraintViolationException on invalid filtering parameter value.
   */
  private void validateFilteringParameter(String searchFor) 
      throws EjbConstraintViolationException {
    
    Set<ConstraintViolation<RegisterService>> violations =
        validator.validateValue(RegisterService.class, "searchFor", searchFor);
    
    if (!violations.isEmpty()) {
      throw new EjbConstraintViolationException(new HashSet<>(violations));
    }  
  }
  
  /**
   * Validates entity against null value and field constraints.
   * 
   * @param <T> Entity type.
   * @param t Entity.
   * @throws EjbConstraintViolationException on constraint violation.
   * @throws EjbValidationException on null value.
   */
  private <T> void validateEntity(T t) 
      throws EjbConstraintViolationException, EjbValidationException {
    
    if (t == null) {
      throw new EjbValidationException("Entity should not be null.");
    }
    
    Set<ConstraintViolation<T>> violations = validator.validate(t);
    if (!violations.isEmpty()) {
      throw new EjbConstraintViolationException(new HashSet<>(violations));
    }
  }
  
  /**
   * Validates actor identifier against expected outcome.
   * 
   * @param id Entity identifier.
   * @param shouldBePresent Expected outcome.
   * @throws EjbValidationException on non-expected outcome.
   */
  public void validateEntityIdentifier(Long id, boolean shouldBePresent)
      throws EjbValidationException {

    if (((id == null) || id.equals(0L)) && shouldBePresent) {
      throw new EjbValidationException("Entity id should not be null.");
    }
    if ((id != null) && ! (id.equals(0L)) && ! shouldBePresent) {
      throw new EjbValidationException("Entity id should be null.");
    }
  }
    
  /**
   * Validates existence of actor against expected outcome.
   * Returns optional actor.
   * 
   * @param id Actor identifier.
   * @param shouldBePresent Expected outcome.
   * @return oprtional actor.
   * @throws EjbValidationException on non-expected outcome.
   */
  private Optional<Actor> validateActorPersistence(Long id, boolean shouldBePresent) 
      throws EjbValidationException {
    
    Optional<Actor> optionalActor = findActorById(id);
    
    if (optionalActor.isPresent() && ! shouldBePresent) {
      throw new EjbValidationException(id + " is already registered.");
    }
    if (optionalActor.isEmpty() && shouldBePresent) {
      throw new EjbValidationException(id + " has not been registered.");
    }
    
    return optionalActor;
  }
  
  /**
   * Validates existence of movie against expected outcome.
   * Returns optional movie.
   *  
   * @param imdbId Movie identifier.
   * @param shouldBePresent Expected outcome.
   * @return optional movie.
   * @throws EjbValidationException on non-expected outcome.
   */
  private Optional<Movie> validateMoviePersistence(String imdbId, boolean shouldBePresent) 
      throws EjbValidationException {
    
    Optional<Movie> optionalMovie = findMovieById(imdbId);
    
    if (optionalMovie.isPresent() && ! shouldBePresent) {
      throw new EjbValidationException(imdbId + " is already registered.");
    }
    if (optionalMovie.isEmpty() && shouldBePresent) {
      throw new EjbValidationException(imdbId + " has not been registered.");
    }
    
    return optionalMovie;
  }
 
  /**
   * Validates existence of movie image against expected outcome.
   * Returns optional movie image.
   * 
   * @param imdbId          Movie identifier.
   * @param id              Image identifier.
   * @param shouldBePresent Expected outcome.
   * @return optional image.
   * @throws EjbValidationException on non-expected outcome.
   */
  private Optional<Image> validateMovieImagePersistence(
      String imdbId, 
      Long id, 
      boolean shouldBePresent) 
      throws EjbValidationException {

    Optional<Image> optionalMovieImage;

    if (imdbId == null) {
      optionalMovieImage = findMovieImageById(id);
    } else {
      optionalMovieImage = findMovieImageById(imdbId, id);
    }

    if (optionalMovieImage.isPresent() && ! shouldBePresent) {
      throw new EjbValidationException(id + " is already registered.");
    }
    if (optionalMovieImage.isEmpty() && shouldBePresent) {
      throw new EjbValidationException(id + " has not been registered.");
    }
    
    return optionalMovieImage;
  }

  /**
   * Validates existence of movie image against expected outcome.
   * 
   * @param movie Movie.
   * @param actor Actor.
   * @param shouldBePresent Expected outcome.
   * @throws EjbValidationException on non-expected outcome.
   */
  private void validateCastPersistence(
      Movie movie, 
      Actor actor, 
      boolean shouldBePresent) 
      throws EjbValidationException {
    
    if (movie.getActors().contains(actor) && ! shouldBePresent) {
      throw new EjbValidationException(movie + " is already casted to " + actor + ".");
    }
    if (! movie.getActors().contains(actor) && shouldBePresent) {
      throw new EjbValidationException(movie + " is not casted to " + actor + ".");
    }
  }

  /**
   * Retrieves enclosing method name and appends identifier.
   * 
   * @return event.
   */
  private String composeEvent(Object...objects) {
    
    StringBuilder sb = new StringBuilder();
    
    sb.append(Thread.currentThread() 
        .getStackTrace()[2] 
        .getMethodName());
    
    if (objects.length == 0) {
      return sb.toString();
    } else {
      sb.append("( ");      
      for (Object o: objects) {
        sb.append(o.toString());
        sb.append(" ");   
      }
      sb.append(")");   
      return sb.toString();
    }
  }

}  
