package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "gid", doNotUseGetters = true)
public class Group implements Unit, Serializable {
    int    gid;
    String name;
    @JsonProperty("photo_medium") String photoMedium;

    @Override public int getId() { return -gid; }

    @Override public String getProfilePic() { return photoMedium; }
}
