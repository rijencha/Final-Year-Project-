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
}
