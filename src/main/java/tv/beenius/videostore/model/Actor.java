package tv.beenius.videostore.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;

/**
 * Entity implementation class for Actor.
 *
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity
@SuppressWarnings("serial")
public class Actor implements Serializable {

  @Id
  @GeneratedValue
  @Column(name = "ID")
  private Long id;

  @NotNull
  @Column(name = "FIRST_NAME", columnDefinition = "VARCHAR_IGNORECASE(50)")
  private String firstName;

  @JsonProperty("lastName")
  @Column(name = "LAST_NAME", columnDefinition = "VARCHAR_IGNORECASE(50)")
  private String lastName;
 
  @JsonDeserialize(using = LocalDateDeserializer.class)  
  @JsonSerialize(using = LocalDateSerializer.class)
  @NotNull
  @PastOrPresent
  @Column(name = "BORN_DATE")
  private LocalDate bornDate;

  @ManyToMany(mappedBy = "actors", fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
  private Set<Movie> movies = new HashSet<>();

  public Actor() {
    super();
  }

  /**
   * Intended to enforce lazy entity retrieval.
   * 
   * @param id Actor identifier.
   * @param firstName First name.
   * @param lastName Last name.
   * @param bornDate Date of birth.
   */
  public Actor(Long id, String firstName, String lastName, LocalDate bornDate) {
    super();
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.bornDate = bornDate;
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
  @JsonIgnore
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

  /**
   *  Actor with movies toString().
   * @return actor string.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    
    sb.append("Actor {")
      .append("id='").append(id).append('\'')
      .append(", firstName='").append(firstName).append('\'')
      .append(", lastName='").append(lastName).append('\'')
      .append(", ").append(moviesToString())
      .append("}");
    
    return sb.toString();
  }
    
  /**
   * Set of movies toString().
   * @return movies string.
   */
  private String moviesToString() {
    
    StringBuilder sb = new StringBuilder();
    
    sb.append("movies={");
    
    if (movies.size() == 0) {
      return sb.append("}").toString();
    } else {
      sb.append(movies.stream().map(Movie::toStringLazily).collect(Collectors.joining(" ")));  
      sb.append("}");   
      return sb.toString();
    }
  }
  
  /**
   *  Actor w/o movies toString().
   * @return actor string.
   */
  public String toStringLazily() {
    StringBuilder sb = new StringBuilder();
    
    sb.append("Actor {")
      .append("id='").append(id).append('\'')
      .append(", firstName='").append(firstName).append('\'')
      .append(", lastName='").append(lastName).append('\'')
      .append("}");
    
    return sb.toString();
  }

}
