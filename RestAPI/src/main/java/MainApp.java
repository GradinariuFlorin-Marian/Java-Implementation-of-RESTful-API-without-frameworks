import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpServer;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class MainApp {
    public static SessionFactory factory;
    public static CustomerDao customer;

    public static void main(String[] args) throws IOException {
        try {
            factory = new Configuration().configure().buildSessionFactory();
            customer = new CustomerDao(factory);
        } catch (Throwable ex) {
            System.err.println("Failed to create sessionFactory object." + ex);
            throw new ExceptionInInitializerError(ex);
        }
        int serverPort = 6666;
        HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
        server.createContext("/customers", (exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                String[] splitter = exchange.getRequestURI().getPath().split("/");
                if (splitter.length > 2) {
                    if (Utils.isNumber(splitter[2])) {
                        if (customer.verifyCustomer(Integer.parseInt(splitter[2]))) {
                            Customer cm = customer.getCustomer(Integer.parseInt(splitter[2]));
                            ObjectMapper mapper = new ObjectMapper();
                            ObjectNode customer = mapper.createObjectNode();
                            customer.put("id", cm.getId());
                            customer.put("FirstName", cm.getFirstName());
                            customer.put("LastName", cm.getLastName());
                            customer.put("Age", cm.getAge());
                            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(customer);
                            System.out.println(json);
                            exchange.sendResponseHeaders(200, json.getBytes().length);
                            OutputStream output = exchange.getResponseBody();
                            output.write(json.getBytes());
                            output.flush();
                        } else {
                            String val = "ID not found!";
                            exchange.sendResponseHeaders(404, val.getBytes().length);
                            OutputStream output = exchange.getResponseBody();
                            output.write(val.getBytes());
                            output.flush();
                        }
                    } else {
                        String val = "ID is not a number!";
                        exchange.sendResponseHeaders(404, val.getBytes().length);
                        OutputStream output = exchange.getResponseBody();
                        output.write(val.getBytes());
                        output.flush();
                    }
                } else {
                    StringBuilder customers = new StringBuilder();
                    ObjectMapper mapper = new ObjectMapper();
                    for (Customer cm : customer.getAllCustomers()) {
                        ObjectNode customer = mapper.createObjectNode();
                        customer.put("id", cm.getId());
                        customer.put("FirstName", cm.getFirstName());
                        customer.put("LastName", cm.getLastName());
                        customer.put("Age", cm.getAge());
                        customers.append(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(customer) + "\n");
                    }
                    exchange.sendResponseHeaders(200, customers.toString().getBytes().length);
                    OutputStream output = exchange.getResponseBody();
                    output.write(customers.toString().getBytes());
                    output.flush();
                }
            } else if ("POST".equals(exchange.getRequestMethod())) {
                String[] splitter = exchange.getRequestURI().getPath().split("/");
                if (splitter.length > 2) {
                    if (Utils.isNumber(splitter[2])) {
                        if (customer.verifyCustomer(Integer.parseInt(splitter[2]))) {
                            String val = "ID already in the database!";
                            exchange.sendResponseHeaders(409, val.getBytes().length);
                            OutputStream output = exchange.getResponseBody();
                            output.write(val.getBytes());
                            output.flush();
                        } else {
                            String val = "Not Found!";
                            exchange.sendResponseHeaders(404, val.getBytes().length);
                            OutputStream output = exchange.getResponseBody();
                            output.write(val.getBytes());
                            output.flush();
                        }
                    } else {
                        String val = "ID is not a number!";
                        exchange.sendResponseHeaders(409, val.getBytes().length);
                        OutputStream output = exchange.getResponseBody();
                        output.write(val.getBytes());
                        output.flush();
                    }
                } else {
                    StringBuilder sb = new StringBuilder();
                    ObjectMapper objectMapper = new ObjectMapper();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                    }
                    String[] jsons = sb.toString().split(";");
                    for (String s : jsons) {
                        Customer c = objectMapper.readValue(s, Customer.class);
                        if (!customer.verifyCustomer(c.getId())) {
                            customer.createCustomer(exchange, c.getFirstName(), c.getLastName(),
                                    c.getAge(), c.getId());
                        }
                    }
                    String val = "Customer(s) created!";
                    exchange.sendResponseHeaders(200, val.getBytes().length);
                    OutputStream output = exchange.getResponseBody();
                    output.write(val.getBytes());
                    output.flush();
                }
            } else if ("PUT".equals(exchange.getRequestMethod())) {
                String[] splitter = exchange.getRequestURI().getPath().split("/");
                if (splitter.length > 2) {
                    if (Utils.isNumber(splitter[2])) {
                        if (customer.verifyCustomer(Integer.parseInt(splitter[2]))) {
                            if (exchange.getRequestHeaders().containsKey("FirstName") && exchange.getRequestHeaders().containsKey("LastName")
                                    && exchange.getRequestHeaders().containsKey("Age")) {
                                if (!exchange.getRequestHeaders().get("FirstName").get(0).isEmpty() && !exchange.getRequestHeaders().get("LastName").get(0).isEmpty() &&
                                        !exchange.getRequestHeaders().get("Age").get(0).isEmpty()) {
                                    customer.modifyCustomer(exchange, Integer.parseInt(splitter[2]), exchange.getRequestHeaders().get("FirstName").get(0), exchange.getRequestHeaders().get("LastName").get(0),
                                            Integer.parseInt(exchange.getRequestHeaders().get("Age").get(0)));
                                    String val = "Data modified!";
                                    exchange.sendResponseHeaders(200, val.getBytes().length);
                                    OutputStream output = exchange.getResponseBody();
                                    output.write(val.getBytes());
                                    output.flush();
                                } else {
                                    String val = "Header value empty!";
                                    exchange.sendResponseHeaders(204, val.getBytes().length);
                                    OutputStream output = exchange.getResponseBody();
                                    output.write(val.getBytes());
                                    output.flush();
                                }
                            } else {
                                String val = "Header(s) not found!";
                                exchange.sendResponseHeaders(404, val.getBytes().length);
                                OutputStream output = exchange.getResponseBody();
                                output.write(val.getBytes());
                                output.flush();
                            }
                        } else {
                            String val = "ID not found!";
                            exchange.sendResponseHeaders(404, val.getBytes().length);
                            OutputStream output = exchange.getResponseBody();
                            output.write(val.getBytes());
                            output.flush();
                        }
                    } else {
                        String val = "ID is not a number!";
                        exchange.sendResponseHeaders(404, val.getBytes().length);
                        OutputStream output = exchange.getResponseBody();
                        output.write(val.getBytes());
                        output.flush();
                    }
                } else {
                    if (exchange.getRequestHeaders().containsKey("FirstName") && exchange.getRequestHeaders().containsKey("LastName")
                            && exchange.getRequestHeaders().containsKey("Age")) {
                        if (!exchange.getRequestHeaders().get("FirstName").get(0).isEmpty() && !exchange.getRequestHeaders().get("LastName").get(0).isEmpty() &&
                                !exchange.getRequestHeaders().get("Age").get(0).isEmpty()) {
                            customer.modifyAllCustomers(exchange.getRequestHeaders().get("FirstName").get(0), exchange.getRequestHeaders().get("LastName").get(0),
                                    Integer.parseInt(exchange.getRequestHeaders().get("Age").get(0)));
                            String val = "Datas modified!";
                            exchange.sendResponseHeaders(405, val.getBytes().length);
                            OutputStream output = exchange.getResponseBody();
                            output.write(val.getBytes());
                            output.flush();
                        } else {
                            String val = "Header value empty!";
                            exchange.sendResponseHeaders(204, val.getBytes().length);
                            OutputStream output = exchange.getResponseBody();
                            output.write(val.getBytes());
                            output.flush();
                        }
                    } else {
                        String val = "Header(s) not found!";
                        exchange.sendResponseHeaders(404, val.getBytes().length);
                        OutputStream output = exchange.getResponseBody();
                        output.write(val.getBytes());
                        output.flush();
                    }

                }
            } else if ("DELETE".equals(exchange.getRequestMethod())) {
                String[] splitter = exchange.getRequestURI().getPath().split("/");
                if (splitter.length > 2) {
                    if (Utils.isNumber(exchange.getRequestURI().getQuery())) {
                        if (customer.deleteCustomer(Integer.parseInt(splitter[2]))) {
                            String val = "OK";
                            exchange.sendResponseHeaders(202, val.getBytes().length);
                            OutputStream output = exchange.getResponseBody();
                            output.write(val.getBytes());
                            output.flush();
                        } else {
                            String val = "ID not found or invalid";
                            exchange.sendResponseHeaders(404, val.getBytes().length);
                            OutputStream output = exchange.getResponseBody();
                            output.write(val.getBytes());
                            output.flush();
                        }
                    } else {
                        String val = "ID is not a number!";
                        exchange.sendResponseHeaders(404, val.getBytes().length);
                        OutputStream output = exchange.getResponseBody();
                        output.write(val.getBytes());
                        output.flush();
                    }
                } else {

                    customer.deleteAllCustomers();
                    String val = "OK";
                    exchange.sendResponseHeaders(405, val.getBytes().length);
                    OutputStream output = exchange.getResponseBody();
                    output.write(val.getBytes());
                    output.flush();
                }
            } else if ("PATCH".equals(exchange.getRequestMethod())) {
                String[] splitter = exchange.getRequestURI().getPath().split("/");
                if (splitter.length > 2) {
                    if (Utils.isNumber(splitter[2])) {
                        if (customer.verifyCustomer(Integer.parseInt(splitter[2]))) {
                            String firstName = null, lastName = null;
                            int age = -1;
                            if (exchange.getRequestHeaders().containsKey("FirstName")) {
                                if (!exchange.getRequestHeaders().get("FirstName").get(0).isEmpty()) {
                                    firstName = exchange.getRequestHeaders().get("FirstName").get(0);
                                }
                            }
                            if (exchange.getRequestHeaders().containsKey("LastName")) {
                                if (!exchange.getRequestHeaders().get("LastName").get(0).isEmpty()) {
                                    lastName = exchange.getRequestHeaders().get("LastName").get(0);
                                }
                            }
                            if (exchange.getRequestHeaders().containsKey("Age")) {
                                if (!exchange.getRequestHeaders().get("Age").get(0).isEmpty()) {
                                    if (Utils.isNumber(exchange.getRequestHeaders().get("Age").get(0))) {
                                        age = Integer.parseInt(exchange.getRequestHeaders().get("Age").get(0));
                                    } else {
                                        String val = "Age is not a number!";
                                        exchange.sendResponseHeaders(404, val.getBytes().length);
                                        OutputStream output = exchange.getResponseBody();
                                        output.write(val.getBytes());
                                        output.flush();
                                        return;
                                    }
                                }
                            }
                            customer.modifyCustomerWithPatch(exchange, Integer.parseInt(splitter[2]), firstName, lastName, age);
                            String val = "Data modified!";
                            exchange.sendResponseHeaders(200, val.getBytes().length);
                            OutputStream output = exchange.getResponseBody();
                            output.write(val.getBytes());
                            output.flush();
                        } else {
                            String val = "ID not found!";
                            exchange.sendResponseHeaders(404, val.getBytes().length);
                            OutputStream output = exchange.getResponseBody();
                            output.write(val.getBytes());
                            output.flush();
                        }
                    } else {
                        String val = "ID is not a number!";
                        exchange.sendResponseHeaders(404, val.getBytes().length);
                        OutputStream output = exchange.getResponseBody();
                        output.write(val.getBytes());
                        output.flush();
                    }
                } else {
                    String firstName = null, lastName = null;
                    int age = -1;
                    if (exchange.getRequestHeaders().containsKey("FirstName")) {
                        if (!exchange.getRequestHeaders().get("FirstName").get(0).isEmpty()) {
                            firstName = exchange.getRequestHeaders().get("FirstName").get(0);
                        }
                    }
                    if (exchange.getRequestHeaders().containsKey("LastName")) {
                        if (!exchange.getRequestHeaders().get("LastName").get(0).isEmpty()) {
                            lastName = exchange.getRequestHeaders().get("LastName").get(0);
                        }
                    }
                    if (exchange.getRequestHeaders().containsKey("Age")) {
                        if (!exchange.getRequestHeaders().get("Age").get(0).isEmpty()) {
                            if (Utils.isNumber(exchange.getRequestHeaders().get("Age").get(0))) {
                                age = Integer.parseInt(exchange.getRequestHeaders().get("Age").get(0));
                            } else {
                                String val = "Age is not a number!";
                                exchange.sendResponseHeaders(404, val.getBytes().length);
                                OutputStream output = exchange.getResponseBody();
                                output.write(val.getBytes());
                                output.flush();
                                return;
                            }
                        }
                    }
                    customer.modifyCustomerWithPatchForAll(exchange, firstName, lastName, age);

                    String val = "Datas modified!";
                    exchange.sendResponseHeaders(405, val.getBytes().length);
                    OutputStream output = exchange.getResponseBody();
                    output.write(val.getBytes());
                    output.flush();
                }
            } else {
                exchange.sendResponseHeaders(405, -1);// 405 Method Not Allowed
            }
            exchange.close();
        }));
        server.setExecutor(null);
        server.start();
    }
}
