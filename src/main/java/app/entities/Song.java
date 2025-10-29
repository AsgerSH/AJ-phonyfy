package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity

@Table(name = "song")
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "song_id", nullable = false, unique = true)
    private Integer songId;

    @Column(name = "song_name", nullable = false)
    private String songName;

    @Column(name = "genre", nullable = false)
    private String genre;

    @Column(name = "featured_artist", nullable = true, length = 100)
    private String featuredArtist;

    @Column(name = "duration", nullable = false)
    private String duration = "0:00";


    // Relations
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "main_artist_id",
            nullable = false,
    foreignKey = @ForeignKey(name = "fk_song_main_artist"))
    private Artist mainArtist;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "album_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_song_album"))
    private Album album;

    @ManyToMany(mappedBy = "songs")
    private Set<Playlist> playlists = new HashSet<>();


    // Convert "mm:ss" → total seconds
    public int getDurationSeconds() {
        String[] parts = duration.split(":");
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);
        return minutes * 60 + seconds;
    }

    // Set duration from seconds
    public void setDurationFromSeconds(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        this.duration = String.format("%d:%02d", minutes, seconds);
    }
}
