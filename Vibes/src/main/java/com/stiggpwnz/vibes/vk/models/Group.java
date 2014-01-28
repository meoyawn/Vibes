package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Group implements Unit, Serializable {

    public int    gid;
    public String name;
    public String photo_medium;

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
        if (getClass() != obj.getClass())
            return false;
        Group other = (Group) obj;
        if (gid != other.gid)
            return false;
        return true;
    }

    @Override
    public int getId() { return -gid; }

    @Override
    public String getName() { return name; }

    @Override
    public String getProfilePic() { return photo_medium; }
}
