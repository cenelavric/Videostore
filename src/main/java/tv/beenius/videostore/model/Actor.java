package tv.beenius.videostore.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;

/**
 * Entity implementation class for Actor.
 *
 */
@Entity

@SuppressWarnings("serial")
public class Actor implements Serializable {

  @Id
  @GeneratedValue
  @Column(name = "ID")
  private Long id;

  @NotNull
  @Column(name = "FIRST_NAME")
  private String firstName;

  @Column(name = "LAST_NAME")
  private String lastName;

  @NotNull
  @Column(name = "BORN_DATE")
  private LocalDate bornDate;

  @ManyToMany(mappedBy = "actors", fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
  private Set<Movie> movies = new HashSet<>();

  public Actor() {
    super();
  }

  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFirstName() {
    return this.firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * Optional supports mononymous (w/o last name) celebrities.
   */
  public Optional<String> getLastName() {
    return Optional.ofNullable(this.lastName);
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public LocalDate getBornDate() {
    return this.bornDate;
  }

  public void setBornDate(LocalDate bornDate) {
    this.bornDate = bornDate;
  }

  public Set<Movie> getMovies() {
    return this.movies;
  }

  public void setMovies(Set<Movie> movies) {
    this.movies = movies;
  }
  
  // Must be always used instead of List.add() method  
  // for adding movies on dependent side of bi-directional relationship.
  public void addMovie(Movie movie) {
    movies.add(movie);
    movie.getActors().add(this);
  }

  @Override
  public String toString() {
    return "Actor {" + "id=" + id 
        + ", firstName='" + firstName + '\'' 
        + ", lastName='" + lastName + '\'' 
        + '}';
  }

}
