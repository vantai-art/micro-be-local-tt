package com.rainbowforest.accountingservice.controller;

import com.rainbowforest.accountingservice.domain.Account;
import com.rainbowforest.accountingservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounting/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;

    @GetMapping
    public ResponseEntity<List<Account>> getAll() {
        return ResponseEntity.ok(accountRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getById(@PathVariable Long id) {
        return accountRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Account>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(accountRepository.findByType(type));
    }

    @PostMapping
    public ResponseEntity<Account> create(@RequestBody Account account) {
        if (account.getCode() == null || account.getName() == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.status(HttpStatus.CREATED).body(accountRepository.save(account));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> update(@PathVariable Long id, @RequestBody Account data) {
        return accountRepository.findById(id).map(a -> {
            if (data.getName() != null) a.setName(data.getName());
            if (data.getType() != null) a.setType(data.getType());
            if (data.getBalance() != null) a.setBalance(data.getBalance());
            if (data.getDescription() != null) a.setDescription(data.getDescription());
            return ResponseEntity.ok(accountRepository.save(a));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!accountRepository.existsById(id)) return ResponseEntity.notFound().build();
        accountRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
