package com.example.dascafe.utils;

import com.example.dascafe.model.Product;
import com.example.dascafe.model.CartItem;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartList;

    private CartManager() {
        cartList = new ArrayList<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(Product product) {
        for (CartItem item : cartList) {
            if (item.product.id == product.id) {
                item.quantity++;
                return;
            }
        }
        cartList.add(new CartItem(product, 1));
    }

    public List<CartItem> getCart() {
        return cartList;
    }

    public void clearCart() {
        cartList.clear();
    }

    public double getTotal() {
        double total = 0;
        for (CartItem item : cartList) {
            total += item.getSubtotal();
        }
        return total;
    }
}
