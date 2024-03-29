package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Track extends Model
{
    private Long trackId;
    private Long albumId;
    private Long mediaTypeId;
    private Long genreId;
    private String name;
    private String artistName;
    private String albumTitle;
    private Long milliseconds;
    private Long bytes;
    private BigDecimal unitPrice;
    public static final String REDIS_CACHE_KEY = "cs440-tracks-count-cache";

    public Track()
    {
        mediaTypeId = 1l;
        genreId = 1l;
        milliseconds  = 0l;
        bytes  = 0l;
        unitPrice = new BigDecimal("0");
    }

    public Track(ResultSet results) throws SQLException
    {
        name = results.getString("Name");
        milliseconds = results.getLong("Milliseconds");
        bytes = results.getLong("Bytes");
        unitPrice = results.getBigDecimal("UnitPrice");
        trackId = results.getLong("TrackId");
        albumId = results.getLong("AlbumId");
        mediaTypeId = results.getLong("MediaTypeId");
        genreId = results.getLong("GenreId");
        artistName = results.getString("ArtistName");
        albumTitle = results.getString("Title");
    }

    public static Track find(long i)
    {
        //SELECT -> tracks.Name AS Name, Milliseconds, Bytes, UnitPrice, TrackId, tracks.AlbumId, MediaTypeId, GenreId, artists.Name AS ArtistName, Title
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT tracks.Name AS Name, Milliseconds, Bytes, UnitPrice, TrackId, tracks.AlbumId, MediaTypeId, GenreId, artists.Name AS ArtistName, Title" +
                     " FROM tracks" +
                     " JOIN albums ON tracks.AlbumId = albums.AlbumId" +
                     " JOIN artists ON albums.ArtistId = artists.ArtistId" +
                     " WHERE TrackId=?"))
        {
            stmt.setLong(1, i);

            ResultSet results = stmt.executeQuery();
            if (results.next())
            {
                return new Track(results);
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

    public static Long count()
    {
         Jedis redisClient = new Jedis(); // use this class to access redis and create a cache

         String nullTemp = "gamer";
         String currCacheVal = redisClient.get(REDIS_CACHE_KEY);

         if (currCacheVal == null || currCacheVal.equals(nullTemp)) // Create a fake Null
         {
            redisClient.set(REDIS_CACHE_KEY, String.valueOf(queryCount()));
            currCacheVal = redisClient.get(REDIS_CACHE_KEY);
         }

         return Long.parseLong(currCacheVal);
    }

    public static Long queryCount()
    {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) as Count FROM tracks"))
        {
            ResultSet results = stmt.executeQuery();
            if (results.next())
            {
                return results.getLong("Count");
            }
            else
            {
                throw new IllegalStateException("Should find a count!");
            }
        }
        catch (SQLException sqlException)
        {
            throw new RuntimeException(sqlException);
        }
    }

    public Album getAlbum() {
        return Album.find(albumId);
    }
    public MediaType getMediaType() {
        return null;
    }
    public Genre getGenre() {
        return null;
    }
    public List<Playlist> getPlaylists()
    {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM tracks" +
                             " JOIN playlist_track ON tracks.TrackId = playlist_track.TrackId" +
                             " JOIN playlists ON playlist_track.PlaylistId = playlists.PlaylistId" +
                             " WHERE tracks.TrackId=?"))
        {
            stmt.setLong(1, this.getTrackId());

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

    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

    public Long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(Long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) { this.albumId = albumId; }

    public void setAlbum(Album album) {
        albumId = album.getAlbumId();
    }

    public Long getMediaTypeId() {
        return mediaTypeId;
    }

    public void setMediaTypeId(Long mediaTypeId) {
        this.mediaTypeId = mediaTypeId;
    }

    public Long getGenreId() {
        return genreId;
    }

    public void setGenreId(Long genreId) {
        this.genreId = genreId;
    }

    // COMPLETE implement more efficiently
    //  hint: cache on this model object
    public String getArtistName() { return artistName; }

    // COMPLETE implement more efficiently
    //  hint: cache on this model object
    public String getAlbumTitle() { return albumTitle; }

    public static List<Track> advancedSearch(int page, int count,
                                             String search, Integer artistId, Integer albumId,
                                             Integer maxRuntime, Integer minRuntime) {
        LinkedList<Object> args = new LinkedList<>();

        String query = "SELECT tracks.Name AS Name, Milliseconds, Bytes, UnitPrice, TrackId, tracks.AlbumId, MediaTypeId, GenreId, artists.Name AS ArtistName, Title" +
                " FROM tracks" +
                " JOIN albums ON tracks.AlbumId = albums.AlbumId" +
                " JOIN artists ON albums.ArtistId = artists.ArtistId" +
                " WHERE tracks.name LIKE ?";
        args.add("%" + search + "%");

        // Here is an example of how to conditionally
        if (artistId != null) {
            query += " AND artists.ArtistId=?";
            args.add(artistId);
        }
        if (albumId != null) {
            query += " AND albums.AlbumId=? ";
            args.add(albumId);
        }
        if (maxRuntime != null) {
            query += " AND Milliseconds<?";
            args.add(maxRuntime);
        }
        if (minRuntime != null) {
            query += " AND Milliseconds>?";
            args.add(minRuntime);
        }
        //  include the limit (you should include the page too :)
        // Page # - One and Multiply By One Hundred --> (i.e. 1 - > 0, 2 - > 100, 3 - > 200, etc.)
        int offsetNum = (page-1) * count;

        query += " LIMIT ?";
        args.add(count);
        query += " OFFSET ?";
        args.add(offsetNum);

        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query))
        {
            for (int i = 0; i < args.size(); i++)
            {
                Object arg = args.get(i);
                stmt.setObject(i + 1, arg);
            }
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

    public static List<Track> search(int page, int count, String orderBy, String search)
    {
        String query = "SELECT tracks.Name AS Name, Milliseconds, Bytes, UnitPrice, TrackId, tracks.AlbumId, MediaTypeId, GenreId, artists.Name AS ArtistName, Title " +
                       "FROM tracks " +
                       "JOIN albums ON tracks.AlbumId = albums.AlbumId " +
                       "JOIN artists ON albums.ArtistId = artists.ArtistId " +
                       "WHERE ArtistName LIKE ? LIMIT ?";
        search = "%" + search + "%";

        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query))
        {
            stmt.setString(1, search);
            stmt.setInt(2, count);

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

    public static List<Track> forAlbum(Long albumId)
    {
        String query = "SELECT * FROM tracks WHERE AlbumId=?";

        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query))
        {
            stmt.setLong(1, albumId);

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

    // Sure would be nice if java supported default parameter values
    public static List<Track> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Track> all(int page, int count) {
        return all(page, count, "TrackId");
    }

    public static List<Track> all(int page, int count, String orderBy)
    {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT tracks.Name AS Name, Milliseconds, Bytes, UnitPrice, TrackId, tracks.AlbumId, MediaTypeId, GenreId, artists.Name AS ArtistName, Title" +
                             " FROM tracks" +
                             " JOIN albums ON tracks.AlbumId = albums.AlbumId" +
                             " JOIN artists ON albums.ArtistId = artists.ArtistId" +
                             " ORDER BY " + orderBy + " LIMIT ? OFFSET ?"))
        {
            int offsetNum = (page - 1) * count; // Page # - One and Multiply By One Hundred --> (i.e. 1 - > 0, 2 - > 100, 3 - > 200, etc.)
            stmt.setInt(1, count);
            stmt.setInt(2, offsetNum);

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

    @Override
    public boolean verify()
    {
        _errors.clear(); // clear any existing errors
        if (name == null || "".equals(name)) {
            addError("Track name can't be null or blank!");
        }
        if (albumId == null || "".equals(albumId)) {
            addError("Album can't be null or blank!");
        }
        return !hasErrors();
    }

    @Override
    public boolean update()
    {
        if (verify())
        {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE tracks SET Name=?, MediaTypeId=?, GenreId=?, Milliseconds=?, Bytes=?, UnitPrice=? WHERE TrackId=?"))
            {
                stmt.setString(1, this.getName());
                stmt.setLong(2, this.getMediaTypeId());
                stmt.setLong(3, this.getGenreId());
                stmt.setLong(4, this.getMilliseconds());
                stmt.setLong(5, this.getBytes());
                stmt.setBigDecimal(6, this.getUnitPrice());
                stmt.setLong(7, this.getTrackId());

                stmt.executeUpdate();
                return true;
            }
            catch (SQLException sqlException)
            {
                throw new RuntimeException(sqlException);
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean create()
    {
        if (verify())
        {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO tracks (Name, MediaTypeId, GenreId, Milliseconds, Bytes, UnitPrice, AlbumId) VALUES (?, ?, ?, ?, ?, ?, ?)"))
            {

                stmt.setString(1, getName());
                stmt.setLong(2, getMediaTypeId());
                stmt.setLong(3, getGenreId());
                stmt.setLong(4, getMilliseconds());
                stmt.setLong(5, getBytes());
                stmt.setBigDecimal(6, getUnitPrice());
                stmt.setLong(7, getAlbumId());

                stmt.executeUpdate();
                trackId = DB.getLastID(conn);

                Jedis redisClient = new Jedis();
                redisClient.del(REDIS_CACHE_KEY);
                redisClient.set(REDIS_CACHE_KEY, "gamer");

                return true;
            }
            catch (SQLException sqlException)
            {
                throw new RuntimeException(sqlException);
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public void delete()
    {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM tracks WHERE TrackId=?"))
        {
            stmt.setLong(1, getTrackId());

            stmt.executeUpdate();
        }
        catch (SQLException sqlException)
        {
            throw new RuntimeException(sqlException);
        }
    }
}
