package restassured;


import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.config.Populate;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PhonyfyApiTest {

    private static Javalin app;
    private static String adminToken;

    @BeforeAll
    static void setup() {
        HibernateConfig.setTest(true);

        ApplicationConfig.startServer(7076);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7076;
        RestAssured.basePath = "/api";

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        Populate.seed(emf);

        adminToken = given()
                .contentType("application/json")
                .body("{\"username\":\"asger\",\"password\":\"asgerpw\"}")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");

    }

    @Test
    @Order(1)
    void getSongs() {
        given()
                .when().get("/songs")
                .then()
                .statusCode(200)
                .log().all()
                .body("songs.size()", greaterThan(0));
    }

    @Test
    @Order(2)
    void getSongById() {
        given()
                .when().get("/songs/1")
                .then()
                .statusCode(200)
                .log().all()
                .body("songId", equalTo(1))
                .body("songName", notNullValue());
    }

    @Test
    @Order(3)
    void createArtist() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body("""
                        {
                          "artistName": "Rest Assured",
                          "type": "Rest Assured Type"
                        }
                        """)
                .when()
                .post("/artists")
                .then()
                .statusCode(201)
                .body("artistName", equalTo("Rest Assured"))
                .body("type", equalTo("Rest Assured Type"))
                .body("id", notNullValue());

    }

    @Test
    @Order(4)
    void createAlbum() {

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body("""
                                      {
                           "albumName": "Rest Assured Album",
                           "releaseDate": "2025-10-24",
                           "totalDuration": "00:00:00"
                         }
                        """)
                .when()
                .post("/artists/1/albums")
                .then()
                .statusCode(201)
                .body("albumName", equalTo("Rest Assured Album"))
                .body("releaseDate", equalTo(Arrays.asList(2025, 10, 24)))
                .body("totalDuration", equalTo("0:00"))
                .body("id", notNullValue());
    }


    @Test
    @Order(5)
    void createSong() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body("""
                                      {
                                        "songName": "Rest Assured Song",
                                        "albumName": "Rest Assured Album",
                                        "mainArtistName": "Daft Punk",
                                        "genre": "REST Assured Genre",
                                        "duration": "04:20"
                                      }
                        """)
                .when()
                .post("/songs")
                .then()
                .statusCode(201)
                .body("songName", equalTo("Rest Assured Song"))
                .body("albumName", equalTo("Rest Assured Album"))
                .body("mainArtistName", equalTo("Daft Punk"))
                .body("genre", equalTo("REST Assured Genre"))
                .body("duration", equalTo("04:20"))
                .body("songId", notNullValue());

    }

    @Test
    @Order(6)
    void getPlaylistFromUsername() {
        String username = "jonas";
        given()
                .when().get("/playlists/user/" + username)
                .then()
                .statusCode(200)
                .log().all()
                .body("playlists.size()", greaterThan(0))
                .body("totalDuration", notNullValue());
    }

    @Test
    @Order(7)
    void createPlaylist() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body("""
                                       {
                                         "playListName": "Rest Assured Vibes",
                                         "ownerUsername": "asger",
                                         "songIds": [1, 2, 3]
                                       }
                        """)
                .when()
                .post("/playlists")
                .then()
                .statusCode(201)
                .body("playListName", equalTo("Rest Assured Vibes"))
                .body("ownerUsername", equalTo("asger"))
                .body("songIds.size()", equalTo(3));
    }

    @Test
    @Order(8)
    void updatePlaylist() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body("""
                                       {
                                         "playListName": "Updated Rest Assured Vibes",
                                         "ownerUsername": "asger",
                                         "songIds": [2, 4]
                                       }
                        """)
                .when()
                .put("/playlists/{id}", 3)
                .then()
                .statusCode(200)
                .body("playListName", equalTo("Updated Rest Assured Vibes"))
                .body("ownerUsername", equalTo("asger"))
                .body("songIds.size()", equalTo(2));

    }

    @Test
    @Order(9)
    void deletePlaylist() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .when()
                .delete("/playlists/{id}", 3)
                .then()
                // Status code 204 means that there's no response body to send back - code is running like it should.
                .statusCode(204);
    }

}
