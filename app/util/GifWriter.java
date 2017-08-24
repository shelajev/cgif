package util;

//
//  GifSequenceWriter.java
//
//  Created by Elliot Kroo on 2009-04-25.
//
// This work is licensed under the Creative Commons Attribution 3.0 Unported
// License. To view a copy of this license, visit
// http://creativecommons.org/licenses/by/3.0/ or send a letter to Creative
// Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.


import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * I've adapted this class just a bit, string values, API more to my taste, etc.
 *
 * The original creator is Elliot Kroo as mentioned above.
 */
public class GifWriter implements AutoCloseable {

  public static byte[] gifify(Collection<BufferedImage> imgs) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageOutputStream ios = new MemoryCacheImageOutputStream(baos);
      GifWriter gw = new GifWriter(ios, 500, "hello world");
      imgs.forEach(gw::addImage);
      gw.close();
      ios.flush();
      return baos.toByteArray();
    }
    catch (IOException e){
      throw new RuntimeException(e);
    }
  }

  protected ImageWriter gifWriter;
  protected ImageWriteParam imageWriteParam;
  protected IIOMetadata imageMetaData;

  public GifWriter(ImageOutputStream out, int delay, String comment) throws IOException {
    this(out, BufferedImage.TYPE_INT_RGB, delay, true, comment);
  }

  public GifWriter(
    ImageOutputStream outputStream,
    int imageType,
    int timeBetweenFramesMS,
    boolean loopContinuously, String comment) throws IIOException, IOException {

    gifWriter = getWriter();
    imageWriteParam = gifWriter.getDefaultWriteParam();
    ImageTypeSpecifier imageTypeSpecifier =
      ImageTypeSpecifier.createFromBufferedImageType(imageType);

    imageMetaData =
      gifWriter.getDefaultImageMetadata(imageTypeSpecifier,
        imageWriteParam);

    String metaFormatName = imageMetaData.getNativeMetadataFormatName();

    IIOMetadataNode root = (IIOMetadataNode)
      imageMetaData.getAsTree(metaFormatName);

    IIOMetadataNode graphicsControlExtensionNode = getNode(
      root,
      "GraphicControlExtension");

    graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
    graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
    graphicsControlExtensionNode.setAttribute(
      "transparentColorFlag",
      "FALSE");
    graphicsControlExtensionNode.setAttribute(
      "delayTime",
      Integer.toString(timeBetweenFramesMS / 10));
    graphicsControlExtensionNode.setAttribute(
      "transparentColorIndex",
      "0");

    IIOMetadataNode commentsNode = getNode(root, "CommentExtensions");
    commentsNode.setAttribute("CommentExtension", comment);

    IIOMetadataNode appEntensionsNode = getNode(
      root,
      "ApplicationExtensions");

    IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");

    child.setAttribute("applicationID", "HelloWorld");
    child.setAttribute("authenticationCode", "2.0");

    int loop = loopContinuously ? 0 : 1;

    child.setUserObject(new byte[]{ 0x1, (byte) (loop & 0xFF), (byte)
      ((loop >> 8) & 0xFF)});
    appEntensionsNode.appendChild(child);

    imageMetaData.setFromTree(metaFormatName, root);

    gifWriter.setOutput(outputStream);

    gifWriter.prepareWriteSequence(null);
  }

  public void addImage(BufferedImage img) {
    try {
      gifWriter.writeToSequence(
        new IIOImage(
          img,
          null,
          imageMetaData),
        imageWriteParam);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Close this GifSequenceWriter object. This does not close the underlying
   * stream, just finishes off the GIF.
   */
  public void close() throws IOException {
    gifWriter.endWriteSequence();
  }

  /**
   * Returns the first available GIF ImageWriter using
   * ImageIO.getImageWritersBySuffix("gif").
   *
   * @return a GIF ImageWriter object
   * @throws IIOException if no GIF image writers are returned
   */
  private static ImageWriter getWriter() throws IIOException {
    Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("gif");
    if(!iter.hasNext()) {
      throw new IIOException("No GIF Image Writers Exist");
    } else {
      return iter.next();
    }
  }

  /**
   * Returns an existing child node, or creates and returns a new child node (if
   * the requested node does not exist).
   *
   * @param rootNode the <tt>IIOMetadataNode</tt> to search for the child node.
   * @param nodeName the name of the child node.
   *
   * @return the child node, if found or a new node created with the given name.
   */
  private static IIOMetadataNode getNode(
    IIOMetadataNode rootNode,
    String nodeName) {
    int nNodes = rootNode.getLength();
    for (int i = 0; i < nNodes; i++) {
      if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName)
        == 0) {
        return((IIOMetadataNode) rootNode.item(i));
      }
    }
    IIOMetadataNode node = new IIOMetadataNode(nodeName);
    rootNode.appendChild(node);
    return(node);
  }
}