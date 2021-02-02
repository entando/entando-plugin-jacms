package com.agiletec.plugins.jacms.aps.system.services.resource.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MultiInstanceResourceTest {

    @Spy TestResource resource;

    @BeforeEach
    public void setUp() {
        resource.resetFileExists();
    }

    @Test
    public void firstFileReturnsCorrectName() {
        String newFileName = resource.getNewInstanceFileName("myFile.jpg", 0, null);

        assertEquals("myFile_d0.jpg", newFileName);
    }

    @Test
    public void thirdFileReturnsCorrectName() {
        String newFileName = resource.getNewInstanceFileName("myFile.jpg", 2, null);

        assertEquals("myFile_d2.jpg", newFileName);
    }

    @Test
    public void fileRepeatedReturnsCorrectName() {

        // Setup to say that file with the same name already exists
        resource.setFileExistsCountdown(2);

        String newFileName = resource.getNewInstanceFileName("myFile.jpg", 0, null);
        assertEquals("myFile_2_d0.jpg", newFileName);
    }

    @Test
    public void fourthFileWithTenRepeatedReturnsCorrectName() {
        // Setup to say that file with the same name already exists
        resource.setFileExistsCountdown(10);

        String tenthName = resource.getNewInstanceFileName("myFile.jpg", 4, null);
        assertEquals("myFile_10_d4.jpg", tenthName);
    }

    @Test
    public void secondFileWithFourRepeatedAndItalianReturnsCorrectName() {
        // Setup to say that file with the same name already exists
        resource.setFileExistsCountdown(4);

        String tenthName = resource.getNewInstanceFileName("myFile.jpg", 2, "it");
        assertEquals("myFile_4_d2_it.jpg", tenthName);
    }

    @Test
    public void secondFileWithFourRepeatedButAlreadySaved() {
        // Setup to say that file with the same name already exists
        resource.setFileExistsCountdown(4);

        String tenthName = resource.getNewInstanceFileName("myFile.jpg", 2, "it", true);
        assertEquals("myFile_d2_it.jpg", tenthName);
    }


    static abstract class TestResource extends AbstractMultiInstanceResource {

        // How many times should reply that the file exists
        private int fileExistsCountdown = 0;

        @Override
        protected boolean exists(String instanceFileName) {
            boolean fileExists = fileExistsCountdown > 0;

            fileExistsCountdown --;

            return fileExists;
        }

        void setFileExistsCountdown(int fileExistsCountdown) {
            this.fileExistsCountdown = fileExistsCountdown;
        }

        void resetFileExists() {
            fileExistsCountdown = 0;
        }
    }


}