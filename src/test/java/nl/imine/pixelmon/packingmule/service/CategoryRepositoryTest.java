package nl.imine.pixelmon.packingmule.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.imine.pixelmon.packingmule.bag.BagCategory;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CategoryRepositoryTest {

    private CategoryRepository categoryRepository = new CategoryRepository(Paths.get("Testfolder", "test.json"));

    @Test
    public void name() throws Exception {
        categoryRepository.objectCache = new HashMap<>();
        categoryRepository.objectCache.put("Ali", new BagCategory("ali", Collections.emptyList()));
        ObjectMapper objectMapper = categoryRepository.createObjectMapper();
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(categoryRepository.objectCache));

        Map map = objectMapper.readValue(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(categoryRepository.objectCache), Map.class);
        System.out.println(map);

    }


}