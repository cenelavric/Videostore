package tv.beenius.videostore.data.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import tv.beenius.videostore.data.ActorRepository;
import tv.beenius.videostore.data.ImageRepository;
import tv.beenius.videostore.data.MovieRepository;
import tv.beenius.videostore.model.Actor;
import tv.beenius.videostore.model.Image;
import tv.beenius.videostore.model.Movie;
import tv.beenius.videostore.service.RegisterService;
import tv.beenius.videostore.util.ImageUtil;
import tv.beenius.videostore.util.Resources;

@RunWith(Arquillian.class)
public class RegisterServiceIT {
  
  // Image files.
  private static String imageGroundhogDayAlarmClock_FILE_NAME = "GroundhogDayAlarmClock.jpg";
  private static String imageGroundhogDayPoster_FILE_NAME     = "GroundhogDayPoster.jpg";
  
  /**
   * Composes and deploys project to test container.
   * 
   * <p>@return WAR
   */
  @Deployment
  public static Archive<?> createTestArchive()  {
    Archive<?> testArchive = ShrinkWrap.create(WebArchive.class, "videostoreTest.war")
        .addClasses(
            Actor.class, 
            ActorRepository.class, 
            Image.class,
            ImageRepository.class,
            ImageUtil.class, 
            Movie.class, 
            MovieRepository.class,
            RegisterService.class,
            Resources.class)
        .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
        .addAsWebInfResource("videostore-test-ds.xml")
        .addAsResource(imageGroundhogDayAlarmClock_FILE_NAME)
        .addAsResource(imageGroundhogDayPoster_FILE_NAME);
    
    //System.out.println(testArchive.toString(true));
    return testArchive;
  }
  
  private static List<String> TABLE_NAMES = 
      new ArrayList<>(Arrays.asList("ACTOR", "CAST", "IMAGE", "MOVIE", "MOVIE_IMAGE"));

  @Inject
  RegisterService registerService;
  
  @Inject
  ImageUtil imageUtil;
  
  @PersistenceContext
  EntityManager em;

  @Inject
  UserTransaction utx;
  
  private Actor actorAndyMacDowell;
  private Actor actorBillMurray;
  private Actor actorChrisElliott;
  private Actor actorJesseEisenberg;
  
  private Image imageGroundhogDayAlarmClock;
  private Image imageGroundhogDayPoster;
  
  private Movie movieArtOfSelfdefense;
  private Movie movieGroudhogDay;
  private Movie movieZombieland;
  
  private List<Actor> registeredActors;
  private List<Image> registeredImages;
  private List<Movie> registeredMovies;
  
  private Optional<Actor> optionalRegisteredActor;
  private Optional<Image> optionalRegisteredImage;
  private Optional<Movie> optionalRegisteredMovie;
  
  private Actor registeredActor;
  private Image registeredImage;
  private Movie registeredMovie;
  
  private Long actorAndyMacDowellId;
  private Long actorBillMurrayId;
  
  private Long imageGroundhogDayPosterId;
  
  private byte[] imageGroundhogDayAlarmClockContent;
  private byte[] imageGroundhogDayPosterContent;
 
  /**
   * Initialize resources before each test.
   * 
   * <p>@throws Exception
   */
  @Before
  public void prepareTestData() throws Exception {
    clearPersistenceData();
    instantiateEntities();
  }

  /**
   * Clears out selected database tables.
   * 
   * <p>@throws Exception
   */  
  private void clearPersistenceData() throws Exception {
    utx.begin();
    em.flush();
    em.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
    TABLE_NAMES
        .forEach(tableName -> em.createNativeQuery("TRUNCATE TABLE " + tableName)
        .executeUpdate());
    em.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    utx.commit();
  }
  
  private void instantiateEntities() throws Exception {

    actorAndyMacDowell = new Actor();
    actorAndyMacDowell.setFirstName("Rosalie");
    actorAndyMacDowell.setLastName("MacDowell");
    actorAndyMacDowell.setBornDate(LocalDate.of(1958, 4, 21));
    
    actorBillMurray = new Actor();
    actorBillMurray.setFirstName("William");
    actorBillMurray.setLastName("Murray");
    actorBillMurray.setBornDate(LocalDate.of(1950, 9, 21));

    actorChrisElliott = new Actor();
    actorChrisElliott.setFirstName("Chriss");
    actorChrisElliott.setLastName("Elliott");
    actorChrisElliott.setBornDate(LocalDate.of(1960, 5, 31));
    
    actorJesseEisenberg = new Actor();
    actorJesseEisenberg.setFirstName("Jesse");
    actorJesseEisenberg.setLastName("Eisenberg");
    actorJesseEisenberg.setBornDate(LocalDate.of(1983, 10, 5));
      
    imageGroundhogDayAlarmClockContent = 
        imageUtil.getImageFromWar(imageGroundhogDayAlarmClock_FILE_NAME);
    imageGroundhogDayPosterContent = 
        imageUtil.getImageFromWar(imageGroundhogDayPoster_FILE_NAME);
        
    imageGroundhogDayAlarmClock = new Image();
    imageGroundhogDayAlarmClock.setDescription("Alarm clock from GroundhogDay.");
    imageGroundhogDayAlarmClock.setContent(imageGroundhogDayAlarmClockContent);
    
    imageGroundhogDayPoster = new Image();
    imageGroundhogDayPoster.setDescription("Poster of GroundhogDay.");
    imageGroundhogDayPoster.setContent(imageGroundhogDayPosterContent);
    
    movieArtOfSelfdefense = new Movie();
    movieArtOfSelfdefense.setImdbId("tt7339248");
    movieArtOfSelfdefense.setTitle("The Art of Self-Defense");
    movieArtOfSelfdefense.setDescription("After being attacked on the street "
        + "a young man enlists at a local dojo, led by a charismatic and mysterious sensei, "
        + "in an effort to learn how to defend himself from future threats.");
    movieArtOfSelfdefense.setYear(2019);

    movieGroudhogDay = new Movie();
    movieGroudhogDay.setImdbId("tt0107048");
    movieGroudhogDay.setTitle("Groundhog Day");
    movieGroudhogDay.setDescription("A weatherman finds himself inexplicably"
        + " living the same day over and over again.");
    movieGroudhogDay.setYear(1993);
    
    movieZombieland = new Movie();
    movieZombieland.setImdbId("tt1156398");
    movieZombieland.setTitle("Zombieland");
    movieZombieland.setDescription("A shy student trying to reach his family in Ohio,"
        + " a gun-toting tough guy trying to find the last Twinkie,"
        + " and a pair of sisters trying to get to an amusement park join forces" 
        + " to travel across a zombie-filled America.");
    movieZombieland.setYear(2009);
  }
  
  /**
   * Testing: JPA creation of entities via RegisterService.
   * Scenario: Register a movie with a movie image.
   * Expected: Movie with movie image becomes managed entity.
   */
  @Test
  public void testRegisterMovieWithImage() throws Exception {
    
    // Registration.
    
    movieGroudhogDay.getImages().add(imageGroundhogDayPoster);
    registerService.registerMovie(movieGroudhogDay);
    
    // Validate registration by retrieving movie with images.  
    
    optionalRegisteredMovie = registerService
        .findMovieWithImagesById(movieGroudhogDay.getImdbId());   
    assertTrue(optionalRegisteredMovie.isPresent());   
    registeredMovie = optionalRegisteredMovie.get();
    assertEquals(movieGroudhogDay.getImdbId(), registeredMovie.getImdbId());
    assertEquals(movieGroudhogDay.getTitle(), registeredMovie.getTitle());
    assertEquals(movieGroudhogDay.getDescription(), registeredMovie.getDescription());
    assertEquals(movieGroudhogDay.getYear(), registeredMovie.getYear());
    
    // Validate image registration by retrieving movie images.  
    
    registeredImages = new ArrayList<>(registeredMovie.getImages());
    assertThat(registeredImages, 
        Matchers.hasItem(
            Matchers.hasProperty("id",
                Matchers.is(imageGroundhogDayPoster.getId()))));
  }

  /**
   * Testing: JPA creation of entities via RegisterService.
   * Scenario: Register a movie with an actor.
   * Expected: Movie with actor become managed entities.
   */
  @Test
  public void testRegisterMovieWithActor() throws Exception {
    
    // Registration.
    
    movieGroudhogDay.getActors().add(actorAndyMacDowell);
    
    registerService.registerMovie(movieGroudhogDay);
    
    // Validate registration by retrieving movie.  
    
    registeredMovies = registerService.findAllMovies();   
    assertEquals(1, registeredMovies.size());
    
    registeredMovie = registeredMovies.get(0);
    assertEquals(movieGroudhogDay.getImdbId(), registeredMovie.getImdbId());
    assertEquals(movieGroudhogDay.getTitle(), registeredMovie.getTitle());
    assertEquals(movieGroudhogDay.getDescription(), registeredMovie.getDescription());
    assertEquals(movieGroudhogDay.getYear(), registeredMovie.getYear());
    
    registeredActors = new ArrayList<>(registeredMovie.getActors()); 
    assertEquals(1, registeredActors.size());

    registeredActor = registeredActors.get(0);
    assertEquals(actorAndyMacDowell.getFirstName(),registeredActor.getFirstName());
    assertEquals(actorAndyMacDowell.getLastName(), registeredActor.getLastName());
    assertEquals(actorAndyMacDowell.getBornDate(), registeredActor.getBornDate());

    // Validate registration by retrieving actor.  

    registeredActors = registerService.findAllActors();   
    assertEquals(1, registeredActors.size());
    
    registeredActor = registeredActors.get(0);
    assertEquals(actorAndyMacDowell.getFirstName(),registeredActor.getFirstName());
    assertEquals(actorAndyMacDowell.getLastName(), registeredActor.getLastName());
    assertEquals(actorAndyMacDowell.getBornDate(), registeredActor.getBornDate());

    registeredMovies = new ArrayList<>(registeredActor.getMovies());   
    assertEquals(1, registeredMovies.size());

    registeredMovie = registeredMovies.get(0);
    assertEquals(movieGroudhogDay.getImdbId(), registeredMovie.getImdbId());
    assertEquals(movieGroudhogDay.getTitle(), registeredMovie.getTitle());
    assertEquals(movieGroudhogDay.getDescription(), registeredMovie.getDescription());
    assertEquals(movieGroudhogDay.getYear(), registeredMovie.getYear());
  }

  /**
   * Testing: JPA creation of entities via RegisterService.
   * Scenario: Register an actor with a movie.
   * Expected: Actor with movie become managed entities.
   */
  @Test
  public void testRegisterActorWithMovie()  throws Exception {
    
    // Registration.
    
    actorAndyMacDowell.addMovie(movieGroudhogDay);  
    actorAndyMacDowellId = registerService.registerActor(actorAndyMacDowell);
    
    // Validate registration by retrieving actor.  

    registeredActors = registerService.findAllActors();   
    assertEquals(1, registeredActors.size());
    
    registeredActor = registeredActors.get(0);
    assertEquals(actorAndyMacDowell.getFirstName(),registeredActor.getFirstName());
    assertEquals(actorAndyMacDowell.getLastName(), registeredActor.getLastName());
    assertEquals(actorAndyMacDowell.getBornDate(), registeredActor.getBornDate());
    
    assertEquals(actorAndyMacDowellId, registeredActor.getId());

    registeredMovies = new ArrayList<>(registeredActor.getMovies());   
    assertEquals(1, registeredMovies.size());

    registeredMovie = registeredMovies.get(0);
    assertEquals(movieGroudhogDay.getImdbId(), registeredMovie.getImdbId());
    assertEquals(movieGroudhogDay.getTitle(), registeredMovie.getTitle());
    assertEquals(movieGroudhogDay.getDescription(), registeredMovie.getDescription());
    assertEquals(movieGroudhogDay.getYear(), registeredMovie.getYear());
    
    // Validate registration by retrieving movie.  
    
    registeredMovies = registerService.findAllMovies();   
    assertEquals(1, registeredMovies.size());
    
    registeredMovie = registeredMovies.get(0);
    assertEquals(movieGroudhogDay.getImdbId(), registeredMovie.getImdbId());
    assertEquals(movieGroudhogDay.getTitle(), registeredMovie.getTitle());
    assertEquals(movieGroudhogDay.getDescription(), registeredMovie.getDescription());
    assertEquals(movieGroudhogDay.getYear(), registeredMovie.getYear());
    
    registeredActors = new ArrayList<>(registeredMovie.getActors()); 
    assertEquals(1, registeredActors.size());

    registeredActor = registeredActors.get(0);
    assertEquals(actorAndyMacDowell.getFirstName(),registeredActor.getFirstName());
    assertEquals(actorAndyMacDowell.getLastName(), registeredActor.getLastName());
    assertEquals(actorAndyMacDowell.getBornDate(), registeredActor.getBornDate());
  }

  /**
   * Testing:  JPA creation of entities via RegisterService.
   * Scenario: Register two movies with same actor. First way to do it.
   * Expected: Movies with same actor become managed entities.
   */
  @Test
  public void testRegisterTwoMoviesWithActor1() throws Exception {
    
    // Registration.
     
    actorBillMurray.addMovie(movieGroudhogDay);
    actorBillMurray.addMovie(movieZombieland);
    registerService.registerActor(actorBillMurray);
    
    // Validate registration by retrieving movies.  
    
    registeredMovies = registerService.findAllMovies();   
    assertEquals(2, registeredMovies.size());

    // Validate registration by retrieving actors.  

    registeredActors = registerService.findAllActors();   
    assertEquals(1, registeredActors.size());    
  }

  /**
   * Testing: JPA creation of entities via RegisterService.
   * Scenario: Register two movies with same actor. Second way to do it.
   * Expected: Movies with same actor become managed entities.
  */
  @Test
  public void testRegisterTwoMoviesWithActor2() throws Exception {
    
    // Registration.
    
    movieGroudhogDay.getActors().add(actorBillMurray);  
    registerService.registerMovie(movieGroudhogDay);
    actorBillMurrayId = actorBillMurray.getId();   
    registerService.registerMovie(movieZombieland);   
    registerService.registerCast(actorBillMurray.getId(), movieZombieland.getImdbId());
    
    // Validate registration by retrieving movies.  
    
    registeredMovies = registerService.findAllMovies();   
    assertEquals(2, registeredMovies.size());

    // Validate registration by retrieving actors.  

    registeredActors = registerService.findAllActors();   
    assertEquals(1, registeredActors.size());    
  }

  /**
   * Testing: JPA creation of entities via RegisterService.
   * Scenario: Register two movies with same actor. Third way to do it.
   * Expected: Movies with same actor become managed entities.
   */
  @Test
  public void testRegisterTwoMoviesWithActor3() throws Exception {
     
    // Registration.
    registerService.registerMovie(movieGroudhogDay);   
    registerService.registerMovie(movieZombieland); 
    actorBillMurrayId = registerService.registerActor(actorBillMurray);  
    registerService.registerCast(actorBillMurrayId, movieGroudhogDay.getImdbId());
    registerService.registerCast(actorBillMurrayId, movieZombieland.getImdbId());
    
    // Validate registration by retrieving movies.  
    
    registeredMovies = registerService.findAllMovies();   
    assertEquals(2, registeredMovies.size());

    // Validate registration by retrieving actors.  

    registeredActors = registerService.findAllActors();   
    assertEquals(1, registeredActors.size());    
  }

  /**
   * Testing: JPA creation of entities via RegisterService.
   * Scenario: Register two actors with same movie. First way to do it.
   * Expected: Actors with same movie become managed entities.
   */
  @Test
  public void testRegisterTwoActorsWithMovie1() throws Exception {
     
    // Registration.
    
    movieGroudhogDay.getActors().add(actorAndyMacDowell);
    movieGroudhogDay.getActors().add(actorBillMurray);
    registerService.registerMovie(movieGroudhogDay);

    // Validate registration by retrieving actors.  

    registeredActors = registerService.findAllActors();   
    assertEquals(2, registeredActors.size());    
    
    // Validate registration by retrieving movies.  
    
    registeredMovies = registerService.findAllMovies();   
    assertEquals(1, registeredMovies.size());
  }

  /**
   * Testing: JPA creation of entities via RegisterService.
   * Scenario: Register two actors with same movie. Second way to do it.
   * Expected: Actors with same movie become managed entities.
   */
  @Test
  public void testRegisterTwoActorsWithMovie2() throws Exception {
    
    // Registration.
    
    actorAndyMacDowell.addMovie(movieGroudhogDay);
    registerService.registerActor(actorAndyMacDowell);  
    actorBillMurrayId = registerService.registerActor(actorBillMurray);   
    registerService.registerCast(actorBillMurrayId, movieGroudhogDay.getImdbId());

    // Validate registration by retrieving actors.  

    registeredActors = registerService.findAllActors();   
    assertEquals(2, registeredActors.size());    
    
    // Validate registration by retrieving movies.  
    
    registeredMovies = registerService.findAllMovies();   
    assertEquals(1, registeredMovies.size());
  }

  /**
   * Testing: JPA creation of entities via RegisterService.
   * Scenario: Register two actors with same movie. Third way to do it.
   * Expected: Actors with same movie become managed entities.
   */
  @Test
  public void testRegisterTwoActorsWithMovie3() throws Exception {
    
    // Registration.
    
    actorAndyMacDowellId = registerService.registerActor(actorAndyMacDowell);
    actorBillMurrayId = registerService.registerActor(actorBillMurray);
    registerService.registerMovie(movieGroudhogDay);
    registerService.registerCast(actorAndyMacDowellId, movieGroudhogDay.getImdbId());
    registerService.registerCast(actorBillMurrayId, movieGroudhogDay.getImdbId());

    // Validate registration by retrieving actors.  

    registeredActors = registerService.findAllActors();   
    assertEquals(2, registeredActors.size());    
    
    // Validate registration by retrieving movies.  
    
    registeredMovies = registerService.findAllMovies();   
    assertEquals(1, registeredMovies.size());
  }

  /**
   * Testing: JPA retrieval of entities via RegisterService.
   * Scenario: Register two movies. Find one of them by part of it's title.
   * Expected: Movie retrieved.
   */ 
  @Test
  public void testFindMovieByTitle() throws Exception {
     
    // Registration.
    
    registerService.registerMovie(movieGroudhogDay);
    registerService.registerMovie(movieZombieland);
    
    registeredMovies = registerService.findMovieByTitle("hog");
    
    // Validate retrieved movies.  
      
    assertEquals(1, registeredMovies.size());
    
    registeredMovie = registeredMovies.get(0);
    assertEquals(movieGroudhogDay.getTitle(), registeredMovie.getTitle()); 
  }

  /**
   * Testing: JPA retrieval of entities via RegisterService.
   * Scenario: Register two movies. Find one of them by imdbId.
   * Expected: Movie retrieved.
   */ 
  @Test
  public void testFindMovieById() throws Exception {
     
    // Registration.
    
    registerService.registerMovie(movieGroudhogDay);
    registerService.registerMovie(movieZombieland);
    
    optionalRegisteredMovie = registerService.findMovieById(movieGroudhogDay.getImdbId());
    
    // Validate retrieved movie.  
    
    assertTrue(optionalRegisteredMovie.isPresent());
    
    registeredMovie = optionalRegisteredMovie.get();
    assertEquals(movieGroudhogDay.getTitle(), registeredMovie.getTitle()); 
  }

  /**
   * Testing: JPA retrieval of entities via RegisterService.
   * Scenario: Register two actors. Find one of them by id.
   * Expected: Actor retrieved.
   */ 
  @Test
  public void testFindActorById() throws Exception {
     
    // Registration.
    
    actorAndyMacDowellId = registerService.registerActor(actorAndyMacDowell);
    actorBillMurrayId = registerService.registerActor(actorBillMurray);

    optionalRegisteredActor = registerService.findActorById(actorAndyMacDowellId);
    
    // Validate retrieved actor.  
    
    assertTrue(optionalRegisteredActor.isPresent());
    
    registeredActor = optionalRegisteredActor.get();
    assertEquals(actorAndyMacDowellId, registeredActor.getId()); 
  }

  /**
   * Testing: JPA update of entities via RegisterService.
   * Scenario: Update movie description..
   * Expected: Movie description successfully updated.
   */
  @Test
  public void testUpdateMovie() throws Exception {
    
    // Registration.
    
    registerService.registerMovie(movieGroudhogDay);
    
    optionalRegisteredMovie = registerService.findMovieById(movieGroudhogDay.getImdbId());   
    assertTrue(optionalRegisteredMovie.isPresent());
    registeredMovie = optionalRegisteredMovie.get();

    // Update movie.
    
    String newDescription = "During his visit in Punxsutawney weatherman finds himself "
        + "inexplicably living the same day over and over again.";
    registeredMovie.setDescription(newDescription);
    
    registerService.updateMovie(registeredMovie);
    
    // Validate result.
    optionalRegisteredMovie = registerService.findMovieById(movieGroudhogDay.getImdbId());   
    assertTrue(optionalRegisteredMovie.isPresent());
    registeredMovie = optionalRegisteredMovie.get();
    
    assertEquals(newDescription, registeredMovie.getDescription());
  }

  /**
   * Testing: JPA update of entities via RegisterService.
   * Scenario: Update movie image description.
   * Expected: Movie image description successfully updated.
   */
  @Test
  public void testUpdateMovieImage() throws Exception {
    
    // Registration.
    
    registerService.registerMovie(movieGroudhogDay);
    imageGroundhogDayPosterId = registerService
        .registerMovieImage(movieGroudhogDay.getImdbId(), imageGroundhogDayPoster);

    // Update movie image.
    String newDescription = "Advertisment for movie GroundhogDay.";
    imageGroundhogDayPoster.setDescription(newDescription);
    
    registerService.updateMovieImage(imageGroundhogDayPoster);
    
    // Validate update result.
    
    optionalRegisteredImage = registerService.findMovieImageById(imageGroundhogDayPosterId);
    assertTrue(optionalRegisteredImage.isPresent());
    registeredImage = optionalRegisteredImage.get();
    
    assertEquals(newDescription, registeredImage.getDescription());
  }

  /**
   * Testing: JPA update of entities via RegisterService.
   * Scenario: Update movie description..
   * Expected: Movie description successfully updated.
   */
  @Test
  public void testUpdateActor() throws Exception {
    
    // Registration.
    
    actorAndyMacDowellId = registerService.registerActor(actorAndyMacDowell);
    
    optionalRegisteredActor = registerService.findActorById(actorAndyMacDowellId);   
    assertTrue(optionalRegisteredActor.isPresent());
    registeredActor = optionalRegisteredActor.get();

    // Update actor.
    
    String newFirstName = "Andy";
    registeredActor.setFirstName(newFirstName);
    
    registerService.updateActor(registeredActor);
    
    // Validate result.
    
    optionalRegisteredActor = registerService.findActorById(actorAndyMacDowellId);   
    assertTrue(optionalRegisteredActor.isPresent());
    registeredActor = optionalRegisteredActor.get();
    
    assertEquals(newFirstName, registeredActor.getFirstName());
  }
  
  /**
   * Testing:  JPA deletion of entities via RegisterService.
   * Scenario: Register actor with two movies. Then unregister first movie.
   * Expected: Second movie with actor should remain.
   */
  @Test
  public void testUnRegisterMovie() throws Exception {
     
    // Registration.
    
    actorBillMurray.addMovie(movieGroudhogDay);
    actorBillMurray.addMovie(movieZombieland);
    registerService.registerActor(actorBillMurray);
    
    // Unregister first movie.
    
    registerService.unRegisterMovie(movieGroudhogDay.getImdbId());
    
    // Validate unRegistration by retrieving movies.  
    
    registeredMovies = registerService.findAllMovies();   
    assertEquals(1, registeredMovies.size());

    registeredMovie = registeredMovies.get(0);
    assertEquals(movieZombieland.getImdbId(), registeredMovie.getImdbId());
    
    // Validate unRegistration by retrieving actors.  

    registeredActors = registerService.findAllActors();   
    assertEquals(1, registeredActors.size());    
  }

  /**
   * Testing:  JPA deletion of entities via RegisterService.
   * Scenario: Register movie with with two actors. Then unregister first actor.
   * Expected: Second actor with movie should remain.
   */
  @Test
  public void testUnRegisterActor() throws Exception {
     
    // Registration.
    
    movieGroudhogDay.getActors().add(actorAndyMacDowell);
    movieGroudhogDay.getActors().add(actorBillMurray);
    registerService.registerMovie(movieGroudhogDay);
    
    // Unregister first actor.
    
    registerService.unRegisterActor(actorAndyMacDowell.getId());
    
    // Validate unRegistration by retrieving movies.  
    
    registeredMovies = registerService.findAllMovies();   
    assertEquals(1, registeredMovies.size());

    // Validate unRegistration by retrieving actors.  

    registeredActors = registerService.findAllActors();   
    assertEquals(1, registeredActors.size());    
    
    registeredActor = registeredActors.get(0);
    assertEquals(actorBillMurray.getId(), registeredActor.getId());
  }

  /**
   * Testing:  JPA deletion of entities via RegisterService.
   * Scenario: Register movie with two actors. Then unregister movie.
   * Expected: Both actors should remain.
   */
  @Test
  public void testUnRegisterSharedMovie() throws Exception {
    
    // Registration.
    
    movieGroudhogDay.getActors().add(actorAndyMacDowell);
    movieGroudhogDay.getActors().add(actorBillMurray);
    registerService.registerMovie(movieGroudhogDay);

    // Unregister movie.
    
    registerService.unRegisterMovie(movieGroudhogDay.getImdbId());
    
    // Validate unRegistration by retrieving movies.  

    registeredMovies = registerService.findAllMovies();   
    assertEquals(0, registeredMovies.size());    
   
    // Validate unRegistration by retrieving actors.  
    
    optionalRegisteredActor = registerService.findActorById(actorAndyMacDowell.getId());  
    assertTrue(optionalRegisteredActor.isPresent());
    
    registeredActor = optionalRegisteredActor.get();
    assertEquals(0, registeredActor.getMovies().size());

    optionalRegisteredActor = registerService.findActorById(actorBillMurray.getId());  
    assertTrue(optionalRegisteredActor.isPresent());
    
    registeredActor = optionalRegisteredActor.get();
    assertEquals(0, registeredActor.getMovies().size());
  }

  /**
   * Testing:  JPA deletion of entities via RegisterService.
   * Scenario: Register actor with two movies. Then unregister actor.
   * Expected: Both movies should remain.
   */
  @Test
  public void testUnRegisterSharedActor() throws Exception {
     
    // Registration.
    
    actorBillMurray.addMovie(movieGroudhogDay);
    actorBillMurray.addMovie(movieZombieland);
    actorBillMurrayId = registerService.registerActor(actorBillMurray);
    
    // Unregister actor.
    
    registerService.unRegisterActor(actorBillMurrayId);
    
    // Validate unRegistration by retrieving movies.  
    
    optionalRegisteredMovie = registerService.findMovieById(movieGroudhogDay.getImdbId());  
    assertTrue(optionalRegisteredMovie.isPresent());
    
    registeredMovie = optionalRegisteredMovie.get();
    assertEquals(0, registeredMovie.getActors().size());
    
    
    optionalRegisteredMovie = registerService.findMovieById(movieZombieland.getImdbId());  
    assertTrue(optionalRegisteredMovie.isPresent());
    
    registeredMovie = optionalRegisteredMovie.get();
    assertEquals(0, registeredMovie.getActors().size());
    // Validate unRegistration by retrieving actors.  

    registeredActors = registerService.findAllActors();  
    assertEquals(0, registeredActors.size());   
  }

  /**
   * Testing: JPA deleting relationship between entities via RegisterService.
   * Scenario: Register a movie with an actor. Delete relationship. 
   * Expected: Movie and actor remain managed entities.
   */
  @Test
  public void testUnRegisterCast() throws Exception {
    
    // Registration.
    
    movieGroudhogDay.getActors().add(actorAndyMacDowell);
    
    registerService.registerMovie(movieGroudhogDay);
    
    // UnRegister cast.
    
    registerService.unRegisterCast(movieGroudhogDay.getImdbId(), actorAndyMacDowell.getId());
    
    // Validate unRegistration by retrieving movie.  
    
    optionalRegisteredMovie = registerService.findMovieById(movieGroudhogDay.getImdbId());  
    assertTrue(optionalRegisteredMovie.isPresent());
    
    registeredMovie = optionalRegisteredMovie.get();
    assertEquals(0, registeredMovie.getActors().size());
    
    // Validate unRegistration by retrieving actor.  
    
    optionalRegisteredActor = registerService.findActorById(actorAndyMacDowell.getId());  
    assertTrue(optionalRegisteredActor.isPresent());
    
    registeredActor = optionalRegisteredActor.get();
    assertEquals(0, registeredActor.getMovies().size());
  }

  /**
   * Testing: JPA retrieval of entities via RegisterService.
   * Scenario: Register three movies. Define page size 2. 
   *           Find second page (starting with element 2). 
   *           Movies are sorted by title.
   * Expected: Single movie is found.
   */ 
  @Test
  public void testFindPageOfMovies() throws Exception {
     
    // Registration.
    
    registerService.registerMovie(movieArtOfSelfdefense);
    registerService.registerMovie(movieGroudhogDay);
    registerService.registerMovie(movieZombieland);
    
    // Retrieve second page of Movies.

    registeredMovies = registerService.findPageOfMovies(2, 2);
    
    // Validate retrieved movies.  
    
    assertEquals(1, registeredMovies.size()); 

    assertThat(registeredMovies, 
        Matchers.hasItem(
            Matchers.hasProperty("imdbId", Matchers.is(movieZombieland.getImdbId()))));
  }

  /**
   * Testing: JPA retrieval of entities via RegisterService.
   * Scenario: Register four actors. Define page size 2. 
   *           Find second page (starting with element 2). 
   *           Actors are sorted by last name.
   * Expected: Two actors are found.
   */ 
  @Test
  public void testFindPageOfActor() throws Exception {
     
    // Registration.
    
    actorAndyMacDowellId = registerService.registerActor(actorAndyMacDowell);
    actorBillMurrayId = registerService.registerActor(actorBillMurray);
    registerService.registerActor(actorChrisElliott);
    registerService.registerActor(actorJesseEisenberg);

    // Retrieve second page of actors.
    
    registeredActors = registerService.findPageOfActors(2, 2);
    
    // Validate retrieved actors.  
    
    assertEquals(2, registeredActors.size()); 

    assertThat(registeredActors, 
        Matchers.hasItem(
            Matchers.hasProperty("id", Matchers.is(actorAndyMacDowellId))));
    assertThat(registeredActors, 
        Matchers.hasItem(
            Matchers.hasProperty("id", Matchers.is(actorBillMurrayId))));
  }  

  /**
   * Testing: JPA registering image to a movie via RegisterService.
   * Scenario: Register an image to a registered movie. 
   * Expected: Image becomes managed entity.
   */
  @Test
  public void testRegisterMovieImage() throws Exception {
    
    // Registration.
    
    registerService.registerMovie(movieGroudhogDay);
    
    // Register movie image to movie.
    
    imageGroundhogDayPosterId = registerService
        .registerMovieImage(movieGroudhogDay.getImdbId(), imageGroundhogDayPoster);
   
    // Validate image registration by retrieving movie images. 
    optionalRegisteredMovie = registerService
        .findMovieWithImagesById(movieGroudhogDay.getImdbId());   
    assertTrue(optionalRegisteredMovie.isPresent());  
    registeredMovie = optionalRegisteredMovie.get(); 
    
    registeredImages = new ArrayList<>(registeredMovie.getImages());
    assertEquals(1, registeredImages.size());
    
    assertThat(registeredImages, 
        Matchers.hasItem(
            Matchers.hasProperty("id",
                Matchers.is(imageGroundhogDayPosterId))));
  }

  /**
   * Testing: JPA registering image to a movie via RegisterService.
   * Scenario: Register two images to a registered movie. Delete one image.
   * Expected: Second image remains managed entity.
   */
  @Test
  public void testUnregisterMovieImage() throws Exception {
    
    // Registration.
    
    movieGroudhogDay.getImages().add(imageGroundhogDayAlarmClock);
    movieGroudhogDay.getImages().add(imageGroundhogDayPoster);
    registerService.registerMovie(movieGroudhogDay);
    
    // Validate registration by retrieving movie with images.  
    
    optionalRegisteredMovie = registerService
        .findMovieWithImagesById(movieGroudhogDay.getImdbId());   
    assertTrue(optionalRegisteredMovie.isPresent());   
    registeredMovie = optionalRegisteredMovie.get();
    
    registeredImages = new ArrayList<>(registeredMovie.getImages());
    assertEquals(2, registeredImages.size());
    
    // Unregister image from movie.
    
    registerService.unRegisterMovieImage(
        movieGroudhogDay.getImdbId(), 
        imageGroundhogDayPoster.getId());
    
    // Validate unregistration by retrieving movie with images.  
    
    optionalRegisteredMovie = registerService
        .findMovieWithImagesById(movieGroudhogDay.getImdbId());   
    assertTrue(optionalRegisteredMovie.isPresent());   
    registeredMovie = optionalRegisteredMovie.get();
    
    registeredImages = new ArrayList<>(registeredMovie.getImages());
    assertEquals(1, registeredImages.size());
    
    registeredImage = registeredImages.get(0);
    assertEquals(imageGroundhogDayAlarmClock.getId(), registeredImage.getId());
  }

}
