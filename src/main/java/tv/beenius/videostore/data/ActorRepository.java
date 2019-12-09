package tv.beenius.videostore.data;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import tv.beenius.videostore.model.Actor;

@ApplicationScoped
public class ActorRepository {

  @Inject
  private EntityManager em;
  
  /**
   * Saves actor into database.
   * 
   * @param actor Actor attributes.
   * @return Actor Saved actor.
   */
  public Actor save(Actor actor) {
    em.persist(actor);
    return actor;
  }
  
  /**
   * Fetches actor entity into persistence context.
   * Updates selected attribute firstName, lastName and bornDate.
   * 
   * @param actor Updated actor.
   * @return Optional Actor.
   */
  public Optional<Actor> update(Actor actor) {
    Optional<Actor> optionalActor = findById(actor.getId());
    
    if (optionalActor.isPresent()) {
      Actor retrievedActor = optionalActor.get();
      
      if (actor.getFirstName() != null) {
        retrievedActor.setFirstName(actor.getFirstName());
      }
      
      if (actor.getLastName().isPresent()) {
        retrievedActor.setLastName(actor.getLastName().orElse(null));
      }
      
      if (actor.getBornDate() != null) {
        retrievedActor.setBornDate(actor.getBornDate());
      }   
    }
    
    return optionalActor;
  }
  
  /**
   * Retrieves actor by identifier.
   * 
   * @param id Actor identifier.
   * @return Optional actor.
   */
  public Optional<Actor> findById(Long id) {
    Actor actor = em.find(Actor.class, id);
    return actor != null ? Optional.of(actor) : Optional.empty();
  }

  /**
   * Retrieves a single actor by identifier lazily.
   *  
   * @param id Actor identifier.
   * @return Optional actor w/o related movies.
   */
  public Optional<Actor> findByIdLazily(Long id) {
    
    TypedQuery<Actor> q = em.createQuery(
        "  SELECT new Actor(a.id, a.firstName, a.lastName, a.bornDate) "
        + "FROM Actor a "
        + "WHERE a.id =  :id", Actor.class);
    q.setParameter("id", id);
    
    List<Actor> actors = q.getResultList();
  
    return actors.size() != 0 ? Optional.of(actors.get(0)) : Optional.empty();
  }  
  
  /**
   * Retrieves all actors ordered by last and first name.
   * 
   * @return Sorted list of actors.
   */
  public List<Actor> findAll() {
    TypedQuery<Actor> q = em.createQuery(
        "SELECT a FROM Actor a "
        + "ORDER BY a.lastName, a.firstName", 
        Actor.class);   

    return q.getResultList();
  }
  
  /**
   * Retrieves all actors for a movie by identifier lazily.
   *  
   * @param imdbId Movie identifier.
   * @return List of actors.
   */
  public List<Actor> findMovieActorsLazily(String imdbId) {
    
    TypedQuery<Actor> q = em.createQuery(
        "  SELECT NEW Actor(a.id, a.firstName, a.lastName, a.bornDate) "
        + "FROM Actor a "
        + "JOIN a.movies m "
        + "WHERE m.imdbId = :imdbId " 
        + "ORDER BY a.lastName, a.firstName", 
        Actor.class);
    q.setParameter("imdbId", imdbId);
  
    return q.getResultList();
  }    
  
  /**
   * Retrieves selected page of actors ordered by last and first name.
   * 
   * @param startPosition Starting actor record for page.
   * @param maxResult Maximum page size.
   * @return Sorted list of actors.
   */
  public List<Actor> findPage(int startPosition, int maxResult) {
    TypedQuery<Actor> q = em.createQuery(
        "  SELECT NEW Actor(a.id, a.firstName, a.lastName, a.bornDate) "
        + "FROM Actor a "
        + "ORDER BY a.lastName, a.firstName ", Actor.class);
    q.setFirstResult(startPosition);
    q.setMaxResults(maxResult);  
    
    return q.getResultList();
  }
  
  /**
   * Retrieves selected page of actors ordered by last and first name.
   * 
   * @param startPosition Starting actor record for page.
   * @param maxResult Maximum page size.
   * @return Sorted list of actors.
   */
  public List<Actor> findPageByName(int startPosition, int maxResult, String searchFor) {
    TypedQuery<Actor> q = em.createQuery(
        "  SELECT NEW Actor(a.id, a.firstName, a.lastName, a.bornDate) "
        + "FROM Actor a "
        + "WHERE a.firstName LIKE :likeString OR a.lastName LIKE :likeString "
        + "ORDER BY a.lastName, a.firstName ", Actor.class);
    q.setParameter("likeString","%" + searchFor + "%");
    q.setFirstResult(startPosition);
    q.setMaxResults(maxResult);  
    
    return q.getResultList();
  }  
  
  /**
   * Removes actor and movie references from database.
   * Operation is idempotent.
   * 
   * @param id Actor identifier.
   * @return Returns true when entity actually existed.
   */
  public boolean removeById(Long id) {    
    Optional<Actor> optionalActor = findById(id);  
    if (optionalActor.isPresent()) {
      Actor actor = optionalActor.get();
      
      actor.getMovies().forEach(movie -> {
        movie.getActors().remove(actor);
      });      
      em.remove(actor);
      return true;
    } 
    
    return false;
  }
 
  /**
   * Counts all actors.
   * 
   * @return Number of all actors.
   */
  public long count() {
    TypedQuery<Long> q = em.createQuery(
        "  SELECT COUNT(a) from Actor a", 
        Long.class);    
    return q.getSingleResult();
  }
  
  /**
   * Counts actors with name containing search string.
   * 
   * @param searchFor Search string from firstname/lastname.
   * @return Number of filtered actors.
   */
  public long countByName(String searchFor) {
    TypedQuery<Long> q = em.createQuery(
        "  SELECT COUNT(a) from Actor a "
        + "WHERE a.firstName LIKE :likeString OR a.lastName LIKE :likeString", 
        Long.class);
    q.setParameter("likeString","%" + searchFor + "%");

    return q.getSingleResult();
  }

}
