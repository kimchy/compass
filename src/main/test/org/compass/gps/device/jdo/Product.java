/*
 * Copyright 2004-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.gps.device.jdo;

/**
 * Definition of a Product Represents a product, and contains the key aspects of
 * the item.
 * 
 */
public class Product {

    private Integer id;

    /**
     * Name of the Product.
     */
    protected String name = null;

    /**
     * Description of the Product.
     */
    protected String description = null;

    /**
     * Value of the Product.
     */
    protected double price = 0.0;

    /**
     * Default constructor.
     */
    protected Product() {
    }

    /**
     * Constructor.
     * 
     * @param name
     *            name of product
     * @param description
     *            description of product
     * @param price
     *            Price
     */
    public Product(String name, String description, double price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    // ------------------------------- Accessors -------------------------------
    /**
     * Accessor for the name of the product.
     * 
     * @return Name of the product.
     */
    public String getName() {
        return name;
    }

    /**
     * Accessor for the description of the product.
     * 
     * @return Description of the product.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Accessor for the price of the product.
     * 
     * @return Price of the product.
     */
    public double getPrice() {
        return price;
    }

    // ------------------------------- Mutators --------------------------------
    /**
     * Mutator for the name of the product.
     * 
     * @param name
     *            Name of the product.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Mutator for the description of the product.
     * 
     * @param description
     *            Description of the product.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Mutator for the price of the product.
     * 
     * @param price
     *            price of the product.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
