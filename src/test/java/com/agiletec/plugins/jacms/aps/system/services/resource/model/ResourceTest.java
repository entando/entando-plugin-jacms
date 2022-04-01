package com.agiletec.plugins.jacms.aps.system.services.resource.model;

import org.mockito.Spy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceTest {

    @Spy private TestResource resource;

    @BeforeEach
    public void setUp() {
        resource.resetFileExists();
    }

    @Test
    public void nullFilenameShouldReturnException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            resource.getUniqueBaseName(null);
        });
    }

    @Test
    public void regularFileNameShouldReturnFileNameBase() {
        String originalName = "myfile.jpg";
        String baseName = "myfile";

        String uniqueBaseName = resource.getUniqueBaseName(originalName);

        assertEquals(baseName, uniqueBaseName);
    }

    @Test
    public void repeatedFileNameShouldReturnFileNameBase() {
        String originalName = "myfile.jpg";
        String baseName = "myfile";

        // First call
        String uniqueBaseName = resource.getUniqueBaseName(originalName);
        assertEquals(baseName, uniqueBaseName);

        // How many times we want the mock to say that file exists
        resource.setFileExistsCountdown(1);

        // Second call
        String secondBaseName = resource.getUniqueBaseName(originalName);
        assertEquals(baseName + "_1", secondBaseName);

        // With even more repeated files
        resource.setFileExistsCountdown(10);

        //
        String tenthName = resource.getUniqueBaseName(originalName);
        assertEquals(baseName + "_10", tenthName);
    }

    static abstract class TestResource extends AbstractResource {

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