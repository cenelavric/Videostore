package tv.beenius.videostore.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;
import javax.servlet.http.Part;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;

@SuppressWarnings("serial")
public class ImageUtil implements Serializable {
  
  public ImageUtil() {
    super();
  }

  /**
   * Reads image file from WAR.
   * 
   * @param fileName File name.
   * @return Byte array with image contents.
   */
  public byte[] getImageFromWar(String fileName) throws Exception {
    
    if (fileName == null) {
      throw new Exception("File name cannot be null.");
    }
  
    InputStream inputStream = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream(fileName);
    
    if (inputStream == null) {
      throw new Exception("Input stream for file with name {" + fileName + "} cannot be created.");
    }

    BufferedImage bufferedImage = ImageIO.read(inputStream);
    ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
    ImageIO.write(bufferedImage, "png", byteOutStream);
    
    return byteOutStream.toByteArray();
  }
  
  /**
   * Reads image file from file system.
   * 
   * @param fileName File name.
   * @return Byte array with image contents.
   */
  public byte[] getImageFromFileSystem(String fileName) throws Exception {

    if (fileName == null) {
      throw new Exception("File name cannot be null.");
    }
  
    File file = new File(fileName);
    
    if (! file.exists()) {
      throw new Exception("File with name {" + fileName + "} does not exist.");
    }
    
    BufferedImage bufferedImage = ImageIO.read(file);
    ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
    ImageIO.write(bufferedImage, "png", byteOutStream);
    
    return byteOutStream.toByteArray();
  }
  
  /**
   * Reads image from Part.
   * 
   * @param part Part.
   * @return Byte array with file content.
   * @throws IOException on read exception.
   */
  public byte[] getImageFromPart(Part part) throws IOException {
    
    if (part == null) {
      return null;
    }
    
    InputStream imageInputStream = part.getInputStream();
    
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int len;
    
    while ((len = imageInputStream.read(buffer)) != -1) {
      byteArrayOutputStream.write(buffer, 0, len);
    }
    
    return byteArrayOutputStream.toByteArray();
  }

  /**
   * Reads image from InputPart.
   * 
   * @param part Part.
   * @return Byte array with file content.
   * @throws IOException on read exception.
   */
  public byte[] getImageFromInputPart(InputPart part) throws IOException {
    
    if (part == null) {
      return null;
    }
    
    InputStream imageInputStream = part.getBody(InputStream.class,null);
    
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int len;
    
    while ((len = imageInputStream.read(buffer)) != -1) {
      byteArrayOutputStream.write(buffer, 0, len);
    }
    
    return byteArrayOutputStream.toByteArray();
  }

}
