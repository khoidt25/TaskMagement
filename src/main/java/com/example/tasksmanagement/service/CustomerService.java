package com.example.tasksmanagement.service;

import com.example.tasksmanagement.entity.Customer;
import com.example.tasksmanagement.repository.CustomerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng ID: " + id));
    }

    public Customer create(Customer customer) {
        if (customer.getCustomerId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không được truyền customerId khi tạo mới");
        }

        validateCustomer(customer);

        if (customerRepository.existsByCustomerCode(customer.getCustomerCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã khách hàng đã tồn tại");
        }

        if (customer.getCustomerStatus() == null) {
            customer.setCustomerStatus("ACTIVE");
        }

        validateStatus(customer.getCustomerStatus());

        return customerRepository.save(customer);
    }

    public Customer update(Long id, Customer request) {
        Customer customer = findById(id);

        validateCustomer(request);

        customerRepository.findByCustomerCode(request.getCustomerCode()).ifPresent(existing -> {
            if (!existing.getCustomerId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã khách hàng đã tồn tại");
            }
        });

        if (request.getCustomerStatus() != null) {
            validateStatus(request.getCustomerStatus());
            customer.setCustomerStatus(request.getCustomerStatus());
        }

        customer.setCustomerCode(request.getCustomerCode());
        customer.setCustomerName(request.getCustomerName());
        customer.setDescription(request.getDescription());

        return customerRepository.save(customer);
    }

    public void delete(Long id) {
        Customer customer = findById(id);
        customerRepository.delete(customer);
    }

    private void validateCustomer(Customer customer) {
        if (customer.getCustomerCode() == null || customer.getCustomerCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã khách hàng không được để trống");
        }

        if (customer.getCustomerName() == null || customer.getCustomerName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên khách hàng không được để trống");
        }
    }

    private void validateStatus(String status) {
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái chỉ được là ACTIVE hoặc INACTIVE");
        }
    }
}
