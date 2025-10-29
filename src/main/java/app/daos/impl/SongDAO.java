package app.daos.impl;


import app.daos.IDAO;
import app.dtos.SongDTO;
import app.entities.Album;
import app.entities.Artist;
import app.entities.Song;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SongDAO implements IDAO<SongDTO, Integer> {

    private static SongDAO instance;
    private static EntityManagerFactory emf;


    public static SongDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new SongDAO();
        }
        return instance;
    }

    @Override
    public SongDTO read(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            Song song = em.createQuery("""
                SELECT s FROM Song s
                JOIN FETCH s.mainArtist
                JOIN FETCH s.album
                WHERE s.songId = :id
                """, Song.class)
                    .setParameter("id", id)
                    .getSingleResult();

            return new SongDTO(song);
        }
    }


    @Override
    public List<SongDTO> readAll() {
        try (EntityManager em = emf.createEntityManager()) {
            List<Song> songs = em.createQuery("""
                SELECT s FROM Song s
                JOIN FETCH s.mainArtist
                JOIN FETCH s.album
                """, Song.class)
                    .getResultList();

            return songs.stream().map(SongDTO::new).toList();
        }
    }

    @Override
    public SongDTO create(SongDTO songDTO) {
        try (EntityManager em = emf.createEntityManager()) {
            // Look up by names first
            Artist artist = em.createQuery(
                            "select a from Artist a where lower(a.artistName) = lower(:n)", Artist.class)
                    .setParameter("n", songDTO.getMainArtistName().trim())
                    .getResultStream().findFirst()
                    .orElse(null);

            if (artist == null) {
                throw new IllegalArgumentException("Artist not found: " + songDTO.getMainArtistName());
            }

            Album album = em.createQuery(
                            "select al from Album al where lower(al.albumName) = lower(:n) and al.artist.id = :aid", Album.class)
                    .setParameter("n", songDTO.getAlbumName().trim())
                    .setParameter("aid", artist.getId())
                    .getResultStream().findFirst()
                    .orElse(null);

            if (album == null) {
                throw new IllegalArgumentException("Album not found for artist '" + artist.getArtistName() +
                        "': " + songDTO.getAlbumName());
            }

            em.getTransaction().begin();

            Song s = songDTO.toEntity();
            s.setSongId(null);
            s.setMainArtist(artist);
            s.setAlbum(album);

            em.persist(s);
            em.getTransaction().commit();
            return new SongDTO(s);
        }
    }


    @Override
    public SongDTO update(Integer integer, SongDTO songDTO) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Song s = em.find(Song.class, integer);
            if (s == null) throw new IllegalArgumentException("Song not found: " + integer);

            s.setSongName(songDTO.getSongName());
            s.setGenre(songDTO.getGenre());
            s.setFeaturedArtist(songDTO.getFeaturedArtist());
            if (songDTO.getDuration() != null) {
                s.setDuration(songDTO.getDuration());
            }

            // Relations, if they are provided
            if (songDTO.getMainArtistId() != null) {
                s.setMainArtist(em.getReference(Artist.class, songDTO.getMainArtistId()));
            }
            if (songDTO.getAlbumId() != null) {
                s.setAlbum(em.getReference(Album.class, songDTO.getAlbumId()));
            }

            Song mergedSong = em.merge(s);
            em.getTransaction().commit();

            return new SongDTO(mergedSong);
        }
    }

    @Override
    public void delete(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Song s = em.find(Song.class, id);
            if (s != null) {
                var playlistsCopy = new java.util.HashSet<>(s.getPlaylists());
                for (var p : playlistsCopy) {
                    p.getSongs().remove(s);
                }
                s.getPlaylists().clear();

                em.remove(s);
            }

            em.getTransaction().commit();
        }
    }

    @Override
    public boolean validatePrimaryKey(Integer integer) {
        try  (EntityManager em = emf.createEntityManager()) {
            Song s = em.find(Song.class, integer);
            return s != null;
        }
    }


}
