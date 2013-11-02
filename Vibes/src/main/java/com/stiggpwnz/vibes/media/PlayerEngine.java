package com.stiggpwnz.vibes.media;

public class PlayerEngine {

    private final Player[] players;
    private Playlist playlist;
    private int currentPlayer;

    public PlayerEngine(int playerCount) {
        players = new Player[playerCount];
        for (int i = 0; i < playerCount; i++) {
            players[i] = new Player();
        }
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public void play() {
        for (Player player : players) {
        }
    }

    public void pause() {

    }

    public void next() {

    }

    public void prev() {

    }
}
