package app.config;

import app.entities.*;
import app.security.entities.Role;
import app.security.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Populate {

    private Populate() {
    }

    private record Track(String name, String genre, String featuredArtist, String mmss) {
    }

    // Create artist + album + all tracks in one go
    private static Album addAlbumWithTracks(EntityManager em,
                                            String artistName,
                                            String artistType,
                                            String albumName,
                                            LocalDate releaseDate,
                                            List<Track> tracks) {

        Artist artist = em.createQuery("SELECT a FROM Artist a WHERE a.artistName = :name", Artist.class)
                .setParameter("name", artistName)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (artist == null) {
            artist = Artist.builder()
                    .artistName(artistName)
                    .type(artistType)
                    .build();
            em.persist(artist);
        }

            Album album = Album.builder()
                    .albumName(albumName)
                    .releaseDate(releaseDate)
                    .artist(artist)
                    .build();
            em.persist(album);

            for (Track t : tracks) {
                Song s = new Song();
                s.setSongName(t.name());
                s.setGenre(t.genre());
                s.setFeaturedArtist(t.featuredArtist);
                s.setDuration(t.mmss());
                s.setMainArtist(artist);
                s.setAlbum(album);
                em.persist(s);
            }
            return album;
        }

// Track...arr means we can pass any number of Track arguments without creating an array ourselves.
        @SafeVarargs
        private static List<Track> tracks (Track...arr){
            List<Track> list = new ArrayList<>(arr.length);
            for (Track t : arr) list.add(t);
            return list;
        }

        private static Track track (String name, String genre, String featuredArtist, String mmss){
            return new Track(name, genre, featuredArtist, mmss);
        }

        private String noArtist = "No featured artist";

        // ------------------------------------------------

        public static void seed (EntityManagerFactory emf){
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();

            try {
                tx.begin();
                long existing = em.createQuery("select count(u) from User u", Long.class).getSingleResult();
                if (existing > 0) {
                    tx.rollback();
                    em.close();
                    return;
                }

                // Roles
                Role roleUser  = em.find(Role.class, "user");
                if (roleUser == null) {
                    roleUser = new Role("user");
                    em.persist(roleUser);
                }

                Role roleAdmin = em.find(Role.class, "admin");
                if (roleAdmin == null) {
                    roleAdmin = new Role("admin");
                    em.persist(roleAdmin);
                }

                // Users (security)
                User jonas = new User("jonas", "jonaspw");
                jonas.addRole(roleUser);
                em.persist(jonas);

                User asger = new User("asger", "asgerpw");
                asger.addRole(roleUser);
                asger.addRole(roleAdmin);
                em.persist(asger);

                // UserProfiles
                UserProfile jonasProfile = UserProfile.builder()
                        .username(jonas.getUsername())
                        .account(jonas)
                        .build();
                em.persist(jonasProfile);

                UserProfile asgerProfile = UserProfile.builder()
                        .username(asger.getUsername())
                        .account(asger)
                        .build();
                em.persist(asgerProfile);


                // Daft Punk - Discovery
                addAlbumWithTracks(em,
                        "Daft Punk", "Electronic music duo",
                        "Discovery", LocalDate.of(2001, 3, 12),
                        tracks(
                                track("One More Time",        "Electronic", "No featured artist", "5:20"),
                                track("Aerodynamic",          "Electronic", "No featured artist", "3:32"),
                                track("Digital Love",         "Electronic", "No featured artist", "5:01"),
                                track("Harder, Better, Faster, Stronger", "Electronic", "No featured artist", "3:46"),
                                track("Crescendolls",         "Electronic", "No featured artist", "3:31"),
                                track("Nightvision",          "Electronic", "No featured artist", "1:44"),
                                track("Superheroes",          "Electronic", "No featured artist", "3:57"),
                                track("High Life",            "Electronic", "No featured artist", "3:21"),
                                track("Something About Us",   "Electronic", "No featured artist", "3:52"),
                                track("Voyager",              "Electronic", "No featured artist", "3:47"),
                                track("Veridis Quo",          "Electronic", "No featured artist", "5:45"),
                                track("Short Circuit",        "Electronic", "No featured artist", "3:26"),
                                track("Face to Face",         "Electronic", "No featured artist", "4:00"),
                                track("Too Long",             "Electronic", "No featured artist", "10:00")
                        )
                );


                // Linkin Park — Hybrid Theory (2000)
                addAlbumWithTracks(em,
                        "Linkin Park", "Band",
                        "Hybrid Theory", LocalDate.of(2000, 10, 24),
                        tracks(
                                track("Papercut",              "Nu Metal", "No featured artist", "3:05"),
                                track("One Step Closer",       "Nu Metal", "No featured artist", "2:36"),
                                track("With You",              "Nu Metal", "No featured artist", "3:23"),
                                track("Points of Authority",   "Nu Metal", "No featured artist", "3:20"),
                                track("Crawling",              "Nu Metal", "No featured artist", "3:29"),
                                track("Runaway",               "Nu Metal", "No featured artist", "3:03"),
                                track("By Myself",             "Nu Metal", "No featured artist", "3:09"),
                                track("In the End",            "Nu Metal", "No featured artist", "3:36"),
                                track("A Place for My Head",   "Nu Metal", "No featured artist", "3:05"),
                                track("Forgotten",             "Nu Metal", "No featured artist", "3:14"),
                                track("Cure for the Itch",     "Instrumental", "No featured artist", "2:37"),
                                track("Pushing Me Away",       "Nu Metal", "No featured artist", "3:12")
                        )
                );


                // Eminem — The Eminem Show (2002)
                addAlbumWithTracks(em,
                        "Eminem", "Solo",
                        "The Eminem Show", LocalDate.of(2002, 5, 26),
                        tracks(
                                track("Curtains Up (Intro)", "Hip Hop", "No featured artist", "0:30"),
                                track("White America", "Hip Hop", "No featured artist", "5:24"),
                                track("Business", "Hip Hop", "No featured artist", "4:12"),
                                track("Cleanin' Out My Closet", "Hip Hop", "No featured artist", "4:57"),
                                track("Square Dance", "Hip Hop", "No featured artist", "5:23"),
                                track("The Kiss (Skit)", "Hip Hop", "No featured artist", "1:15"),
                                track("Soldier", "Hip Hop", "No featured artist", "3:46"),
                                track("Say Goodbye Hollywood", "Hip Hop", "No featured artist", "4:32"),
                                track("Drips", "Hip Hop", "Obie Trice", "4:45"),
                                track("Without Me", "Hip Hop", "No featured artist", "4:50"),
                                track("Paul Rosenberg (Skit)", "Hip Hop", "No featured artist", "0:22"),
                                track("Sing for the Moment", "Hip Hop", "No featured artist", "5:40"),
                                track("Superman", "Hip Hop", "Dina Rae", "5:50"),
                                track("Hailie's Song", "Hip Hop", "No featured artist", "5:20"),
                                track("Steve Berman (Skit)", "Hip Hop", "No featured artist", "0:33"),
                                track("When the Music Stops", "Hip Hop", "D12", "4:29"),
                                track("Say What You Say", "Hip Hop", "Dr. Dre", "5:09"),
                                track("’Till I Collapse", "Hip Hop", "Nate Dogg", "4:57"),
                                track("My Dad’s Gone Crazy", "Hip Hop", "Hailie Jade", "4:27"),
                                track("Curtains Close (Outro)", "Hip Hop", "No featured artist", "1:01")
                        )
                );


                // Kanye West — Graduation (2007)
                addAlbumWithTracks(em,
                        "Kanye West", "Solo",
                        "Graduation", LocalDate.of(2007, 9, 11),
                        tracks(
                                track("Good Morning", "Hip Hop", "No featured artist", "3:15"),
                                track("Champion", "Hip Hop", "No featured artist", "2:48"),
                                track("Stronger", "Hip Hop", "No featured artist", "5:11"),
                                track("I Wonder", "Hip Hop", "No featured artist", "4:03"),
                                track("Good Life", "Hip Hop", "T-Pain", "3:27"),
                                track("Can't Tell Me Nothing", "Hip Hop", "No featured artist", "4:32"),
                                track("Barry Bonds", "Hip Hop", "Lil Wayne", "3:24"),
                                track("Drunk and Hot Girls", "Hip Hop", "Mos Def", "5:13"),
                                track("Flashing Lights", "Hip Hop", "Dwele", "3:58"),
                                track("Everything I Am", "Hip Hop", "No featured artist", "3:48"),
                                track("The Glory", "Hip Hop", "No featured artist", "3:32"),
                                track("Homecoming", "Hip Hop", "Chris Martin", "3:23"),
                                track("Big Brother", "Hip Hop", "No featured artist", "4:47")
                        )
                );



                // Sleep Token — Even in Arcadia (2024)
                addAlbumWithTracks(em,
                        "Sleep Token", "Band",
                        "Even in Arcadia", LocalDate.of(2024, 7, 19),
                        tracks(
                                track("Chokehold", "Alternative Metal", "No featured artist", "5:04"),
                                track("The Summoning", "Alternative Metal", "No featured artist", "6:35"),
                                track("Granite", "Alternative Metal", "No featured artist", "3:45"),
                                track("Aqua Regia", "Alternative Metal", "No featured artist", "3:56"),
                                track("Vore", "Alternative Metal", "No featured artist", "5:42"),
                                track("Ascensionism", "Alternative Metal", "No featured artist", "7:08"),
                                track("Are You Really Okay?", "Alternative Metal", "No featured artist", "5:06"),
                                track("The Apparition", "Alternative Metal", "No featured artist", "4:30"),
                                track("DYWTYLM", "Alternative Metal", "No featured artist", "3:51"),
                                track("Rain", "Alternative Metal", "No featured artist", "4:12"),
                                track("Take Me Back to Eden", "Alternative Metal", "No featured artist", "8:20"),
                                track("Euclid", "Alternative Metal", "No featured artist", "5:13")
                        )
                );



                // Mac Miller — Swimming (2018)
                addAlbumWithTracks(em,
                        "Mac Miller", "Solo",
                        "Swimming", LocalDate.of(2018, 8, 3),
                        tracks(
                                track("Come Back to Earth", "Hip Hop", "No featured artist", "2:41"),
                                track("Hurt Feelings", "Hip Hop", "No featured artist", "4:10"),
                                track("What's the Use?", "Hip Hop", "No featured artist", "4:48"),
                                track("Perfecto", "Hip Hop", "No featured artist", "3:36"),
                                track("Self Care", "Hip Hop", "No featured artist", "5:45"),
                                track("Wings", "Hip Hop", "No featured artist", "4:10"),
                                track("Ladders", "Hip Hop", "No featured artist", "4:47"),
                                track("Small Worlds", "Hip Hop", "No featured artist", "4:31"),
                                track("Conversation Pt. 1", "Hip Hop", "No featured artist", "3:30"),
                                track("Dunno", "Hip Hop", "No featured artist", "3:57"),
                                track("Jet Fuel", "Hip Hop", "No featured artist", "5:45"),
                                track("2009", "Hip Hop", "No featured artist", "5:48"),
                                track("So It Goes", "Hip Hop", "No featured artist", "5:13")
                        )
                );



                // Post Malone — Hollywood's Bleeding (2019)
                addAlbumWithTracks(em,
                        "Post Malone", "Solo",
                        "Hollywood's Bleeding", LocalDate.of(2019, 9, 6),
                        tracks(
                                track("Hollywood’s Bleeding", "Pop Rap", "No featured artist", "2:36"),
                                track("Saint-Tropez", "Pop Rap", "No featured artist", "2:30"),
                                track("Enemies", "Pop Rap", "DaBaby", "3:16"),
                                track("Allergic", "Pop Rap", "No featured artist", "2:36"),
                                track("A Thousand Bad Times", "Pop Rap", "No featured artist", "3:41"),
                                track("Circles", "Pop Rap", "No featured artist", "3:35"),
                                track("Die For Me", "Pop Rap", "Future & Halsey", "4:05"),
                                track("On The Road", "Pop Rap", "Meek Mill & Lil Baby", "3:38"),
                                track("Take What You Want", "Pop Rap", "Ozzy Osbourne & Travis Scott", "3:49"),
                                track("I’m Gonna Be", "Pop Rap", "No featured artist", "3:20"),
                                track("Staring at the Sun", "Pop Rap", "SZA", "2:48"),
                                track("Sunflower", "Pop Rap", "Swae Lee", "2:38"),
                                track("Internet", "Pop Rap", "No featured artist", "2:03"),
                                track("Goodbyes", "Pop Rap", "Young Thug", "2:55"),
                                track("Myself", "Pop Rap", "No featured artist", "2:38"),
                                track("I Know", "Pop Rap", "No featured artist", "2:21"),
                                track("Wow.", "Pop Rap", "No featured artist", "2:29")
                        )
                );



                // Volbeat — Outlaw Gentlemen & Shady Ladies (2013)
                addAlbumWithTracks(em,
                        "Volbeat", "Band",
                        "Outlaw Gentlemen & Shady Ladies", LocalDate.of(2013, 4, 5),
                        tracks(
                                track("Let’s Shake Some Dust", "Heavy Metal", "No featured artist", "1:28"),
                                track("Pearl Hart", "Heavy Metal", "No featured artist", "3:27"),
                                track("The Nameless One", "Heavy Metal", "No featured artist", "3:52"),
                                track("Dead but Rising", "Heavy Metal", "No featured artist", "3:35"),
                                track("Cape of Our Hero", "Heavy Metal", "No featured artist", "3:49"),
                                track("Room 24", "Heavy Metal", "King Diamond", "5:05"),
                                track("The Hangman’s Body Count", "Heavy Metal", "No featured artist", "5:16"),
                                track("My Body", "Heavy Metal", "No featured artist", "3:43"),
                                track("Lola Montez", "Heavy Metal", "No featured artist", "4:28"),
                                track("Black Bart", "Heavy Metal", "No featured artist", "4:49"),
                                track("Lonesome Rider", "Heavy Metal", "Sarah Blackwood", "4:06"),
                                track("The Sinner Is You", "Heavy Metal", "No featured artist", "4:16"),
                                track("Doc Holliday", "Heavy Metal", "No featured artist", "5:47"),
                                track("Our Loved Ones", "Heavy Metal", "No featured artist", "4:51")
                        )
                );



                // Lukas Graham — Lukas Graham (Blue Album) (2015)
                addAlbumWithTracks(em,
                        "Lukas Graham", "Band",
                        "Lukas Graham (Blue Album)", LocalDate.of(2015, 6, 16),
                        tracks(
                                track("7 Years", "Pop", "No featured artist", "3:57"),
                                track("Take the World by Storm", "Pop", "No featured artist", "3:12"),
                                track("Mama Said", "Pop", "No featured artist", "3:26"),
                                track("Happy Home", "Pop", "No featured artist", "3:38"),
                                track("Drunk in the Morning", "Pop", "No featured artist", "3:23"),
                                track("Better Than Yourself (Criminal Mind Pt. 2)", "Pop", "No featured artist", "3:20"),
                                track("Don’t You Worry ’Bout Me", "Pop", "No featured artist", "3:11"),
                                track("What Happened to Perfect", "Pop", "No featured artist", "3:55"),
                                track("Strip No More", "Pop", "No featured artist", "3:26"),
                                track("You’re Not There", "Pop", "No featured artist", "3:21"),
                                track("Funeral", "Pop", "No featured artist", "3:04")
                        )
                );



                // Travis Scott — Astroworld (2018)
                addAlbumWithTracks(em,
                        "Travis Scott", "Solo",
                        "Astroworld", LocalDate.of(2018, 8, 3),
                        tracks(
                                track("STARGAZING", "Hip Hop", "No featured artist", "4:31"),
                                track("CAROUSEL", "Hip Hop", "No featured artist", "3:00"),
                                track("SICKO MODE", "Hip Hop", "No featured artist", "5:12"),
                                track("R.I.P. SCREW", "Hip Hop", "No featured artist", "3:06"),
                                track("STOP TRYING TO BE GOD", "Hip Hop", "No featured artist", "5:38"),
                                track("NO BYSTANDERS", "Hip Hop", "No featured artist", "3:38"),
                                track("SKELETONS", "Hip Hop", "No featured artist", "2:26"),
                                track("WAKE UP", "Hip Hop", "No featured artist", "3:51"),
                                track("5% TINT", "Hip Hop", "No featured artist", "3:16"),
                                track("NC-17", "Hip Hop", "21 Savage", "2:37"),
                                track("ASTROTHUNDER", "Hip Hop", "No featured artist", "2:22"),
                                track("YOSEMITE", "Hip Hop", "No featured artist", "2:30"),
                                track("CAN’T SAY", "Hip Hop", "Don Toliver", "3:18"),
                                track("WHO? WHAT!", "Hip Hop", "Quavo & Takeoff", "2:57"),
                                track("BUTTERFLY EFFECT", "Hip Hop", "No featured artist", "3:10"),
                                track("HOUSTONFORNICATION", "Hip Hop", "No featured artist", "3:38"),
                                track("COFFEE BEAN", "Hip Hop", "No featured artist", "3:29")
                        )
                );



                // Twenty One Pilots — Blurryface (2015)
                addAlbumWithTracks(em,
                        "Twenty One Pilots", "Duo",
                        "Blurryface", LocalDate.of(2015, 5, 17),
                        tracks(
                                track("Heavydirtysoul", "Alternative", "No featured artist", "3:54"),
                                track("Stressed Out", "Alternative", "No featured artist", "3:22"),
                                track("Ride", "Alternative", "No featured artist", "3:34"),
                                track("Fairly Local", "Alternative", "No featured artist", "3:27"),
                                track("Tear in My Heart", "Alternative", "No featured artist", "3:08"),
                                track("Lane Boy", "Alternative", "No featured artist", "4:13"),
                                track("The Judge", "Alternative", "No featured artist", "4:57"),
                                track("Doubt", "Alternative", "No featured artist", "3:11"),
                                track("Polarize", "Alternative", "No featured artist", "3:46"),
                                track("We Don’t Believe What’s on TV", "Alternative", "No featured artist", "2:57"),
                                track("Message Man", "Alternative", "No featured artist", "4:00"),
                                track("Hometown", "Alternative", "No featured artist", "3:54"),
                                track("Not Today", "Alternative", "No featured artist", "3:58"),
                                track("Goner", "Alternative", "No featured artist", "3:56")
                        )
                );



                // URO — Allerhest vil vi elskes
                addAlbumWithTracks(em,
                        "URO", "Solo",
                        "Allerhest vil vi elskes", LocalDate.of(2023, 2, 24),
                        tracks(
                                track("Giv Mig Love", "Pop", "No featured artist", "3:00"),
                                track("Er Det For Sent", "Pop", "No featured artist", "2:50"),
                                track("På Mine Læber", "Pop", "No featured artist", "2:50"),
                                track("Endorfiner", "Pop", "No featured artist", "3:00"),
                                track("Godthåbsvej", "Pop", "No featured artist", "3:17"),
                                track("Når Jeg Går I Storm", "Pop", "No featured artist", "2:05"),
                                track("Lille Blomst", "Pop", "Wads", "3:12"),
                                track("Føles Godt", "Pop", "Mekdes", "2:50"),
                                track("I går Var Jeg Varm", "Pop", "Ramón", "2:19"),
                                track("Bare En Dreng", "Pop", "No featured artist", "3:04"),
                                track("Gemmer På Noget", "Pop", "KOPS", "3:18"),
                                track("Markereret", "Pop", "No featured artist", "2:43")
                        )
                );


                List<Song> allSongs = em.createQuery("SELECT s FROM Song s", Song.class).getResultList();
                Collections.shuffle(allSongs);

                // Jonas' playlist
                Playlist j = Playlist.builder()
                        .playListName("Jonas – Mixed")
                        .owner(jonasProfile)
                        .build();
                allSongs.stream().limit(13).forEach(j.getSongs()::add);
                j.updateTotalDuration();
                em.persist(j);


                // Asgers playlist
                Collections.shuffle(allSongs);
                Playlist a = Playlist.builder()
                        .playListName("Asger – Mixed")
                        .owner(asgerProfile)
                        .build();
                allSongs.stream().limit(12).forEach(a.getSongs()::add);
                a.updateTotalDuration();
                em.persist(a);

                tx.commit();

            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                throw ex;
            } finally {
                em.close();
            }
        }
    }
