package br.com.firsti.packages.purchase.modules.purchaseRequest.actions;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import br.com.firsti.languages.LRCore;
import br.com.firsti.languages.LRProduct;
import br.com.firsti.languages.LRPurchase;
import br.com.firsti.module.actions.AbstractActionEdit;
import br.com.firsti.module.exceptions.AccessDeniedException;
import br.com.firsti.module.exceptions.ActionErrorException;
import br.com.firsti.module.exceptions.InternalServerErrorException;
import br.com.firsti.module.exceptions.PermissionDeniedException;
import br.com.firsti.module.exceptions.ResourceNotFoundException;
import br.com.firsti.module.requests.ActionRequest;
import br.com.firsti.module.structures.ActionValidation.ActionValidationBuilder;
import br.com.firsti.module.structures.ActivityData.ActivityBuilder;
import br.com.firsti.packages.organization.entities.Company;
import br.com.firsti.packages.product.entities.*;
import br.com.firsti.packages.purchase.entities.PurchaseRequest;
import br.com.firsti.packages.purchase.entities.PurchaseRequest.PurchaseRequestStatus;
import br.com.firsti.packages.purchase.modules.purchaseRequest.ModulePurchaseRequest;
import br.com.firsti.packages.stock.entities.Warehouse;
import br.com.firsti.persistence.EntityManagerWrapper;
import br.com.firsti.services.websocket.messages.output.elements.items.*;
import br.com.firsti.system.LanguageResource;

public class PurchaseRequestEdit extends AbstractActionEdit<ModulePurchaseRequest> {

    public PurchaseRequestEdit() {
        super(new Builder<ModulePurchaseRequest>(Access.COMPANY_PRIVATE));
    }
   
    @Override
    public void onWindowRequest(EntityManagerWrapper entityManager, ActionRequest request, WindowBuilder windowBuilder)
            throws AccessDeniedException, PermissionDeniedException, InternalServerErrorException, ResourceNotFoundException {

        PurchaseRequest entity = entityManager.find(PurchaseRequest.class, request.getEntityId());
        if (entity == null) throw new ResourceNotFoundException();

        if (!PurchaseRequestStatus.PENDING.equals(entity.getStatus()) ||
            (!Objects.equals(entity.getRequester(), request.getUserProfile().getCollaborator())
            		&& !request.getUserProfile().isAdministrator())) {
            throw new PermissionDeniedException();
        }

     // Preloads
        entityManager.createNamedQuery("Company.findAllWithSocialName", Company.class)
            .getResultStream()
            .forEach(company -> windowBuilder.getPreloadBuilder().addTo("company", company.getId(), company.getName()));

        entityManager.createQuery("FROM Warehouse w WHERE w.company = ?1 ORDER BY w.name", Warehouse.class)
            .setParameter(1, entity.getWarehouse().getCompany())
            .getResultStream()
            .forEach(w -> windowBuilder.getPreloadBuilder().addTo("warehouse", w.getId(), w.getName()));

        entityManager.createQuery("FROM ProductType pt ORDER BY pt.name", ProductType.class)
            .getResultStream()
            .forEach(pt -> windowBuilder.getPreloadBuilder().addTo("productType", pt.getId(), pt.getName()));

        entityManager.createQuery("FROM Product p ORDER BY p.model", Product.class)
            .getResultStream()
            .forEach(p -> windowBuilder.getPreloadBuilder().addTo("product", p.getId(), p.getModel()));
        
            entityManager.createQuery("FROM ProductCategory c ORDER BY c.name", ProductCategory.class)
            .getResultStream()
            .forEach(category -> {
                windowBuilder.getPreloadBuilder().addTo("category", category.getId(), category.getName());
            });
        
     
        request.set("entity", entity); // só setamos aqui para reutilizar no save

        windowBuilder.getDataBuilder()
            .addEntityId("company", entity.getWarehouse().getCompany().getId())
            .addEntityId("warehouse", entity.getWarehouse().getId())
            .add("status", entity.getStatus())
            .add("requester", entity.getRequester().getName())
            .addEntityId("category", entity.getProductType().getCategory().getId())
            .addEntityId("productType", entity.getProductType().getId())
            .addEntityId("product", entity.getProduct() != null ? entity.getProduct().getId() : null)
            .add("quantity", entity.getQuantity())
            .add("description", entity.getDescription());

        windowBuilder.getHeaderBuilder()
            .add(new Select("company").setLabel(LRCore.COMPANY).addClass("col-4"))
            .add(new Select("warehouse").setLabel(LRCore.WAREHOUSE).addClass("col-6"))
            .add(new InputView("status").setLabel(LRCore.STATUS).setTranslate(LRPurchase.class).addClass("col-2"));

        windowBuilder.getBodyBuilder()
            .add(new InputView("requester").setLabel(LRCore.REQUESTER).addClass("col-4"))
            .add(new InputView("creation").setLabel(LRCore.CREATION).addClass("col-6"))
            .add(new Select("category").setLabel(LRProduct.CATEGORY).addClass("col-2"))
            .add(new Select("productType").setLabel(LRProduct.PRODUCT_TYPE).addClass("col-4"))
            .add(new Select("product").setLabel(LRProduct.PRODUCT).addClass("col-4"))
            .add(new InputText("quantity").setLabel(LRCore.QUANTITY).addClass("col-2"))
            .add(new Textarea("description").setLabel(LRCore.DESCRIPTION).addClass("col-12").setMinHeight(100));
    }


    @Override
    public void onValidationRequest(EntityManagerWrapper entityManager, ActionRequest request,
                                    ActionValidationBuilder validationBuilder)
            throws AccessDeniedException, ResourceNotFoundException, ActionErrorException {

        PurchaseRequest entity = entityManager.find(PurchaseRequest.class, request.getEntityId());
        if (entity == null) throw new ResourceNotFoundException();

        if (!PurchaseRequestStatus.PENDING.equals(entity.getStatus()) ||
                (!Objects.equals(entity.getRequester(), request.getUserProfile().getCollaborator())
                		&& !request.getUserProfile().isAdministrator())) {
            throw new AccessDeniedException();
        }

//        // Verificação de segurança de dados incompletos
//        if (entity.getWarehouse() == null || entity.getWarehouse().getCompany() == null) {
//            throw new ActionErrorException(new LanguageResource("Dados de armazém da requisição incompletos."));
//        }
//
//        if (entity.getProductType() == null || entity.getProductType().getCategory() == null) {
//            throw new ActionErrorException(new LanguageResource("Dados de produto da requisição incompletos."));
//        }

        // Validação de campos obrigatórios
        if (request.isEmpty("company")) {
            validationBuilder.addCannotBeEmpty("company");
        } else {
            Company company = entityManager.find(Company.class, request.getInteger("company"));
            if (company == null) validationBuilder.addInvalidValue("company");
        }

        if (request.isEmpty("warehouse")) {
            validationBuilder.addCannotBeEmpty("warehouse");
        } else {
            Warehouse warehouse = entityManager.find(Warehouse.class, request.getInteger("warehouse"));
            if (warehouse == null) validationBuilder.addInvalidValue("warehouse");
        }

        if (request.isEmpty("category")) {
            validationBuilder.addCannotBeEmpty("category");
        }

        ProductType type = null;
        if (request.isEmpty("productType")) {
            validationBuilder.addCannotBeEmpty("productType");
        } else {
            type = entityManager.find(ProductType.class, request.getInteger("productType"));
            if (type == null) {
                validationBuilder.addInvalidValue("productType");
            } else {
                request.set("productType", type);
            }
        }

        if (!request.isEmpty("product")) {
            Product product = entityManager.find(Product.class, request.getInteger("product"));
            if (product == null) {
                validationBuilder.addInvalidValue("product");
            } else {
                request.set("product", product);
            }
        }

        if (request.isEmpty("quantity")) {
            validationBuilder.addCannotBeEmpty("quantity");
        }

        String description = request.getString("description");
        if (description == null || description.trim().length() < 25) {
            validationBuilder.addMinimumLength("description", 25);
        }

        request.set("entity", entity);
    }


    @Override
    public void onSaveRequest(EntityManagerWrapper entityManager, ActionRequest request,
                               ActivityBuilder activityBuilder)
            throws ResourceNotFoundException, ActionErrorException, InternalServerErrorException {

        PurchaseRequest entity = request.get("entity", PurchaseRequest.class);
        if (entity == null) throw new ResourceNotFoundException();

        Warehouse warehouse = entityManager.find(Warehouse.class, request.getInteger("warehouse"));
        ProductType productType = request.get("productType", ProductType.class);
        Product product = request.isSet("product") ? request.get("product", Product.class) : null;
        String description = request.getString("description");

        BigDecimal quantity= request.getBigDecimal("quantity");
        

        activityBuilder.checkField(LRCore.WAREHOUSE, entity.getWarehouse(), warehouse);
        activityBuilder.checkField(LRProduct.PRODUCT_TYPE, entity.getProductType(), productType);
        activityBuilder.checkField(LRProduct.PRODUCT, entity.getProduct(), product);
        activityBuilder.checkField(LRCore.QUANTITY, entity.getQuantity(), quantity);
        activityBuilder.checkField(LRCore.DESCRIPTION, !Objects.equals(entity.getDescription(), description));
        
        entity.setWarehouse(warehouse);
        entity.setProductType(productType);
        entity.setProduct(product);
        entity.setQuantity(quantity);
        entity.setDescription(description);

      
    }
}
