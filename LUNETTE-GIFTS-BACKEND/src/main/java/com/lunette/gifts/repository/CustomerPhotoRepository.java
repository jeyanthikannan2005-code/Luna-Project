package com.lunette.gifts.repository;

import com.lunette.gifts.entity.CustomerPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerPhotoRepository extends JpaRepository<CustomerPhoto, Long> {
    List<CustomerPhoto> findByOrderItemId(Long orderItemId);
    List<CustomerPhoto> findByOrderItemIdAndFileType(Long orderItemId, String fileType);
}
