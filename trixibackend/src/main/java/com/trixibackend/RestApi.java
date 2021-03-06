package com.trixibackend;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.trixibackend.entity.*;
import express.Express;
import express.http.SessionCookie;
import express.middleware.Middleware;
import express.utils.Status;
import org.apache.commons.fileupload.FileItem;
import org.bson.types.ObjectId;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import static java.lang.Integer.parseInt;

public class RestApi {

    Express app = new Express();
    DatabaseHandler db = new DatabaseHandler();

    public RestApi() {
        initApi();
    }

    private void initApi() {
        app.listen(3000);
        // sets a unique cookie on each client to track authentication
        app.use(Middleware.cookieSession("f3v4", 60 * 60 * 24 * 7));
        System.out.println("server started on port 3000");

        var allCollectionsName = db.getDatabase().listCollectionNames();

        allCollectionsName.forEach(collectionName -> {
            setUpGetApi(collectionName);
            setUpPostApi(collectionName);
            setUpDeleteApi(collectionName);

        });
        setUpUpdateApi();
        followUnfollowApi();
        likeAndCommentApi();
        setLoginUser();
        getLoggedinUser();
        logoutUser();
        setImagePostApi();

        try {
            app.use(Middleware.statics(Paths.get("").toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void followUnfollowApi() {

        app.post("/api/users/follow/:userid/:followingId", (req, res) -> {

            String userid = req.getParam("userid");
            String followingId = req.getParam("followingId");

            User user = db.getUserHandler().findUserById(userid);
            User followingUser = db.getUserHandler().findUserById(followingId);

            if (followingUser == null) {
                Pet followingPet = db.getPetHandler().findPetById(followingId);
                System.out.println("(Pet) Following:  " + followingPet);
                var updatedUser = db.getUserHandler().updateFollowPetList(user, followingPet);
                if (updatedUser == null) {
                    res.setStatus(Status._403);
                    res.send("Error: you are already following this Pet");
                    return;
                }
                res.json(updatedUser);
            } else {
                var updatedUser = db.getUserHandler().updateFollowUserList(user, followingUser);
                if (updatedUser == null) {
                    res.setStatus(Status._403);
                    res.send("Error: you are already following this User");
                    return;
                }
                res.json(updatedUser);

            }
        });


        app.post("/api/users/un_follow/:userid/:followingId", (req, res) -> {

            String userid = req.getParam("userid");
            String followingId = req.getParam("followingId");

            User followingUser = db.getUserHandler().findUserById(followingId);
            User user = db.getUserHandler().findUserById(userid);

            if (followingUser == null) {
                Pet followingPet = db.getPetHandler().findPetById(followingId);

                var updatedUser = db.getUserHandler().removeFromFollowPetList(user, followingPet);
                if (updatedUser == null) {
                    res.setStatus(Status._403);
                    res.send("Error: you are not following this Pet");
                    return;
                }
                res.json(updatedUser);
            } else {
                var updatedUser = db.getUserHandler().removeFromFollowUserList(user, followingUser);
                if (updatedUser == null) {
                    res.setStatus(Status._403);
                    res.send("Error: you are not following this user");
                    return;
                }
                res.json(updatedUser);
            }
        });

    }

    private void setUpUpdateApi() {

        app.post("/rest/update_post", (req, res) -> {
            Post updatedPost = (Post) req.getBody(Post.class);
            var sessionCookie = (SessionCookie) req.getMiddlewareContent("sessioncookie");
            var loggedInUser = (User) sessionCookie.getData();

            if (updatedPost != null ) {
//                if(!loggedInUser.getUid().equals(updatedPost.getOwnerId())){
//                    res.send("Not allowed");
//                    return;
//                }
                Post oldPost = db.getPostHandler().findPostById(updatedPost.getUid());
                updatedPost.setId(oldPost.getId());
                updatedPost.setFilePath(oldPost.getFilePath());
                updatedPost.setFileType(oldPost.getFileType());
                updatedPost.setLikes(oldPost.getLikes());
                updatedPost.setComments(oldPost.getComments());
                db.save(updatedPost);
                res.json(db.getPostHandler().findPostById(updatedPost.getUid()));
            }
        });
    }


    private void setUpDeleteApi(String collectionName) {

        app.delete("/rest/" + collectionName + "/:id", (req, res) -> {
            var sessionCookie = (SessionCookie) req.getMiddlewareContent("sessioncookie");
            var loggedInUser = (User) sessionCookie.getData();

            var id = req.getParam("id");
            var obj = db.deleteById(collectionName, id,loggedInUser,res,req);
            res.json(obj);
            res.send("Succesfully deleted");
        });
    }

    private void setImagePostApi() {
        app.post("/rest/image", (req, res) -> {
            List<FileItem> files = null;
            String fileUrl = null;
            try {
                files = req.getFormData("file");
                fileUrl = db.uploadImage(files.get(0));
                System.out.println(files.get(0).getName());
                res.json(Map.of("url", files.get(0).getName()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setUpPostApi(String collectionName) {

        app.post("/rest/" + collectionName, (req, res) -> {
            var sessionCookie = (SessionCookie) req.getMiddlewareContent("sessioncookie");
            var loggedInUser = (User) sessionCookie.getData();
            switch (collectionName) {
                case "users":

                    List<FileItem> files = null;
                    String uid = null;
                    String fileUrl = null;
                    String userName = null;
                    String email = null;
                    String password = null;
                    String bio = null;

                    try {
                        files = (req.getFormData("file"));
                        uid = (req.getFormData("uid") != null ? req.getFormData("uid").get(0).getString().replace("\"", "") : null);
                        userName = req.getFormData("userName").get(0).getString().replace("\"", "");
                        email = req.getFormData("email").get(0).getString().replace("\"", "");
                        password = (req.getFormData("password") != null ? req.getFormData("password").get(0).getString().replace("\"", "") : null);
                        bio = (req.getFormData("bio") != null ? req.getFormData("bio").get(0).getString().replace("\"", "") : null);

                        User user = new User();
                        User oldUser = db.getUserHandler().findUserById(uid);

                        if (uid != null) {
                            if(!loggedInUser.getUid().equals(uid)){
                                res.send("not allowed");
                                return;
                            }
                            user.setUid(uid);
                            user.setId(new ObjectId(uid));
                            user.setFollowingsUser(oldUser.getFollowingsUser());
                            user.setFollowingsPet(oldUser.getFollowingsPet());
                            user.setFollowers(oldUser.getFollowers());
                            if (password == null) {
                                user.setPassword(oldUser.getPassword());
                            } else {
                                String hashedPassword = BCrypt.withDefaults().hashToString(10, password.toCharArray());
                                user.setPassword(hashedPassword);
                            }
                        }

                        user.setUserName(userName);
                        user.setEmail(email);

                        if (bio != null) {
                            user.setBio(bio.trim());
                        }

                        if (password != null) {
                            String hashedPassword = BCrypt.withDefaults().hashToString(10, password.toCharArray());
                            user.setPassword(hashedPassword);
                        }

                        if (files != null) {
                            user.setImageUrl(db.uploadImage(files.get(0)));
                        } else user.setImageUrl(oldUser.getImageUrl());

                        user.setRole("user");
                        System.out.println(user.getUserName());
                        db.save(user);

                        //var sessionCookie = (SessionCookie) req.getMiddlewareContent("sessioncookie");

                        user.setPassword(null);
                        sessionCookie.setData(user);

                        var userLoggedIn = (User) sessionCookie.getData();
                        userLoggedIn.setImageUrl(user.getImageUrl());
                        userLoggedIn.setPassword(null); // sanitize password
                        userLoggedIn.setUid(user.getId().toString());
                        userLoggedIn.setBio(user.getBio());
                        userLoggedIn.setPosts(db.getPostHandler().findPostsByOwner(user.getUid()));
                        userLoggedIn.setPets(db.getPetHandler().findPetsByOwner(user.getUid()));
                        userLoggedIn.getPosts().forEach(post -> {
                            post.setUid(post.getId().toString());

                        });

                        res.json(userLoggedIn);
                        res.send("Created User");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case "posts":
                    List<FileItem> Postfiles = null;
                    String PostfileUrl = null;
                    String postId = null;
                    String description = null;
                    String ownerId = null;
                    String title = null;
                    String categoryName = null;
                    String fileType = null;

                    try {
                        Postfiles = req.getFormData("file");
                        postId = (req.getFormData("uid") != null ? req.getFormData("uid").get(0).getString().replace("\"", "") : null);
                        description = req.getFormData("description").get(0).getString().replace("\"", "");
                        ownerId = req.getFormData("ownerId").get(0).getString().replace("\"", "");
                        title = req.getFormData("title").get(0).getString().replace("\"", "");
                        categoryName = req.getFormData("categoryName").get(0).getString().replace("\"", "");
                        fileType = req.getFormData("fileType").get(0).getString().replace("\"", "");

                        if(!loggedInUser.getUid().equals(ownerId)){
                            res.send("not allowed");
                            return;
                        }

                        PostfileUrl = db.uploadImage(Postfiles.get(0));

                        Post post = new Post();
                        post.setDescription(description);
                        post.setOwnerId(ownerId);
                        post.setTitle(title);
                        post.setFilePath(PostfileUrl);
                        post.setFileType(fileType);
                        post.setCategoryName(categoryName);

                        db.save(post);
                        post.setUid(post.getId().toString());

                        res.json(post);
                        res.send("Created Post");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                case "pets":
                    List<FileItem> Petfiles = null;
                    String PetFileUrl = null;
                    String petOwnerId = null;
                    String name = null;
                    String petUid = null;
                    String age = null;
                    String petBio = null;
                    String breed = null;
                    String Type = null;
                    String gender = null;

                    try {
                        Petfiles = req.getFormData("file");
                        name = req.getFormData("name").get(0).getString().replace("\"", "");
                        petUid = (req.getFormData("uid") != null ? req.getFormData("uid").get(0).getString().replace("\"", "") : null);
                        petOwnerId = req.getFormData("ownerId").get(0).getString().replace("\"", "");
                        age = req.getFormData("age").get(0).getString().replace("\"", "");
                        petBio = req.getFormData("bio").get(0).getString().replace("\"", "");
                        breed = req.getFormData("breed").get(0).getString().replace("\"", "");
                        Type = req.getFormData("petType").get(0).getString().replace("\"", "");
                        gender = req.getFormData("gender").get(0).getString().replace("\"", "");

                        if(!loggedInUser.getUid().equals(petOwnerId)){
                            res.send("not allowed");
                            return;
                        }

                        Pet pet = new Pet();

                        if(petUid != null){
                            Pet oldPet = db.getPetHandler().findPetById(petUid);
                            pet.setUid(oldPet.getUid());
                            pet.setId(oldPet.getId());
                            if(Petfiles == null){
                                pet.setImageUrl(oldPet.getImageUrl());
                            }
                        }

                        if(Petfiles != null){
                            PetFileUrl = db.uploadImage(Petfiles.get(0));
                            pet.setImageUrl(PetFileUrl);
                        }

                        pet.setName(name);
                        pet.setOwnerId(petOwnerId);
                        pet.setAge(age);
                        pet.setBio(petBio);
                        pet.setBreed(breed);
                        pet.setPetType(Type);
                        pet.setGender(gender);



                        db.save(pet);

                        pet.setUid(pet.getId().toString());

                        System.out.println(pet.getUid());
                        System.out.println(pet);

                        res.json(pet);
                        res.send("Created Pet");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "categories":
                    Category category = (Category) req.getBody(Category.class);
                    res.json(db.save(category));
                    break;
                case "pet_types":
                    PetType petType = (PetType) req.getBody(PetType.class);
                    res.json(db.save(petType));
                case "reports":
                    Report report = (Report) req.getBody(Report.class);
                    System.out.println(report);
                    res.json(db.save(report));
                default:
                    break;
            }
        });
    }

    private void setUpGetApi(String collectionName) {

        app.get("/rest/" + collectionName, (req, res) -> res.json(db.getAll(collectionName)));

        app.get("/rest/posts/pagelimit/", (req, res) -> {
            String page = req.getQuery("page");
            String limit = req.getQuery("limit");

            int pageNumber = parseInt(page);
            int limitNumber = parseInt(limit);

            int startIndex = (pageNumber -1) * limitNumber;
            int endIndex = startIndex + limitNumber;
            var results = db.getPostHandler().getAllPosts();
            
            int lastPage = results.size()/limitNumber + 1;

            if(pageNumber > lastPage){
                res.json(null);
            } else if (pageNumber == lastPage) {
                var re = results.subList(startIndex, results.size());
                res.json(re);
            } else {
                var re = results.subList(startIndex, endIndex);

                res.json(re);
            }

        });

        app.get("/rest/posts/by_category/:id", (req, res) -> {

            String id = req.getParam("id");
            var obj = db.getPostHandler().getPostsByCategory(id);
            if (obj == null) {
                res.send("Error: no Object found");
                return;
            }
            res.json(db.getPostHandler().getPostsByCategory(id));
        });

        app.get("/rest/pets/by_pet_type/:id", (req, res) -> {

            String id = req.getParam("id");
            var obj = db.getPetHandler().findPetsByPetType(id);
            if (obj == null) {
                res.send("Error: no Object found");
                return;
            }
            res.json(db.getPetHandler().findPetsByPetType(id));
        });


        //b
        app.get("/rest/" + collectionName + "/by_owner/:id", (req, res) -> {

            String id = req.getParam("id");
            var obj = db.getByOwner(collectionName, id);
            if (obj == null) {
                res.send("Error: no Object found");
                return;
            }
            res.json(db.getByOwner(collectionName, id));
        });

        app.get("/rest/" + collectionName + "/:id", (req, res) -> {

            String id = req.getParam("id");

            if (collectionName.equals("notifications")) {
                var notifications = db.getNotificationHandler().getNotificationByPostOwner(id);
                res.json(notifications);
                return;
            }
            var obj = db.getById(collectionName, id);
            if (obj == null) {
                res.send("Error: no Object found");
                return;
            }

            res.json(db.getById(collectionName, id));
        });


        app.get("/api/getUserFollowingPost/:id", (req, res) -> {

            String id = req.getParam("id");

            User user = db.getUserHandler().findUserById(id);

            var followingPostList = db.getUserHandler().findUserFollowingPostList(user);
            if (followingPostList == null) {
                res.setStatus(Status._403);
                return;
            }
            System.out.println(followingPostList.size());
            res.json(followingPostList);
        });

        app.get("/api/search/:searchterm", (req, res) -> {

            try {
                String searchterm = req.getParam("searchterm");

                var alluser = db.getUserHandler().getAllUsers();
                var allpet = db.getPetHandler().getAllPets();

                var searchPost = db.getPostHandler().searchPost(searchterm, alluser, allpet);
                if (searchPost == null) {
                    res.setStatus(Status._403);
                    //res.send("Error: you are not following this Pet");
                    return;
                }
                res.json(searchPost);
            } catch (Exception e) {
                e.printStackTrace();

            }
        });

        app.get("/api/searchPost/", (req, res) -> {

            try {


                var alluser = db.getUserHandler().getAllUsers();
                var allpet = db.getPetHandler().getAllPets();

                var searchPost = db.getPostHandler().findPostWhitouthUser(alluser, allpet);

                System.out.println(searchPost.size());


                res.json(searchPost);
            } catch (Exception e) {
                e.printStackTrace();

            }
        });


        app.get("/api/discover/:id", (req, res) -> {

            String id = req.getParam("id");

            User user = db.getUserHandler().findUserById(id);

            var followingPostList = db.getUserHandler().discoverList(user);
            if (followingPostList == null) {
                res.setStatus(Status._403);
                return;
            }
            System.out.println(followingPostList.size());
            res.json(followingPostList);
        });

        app.get("/api/posttype/:pettype", (req, res) -> {

            String petType = req.getParam("pettype");

            var petsByType = db.getPetHandler().findPetsByPetTypeName(petType);
            var postByPetType = db.getPostHandler().findPostByPetType(petsByType);

            res.json(postByPetType);
        });

    }

    private void setLoginUser() {

        app.post("/rest/login", (req, res) -> {

            var sessionCookie = (SessionCookie) req.getMiddlewareContent("sessioncookie");

            if (sessionCookie.getData() != null) {
                res.send("Already logged in");
                return;
            }

            User loggedInUser = (User) req.getBody(User.class);
            User user = (User) db.getLoginByNameOrEmail(loggedInUser);

            if (user == null) {
                res.send((loggedInUser.getUserName() == "" || loggedInUser.getUserName() == null ? "Email: " + loggedInUser.getEmail() : "Username: " + loggedInUser.getUserName()) + " does not exist");
                return;
            }

            var result = BCrypt.verifyer().verify(loggedInUser.getPassword().toCharArray(), user.getPassword().toCharArray());
            if (!result.verified) {
                res.setStatus(Status._401);
                res.send("password and username/email dont match");
                res.json(user);
                return;
            }

            user.setPassword(null); // sanitize password
            sessionCookie.setData(user);

            var userLoggedIn = (User) sessionCookie.getData();
            userLoggedIn.setPassword(null); // sanitize password
            userLoggedIn.setUid(user.getId().toString());
            userLoggedIn.setPosts(db.getPostHandler().findPostsByOwner(user.getUid()));
            userLoggedIn.setPets(db.getPetHandler().findPetsByOwner(user.getUid()));

            res.json(userLoggedIn);
        });
    }


    private void getLoggedinUser() {
        app.get("/rest/login", (req, res) -> {
            var sessionCookie = (SessionCookie) req.getMiddlewareContent("sessioncookie");

            if (sessionCookie.getData() == null) {
                res.send("Not logged in");
                return;
            }

            var user = (User) sessionCookie.getData();
            user.setUid(user.getId().toString());
            user.setPassword(null); // sanitize password

            res.json(user);
        });
    }

    private void logoutUser() {
        app.get("/rest/logout", (req, res) -> {
            var sessionCookie = (SessionCookie) req.getMiddlewareContent("sessioncookie");
            sessionCookie.setData(null);
            res.send("Successfully logged out");
        });
    }

    private void likeAndCommentApi() {
        app.post("/rest/likes", (req, res) -> {
            Like like = (Like) req.getBody(Like.class);
            Post p = db.getPostHandler().addLike(like);
            if (p == null) {
                res.setStatus(Status._403);
                res.send("Error: you already liked this post");
                return;
            }
            if (!like.getUserId().equals(p.getOwnerId())) {

                Notification notification = new Notification();
                notification.setLike(like);
                notification.setComment(null);
                notification.setPost(p);
                notification.setPostOwnerId(p.getOwnerId());
                db.save(notification);
                notification.setUid(notification.getId().toString());
            }
            res.json(p);
        });

        app.post("/rest/unlike", (req, res) -> {
            Like like = (Like) req.getBody(Like.class);
            Post p = db.getPostHandler().unlike(like);
            if (p == null) {
                res.setStatus(Status._403);
                res.send("Error: you already not liking this post");
                return;
            }
            db.getNotificationHandler().deleteNotificationByLike(like);
            //System.out.println("notification deleted: " + result);
            res.json(p);
        });

        app.post("/rest/comments", (req, res) -> {
            Comment comment = (Comment) req.getBody(Comment.class);
            comment.setId(UUID.randomUUID().toString());

            Post p = db.getPostHandler().findPostById(comment.getPostId());
            if (!comment.getUserId().equals(p.getOwnerId())) {
                Notification notification = new Notification();
                notification.setComment(comment);
                notification.setPost(p);
                notification.setPostOwnerId(p.getOwnerId());
                db.save(notification);
                notification.setUid(notification.getId().toString());
            }
            res.json(db.getPostHandler().addComment(comment));
        });

        app.post("/rest/delete_comment", (req, res) -> {
            Comment comment = (Comment) req.getBody(Comment.class);
            Post p = db.getPostHandler().deleteComment(comment);
            if (p == null) {
                res.setStatus(Status._403);
                res.send("Error, comment doesn't exist");
                return;
            }
            res.json(p);
        });
    }

}
