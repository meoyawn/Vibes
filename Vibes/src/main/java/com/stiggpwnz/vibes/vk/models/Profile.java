package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Profile implements Unit, Serializable {

    private int    uid;
    private String firstName;
    private String lastName;
    private String photoMediumRec;

    @Override
    public String getName() { return String.format("%s %s", firstName, lastName); }

    @JsonProperty("uid") @Override
    public int getId() { return uid; }

    @JsonProperty("uid")
    public void setUid(int uid) { this.uid = uid; }

    @JsonProperty("first_name")
    public String getFirstName() { return firstName; }

    @JsonProperty("first_name")
    public void setFirstName(String firstName) { this.firstName = firstName; }

    @JsonProperty("last_name")
    public String getLastName() { return lastName; }

    @JsonProperty("last_name")
    public void setLastName(String lastName) { this.lastName = lastName; }

    @JsonProperty("photo_medium_rec") @Override
    public String getProfilePic() { return photoMediumRec; }

    @JsonProperty("photo_medium_rec")
    public void setPhotoMediumRec(String photoMediumRec) { this.photoMediumRec = photoMediumRec; }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + uid;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Profile other = (Profile) obj;
        if (uid != other.uid)
            return false;
        return true;
    }
}
