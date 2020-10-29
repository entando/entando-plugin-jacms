package com.agiletec.plugins.jacms.aps.system.services.resource.model.imageresizer;

import static org.junit.Assert.assertEquals;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.entando.entando.ent.exception.EntException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PNGImageResizerTest {

    private PNGImageResizer pngImageResizer;

    @Before
    public void setUp() {
        pngImageResizer = new PNGImageResizer();
    }

    @Test
    public void toBufferedImage() throws IOException, EntException {

        File file = new File("src/test/resources/images/entando.png");

        BufferedImage image = ImageIO.read(file);
        ImageIcon imageIcon = new ImageIcon(image);

        BufferedImage response = pngImageResizer.toBufferedImage(imageIcon, image.getWidth(),
                image.getHeight());

        assertEquals(200, response.getRaster().getWidth());
        assertEquals(200, response.getRaster().getHeight());

    }

    @Test
    public void toBufferedImageWhenScreenIsNotPresent() throws IOException {

        File file = new File("src/test/resources/images/entando.png");

        BufferedImage image = ImageIO.read(file);
        ImageIcon imageIcon = new ImageIcon(image);

        BufferedImage response = pngImageResizer.toBufferedImageWhenScreenIsNotPresent(imageIcon, image.getWidth(),
                image.getHeight(), image,
                false);

        assertEquals(200, response.getRaster().getWidth());
        assertEquals(200, response.getRaster().getHeight());

        response = pngImageResizer.toBufferedImageWhenScreenIsNotPresent(imageIcon, image.getWidth(),
                image.getHeight(), image,
                true);

        assertEquals(200, response.getRaster().getWidth());
        assertEquals(200, response.getRaster().getHeight());
    }
}
