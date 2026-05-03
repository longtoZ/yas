package com.yas.media.controller;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.commonlibrary.exception.ApiExceptionHandler;
import com.yas.media.model.Media;
import com.yas.media.model.dto.MediaDto;
import com.yas.media.service.MediaService;
import com.yas.media.viewmodel.MediaVm;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@ContextConfiguration(classes = {
    MediaController.class,
    ApiExceptionHandler.class
})
@AutoConfigureMockMvc(addFilters = false)
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MediaService mediaService;

    @Nested
    class CreateTest {

        @Test
        void create_whenRequestIsValid_thenReturnCreatedMediaWithoutFile() throws Exception {
            MockMultipartFile multipartFile = new MockMultipartFile(
                "multipartFile",
                "image.png",
                MediaType.IMAGE_PNG_VALUE,
                createImageBytes("png")
            );
            Media media = new Media();
            media.setId(1L);
            media.setCaption("sample");
            media.setFileName("image.png");
            media.setMediaType(MediaType.IMAGE_PNG_VALUE);

            when(mediaService.saveMedia(any())).thenReturn(media);

            mockMvc.perform(multipart("/medias")
                    .file(multipartFile)
                    .param("caption", "sample")
                    .param("fileNameOverride", "image.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.caption").value("sample"))
                .andExpect(jsonPath("$.fileName").value("image.png"))
                .andExpect(jsonPath("$.mediaType").value(MediaType.IMAGE_PNG_VALUE));

            verify(mediaService).saveMedia(any());
        }

        @Test
        void create_whenMultipartFileIsMissing_thenReturnBadRequest() throws Exception {
            mockMvc.perform(multipart("/medias")
                    .param("caption", "sample")
                    .param("fileNameOverride", "image.png"))
                .andExpect(status().isBadRequest());
        }

        @Test
        void create_whenMultipartFileTypeIsInvalid_thenReturnBadRequest() throws Exception {
            MockMultipartFile multipartFile = new MockMultipartFile(
                "multipartFile",
                "note.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "plain-text".getBytes(StandardCharsets.UTF_8)
            );

            mockMvc.perform(multipart("/medias")
                    .file(multipartFile)
                    .param("caption", "sample")
                    .param("fileNameOverride", "note.txt"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Request information is not valid"))
                .andExpect(jsonPath("$.fieldErrors[0]").value("multipartFile File type not allowed. Allowed types are: JPEG, PNG, GIF"));
        }
    }

    @Nested
    class DeleteTest {

        @Test
        void delete_whenIdExists_thenReturnNoContent() throws Exception {
            doNothing().when(mediaService).removeMedia(1L);

            mockMvc.perform(delete("/medias/1"))
                .andExpect(status().isNoContent());

            verify(mediaService).removeMedia(1L);
        }

        @Test
        void delete_whenMediaDoesNotExist_thenReturnNotFound() throws Exception {
            var message = "Media 99 is not found";
            org.mockito.Mockito.doThrow(new NotFoundException(message)).when(mediaService).removeMedia(99L);

            mockMvc.perform(delete("/medias/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(message));
        }
    }

    @Nested
    class GetTest {

        @Test
        void get_whenMediaExists_thenReturnOk() throws Exception {
            MediaVm mediaVm = new MediaVm(1L, "sample", "image.png", MediaType.IMAGE_PNG_VALUE, "/medias/1/file/image.png");
            when(mediaService.getMediaById(1L)).thenReturn(mediaVm);

            mockMvc.perform(get("/medias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.caption").value("sample"))
                .andExpect(jsonPath("$.fileName").value("image.png"))
                .andExpect(jsonPath("$.mediaType").value(MediaType.IMAGE_PNG_VALUE))
                .andExpect(jsonPath("$.url").value("/medias/1/file/image.png"));
        }

        @Test
        void get_whenMediaDoesNotExist_thenReturnNotFound() throws Exception {
            when(mediaService.getMediaById(99L)).thenReturn(null);

            mockMvc.perform(get("/medias/99"))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class GetByIdsTest {

        @Test
        void getByIds_whenMediaExists_thenReturnOk() throws Exception {
            MediaVm first = new MediaVm(1L, "sample-1", "image-1.png", MediaType.IMAGE_PNG_VALUE, "/medias/1/file/image-1.png");
            MediaVm second = new MediaVm(2L, "sample-2", "image-2.png", MediaType.IMAGE_JPEG_VALUE, "/medias/2/file/image-2.png");

            when(mediaService.getMediaByIds(List.of(1L, 2L))).thenReturn(List.of(first, second));

            mockMvc.perform(get("/medias").param("ids", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
        }

        @Test
        void getByIds_whenNoMediaFound_thenReturnNotFound() throws Exception {
            when(mediaService.getMediaByIds(List.of(5L))).thenReturn(List.of());

            mockMvc.perform(get("/medias").param("ids", "5"))
                .andExpect(status().isNotFound());
        }

        @Test
        void getByIds_whenIdsParameterIsMissing_thenReturnBadRequest() throws Exception {
            mockMvc.perform(get("/medias"))
                .andExpect(status().isBadRequest());
        }

        @Test
        void getByIds_whenIdsParameterIsEmpty_thenReturnBadRequest() throws Exception {
            mockMvc.perform(get("/medias").param("ids", ""))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetFileTest {

        @Test
        void getFile_whenRequestIsValid_thenReturnFileStream() throws Exception {
            byte[] fileContent = "file-content".getBytes(StandardCharsets.UTF_8);
            MediaDto mediaDto = MediaDto.builder()
                .content(new ByteArrayInputStream(fileContent))
                .mediaType(MediaType.IMAGE_PNG)
                .build();

            when(mediaService.getFile(1L, "image.png")).thenReturn(mediaDto);

            mockMvc.perform(get("/medias/1/file/image.png"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"image.png\""))
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(fileContent));

            verify(mediaService).getFile(eq(1L), eq("image.png"));
        }
    }

    private byte[] createImageBytes(String format) throws IOException {
        BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, format, outputStream);
        return outputStream.toByteArray();
    }
}
