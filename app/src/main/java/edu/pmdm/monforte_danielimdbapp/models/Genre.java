package edu.pmdm.monforte_danielimdbapp.models;

public class Genre {
    private String id; //Id del género, usado para trabajar con la API de TMDB filtrando las películas por su género
    private String name; //Nombre del género, usado para mostrar al usuario los diferentes géneros que hay

    public Genre(String id,String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return name; //Al imprimir un género, imprimiremos su nombre
    }
}
