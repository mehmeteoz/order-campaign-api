package com.ecommerce.order_api.seeder;

import com.ecommerce.order_api.entity.Author;
import com.ecommerce.order_api.entity.Campaign;
import com.ecommerce.order_api.entity.Category;
import com.ecommerce.order_api.entity.Product;
import com.ecommerce.order_api.repository.AuthorRepository;
import com.ecommerce.order_api.repository.CampaignRepository;
import com.ecommerce.order_api.repository.CategoryRepository;
import com.ecommerce.order_api.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final ProductRepository productRepository;
    private final CampaignRepository campaignRepository;
    private final ObjectMapper objectMapper;

    public DataSeeder(CategoryRepository categoryRepository,
                      AuthorRepository authorRepository,
                      ProductRepository productRepository,
                      CampaignRepository campaignRepository,
                      ObjectMapper objectMapper) {
        this.categoryRepository = categoryRepository;
        this.authorRepository = authorRepository;
        this.productRepository = productRepository;
        this.campaignRepository = campaignRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {

        if (categoryRepository.count() == 0) {
            loadCategories();
        }

        if (authorRepository.count() == 0) {
            loadAuthors();
        }

        if (productRepository.count() == 0) {
            loadProducts();
        }

        if (campaignRepository.count() == 0) {
            loadCampaigns();
        }

        System.out.println("Data seeding has been completed successfully with json files.");
    }

    private void loadCategories() throws Exception {

        try (InputStream inputStream = getClass().getResourceAsStream("/categories.json")) {
            if (inputStream == null) {
                throw new RuntimeException("categories.json file not found!");
            }
            List<Category> categories = objectMapper.readValue(inputStream, new TypeReference<List<Category>>() {});
            categoryRepository.saveAll(categories);
        }
    }

    private void loadAuthors() throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream("/authors.json")) {
            if (inputStream == null) {
                throw new RuntimeException("authors.json file not found!");
            }
            List<Author> authors = objectMapper.readValue(inputStream, new TypeReference<List<Author>>() {});
            authorRepository.saveAll(authors);
        }
    }

    private void loadProducts() throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream("/products.json")) {
            if (inputStream == null) {
                throw new RuntimeException("products.json file not found!");
            }
            List<Product> products = objectMapper.readValue(inputStream, new TypeReference<List<Product>>() {});
            productRepository.saveAll(products);
        }
    }

    private void loadCampaigns() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/campaigns.json")) {
            if (inputStream == null) {
                throw new RuntimeException("campaigns.json file not found!");
            }
            List<Campaign> campaigns = objectMapper.readValue(inputStream, new TypeReference<List<Campaign>>() {});
            campaignRepository.saveAll(campaigns);
        }
    }


}
