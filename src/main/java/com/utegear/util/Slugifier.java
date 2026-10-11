package com.utegear.util;
import java.text.Normalizer; import java.util.Locale;
public final class Slugifier { private Slugifier(){} public static String slugify(String value){ return Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT).replace('đ','d'),Normalizer.Form.NFD).replaceAll("\\p{M}","").replaceAll("[^a-z0-9]+","-").replaceAll("(^-|-$)",""); } }
