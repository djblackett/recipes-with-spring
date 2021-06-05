package recipes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;


@Controller
public class RecipesController {

    public static Gson gson = new Gson();


    RecipeService recipeService;

    @Autowired
    public RecipesController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/api/recipe/{id}")
    ResponseEntity<Recipe> getRecipe(@PathVariable Long id, HttpServletResponse response) {
        response.setContentType("application/json");
        Recipe recipe;

        try {
            recipe = recipeService.getRecipe(id);
            if (recipe == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(recipe, HttpStatus.OK);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/api/recipe/new")
    public ResponseEntity<Object> newRecipe(@RequestBody String body) {
        JSONObject idResponse = new JSONObject();

        try {
            Recipe newRecipe = gson.fromJson(body, Recipe.class);

            // Null and empty value checks. Sends 404 on failure
            if (newRecipe.getName() == null || newRecipe.getName().trim().equals("") ||
                    newRecipe.getDescription() == null || newRecipe.getDescription().trim().equals("") ||
                    newRecipe.getIngredients() == null || newRecipe.getDirections() == null ||
                    newRecipe.getDirections().isEmpty() || newRecipe.getIngredients().isEmpty()
            ) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // Persist Recipe and return id
            recipeService.newRecipe(newRecipe);
            Long newRecipeId = newRecipe.getId();
            idResponse.put("id", newRecipeId);
            return new ResponseEntity<>(idResponse.toMap(), HttpStatus.OK);

        } catch (Exception e) {
            System.out.println("error caught in post method");
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/api/recipe/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {

        try {
            recipeService.deleteRecipeById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/api/recipe/search")
    ResponseEntity<List<Recipe>> search(@RequestParam(required = false) String name, @RequestParam(required = false) String category) {

        // XOR
        if ((name == null && category == null) || (name != null && category != null)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<Recipe> recipes = null;

        try {
            if (name != null) {
                recipes = recipeService.recipeRepository.findByNameContainingIgnoreCaseOrderByDateDesc(name);
            }

            if (category != null) {
                recipes = recipeService.getRecipesByCategory(category);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

        return new ResponseEntity<>(recipes, HttpStatus.OK);
    }

    @PutMapping("/api/recipe/{id}")
    ResponseEntity<Void> updateRecipe(@PathVariable Long id, @Validated @RequestBody String body) {
        Optional<Recipe> recipeOpt;
        Recipe newRecipeDetails;

        if (body == null || body.equals("")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            recipeOpt = recipeService.findById(id);
            newRecipeDetails = gson.fromJson(body, Recipe.class);

            if (recipeOpt.isPresent()) {
                // todo use setter methods to complete
                //recipeService.updateRecipe(id);
                Recipe recipe = recipeOpt.get();
                System.out.println(newRecipeDetails);
                recipe.setName(newRecipeDetails.getName());
                recipe.setDescription(newRecipeDetails.getDescription());
                recipe.setIngredients(newRecipeDetails.getIngredients());
                recipe.setDirections(newRecipeDetails.getDirections());
                recipe.setCategory(newRecipeDetails.getCategory());
                recipeService.newRecipe(recipe);

                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("api/registration")
    ResponseEntity<Void> registerUser(@RequestBody String body) {

        User user = gson.fromJson(body, User.class);

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
