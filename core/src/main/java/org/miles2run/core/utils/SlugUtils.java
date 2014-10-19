package org.miles2run.core.utils;

import com.github.slugify.Slugify;

public abstract class SlugUtils {
    private static Slugify slugify = new Slugify();

    public static String toSlug(String text) {
        return slugify.slugify(text);
    }
}
