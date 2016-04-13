package course;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.descending;

public class BlogPostDAO {
    MongoCollection<Document> postsCollection;

    public BlogPostDAO(final MongoDatabase blogDatabase) {
        postsCollection = blogDatabase.getCollection("posts");
    }

    // Return a single post corresponding to a permalink
    public Document findByPermalink(String permalink) {

        Document post;
        Bson filter = eq("permalink", permalink);
        post = postsCollection.find(filter).first();
        return post;
    }

    // Return a list of posts in descending order. Limit determines
    // how many posts are returned.
    public List<Document> findByDateDescending(int limit) {

        List<Document> posts;
        Bson sort = descending("date");
        posts = postsCollection.find()
                .sort(sort)
                .limit(limit)
                .into(new ArrayList<Document>());

        return posts;
    }


    public String addPost(String title, String body, List tags, String username) {

        System.out.println("inserting blog entry " + title + " " + body);

        String permalink = title.replaceAll("\\s", "_"); // whitespace becomes _
        permalink = permalink.replaceAll("\\W", ""); // get rid of non alphanumeric
        permalink = permalink.toLowerCase();

        // Build the post object and insert it
        Document post = new Document();
        post.append("author", username)
                .append("body", body)
                .append("comments", new ArrayList<Document>())
                .append("date", new Date())
                .append("permalink", permalink)
                .append("tags", tags)
                .append("title", title);
        postsCollection.insertOne(post);

        return permalink;
    }

    // Append a comment to a blog post
    public void addPostComment(final String name, final String email, final String body,
                               final String permalink) {

        Document post = findByPermalink(permalink);

        Document postComment = new Document();

        postComment.append("author", name).append("body", body);

        if (email != null && !email.equals("")) {
            // the provided email address
            postComment.append("email", email);
        }

        post.append("comments", postComment);

        try {
            postsCollection.updateOne(eq("permalink", permalink), new Document("$push", new Document("comments",postComment)));
        } catch (MongoWriteException e) {
            System.out.println("Exception in addPostComment method");
            throw e;
        }
   }
}
