package fi.jamk.l3329.stockpile;

import android.net.Uri;

//import com.google.android.gms.maps.model.LatLng;

/**
 * Created by peter on 6.11.2017.
 */

public class Item {

    private int id;
    private String name;
    private float price;
    private float amount;
    private String units;
//    private LatLng place;
    private Uri picture;
    private boolean isBought;
    private String currency;

    private Item(ItemBuilder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.price = builder.price;
        this.amount = builder.amount;
        this.units = builder.units;
//        this.place = builder.place;
        this.picture = builder.picture;
        this.isBought = builder.isBought;
        this.currency = builder.currency;
    }

    public void setBought(boolean bought) {
        isBought = bought;
    }

    public int getId() {
        return id;
    }

    public String getUnits() {
        return units;
    }

    public boolean isBought() {
        return isBought;
    }

    public String getName() {
        return name;
    }

    public float getPrice() {
        return price;
    }

    public float getAmount() {
        return amount;
    }

//    public LatLng getPlace() {
//        return place;
//    }

    public Uri getPicture() {
        return picture;
    }

    public String getCurrency() {
        return currency;
    }

    public static class ItemBuilder {

        private int id;
        private String name;
        private float price;
        private float amount;
        private String units;
//        private LatLng place;
        private Uri picture;
        private boolean isBought;
        private String currency;

        public ItemBuilder id(int id) {
            this.id = id;
            return this;
        }

        public ItemBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public ItemBuilder units(String units) {
            this.units = units;
            return this;
        }

        public ItemBuilder isBought(boolean bought) {
            isBought = bought;
            return this;
        }

        public Item build(){
            return new Item(this);
        }

        public ItemBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ItemBuilder price(float price) {
            this.price = price;
            return this;
        }

        public ItemBuilder amount(float amount) {
            this.amount = amount;
            return this;
        }

//        public ItemBuilder place(LatLng place) {
//            this.place = place;
//            return this;
//        }

        public ItemBuilder picture(Uri picture) {
            this.picture = picture;
            return this;
        }
    }
}
