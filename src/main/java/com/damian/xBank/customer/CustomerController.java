package com.damian.xBank.customer;

import com.damian.xBank.banking.account.BankingAccountService;
import com.damian.xBank.common.utils.AuthUtils;
import com.damian.xBank.customer.dto.CustomerDTO;
import com.damian.xBank.customer.dto.CustomerDTOMapper;
import com.damian.xBank.customer.dto.CustomerWithProfileDTO;
import com.damian.xBank.customer.http.request.CustomerEmailUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1")
@RestController
public class CustomerController {
    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService, BankingAccountService bankingAccountService) {
        this.customerService = customerService;
    }

    // endpoint to receive logged customer
    @GetMapping("/customers/me")
    public ResponseEntity<?> getLoggedCustomerData() {
        // TODO check if this works. If not, try to change to customerService.getCustomer(id)
        Customer customer = AuthUtils.getLoggedCustomer();
        CustomerWithProfileDTO dto = CustomerDTOMapper.toCustomerWithProfileDTO(customer);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(dto);
    }

    // TODO should really return CustomerDTO?
    // endpoint to modify logged customer email
    @PatchMapping("/customers/me/email")
    public ResponseEntity<?> updateLoggedCustomerEmail(
            @Validated @RequestBody
            CustomerEmailUpdateRequest request
    ) {
        Customer customer = customerService.updateEmail(request);
        CustomerDTO customerDTO = CustomerDTOMapper.toCustomerDTO(customer);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customerDTO);
    }
}

