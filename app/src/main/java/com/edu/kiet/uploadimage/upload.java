package com.edu.kiet.uploadimage;

public class upload {
    private String imageName;
    private String imageUrl;

    public upload(){
//empty constructor needed;

    }

    public upload(String name,String imageurl){
        if(name.trim().equals(" ")){
            name="No name";
        }

        imageName=name;
        imageUrl=imageurl;

    }

    public String getname(){
        return imageName;
    }

    public void setname(String name){
        imageName=name;
    }

    public String getImageUrl(){
        return imageUrl;
    }

    public void setImageUrl(String imageurl){
        imageUrl=imageurl;
    }


}
