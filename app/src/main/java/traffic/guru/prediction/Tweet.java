package traffic.guru.prediction;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardGridView;

public class Tweet extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    static CardGridArrayAdapter mCardArrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_activity_tweet);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Traffic Tweets");
        getTweet();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    public class GplayGridCard extends Card {

        protected String mTitle;
        protected TextView mSecondaryTitle;
        protected RatingBar mRatingBar;
        protected int resourceIdThumbnail = -1;
        protected int count;
        protected String url;

        protected String headerTitle;
        protected String secondaryTitle;
        protected float rating;

        public GplayGridCard(Context context) {
            super(context, R.layout.inner_content_tweet);
        }


        public GplayGridCard(Context context, int innerLayout) {
            super(context, innerLayout);
        }

        private void init() {

            CardHeader header = new CardHeader(getContext());
            header.setButtonOverflowVisible(true);
            header.setTitle(headerTitle);
            header.setPopupMenu(R.menu.popupmain, new CardHeader.OnClickCardHeaderPopupMenuListener() {
                @Override
                public void onMenuItemClick(BaseCard card, MenuItem item) {
                    String selected = card.getId();
//                    ID = card.getId();
                    if (card.getTitle().equals("Info")) {
                        Toast.makeText(getContext(), "No info currently!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            addCardHeader(header);
          /*  OnCardClickListener clickListener = new OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    //Do something
                }
            };

            addPartialOnClickListener(Card.CLICK_LISTENER_CONTENT_VIEW, clickListener);*/
         /*   setOnClickListener(new OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
//                    Do something
                    String selected= card.getId();
                    Toast.makeText(getBaseContext(), "Item ID is" + selected, Toast.LENGTH_LONG).show();
                   *//* Intent intent =new Intent(getBaseContext(),ProductDetail.class);
                    intent.putExtra("id",selected);
                    intent.putExtra("product_name",card.getTitle());
                    startActivity(intent);*//*
                }
            });*/


        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {

            TextView title = (TextView) view.findViewById(R.id.carddemo_gplay_main_inner_subtitle);
            title.setText(mTitle);

            final TextView subtitle = (TextView) view.findViewById(R.id.carddemo_gplay_main_inner_subtitle2);
            subtitle.setText(secondaryTitle);
//            subtitle.setTextIsSelectable(true);
            subtitle.setClickable(true);
            subtitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   /* ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setText(subtitle.getText());
//                    Toast.makeText(getContext(), "Copied to clipboard!", Toast.LENGTH_SHORT).show();
                    MyShortcuts.showToast("Copied to clipboard!", getBaseContext());
                *//*    Intent intent = new Intent(getContext(), EditPatient.class);
                    intent.putExtra("ID", getId());
                    startActivity(intent);*//*
//                    getParentCard().getId();
                    subtitle.getText();*/
                }
            });


        }
    }

    private void getTweet(){

        Post.getData(MyShortcuts.tweetURL(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    ArrayList<Card> cards = new ArrayList<Card>();
//                    Log.e("response", response);
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("statuses");
                    Log.e("Top",jsonObject.toString());

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject1= jsonArray.getJSONObject(i);

                        JSONObject jsonObject2= jsonObject1.getJSONObject("user");


                        GplayGridCard card = new GplayGridCard(getBaseContext());
                        CardThumbnail thumb = new CardThumbnail(getBaseContext());

                        //Set URL resource
//                        thumb.setDrawableResource();
                    thumb.setUrlResource(jsonObject2.getString("profile_image_url"));
                        card.secondaryTitle = jsonObject1.getString("text");
                        card.mTitle=jsonObject2.getString("screen_name");
                        thumb.setErrorResource(R.drawable.user);

                        //Add thumbnail to a card
                        card.addCardThumbnail(thumb);

                        card.init();
                        cards.add(card);



//                Toast.makeText(MainActivity.this , "Successfully loaded the data" , Toast.LENGTH_LONG).show();
                        //finish();
                    }
                    mCardArrayAdapter = new CardGridArrayAdapter(getBaseContext(), cards);

                    CardGridView listView = (CardGridView) findViewById(R.id.card_grid);
                    if (listView != null) {
                        listView.setAdapter(mCardArrayAdapter);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.prediction) {
            Intent intent = new Intent(getBaseContext(),TrafficPredictionActivity.class);
            startActivity(intent);
        } else if (id == R.id.twitter) {
            Intent intent = new Intent(getBaseContext(),Tweet.class);
            startActivity(intent);

        } else if (id == R.id.graph) {
            Intent intent = new Intent(getBaseContext(),Graph.class);
            startActivity(intent);

        }

       /* } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
