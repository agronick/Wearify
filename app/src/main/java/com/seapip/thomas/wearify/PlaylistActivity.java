package com.seapip.thomas.wearify;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.wearable.view.WearableRecyclerView;
import android.support.wearable.view.drawer.WearableActionDrawer;
import android.support.wearable.view.drawer.WearableDrawerLayout;
import android.support.wearable.view.drawer.WearableNavigationDrawer;
import android.widget.ImageView;

import com.seapip.thomas.wearify.Browse.ActionButtonSmall;
import com.seapip.thomas.wearify.Browse.Activity;
import com.seapip.thomas.wearify.Browse.Adapter;
import com.seapip.thomas.wearify.Browse.Header;
import com.seapip.thomas.wearify.Browse.Item;
import com.seapip.thomas.wearify.Browse.Loading;
import com.seapip.thomas.wearify.Spotify.Callback;
import com.seapip.thomas.wearify.Spotify.Manager;
import com.seapip.thomas.wearify.Spotify.Paging;
import com.seapip.thomas.wearify.Spotify.Playlist;
import com.seapip.thomas.wearify.Spotify.PlaylistTrack;
import com.seapip.thomas.wearify.Spotify.Service;
import com.seapip.thomas.wearify.Spotify.User;
import com.seapip.thomas.wearify.Spotify.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

import static com.seapip.thomas.wearify.Spotify.Util.largestImageUrl;
import static com.seapip.thomas.wearify.Spotify.Util.songCount;

public class PlaylistActivity extends Activity {

    private String mUri;
    private WearableRecyclerView mRecyclerView;
    private ArrayList<Item> mItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_background);

        setDrawers((WearableDrawerLayout) findViewById(R.id.drawer_layout),
                (WearableNavigationDrawer) findViewById(R.id.top_navigation_drawer),
                (WearableActionDrawer) findViewById(R.id.bottom_action_drawer));

        mRecyclerView = (WearableRecyclerView) findViewById(R.id.content);
        setGradientOverlay(mRecyclerView, (ImageView) findViewById(R.id.background_overlay));

        final ImageView backgroundImage = (ImageView) findViewById(R.id.background_image);
        mItems = new ArrayList<>();
        mItems.add(new Header(""));
        ActionButtonSmall shuffle = new ActionButtonSmall();
        shuffle.icon = getDrawable(R.drawable.ic_shuffle_black_24dp);
        shuffle.iconColor = Color.argb(180, 0, 0, 0);
        shuffle.backgroundColor = Color.parseColor("#00ffe0");
        shuffle.text = "Shuffle Play";
        mItems.add(shuffle);
        mItems.add(new Loading(Color.parseColor("#00ffe0")));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new Adapter(this, mItems));
        mUri = getIntent().getStringExtra("uri");
        Manager.getService(new Callback() {
            @Override
            public void onSuccess(Service service) {
                Call<Playlist> call = service.getPlaylist(mUri.split(":")[2], mUri.split(":")[4],
                        "name,images,uri,owner.id,tracks.items(track.artists(name),track.name,track.uri),tracks.total,tracks.offset",
                        "from_token");
                call.enqueue(new retrofit2.Callback<Playlist>() {
                    @Override
                    public void onResponse(Call<Playlist> call, Response<Playlist> response) {
                        if (response.isSuccessful()) {
                            final Playlist playlist = response.body();
                            mItems.remove(2);
                            mItems.get(0).title = playlist.name;
                            mItems.get(0).subTitle = "Playlist";
                            Manager.getService(new Callback() {
                                @Override
                                public void onSuccess(Service service) {
                                    Call<User> call = service.getUser(playlist.owner.id);
                                    call.enqueue(new retrofit2.Callback<User>() {
                                        @Override
                                        public void onResponse(Call<User> call, Response<User> response) {
                                            if (response.isSuccessful()) {
                                                User user = response.body();
                                                mItems.get(0).subTitle = "by ";
                                                if (user.display_name != null) {
                                                    mItems.get(0).subTitle += user.display_name;
                                                } else {
                                                    mItems.get(0).subTitle += user.id;
                                                }
                                                mItems.get(0).subTitle += " • " + songCount(playlist.tracks.total);
                                                mRecyclerView.getAdapter().notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<User> call, Throwable t) {

                                        }
                                    });
                                }
                            });
                            boolean charts = playlist.owner.id.equals("spotifycharts");
                            addTracks(playlist.tracks.items, 0, charts);
                            Picasso.with(getApplicationContext())
                                    .load(largestImageUrl(playlist.images))
                                    .fit().into(backgroundImage);
                            if (playlist.tracks.total > playlist.tracks.items.length) {
                                getTracks(100, playlist.tracks.items.length, charts);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Playlist> call, Throwable t) {

                    }
                });
            }
        });
    }

    private void getTracks(final int limit, final int offset, final boolean charts) {
        Manager.getService(new Callback() {
            @Override
            public void onSuccess(Service service) {
                Call<Paging<PlaylistTrack>> call = service.getPlaylistTracks(mUri.split(":")[2],
                        mUri.split(":")[4],
                        "items(track.artists(name),track.name,track.uri),total,offset", limit,
                        offset, "from_token");
                call.enqueue(new retrofit2.Callback<Paging<PlaylistTrack>>() {
                    @Override
                    public void onResponse(Call<Paging<PlaylistTrack>> call, Response<Paging<PlaylistTrack>> response) {
                        if (response.isSuccessful()) {
                            Paging<PlaylistTrack> playlistTracks = response.body();
                            if (playlistTracks.items != null) {
                                addTracks(playlistTracks.items, offset, charts);
                                if (playlistTracks.total > playlistTracks.offset + limit) {
                                    getTracks(limit, playlistTracks.offset + limit, charts);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Paging<PlaylistTrack>> call, Throwable t) {

                    }
                });
            }
        });
    }

    private void addTracks(PlaylistTrack[] playlistTracks, int offset, boolean charts) {
        for (PlaylistTrack playlistTrack : playlistTracks) {
            Item item = new Item();
            item.setTrack(playlistTrack.track);
            if (charts) {
                offset++;
                item.number = offset;
            }
            mItems.add(item);
        }
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }
}