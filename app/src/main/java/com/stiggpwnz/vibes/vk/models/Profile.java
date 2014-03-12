package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "uid")
public class Profile implements Unit, Serializable {
    int uid;
    @JsonProperty("first_name")       String firstName;
    @JsonProperty("last_name")        String lastName;
    @JsonProperty("photo_medium_rec") String photoMediumRec;

    @Override public int getId() { return uid; }

    @Override public String getName() { return String.format("%s %s", firstName, lastName); }

    @Override public String getProfilePic() { return photoMediumRec; }
}
