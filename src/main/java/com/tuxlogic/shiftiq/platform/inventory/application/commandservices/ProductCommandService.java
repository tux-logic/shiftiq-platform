package com.tuxlogic.shiftiq.platform.inventory.application.commandservices;

import com.tuxlogic.shiftiq.platform.inventory.domain.model.aggregates.Product;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.AddBatchToProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.CreateProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.DeleteProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.UpdateProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.entities.ProductBatch;

import java.util.Optional;

public interface ProductCommandService {
    Optional<Product> handle(CreateProductCommand command);
    Optional<ProductBatch> handle(AddBatchToProductCommand command);
    Optional<Product> handle(UpdateProductCommand command);
    void handle(DeleteProductCommand command);
}
