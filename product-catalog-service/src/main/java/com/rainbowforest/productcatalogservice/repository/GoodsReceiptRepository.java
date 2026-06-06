package com.rainbowforest.productcatalogservice.repository;

import com.rainbowforest.productcatalogservice.entity.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {

    Optional<GoodsReceipt> findByReceiptCode(String receiptCode);

    List<GoodsReceipt> findByReceiptDateBetween(LocalDate from, LocalDate to);

    List<GoodsReceipt> findBySupplier_Id(Long supplierId);

    List<GoodsReceipt> findByWarehouse_Id(Long warehouseId);

    List<GoodsReceipt> findByStatus(String status);
}
