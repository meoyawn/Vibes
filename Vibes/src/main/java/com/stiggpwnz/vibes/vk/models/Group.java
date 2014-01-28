package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Group implements Unit, Serializable {

    private int    gid;
    private String name;
    private String photoMedium;

    @Override
    @JsonProperty("gid")
    public int getId() { return -gid; }

    public void setGid(int gid) { this.gid = gid; }

    @Override
    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    @Override
    @JsonProperty("photo_medium")
    public String getProfilePic() { return photoMedium; }

    @JsonProperty("photo_medium")
    public void setPhotoMedium(String photoMedium) { this.photoMedium = photoMedium; }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + gid;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (((Object) this).getClass() != obj.getClass())
            return false;
        Group other = (Group) obj;
        if (gid != other.gid)
            return false;
        return true;
    }
}
