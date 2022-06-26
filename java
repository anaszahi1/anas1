  <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
 90  
ebanking-backend/src/main/java/com/example/ebankingbackend/EbankingBackendApplication.java
@@ -1,13 +1,103 @@
package com.example.ebankingbackend;

import com.example.ebankingbackend.entities.*;
import com.example.ebankingbackend.enums.AccountStatus;
import com.example.ebankingbackend.enums.OperationType;
import com.example.ebankingbackend.exceptions.BalanceNotSufficientException;
import com.example.ebankingbackend.exceptions.BankAccountNotFoundException;
import com.example.ebankingbackend.exceptions.CustomerNotFoundException;
import com.example.ebankingbackend.repositories.AccountOperationsRepository;
import com.example.ebankingbackend.repositories.BankAccountRepository;
import com.example.ebankingbackend.repositories.CustomerRepository;
import com.example.ebankingbackend.services.BankAccountServices;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
public class EbankingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EbankingBackendApplication.class, args);
    }
    @Bean
    CommandLineRunner commandLineRunner(BankAccountServices bankAccountServices){
        return args->{
           Stream.of("Slimani","Elbennani","ElMejati").forEach(name->{
               Customer customer=new Customer();
               customer.setName(name);
               customer.setEmail(name+"@mail.com");
               bankAccountServices.saveCustomer(customer);
           });
           bankAccountServices.lisCustomers().forEach(customer -> {
               try {
                   bankAccountServices.saveCurrentBankAccount(Math.random()*90000,90000,customer.getId());
                   bankAccountServices.saveSavingBankAccount(Math.random()*622542, Math.random()*6,customer.getId());
                     List<BankAccount> bankAccountList=bankAccountServices.bankAccountList();
                     for(BankAccount bankAccount:bankAccountList){
                        for(int i=0;i<10;i++){
                             bankAccountServices.credit(bankAccount.getID(), 10000 + Math.random() * 12000, "credit");
                             bankAccountServices.debit(bankAccount.getID(),1000+Math.random()*9000,"description");
                         }
                     }

               } catch (CustomerNotFoundException | BankAccountNotFoundException | BalanceNotSufficientException e) {
                   e.printStackTrace();
               }
           });
        };
    }
//@Bean
    CommandLineRunner start(CustomerRepository customerRepository,
                            BankAccountRepository bankAccountRepository,
                            AccountOperationsRepository accountOperationsRepository){
        return args -> {
          Stream.of("Slimani","BENMAATI","OUAZZANI").forEach(name->{
              Customer customer=new Customer();
              customer.setName(name);
              customer.setEmail(name+"@mail.com");

              customerRepository.save(customer);
          });
          customerRepository.findAll().forEach(customer -> {

              CurrentAccount currentAccount=new CurrentAccount();
              currentAccount.setID(UUID.randomUUID().toString());
              currentAccount.setBalance(Math.random()*980000);
              currentAccount.setCreationDate(new Date());
              currentAccount.setStatus(AccountStatus.CREATED);
              currentAccount.setCustomer(customer);
              currentAccount.setOverDraft(9000);
              bankAccountRepository.save(currentAccount);

              SavingAccount savingAccount=new SavingAccount();
              savingAccount.setID(UUID.randomUUID().toString());
              savingAccount.setBalance(Math.random()*980000);
              savingAccount.setCreationDate(new Date());
              savingAccount.setStatus(AccountStatus.CREATED);
              savingAccount.setCustomer(customer);
              savingAccount.setInterestRate(4.5);
              bankAccountRepository.save(savingAccount);
          });
          bankAccountRepository.findAll().forEach(bankAccount -> {
              for(int i=0;i<5;i++){
                  AccountOperations accountOperations=new AccountOperations();
                  accountOperations.setOperationDate(new Date());
                  accountOperations.setAmount(Math.random()*40000);
                  accountOperations.setOperationType(Math.random()>0.5? OperationType.DEBIT:OperationType.CREDIT);
                  accountOperations.setBankAccount(bankAccount);
                  accountOperationsRepository.save(accountOperations);

              }
          });
        };

    }

}
 15  
ebanking-backend/src/main/java/com/example/ebankingbackend/dtos/CustomerDTO.java
@@ -0,0 +1,15 @@
package com.example.ebankingbackend.dtos;

import lombok.Data;


@Data

public class CustomerDTO {

    private Long id;
    private String name;
    //private String email;


}
 12  
...main/java/entities/AccountOperations.java → ...ngbackend/entities/AccountOperations.java
@@ -1,23 +1,23 @@
package entities;
package com.example.ebankingbackend.entities;

import enums.OperationType;
import com.example.ebankingbackend.enums.OperationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.*;
import java.util.Date;
@Entity
@Data@NoArgsConstructor@AllArgsConstructor
public class AccountOperations {
    @Id
    @Id @GeneratedValue
    private Long id;
    private Date operationDate;
    private double amount;
    @Enumerated(EnumType.STRING)
    private OperationType operationType;
    @ManyToOne
    private BankAccount bankAccount;
    private String description;

}
  12  
...d/src/main/java/entities/BankAccount.java → ...ebankingbackend/entities/BankAccount.java
@@ -1,17 +1,16 @@
package entities;
package com.example.ebankingbackend.entities;

import enums.AccountStatus;
import com.example.ebankingbackend.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.*;
import java.util.Date;
import java.util.List;
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type",length = 4)
@Data
@AllArgsConstructor
@NoArgsConstructor
@@ -20,6 +19,7 @@ public class BankAccount {
    private String ID;
    private double balance;
    private Date creationDate;
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    @ManyToOne
    private Customer customer;
 7  
...rc/main/java/entities/CurrentAccount.java → ...nkingbackend/entities/CurrentAccount.java
@@ -1,10 +1,15 @@
package entities;
package com.example.ebankingbackend.entities;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("CA")
@Data@NoArgsConstructor@AllArgsConstructor
public class CurrentAccount extends BankAccount{
    private double overDraft;
  4  
...kend/src/main/java/entities/Customer.java → ...le/ebankingbackend/entities/Customer.java
@@ -1,5 +1,6 @@
package entities;
package com.example.ebankingbackend.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@@ -15,5 +16,6 @@ public class Customer {
    private String name;
    private String email;
    @OneToMany(mappedBy = "customer")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<BankAccount> bankAccountList;
}
 7  
...src/main/java/entities/SavingAccount.java → ...ankingbackend/entities/SavingAccount.java
@@ -1,11 +1,16 @@
package entities;
package com.example.ebankingbackend.entities;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@Data@NoArgsConstructor@AllArgsConstructor
@DiscriminatorValue("SA")
public class SavingAccount  extends BankAccount{
    private double interestRate;
}
 2  
...nd/src/main/java/enums/AccountStatus.java → .../ebankingbackend/enums/AccountStatus.java
@@ -1,4 +1,4 @@
package enums;
package com.example.ebankingbackend.enums;

public enum AccountStatus {
    CREATED,SUSPENDED,ACTIVATED
 2  
...nd/src/main/java/enums/OperationType.java → .../ebankingbackend/enums/OperationType.java
@@ -1,4 +1,4 @@
package enums;
package com.example.ebankingbackend.enums;

public enum OperationType {
    DEBIT,CREDIT
 7  
...d/src/main/java/com/example/ebankingbackend/exceptions/BalanceNotSufficientException.java
@@ -0,0 +1,7 @@
package com.example.ebankingbackend.exceptions;

public class BalanceNotSufficientException extends Exception {
    public BalanceNotSufficientException(String balance_not_sufficient) {
        super(balance_not_sufficient);
    }
}
 7  
...nd/src/main/java/com/example/ebankingbackend/exceptions/BankAccountNotFoundException.java
@@ -0,0 +1,7 @@
package com.example.ebankingbackend.exceptions;

public class BankAccountNotFoundException extends Exception {
    public BankAccountNotFoundException(String s) {
        super(s);
    }
}
 7  
...ckend/src/main/java/com/example/ebankingbackend/exceptions/CustomerNotFoundException.java
@@ -0,0 +1,7 @@
package com.example.ebankingbackend.exceptions;

public class CustomerNotFoundException extends Exception {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
 23  
...king-backend/src/main/java/com/example/ebankingbackend/mappers/BankAccountMapperImpl.java
@@ -0,0 +1,23 @@
package com.example.ebankingbackend.mappers;

import com.example.ebankingbackend.dtos.CustomerDTO;
import com.example.ebankingbackend.entities.Customer;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service

public class BankAccountMapperImpl {
    public CustomerDTO fromCustomer(Customer customer){

        CustomerDTO customerDTO=new CustomerDTO();
        BeanUtils.copyProperties(customer,customerDTO);
        return customerDTO;
    }

    public Customer fromCustomerDTO(CustomerDTO customerDTO){
        Customer customer=new Customer();
        BeanUtils.copyProperties(customerDTO,customer );
        return customer;
    }
}
 8  
...d/src/main/java/com/example/ebankingbackend/repositories/AccountOperationsRepository.java
@@ -0,0 +1,8 @@
package com.example.ebankingbackend.repositories;

import com.example.ebankingbackend.entities.AccountOperations;
import com.example.ebankingbackend.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountOperationsRepository extends JpaRepository<AccountOperations,Long> {
}
 7  
...backend/src/main/java/com/example/ebankingbackend/repositories/BankAccountRepository.java
@@ -0,0 +1,7 @@
package com.example.ebankingbackend.repositories;

import com.example.ebankingbackend.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount,String> {
}
 7  
...ng-backend/src/main/java/com/example/ebankingbackend/repositories/CustomerRepository.java
@@ -0,0 +1,7 @@
package com.example.ebankingbackend.repositories;

import com.example.ebankingbackend.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer,Long> {
}
 26  
ebanking-backend/src/main/java/com/example/ebankingbackend/services/BankAccountServices.java
@@ -0,0 +1,26 @@
package com.example.ebankingbackend.services;

import com.example.ebankingbackend.dtos.CustomerDTO;
import com.example.ebankingbackend.entities.BankAccount;
import com.example.ebankingbackend.entities.CurrentAccount;
import com.example.ebankingbackend.entities.Customer;
import com.example.ebankingbackend.entities.SavingAccount;
import com.example.ebankingbackend.exceptions.BalanceNotSufficientException;
import com.example.ebankingbackend.exceptions.BankAccountNotFoundException;
import com.example.ebankingbackend.exceptions.CustomerNotFoundException;

import java.util.List;

public interface BankAccountServices {
    Customer saveCustomer(Customer customer);
    CurrentAccount saveCurrentBankAccount(double initialBalance, double overDraft, Long customerID) throws CustomerNotFoundException;
    SavingAccount saveSavingBankAccount(double initialBalance, double interestRate, Long customerID) throws CustomerNotFoundException;

    List<CustomerDTO> lisCustomers();
    BankAccount getBankAccount(String accountID) throws BankAccountNotFoundException;
    void debit(String accountID,double amount,String description) throws BankAccountNotFoundException, BalanceNotSufficientException;
    void credit(String accountID,double amount,String description) throws BankAccountNotFoundException;
    void transfer(String accountIDSource,String accountIDDestination,double amount) throws BankAccountNotFoundException, BalanceNotSufficientException;

    List<BankAccount> bankAccountList();
}
 140  
...g-backend/src/main/java/com/example/ebankingbackend/services/BankAccountServicesImpl.java
@@ -0,0 +1,140 @@
package com.example.ebankingbackend.services;

import com.example.ebankingbackend.dtos.CustomerDTO;
import com.example.ebankingbackend.entities.*;
import com.example.ebankingbackend.enums.OperationType;
import com.example.ebankingbackend.exceptions.BalanceNotSufficientException;
import com.example.ebankingbackend.exceptions.BankAccountNotFoundException;
import com.example.ebankingbackend.exceptions.CustomerNotFoundException;
import com.example.ebankingbackend.mappers.BankAccountMapperImpl;
import com.example.ebankingbackend.repositories.AccountOperationsRepository;
import com.example.ebankingbackend.repositories.BankAccountRepository;
import com.example.ebankingbackend.repositories.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
@Slf4j


public class BankAccountServicesImpl implements BankAccountServices {


        private CustomerRepository customerRepository;
        private BankAccountRepository bankAccountRepository;
        private AccountOperationsRepository accountOperationsRepository;
        private BankAccountMapperImpl dtoMapper;


    @Override
    public Customer saveCustomer(Customer customer) {
        log.info("Saving new Customer");
        Customer SavedCustomer= customerRepository.save(customer);
        return SavedCustomer;
    }



    @Override
    public SavingAccount saveSavingBankAccount(double initialBalance, double interestRate, Long customerID) throws CustomerNotFoundException {
        SavingAccount bankAccount = new SavingAccount();

        Customer customer= customerRepository.findById(customerID).orElse(null);
        if(customer==null){
            throw new CustomerNotFoundException("Customer not Found");
        }
        bankAccount.setID(UUID.randomUUID().toString());
        bankAccount.setCreationDate(new Date());
        bankAccount.setBalance(initialBalance);
        bankAccount.setInterestRate(interestRate);
        bankAccount.setCustomer(customer);
        SavingAccount savedBankAccount=bankAccountRepository.save(bankAccount);
        return savedBankAccount;


    }

    @Override
    public CurrentAccount saveCurrentBankAccount(double initialBalance, double overDraft, Long customerID) throws CustomerNotFoundException
    {
        CurrentAccount bankAccount = new CurrentAccount();

        Customer customer= customerRepository.findById(customerID).orElse(null);
        if(customer==null){
            throw new CustomerNotFoundException("Customer not Found");
        }
        bankAccount.setID(UUID.randomUUID().toString());
        bankAccount.setCreationDate(new Date());
        bankAccount.setBalance(initialBalance);
        bankAccount.setOverDraft(overDraft);
        bankAccount.setCustomer(customer);
CurrentAccount savedBankAccount=bankAccountRepository.save(bankAccount);
        return savedBankAccount;
    }

    @Override
    public List<CustomerDTO> lisCustomers() {
       List<Customer> customers=customerRepository.findAll();
    List<CustomerDTO> collect= customers.stream().map(customer -> dtoMapper.fromCustomer(customer)).collect(Collectors.toList());
     return collect;
    }

    @Override
    public BankAccount getBankAccount(String accountID) throws BankAccountNotFoundException {
        BankAccount bankAccount=bankAccountRepository.findById(accountID).orElseThrow(()->new BankAccountNotFoundException("BankAccount not Found!"));

    return bankAccount;
    }

    @Override
    public void debit(String accountID, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
BankAccount bankAccount =getBankAccount(accountID);
if(bankAccount.getBalance()<amount)
    throw new BalanceNotSufficientException("Balance_not_Sufficient");
        AccountOperations accountOperations=new AccountOperations();
        accountOperations.setOperationType(OperationType.DEBIT);
        accountOperations.setOperationDate(new Date());
        accountOperations.setAmount(amount);
        accountOperations.setDescription(description);
        accountOperations.setBankAccount(bankAccount);
        accountOperationsRepository.save(accountOperations);
        bankAccount.setBalance(bankAccount.getBalance()-amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void credit(String accountID, double amount, String description) throws BankAccountNotFoundException {
        BankAccount bankAccount =getBankAccount(accountID);
        AccountOperations accountOperations=new AccountOperations();
        accountOperations.setOperationType(OperationType.DEBIT);
        accountOperations.setOperationDate(new Date());
        accountOperations.setAmount(amount);
        accountOperations.setDescription(description);
        accountOperations.setBankAccount(bankAccount);
        accountOperationsRepository.save(accountOperations);
        bankAccount.setBalance(bankAccount.getBalance()+amount);
        bankAccountRepository.save(bankAccount);

    }

    @Override
    public void transfer(String accountIDSource, String accountIDDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException {
        debit(accountIDSource,amount,"transfer to "+accountIDDestination);
        credit(accountIDDestination,amount,"transfer from "+accountIDSource);


    }
    @Override
    public List<BankAccount> bankAccountList(){
        return bankAccountRepository.findAll();
    }
}
 12  
ebanking-backend/src/main/java/com/example/ebankingbackend/services/BankService.java
@@ -0,0 +1,12 @@
package com.example.ebankingbackend.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BankService {
    public void consulter(){

    }
}
 24  
ebanking-backend/src/main/java/com/example/ebankingbackend/web/CustomerRestController.java
@@ -0,0 +1,24 @@
package com.example.ebankingbackend.web;


import com.example.ebankingbackend.dtos.CustomerDTO;
import com.example.ebankingbackend.entities.Customer;
import com.example.ebankingbackend.services.BankAccountServices;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j

public class CustomerRestController {
    private BankAccountServices bankAccountServices;
@GetMapping("/customers")
    public List<CustomerDTO> customerList(){
        return bankAccountServices.lisCustomers();
    }
}
 9  
ebanking-backend/src/main/resources/application.properties
@@ -1 +1,10 @@

#spring.datasource.url=jdbc:h2:mem:bank
#spring.h2.console.enabled=true
server.port=8085
spring.datasource.url=jdbc:mysql:// localhost:3306/E--BANK?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDBDialect
spring.jpa.show-sql=true 
