package com.yas.media.mapper;

import com.yas.media.model.Media;
import com.yas.media.viewmodel.MediaVm;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MediaVmMapperTest {

    private final MediaVmMapper mediaVmMapper = Mappers.getMapper(MediaVmMapper.class);

    @Test
    void toVm_whenMediaIsProvided_thenMapCommonFields() {
        Media media = new Media();
        media.setId(1L);
        media.setCaption("caption");
        media.setFileName("image.png");
        media.setFilePath("/tmp/image.png");
        media.setMediaType("image/png");

        MediaVm mediaVm = mediaVmMapper.toVm(media);

        assertEquals(1L, mediaVm.getId());
        assertEquals("caption", mediaVm.getCaption());
        assertEquals("image.png", mediaVm.getFileName());
        assertEquals("image/png", mediaVm.getMediaType());
        assertNull(mediaVm.getUrl());
    }

    @Test
    void toModel_whenMediaVmIsProvided_thenIgnoreUrlAndMapSharedFields() {
        MediaVm mediaVm = new MediaVm(2L, "caption", "image.jpeg", "image/jpeg", "/medias/2/file/image.jpeg");

        Media media = mediaVmMapper.toModel(mediaVm);

        assertEquals(2L, media.getId());
        assertEquals("caption", media.getCaption());
        assertEquals("image.jpeg", media.getFileName());
        assertEquals("image/jpeg", media.getMediaType());
        assertNull(media.getFilePath());
    }

    @Test
    void partialUpdate_whenVmContainsNulls_thenKeepExistingEntityValues() {
        Media media = new Media();
        media.setId(3L);
        media.setCaption("old-caption");
        media.setFileName("old-name.png");
        media.setFilePath("/tmp/old-name.png");
        media.setMediaType("image/png");

        MediaVm mediaVm = new MediaVm(3L, "new-caption", null, "image/jpeg", null);

        mediaVmMapper.partialUpdate(media, mediaVm);

        assertEquals(3L, media.getId());
        assertEquals("new-caption", media.getCaption());
        assertEquals("old-name.png", media.getFileName());
        assertEquals("/tmp/old-name.png", media.getFilePath());
        assertEquals("image/jpeg", media.getMediaType());
    }
}
