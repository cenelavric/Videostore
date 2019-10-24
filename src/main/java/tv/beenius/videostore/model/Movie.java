package tv.beenius.videostore.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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

/**
 * Entity implementation class for Movie.
 *
 */
@Entity

@SuppressWarnings("serial")
public class Movie implements Serializable {

  @Id
  @Pattern(regexp = "ev\\d{7}\\/\\d{4}(-\\d)?|(ch|co|ev|ni|nm|tt)\\d{7}", 
      message = "Identifier should match Internet Movie Database identifier pattern")
  @Column(name = "IMDB_ID")
  @NotNull
  private String imdbId;

  @NotNull
  @Size(min = 1, max = 600)
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

  @OneToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "MOVIE_IMAGE", joinColumns = {
      @JoinColumn(name = "IMDB_ID", referencedColumnName = "IMDB_ID") }, inverseJoinColumns = {
          @JoinColumn(name = "IMAGE_ID", referencedColumnName = "ID", unique = true) })
  private Set<Image> images = new HashSet<>();

  public Movie() {
    super();
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

  @Override
  public String toString() {
    return "Movie {" + "imdbId=" + imdbId 
        + ", title='" + title + '\'' 
        + '}';
  }

}
