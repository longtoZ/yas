package com.yas.media;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.yas.media.config.FilesystemConfig;
import com.yas.media.repository.FileSystemRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@Slf4j
class FileSystemRepositoryTest {

    @Mock
    private FilesystemConfig filesystemConfig;

    @InjectMocks
    private FileSystemRepository fileSystemRepository;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPersistFile_whenDirectoryNotExist_thenThrowsException() {
        String directoryPath = "non-exist-directory";
        String filename = "test-file.png";
        byte[] content = "test-content".getBytes();

        when(filesystemConfig.getDirectory()).thenReturn(directoryPath);

        assertThrows(IllegalStateException.class, () -> fileSystemRepository.persistFile(filename, content));
    }

    @Test
    void testPersistFile_whenFilenameContainsParentTraversal_thenThrowsException() {
        String filename = "../test-file.png";
        byte[] content = "test-content".getBytes();

        when(filesystemConfig.getDirectory()).thenReturn(tempDir.toString());

        IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> fileSystemRepository.persistFile(filename, content));

        assertEquals("Invalid filename", exception.getMessage());
    }

    @Test
    void testPersistFile_whenFilenameContainsPathSeparator_thenThrowsException() {
        String filename = "nested/test-file.png";
        byte[] content = "test-content".getBytes();

        when(filesystemConfig.getDirectory()).thenReturn(tempDir.toString());

        IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> fileSystemRepository.persistFile(filename, content));

        assertEquals("Invalid filename", exception.getMessage());
    }

    @Test
    void testPersistFile_whenDirectoryExistsAndFilenameIsSafe_thenWriteFile() throws IOException {
        String filename = "test-file.png";
        byte[] content = "test-content".getBytes();

        when(filesystemConfig.getDirectory()).thenReturn(tempDir.toString());

        String storedPath = fileSystemRepository.persistFile(filename, content);

        Path persistedFile = Path.of(storedPath);
        assertTrue(Files.exists(persistedFile));
        assertTrue(persistedFile.startsWith(tempDir));
        assertArrayEquals(content, Files.readAllBytes(persistedFile));
    }

    @Test
    void testGetFile_whenDirectIsExist_thenReturnFile() throws IOException {
        String filename = "test-file.png";
        byte[] content = "test-content".getBytes();
        Path filePath = tempDir.resolve(filename);

        when(filesystemConfig.getDirectory()).thenReturn(tempDir.toString());

        Files.write(filePath, content);

        InputStream inputStream = fileSystemRepository.getFile(filePath.toString());
        byte[] fileContent = inputStream.readAllBytes();
        assertArrayEquals(content, fileContent);
    }

    @Test
    void testGetFileDirectoryDoesNotExist_thenThrowsException() {
        String directoryPath = "non-exist-directory";
        String filename = "test-file.png";
        String filePathStr = Path.of(directoryPath, filename).toString();

        when(filesystemConfig.getDirectory()).thenReturn(directoryPath);

        assertThrows(IllegalStateException.class, () -> fileSystemRepository.getFile(filePathStr));
    }

    @Test
    void testGetFile_whenRequestedFileIsMissingInExistingDirectory_thenThrowsException() {
        String missingFilePath = tempDir.resolve("missing.png").toString();

        when(filesystemConfig.getDirectory()).thenReturn(tempDir.toString());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> fileSystemRepository.getFile(missingFilePath));

        assertEquals(String.format("Directory %s does not exist.", tempDir), exception.getMessage());
    }

}

