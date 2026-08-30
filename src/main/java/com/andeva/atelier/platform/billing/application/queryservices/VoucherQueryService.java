package com.andeva.atelier.platform.billing.application.queryservices;

import com.andeva.atelier.platform.billing.domain.model.aggregates.Voucher;
import com.andeva.atelier.platform.billing.domain.model.queries.GetVoucherByIdQuery;

import com.andeva.atelier.platform.billing.domain.model.queries.GetVouchersByBranchIdQuery;

import java.util.Optional;
import java.util.List;

/**
 * Service interface for querying Vouchers.
 */
public interface VoucherQueryService {
    /**
     * Retrieves a Voucher by its unique identifier.
     *
     * @param query the query containing the voucher ID
     * @return an Optional containing the Voucher if found, or empty otherwise
     */
    Optional<Voucher> handle(GetVoucherByIdQuery query);

    /**
     * Retrieves all Vouchers emitted in a specific branch.
     *
     * @param query the query containing the branch ID
     * @return a List of Vouchers
     */
    List<Voucher> handle(GetVouchersByBranchIdQuery query);
}
