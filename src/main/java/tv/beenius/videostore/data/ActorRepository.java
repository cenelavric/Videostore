package tv.beenius.videostore.data;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;

import tv.beenius.videostore.model.Actor;

@ApplicationScoped
public class ActorRepository {

  @Inject
  private EntityManager em;
  
  /**
   * Saves actor into database.
   * 
   * @param actor Actor attributes.
   * @return Auto-generated actor id.
   * @throws Exception on persistence failure.
   */
  public Long save(@NotNull Actor actor) throws Exception {
    try {
      em.persist(actor);
    } catch (Exception e) {
      throw new Exception(actor.toString() + " failed to persist. Check entity attributes.",
          e.getCause());
    }
    return actor.getId();
  }
  
  /**
   * Fetches actor entity into persistence context. of managed entity actor:
   * Updates selected attribute firstName, lastName and bornDate.
   * 
   * @param actor Updated actor.
   * @throws Exception on update failure.
   */
  public void update(@NotNull Actor actor) throws Exception {
    try {
      Optional<Actor> optionalActor = findById(actor.getId());
      
      if (optionalActor.isPresent()) {
        Actor retrievedActor = optionalActor.get();
        
        retrievedActor.setFirstName(actor.getFirstName());
        retrievedActor.setLastName(actor.getLastName().orElse(null));
        retrievedActor.setBornDate(actor.getBornDate());   
      }
    } catch (Exception e) {
      throw new Exception(actor.toString() + " failed to update. "
          + "Refresh actor set and check entity attributes.",
          e.getCause());
    }
  }
  
  /**
   * Retrieves actor by identifier.
   * 
   * @param id Actor identifier.
   * @return Optional actor.
   * @throws Exception on retrieval failure.
   */
  public Optional<Actor> findById(@NotNull Long id) throws Exception {
    Actor actor;
    try {
      actor = em.find(Actor.class, id);
    } catch (Exception e) {
      throw new Exception(" Failed to retrieve actor with id {" 
          + id + "}. Check database status.",
          e.getCause());
    }
    return actor != null ? Optional.of(actor) : Optional.empty();
  }
  
  /**
   * Retrieves all actors ordered by last and first name.
   * 
   * @return Sorted list of actors.
   * @throws Exception on retrieval failure.
   */
  public List<Actor> findAll() throws Exception {
    try {
      TypedQuery<Actor> q = em.createQuery(
          "SELECT a FROM Actor a "
          + "ORDER BY a.lastName, a.firstName", 
          Actor.class);   
      return q.getResultList();
    } catch (Exception e) {
      throw new Exception(" Failed to retrieve any actors. Check database status.",
          e.getCause());
    }
  }
  
  /**
   * Retrieves selected page of actors ordered by last and first name.
   * 
   * @param startPosition Starting actor record for page.
   * @param maxResult Maximum page size.
   * @return Sorted list of actors.
   * @throws Exception on retrieval failure.
   */
  public List<Actor> findPage(int startPosition, int maxResult) throws Exception {
    try {
      TypedQuery<Actor> q = em.createQuery(
          "SELECT a FROM Actor a ORDER BY a.lastName, a.firstName ", 
          Actor.class);
      q.setFirstResult(startPosition);
      q.setMaxResults(maxResult);  
      return q.getResultList();
    } catch (Exception e) {
      throw new Exception(" Failed to retrieve page of actors starting with " 
          + "{" + startPosition + "," + " with max size " + maxResult + "} of actors. " 
          + "Refresh actor set and check request parameters.",
          e.getCause());
    }
  }
  
  /**
   * Removes actor and movie references from database.
   * 
   * @param id Actor identifier.
   * @throws Exception on failure to remov actor.
   */
  public void removeById(@NotNull Long id) throws Exception {    
    try {
      Optional<Actor> optionalActor = findById(id);  
      if (optionalActor.isPresent()) {
        try {
          Actor actor = optionalActor.get();
          
          actor.getMovies().forEach(movie -> {
            movie.getActors().remove(actor);
          });      
          em.remove(actor);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      throw new Exception(" Failed to remove actor with id {" 
          + id + "}. Refresh actor set and check parameter id.",
          e.getCause());
    }
  }
  
}
