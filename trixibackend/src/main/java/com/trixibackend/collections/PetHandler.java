package com.trixibackend.collections;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.trixibackend.entity.Pet;
import com.trixibackend.entity.User;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;


public class PetHandler {

    private MongoCollection<Pet> petColl;
    private PostHandler postHandler;

    public PetHandler(MongoDatabase database) {
        petColl = database.getCollection("pets", Pet.class);
        postHandler = new PostHandler(database);
    }

    public MongoCollection<Pet> getPetColl() {
        return petColl;
    }

    public List<Pet> getAllPets() {
        List<Pet> pets = null;
        try {
            FindIterable<Pet> petIter = petColl.find();
            pets = new ArrayList<>();
            petIter.forEach(pets::add);
            pets.forEach(pet -> {
                pet.setUid(pet.getId().toString());
                pet.setPosts(postHandler.findPostsByOwner(pet.getUid()));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pets;
    }

    public Pet findPetById(String id) {
        try {
            var petIter = petColl.find(eq("_id", new ObjectId(id)));
            var pet = petIter.first();
            if (pet == null) return null;
            pet.setUid(pet.getId().toString());
            pet.setPosts(postHandler.findPostsByOwner(pet.getUid()));
            return pet;
        } catch (Exception e) {
            return null;
        }
    }

    public List<Pet> findPetsByOwner(String id) {
        List<Pet> pets = null;
        try {
            FindIterable<Pet> petsIter = petColl.find(eq("ownerId", id));
            pets = new ArrayList<>();
            petsIter.forEach(pets::add);
            pets.forEach(pet -> pet.setUid(pet.getId().toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pets;

    }
}