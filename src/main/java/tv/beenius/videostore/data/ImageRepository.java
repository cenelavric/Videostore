package tv.beenius.videostore.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import tv.beenius.videostore.model.Image;
import tv.beenius.videostore.model.Movie;

@ApplicationScoped
public class ImageRepository {
  
  @Inject
  private EntityManager em;
    
  /**
   * Fetches image entity into persistence context.
   * Updates selected attributes description and contents.
   * 
   * @param image Updated image.
   * @return Optional image.
   */
  public Optional<Image> update(Image image) {
    Optional<Image> optionalImage = findById(image.getId());
    
    if (optionalImage.isPresent()) {
      Image retrievedImage = optionalImage.get();
      
      if (image.getDescription() != null) {
        retrievedImage.setDescription(image.getDescription());
      }
      
      if (image.getContent() != null) {
        retrievedImage.setContent(image.getContent());
      }
    }
    
    return optionalImage;
  }
  
  /**
   * Retrieves movie images. 
   * 
   * @param imdbId Movie identifier.
   * @return List of images.
   */
  public List<Image> findMovieImagesById(String imdbId) {
    TypedQuery<Movie> q = em.createQuery(
        "  SELECT m FROM Movie m "
        + "LEFT JOIN FETCH m.images i "
        + "WHERE m.imdbId = :imdbId " 
        + "ORDER BY i.description", 
        Movie.class);
    q.setParameter("imdbId", imdbId);
    
    List<Movie> movies = q.getResultList();
    
    if (movies.isEmpty()) {
      return new ArrayList<Image>();
    }
    
    Movie movie = movies.get(0);
    
    return movie != null ? new ArrayList<Image>(movie.getImages()) : new ArrayList<Image>();
  }
  
  /**
   * Retrieves a movie image. 
   * 
   * @param imdbId Movie identifier.
   * @return Optional image.
   */
  public Optional<Image> findMovieImageById(String imdbId, Long id) {
    TypedQuery<Movie> q = em.createQuery(
        "  SELECT m FROM Movie m "
        + "LEFT JOIN FETCH m.images i "
        + "WHERE m.imdbId = :imdbId " 
        + "  AND i.id = :id " 
        + "ORDER BY i.description", 
        Movie.class);
    q.setParameter("imdbId", imdbId);
    q.setParameter("id", id);
    
    List<Movie> movies = q.getResultList();
    
    if (movies.isEmpty()) {
      return Optional.empty();
    }
    
    List<Image> images = new ArrayList<Image>(movies.get(0).getImages());
    
    return images.isEmpty() ? Optional.empty() : Optional.of(images.get(0));
  } 
  
  /**
   * Retrieves an image by id.
   *  
   * @param id Image identifier.
   * @return Optional image.
   */
  public Optional<Image> findById(Long id) {
    Image image = em.find(Image.class, id);
    
    return image != null ? Optional.of(image) : Optional.empty();
  }  
  
}
