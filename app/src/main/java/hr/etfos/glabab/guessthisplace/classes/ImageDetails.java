package hr.etfos.glabab.guessthisplace.classes;

import com.google.android.gms.maps.model.LatLng;

public class ImageDetails {
    String user;
    LatLng coordinates;
    int score;

    public ImageDetails(String user, LatLng coordinates, int score){
        this.user = user;
        this.coordinates = coordinates;
        this.score = score;
    }

    public ImageDetails(){
        this.user = "";
        this.coordinates = new LatLng(0,0);
        this.score = 0;
    }

    public String getUser() {return user;}

    public LatLng getCoordinates() {return coordinates;}

    public int getScore() {return score;}

    public void setUser(String user){ this.user = user;}

    public void setCoordinates(LatLng coordinates){ this.coordinates = coordinates; }

    public void setScore(int score){ this.score = score; }

    public void setImageDetails(String user, LatLng coordinates, int score) {
        this.user = user;
        this.coordinates = coordinates;
        this.score = score;
    }
}
