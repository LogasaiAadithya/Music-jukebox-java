package org.example;

public class Song
{
    private String title;
    private String artist;
    private String album;
    private String genre;
    private String filePath;
    private int id;

    public String getTitle() {
        return title;
    }
    public String getArtist() {
        return artist;
    }
    public String getAlbum()
    {
        return album;
    }
    public String getGenre()
    {
        return genre;
    }
    public String getFilePath()
    {
        return filePath;
    }
    public void setFilePath(String filePath)
    {
        filePath.replace("/", "\\");
    }
    public int getId() {
        return id;
    }
    Song(String title,String artist,String album,String genre,String filePath)
    {
        this.title = title;
        this.artist = artist;
        this.album=album;
        this.genre=genre;
        this.filePath=filePath;
    }
}