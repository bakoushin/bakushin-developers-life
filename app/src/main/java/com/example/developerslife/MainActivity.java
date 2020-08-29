package com.example.developerslife;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();

    private ImageView imageView;
    private TextView textView;
    private Button buttonPrev;
    private Button buttonNext;
    private TextView emptyFeed;
    private LinearLayout networkError;

    private RequestQueue queue;
    private ArrayList<History> sections;
    private History history;

    private boolean isNetworkError;
    private boolean isLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        buttonPrev = findViewById(R.id.buttonPrev);
        buttonNext = findViewById(R.id.buttonNext);
        emptyFeed = findViewById(R.id.textViewEmptyFeed);
        networkError = findViewById(R.id.networkError);

        queue = Volley.newRequestQueue(this);

        sections = new ArrayList<>();
        sections.add(new History("https://developerslife.ru/latest/"));
        sections.add(new History("https://developerslife.ru/top/"));
        sections.add(new History("https://developerslife.ru/hot/"));
        history = sections.get(0);

        setControlsState();
        fetchNext(history);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                emptyFeed.setVisibility(View.GONE);

                history = sections.get(tab.getPosition());
                if (history.empty()) {
                    fetchNext(history);
                } else {
                    Item item = history.current();
                    loadItem(item);
                }
                setControlsState();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    public void prevClick(View view) {
        Item item = history.prev();
        loadItem(item);
        setControlsState();
    }

    public void nextClick(View view) {
        if (history.isAtEnd()) {
            fetchNext(history);
        } else {
            Item item = history.next();
            loadItem(item);
        }
        setControlsState();
    }

    public void retry(View view) {
        isNetworkError = false;
        setControlsState();
        fetchNext(history);
    }

    private void loadItem(final Item item) {
        isLoading = true;
        setControlsState();

        imageView.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);

        textView.setText("");

        Glide.with(imageView.getContext())
            .load(item.getUrl())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable final GlideException e,
                                                final Object model, final Target<Drawable> target,
                                                final boolean isFirstResource) {
                        isNetworkError = true;
                        setControlsState();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(final Drawable resource,
                                                   final Object model,
                                                   final Target<Drawable> target,
                                                   final DataSource dataSource,
                                                   final boolean isFirstResource) {
                        textView.setText(item.getTitle());
                        isNetworkError = false;
                        isLoading = false;
                        setControlsState();
                        return false;
                    }
                })
                .into(imageView);
    }

    private void fetchNext(final History history) {
        isLoading = true;

        imageView.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);

        int pageNumber = (int) (Math.max(0, history.size()) / 5);
        String url = history.getApiUrl() + pageNumber + "?json=true";

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try {
                        JSONArray result = jsonObject.getJSONArray("result");
                        if (result.length() > 0) {
                            for (int i = 0; i < result.length(); i++) {
                                JSONObject resultItem = result.getJSONObject(i);
                                String url = resultItem.getString("gifURL").replaceFirst("(?i)^http://", "https://");
                                String title = resultItem.getString("description");
                                Item item = new Item(url, title);
                                history.add(item);
                                if (i == 0) {
                                    loadItem(history.next());
                                    setControlsState();
                                } else {
                                    preloadImage(url);
                                }
                            }
                        } else {
                            if (history.empty()) {
                                emptyFeed.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(getApplicationContext(),
                                    "No more items",
                                    Toast.LENGTH_LONG).show();
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    Log.e(TAG, e.toString());
                    isNetworkError = true;
                    setControlsState();
                }
            });
        queue.add(request);
    }

    private void preloadImage(String url) {
        Glide.with(imageView.getContext()).load(url).preload();
    }

    private void setControlsState() {
        buttonPrev.setEnabled(!isLoading && !history.isAtStart());
        buttonNext.setEnabled(!isLoading && !history.empty());

        if (isNetworkError) {
            networkError.setVisibility(View.VISIBLE);
        } else {
            networkError.setVisibility(View.GONE);
        }
    }
}