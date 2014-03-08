package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.stiggpwnz.vibes.util.HtmlDeserializer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.Data;
import lombok.EqualsAndHashCode;
import rx.functions.Func1;

@Data
@EqualsAndHashCode(of = "aid")
public class Audio implements Serializable {
    public static Func1<Audio[], Audio[]> removeFirstItem() {
        return audios -> {
            Audio[] copy = new Audio[audios.length - 1];
            System.arraycopy(audios, 1, copy, 0, audios.length - 1);
            return copy;
        };
    }

    int aid;
    @JsonProperty("owner_id")                        int    ownerId;
    @JsonDeserialize(using = HtmlDeserializer.class) String artist;
    @JsonDeserialize(using = HtmlDeserializer.class) String title;
    int duration;
    @JsonProperty("lyrics_id") int lyricsId;
    String url;

    @NotNull static Audio[] arrayFrom(@Nullable Attachment[] attachments) {
        if (attachments != null) {
            List<Audio> audioList = new ArrayList<>(attachments.length);
            for (Attachment attachment : attachments) {
                if (attachment.getType() == Attachment.Type.AUDIO) {
                    audioList.add(attachment.getAudio());
                }
            }
            return audioList.toArray(new Audio[audioList.size()]);
        }
        throw new RuntimeException("didn't find any audios");
    }

    public String ownerIdAidParam() { return String.format(Locale.US, "%d_%d", ownerId, aid); }
}
