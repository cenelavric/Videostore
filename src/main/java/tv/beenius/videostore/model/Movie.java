package tv.beenius.videostore.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Entity implementation class for Movie.
 *
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "imdbId")
@Entity
@SuppressWarnings("serial")
@XmlRootElement
public class Movie implements Serializable {

  @Id
  @Pattern(regexp = "ev\\d{7}\\/\\d{4}(-\\d)?|(ch|co|ev|ni|nm|tt)\\d{7}", 
      message = "Identifier should match Internet Movie Database identifier pattern")
  @Column(name = "IMDB_ID")
  @NotNull
  private String imdbId;

  @NotNull
  @Size(min = 1, max = 600)
  @Column(columnDefinition = "VARCHAR_IGNORECASE(600)")
  private String title;

  @NotNull
  @Min(1900)
  @Max(2099)
  private Integer year;

  @Size(max = 10000)
  private String description;

  @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
  @JoinTable(name = "CAST", 
      joinColumns = { @JoinColumn(name = "IMDB_ID") }, 
      inverseJoinColumns = {@JoinColumn(name = "ACTOR_ID") })
  private Set<Actor> actors = new HashSet<>();

  @JsonIgnore
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinTable(name = "MOVIE_IMAGE", joinColumns = {
      @JoinColumn(name = "IMDB_ID", referencedColumnName = "IMDB_ID") }, inverseJoinColumns = {
          @JoinColumn(name = "IMAGE_ID", referencedColumnName = "ID", unique = true) })
  private Set<Image> images = new HashSet<>();

  public Movie() {
    super();
  }
  
  /**
   * Intended to enforce lazy entity retrieval.
   * 
   * @param imdbId Movie IMDB identifier.
   * @param title Movie title.
   * @param year Movie year.
   * @param description Movie description.
   */
  public Movie(String imdbId, String title, Integer year, String description) {
    super();
    this.imdbId = imdbId;
    this.title = title;
    this.year = year;
    this.description = description;
  }

  public String getImdbId() {
    return this.imdbId;
  }

  public void setImdbId(String id) {
    this.imdbId = id;
  }

  public String getTitle() {
    return this.title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Integer getYear() {
    return this.year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Set<Actor> getActors() {
    return this.actors;
  }

  public void setActors(Set<Actor> actors) {
    this.actors = actors;
  }

  public Set<Image> getImages() {
    return this.images;
  }

  public void setImages(Set<Image> images) {
    this.images = images;
  }

  /**
   *  Movie w/o actors toString().
   * @return movie string.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    
    sb.append("Movie {")
      .append("imdbId='").append(imdbId).append('\'')
      .append(", title='").append(title).append('\'')
      .append(", ").append(actorsToString())
      .append("}");
    
    return sb.toString();
  }
  
  /**
   * Set of actors toString().
   * @return actors string.
   */
  private String actorsToString() {
    
    StringBuilder sb = new StringBuilder();
    
    sb.append("actors={");
    
    if (actors.size() == 0) {
      return sb.append("}").toString();
    } else {
      sb.append(actors.stream().map(Actor::toStringLazily).collect(Collectors.joining(" ")));  
      sb.append("}");   
      return sb.toString();
    }
  }

  /**
   *  Movie w/o actors toString().
   * @return movie string.
   */
  public String toStringLazily() {
    StringBuilder sb = new StringBuilder();
    
    sb.append("Movie {")
      .append("imdbId='").append(imdbId).append('\'')
      .append(", title='").append(title).append('\'')
      .append("}");
    
    return sb.toString();
  }

}
