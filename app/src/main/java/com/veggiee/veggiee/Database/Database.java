package com.veggiee.veggiee.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import android.widget.Toast;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import com.veggiee.veggiee.Model.Order;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteAssetHelper {
    private static final String DB_name="veggieeDb.db";
    private static final int DB_VER=1;
    public Database(Context context) {
        super(context, DB_name, null , DB_VER);
    }


    public List<Order> getCartItems()
    {
        SQLiteDatabase database= getReadableDatabase();
        SQLiteQueryBuilder queryBuilder=new SQLiteQueryBuilder();

        String[] sqlSelect={"ProductName","ProductId","Quantity","Price","Discount"};
        String sqlTable="OrderDetail";

        queryBuilder.setTables(sqlTable);
        Cursor c=queryBuilder.query(database,sqlSelect,null,null,null,null,null,null);

        List<Order> result=new ArrayList<>();

        if(c.moveToFirst())
        {
            do{
                result.add(
                        new Order(c.getString(c.getColumnIndex("ProductId")),
                        c.getString(c.getColumnIndex("ProductName")),
                        c.getString(c.getColumnIndex("Quantity")),
                        c.getString(c.getColumnIndex("Price")),
                        c.getString(c.getColumnIndex("Discount"))
                        )
                );
            }while (c.moveToNext());
        }

        return result;
    }

    public void addToCart(Order order)
    {
        SQLiteDatabase database=getReadableDatabase();
        SQLiteQueryBuilder queryBuilder=new SQLiteQueryBuilder();

        String[] sqlSelect={"ProductId","Quantity", "Price"};
        String whereClause = "ProductId = '" + order.getProductId()+"'";
        String sqlTable="OrderDetail";
        queryBuilder.setTables(sqlTable);
        Cursor cur = queryBuilder.query(database,sqlSelect,whereClause,null,null,null,null,null);

        if(!(cur.moveToFirst()) || cur.getCount() == 0){

            String query=String.format("INSERT INTO OrderDetail(ProductId,ProductName,Quantity,Price,Discount) " +
                    "VALUES ('"+order.getProductId()+"','"+order.getProductName()+"','"+order.getQuantity()+"','"+order.getPrice()+"','"+order.getDiscount()+"');");

            database.execSQL(query);
        }
        else{

            String count=cur.getString(cur.getColumnIndex("Quantity"));
            String price=cur.getString(cur.getColumnIndex("Price"));

            int previousPrice=Integer.parseInt(price);
            int previousQuantity=Integer.parseInt(count);

            int perItemPrice = previousPrice/previousQuantity;

            int newQuantity=Integer.parseInt(order.getQuantity());
            int finalQuantity= previousQuantity+newQuantity;

            String finalPrice=String.valueOf(perItemPrice*finalQuantity);

            String query="UPDATE OrderDetail SET Quantity='"+String.valueOf(finalQuantity)+"', Price='"+finalPrice+"' WHERE ProductId='"+order.getProductId()+"'";
            database.execSQL(query);
        }

    }

    public void cleanCart()
    {
        SQLiteDatabase database=getReadableDatabase();
        String query= "DELETE FROM OrderDetail";
        database.execSQL(query);
    }

    public void updateCartItem(String productId, int newQuantity, String price) {

        SQLiteDatabase database=getReadableDatabase();
        String query="UPDATE OrderDetail SET Quantity='"+newQuantity+"',Price='"+price+"' WHERE ProductId='"+productId+"'";
        database.execSQL(query);
    }

    public void deleteItem(String productId) {

        SQLiteDatabase database=getReadableDatabase();
        String query="DELETE FROM OrderDetail WHERE ProductId='"+productId+"'";
        database.execSQL(query);
    }
}
