package tv.beenius.videostore.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;


/**
 * Entity implementation class for Image.
 *
 * <p>This entity stores images of Movies.
 *
 * <p>But in the future this entity might also store images of Actors.
 * That's why Movies comprise unidirectional one-one-to-many relationships.
 * Reference: https://en.wikibooks.org/wiki/Java_Persistence/OneToMany
 *
 */
@SuppressWarnings("serial")
@Entity
public class Image implements Serializable {

  @Id
  @GeneratedValue
  @Column(name = "ID")
  private Long id;

  @NotNull
  private String description;
  
  @Lob
  @NotNull
  private byte[] content;

  public Image() {
    super();
  }

  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public byte[] getContent() {
    return this.content;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    
    sb.append("Image {")
      .append("id='").append(id).append('\'')
      .append(", description='").append(description).append('\'')
      .append("}");
    
    return sb.toString();
  }

}
