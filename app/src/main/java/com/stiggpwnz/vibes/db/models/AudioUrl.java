package com.stiggpwnz.vibes.db.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Created by adel on 15/03/14
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@AllArgsConstructor(suppressConstructorProperties = true)
public class AudioUrl {
    Long   _id;
    String url;
}
