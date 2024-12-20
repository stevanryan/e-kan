package com.example.backend.controllers;

import com.example.backend.dtos.ApiResp;
import com.example.backend.dtos.DtoMapper;
import com.example.backend.dtos.InvoiceDtos.InvoiceDto;
import com.example.backend.models.AlamatPembeliModel;
import com.example.backend.models.InvoiceModel;
import com.example.backend.models.PembeliModel;
import com.example.backend.services.AlamatPembeliService;
import com.example.backend.services.InvoiceService;
import com.example.backend.services.PembeliService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/transactions")
@AllArgsConstructor
public class InvoiceController {


    private final InvoiceService invoiceService;
    private final PembeliService pembeliService ;
    private final AlamatPembeliService alamatService ;
    private final DtoMapper mapper ;

    @PostMapping("/cart")
    public ResponseEntity<ApiResp<InvoiceDto>> checkoutFromCart(
            @RequestParam UUID alamatId
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication.getPrincipal() instanceof PembeliModel currentUser) {
                AlamatPembeliModel alamat = alamatService.getById(alamatId);

                if (!alamat.getPembeli().getIdPembeli().equals(currentUser.getIdPembeli())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                            new ApiResp<>(
                                    HttpStatus.FORBIDDEN.value(),
                                    "This user does not own this alamat" ,
                                    null
                            )
                    );
                }
                InvoiceModel nota = invoiceService.createTransactionFromCart(currentUser , alamat);

                return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResp<>(
                        HttpStatus.CREATED.value(),
                        "Created Invoice" ,
                        mapper.toInvoiceDto(nota)
                ));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/direct")
    public ResponseEntity<ApiResp<InvoiceDto>> directPurchase(
            @RequestParam UUID itemId,
            @RequestParam UUID alamatId,
            @RequestParam int quantity
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication.getPrincipal() instanceof PembeliModel currentUser) {
                AlamatPembeliModel alamat = alamatService.getById(alamatId);

                if (!alamat.getPembeli().getIdPembeli().equals(currentUser.getIdPembeli())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                            new ApiResp<>(
                                    HttpStatus.FORBIDDEN.value(),
                                    "This user does not own this alamat" ,
                                   null
                            )
                    );
                }

                InvoiceModel nota = invoiceService.createTransactionDirect(currentUser, itemId, quantity , alamat);
                return ResponseEntity.status(HttpStatus.CREATED).body(
                        new ApiResp<>(
                                HttpStatus.CREATED.value(),
                                "Created Invoice" ,
                                mapper.toInvoiceDto(nota)
                        )
                );
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<ApiResp<InvoiceDto>> getTransaction(@PathVariable UUID invoiceId) {
        try {
            InvoiceModel nota = invoiceService.getTransactionById(invoiceId);
            return ResponseEntity.ok(new ApiResp<>(
                    HttpStatus.OK.value(),
                    "Success retrieve invoice" ,
                    mapper.toInvoiceDto(nota))
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping
    public ResponseEntity<ApiResp<List<InvoiceDto>>> getAllInvoices () {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getPrincipal() instanceof PembeliModel currentUser) {
            List<InvoiceModel> invoiceList = invoiceService.getAllInvoice(currentUser);

            if (invoiceList.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(
                    new ApiResp<>(
                            HttpStatus.OK.value() ,
                            "Success retrieve all invoices" ,
                            invoiceList.stream().map(mapper :: toInvoiceDto).collect(Collectors.toList())
                    )
            );
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @DeleteMapping("/{invoiceId}")
    public ResponseEntity<ApiResp<Object>> destroyInvoice(@PathVariable UUID invoiceId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getPrincipal() instanceof PembeliModel currentUser) {
           InvoiceModel invoice = invoiceService.getTransactionById(invoiceId);

            // Check if the invoice belongs to the current user
            if (!invoice.getPembeli().getIdPembeli().equals(currentUser.getIdPembeli())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResp<>(HttpStatus.FORBIDDEN.value(),
                                "only owner can delete the invoice", null));
            }

            invoiceService.deleteInvoice(invoiceId);
            return ResponseEntity.ok(new ApiResp<>(HttpStatus.OK.value(), "Invoice deleted successfully", null));
        }
        // Unauthorized access
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResp<>(HttpStatus.UNAUTHORIZED.value(), "User not authenticated", null));
    }



}
