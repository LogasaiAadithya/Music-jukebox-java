package org.example;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Jukebox {
    private static String password;
    private static String loggedInUserId;
    private List<Song> catalog = new ArrayList<>();
    private static User loggedInUser;
    private static String userId;

    public void registerUser(String userId, String password) {
        //String passwordHash = hashPassword(password);

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jukebox", "root", "Logarox_10")) {
            String insertQuery = "INSERT INTO User (userId, password) VALUES (?, ?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
            insertStatement.setString(1, userId);
            insertStatement.setString(2, password);
            insertStatement.executeUpdate();
            insertStatement.close();
            System.out.println("User registered successfully.");
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
        }
    }

    public boolean authenticateUser(String userId, String password) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jukebox", "root", "Logarox_10")) {
            String query = "SELECT userId FROM User WHERE userId = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                loggedInUserId = String.valueOf(resultSet.getString("userId"));
                System.out.println("User '" + userId + "' authenticated successfully.");
                return true;
            }


        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }

        System.out.println("Authentication failed. Please check your credentials.");
        return false;
    }


    private String hashPassword(String password) {

        return password;
    }
    public static void createPlaylist(String name) {
        Playlist playlist = new Playlist(name);

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jukebox", "root", "Logarox_10")) {
            String insertQuery = "INSERT INTO Playlists (playlist_name, user_id) VALUES (?, ?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
            insertStatement.setString(1, name);
            insertStatement.setString(2, loggedInUserId);
            insertStatement.executeUpdate();
            insertStatement.close();
        } catch (SQLException e) {
            System.err.println("Error inserting playlist into the database: " + e.getMessage());
        }
    }


    public void displayPlaylists(String userId) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jukebox", "root", "Logarox_10")) {
            String selectQuery = "SELECT playlist_name FROM Playlists WHERE user_id = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
            System.out.println(userId);
            selectStatement.setString(1,userId);
            ResultSet resultSet = selectStatement.executeQuery();

            System.out.println("Playlists:");
            int playlistNumber = 1;
            while (resultSet.next()) {
                String playlistName = resultSet.getString("playlist_name");
                System.out.println(playlistNumber + ". " + playlistName);
                playlistNumber++;
            }
        } catch (SQLException e) {
            System.err.println("Error displaying playlists: " + e.getMessage());
        }
    }

    public void addSong(Song song) {
        //String basePath = "C:/Users/logas/Music";
        //String filePath = basePath + song.getTitle() + ".wav";
       // song.setFilePath(filePath);
        catalog.add(song);
    }

    public void displayCatalog() {
        System.out.println("Catalog:");
        System.out.println("+----------------------+----------------------+---------------------+");
        System.out.println("| Song                 | Artist               | Genre               |");
        System.out.println("+----------------------+----------------------+---------------------+");

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jukebox", "root", "Logarox_10")) {
            String query = "SELECT s.title, artist, genre FROM Songs s";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String title = resultSet.getString("title");
                String artist = resultSet.getString("artist");
                String genre = resultSet.getString("genre");

                System.out.printf("| %-20s | %-20s | %-20s |\n", title, artist, genre);
            }

            System.out.println("+----------------------+----------------------+---------------------+");
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Error displaying catalog: " + e.getMessage());
        }
    }

    public void playSong(String songTitle, Scanner scanner) {
        Song song = findSongByTitle(songTitle);
        System.out.println("Found song "+song.getTitle());


        if (song != null) {

            System.out.println("Now playing: " + song.getTitle());

            try {
                File audioFile = new File(song.getFilePath());
                System.out.println(audioFile);
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();


                boolean isPlaying = true;
                boolean isPaused = false;

                while (isPlaying) {
                    System.out.println("------------------");
                    System.out.println("|    |   Options: |");
                    System.out.println("-------------------");
                    System.out.println("|1.  |     Pause  |");
                    System.out.println("|2.  |     Resume |");
                    System.out.println("|3.  |     Stop   |");
                    System.out.println("|4.  |     Exit   |");
                    System.out.println("-------------------");

                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    switch (choice) {
                        case 1:
                            if (!isPaused) {
                                clip.stop();
                                isPaused = true;
                                System.out.println("Song paused.");
                            }
                            break;
                        case 2:
                            if (isPaused) {
                                clip.start();
                                isPaused = false;
                                System.out.println("Song resumed.");
                            }
                            break;
                        case 3:
                            clip.stop();
                            isPlaying = false;
                            System.out.println("Song stopped.");
                            break;
                        case 4:
                            clip.stop();
                            isPlaying = false;
                            break;
                        default:
                            System.out.println("Invalid option.");
                            break;
                    }
                }

                System.out.println("Song playback finished.");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error playing the song.");
            }
        } else {
            System.out.println("Song not found.");
        }
    }
    public void playPlaylist(String playlistName, Scanner scanner) {
        try {
            String playlistId = findPlaylistIdByName(playlistName);
            if (playlistId == null) {
                System.out.println("Playlist not found.");
                return;
            }

            String query = "SELECT * FROM songplaylist WHERE playlistId = ?";
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jukebox", "root", "Logarox_10");
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, playlistId);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<Song> playlistSongs = new ArrayList<>();

            while (resultSet.next()) {
                String songTitle = resultSet.getString("title");
                String artist = resultSet.getString("artist");
                String album = resultSet.getString("album");
                String genre = resultSet.getString("genre");
                String filePath = resultSet.getString("filePath");
                Song song = new Song(songTitle, artist, album, genre, filePath);
                playlistSongs.add(song);
            }

            if (!playlistSongs.isEmpty()) {
                System.out.println("Now playing playlist: " + playlistName);

                for (Song song : playlistSongs) {
                    playSongFromPlaylist(song, scanner);
                    System.out.println("Options:");
                    System.out.println("1. Next Song");
                    System.out.println("2. Stop Playlist");

                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    if (choice == 2) {
                        break;
                    }
                }

                System.out.println("Playlist playback finished.");
            } else {
                System.out.println("Playlist is empty.");
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            System.err.println("Error playing playlist from the database: " + e.getMessage());
        }
    }

    public void playSongFromPlaylist(Song song, Scanner scanner) {
        System.out.println("Now playing: " + song.getTitle());
        try {
            File audioFile = new File(song.getFilePath());
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();

            boolean isPlaying = true;
            boolean isPaused = false;

            while (isPlaying) {
                System.out.println("------------------");
                System.out.println("|    |   Options: |");
                System.out.println("-------------------");
                System.out.println("|1.  |     Pause  |");
                System.out.println("|2.  |     Resume |");
                System.out.println("|3.  |     Stop   |");
                System.out.println("|4.  |     Exit   |");
                System.out.println("-------------------");

                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        if (!isPaused) {
                            clip.stop();
                            isPaused = true;
                            System.out.println("Song paused.");
                        }
                        break;
                    case 2:
                        if (isPaused) {
                            clip.start();
                            isPaused = false;
                            System.out.println("Song resumed.");
                        }
                        break;
                    case 3:
                        clip.stop();
                        isPlaying = false;
                        System.out.println("Song stopped.");
                        break;
                    case 4:
                        clip.stop();
                        isPlaying = false;
                        break;
                    default:
                        System.out.println("Invalid option.");
                        break;
                }
            }

            System.out.println("Song playback finished.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error playing the song.");
        }
    }

    private String findPlaylistIdByName(String playlistName) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jukebox", "root", "Logarox_10");
            String query = "SELECT playlist_name FROM Playlists WHERE playlist_name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, playlistName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("playlist_name");
            }
        } catch (SQLException e) {
            System.err.println("Error finding playlist ID: " + e.getMessage());
        }
        return null;
    }

    private Song findSongByTitle(String title) {
        for (Song song : catalog) {
            if (song.getTitle().equalsIgnoreCase(title)) {
                return song;
            }
        }
        return null;
    }


    public void addSongToPlaylist(String playlistName, String songTitle) {
        String playlistId = findPlaylistIdByName(playlistName);
        if (playlistId == null) {
            System.out.println("Playlist not found.");
            return;
        }

        Song song = findSongByTitle(songTitle);
        if (song == null) {
            System.out.println("Song not found.");
            return;
        }

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jukebox", "root", "Logarox_10")) {
            System.out.println("Connection successful");
            String insertQuery = "INSERT INTO songplaylist VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
            insertStatement.setString(1, playlistId);
            insertStatement.setString(2, song.getTitle());
            insertStatement.setString(3, song.getArtist());
            insertStatement.setString(4, song.getAlbum());
            insertStatement.setString(5,song.getGenre());
            insertStatement.setString(6, song.getFilePath());
            insertStatement.executeUpdate();
            insertStatement.close();
            System.out.println("Song added to the playlist successfully.");
        } catch (SQLException e) {
            System.err.println("Error adding song to the playlist: " + e.getMessage());
        }
    }
    public void searchSong(String searchQuery, Scanner scanner) {
        boolean found = false;

        System.out.println("Search results for: " + searchQuery);

        for (Song song : catalog) {
            if (song.getTitle().equalsIgnoreCase(searchQuery)) {
                found = true;
                System.out.println("Song: " + song.getTitle()+ "  available");
                System.out.println("Do you want to play this song? (yes/no)");
                String playChoice = scanner.nextLine();

                if (playChoice.equalsIgnoreCase("yes")) {
                    playSong(song.getTitle(), scanner);
                }
                break;
            }
        }

        if (!found) {
            System.out.println("Song not found.");
        }
    }

    private static String hidePasswordInput() {
        Console console = System.console();

        if (console != null) {
            char[] passwordChars = console.readPassword("Enter your password: ");
            return new String(passwordChars);
        } else {
            return new Scanner(System.in).nextLine();
        }
    }

    public void deletePlaylist(String playlistId) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jukebox", "root", "Logarox_10")) {

            String deleteSongPlaylistQuery = "DELETE FROM songplaylist WHERE playlistId = ?";
            PreparedStatement deleteSongPlaylistStatement = connection.prepareStatement(deleteSongPlaylistQuery);
            deleteSongPlaylistStatement.setString(1, playlistId);
            deleteSongPlaylistStatement.executeUpdate();

            String deleteQuery = "DELETE FROM Playlists WHERE playlist_name = ?";
            PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
            deleteStatement.setString(1, playlistId);
            int rowsAffected = deleteStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Playlist deleted successfully.");
            } else {
                System.out.println("Playlist not found or couldn't be deleted.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting playlist: " + e.getMessage());
        }
    }



    public static void main(String[] args) throws IOException {
        Jukebox jukebox = new Jukebox();
        Scanner scanner = new Scanner(System.in);

        final String DB_URL = "jdbc:mysql://localhost:3306/jukebox";
        final String DB_USER = "root";
        final String DB_PASSWORD ="Logarox_10";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jukebox", "root", "Logarox_10")) {
            loadSongsFromDatabase(connection, jukebox);
            System.out.println("song import complete");
        } catch (SQLException e) {
            System.err.println("Database connection error!");
            e.printStackTrace();
            return;
        }

        boolean loggedIn = false;
        loggedInUser = new User(userId, password);
        loggedInUser.setUserId(userId);

        while (!loggedIn) {
            System.out.println("Choose an option:");
            System.out.println("------------------");
            System.out.println("|1.   | Login    |");
            System.out.println("|2.   | Register |");
            System.out.println("|3.   | Exit     |");
            System.out.println("------------------");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("Enter your user ID:");
                    String userId = scanner.nextLine();
                    System.out.println("Enter your password:");
                    String password = scanner.nextLine();

                    loggedIn = jukebox.authenticateUser(userId, password);
                    break;

                case 2:
                    System.out.println("Enter a new user ID:");
                    String newUserId = scanner.nextLine();
                    System.out.println("Enter a password:");
                    String newPassword = hidePasswordInput();

                    jukebox.registerUser(newUserId, newPassword);
                    break;

                case 3:
                    System.out.println("Exiting...");
                    scanner.close();
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jukebox","root", "Logarox_10")) {
            loadSongsFromDatabase(connection, jukebox);
        } catch (SQLException e) {
            System.err.println("Database connection error!");
            e.printStackTrace();
            return;
        }

        while (true) {
            System.out.println("Choose an option:");
            System.out.println("-------------------------------");
            System.out.println("s.no   |  options             |");
            System.out.println("-------------------------------");
            System.out.println("  1.   |  Play Song           |");
            System.out.println("  2.   |  Create Playlist     |");
            System.out.println("  3.   |  Display Playlists   |");
            System.out.println("  4.   |  Display Catalog     |");
            System.out.println("  5.   |  Add Song to Playlist|");
            System.out.println("  6.   |  Play playlist       |");
            System.out.println("  7.   |  Search              |");
            System.out.println("  8.   |  Delete              |");
            System.out.println("  9.   |  Exit                |");
            System.out.println("-------------------------------");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    jukebox.displayCatalog();

                    System.out.println("Enter the song title to play:");
                    String songTitle = scanner.nextLine();
                    jukebox.playSong(songTitle, scanner);
                    break;

                case 2:
                    System.out.println("Enter the playlist name:");
                    String playlistName = scanner.nextLine();
                    createPlaylist(playlistName);
                    break;

                case 3:
                    jukebox.displayPlaylists(loggedInUserId);
                    break;

                case 4:
                    jukebox.displayCatalog();
                    break;

                case 5:
                    System.out.println("Available Playlists:");
                    jukebox.displayPlaylists(loggedInUserId);

                    System.out.println("Song Catalog:");
                    jukebox.displayCatalog();
                    System.out.println("Enter the playlist name:");
                    String playlistToAdd = scanner.nextLine();
                    System.out.println("Enter the song title:");
                    String songToAdd = scanner.nextLine();
                    jukebox.addSongToPlaylist(playlistToAdd, songToAdd);

                    break;

                case 6:
                    System.out.println("Enter the playlist name to play:");
                    String playlistToPlay = scanner.nextLine();
                    jukebox.playPlaylist(playlistToPlay, scanner);
                    break;

                case 7:
                    System.out.println("Enter the song title to search:");
                    String searchQuery = scanner.nextLine();
                    jukebox.searchSong(searchQuery, scanner);

                    break;
                case 8:
                    System.out.println("Enter the playlist name to delete:");
                    Scanner sc=new Scanner(System.in);
                    String deletePlaylistName = sc.nextLine();
                    String playlistId = String.valueOf(jukebox.findPlaylistIdByName(deletePlaylistName));
                    System.out.println("playlist id="+playlistId);
                    if (playlistId != null) {
                        jukebox.deletePlaylist(playlistId);
                    } else {
                        System.out.println("Playlist not found.");
                    }
                    break;

                case 9:
                    System.out.println("Exiting...");
                    scanner.close();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private int getPlaylistIdByName(String deletePlaylistName) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jukebox", "root", "Logarox_10")) {
            String selectQuery = "SELECT playlist_id FROM Playlists WHERE playlist_name = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
            selectStatement.setString(1, deletePlaylistName);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("playlist_id");
            }
        } catch (SQLException e) {
            System.err.println("Error getting playlist ID: " + e.getMessage());
        }
        return -1;
    }

    private static void loadSongsFromDatabase(Connection connection, Jukebox jukebox) throws SQLException {
        String query = "SELECT * FROM songs";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                String artist = resultSet.getString("artist");
                String album = resultSet.getString("album");
                String genre = resultSet.getString("genre");
                String filePath = resultSet.getString("filepath");
                Song song = new Song(title, artist, album, genre, filePath);

                jukebox.addSong(song);
            }
        }
    }
}