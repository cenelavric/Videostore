package tv.beenius.videostore.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class ImageUtil {

  /**
   * Reads image file from WAR.
   * 
   * @param fileName File name.
   * @return Byte array with image contents.
   */
  public byte[] getImageFromWar(String fileName) throws Exception {
    InputStream inpuStream = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream(fileName);
    if (inpuStream != null) {
      try {
        BufferedImage bufferedImage = ImageIO.read(inpuStream);
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", byteOutStream);
        return byteOutStream.toByteArray();
      } catch (IOException e) {
        throw new Exception("File with name {" + fileName + "} could not be read.", e.getCause());
      }
    } else {
      throw new Exception("Input stream for file with name {" + fileName + "} cannot be created.");
    }
  }
  
  /**
   * Reads image file from file system.
   * 
   * @param fileName File name.
   * @return Byte array with image contents.
   */
  public byte[] getImageFromFileSystem(String fileName) throws Exception {
    File file = new File(fileName);
    if (file.exists()) {
      try {
        BufferedImage bufferedImage = ImageIO.read(file);
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", byteOutStream);
        return byteOutStream.toByteArray();
      } catch (IOException e) {
        throw new Exception("File with name {" + fileName + "} could not be read.", e.getCause());
      }
    } else {
      throw new Exception("File with name {" + fileName + "} does not exist.");
    }
  }

}
