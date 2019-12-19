package lk.ijse.dep.pos.service;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(urlPatterns = "/api/v1/order")
public class OrderServlet extends HttpServlet {

    private Connection getConnection(){
        try {
            return ((BasicDataSource)getServletContext().getAttribute("pool")).getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Connection connection = getConnection();

        try{
            JsonReader reader = Json.createReader(req.getReader());
            JsonObject orderObject = reader.readObject();
            //start transaction
            connection.setAutoCommit(false);
            //save order
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO orders values(?,?,?)");
            String orderId = null;
            try {
                orderId=orderObject.getString("orderId");
                pstm.setObject(1,orderId);
                pstm.setObject(2,orderObject.getString("date"));
                pstm.setObject(3,orderObject.getString("customerId"));

            }catch (NullPointerException e){
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
            System.out.println(orderId);
            boolean result1 = pstm.executeUpdate()>0;
            if (!result1){
                connection.rollback();
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                throw new RuntimeException("Something went wrong.");
            }
            System.out.println("Order inserted");
            //save orderDetails
            PreparedStatement pstm2 = connection.prepareStatement("INSERT INTO orderDetails values (?,?,?,?)");
            PreparedStatement pstm3 = connection.prepareStatement("UPDATE item SET qty=? WHERE code=?");
            PreparedStatement pstmGetQty = connection.prepareStatement("SELECT qty FROM item WHERE code=?");

            try {
            JsonArray ordrDetails =  orderObject.getJsonArray("orderDetails");
                System.out.println("OrderDetails array : " + ordrDetails);

                for (JsonValue ordrDetail : ordrDetails) {
                    System.out.println("inside orderDetail object");
                    System.out.println("Order ID : "+orderId);
                    JsonObject jsonObject = ordrDetail.asJsonObject();
                    pstm2.setObject(1,orderId);
                    pstm2.setObject(2,jsonObject.getString("itemCode"));
                    pstm2.setObject(3,jsonObject.getInt("unitPrice"));
                    pstm2.setObject(4,jsonObject.getInt("qty"));


                    //update qty

//                    ItemServlet itemServlet = (ItemServlet) getServletConfig().getServletContext().getAttribute("ItemServlet");
//                    resp.sendRedirect(req.getContextPath()+"/api/v1/item");
//                    Update item set qtyOnHand=(select qtyOnHand from item where itemCode="I001") - 9 where itemCode="I001";

                    pstmGetQty.setObject(1,jsonObject.getString("itemCode"));
                    ResultSet rst = pstmGetQty.executeQuery();
                    int qtyOnHand=0;
                    if (rst.next()){
                        qtyOnHand=rst.getInt(1);
                    }
                    System.out.println("Qty on hand : "+qtyOnHand);
                    int newqty =  qtyOnHand-jsonObject.getInt("qty");
                    pstm3.setObject(1,newqty);
                    pstm3.setObject(2,jsonObject.getString("itemCode"));

                    boolean result = pstm3.executeUpdate()>0;
                    if (!result){
                        connection.rollback();
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    }
                    System.out.println("Item updated");
                }
            }catch (NullPointerException e){
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
            boolean result2 = pstm2.executeUpdate()>0;
            if (!result2){
                connection.rollback();
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
            System.out.println("Order details inserted");

        }catch (Exception e){
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }finally {
            try {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }
}
