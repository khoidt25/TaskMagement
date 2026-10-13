
package com.example.tasksmanagement.repository;

import com.example.tasksmanagement.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerCode(String customerCode);

    boolean existsByCustomerCode(String customerCode);

    boolean existsByCustomerCodeIgnoreCase(String customerCode);

    @Query("""
            SELECT c
            FROM Customer c
            WHERE
                (
                    :keyword IS NULL OR :keyword = ''
                    OR LOWER(c.customerCode)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(c.customerName)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
                AND (
                    :status IS NULL OR :status = ''
                    OR c.customerStatus = :status
                )
            """)
    Page<Customer> searchCustomer(
            @Param("keyword") String keyword,
            @Param("status") String status,
            Pageable pageable
    );
}
