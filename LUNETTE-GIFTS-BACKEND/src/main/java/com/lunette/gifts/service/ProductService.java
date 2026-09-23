package com.lunette.gifts.service;

import com.lunette.gifts.dto.AdminDto;
import com.lunette.gifts.entity.Product;
import com.lunette.gifts.entity.ProductVariant;
import com.lunette.gifts.repository.ProductRepository;
import com.lunette.gifts.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    public ProductService(ProductRepository productRepository, ProductVariantRepository variantRepository) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    public List<Product> getAllActiveProducts() {
        return productRepository.findByActiveTrueOrderByIdAsc();
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryAndActiveTrue(category);
    }

    public Product getProductBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with slug: " + slug));
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public Product updateProduct(Long id, AdminDto.ProductUpdateRequest req) {
        Product p = getProductById(id);
        if (req.getName() != null) p.setName(req.getName());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        if (req.getBasePrice() != null) p.setBasePrice(req.getBasePrice());
        if (req.getCustomizationPrice() != null) p.setCustomizationPrice(req.getCustomizationPrice());
        if (req.getMinQuantity() > 0) p.setMinQuantity(req.getMinQuantity());
        if (req.getPhotoRule() != null) p.setPhotoRule(req.getPhotoRule());
        if (req.getLightPrice() != null) p.setLightPrice(req.getLightPrice());
        if (req.getRingPrice() != null) p.setRingPrice(req.getRingPrice());
        if (req.getStockQuantity() >= 0) p.setStockQuantity(req.getStockQuantity());
        if (req.getLowStockThreshold() >= 0) p.setLowStockThreshold(req.getLowStockThreshold());
        return productRepository.save(p);
    }

    /**
     * Updates frame variant pricing and configured photo count in the database.
     * This fulfills the requirement: "Admin must be able to configure the required number of customer photos for each frame."
     */
    public ProductVariant updateVariant(Long variantId, AdminDto.VariantUpdateRequest req) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found with id: " + variantId));

        if (req.getName() != null) variant.setName(req.getName());
        if (req.getDimensions() != null) variant.setDimensions(req.getDimensions());
        if (req.getPrice() != null) variant.setPrice(req.getPrice());
        if (req.getRequiredPhotos() > 0) {
            variant.setRequiredPhotos(req.getRequiredPhotos());
        }
        return variantRepository.save(variant);
    }
}
