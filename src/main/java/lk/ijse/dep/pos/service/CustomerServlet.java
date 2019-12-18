package lk.ijse.dep.pos.service;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

@WebServlet(urlPatterns = "/api/v1/customer")
public class CustomerServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        super.init();
    }

    private Connection getconnection(){
        try {
            return  DriverManager.getConnection("jdbc:mysql://localhost:3306/sample2","root","mysql");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            Connection connection = getconnection();
        try {
            PreparedStatement pstm = connection.prepareStatement("SELECT  * FROM  customer");
            ResultSet rst = pstm.executeQuery();
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            while (rst.next()){
                JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
                objectBuilder.add("id",rst.getString(1));
                objectBuilder.add("name",rst.getString(2));
                objectBuilder.add("address",rst.getString(3));
                arrayBuilder.add(objectBuilder.build());
            }
            JsonArray customer= arrayBuilder.build();
            resp.setContentType("application/json");
            resp.setIntHeader("X-count",customer.size());
            resp.getWriter().println(customer.toString());
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection connection = getconnection();
        try {
            JsonReader reader = Json.createReader(req.getReader());
            JsonObject customer = reader.readObject();
            PreparedStatement pstm = connection.prepareStatement("Insert into customer values(?,?,?)");

            try {
                if(customer.getString("id").trim().length()==0){
                    throw new NullPointerException();
                }

                pstm.setObject(1,customer.getString("id"));
                pstm.setObject(2,customer.getString("name"));
                pstm.setObject(3,customer.getString("address"));
            }catch (NullPointerException e){
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
             pstm.executeUpdate();
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection connection = getconnection();
        try {
            JsonReader reader = Json.createReader(req.getReader());
            JsonObject customer = reader.readObject();
            PreparedStatement pstm = connection.prepareStatement("UPDATE customer set name=?,address=? WHERE  id=?");
            try {
                pstm.setObject(1,customer.getString("name"));
                pstm.setObject(2,customer.getString("address"));
                pstm.setObject(3,customer.getString("id"));
            }catch (NullPointerException e){
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
            pstm.executeUpdate();
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

        }catch (Exception e){
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection connection = getconnection();
        String customerId = req.getParameter("customerId");
        if(customerId.trim() == null || customerId.trim().length()==0){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            PreparedStatement pstm = connection.prepareStatement("DELETE From customer WHERE id=?");
            pstm.setObject(1,customerId);
            boolean result =pstm.executeUpdate()>0;
            if(result){
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }else{
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }

        }catch (Exception e){
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }
}
