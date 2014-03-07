package com.stiggpwnz.vibes.vk.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

public @Data class Photo implements Serializable {
    @Nullable static Photo[] arrayFrom(@Nullable Attachment[] attachments) {
        if (attachments != null) {
            List<Photo> photoList = new ArrayList<>(attachments.length);
            for (Attachment attachment : attachments) {
                if (attachment.getType() == Attachment.Type.PHOTO) {
                    photoList.add(attachment.getPhoto());
                }
            }
            return photoList.toArray(new Photo[photoList.size()]);
        }
        return null;
    }

    String src;
    @NotNull String src_big;
    String src_small;
    String src_xbig;
    String src_xxbig;
    String src_xxxbig;
    int    width;
    int    height;
    String text;

    public float getRatio() { return (float) height / width; }

    @NotNull public String getUrl(int width) {
        if (width > 1500) {
            return urlFor1500Plus();
        }
        if (width > 1000) {
            return urlFor1000Plus();
        }
        if (width > 800) {
            return urlFor800Plus();
        }
        return src_big;
    }

    @NotNull String urlFor800Plus() {
        if (src_xbig != null) {
            return src_xbig;
        }
        return src_big;
    }

    @NotNull String urlFor1000Plus() {
        if (src_xxbig != null) {
            return src_xxbig;
        }
        return urlFor800Plus();
    }

    @NotNull String urlFor1500Plus() {
        if (src_xxxbig != null) {
            return src_xxxbig;
        }
        return urlFor1000Plus();
    }
}
