package com.example.photoGroupe.dto.category;

public class CategoryPreferenceDtos {

    public static class CategoryPreferenceResponse {
        private final Long categoryId;
        private final String categoryName;
        private final String categorySlug;
        private final String categoryCoverImage;
        private final double weight;
        private final String level; // human label: "See much less" ... "See much more"

        public CategoryPreferenceResponse(Long categoryId, String categoryName, String categorySlug,
                                          String categoryCoverImage, double weight, String level) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.categorySlug = categorySlug;
            this.categoryCoverImage = categoryCoverImage;
            this.weight = weight;
            this.level = level;
        }

        public Long getCategoryId()            { return categoryId; }
        public String getCategoryName()        { return categoryName; }
        public String getCategorySlug()        { return categorySlug; }
        public String getCategoryCoverImage()  { return categoryCoverImage; }
        public double getWeight()              { return weight; }
        public String getLevel()               { return level; }
    }
}