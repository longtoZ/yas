package com.yas.media.utils;

import jakarta.validation.ConstraintValidatorContext;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FileTypeValidatorTest {

    private static final String MESSAGE = "File type not allowed. Allowed types are: JPEG, PNG, GIF";

    private final FileTypeValidator validator = new FileTypeValidator();
    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        validator.initialize(new TestValidFileType());
        context = mock(ConstraintValidatorContext.class);
        violationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(MESSAGE)).thenReturn(violationBuilder);
    }

    @Test
    void isValid_whenFileIsNull_thenReturnFalse() {
        assertFalse(validator.isValid(null, context));

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(MESSAGE);
        verify(violationBuilder).addConstraintViolation();
    }

    @Test
    void isValid_whenContentTypeIsNull_thenReturnFalse() {
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getContentType()).thenReturn(null);

        assertFalse(validator.isValid(multipartFile, context));

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(MESSAGE);
        verify(violationBuilder).addConstraintViolation();
    }

    @Test
    void isValid_whenAllowedTypeAndImageIsValid_thenReturnTrue() throws IOException {
        MockMultipartFile multipartFile = new MockMultipartFile(
            "multipartFile",
            "image.png",
            "image/png",
            createImageBytes("png")
        );

        assertTrue(validator.isValid(multipartFile, context));
        verifyNoInteractions(context);
    }

    @Test
    void isValid_whenAllowedTypeButImageBytesAreInvalid_thenReturnFalse() {
        MockMultipartFile multipartFile = new MockMultipartFile(
            "multipartFile",
            "image.png",
            "image/png",
            "not-an-image".getBytes()
        );

        assertFalse(validator.isValid(multipartFile, context));
        verifyNoInteractions(context);
    }

    @Test
    void isValid_whenReadingFileThrowsIOException_thenReturnFalse() throws IOException {
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.getInputStream()).thenThrow(new IOException("boom"));

        assertFalse(validator.isValid(multipartFile, context));
        verifyNoInteractions(context);
    }

    @Test
    void isValid_whenTypeIsNotAllowed_thenReturnFalse() {
        MockMultipartFile multipartFile = new MockMultipartFile(
            "multipartFile",
            "note.txt",
            "text/plain",
            "plain-text".getBytes()
        );

        assertFalse(validator.isValid(multipartFile, context));

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(MESSAGE);
        verify(violationBuilder).addConstraintViolation();
    }

    private byte[] createImageBytes(String format) throws IOException {
        BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, format, outputStream);
        return outputStream.toByteArray();
    }

    private static final class TestValidFileType implements ValidFileType {

        @Override
        public String[] allowedTypes() {
            return new String[] {"image/jpeg", "image/png", "image/gif"};
        }

        @Override
        public String message() {
            return MESSAGE;
        }

        @Override
        public Class<?>[] groups() {
            return new Class<?>[0];
        }

        @Override
        public Class<? extends jakarta.validation.Payload>[] payload() {
            return new Class[0];
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return ValidFileType.class;
        }
    }
}
