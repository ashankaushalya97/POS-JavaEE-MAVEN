package lk.ijse.dep.pos.service;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

@WebServlet(urlPatterns = "/api/v1/item")
public class ItemServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private Connection getConnection()  {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/sample2","root","mysql");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection connection = getConnection();

        try {
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM item");
            ResultSet rst = pstm.executeQuery();
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            while (rst.next()){
                JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
                objectBuilder.add("id",rst.getString(1));
                objectBuilder.add("description",rst.getString(2));
                objectBuilder.add("qty",rst.getInt(3));
                objectBuilder.add("unitPrice",rst.getString(4));
                arrayBuilder.add(objectBuilder.build());
            }
            JsonArray items = arrayBuilder.build();
            resp.setHeader("Content-Type","application/json");
            resp.setIntHeader("X-count",items.size());
            resp.getWriter().println(items.toString());

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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection connection = getConnection();
        try {
            JsonReader reader = Json.createReader(req.getReader());
            JsonObject item = reader.readObject();
            PreparedStatement pstm = connection.prepareStatement("Insert into item values(?,?,?,?)");

            try {
                pstm.setObject(1,item.getString("code"));
                pstm.setObject(2,item.getString("description"));
                pstm.setObject(3,item.getString("qty"));
                pstm.setObject(4,item.getString("unitPrice"));
            }catch (NullPointerException e){
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
            pstm.executeUpdate();
            resp.setStatus(HttpServletResponse.SC_CREATED);
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
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection connection = getConnection();
        try {
            JsonReader reader = Json.createReader(req.getReader());
            JsonObject item = reader.readObject();
            PreparedStatement pstm = connection.prepareStatement("UPDATE item SET description=?, qty=?, unit_price=? WHERE code=?");
            try {
                pstm.setObject(1,item.getString("description"));
                pstm.setObject(2,Integer.parseInt(item.getString("qty")));
                pstm.setObject(3,Integer.parseInt(item.getString("unitPrice")));
                pstm.setObject(4,item.getString("code"));
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
        String itemCode = req.getParameter("itemCode");
        if(itemCode.trim() == null || itemCode.trim().length()==0){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Connection connection = getConnection();
        try {
            PreparedStatement pstm = connection.prepareStatement("DELETE FROM item WHERE code=?");
            pstm.setObject(1,itemCode);
            boolean result = pstm.executeUpdate()>0;
            if (result){
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
