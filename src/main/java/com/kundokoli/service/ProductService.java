package com.kundokoli.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.kundokoli.model.Product;
import com.kundokoli.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Value("${CLOUDINARY_CLOUD_NAME}")
    private String cloudName;

    @Value("${CLOUDINARY_API_KEY}")
    private String apiKey;

    @Value("${CLOUDINARY_API_SECRET}")
    private String apiSecret;

    private Cloudinary getCloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category.toUpperCase());
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    public Product createProduct(Product product, MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            String imageUrl = uploadToCloudinary(image);
            product.setImageUrl(imageUrl);
        }
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product updated, MultipartFile image) throws IOException {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setCategory(updated.getCategory());
        existing.setStockQuantity(updated.getStockQuantity());

        if (image != null && !image.isEmpty()) {
            // Delete old image from Cloudinary
            if (existing.getImageUrl() != null) {
                deleteFromCloudinary(existing.getImageUrl());
            }
            existing.setImageUrl(uploadToCloudinary(image));
        }
        return productRepository.save(existing);
    }

    public void deleteProduct(Long id) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        if (product.getImageUrl() != null) {
            deleteFromCloudinary(product.getImageUrl());
        }
        productRepository.deleteById(id);
    }

    private String uploadToCloudinary(MultipartFile file) throws IOException {
        Map uploadResult = getCloudinary().uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("folder", "kundokoli")
        );
        return (String) uploadResult.get("secure_url");
    }

    private void deleteFromCloudinary(String imageUrl) {
        try {
            String publicId = extractPublicId(imageUrl);
            getCloudinary().uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            System.out.println("Could not delete image: " + e.getMessage());
        }
    }

    private String extractPublicId(String imageUrl) {
        // Extract public_id from Cloudinary URL
        String[] parts = imageUrl.split("/");
        String filename = parts[parts.length - 1];
        return "kundokoli/" + filename.split("\\.")[0];
    }
}
