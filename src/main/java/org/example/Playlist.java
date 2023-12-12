package org.example;

import org.example.Song;
import org.example.User;

import javax.sound.sampled.Clip;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Playlist {
    private String name;
    private int id;
    private List<Song> songs = new ArrayList<>();
    private User owner;

    public Playlist(String name) {
        this.name = name;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void play(Song currentSong, Clip clip, Scanner scanner) {
    }
    public int getId() {

        return id;
    }


    public void add(Playlist playlist) {
    }
}
