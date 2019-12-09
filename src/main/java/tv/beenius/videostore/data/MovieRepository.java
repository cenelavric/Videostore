package tv.beenius.videostore.data;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;

import tv.beenius.videostore.model.Image;
import tv.beenius.videostore.model.Movie;

@ApplicationScoped
public class MovieRepository {
  
  @Inject
  private EntityManager em;
  
  /**
   * Saves movie into database.
   * 
   * @param movie Movie attributes.
   * @return Movie Saved movie.
   */
  public Movie save(Movie movie) {  
    em.persist(movie);
    return movie;
  }
  
  /**
   * Fetches movie entity into persistence context.
   * Updates selected attributes title, description and year.
   * 
   * @param movie Movie to be updated.
   * @return Optional Movie.
   */
  public Optional<Movie> update(Movie movie) {
    Optional<Movie> optionalMovie = findById(movie.getImdbId());
    
    if (optionalMovie.isPresent()) {
      Movie retrievedMovie = optionalMovie.get();
      
      if (movie.getTitle() != null) {
        retrievedMovie.setTitle(movie.getTitle());
      }
      if (movie.getDescription() != null) {
        retrievedMovie.setDescription(movie.getDescription());
      }
      if (movie.getYear() != null) {
        retrievedMovie.setYear(movie.getYear());
      }
    }
    
    return optionalMovie;
  }
  
  /**
   * Retrieves a single movie by identifier.
   *  
   * @param imdbId Movie identifier.
   * @return Optional movie together with related actors and images.
   */
  public Optional<Movie> findById(String imdbId) {
    Movie movie  = em.find(Movie.class, imdbId);   
    return movie != null ? Optional.of(movie) : Optional.empty();
  }

  /**
   * Retrieves a single movie by identifier lazily.
   *  
   * @param imdbId Movie identifier.
   * @return Optional movie w/o related actors and images.
   */
  public Optional<Movie> findByIdLazily(String imdbId) {
    
    TypedQuery<Movie> q = em.createQuery(
        "  SELECT new Movie(m.imdbId, m.title, m.year, m.description) "
        + "FROM Movie m "
        + "WHERE m.imdbId =  :imdbId", Movie.class);
    q.setParameter("imdbId", imdbId);
    
    List<Movie> movies = q.getResultList();
  
    return movies.size() != 0 ? Optional.of(movies.get(0)) : Optional.empty();
  }
  
  /**
   * Retrieves a sorted list of movies containing search string.
   * 
   * @param searchFor Search string from Title.
   * @return List of selected movies ordered by title.
   */
  public List<Movie> findByTitle(String searchFor) {
    TypedQuery<Movie> q = em.createQuery(
        "  SELECT new Movie(m.imdbId, m.title, m.year, m.description) FROM Movie m "
        + "WHERE m.title LIKE :likeString "
        + "ORDER BY m.title", 
        Movie.class);
    q.setParameter("likeString","%" + searchFor + "%");
    
    return q.getResultList();
  }
  
  /**
   * Retrieves all movies ordered by title.
   * 
   * @return List of movies.
   */
  public List<Movie> findAll() {
    TypedQuery<Movie> q = em.createQuery(
        "  SELECT new Movie(m.imdbId, m.title, m.year, m.description) FROM Movie m "
        + "ORDER BY m.title", 
        Movie.class);  
    
    return q.getResultList();
  }

  /**
   * Retrieves all movies for an actor by identifier lazily.
   *  
   * @param id Actor identifier.
   * @return List of movies.
   */
  public List<Movie> findActorMoviesLazily(Long id) {
    
    TypedQuery<Movie> q = em.createQuery(
        "  SELECT NEW Movie(m.imdbId, m.title, m.year, m.description) "
        + "FROM Movie m "
        + "JOIN m.actors a "
        + "WHERE a.id = :id " 
        + "ORDER BY m.title", 
        Movie.class);
    q.setParameter("id", id);
  
    return q.getResultList();
  }    
  
  /**
   * Retrieves selected page of movies lazily ordered by title.
   * 
   * @param startPosition Starting movie record for page.
   * @param maxResult Maximum page size.
   * @return Page with sorted list movies.
   */
  public List<Movie> findPage(int startPosition, int maxResult) {
    TypedQuery<Movie> q = em.createQuery(
        "  SELECT NEW Movie(m.imdbId, m.title, m.year, m.description) FROM Movie m "
        + "ORDER BY m.title", Movie.class);
    q.setFirstResult(startPosition);
    q.setMaxResults(maxResult); 
    
    return q.getResultList();
  }

  /**
   * Retrieves selected page of movies lazily with title containing searchFor ordered by title.
   * 
   * @param startPosition Starting movie record for page.
   * @param maxResult Maximum page size.
   * @param searchFor Search string from title.
   * @return Page with sorted list of filtered movies.
   */
  public List<Movie> findPageByTitle(int startPosition, int maxResult, String searchFor) {
  
    TypedQuery<Movie> q = em.createQuery(
        "  SELECT NEW Movie(m.imdbId, m.title, m.year, m.description) FROM Movie m "
        + "WHERE m.title LIKE :likeString "
        + "ORDER BY m.title", Movie.class);
    q.setParameter("likeString","%" + searchFor + "%");
    q.setFirstResult(startPosition);
    q.setMaxResults(maxResult); 
    
    return q.getResultList();
  }
  
  /**
   * Removes a movie, images and actor references from database.
   * Operation is idempotent.
   *  
   * @param imdbId Movie identifier.
   * @return Returns true when entity actually existed.
   */
  public boolean removeById(String imdbId) {
    Optional<Movie> optionalMovie = findById(imdbId);
    
    if (optionalMovie.isPresent()) {
      Movie movie = optionalMovie.get();
      movie.getActors().forEach(actor -> {
        actor.getMovies().remove(movie);
      });
      em.remove(movie);
      return true;
    }
    
    return false;
  }
  
  /**
   * Saves a movie image.
   * 
   * @param imdbId Movie identifier.
   * @param image Image to be added.
   * @return image.
   */
  public Image saveMovieImage(String imdbId, Image image) {
    Optional<Movie> optionalMovie = findById(imdbId);
    Movie retrievedMovie = optionalMovie.get();
    
    retrievedMovie.getImages().add(image);
    
    em.flush();
    
    return image;
  }

  /**
   * Find an image in movie.
   * 
   * @param imdbId Movie identifier.
   * @param id Image identifier.
   */
  public Optional<Image> findMovieImageById(String imdbId, Long id) {
    
    Optional<Movie> optionalMovie = findById(imdbId);
    
    if (optionalMovie.isEmpty()) {
      return Optional.empty();
    }
    
    Movie retrievedMovie = optionalMovie.get();
    
    Iterator<Image> iterator =  retrievedMovie.getImages().iterator();
    while (iterator.hasNext()) {
      Image image = (Image) iterator.next();
      if (image.getId().equals(id)) {
        return Optional.of(image);
      }
    }
    
    return Optional.empty();
  }
  
  /**
   * Removes an image from a movie.
   * Operation is idempotent.
   * 
   * <p>Movie persistence must be checked by caller.
   * 
   * @param imdbId Movie identifier.
   * @param id Image identifier.
   * @return Returns true when entity actually existed.
   */
  public boolean removeMovieImageById(String imdbId, Long id) {
    
    Optional<Movie> optionalMovie = findById(imdbId);
    Movie retrievedMovie = optionalMovie.get();
    
    Set<Image> retrievedImages = retrievedMovie.getImages();
    
    Iterator<Image> iterator =  retrievedMovie.getImages().iterator();
    while (iterator.hasNext()) {
      Image image = (Image) iterator.next();
      if (image.getId().equals(id)) {
        retrievedImages.remove(image);
        em.remove(image);
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * Counts all movies.
   * 
   * @return Number of all movies.
   */
  public long count() {
    TypedQuery<Long> q = em.createQuery("  SELECT COUNT(m) from Movie m", Long.class);
    
    return q.getSingleResult();
  }

  /**
   * Counts movies containing search string.
   * 
   * @param searchFor Search string from Title.
   * @return Number of filtered movies.
   */
  public long countByTitle(String searchFor) {
    TypedQuery<Long> q = em.createQuery(
        "  SELECT COUNT(m) from Movie m "
        + "WHERE m.title LIKE :likeString", 
        Long.class);
    q.setParameter("likeString","%" + searchFor + "%");

    return q.getSingleResult();
  }

  /**
   * Counts all images for a movie. 
   * 
   * @param imdbId Movie identifier.
   * @return Number of images for a movie.
   */
  public long countMovieImages(@NotNull String imdbId) {
    TypedQuery<Long> q = em.createQuery(
        "  SELECT COUNT(i) FROM Movie m "
        + "JOIN m.images i "
        + "WHERE m.imdbId = :imdbId", 
        Long.class);
    q.setParameter("imdbId", imdbId);
    
    return q.getSingleResult();
  }
  
}
