package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder

public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "playlist_id",unique = true, nullable = false)
    private int id;

    @Column(name = "playlist_name")
    private String playListName;

    @Column(name = "total_duration", nullable = false)
    private String totalDuration = "0:00";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_username", nullable = false,
    foreignKey = @ForeignKey(name = "fk_playlist_owner"))
    @ToString.Exclude
    private UserProfile owner;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "playlist_songs", joinColumns = {@JoinColumn(name = "playlist_id",
            referencedColumnName = "playlist_id")},
            inverseJoinColumns = {@JoinColumn(name = "song_id", referencedColumnName = "song_id")}
    )

    @Builder.Default
    private Set<Song> songs = new HashSet<>();


    public void updateTotalDuration() {
        int totalSeconds = songs.stream()
                .mapToInt(song -> {
                    String[] parts = song.getDuration().split(":");
                    int minutes = Integer.parseInt(parts[0]);
                    int seconds = Integer.parseInt(parts[1]);
                    return minutes * 60 + seconds;
                })
                .sum();

        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        this.totalDuration = String.format("%d:%02d", minutes, seconds);
    }

    @PrePersist
    @PreUpdate
    public void recalcDurationBeforeSave() {
        updateTotalDuration();
    }

}
