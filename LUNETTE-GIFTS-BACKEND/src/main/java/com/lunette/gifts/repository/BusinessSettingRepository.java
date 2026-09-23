package com.lunette.gifts.repository;

import com.lunette.gifts.entity.BusinessSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessSettingRepository extends JpaRepository<BusinessSetting, String> {
}
