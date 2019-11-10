package com.example.songstudio;

import android.os.Parcel;
import android.os.Parcelable;

//here we have implemented Parcelable interface so that we can pass this object to another activity via intents
public class Song_details implements Parcelable {
    public String name;
    public String url;
    public String artists;
    public String cover_image;

    public Song_details(String name, String url, String artists, String cover_image) {
        this.name = name;
        this.url = url;
        this.artists = artists;
        this.cover_image = cover_image;
    }

    public Song_details(Parcel in){
        String[] data= new String[4];

        in.readStringArray(data);
        this.name= data[0];
        this.url= data[1];
        this.artists= data[2];
        this.cover_image = data[3];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{this.name,this.url,this.artists, this.cover_image});
    }
    public static final Parcelable.Creator<Song_details> CREATOR= new Parcelable.Creator<Song_details>() {

        @Override
        public Song_details createFromParcel(Parcel source) {
// TODO Auto-generated method stub
            return new Song_details(source);  //using parcelable constructor
        }

        @Override
        public Song_details[] newArray(int size) {
// TODO Auto-generated method stub
            return new Song_details[size];
        }
    };
}
