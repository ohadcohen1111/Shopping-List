package com.example.shoplist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddListActivity extends AppCompatActivity {
    private ArrayList<Item> items;
    private ArrayAdapter<Item> itemsAdapter;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference firebaseReference;

    private ListView listView;
    private Button button;

    private String listId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_list);

        listView = findViewById(R.id.listView);
        button = findViewById(R.id.button);

        //take the uid of the list that the user made
//        String listId = getIntent().getStringExtra("key");
       listId = getIntent().getStringExtra("key");


        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseReference = firebaseDatabase.getReference("\"shopList\"");


        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                addItem(view);
            }
        });

        items = new ArrayList<>();
        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(itemsAdapter);
        setUpListViewListener();
    }

    //remove item from the list
    private void setUpListViewListener() {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Context context = getApplicationContext();
                Toast.makeText(context, "Item Removed", Toast.LENGTH_LONG).show();
                //the item we want to remove
                Item itemRemove = items.get(i);
                items.remove(i);
                //refresh the list (the display list)
                itemsAdapter.notifyDataSetChanged();
                //remove the item from database
                firebaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<Item> itemsToUpdate = new ArrayList<>();
                        itemsToUpdate = snapshot.child(listId).getValue(ShopList.class).getItems();
                        for (int j = 0; j < itemsToUpdate.size(); j++){
                            System.out.println("itemsToUpdate at index j: " + itemsToUpdate.get(j).getName() +  " " + itemsToUpdate.get(j).getQuantity() + "      Item Name: " + itemRemove.getName() + " " + itemRemove.getQuantity());
                            if(itemsToUpdate.get(j).getName().toString() == itemRemove.getName().toString() &&
                                itemsToUpdate.get(j).getQuantity() == itemRemove.getQuantity()){
                                System.out.println("Item name: " + itemsToUpdate.get(j));
                                itemsToUpdate.remove(j);
                            }
                        }
//                        firebaseReference.child(listId).setValue(shopList);
                    }


                    @Override
                    public void onCancelled (@NonNull DatabaseError error){
                    }
                });
                return true;
            }
        });
    }

    private void addItem(View view){
        EditText input = findViewById(R.id.input);
        String itemText = input.getText().toString();
        EditText quantityItem = findViewById(R.id.quantityItems);
        int quantity = Integer.parseInt(quantityItem.getText().toString());
        Item item = new Item(itemText, quantity);



        if(!itemText.equals("")){
//            if (items.contains(item.name)){
//
//
//            }

            //add the item to the list
            itemsAdapter.add(item);

            //add the item to database
           firebaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ShopList shopList = new ShopList();
                    //get the items arraylist from the snapshot and put in the new Shoplist object
                    shopList.setItems(snapshot.child(listId).getValue(ShopList.class).getItems());
                    //get the list name from the snapshot and put in the new Shoplist object
                    shopList.setName(snapshot.child(listId).getValue(ShopList.class).getName());
                    //add the new item that the user add to the list
                    shopList.getItems().add(item);
                    //update the database with the new list
                    firebaseReference.child(listId).setValue(shopList);
                }


                    @Override
                    public void onCancelled (@NonNull DatabaseError error){
                    }
                });

            input.setText("");
            quantityItem.setText("");
        } else {
          Toast.makeText(getApplicationContext(), "Please enter text", Toast.LENGTH_LONG).show();
        }
    }
}