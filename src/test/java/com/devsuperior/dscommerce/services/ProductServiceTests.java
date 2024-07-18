package com.devsuperior.dscommerce.services;

import com.devsuperior.dscommerce.dto.ProductDTO;
import com.devsuperior.dscommerce.dto.ProductMinDTO;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.repositories.ProductRepository;
import com.devsuperior.dscommerce.services.exceptions.DatabaseException;
import com.devsuperior.dscommerce.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscommerce.tests.ProductFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

    @InjectMocks
    private ProductService service;

    @Mock
    private ProductRepository repository;

    private long existingProductId, nonExistingProductId, dependenProductId;
    private Product product;
    private String productName;
    private PageImpl<Product> page;
    private ProductDTO productDTO;


    @BeforeEach
    void setUp() throws Exception {
        existingProductId = 1L;
        nonExistingProductId = 2L;
        dependenProductId = 3L;

        productName = "PlayStation 5";

        product = ProductFactory.createProduct(productName);
        page = new PageImpl<>(List.of(product));
        productDTO = new ProductDTO(product);

        Mockito.when(repository.findById(existingProductId)).thenReturn(Optional.of(product));
        Mockito.when(repository.findById(nonExistingProductId)).thenReturn(Optional.empty());

        Mockito.when(repository.searchByName(any(), (Pageable) any())).thenReturn(page);

        Mockito.when(repository.save(any())).thenReturn(product);

        Mockito.when(repository.getReferenceById(existingProductId)).thenReturn(product);
        Mockito.when(repository.getReferenceById(nonExistingProductId)).thenThrow(EntityNotFoundException.class);

        Mockito.when(repository.existsById(existingProductId)).thenReturn(true);
        Mockito.when(repository.existsById(dependenProductId)).thenReturn(true);
        Mockito.when(repository.existsById(nonExistingProductId)).thenReturn(false);

        Mockito.doNothing().when(repository).deleteById(existingProductId);
        Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependenProductId);

    }

    // findById deve retornar ProductDTO quando o ID existir
    @Test
    public void findByIdShouldReturnProductDTOWhenIdExists() {

        ProductDTO result = service.findById(existingProductId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), existingProductId);
        Assertions.assertEquals(result.getName(), product.getName());

    }

    // findById que retorna lançe exceção ResouceNotFound quando id não existir
    @Test
    public void findByIdShouldReturnResouceNotFoundExceptionWhenIdDoesNotExists() {

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.findById(nonExistingProductId);
        });
    }

    // findAll deve retorna page de ProductMinDTO
    @Test
    public void findAllShouldReturnPagedProductMinDTO() {

        Pageable pageable = PageRequest.of(0, 12);
//        String name = "PlayStation 5";

        Page<ProductMinDTO> result = service.findAll(productName, pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getSize(), 1);
        Assertions.assertEquals(result.iterator().next().getName(), productName);
    }

    // inserir deve retornar ProductDTO
    @Test
    public void insertShouldReturnProductDTO() {

        ProductDTO result = service.insert(productDTO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), product.getId());
    }

    // atualização deve retornar ProductDTO quando o ID existir
    @Test
    public void updateShouldReturnProductDTOWhenIdExists() {

        ProductDTO result = service.update(existingProductId, productDTO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), existingProductId);
        Assertions.assertEquals(result.getName(), productDTO.getName());
    }

    // Atualização deve retorna exceção ResourceNotFoundException para id inexistente
    @Test
    public void updateShouldReturnResourceNotFoundExceptionWhenIdDoesNotExist() {

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.update(nonExistingProductId, productDTO);
        });
    }

    // delete não deve fazer nada quando o ID existir
    @Test
    public void deleteShouldDoNothingWhenIdExists() {

        Assertions.assertDoesNotThrow(() -> {
            service.delete(existingProductId);
        });
    }

    // delete deve lançar ResourceNotFoundException quando o ID não existir
    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.delete(nonExistingProductId);
        });
    }


    // delete deve lançar DatabaseException quando o ID for dependente
    @Test
    public void deleteShouldThrowDatabaseExceptionWhenIdForDependente() {

        Assertions.assertThrows(DatabaseException.class, () -> {
            service.delete(dependenProductId);
        });
    }
}
