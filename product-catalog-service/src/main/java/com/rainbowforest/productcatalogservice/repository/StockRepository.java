package com.rainbowforest.productcatalogservice.repository;

import com.rainbowforest.productcatalogservice.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByProduct_IdAndWarehouse_Id(Long productId, Long warehouseId);

    List<Stock> findByProduct_Id(Long productId);

    List<Stock> findByWarehouse_Id(Long warehouseId);

    /** Lấy tất cả mặt hàng tồn kho thấp hơn ngưỡng */
    @Query("SELECT s FROM Stock s WHERE s.quantity <= s.minThreshold")
    List<Stock> findLowStockItems();
}
