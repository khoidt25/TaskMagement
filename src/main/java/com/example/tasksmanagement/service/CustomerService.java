package com.example.tasksmanagement.service;

import com.example.tasksmanagement.entity.Customer;
import com.example.tasksmanagement.entity.Project;
import com.example.tasksmanagement.repository.CustomerRepository;

import com.example.tasksmanagement.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final TaskRepository taskRepository;

    public CustomerService(CustomerRepository customerRepository, TaskRepository taskRepository) {

        this.customerRepository = customerRepository;
        this.taskRepository = taskRepository;
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Page<Customer> searchCustomer(
            String keyword,
            String status,
            Pageable pageable) {

        return customerRepository.searchCustomer(
                keyword,
                status,
                pageable
        );
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

    public boolean delete(Long id) {

        // Kiểm tra ID của Customer, không phải ID của Customer.
        if (id == null || !customerRepository.existsById(id)) {
            return false;
        }

        if (taskRepository.existsByProjectId(id)) {
            throw new IllegalStateException(
                    "Không thể xóa customer task công việc đã có khách hàng.");
        }

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy Customer"));

        customer.setCustomerStatus("INACTIVE");
        customerRepository.save(customer);

        return true;

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
