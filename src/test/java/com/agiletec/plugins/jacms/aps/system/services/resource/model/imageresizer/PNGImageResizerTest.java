package com.agiletec.plugins.jacms.aps.system.services.resource.model.imageresizer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.entando.entando.ent.exception.EntException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PNGImageResizerTest {

    private PNGImageResizer pngImageResizer;

    @BeforeEach
    public void setUp() {
        pngImageResizer = new PNGImageResizer();
    }

    @Test
    public void bufferedImageToBufferedImage() throws IOException, EntException {

        File file = new File("src/test/resources/images/entando.png");

        BufferedImage image = ImageIO.read(file);
        ImageIcon imageIcon = new ImageIcon(image);

        BufferedImage response = pngImageResizer.toBufferedImage(imageIcon, image.getWidth(),
                image.getHeight());

        assertEquals(200, response.getRaster().getWidth());
        assertEquals(200, response.getRaster().getHeight());
    }

    @Test
    public void volatileImageToBufferedImage() throws IOException, EntException {
        File file = new File("src/test/resources/images/entando.png");
        BufferedImage image = ImageIO.read(file);
        Graphics2D g = image.createGraphics();

        VolatileImage vImage = g.getDeviceConfiguration().createCompatibleVolatileImage(image.getWidth(null),
                image.getHeight(null));

        BufferedImage response = pngImageResizer.toBufferedImage(new ImageIcon(vImage), image.getWidth(null),
                image.getHeight(null));

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
