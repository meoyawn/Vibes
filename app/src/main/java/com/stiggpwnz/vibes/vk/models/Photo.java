package com.stiggpwnz.vibes.vk.models;

import java.io.Serializable;

public class Photo implements Serializable {

    public String src;
    public String src_big;
    public String src_small;
    public String src_xbig;
    public String src_xxbig;
    public String src_xxxbig;
    public int    width;
    public int    height;
    public String text;

    public float getRatio() {
        return (float) height / width;
    }

    public String getUrl(int width) {
        String url;
        if (width > 1500) {
            if (src_xxxbig != null) {
                url = src_xxxbig;
            } else if (src_xxbig != null) {
                url = src_xxbig;
            } else if (src_xbig != null) {
                url = src_xbig;
            } else {
                url = src_big;
            }
        } else if (width > 1000) {
            if (src_xxbig != null) {
                url = src_xxbig;
            } else if (src_xbig != null) {
                url = src_xbig;
            } else {
                url = src_big;
            }
        } else if (width > 800) {
            if (src_xbig != null) {
                url = src_xbig;
            } else {
                url = src_big;
            }
        } else {
            url = src_big;
        }
        return url;
    }
}