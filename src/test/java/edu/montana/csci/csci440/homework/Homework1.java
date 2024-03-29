package edu.montana.csci.csci440.homework;

import edu.montana.csci.csci440.DBTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class Homework1 extends DBTest {

    @Test
    /*
     * Write a query in the string below that returns all artists that have an 'A' in their name
     */
    void selectArtistsWhoseNameHasAnAInIt(){
        List<Map<String, Object>> results = executeSQL("SELECT * FROM artists WHERE artists.name LIKE '%A%'");
        assertEquals(211, results.size());
    }

    @Test
    /*
     * Write a query in the string below that returns all artists that have more than one album
     */
    void selectAllArtistsWithMoreThanOneAlbum(){
        List<Map<String, Object>> results = executeSQL(
                "SELECT artists.ArtistId, Name, Count(*) as AlbumCount FROM artists JOIN albums on artists.ArtistId = albums.ArtistId GROUP BY artists.ArtistId HAVING AlbumCount > 1");

        assertEquals(56, results.size());
        assertEquals("AC/DC", results.get(0).get("Name"));
    }

    @Test
        /*
         * Write a query in the string below that returns all tracks longer than six minutes along with the
         * album and artist name
         */
    void selectTheTrackAndAlbumAndArtistForAllTracksLongerThanSixMinutes() {
        List<Map<String, Object>> results = executeSQL(
                "SELECT\n" +
                        "    sum(tracks.Milliseconds) as Milliseconds,\n" +
                        "    tracks.Name as TrackName,\n" +
                        "    albums.Title as AlbumTitle,\n" +
                        "    artists.Name as ArtistsName\n" +
                        "FROM\n" +
                        "    tracks\n" +
                        "JOIN\n" +
                        "    albums on tracks.AlbumId = albums.AlbumId\n" +
                        "JOIN\n" +
                        "    artists on albums.ArtistId = artists.ArtistId\n" +
                        "GROUP BY\n" +
                        "    tracks.TrackId\n" +
                        "HAVING\n" +
                        "    Milliseconds > (6 * 60 * 1000)");

        assertEquals(623, results.size());

        // For now just get the count right, we'll do more elaborate stuff when we get
        // to ORDER BY
        //
        //
//        assertEquals("Princess of the Dawn", results.get(0).get("TrackName"));
//        assertEquals("Restless and Wild", results.get(0).get("AlbumTitle"));
//        assertEquals("Accept", results.get(0).get("ArtistsName"));
//
//        assertEquals("Snoopy's search-Red baron", results.get(10).get("TrackName"));
//        assertEquals("The Best Of Billy Cobham", results.get(10).get("AlbumTitle"));
//        assertEquals("Billy Cobham", results.get(10).get("ArtistsName"));

    }

}
