package com.example.aeroalarm;

import com.google.gson.annotations.SerializedName;

public class JikanResponse {
    @SerializedName("data")
    private CharacterData data;

    public CharacterData getData() {
        return data;
    }

    public static class CharacterData {
        @SerializedName("images")
        private CharacterImages images;

        @SerializedName("name")
        private String name;

        public CharacterImages getImages() {
            return images;
        }

        public String getName() {
            return name;
        }
    }

    public static class CharacterImages {
        @SerializedName("jpg")
        private ImageType jpg;

        public ImageType getJpg() {
            return jpg;
        }
    }

    public static class ImageType {
        @SerializedName("image_url")
        private String imageUrl;

        public String getImageUrl() {
            return imageUrl;
        }
    }
}
