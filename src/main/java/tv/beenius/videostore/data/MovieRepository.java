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
   * @return Movie imdbId.
   * @throws Exception persistence failure.
   */
  public String save(@NotNull Movie movie) throws Exception {
    try {
      em.persist(movie);
    } catch (Exception e) {
      throw new Exception(movie.toString() + " failed persisting. Check entity attributes.",
          e.getCause());
    }
    return movie.getImdbId();
  }
  
  /**
   * Fetches movie entity into persistence context.
   * Updates selected attributes title, description, year and images.
   * 
   * @param movie Updated movie.
   * @throws Exception on update failure.
   */
  public void update(@NotNull Movie movie) throws Exception {
    try {
      Optional<Movie> optionalMovie = findById(movie.getImdbId());
      
      if (optionalMovie.isPresent()) {
        Movie retrievedMovie = optionalMovie.get();
        
        retrievedMovie.setTitle(movie.getTitle());
        retrievedMovie.setDescription(movie.getDescription());
        retrievedMovie.setYear(movie.getYear());
        
        retrievedMovie.setImages(movie.getImages());
        
        // Enforce generation of image entity identifiers.
        em.flush();
      }
    } catch (Exception e) {
      throw new Exception(movie.toString() + " failed to update. "
          + "Refresh movie set and check entity attributes.",
          e.getCause());
    }
  }
  
  /**
   * Retrieves a movie by identifier.
   *  
   * @param imdbId Movie identifier.
   * @return Optional movie.
   * @throws Exception on retrieval failure.
   */
  public Optional<Movie> findById(@NotNull String imdbId) throws Exception {
    Movie movie;
    try {
      movie = em.find(Movie.class, imdbId);
    } catch (Exception e) {
      throw new Exception(" Failed to retrieve movie with imdbId {" 
        + imdbId + "}. Check database status.",
        e.getCause());
    }
    
    return movie != null ? Optional.of(movie) : Optional.empty();
  }
  
  /**
   * Retrieves a sorted list of movies containing search string.
   * 
   * @param searchFor Search string.
   * @return List of selected movies ordered by title.
   * @throws Exception on retrieval failure.
   */
  public List<Movie> findByTitle(@NotNull String searchFor) throws Exception {
    try {
      TypedQuery<Movie> q = em.createQuery(
          "  SELECT m FROM Movie m "
          + "WHERE m.title LIKE :likeString "
          + "ORDER BY m.title", 
          Movie.class);
      q.setParameter("likeString","%" + searchFor + "%");
      return q.getResultList();
    } catch (Exception e) {
      throw new Exception(" Failed to retrieve movie(s) with title containing "
          + " search string {" + searchFor + "}. Check database status.",
          e.getCause());
    }
  }
  
  /**
   * Retrieves all movies ordered by title.
   * 
   * @return List of movies.
   * @throws Exception on retrieval failure.
   */
  public List<Movie> findAll() throws Exception {
    try {
      TypedQuery<Movie> q = em.createQuery(
          "  SELECT m from Movie m "
          + "ORDER BY m.title", 
          Movie.class);    
      return q.getResultList();
    } catch (Exception e) {
      throw new Exception(" Failed to retrieve any movies. Check database status.",
          e.getCause());
    }
  }

  /**
   * Retrieves all images for a movie. 
   * 
   * @param imdbId Movie identifier.
   * @return List of movies.
   * @throws Exception on retrieval failure.
   */
  public Optional<Movie> findWithImagesById(@NotNull String imdbId) throws Exception {
    try {
      TypedQuery<Movie> q = em.createQuery(
          "  SELECT m FROM Movie m "
          + "LEFT JOIN FETCH m.images i "
          + "WHERE m.imdbId = :imdbId " 
          + "ORDER BY i.description", 
          Movie.class);
      q.setParameter("imdbId", imdbId);
      
      Movie movie = q.getSingleResult();
      
      return movie != null ? Optional.of(movie) : Optional.empty();
    } catch (Exception e) {
      throw new Exception(" Failed to retrieve any images for movie with imdbId {" 
        + imdbId + "}. Refresh movie set and check parameter imdbId.",
        e.getCause());
    }
  }
  
  /**
   * Retrieves selected page of movies ordered by title.
   * 
   * @param startPosition Starting movie record for page.
   * @param maxResult Maximum page size.
   * @return Sorted list of actors.
   * @throws Exception on retrieval failure.
   */
  public List<Movie> findPage(int startPosition, int maxResult) throws Exception {
    try {
      TypedQuery<Movie> q = em.createQuery("SELECT m from Movie m ORDER BY m.title", Movie.class);
      q.setFirstResult(startPosition);
      q.setMaxResults(maxResult); 
      return q.getResultList();
    } catch (Exception e) {
      throw new Exception(" Failed to retrieve page of movies starting with " 
        + "{" + startPosition + "," + " with max size " + maxResult + "} of actors. " 
        + "Refresh actor set and check request parameters.",
        e.getCause());
    }
  }
  
  /**
   * Removes a movie, images and actor references from database.
   *  
   * @param imdbId Movie identifier.
   * @throws Exception on failure to remove movie.
   */
  public void removeById(@NotNull String imdbId) throws Exception {
    try {
      Optional<Movie> optionalMovie = findById(imdbId);
      
      if (optionalMovie.isPresent()) {
        Movie movie = optionalMovie.get();
        movie.getActors().forEach(actor -> {
          actor.getMovies().remove(movie);
        });
        em.remove(movie);
      }
    } catch (Exception e) {
      throw new Exception(" Failed to remove movie with imdbId {" 
          + imdbId + "}. Refresh movie set and check parameter imdbid.",
          e.getCause());
    }
  }
  
  /**
   * Removes an image from a movie and database.
   * 
   * @param imdbId Movie identifier.
   * @param id Image identifier.
   * @throws Exception on failure to remove image.
   */
  public void removeMovieImageById(@NotNull String imdbId, @NotNull Long id) throws Exception {
    try {
      
      Optional<Movie> optionalMovie = findWithImagesById(imdbId);
      if (optionalMovie.isEmpty()) {
        throw new Exception("Movie with id {" + imdbId + "} could not be retreieved.");      
      }
      Movie retrievedMovie = optionalMovie.get();
      
      Set<Image> retrievedImages = retrievedMovie.getImages();
      
      Iterator<Image> iterator =  retrievedMovie.getImages().iterator();
      while (iterator.hasNext()) {
        Image image = (Image) iterator.next();
        if (image.getId() == id) {
          retrievedImages.remove(image);
          return;
        }
      }
    } catch (Exception e) {
      throw new Exception(" Failed to remove image with id {" + id + "} from movie with imdbId {" 
          + imdbId + "}. Refresh movie image set and check for parameters imdbId and id.",
          e.getCause());
    }
  }
  
}
