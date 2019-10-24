package tv.beenius.videostore.data;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;

import tv.beenius.videostore.model.Image;

@ApplicationScoped
public class ImageRepository {
  
  @Inject
  private EntityManager em;
    
  /**
   * Fetches image entity into persistence context.
   * Updates selected attributes description and contents.
   * 
   * @param image Updated image.
   * @throws Exception on update failure.
   */
  public void update(@NotNull Image image) throws Exception {
    try {
      Optional<Image> optionalImage = findById(image.getId());
      
      if (optionalImage.isPresent()) {
        Image retrievedImage = optionalImage.get();
        
        retrievedImage.setDescription(image.getDescription());
        retrievedImage.setContent(image.getContent());
      }
    } catch (Exception e) {
      throw new Exception(image.toString() + " failed to update. "
          + "Refresh movie and image sets. Check entity attributes.",
          e.getCause());
    }
  }
  
  /**
   * Retrieves an image by id.
   *  
   * @param id Image identifier.
   * @return Optional image.
   * @throws Exception on retrieval failure.
   */
  public Optional<Image> findById(@NotNull Long id) throws Exception {
    Image image;
    try {
      image = em.find(Image.class, id);
    } catch (Exception e) {
      throw new Exception(" Failed to retrieve image by id {" 
        + id + "}. Refresh image set and check request parameter id.",
        e.getCause());
    }
    
    return image != null ? Optional.of(image) : Optional.empty();
  }  
}
