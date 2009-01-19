/*
 * Copyright 2004-2009 the original author or authors.
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

package org.compass.sample.library;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author kimchy
 */
public class Author implements Identifiable {

    private Long id;

    private Name name;

    private Date birthdate;

    private String[] keywords;

    private List articles = new ArrayList();

    private List books = new ArrayList();

    public Author() {

    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public List getBooks() {
        return books;
    }

    public void setBooks(List books) {
        this.books = books;
    }

    public void addBook(Book book) {
        books.add(book);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public List getArticles() {
        return articles;
    }

    public void setArticles(List publications) {
        this.articles = publications;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public String toString() {
        return name.toString();
    }

}
