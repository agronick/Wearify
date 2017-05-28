package com.seapip.thomas.wearify.Spotify;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Service {
    @GET("me/player/recently-played")
    Call<CursorPaging<PlayHistory>> getRecentPlayed(@Query("limit") int limit);

    @GET("me/tracks")
    Call<Paging<SavedTrack>> getTracks(@Query("limit") int limit,
                                       @Query("offset") int offset,
                                       @Query("market") String market);

    @GET("me/albums")
    Call<Paging<SavedAlbum>> getAlbums(@Query("limit") int limit,
                                       @Query("offset") int offset,
                                       @Query("market") String market);

    @GET("me/playlists")
    Call<Paging<Playlist>> getPlaylists(@Query("limit") int limit,
                                        @Query("offset") int offset);

    @GET("users/{user_id}/playlists/{playlist_id}")
    Call<Playlist> getPlaylist(@Path("user_id") String userId,
                               @Path("playlist_id") String playlistId,
                               @Query("fields") String fields,
                               @Query("market") String market);

    @GET("users/{user_id}/playlists/{playlist_id}/tracks")
    Call<Paging<PlaylistTrack>> getPlaylistTracks(@Path("user_id") String userId,
                                                  @Path("playlist_id") String playlistId,
                                                  @Query("fields") String fields,
                                                  @Query("limit") int limit,
                                                  @Query("offset") int offset,
                                                  @Query("market") String market);

    @GET("albums/{id}")
    Call<Album> getAlbum(@Path("id") String id,
                         @Query("market") String market);

    @GET("albums/{id}/tracks")
    Call<Paging<Track>> getAlbumTracks(@Path("id") String id,
                                       @Query("limit") int limit,
                                       @Query("offset") int offset,
                                       @Query("market") String market);

    @GET("users/{user_id}")
    Call<User> getUser(@Path("user_id") String userId);

    @GET("artists")
    Call<Artists> getArtists(@Query("ids") String ids);

    @PUT("me/player/play")
    Call<Void> play(@Body Play play);

    @PUT("me/player/shuffle")
    Call<Void> shuffle(@Query("state") boolean state,
                       @Query("device_id") String device_id);
}