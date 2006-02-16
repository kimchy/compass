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
 * Definition of a Book. Extends basic Product class.
 * 
 */
public class Book {

    private Integer id;

    /**
     * Default Constructor.
     */
    protected Book() {
        super();
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
     * @param author
     *            Author of the book
     * @param isbn
     *            ISBN number of the book
     * @param publisher
     *            Name of publisher of the book
     */
    public Book(String name, String description, double price, String author, String isbn, String publisher) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.author = author;
        this.isbn = isbn;
        this.publisher = publisher;
    }

    protected String name = null;

    protected String description = null;

    protected double price = 0.0;

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

    /**
     * Author of the Book.
     */
    protected String author = null;

    /**
     * ISBN number of the book.
     */
    protected String isbn = null;

    /**
     * Publisher of the Book.
     */
    protected String publisher = null;

    // ------------------------------- Accessors -------------------------------
    /**
     * Accessor for the author of the book.
     * 
     * @return Author of the book.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Accessor for the isbn of the book.
     * 
     * @return ISBN of the book.
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * Accessor for the publisher of the book.
     * 
     * @return Publisher of the book.
     */
    public String getPublisher() {
        return publisher;
    }

    // ------------------------------- Mutators --------------------------------
    /**
     * Mutator for the author of the book.
     * 
     * @param author
     *            Author of the book.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Mutator for the ISBN of the book.
     * 
     * @param isbn
     *            ISBN of the book.
     */
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    /**
     * Mutator for the publisher of the book.
     * 
     * @param publisher
     *            Publisher of the book.
     */
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
