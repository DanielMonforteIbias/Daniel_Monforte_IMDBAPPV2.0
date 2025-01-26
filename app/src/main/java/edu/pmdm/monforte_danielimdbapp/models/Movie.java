package edu.pmdm.monforte_danielimdbapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Movie implements Parcelable {
    private String id;
    private String titulo;
    private String portada;
    private String fecha;
    private double rating; //A pesar de llamarse rating, es el puesto que ocupan en el ranking
    private String descripcion;

    public Movie(String id, String titulo, String portada, String fecha, double rating) {
        this.id=id;
        this.titulo = titulo;
        this.portada = portada;
        this.fecha = fecha;
        this.rating = rating;
        this.descripcion="";
    }

    public Movie(String id, String titulo, String portada, String fecha, double rating,String descripcion) {
        this.id=id;
        this.titulo = titulo;
        this.portada = portada;
        this.fecha = fecha;
        this.rating = rating;
        this.descripcion=descripcion;
    }

    public Movie() {
        id = "";
        titulo="";
        portada="";
        fecha="";
        rating=0;
        descripcion="";
    }

    protected Movie(Parcel in) {
        id = in.readString();
        titulo = in.readString();
        portada = in.readString();
        fecha = in.readString();
        rating = in.readDouble();
        descripcion = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getPortada() {
        return portada;
    }

    public void setPortada(String portada) {
        this.portada = portada;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(titulo);
        dest.writeString(portada);
        dest.writeString(fecha);
        dest.writeDouble(rating);
        dest.writeString(descripcion);
    }

    @Override
    public String toString() {
        return "{" +
                "id='" + id + '\'' +
                ", titulo='" + titulo + '\'' +
                ", portada='" + portada + '\'' +
                ", fecha='" + fecha + '\'' +
                ", rating=" + rating +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
