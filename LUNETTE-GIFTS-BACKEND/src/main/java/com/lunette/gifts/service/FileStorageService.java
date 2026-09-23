package com.lunette.gifts.service;

import com.lunette.gifts.entity.CustomerPhoto;
import com.lunette.gifts.entity.Order;
import com.lunette.gifts.entity.OrderItem;
import com.lunette.gifts.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileStorageService {

    private final Path uploadLocation;
    private final OrderRepository orderRepository;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    public FileStorageService(@Value("${lunette.upload.dir:./data/uploads}") String uploadDir,
                              OrderRepository orderRepository) {
        this.uploadLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.orderRepository = orderRepository;
        try {
            Files.createDirectories(this.uploadLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create upload directory", ex);
        }
    }

    public Map<String, Object> storeFile(MultipartFile file, String category) throws IOException {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = getFileExtension(originalFilename).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Invalid file format. Allowed formats: JPG, JPEG, PNG, WEBP");
        }

        // Limit file size to 25MB
        if (file.getSize() > 25 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 25MB");
        }

        String subDir = category != null ? category : "general";
        Path targetDir = this.uploadLocation.resolve(subDir);
        Files.createDirectories(targetDir);

        String storedFilename = UUID.randomUUID() + "." + extension;
        Path targetPath = targetDir.resolve(storedFilename);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        String fileUrl = "/uploads/" + subDir + "/" + storedFilename;

        Map<String, Object> result = new HashMap<>();
        result.put("fileUrl", fileUrl);
        result.put("originalFilename", originalFilename);
        result.put("contentType", file.getContentType());
        result.put("fileSize", file.getSize());
        return result;
    }

    public byte[] createOrderZip(Long orderId) throws IOException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {

            // 1. Write instructions.txt
            StringBuilder sb = new StringBuilder();
            sb.append("====================================================\n");
            sb.append("LUNETTE GIFTS — ORDER FILE PACKAGE\n");
            sb.append("====================================================\n\n");
            sb.append("Order Number:    ").append(order.getOrderNumber()).append("\n");
            sb.append("Date:            ").append(order.getCreatedAt()).append("\n");
            sb.append("Customer Name:   ").append(order.getCustomerName()).append("\n");
            sb.append("Phone:           ").append(order.getCustomerPhone()).append("\n");
            sb.append("Email:           ").append(order.getCustomerEmail() != null ? order.getCustomerEmail() : "N/A").append("\n");
            sb.append("Shipping Address:\n").append(order.getShippingAddress()).append("\n");
            sb.append("PIN Code:        ").append(order.getPinCode()).append(" (").append(order.getDeliveryZone()).append(")\n");
            sb.append("Payment Status:  ").append(order.getPaymentStatus()).append("\n");
            sb.append("Payment Ref:     ").append(order.getPaymentReference() != null ? order.getPaymentReference() : "N/A").append("\n");
            sb.append("Order Status:    ").append(order.getOrderStatus()).append("\n");
            if (order.getOrderNotes() != null && !order.getOrderNotes().isBlank()) {
                sb.append("Order Notes:     ").append(order.getOrderNotes()).append("\n");
            }
            sb.append("\n----------------------------------------------------\n");
            sb.append("ITEMS & CUSTOMIZATION DETAILS:\n");
            sb.append("----------------------------------------------------\n\n");

            int itemIdx = 1;
            int photoIdx = 1;
            int refIdx = 1;

            for (OrderItem item : order.getItems()) {
                sb.append(itemIdx).append(". Product: ").append(item.getProductName()).append("\n");
                if (item.getVariantName() != null) {
                    sb.append("   Variant: ").append(item.getVariantName());
                    if (item.getVariantDimensions() != null) sb.append(" (").append(item.getVariantDimensions()).append(")");
                    sb.append("\n");
                }
                sb.append("   Quantity: ").append(item.getQuantity()).append("\n");
                sb.append("   Customization: ").append(item.isCustomizationSelected() ? "YES (+₹" + item.getCustomizationCharge() + ")" : "NO").append("\n");
                if (item.isCustomizationSelected() && item.getCustomizationInstructions() != null) {
                    sb.append("   Customization Instructions: ").append(item.getCustomizationInstructions()).append("\n");
                }
                sb.append("   Required Photos: ").append(item.getRequiredPhotos()).append("\n");
                sb.append("   Uploaded Photos: ").append(item.getUploadedPhotos()).append("\n\n");

                // Process photos in zip
                for (CustomerPhoto photo : item.getPhotos()) {
                    String relativeUrl = photo.getFileUrl(); // e.g. /uploads/orders/uuid.jpg
                    Path localFilePath = resolveLocalPath(relativeUrl);

                    if (Files.exists(localFilePath)) {
                        String zipFolder;
                        String entryName;
                        if ("CUSTOMIZATION_REFERENCE".equalsIgnoreCase(photo.getFileType())) {
                            zipFolder = "customization_reference/";
                            String ext = getFileExtension(photo.getOriginalFilename());
                            entryName = zipFolder + "item" + itemIdx + "_ref_" + refIdx + (ext.isEmpty() ? ".jpg" : "." + ext);
                            refIdx++;
                        } else {
                            zipFolder = "customer_photos/";
                            String ext = getFileExtension(photo.getOriginalFilename());
                            entryName = zipFolder + "photo_" + String.format("%02d", photoIdx) + (ext.isEmpty() ? ".jpg" : "." + ext);
                            photoIdx++;
                        }

                        ZipEntry zipEntry = new ZipEntry(entryName);
                        zos.putNextEntry(zipEntry);
                        Files.copy(localFilePath, zos);
                        zos.closeEntry();
                    }
                }
                itemIdx++;
            }

            // Put instructions.txt
            ZipEntry textEntry = new ZipEntry("instructions.txt");
            zos.putNextEntry(textEntry);
            zos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        return baos.toByteArray();
    }

    private Path resolveLocalPath(String fileUrl) {
        if (fileUrl == null) return Paths.get("");
        String cleaned = fileUrl.replace("/uploads/", "");
        return this.uploadLocation.resolve(cleaned);
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1) : "";
    }
}
