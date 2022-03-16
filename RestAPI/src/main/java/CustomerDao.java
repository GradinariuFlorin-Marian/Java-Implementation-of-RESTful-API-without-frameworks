import com.sun.net.httpserver.HttpExchange;
import org.hibernate.SessionFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class CustomerDao {
    private SessionFactory fact;

    public CustomerDao(SessionFactory fact) {
        this.fact = fact;
    }


    public void createCustomer(HttpExchange exchange, String firstName, String lastName, int age, int id) {
        try {
            EntityManager EntityManager = fact.createEntityManager();
            EntityTransaction transaction = EntityManager.getTransaction();
            transaction.begin();
            Customer customer = new Customer();
            customer.setId(id);
            customer.setAge(age);
            customer.setFirstName(firstName);
            customer.setLastName(lastName);
            EntityManager.persist(customer);
            transaction.commit();
            EntityManager.close();
        } catch (Exception e) {
            String val = "Internal Server Error!";
            try {
                exchange.sendResponseHeaders(500, val.getBytes().length);
                OutputStream output = exchange.getResponseBody();
                output.write(val.getBytes());
                output.flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public Customer getCustomer(int id) {
        return fact.createEntityManager().getReference(Customer.class, id);
    }

    public boolean verifyCustomer(int id) {
        if (fact.createEntityManager().find(Customer.class, id) != null) {
            return true;
        }
        return false;
    }

    public List<Customer> getAllCustomers() {
        return fact.createEntityManager().createQuery("SELECT c FROM Customer c", Customer.class).getResultList();
    }

    public boolean deleteCustomer(int id) {
        EntityManager EntityManager = fact.createEntityManager();
        EntityTransaction transaction = EntityManager.getTransaction();
        try {
            Customer cm = EntityManager.getReference(Customer.class, id);
            transaction.begin();
            EntityManager.remove(cm);
            transaction.commit();
            EntityManager.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteAllCustomers() {
        EntityManager EntityManager = fact.createEntityManager();
        EntityTransaction transaction = EntityManager.getTransaction();
        transaction.begin();
        for (Customer cst : getAllCustomers()) {
            EntityManager.remove(cst);
        }
        transaction.commit();
        EntityManager.close();
    }

    public void modifyCustomer(HttpExchange exchange, int id, String firstName, String lastName, int age) {
        try {
            EntityManager EntityManager = fact.createEntityManager();
            EntityTransaction transaction = EntityManager.getTransaction();
            Customer cst = fact.createEntityManager().getReference(Customer.class, id);
            cst.setFirstName(firstName);
            cst.setLastName(lastName);
            cst.setAge(age);
            transaction.begin();
            EntityManager.merge(cst);
            transaction.commit();
            EntityManager.close();
        } catch (Exception e) {
            String val = "Internal Server Error!";
            try {
                exchange.sendResponseHeaders(500, val.getBytes().length);
                OutputStream output = exchange.getResponseBody();
                output.write(val.getBytes());
                output.flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void modifyAllCustomers(String firstName, String lastName, int age) {
        EntityManager EntityManager = fact.createEntityManager();
        EntityTransaction transaction = EntityManager.getTransaction();
        transaction.begin();
        for (Customer cst : getAllCustomers()) {
            cst.setFirstName(firstName);
            cst.setLastName(lastName);
            cst.setAge(age);
            EntityManager.merge(cst);
        }
        transaction.commit();
        EntityManager.close();
    }

    public void modifyCustomerWithPatch(HttpExchange exchange, int id, String firstName, String lastName, int age) {
        try {
            EntityManager EntityManager = fact.createEntityManager();
            EntityTransaction transaction = EntityManager.getTransaction();
            Customer cst = fact.createEntityManager().getReference(Customer.class, id);
            if (firstName != null)
                cst.setFirstName(firstName);
            if (lastName != null)
                cst.setLastName(lastName);
            if (age != -1)
                cst.setAge(age);
            transaction.begin();
            EntityManager.merge(cst);
            transaction.commit();
            EntityManager.close();
        } catch (Exception e) {
            String val = "Internal Server Error!";
            try {
                exchange.sendResponseHeaders(500, val.getBytes().length);
                OutputStream output = exchange.getResponseBody();
                output.write(val.getBytes());
                output.flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void modifyCustomerWithPatchForAll(HttpExchange exchange, String firstName, String lastName, int age) {
        try {
            EntityManager EntityManager = fact.createEntityManager();
            EntityTransaction transaction = EntityManager.getTransaction();
            transaction.begin();
            for (Customer cst : getAllCustomers()) {
                if (firstName != null)
                    cst.setFirstName(firstName);
                if (lastName != null)
                    cst.setLastName(lastName);
                if (age != -1)
                    cst.setAge(age);
                EntityManager.merge(cst);
            }
            transaction.commit();
            EntityManager.close();
        } catch (Exception e) {
            String val = "Internal Server Error!";
            try {
                exchange.sendResponseHeaders(500, val.getBytes().length);
                OutputStream output = exchange.getResponseBody();
                output.write(val.getBytes());
                output.flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
