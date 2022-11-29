package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Playlist extends Model
{
    Long playlistId;
    String name;

    public Playlist()
    {
    }

    public Playlist(ResultSet results) throws SQLException
    {
        name = results.getString("Name");
        playlistId = results.getLong("PlaylistId");
    }


    public List<Track> getTracks()
    {
        // COMPLETE implement, order by track name
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT tracks.Name AS Name, Milliseconds, Bytes, UnitPrice, tracks.TrackId, tracks.AlbumId, MediaTypeId, GenreId, artists.Name AS ArtistName, Title" +
                             " FROM playlists" +
                             " JOIN playlist_track ON playlists.PlaylistId = playlist_track.PlaylistId" +
                             " JOIN tracks ON playlist_track.TrackId = tracks.TrackId" +
                             " JOIN albums ON tracks.AlbumId = albums.AlbumId" +
                             " JOIN artists ON albums.ArtistId = artists.ArtistId" +
                             " WHERE playlists.PlaylistId LIKE 3" +
                             " ORDER BY tracks.name"))
        {
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();

            while (results.next())
            {
                resultList.add(new Track(results));
            }
            return resultList;
        }
        catch (SQLException sqlException)
        {
            throw new RuntimeException(sqlException);
        }
    }

    public Long getPlaylistId() {
        return playlistId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<Playlist> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Playlist> all(int page, int count)
    {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM playlists LIMIT ? OFFSET ?"))
        {
            int offsetNum = (page - 1) * count; // Page # - One and Multiply By One Hundred --> (i.e. 1 - > 0, 2 - > 100, 3 - > 200, etc.)

            stmt.setInt(1, count);
            stmt.setInt(2, offsetNum);
            ResultSet results = stmt.executeQuery();
            List<Playlist> resultList = new LinkedList<>();

            while (results.next())
            {
                resultList.add(new Playlist(results));
            }
            return resultList;
        }
        catch (SQLException sqlException)
        {
            throw new RuntimeException(sqlException);
        }
    }

    public static Playlist find(int i)
    {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM playlists WHERE PlaylistId=?"))
        {
            stmt.setLong(1, i);
            ResultSet results = stmt.executeQuery();

            if (results.next())
            {
                return new Playlist(results);
            }
            else
            {
                return null;
            }
        }
        catch (SQLException sqlException)
        {
            throw new RuntimeException(sqlException);
        }
    }

}
