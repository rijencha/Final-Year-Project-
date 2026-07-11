package com.example.photoGroupe.service.upload;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadProfilePicture(MultipartFile file, Long userId) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder",         "photogroupe/profiles",
                        "public_id",      "user_" + userId,   // overwrites old pic automatically
                        "overwrite",      true,
                        "resource_type",  "image"
                )
        );
        return (String) result.get("secure_url");  // ← HTTPS url to store in DB
    }

    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    public Map<?, ?> uploadCategoryImage(MultipartFile file, String slug) throws IOException {
        return cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder",        "photogroupe/categories",
                        "public_id",     "category_" + slug,
                        "overwrite",     true,
                        "resource_type", "image"
                )
        );
    }

    public String uploadWorkshopCover(MultipartFile file, Long workshopId) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder",        "photogroupe/workshops",
                        "public_id",     "workshop_cover_" + workshopId,
                        "overwrite",     true,
                        "resource_type", "image"
                )
        );
        return (String) result.get("secure_url");
    }

    public String[] uploadAlbumCover(MultipartFile file, Long userId) throws IOException {
        String publicId = "photogroupe/albums/cover_" + userId + "_" + System.currentTimeMillis();

        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "public_id",     publicId,
                        "overwrite",     false,
                        "resource_type", "image",
                        "quality",       "auto",
                        "fetch_format",  "auto"
                )
        );

        return new String[]{
            (String) result.get("secure_url"),
                (String) result.get("public_id")
        };
    }
}
