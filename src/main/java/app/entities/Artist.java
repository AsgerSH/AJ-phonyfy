package app.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artist_id")
    private int id;

    @Column(name = "artist_name", length = 100, nullable = false)
    private String artistName;

    private String type;

    @OneToMany(mappedBy = "mainArtist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<Song> songSet = new HashSet<>();

    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<Album> albumSet = new HashSet<>();

    public void addSong(Song s) {
        songSet.add(s);
        s.setMainArtist(this);
    }

    public void addAlbum(Album a) {
        albumSet.add(a);
        a.setArtist(this);
    }
}

