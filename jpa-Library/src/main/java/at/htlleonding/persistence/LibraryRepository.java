package at.htlleonding.persistence;
///home/peter/src/dbi4/quarkus-hibernate-cmdline

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

// @Transactional
// https://quarkus.io/guides/transaction
// https://quarkus.io/guides/hibernate-orm
// Mark your CDI bean method as @Transactional and the EntityManager will enlist and flush at commit.
// Make sure to wrap methods modifying your database (e.g. entity.persist()) within a transaction.
// Marking a CDI bean method @Transactional will do that for you and make that method a transaction boundary.
// We recommend doing so at your application entry point boundaries like your REST endpoint controllers.

@ApplicationScoped
public class LibraryRepository {

    @Inject
    EntityManager entityManager;

    @Transactional
    public void add(Author a) {
        entityManager.persist(a);
    }

    @Transactional
    public void add(Book b) { entityManager.persist(b);  }

    @Transactional
    public void add(Topic t) {
        entityManager.persist(t);
    }

    @Transactional
    public void add(Book b, Topic t) {
        if(b.getId() == null) {
            add(b);
        }
        if(t.getId() == null) {
            add(t);
        }

        b.getTopics().add(t);
        t.getBooks().add(b);

        entityManager.persist(b);
        entityManager.persist(t);
    }

    @Transactional
    public void add(Genre g) {
        entityManager.persist(g);
    }

    @Transactional
    public void add(Book b, Genre g) {
        if(b.getId() == null) {
            add(b);
        }
        if(g.getId() == null) {
            add(g);
        }

        b.setGenre(g);
        g.getBooks().add(b);

        entityManager.persist(b);
        entityManager.persist(g);
    }

    @Transactional
    public List<Author> getAllAuthors() {
        return
                entityManager
                        .createQuery("select a from Author a", Author.class)
                        .getResultList();
    }

    @Transactional
    public Author getAuthorByLastName(String lastName) {
        try {
            return entityManager
                    .createQuery("select p from Author p where p.lastName = :name ", Author.class)
                    .setParameter("name", lastName)
                    .getSingleResult();
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }


    @Transactional
    public Book getBook(String authorLastName, String title) {
        try {
            return entityManager
                    .createQuery("select ba.book from BookAuthor ba where ba.book.title = :title and ba.author.lastName = :authorLastName", Book.class)
                    .setParameter("title", title)
                    .setParameter("authorLastName", authorLastName)
                    .getSingleResult();
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Transactional
    public Genre getGenre(String keyword) {
        try {
            return entityManager
                    .createQuery("select g from Genre g where g.keyword = :keyword", Genre.class)
                    .setParameter("keyword", keyword)
                    .getSingleResult();
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Transactional
    public Topic getTopic(String keyword) {
        try {
            return entityManager
                    .createQuery("select t from Topic t where t.keyword = :keyword", Topic.class)
                    .setParameter("keyword", keyword)
                    .getSingleResult();
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Transactional
    public List<Book> getAllBooks(boolean includeAuthors) {
        try {
            var query = includeAuthors ?
                    entityManager.createQuery("select distinct b from Book b join fetch b.myAuthors", Book.class)
                    :
                    entityManager.createQuery("select b from Book b", Book.class);
            return query
                    .getResultList();
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Transactional
    public List<Book> getBooksOfTopic(String keyword) {
        try {
            var query = entityManager
                    .createQuery(
                            "select distinct b from Book b " +
                                    "join b.myAuthors " +
                                    "join b.topics " +
                                    "where " +
                                    "(select count(t.keyword) from b.topics t where t.keyword = :keyword) > 0 ", Book.class);

            return query
                    .setParameter("keyword", keyword)
                    .getResultList();
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Transactional
    public List<Book> getBooksOfGenre(String keyword) {
        try {
            var query = entityManager
                    .createQuery(
                            "select distinct b from Book b " +
                                    "join b.myAuthors " +
                                    "join b.genre " +
                                    "where " +
                                    "b.genre.keyword = :keyword", Book.class);
            return query
                    .setParameter("keyword", keyword)
                    .getResultList();
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Transactional
    public List<Book> getInventory() {
        try {
            var query = entityManager
                    .createQuery(
                            "select distinct (b) from Book b " +
                                    "join BookAuthor ba on ba.book.id = b.id " +
                                    "join fetch b.topics " +
                                    "join fetch b.genre " +
                                    "join fetch b.myAuthors " +
                                    "where ba.isPrimaryAuthor = true "
                            , Book.class);

            return query
                    .getResultList();
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}
