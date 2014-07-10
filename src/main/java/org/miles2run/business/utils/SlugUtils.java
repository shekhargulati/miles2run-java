package org.miles2run.business.utils;

import com.github.slugify.Slugify;

/**
 * Created by shekhargulati on 10/07/14.
 */
public abstract class SlugUtils {
    private static Slugify slugify = new Slugify();

    public static String toSlug(String text) {
        return slugify.slugify(text);
    }
}
