package tv.beenius.videostore.service;

import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.inject.Inject;

import lombok.NonNull;
import tv.beenius.videostore.data.ActorRepository;
import tv.beenius.videostore.data.ImageRepository;
import tv.beenius.videostore.data.MovieRepository;
import tv.beenius.videostore.model.Actor;
import tv.beenius.videostore.model.Image;
import tv.beenius.videostore.model.Movie;

@Stateless
public class RegisterService {

  @Inject
  ActorRepository actorRepo;

  @Inject
  ImageRepository imageRepo;

  @Inject
  MovieRepository movieRepo;

  /**
   * Saves and relates new actor and new movies.
   * 
   * <p>Actor and movies are saved into database.
   * A bi-directional relationship is established between actor and movies.
   * Any missing inverse movie to actor relationships are silently established.
   * Registration fails when actor or any of listed movies have already been registered. 
   * Registered actor can be later updated via {@link #updateActor(Actor)}.
   * Registered actor and registered movie can be later related via 
   * {@link #registerCast(Long, String)}.
   * 
   * @param actor Actor.
   * @return Actor identifier.
   * @throws Exception on registration failures.
   */
  public Long registerActor(@NonNull Actor actor) throws Exception {

    // Cannot re-register actor.
    if ((actor.getId() != null) && actorRepo.findById(actor.getId()).isPresent()) {
      throw new Exception(actor.toString() + " has already been registered. "
          + "Register any unregistered movie(s) first. " 
          + "Then register cast(s) between Actor and these Movies(s).");
    }

    // Cannot register actor with already registered movie(s).
    for (Movie movie : actor.getMovies()) {
      if (movieRepo.findById(movie.getImdbId()).isPresent()) {
        throw new Exception(
            actor.toString() + " cannot be registered with registered movie(s)." 
            + "Register actor without " + movie.toString() + " first. "
            + "Then register only cast(s) between Actor and Movie(s).");
      }
    }

    // Silently establish any missing bi-directional relationships.
    actor.getMovies().forEach(movie -> {
      movie.getActors().add(actor);
    });

    return actorRepo.save(actor);
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
   * {@link #registerCast(Long, String)}.
   * New movie image can be later assigned to registered movie via 
   * {@link #registerMovieImage(String, Image)}.
   * 
   * @param movie Movie.
   * @throws Exception on registration failure.
   */
  public void registerMovie(@NonNull Movie movie) throws Exception {

    // Cannot re-register movie.
    if (movieRepo.findById(movie.getImdbId()).isPresent()) {
      throw new Exception(movie.toString() + " has already been registered. "
          + "Register any unregistered actor(s) first. " 
          + "Then register cast(s) between Movie and these Actor(s).");
    }

    // Cannot register movie with an already registered actor(s).
    for (Actor actor : movie.getActors()) {
      if ((actor.getId() != null) && actorRepo.findById(actor.getId()).isPresent()) {
        throw new Exception(
            movie.toString() + " cannot be registered with registered actor(s)." 
              + "Register movie without " + actor.toString() + " first. " 
                + "Then register only cast(s) between Movie and Actor(s).");
      }
    }

    movieRepo.save(movie);
  }

  /**
   * Saves and relates a new movie image to a registered movie.
   * 
   * <p>One-to-many relationship  between movie and image is extended with
   * new movie image. Related movie image is saved to image database.
   * Registration fails when movie image has already been registered
   * or movie has not been registered.
   * 
   * @param imdbId  Movie identifier.
   * @param image movie Image.
   * @return Image identifier.
   * @throws Exception on registration failure.
   */
  public Long registerMovieImage(@NonNull String imdbId, @NonNull Image image) throws Exception {

    // Cannot re-register movie image.
    if ((image.getId() != null) && imageRepo.findById(image.getId()).isPresent()) {
      throw new Exception(image.toString() + " has already been registered. "
          + "Update movie image instead.");
    }

    // Retrieve movie.
    final Optional<Movie> optionalFoundMovie = movieRepo.findById(imdbId);
    if (optionalFoundMovie.isEmpty()) {
      throw new Exception("Registration of image " + image + " to " 
          + "movie with imdbId {" + imdbId + "}  failed. "
          + "Movie could not be found in database ." 
          + "Refresh movie image set and check for parameter imdbId.");
    }

    Movie movie = optionalFoundMovie.get();
    movie.getImages().add(image);
    movieRepo.update(movie);

    return image.getId();
  }

  /**
   * Update actor attributes.
   * 
   * <p>Updates movie image attributes by calling 
   * {@link tv.beenius.videostore.data.ActorRepository#update(Actor)}.
   * 
   * @param actor Actor.
   * @throws Exception on exception from
   * {@link tv.beenius.videostore.data.ActorRepository#update(Actor)}.
   */
  public void updateActor(@NonNull Actor actor) throws Exception {
    actorRepo.update(actor);
  }

  /**
   * Update movie attributes.
   * 
   * <p>Updates movie attributes by calling 
   * {@link tv.beenius.videostore.data.MovieRepository#update(Movie)}.
   * 
   * @param movie Movie
   * @throws Exception on exception from 
   * {@link tv.beenius.videostore.data.MovieRepository#update(Movie)}
   */
  public void updateMovie(@NonNull Movie movie) throws Exception {
    movieRepo.update(movie);
  }

  /**
   * Update movie image attributes.
   * 
   * <p>Updates movie image attributes by calling 
   * {@link tv.beenius.videostore.data.ImageRepository#update(Image)}.
   * 
   * @param image movie Image.
   * @throws Exception on exception from
   * {@link tv.beenius.videostore.data.ImageRepository#update(Image)}.
   */
  public void updateMovieImage(@NonNull Image image) throws Exception {
    imageRepo.update(image);
  }

  /**
   * Removes an actor.
   * 
   * <p>Removes movie by calling 
   * {@link tv.beenius.videostore.data.ActorRepository#removeById(Long)}.
   * 
   * @param id Actor identifier.
   * @throws Exception on exception from
   * {@link tv.beenius.videostore.data.ActorRepository#removeById(Long)}.
   */
  public void unRegisterActor(@NonNull Long id) throws Exception {
    actorRepo.removeById(id);
  }

  /**
   * Removes a movie.
   * 
   * <p>Removes movie by calling 
   * {@link tv.beenius.videostore.data.MovieRepository#removeById(String)}.
   * 
   * @param imdbId Movie identifier.
   * @throws Exception on exception frm
   * {@link tv.beenius.videostore.data.MovieRepository#removeById(String)}.
   */
  public void unRegisterMovie(@NonNull String imdbId) throws Exception {
    movieRepo.removeById(imdbId);
  }

  /**
   * Removes a movie image.
   * 
   * <p>Removes movie image by calling 
   * {@link tv.beenius.videostore.data.MovieRepository#removeMovieImageById(String, Long)}.
   * 
   * @param imdbId Movie identifier.
   * @param id Movie Image identifier
   * @throws Exception on exception from
   * {@link tv.beenius.videostore.data.MovieRepository#removeMovieImageById(String, Long)}.
   */
  public void unRegisterMovieImage(@NonNull String imdbId, @NonNull Long id) throws Exception {
    movieRepo.removeMovieImageById(imdbId, id);
  }
  
  /**
   * Registers cast between movie and actor.
   * 
   * <p>A bi-directional relationship is established between movie and actor.
   * Registration fails when movie or actor have not been registered. 
   * Movie can be registered by {@link #registerMovie(Movie)}.
   * Actor can be registered by {@link #registerACtor(Actor)}.
   * 
   * @param id Actor identifier.
   * @param imdbId Movie identifier.
   * 
   * @throws Exception on registration failure.
   */
  public void registerCast(@NonNull Long id, @NonNull String imdbId) throws Exception {

    final Optional<Movie> optionalFoundMovie = movieRepo.findById(imdbId);
    if (optionalFoundMovie.isEmpty()) {
      throw new Exception("Registration of cast between " 
          + "movie with IMDB id {" + imdbId + "} and actor with id {"
          + id + "} failed. " + "Movie could not be found in database. " 
          + "Register movie first.");
    }

    final Optional<Actor> optionalFoundActor = actorRepo.findById(id);
    if (optionalFoundMovie.isEmpty()) {
      throw new Exception("Registration of cast between " 
          + "movie with IMDB id {" + imdbId + "} and actor with id {"
          + id + "} failed. " + "Actor could not be found in database. "
          + "Register actor first.");
    }

    Movie movie = optionalFoundMovie.get();
    Actor actor = optionalFoundActor.get();

    movie.getActors().add(actor);
  }

  /**
   * Unregisters cast between movie and actor.
   * 
   * <p>A bi-directional relationship between movie and actor is removed.
   * Method fails when movie or actor have not been registered. 
   * 
   * @param imdbId Movie identifier.
   * @param id Actor identifier.
   * @throws Exception on failure.
   */
  public void unRegisterCast(@NonNull String imdbId, @NonNull Long id) throws Exception {
    final Optional<Movie> optionalFoundMovie = movieRepo.findById(imdbId);
    if (optionalFoundMovie.isEmpty()) {
      throw new Exception("Unregistration of cast between " 
          + "movie with IMDB id {" + imdbId + "} and actor with id {"
          + id + "} failed. " + "Movie could not be found in database." 
          + "So, unregistration is redundant.");
    }

    final Optional<Actor> optionalFoundActor = actorRepo.findById(id);
    if (optionalFoundMovie.isEmpty()) {
      throw new Exception("Unregistration of cast between " 
          + "movie with IMDB id {" + imdbId + "} and actor with id {"
          + id + "} failed. " + "Actor could not be found in database."
          + "So, unregistration is redundant.");
    }

    Movie movie = optionalFoundMovie.get();
    Actor actor = optionalFoundActor.get();

    movie.getActors().remove(actor);
    actor.getMovies().remove(movie);
  }

  /**
   * Finds actor by id.
   * 
   * <p>Retrieves actor by calling 
   * {@link tv.beenius.videostore.data.ActorRepository#findById(Long)}.
   * 
   * @param id Actor identifier
   * @return Actor-
   * @throws Exception on exception from 
   * {@link tv.beenius.videostore.data.ActorRepository#findById(Long)}.
   */
  public Optional<Actor> findActorById(@NonNull Long id) throws Exception {
    return actorRepo.findById(id);
  }

  /**
   * Finds Movie by imdbId.
   * 
   * <p>Retrieves movie by calling 
   * {@link tv.beenius.videostore.data.MovieRepository#findById(String)}.
   * Actor contains related movies.
   * 
   * @param imdbId Movie identifier.
   * @return Movie.
   * @throws Exception on exception from 
   * {@link tv.beenius.videostore.data.MovieRepository#findById(String)}.
   */
  public Optional<Movie> findMovieById(@NonNull String imdbId) throws Exception {
    return movieRepo.findById(imdbId);
  }

  /**
   * Retrieves a list of movies containing search string..
   * 
   * <p>Retrieves a list of movies containing search string and sorted by title by calling 
   * {@link tv.beenius.videostore.data.MovieRepository#findByTitle(String)}.
   * Movie contains related actors.
   * 
   * <p>Movie does not contain movie images due to lazy fetch.
   * Call {@link #findMovieWithImagesById(String)} to retrieve specific movie with images..
   * 
   *  
   * @param searchFor Search string.
   * @return Movie
   * @throws Exception on exception from
   * {@link tv.beenius.videostore.data.MovieRepository#findByTitle(String)}.
   */
  public List<Movie> findMovieByTitle(@NonNull String searchFor) throws Exception {
    return movieRepo.findByTitle(searchFor);
  }

  /**
   * Retrieves a list of all actors.
   * 
   * <p>Retrieves a sorted (by last and first name) list of all actors by calling 
   * {@link tv.beenius.videostore.data.ActorRepository#findAll()}.
   * Actors contain related movies.
   * 
   * @return List of actors.
   * @throws Exception on exception from 
   * {@link tv.beenius.videostore.data.ActorRepository#findAll()}.
   */
  public List<Actor> findAllActors() throws Exception {
    return actorRepo.findAll();
  }

  /**
   * Retrieves a list of all movies.
   * 
   * <p>Retrieves a sorted (by title) list of all movies by calling 
   * {@link tv.beenius.videostore.data.MovieRepository#findAll()}.
   * Movies contain related actors.
   * 
   * <p>Movies do not contain movie images due to lazy fetch.
   * Call {@link #findMovieWithImagesById(String)} to retrieve specific movie with images.
   * 
   * @return List of movies.
   * @throws Exception on exception from 
   * {@link tv.beenius.videostore.data.MovieRepository#findAll()}.
   */
  public List<Movie> findAllMovies() throws Exception {
    return movieRepo.findAll();
  }

  /**
   * Retrieves movie with populated list of all movie images for a movie.
   * 
   * <p>Retrieves a sorted list (by description) of all movie images for a movie by calling 
   * {@link tv.beenius.videostore.data.MovieRepository#findWithImagesById(String)}.
   * 
   * @param imdbId Movie identifier.
   * @return Optional movie.
   * @throws Exception on exception from 
   * {@link tv.beenius.videostore.data.MovieRepository#findWithImagesById(String)}.
   */
  public Optional<Movie> findMovieWithImagesById(@NonNull String imdbId) throws Exception {
    return movieRepo.findWithImagesById(imdbId);
  }

  /**
   * Retrieves a movie image.
   * 
   * <p>Retrieves movie image by calling 
   * {@link tv.beenius.videostore.data.ImageRepository#findById(Long)}.
   * 
   * @param id Movie Image identifier.
   * @return Optiional movie image.
   * @throws Exception on exception from 
   * {@link tv.beenius.videostore.data.ImageRepository#findById(Long)}.
   */
  public Optional<Image> findMovieImageById(@NonNull Long id) throws Exception {
    return imageRepo.findById(id);
  }

  /**
   * Retrieves a page of movies.
   * 
   * <p>Retrieves a page from sorted (by title) list of all movies by calling 
   * {@link tv.beenius.videostore.data.MovieRepository#findPage(int, int)}.
   * 
   * <p>Movies do not contain movie images due to lazy fetch.
   * Call {@link #findMovieWithImagesById(String)} to retrieve specific movie with images.
   * Movies contain related actors.
   * 
   * @param startPosition Starting movie record for page. Records start with 0.
   * @param maxResult Maximum page size.
   * @return Sorted list of actors.
   * @throws Exception on exception from 
   * {@link tv.beenius.videostore.data.MovieRepository#findPage(int, int)}.
   */
  public List<Movie> findPageOfMovies(int startPosition, int maxResult) throws Exception {
    return movieRepo.findPage(startPosition, maxResult);
  }

  /**
   * Retrieves page of actors.
   * 
   * <p>Retrieves a page from sorted (by last and first name) list of all actors by calling 
   * {@link tv.beenius.videostore.data.ActorRepository#findPage(int, int)}.
   * Actors contain related movies.
   * 
   * @param startPosition Starting movie record for page. Records start with 0.
   * @param maxResult Maximum page size.
   * @return Sorted list of actors.
   * @throws Exception on exception from 
   * {@link tv.beenius.videostore.data.ActorRepository#findPage(int, int)}.
   */
  public List<Actor> findPageOfActors(int startPosition, int maxResult) throws Exception {
    return actorRepo.findPage(startPosition, maxResult);
  }

}
