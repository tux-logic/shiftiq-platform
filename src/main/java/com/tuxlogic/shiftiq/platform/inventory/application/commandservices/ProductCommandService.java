package com.tuxlogic.shiftiq.platform.inventory.application.commandservices;

import com.tuxlogic.shiftiq.platform.inventory.domain.model.aggregates.Product;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.AddBatchToProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.CreateProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.DeleteProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.UpdateProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.entities.ProductBatch;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.ProductCommandFailure;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;

public interface ProductCommandService {
    Result<Product, ProductCommandFailure> handle(CreateProductCommand command);
    Result<ProductBatch, ProductCommandFailure> handle(AddBatchToProductCommand command);
    Result<Product, ProductCommandFailure> handle(UpdateProductCommand command);
    Result<Void, ProductCommandFailure> handle(DeleteProductCommand command);
}
